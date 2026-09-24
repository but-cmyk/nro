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
        "bad_pattern": r"static\s+Connection\s+",
        "exclude_pattern": None,
        "message": "Đang giữ static Connection, vi phạm invariant DB connection pool",
        "severity": "CRITICAL"
    },
    {
        "id": "raw_arraylist_in_zone",
        "pattern_id": "concurrent_modification",
        "description": "Dùng ArrayList (không thread-safe) trong Zone cho players/mobs",
        "glob": "src/models/map/Zone.java",
        "bad_pattern": r"new\s+ArrayList\s*<\s*(Player|Mob)",
        "exclude_pattern": None,
        "message": "Zone dùng ArrayList thay vì CopyOnWriteArrayList/ConcurrentHashMap cho player/mob list",
        "severity": "HIGH"
    },
    {
        "id": "missing_backtick_mysql_keywords",
        "pattern_id": "db_autoclose",
        "description": "Thiếu backtick cho cột MySQL keyword (rank, order, group, name)",
        "glob": "src/database/**/*.java",
        "bad_pattern": r'"\s*(?:SELECT|INSERT|UPDATE|DELETE).*\b(rank|order|group|name)\b(?!`)',
        "exclude_pattern": r"`(rank|order|group|name)`",
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
    """Cập nhật violation_count trong PATTERNS.json dựa trên kết quả scan."""
    if not PATTERNS_FILE.exists():
        return
    try:
        data = json.loads(PATTERNS_FILE.read_text(encoding="utf-8"))
        changed = False
        for p in data.get("patterns", []):
            pid = p.get("id", "")
            # Tìm violations liên quan đến pattern này
            count = 0
            for check in VIOLATION_CHECKS:
                if check.get("pattern_id") == pid:
                    count += violations_by_check_id.get(check["id"], 0)
            if count > 0:
                p["violation_count"] = p.get("violation_count", 0) + count
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


def run_scan(update_counts=True):
    """Chạy toàn bộ violation checks trên codebase."""
    print("=" * 60)
    print("INVARIANT VIOLATION SCANNER")
    print("=" * 60)

    total_violations = 0
    violations_by_check = {}

    for check in VIOLATION_CHECKS:
        pattern = str(PROJECT_ROOT / check["glob"])
        files = [Path(f) for f in glob.glob(pattern, recursive=True)]

        violations = []
        for f in files:
            violations.extend(scan_file(f, check))

        violations_by_check[check["id"]] = len(violations)

        sev = check.get("severity", "INFO")
        icon = {"CRITICAL": "🔴", "HIGH": "🟠", "MEDIUM": "🟡"}.get(sev, "⚪")

        if violations:
            print(f"\n{icon} [{sev}] {check['id']}: {check['description']} ({len(violations)} vi phạm)")
            for v in violations[:5]:  # hiển thị tối đa 5
                print(f"   {v['file']}:{v['line']} → {v['code']}")
            if len(violations) > 5:
                print(f"   ... và {len(violations) - 5} vi phạm khác")
            total_violations += len(violations)
        else:
            print(f"✅ [{check['id']}] {check['description']} — OK ({len(files)} files scanned)")

    print(f"\n{'=' * 60}")
    if total_violations > 0:
        print(f"[RESULT] Tổng vi phạm: {total_violations}")
    else:
        print(f"[RESULT] ✅ Không phát hiện vi phạm invariant nào!")

    # Update violation counts in PATTERNS.json
    if update_counts and total_violations > 0:
        update_violation_counts(violations_by_check)

    return total_violations == 0


if __name__ == "__main__":
    no_update = "--no-update" in sys.argv
    ok = run_scan(update_counts=not no_update)
    sys.exit(0 if ok else 1)
