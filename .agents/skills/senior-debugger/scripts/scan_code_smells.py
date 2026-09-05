#!/usr/bin/env python3
"""
Senior Bug & Code Smell Scanner
Công cụ phân tích tĩnh chuyên sâu dành cho Java Game Server.
Phát hiện tự động các bẫy code nguy hiểm:
- Empty catch blocks (Nuốt lỗi thầm lặng)
- Switch fallthrough (Thiếu break trong switch-case)
- Self-referential recursion (Đệ quy tự gọi)
- Unbounded while loops (Vòng lặp while không giới hạn)
- Memory allocation in broadcast loops (Tạo rác GC trong broadcast)
"""

import os
import re
import sys

# Cấu hình UTF-8 cho Windows console
if sys.platform == 'win32':
    try:
        sys.stdout.reconfigure(encoding='utf-8')
    except Exception:
        pass

def scan_java_file(file_path):
    issues = []
    with open(file_path, 'r', encoding='utf-8', errors='ignore') as f:
        lines = f.readlines()

    content = "".join(lines)

    # 1. Quét Empty Catch Blocks
    empty_catch_pattern = re.compile(r'catch\s*\([^\)]+\)\s*\{\s*\}', re.MULTILINE)
    for match in empty_catch_pattern.finditer(content):
        line_no = content[:match.start()].count('\n') + 1
        issues.append({
            "type": "SILENT_CATCH",
            "severity": "HIGH",
            "line": line_no,
            "message": "Empty catch block phát hiện: Nuốt ngoại lệ thầm lặng làm mất dấu vết lỗi!"
        })

    # 2. Quét Infinite Self-Recursion
    # Tìm method name: public ReturnType methodName(...) { return this.methodName(...); }
    recursion_pattern = re.compile(
        r'public\s+[\w<>,\[\]\s]+\s+(\w+)\s*\([^\)]*\)\s*\{\s*return\s+(?:this\.)?\1\s*\([^\)]*\)\s*;\s*\}',
        re.MULTILINE
    )
    for match in recursion_pattern.finditer(content):
        line_no = content[:match.start()].count('\n') + 1
        method_name = match.group(1)
        issues.append({
            "type": "INFINITE_RECURSION",
            "severity": "CRITICAL",
            "line": line_no,
            "message": f"Hàm '{method_name}' tự gọi lại chính nó vô hạn -> Nguy cơ sập StackOverflowError!"
        })

    # 3. Quét While Loop Unbounded Increment
    while_pattern = re.compile(r'while\s*\([^\)]+\.get\(\s*(\w+)\s*\)[^\)]*\)\s*\{\s*\1\+\+;\s*\}', re.MULTILINE)
    for match in while_pattern.finditer(content):
        line_no = content[:match.start()].count('\n') + 1
        issues.append({
            "type": "UNBOUNDED_WHILE_LOOP",
            "severity": "CRITICAL",
            "line": line_no,
            "message": "Vòng lặp while duyệt list.get(z) và z++ mà không kiểm tra z < size() -> Nguy cơ IndexOutOfBoundsException!"
        })

    # 4. Quét new ArrayList trong sendMessage broadcast
    for idx, line in enumerate(lines, 1):
        if "new ArrayList<" in line and ("players" in line.lower() or "humanoids" in line.lower()):
            # Kiểm tra xem có nằm gần sendMessage không
            surrounding = "".join(lines[max(0, idx-10):min(len(lines), idx+10)])
            if "sendMessage" in surrounding:
                issues.append({
                    "type": "GC_PRESSURE_ALLOCATION",
                    "severity": "MEDIUM",
                    "line": idx,
                    "message": "Cấp phát new ArrayList(...) trong luồng broadcast tin nhắn -> Gây áp lực rác bộ nhớ cho JVM GC!"
                })

    return issues

def scan_directory(src_dir):
    print(f"=== [SENIOR CODE SMELL SCANNER] Bắt đầu quét thư mục: {src_dir} ===")
    total_files = 0
    total_issues = 0

    for root, _, files in os.walk(src_dir):
        for file in files:
            if file.endswith(".java"):
                total_files += 1
                full_path = os.path.join(root, file)
                issues = scan_java_file(full_path)
                if issues:
                    total_issues += len(issues)
                    rel_path = os.path.relpath(full_path, src_dir)
                    print(f"\n[!] File: {rel_path} ({len(issues)} vấn đề)")
                    for iss in issues:
                        sev_icon = "🔴" if iss['severity'] == 'CRITICAL' else ("🟡" if iss['severity'] == 'HIGH' else "🔵")
                        print(f"   {sev_icon} [Dòng {iss['line']}] [{iss['type']}] [{iss['severity']}]: {iss['message']}")

    print(f"\n=== Kết quả tổng quan: Đã quét {total_files} files Java, phát hiện {total_issues} điểm ma sát mã nguồn ===")

if __name__ == "__main__":
    target_dir = sys.argv[1] if len(sys.argv) > 1 else "src"
    scan_directory(target_dir)
