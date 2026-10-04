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
from datetime import datetime
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from memlib import PROJECT_ROOT, ALL_KNOWN_MODULES, tokenize, force_utf8_stdout

force_utf8_stdout()

ROUTING_FILE = PROJECT_ROOT / "tools" / "memory" / "CODE_ROUTING.json"
LOCK_FILE = PROJECT_ROOT / "tools" / "memory" / ".memory.lock"
ALERT_FILE = PROJECT_ROOT / "tools" / "memory" / "PERFORMANCE_ALERTS.json"

MAX_LINES_PER_GEMINI = 120
MAX_BYTES_PER_GEMINI = 8000      # token budget: module files are auto-loaded as rules
MAX_CHARS_PER_BULLET = 450       # longer bullets = feature specs, belong elsewhere
PERF_MIN_SAMPLES = 20            # alerts with fewer calls are informational only

MANDATORY_SECTIONS = [
    "## 1. Trách Nhiệm & Kiến Trúc",
    "## 2. Landmark Index & Core Methods",
    "## 3. Các Bất Biến Bắt Buộc",
    "## 4. Lỗi Thường Gặp Cần Tránh"
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
    size = len(content.encode("utf-8"))
    errors = []
    warnings = []

    # 1. Check size limits (lines + bytes)
    if len(lines) > MAX_LINES_PER_GEMINI:
        errors.append(f"Vượt quá giới hạn dòng: {len(lines)}/{MAX_LINES_PER_GEMINI} dòng. Cần Pruning bớt nội dung cũ.")
    if size > MAX_BYTES_PER_GEMINI:
        errors.append(f"Vượt quá dung lượng: {size}/{MAX_BYTES_PER_GEMINI} bytes. Chuyển chi tiết sang LESSONS.jsonl.")

    # 2. Check 4 mandatory sections
    for sec in MANDATORY_SECTIONS:
        if sec not in content:
            errors.append(f"Thiếu mục bắt buộc: '{sec}'")

    # 3. Soft check: over-long bullets
    for i, l in enumerate(lines, 1):
        if l.strip().startswith("- ") and len(l) > MAX_CHARS_PER_BULLET:
            warnings.append(f"Dòng {i} dài {len(l)} ký tự (> {MAX_CHARS_PER_BULLET}) — nên rút gọn")

    if errors:
        return False, errors + warnings
    return True, [f"OK ({len(lines)} dòng, {size} bytes)"] + [f"[WARN] {w}" for w in warnings]


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
    n = len(ALL_KNOWN_MODULES)
    if all_passed:
        print(f"[SUCCESS] Tất cả {n}/{n} memory modules đạt chuẩn Guardrails!")
    else:
        print("[WARNING] Có module vi phạm schema hoặc vượt giới hạn dung lượng!")

    # Phase 2: Violation scan (only NEW violations vs baseline fail the gate)
    print("\n" + "=" * 60)
    print("KIỂM TRA VI PHẠM INVARIANT TRONG CODE THỰC TẾ")
    print("=" * 60)
    try:
        from violation_scanner import run_scan
        scan_ok = run_scan(update_counts=False)
        if not scan_ok:
            all_passed = False
    except ImportError:
        print("[SKIP] violation_scanner.py chưa được tạo")
    except Exception as e:
        print(f"[ERROR] Lỗi khi chạy violation scanner: {e}")

    # Phase 3: Performance Alerts Check
    print("\n" + "=" * 60)
    print("KIỂM TRA SỰ CỐ HIỆU NĂNG SERVER (PERFORMANCE ALERTS)")
    print("=" * 60)
    if not check_perf_alerts():
        all_passed = False

    return all_passed


def check_perf_alerts():
    """OPEN alerts with enough samples fail the gate; low-sample alerts are informational."""
    if not ALERT_FILE.exists():
        print("✅ Chưa có file PERFORMANCE_ALERTS.json (Hệ thống hoạt động bình thường).")
        return True
    try:
        data = json.loads(ALERT_FILE.read_text(encoding="utf-8"))
    except Exception as e:
        print(f"⚠️ Không đọc được PERFORMANCE_ALERTS.json: {e}")
        return True

    open_alerts = [a for a in data.get("alerts", []) if a.get("status") == "OPEN"]
    real = [a for a in open_alerts if a.get("call_count", 0) >= PERF_MIN_SAMPLES]
    low = [a for a in open_alerts if a.get("call_count", 0) < PERF_MIN_SAMPLES]

    def fmt(a):
        cmd = a.get("cmd")
        return (f"CMD {cmd} ({a.get('command_name', f'CMD_{cmd}')}) | Max: {a.get('max_latency_ms', 0)}ms, "
                f"Avg: {a.get('avg_latency_ms', 0)}ms | Calls: {a.get('call_count', 0)} | "
                f"Context: {a.get('slowest_context', '')}")

    for a in low:
        print(f"⚪ [LOW-SAMPLE] {fmt(a)} — chưa đủ {PERF_MIN_SAMPLES} mẫu, chưa kết luận")
    if real:
        print(f"🔴 [CẢNH BÁO] {len(real)} chức năng nghẽn hiệu năng (đủ mẫu):")
        for a in real:
            print(f"  * {fmt(a)}")
        print("\n→ [YÊU CẦU AI AGENT]: Bóc tách nguyên nhân & lập kế hoạch fix trình User phê duyệt!")
        print("  Sau khi xử lý/xác nhận: python tools/memory/guardrails.py --ack <cmd> [RESOLVED|ACK]")
        return False
    if not low:
        print("✅ Không có cảnh báo nghẽn hiệu năng đang mở (All clear).")
    return True


def ack_alert(cmd, status="ACK"):
    if not ALERT_FILE.exists():
        print("[ERROR] Không có PERFORMANCE_ALERTS.json")
        return False
    data = json.loads(ALERT_FILE.read_text(encoding="utf-8"))
    hit = False
    for a in data.get("alerts", []):
        if str(a.get("cmd")) == str(cmd):
            a["status"] = status
            a["acked_at"] = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
            hit = True
    if hit:
        ALERT_FILE.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
        print(f"[OK] CMD {cmd} → {status}")
    else:
        print(f"[ERROR] Không tìm thấy alert CMD {cmd}")
    return hit


def validate_routing():
    """Verify every core_file / doc_module in CODE_ROUTING.json exists on disk."""
    data = json.loads(ROUTING_FILE.read_text(encoding="utf-8"))
    missing = []
    for route in data.get("routes", []):
        for f in [route.get("doc_module")] + route.get("core_files", []):
            if f and not (PROJECT_ROOT / f).exists():
                missing.append((route["domain"], f))
    if missing:
        print(f"[FAIL] {len(missing)} đường dẫn trong CODE_ROUTING.json không tồn tại:")
        for d, f in missing:
            print(f"  - [{d}] {f}")
        return False
    print("[PASS] Tất cả đường dẫn trong CODE_ROUTING.json đều tồn tại.")
    return True


def query_routing(query_str):
    """Performs semantic fast-path routing lookup given user intent keywords."""
    if not ROUTING_FILE.exists():
        print(f"[ERROR] Không tìm thấy file {ROUTING_FILE}")
        return []

    with open(ROUTING_FILE, "r", encoding="utf-8") as f:
        data = json.load(f)

    # Normalize query: strip Vietnamese diacritics, add bigrams ("dap do" -> "dap_do")
    tokens = tokenize(query_str)
    results = []

    for route in data.get("routes", []):
        score = 0
        keywords = [k.lower() for k in route.get("keywords", [])]
        kw_set = set(keywords)
        for token in tokens:
            if token in kw_set:
                score += 4 if "_" in token else 3          # exact (bigram stronger)
            elif len(token) >= 4 and any(token in kw or kw in token for kw in keywords if len(kw) >= 4):
                score += 1                                  # partial, long tokens only
            for cf in route.get("core_files", []) + route.get("key_methods", []):
                if len(token) >= 4 and token in cf.lower():
                    score += 1
                    break

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
        elif sys.argv[1] == "--validate-routing":
            sys.exit(0 if validate_routing() else 1)
        elif sys.argv[1] == "--ack" and len(sys.argv) > 2:
            st = sys.argv[3].upper() if len(sys.argv) > 3 else "ACK"
            sys.exit(0 if ack_alert(sys.argv[2], st) else 1)
        elif sys.argv[1] == "--lock-test":
            print("[TEST] Thử nghiệm MemoryLock...")
            with MemoryLock():
                print("[OK] Đã giữ lock thành công!")
            print("[OK] Đã giải phóng lock!")
            sys.exit(0)
    # Default behavior
    check_all()
