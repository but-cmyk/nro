#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Production Guardrails & Validator for NRO Memory Architecture.
Ensures schema adherence, size limits (pruning), routing verification, and concurrency locks.
"""

import sys
import os
import json
import re
import time
from pathlib import Path

# Force UTF-8 stdout
if sys.stdout.encoding != 'utf-8':
    try:
        sys.stdout.reconfigure(encoding='utf-8')
    except Exception:
        pass

PROJECT_ROOT = Path(__file__).resolve().parent.parent.parent
ROUTING_FILE = PROJECT_ROOT / "tools" / "memory" / "CODE_ROUTING.json"
LOCK_FILE = PROJECT_ROOT / "tools" / "memory" / ".memory.lock"

MAX_LINES_PER_GEMINI = 120

MANDATORY_SECTIONS = [
    "## 1. Trách Nhiệm & Kiến Trúc",
    "## 2. Landmark Index & Core Methods",
    "## 3. Các Bất Biến Bắt Buộc",
    "## 4. Lỗi Thường Gặp Cần Tránh"
]

ALL_KNOWN_MODULES = [
    "src/services/GEMINI.md",
    "src/models/player/GEMINI.md",
    "src/models/map/GEMINI.md",
    "src/models/boss/GEMINI.md",
    "src/models/task/GEMINI.md",
    "src/network/GEMINI.md",
    "src/database/GEMINI.md",
    "Client/Client/Assets/Scripts/GEMINI.md"
]

class MemoryLock:
    """Simple file-based lock with timeout for concurrency safety."""
    def __init__(self, lock_path=LOCK_FILE, timeout_sec=5.0):
        self.lock_path = Path(lock_path)
        self.timeout_sec = timeout_sec

    def __enter__(self):
        start = time.time()
        while True:
            try:
                # Open with x flag (exclusive creation)
                fd = os.open(str(self.lock_path), os.O_CREAT | os.O_EXCL | os.O_WRONLY)
                os.write(fd, f"PID:{os.getpid()} TIME:{time.time()}".encode("utf-8"))
                os.close(fd)
                return self
            except FileExistsError:
                if time.time() - start > self.timeout_sec:
                    # Stale lock check (older than 30s)
                    try:
                        mtime = os.path.getmtime(str(self.lock_path))
                        if time.time() - mtime > 30:
                            os.remove(str(self.lock_path))
                            continue
                    except Exception:
                        pass
                    raise TimeoutError(f"Could not acquire memory lock after {self.timeout_sec}s: {self.lock_path}")
                time.sleep(0.1)

    def __exit__(self, exc_type, exc_val, exc_tb):
        try:
            if self.lock_path.exists():
                self.lock_path.unlink()
        except Exception:
            pass


def validate_gemini_file(rel_path):
    """Checks schema, line count, and mandatory sections of a GEMINI.md file."""
    full_path = PROJECT_ROOT / rel_path
    if not full_path.exists():
        return False, [f"File không tồn tại: {rel_path}"]

    try:
        content = full_path.read_text(encoding="utf-8")
    except Exception as e:
        return False, [f"Không đọc được file {rel_path}: {e}"]

    lines = content.splitlines()
    errors = []

    # 1. Check size limit
    if len(lines) > MAX_LINES_PER_GEMINI:
        errors.append(f"Vượt quá giới hạn dòng: {len(lines)}/{MAX_LINES_PER_GEMINI} dòng. Cần Pruning bớt nội dung cũ.")

    # 2. Check 4 mandatory sections
    for sec in MANDATORY_SECTIONS:
        if sec not in content:
            errors.append(f"Thiếu mục bắt buộc: '{sec}'")

    if errors:
        return False, errors
    return True, [f"OK ({len(lines)} dòng)"]


def check_all():
    """Validates all GEMINI.md modules in the workspace."""
    print("=" * 60)
    print("KIỂM TRA SCHEMA & GUARDRAILS TOÀN BỘ MEMORY MODULES")
    print("=" * 60)
    all_passed = True
    for mod in ALL_KNOWN_MODULES:
        passed, msgs = validate_gemini_file(mod)
        status = "[PASS]" if passed else "[FAIL]"
        print(f"{status:6} | {mod}")
        for m in msgs:
            prefix = "  - " if not passed else "    "
            print(f"{prefix}{m}")
        if not passed:
            all_passed = False

    print("-" * 60)
    if all_passed:
        print("[SUCCESS] Tất cả 8/8 memory modules đạt chuẩn 100% Guardrails!")
    else:
        print("[WARNING] Có module vi phạm schema hoặc vượt giới hạn dung lượng!")
    return all_passed


def query_routing(query_str):
    """Performs semantic fast-path routing lookup given user intent keywords."""
    if not ROUTING_FILE.exists():
        print(f"[ERROR] Không tìm thấy file {ROUTING_FILE}")
        return []

    with open(ROUTING_FILE, "r", encoding="utf-8") as f:
        data = json.load(f)

    # Normalize query keywords
    tokens = set(re.findall(r"\w+", query_str.lower()))
    results = []

    for route in data.get("routes", []):
        score = 0
        keywords = route.get("keywords", [])
        for token in tokens:
            for kw in keywords:
                if token in kw or kw in token:
                    score += 2
            # Also check in files/methods
            for cf in route.get("core_files", []):
                if token in cf.lower():
                    score += 1

        if score > 0:
            results.append((score, route))

    results.sort(key=lambda x: x[0], reverse=True)
    return results


def print_routing_results(query_str):
    print(f"\n[FAST-PATH ROUTING QUERY]: '{query_str}'")
    results = query_routing(query_str)
    if not results:
        print("  -> Không tìm thấy route trực tiếp. Cần fallback sang index map tổng thể.")
        return

    top_score, top_route = results[0]
    print(f"  -> Match Domain: {top_route['domain']} (Score: {top_score})")
    print(f"  -> Doc Module:   {top_route['doc_module']}")
    print(f"  -> Core Files:")
    for cf in top_route.get("core_files", [])[:3]:
        print(f"     * {cf}")
    print(f"  -> Key Methods:")
    for km in top_route.get("key_methods", [])[:3]:
        print(f"     * {km}()")
    print(f"  -> Key Invariants:")
    for inv in top_route.get("invariants", [])[:2]:
        print(f"     ! {inv}")


if __name__ == "__main__":
    if len(sys.argv) > 1:
        if sys.argv[1] == "--check-all":
            ok = check_all()
            sys.exit(0 if ok else 1)
        elif sys.argv[1] == "--test-routing" and len(sys.argv) > 2:
            query = " ".join(sys.argv[2:])
            print_routing_results(query)
            sys.exit(0)
        elif sys.argv[1] == "--lock-test":
            print("[TEST] Thử nghiệm MemoryLock...")
            with MemoryLock():
                print("[OK] Đã giữ lock thành công!")
            print("[OK] Đã giải phóng lock!")
            sys.exit(0)
    # Default behavior
    check_all()
