#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Dreaming Engine for NRO Project.
Performs offline memory consolidation: inspects git diff / traces,
extracts learned invariants, and updates modular GEMINI.md files safely.
"""

import sys
import os
import re
import subprocess
from pathlib import Path

# Force UTF-8 stdout
if sys.stdout.encoding != 'utf-8':
    try:
        sys.stdout.reconfigure(encoding='utf-8')
    except Exception:
        pass

PROJECT_ROOT = Path(__file__).resolve().parent.parent.parent

# Import guardrails from the same directory
sys.path.insert(0, str(Path(__file__).resolve().parent))
from guardrails import MemoryLock, validate_gemini_file, MAX_LINES_PER_GEMINI

MODULE_MAP = {
    "src/services/": "src/services/GEMINI.md",
    "src/models/player/": "src/models/player/GEMINI.md",
    "src/models/map/": "src/models/map/GEMINI.md",
    "src/models/boss/": "src/models/boss/GEMINI.md",
    "src/models/task/": "src/models/task/GEMINI.md",
    "src/network/": "src/network/GEMINI.md",
    "src/server/": "src/network/GEMINI.md",
    "src/database/": "src/database/GEMINI.md",
    "Client/": "Client/Client/Assets/Scripts/GEMINI.md"
}

HEURISTIC_PATTERNS = [
    {
        "regex": r"(synchronized|Lock|ReentrantLock)",
        "section": "## 3. Các Bất Biến Bắt Buộc",
        "category": "Concurrency & Anti-Dupe Lock",
        "template": "- Khóa Đồng Bộ Luồng: Luôn đảm bảo lock trạng thái khi thao tác đa luồng hoặc spam packet để chống bug dupe."
    },
    {
        "regex": r"\b(long)\b\s+(hp|damage|dame|point|tiemNang|sucManh|exp|money|xu|vang)",
        "section": "## 3. Các Bất Biến Bắt Buộc",
        "category": "Numeric Overflow Guard",
        "template": "- Phòng Ngừa Tràn Số (Numeric Overflow): Luôn sử dụng kiểu `long` cho các trường tích lũy (HP, sức mạnh, tiền, kinh nghiệm) để tránh tràn mốc 2 tỷ."
    },
    {
        "regex": r"try\s*\(\s*Connection\s+",
        "section": "## 3. Các Bất Biến Bắt Buộc",
        "category": "Database Auto-Close Resource",
        "template": "- Đóng Tài Nguyên DB Tự Động: 100% truy vấn DB phải bọc trong try-with-resources để giải phóng connection về HikariCP pool."
    },
    {
        "regex": r"writeByte|readByte|writeShort|readShort|writeInt|readInt|writeUTF",
        "section": "## 3. Các Bất Biến Bắt Buộc",
        "category": "Binary Protocol Alignment 1:1",
        "template": "- Đồng Bộ Gói Tin Nhị Phân 1:1: Mọi thứ tự write/read packet giữa Server Java và Client Unity C# phải hoàn toàn tương ứng."
    }
]


def run_git(cmd_args):
    """Executes a git command and returns stdout text."""
    try:
        res = subprocess.run(
            ["git"] + cmd_args,
            cwd=str(PROJECT_ROOT),
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
            encoding="utf-8",
            errors="ignore"
        )
        return res.stdout.strip()
    except Exception as e:
        return ""


def get_recent_diff():
    """Gets diff from working tree (uncommitted) or last commit (HEAD~1)."""
    # First check working tree changes
    diff = run_git(["diff", "HEAD"])
    if not diff:
        diff = run_git(["diff", "HEAD~1"])
    return diff


def detect_affected_modules(diff_text):
    """Finds which GEMINI.md modules correspond to the files modified in diff."""
    affected = {}
    for line in diff_text.splitlines():
        if line.startswith("+++ b/"):
            file_path = line[6:].replace("\\", "/")
            for prefix, module_doc in MODULE_MAP.items():
                if file_path.startswith(prefix):
                    if module_doc not in affected:
                        affected[module_doc] = []
                    affected[module_doc].append(file_path)
                    break
    return affected


def analyze_diff_for_invariants(diff_text):
    """Analyzes added lines (+) in diff to find invariant patterns."""
    findings = []
    added_lines = [l[1:].strip() for l in diff_text.splitlines() if l.startswith("+") and not l.startswith("+++")]
    added_chunk = "\n".join(added_lines)

    for item in HEURISTIC_PATTERNS:
        if re.search(item["regex"], added_chunk, re.IGNORECASE):
            findings.append(item)
    return findings


def apply_patch_to_module(module_rel_path, new_bullet, section_name, dry_run=True):
    """Inserts a new bullet point under the given section with concurrency lock and guardrail checks."""
    full_path = PROJECT_ROOT / module_rel_path
    if not full_path.exists():
        print(f"[ERROR] Không tìm thấy file {module_rel_path}")
        return False

    with MemoryLock():
        content = full_path.read_text(encoding="utf-8")
        lines = content.splitlines()

        # Check for duplication
        norm_bullet = re.sub(r"[^\w]", "", new_bullet.lower())
        for l in lines:
            if norm_bullet in re.sub(r"[^\w]", "", l.lower()):
                print(f"  [SKIP] Quy tắc tương tự đã tồn tại trong {module_rel_path}:")
                print(f"         > {l}")
                return True

        # Check line limit before adding
        if len(lines) + 2 > MAX_LINES_PER_GEMINI:
            print(f"  [WARN] File {module_rel_path} đã đạt {len(lines)}/{MAX_LINES_PER_GEMINI} dòng. Cần Pruning trước khi thêm!")
            return False

        # Find target section
        sec_idx = -1
        for i, l in enumerate(lines):
            if l.strip() == section_name.strip():
                sec_idx = i
                break

        if sec_idx == -1:
            print(f"  [ERROR] Không tìm thấy mục '{section_name}' trong {module_rel_path}")
            return False

        # Find where section ends (next '## ' or end of file)
        insert_idx = len(lines)
        for i in range(sec_idx + 1, len(lines)):
            if lines[i].startswith("## "):
                insert_idx = i - 1
                break

        # Insert before next section
        new_lines = list(lines)
        new_lines.insert(insert_idx, new_bullet)

        if dry_run:
            print(f"\n[DREAMING PROPOSAL - DRY RUN] -> {module_rel_path} (Dưới {section_name}):")
            print(f"  + {new_bullet}")
            return True

        # Apply change
        full_path.write_text("\n".join(new_lines) + "\n", encoding="utf-8")
        passed, errors = validate_gemini_file(module_rel_path)
        if not passed:
            print(f"  [REVERT] Vi phạm guardrails sau khi ghi: {errors}")
            full_path.write_text(content, encoding="utf-8")
            return False

        print(f"[APPLIED] Đã cập nhật thành công vào {module_rel_path}!")
        return True


def run_dreaming_cycle(dry_run=True):
    """Executes a full Dreaming consolidation cycle."""
    print("=" * 60)
    print(f"DREAMING CONSOLIDATION CYCLE (Mode: {'DRY-RUN' if dry_run else 'APPLY'})")
    print("=" * 60)

    diff = get_recent_diff()
    if not diff:
        print("[INFO] Không phát hiện git diff mới nào trong workspace để phân tích.")
        print("[TIP] Bạn có thể dùng lệnh: python tools/memory/dreaming_engine.py --learn \"<bài học>\" --module <module>")
        return

    affected_mods = detect_affected_modules(diff)
    if not affected_mods:
        print("[INFO] Các file thay đổi không thuộc phạm vi các module nghiệp vụ cốt lõi.")
        return

    print("[DETECTED AFFECTED MODULES]:")
    for mod, files in affected_mods.items():
        print(f"  * {mod} ({len(files)} files modified)")

    findings = analyze_diff_for_invariants(diff)
    if not findings:
        print("\n[INFO] Không phát hiện pattern bất biến mới nào cần đúc kết.")
        return

    print(f"\n[FOUND {len(findings)} INVARIANT PATTERNS]:")
    for item in findings:
        print(f"  - Category: {item['category']}")
        for mod in affected_mods.keys():
            apply_patch_to_module(mod, item["template"], item["section"], dry_run=dry_run)


if __name__ == "__main__":
    is_dry_run = "--apply" not in sys.argv

    if "--learn" in sys.argv:
        # Direct learning mode: python dreaming_engine.py --learn "Quy tắc..." --module "src/services/GEMINI.md"
        idx = sys.argv.index("--learn")
        rule_text = sys.argv[idx + 1] if idx + 1 < len(sys.argv) else ""
        target_mod = "src/services/GEMINI.md"
        if "--module" in sys.argv:
            m_idx = sys.argv.index("--module")
            target_mod = sys.argv[m_idx + 1] if m_idx + 1 < len(sys.argv) else target_mod

        bullet = f"- {rule_text}" if not rule_text.startswith("-") else rule_text
        print(f"[DIRECT LEARN] Thêm bài học vào {target_mod}...")
        apply_patch_to_module(target_mod, bullet, "## 3. Các Bất Biến Bắt Buộc", dry_run=is_dry_run)
        sys.exit(0)

    run_dreaming_cycle(dry_run=is_dry_run)
