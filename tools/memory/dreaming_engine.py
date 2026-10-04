#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Dreaming Engine for NRO Project (v2).

v1 matched regexes in git diff and re-stamped 8 fixed templates into every module,
which produced duplicated, generic bullets and never learned anything new.

v2 responsibilities:
  * Default cycle  -> session digest (delegates to memory.py digest): show changed files,
                      affected modules, related lessons, and prompt the agent to capture
                      a *specific* lesson (symptom -> cause -> rule) only if one exists.
  * --learn        -> write a distilled rule straight into a GEMINI.md module
                      (timestamp-insensitive fuzzy dedup, safe pruning to ARCHIVE).
  * apply_patch_to_module() is reused by `memory.py promote`.
"""

import sys
import re
from pathlib import Path
from datetime import datetime

sys.path.insert(0, str(Path(__file__).resolve().parent))
from memlib import PROJECT_ROOT, MEMORY_DIR, MODULE_MAP, find_similar_line, force_utf8_stdout  # noqa: F401
from guardrails import MemoryLock, validate_gemini_file, MAX_LINES_PER_GEMINI, MAX_BYTES_PER_GEMINI

force_utf8_stdout()

ARCHIVE_FILE = MEMORY_DIR / "ARCHIVE.md"
DATE_RE = re.compile(r"\[(\d{4}-\d{2}-\d{2})\]")

SECTION_ALIASES = {
    "invariant": "## 3. Các Bất Biến Bắt Buộc",
    "error": "## 4. Lỗi Thường Gặp Cần Tránh",
    "arch": "## 1. Trách Nhiệm & Kiến Trúc",
    "landmark": "## 2. Landmark Index & Core Methods",
}


def _section_bounds(lines, section_name):
    start = next((i for i, l in enumerate(lines) if l.strip() == section_name.strip()), -1)
    if start == -1:
        return -1, -1
    end = next((i for i in range(start + 1, len(lines)) if lines[i].startswith("## ")), len(lines))
    return start, end


def archive_oldest_dated_bullets(module_rel_path, section_name, num=2):
    """
    Move the N oldest *dated* bullets of a section to ARCHIVE.md.
    Undated bullets are hand-written core invariants and are NEVER pruned automatically.
    """
    full_path = PROJECT_ROOT / module_rel_path
    lines = full_path.read_text(encoding="utf-8").splitlines()
    start, end = _section_bounds(lines, section_name)
    if start == -1:
        return 0
    dated = []
    for i in range(start + 1, end):
        if lines[i].strip().startswith("- "):
            m = DATE_RE.search(lines[i])
            if m:
                dated.append((m.group(1), i))
    dated.sort()
    victims = [i for _, i in dated[:num]]
    if not victims:
        return 0
    with open(ARCHIVE_FILE, "a", encoding="utf-8") as f:
        for i in victims:
            f.write(f"{lines[i].strip()} <!-- from {module_rel_path} archived {datetime.now():%Y-%m-%d} -->\n")
            print(f"  [ARCHIVE] {lines[i].strip()[:90]}...")
    keep = [l for i, l in enumerate(lines) if i not in set(victims)]
    full_path.write_text("\n".join(keep) + "\n", encoding="utf-8")
    return len(victims)


def _over_budget(lines, extra=""):
    text = "\n".join(lines) + "\n" + extra
    return len(lines) + 1 > MAX_LINES_PER_GEMINI or len(text.encode("utf-8")) > MAX_BYTES_PER_GEMINI


def apply_patch_to_module(module_rel_path, new_bullet, section_name, dry_run=True):
    """Insert a bullet under a section with lock, fuzzy dedup, safe archiving and guardrail revert."""
    full_path = PROJECT_ROOT / module_rel_path
    if not full_path.exists():
        print(f"[ERROR] Không tìm thấy file {module_rel_path}")
        return False

    new_bullet = new_bullet.strip()
    if not new_bullet.startswith("- "):
        new_bullet = "- " + new_bullet.lstrip("- ")

    with MemoryLock():
        content = full_path.read_text(encoding="utf-8")
        lines = content.splitlines()

        # 1. Dedup BEFORE adding timestamp (v1 bug: timestamp made every day look "new")
        dup = find_similar_line(lines, new_bullet)
        if dup:
            print(f"  [SKIP] Quy tắc tương tự đã tồn tại trong {module_rel_path}:\n         > {dup.strip()[:160]}")
            return True

        if not DATE_RE.search(new_bullet):
            new_bullet = f"{new_bullet} `[{datetime.now():%Y-%m-%d}]`"

        # 2. Budget: archive oldest dated bullets (never undated core invariants)
        if _over_budget(lines, new_bullet):
            if dry_run:
                print("  [DRY-RUN] Vượt ngân sách → sẽ chuyển bullet có ngày cũ nhất sang ARCHIVE.md")
            else:
                for _ in range(3):
                    if not _over_budget(lines, new_bullet):
                        break
                    if archive_oldest_dated_bullets(module_rel_path, section_name, 2) == 0:
                        break
                    lines = full_path.read_text(encoding="utf-8").splitlines()
                if _over_budget(lines, new_bullet):
                    print("  [ERROR] Vẫn vượt ngân sách. Cần rút gọn thủ công hoặc chuyển chi tiết sang LESSONS.jsonl.")
                    return False

        # 3. Insert at end of section (before trailing blank line)
        start, end = _section_bounds(lines, section_name)
        if start == -1:
            print(f"  [ERROR] Không tìm thấy mục '{section_name}' trong {module_rel_path}")
            return False
        insert_idx = end
        while insert_idx - 1 > start and not lines[insert_idx - 1].strip():
            insert_idx -= 1
        new_lines = lines[:insert_idx] + [new_bullet] + lines[insert_idx:]

        if dry_run:
            print(f"\n[DRY-RUN] -> {module_rel_path} ({section_name}):\n  + {new_bullet}")
            return True

        original = full_path.read_text(encoding="utf-8")
        full_path.write_text("\n".join(new_lines) + "\n", encoding="utf-8")
        passed, errors = validate_gemini_file(module_rel_path)
        if not passed:
            print(f"  [REVERT] Vi phạm guardrails sau khi ghi: {errors}")
            full_path.write_text(original, encoding="utf-8")
            return False
        print(f"[APPLIED] Đã cập nhật {module_rel_path}")
        return True


def run_dreaming_cycle():
    from memory import cmd_digest
    return cmd_digest()


def _arg(name, default=None):
    if name in sys.argv:
        i = sys.argv.index(name)
        if i + 1 < len(sys.argv):
            return sys.argv[i + 1]
    return default


if __name__ == "__main__":
    is_dry_run = "--apply" not in sys.argv

    if "--learn" in sys.argv:
        rule_text = _arg("--learn", "")
        target_mod = _arg("--module")
        if not target_mod:
            print("[ERROR] Bắt buộc --module (vd. src/models/tournament/GEMINI.md). "
                  "Không còn mặc định src/services để tránh ghi lạc module.")
            print(f"        Module hợp lệ: {', '.join(sorted(set(MODULE_MAP.values())))}")
            sys.exit(1)
        sec = _arg("--section", "invariant")
        target_section = SECTION_ALIASES.get(sec, sec)
        print(f"[DIRECT LEARN] → {target_mod} | Section: {target_section}")
        ok = apply_patch_to_module(target_mod, rule_text, target_section, dry_run=is_dry_run)
        sys.exit(0 if ok else 1)

    sys.exit(run_dreaming_cycle() or 0)
