#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Invariant Violation Scanner for NRO Project.
Scans actual source code to verify whether documented invariants
are being respected. Reports violations with file:line references.
"""

import sys
import os
import re
import json
import glob
from pathlib import Path
from datetime import datetime

# Force UTF-8 stdout
if sys.stdout.encoding != 'utf-8':
    try:
        sys.stdout.reconfigure(encoding='utf-8')
    except Exception:
        pass

PROJECT_ROOT = Path(__file__).resolve().parent.parent.parent
PATTERNS_FILE = Path(__file__).resolve().parent / "PATTERNS.json"
BASELINE_FILE = Path(__file__).resolve().parent / "VIOLATIONS_BASELINE.json"

# ── Violation Check Rules ───────────────────────────────────────────
# Each rule defines a "bad_pattern" (violation) to search for in specific files.
# Some rules also define a "good_pattern" (correct usage) for context.
VIOLATION_CHECKS = [
    {
        "id": "db_no_try_with_resources",
        "pattern_id": "db_autoclose",
        "description": "Truy vấn DB không dùng try-with-resources",
        "glob": "src/database/**/*.java",
        "bad_pattern": r"Connection\s+\w+\s*=\s*.*getConnection\(\)\s*;",
        "exclude_pattern": r"try\s*\(",
        "message": "Connection không được bọc trong try-with-resources",
        "severity": "CRITICAL"
    },
    {
        "id": "int_instead_of_long_npoint",
        "pattern_id": "numeric_overflow",
        "description": "Dùng int cho biến tích lũy trong NPoint thay vì long",
        "glob": "src/models/player/NPoint.java",
        "bad_pattern": r"\bint\s+(hp|mp|damage|tiemNang|sucManh|exp|maxHp|maxMp)\b",
        "exclude_pattern": None,
        "message": "Biến tích lũy đang dùng int, cần đổi sang long",
        "severity": "CRITICAL"
    },
    {
        "id": "int_instead_of_long_player",
        "pattern_id": "numeric_overflow",
        "description": "Dùng int cho biến tích lũy trong Player thay vì long",
        "glob": "src/models/player/Player.java",
        "bad_pattern": r"\bint\s+(tiemNang|sucManh|exp|money|xu|vang)\b",
        "exclude_pattern": None,
        "message": "Biến tích lũy trong Player đang dùng int, cần đổi sang long",
        "severity": "CRITICAL"
    },
    {
        "id": "static_connection",
        "pattern_id": "db_autoclose",
        "description": "Connection static dùng chung (vi phạm pool management)",
        "glob": "src/database/**/*.java",
        # Only static FIELDS (`static Connection con;` / `= ...`), not static factory methods
        "bad_pattern": r"\bstatic\s+(?:final\s+)?Connection\s+\w+\s*[;=]",
        "exclude_pattern": None,
        "message": "Đang giữ static Connection field, vi phạm invariant DB connection pool",
        "severity": "CRITICAL"
    },
    {
        "id": "raw_arraylist_in_zone",
        "pattern_id": "concurrent_modification",
        "description": "Field ArrayList (không thread-safe) trong Zone cho players/mobs",
        "glob": "src/models/map/Zone.java",
        # Only class-level fields (access modifier at start); local temp lists are fine
        "bad_pattern": r"^\s*(?:public|private|protected)\s+(?:final\s+)?(?:List|ArrayList)\s*<\s*(?:Player|Mob)\s*>\s+\w+\s*=\s*new\s+ArrayList",
        "exclude_pattern": None,
        "message": "Zone dùng ArrayList thay vì CopyOnWriteArrayList/ConcurrentHashMap cho player/mob list",
        "severity": "HIGH"
    },
    {
        "id": "missing_backtick_mysql_keywords",
        "pattern_id": "db_autoclose",
        "description": "Thiếu backtick cho cột là từ khóa MySQL 8 (rank, group, order) khi dùng làm tên cột",
        "glob": "src/database/**/*.java",
        # Column usage: `rank` anywhere as identifier; `group`/`order` only when NOT followed by BY
        "bad_pattern": r'"[^"]*(?:SELECT|INSERT|UPDATE|DELETE|WHERE|SET)[^"]*(?<![`\w])(?:rank(?![`\w])|(?:group|order)(?![`\w])(?!\s+by\b))',
        "exclude_pattern": None,
        "message": "Cột MySQL keyword thiếu backtick, có thể gây lỗi SQL syntax",
        "severity": "MEDIUM"
    }
]


def scan_file(filepath, check):
    """Quét 1 file theo 1 check rule, trả về danh sách vi phạm."""
    violations = []
    try:
        content = filepath.read_text(encoding="utf-8", errors="ignore")
        lines = content.splitlines()
        for i, line in enumerate(lines, 1):
            stripped = line.strip()
            if stripped.startswith("//") or stripped.startswith("*"):
                continue  # commented-out code is not a violation
            if re.search(check["bad_pattern"], line, re.IGNORECASE):
                # Nếu có exclude_pattern, skip nếu dòng cũng match exclude
                if check.get("exclude_pattern"):
                    if re.search(check["exclude_pattern"], line, re.IGNORECASE):
                        continue
                    # Kiểm tra dòng trước đó (try-with-resources thường ở dòng trên)
                    if i >= 2 and re.search(check["exclude_pattern"], lines[i - 2], re.IGNORECASE):
                        continue
                violations.append({
                    "file": str(filepath.relative_to(PROJECT_ROOT)),
                    "line": i,
                    "code": line.strip()[:120],
                    "check_id": check["id"],
                    "message": check["message"],
                    "severity": check.get("severity", "INFO")
                })
    except Exception as e:
        pass
    return violations


def update_violation_counts(violations_by_check_id):
    """Gán (không cộng dồn) violation_count hiện tại trong PATTERNS.json."""
    if not PATTERNS_FILE.exists():
        return
    try:
        data = json.loads(PATTERNS_FILE.read_text(encoding="utf-8"))
        changed = False
        for p in data.get("patterns", []):
            pid = p.get("id", "")
            count = sum(violations_by_check_id.get(c["id"], 0)
                        for c in VIOLATION_CHECKS if c.get("pattern_id") == pid)
            if p.get("violation_count", 0) != count:
                p["violation_count"] = count
                if count > 0:
                    p["last_violated"] = datetime.now().strftime("%Y-%m-%d")
                changed = True
        if changed:
            PATTERNS_FILE.write_text(
                json.dumps(data, indent=2, ensure_ascii=False) + "\n",
                encoding="utf-8"
            )
            print(f"\n[UPDATED] Đã cập nhật violation_count trong PATTERNS.json")
    except Exception as e:
        print(f"[WARN] Không thể cập nhật PATTERNS.json: {e}")


def _fingerprint(v):
    # Line numbers shift with edits; fingerprint by check + file + code text
    return f"{v['check_id']}|{v['file'].replace(chr(92), '/')}|{v['code']}"


def load_baseline():
    if BASELINE_FILE.exists():
        try:
            return set(json.loads(BASELINE_FILE.read_text(encoding="utf-8")).get("fingerprints", []))
        except Exception:
            pass
    return set()


def collect_violations():
    all_v = []
    files_scanned = {}
    for check in VIOLATION_CHECKS:
        files = [Path(f) for f in glob.glob(str(PROJECT_ROOT / check["glob"]), recursive=True)]
        files_scanned[check["id"]] = len(files)
        for f in files:
            all_v.extend(scan_file(f, check))
    return all_v, files_scanned


def write_baseline():
    all_v, _ = collect_violations()
    fps = sorted({_fingerprint(v) for v in all_v})
    BASELINE_FILE.write_text(json.dumps({
        "created_at": datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
        "note": "Vi phạm tồn đọng đã biết. Chỉ vi phạm MỚI (không có trong danh sách) mới làm fail gate.",
        "fingerprints": fps
    }, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    print(f"[BASELINE] Đã ghi {len(fps)} vi phạm tồn đọng vào {BASELINE_FILE.name}")


def run_scan(update_counts=True):
    """Chạy toàn bộ violation checks. Chỉ vi phạm MỚI (ngoài baseline) làm fail."""
    print("=" * 60)
    print("INVARIANT VIOLATION SCANNER")
    print("=" * 60)

    baseline = load_baseline()
    all_v, files_scanned = collect_violations()
    violations_by_check = {}
    new_total = 0
    legacy_total = 0

    for check in VIOLATION_CHECKS:
        vs = [v for v in all_v if v["check_id"] == check["id"]]
        violations_by_check[check["id"]] = len(vs)
        new_vs = [v for v in vs if _fingerprint(v) not in baseline]
        legacy = len(vs) - len(new_vs)
        new_total += len(new_vs)
        legacy_total += legacy

        sev = check.get("severity", "INFO")
        icon = {"CRITICAL": "🔴", "HIGH": "🟠", "MEDIUM": "🟡"}.get(sev, "⚪")

        if new_vs:
            print(f"\n{icon} [{sev}] {check['id']}: {check['description']} ({len(new_vs)} vi phạm MỚI)")
            for v in new_vs[:5]:
                print(f"   {v['file']}:{v['line']} → {v['code']}")
            if len(new_vs) > 5:
                print(f"   ... và {len(new_vs) - 5} vi phạm khác")
        elif legacy:
            print(f"⚪ [{check['id']}] {legacy} vi phạm tồn đọng (baseline) — không có vi phạm mới")
        else:
            print(f"✅ [{check['id']}] {check['description']} — OK ({files_scanned[check['id']]} files scanned)")

    print(f"\n{'=' * 60}")
    if new_total > 0:
        print(f"[RESULT] 🔴 {new_total} vi phạm MỚI (tồn đọng: {legacy_total})")
    else:
        print(f"[RESULT] ✅ Không có vi phạm mới (tồn đọng baseline: {legacy_total})")

    if update_counts:
        update_violation_counts(violations_by_check)

    return new_total == 0


if __name__ == "__main__":
    if "--write-baseline" in sys.argv:
        write_baseline()
        sys.exit(0)
    no_update = "--no-update" in sys.argv
    ok = run_scan(update_counts=not no_update)
    sys.exit(0 if ok else 1)
