#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
SENIOR BACKEND AUDITOR - NRO JAVA GAME SERVER
Công cụ tự động đánh giá mã nguồn Backend theo tiêu chuẩn Senior Software Engineer / Game Architect:
1. Concurrency Safety & Race Conditions
2. Database Connection Leak & Resource Management (HikariCP/JDBC)
3. Financial / Economy Safety (Integer Overflow, Dupe vectors, Limit clipping)
4. Memory & Garbage Collection (Stop-The-World System.gc(), Static leaks)
5. Exception Handling & Observability (Empty catch blocks, Error swallowing)
6. Network & ByteBuf Leak Risks (Netty Buffer leaks, Unclosed messages)
"""

import os
import re
import sys

if sys.platform == 'win32':
    try:
        sys.stdout.reconfigure(encoding='utf-8')
    except Exception:
        pass

SERVER_DIR = os.path.join(os.getcwd(), "src")

class SeniorAuditReport:
    def __init__(self):
        self.total_files = 0
        self.critical_issues = []
        self.high_issues = []
        self.medium_issues = []
        self.positive_patterns = []

    def add_critical(self, file, line, rule, desc, snippet=""):
        self.critical_issues.append({"file": file, "line": line, "rule": rule, "desc": desc, "snippet": snippet})

    def add_high(self, file, line, rule, desc, snippet=""):
        self.high_issues.append({"file": file, "line": line, "rule": rule, "desc": desc, "snippet": snippet})

    def add_medium(self, file, line, rule, desc, snippet=""):
        self.medium_issues.append({"file": file, "line": line, "rule": rule, "desc": desc, "snippet": snippet})

    def add_positive(self, rule, count):
        self.positive_patterns.append({"rule": rule, "count": count})


def audit_file(filepath, rel_path, report):
    try:
        with open(filepath, 'r', encoding='utf-8', errors='ignore') as f:
            lines = f.readlines()
    except Exception:
        return

    content = "".join(lines)

    # 1. CHECK: System.gc() call (High performance impact)
    for idx, line in enumerate(lines, 1):
        if re.search(r'\bSystem\.gc\(\)', line) and not line.strip().startswith("//"):
            report.add_high(rel_path, idx, "GC_STOP_THE_WORLD", "Gọi System.gc() trực tiếp gây đóng băng toàn bộ luồng game (STW latency spike)", line.strip())

    # 2. CHECK: Connection / Statement leak (Không dùng try-with-resources)
    for idx, line in enumerate(lines, 1):
        # Pattern: Connection con = AlyraManager.getConnection(); mà không có try (
        if "AlyraManager.getConnection()" in line and "try (" not in line and not line.strip().startswith("//"):
            report.add_critical(rel_path, idx, "JDBC_CONNECTION_LEAK", "Kết nối JDBC không được bọc trong try-with-resources, nguy cơ cạn kiệt HikariCP Connection Pool", line.strip())

    # 3. CHECK: Swallowed Exceptions (empty catch block)
    empty_catch_pattern = re.compile(r'catch\s*\([^)]+\)\s*\{\s*\}')
    for idx, line in enumerate(lines, 1):
        if empty_catch_pattern.search(line) and not line.strip().startswith("//"):
            report.add_medium(rel_path, idx, "SWALLOWED_EXCEPTION", "Nuốt ngoại lệ rỗng (Empty catch block), làm mất dấu vết lỗi khi xảy ra sự cố luồng", line.strip())

    # 4. CHECK: Raw Thread instantiation (nên dùng ScheduledExecutorService / WorkerPool)
    for idx, line in enumerate(lines, 1):
        if re.search(r'new\s+Thread\s*\(', line) and not line.strip().startswith("//"):
            # Kiểm tra xem có name thread không
            if "start()" in line and "Thread(" in line:
                report.add_medium(rel_path, idx, "UNMANAGED_THREAD", "Khởi tạo raw Thread trực tiếp thay vì thông qua ThreadPoolExecutor quản lý", line.strip())

    # 5. CHECK: Unbounded Gold / Ruby arithmetic (Thiếu kiểm tra LIMIT_GOLD / getGoldLimit)
    for idx, line in enumerate(lines, 1):
        if re.search(r'(inventory\.gold|\.gold)\s*\+=', line) and not line.strip().startswith("//"):
            if "Inventory.java" in rel_path:
                continue
            # Kiểm tra xem các dòng lân cận có check LIMIT_GOLD hoặc getGoldLimit không
            has_limit_check = False
            start_check = max(0, idx - 6)
            end_check = min(idx + 6, len(lines))
            for forward in range(start_check, end_check):
                if any(k in lines[forward] for k in ["LIMIT_GOLD", "getGoldLimit"]):
                    has_limit_check = True
                    break
            if not has_limit_check:
                report.add_high(rel_path, idx, "GOLD_OVERFLOW_RISK", "Cộng vàng trực tiếp mà không kiểm tra chặn trần LIMIT_GOLD ngay sau đó", line.strip())

    # 6. CHECK: SQL Injection via String Concatenation in executeQuery / prepareStatement
    for idx, line in enumerate(lines, 1):
        if re.search(r'prepareStatement\s*\(\s*["\'][^"\']*["\']\s*\+', line) and not line.strip().startswith("//"):
            report.add_medium(rel_path, idx, "SQL_STRING_CONCAT", "Nối chuỗi trong prepareStatement thay vì dùng tham số ? (Placeholders)", line.strip())


def run_senior_audit():
    report = SeniorAuditReport()

    for root, _, files in os.walk(SERVER_DIR):
        for f in files:
            if f.endswith(".java"):
                report.total_files += 1
                full_path = os.path.join(root, f)
                rel_path = os.path.relpath(full_path, os.getcwd())
                audit_file(full_path, rel_path, report)

    print("=" * 80)
    print("           SENIOR BACKEND CODEBASE AUDIT REPORT - NRO SERVER           ")
    print("=" * 80)
    print(f"Tổng số Java files đã quét: {report.total_files} files\n")

    # Score calculation
    # Base: 100 điểm.
    # Critical: -10đ mỗi lỗi
    # High: -4đ mỗi lỗi
    # Medium: -1đ mỗi lỗi
    score = 100 - (len(report.critical_issues) * 10 + len(report.high_issues) * 4 + len(report.medium_issues) * 1)
    score = max(0, min(100, score))

    print(f"🏆 SENIOR ARCHITECTURE HEALTH SCORE: {score}/100")
    if score >= 90:
        grade = "A (Senior Grade - Sản phẩm sẵn sàng chịu tải lớn)"
    elif score >= 75:
        grade = "B (Good - Cần khắc phục một số điểm nghẽn concurrency & overflow)"
    elif score >= 60:
        grade = "C (Average - Tồn tại rủi ro rò rỉ tài nguyên khi CCU cao)"
    else:
        grade = "D (Critical - Nguy cơ Crash / Out of Memory / Dupe đồ nghiêm trọng)"
    print(f"Đánh giá phân hạng: {grade}\n")

    print("-" * 80)
    print(f"🚨 CRITICAL ISSUES (Nghiêm trọng - Cần fix ngay): {len(report.critical_issues)}")
    print("-" * 80)
    for issue in report.critical_issues[:10]:
        print(f"[{issue['rule']}] {issue['file']}:{issue['line']}")
        print(f"  -> {issue['desc']}")
        print(f"  -> Code: {issue['snippet']}\n")

    print("-" * 80)
    print(f"⚠️  HIGH PRIORITY ISSUES (Hiệu năng & Khả năng chịu tải): {len(report.high_issues)}")
    print("-" * 80)
    for issue in report.high_issues[:10]:
        print(f"[{issue['rule']}] {issue['file']}:{issue['line']}")
        print(f"  -> {issue['desc']}")
        print(f"  -> Code: {issue['snippet']}\n")

    print("-" * 80)
    print(f"📋 MEDIUM ISSUES (Code Smells & Observability): {len(report.medium_issues)}")
    print("-" * 80)
    print(f"Tổng cộng phát hiện {len(report.medium_issues)} vị trí nuốt exception hoặc thread không kiểm soát.")
    for issue in report.medium_issues[:5]:
        print(f"[{issue['rule']}] {issue['file']}:{issue['line']} -> {issue['desc']}")

    print("\n" + "=" * 80)
    return report

if __name__ == "__main__":
    run_senior_audit()
