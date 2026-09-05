#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
scan_client_smells.py - Công Cụ Phân Tích Tĩnh Mã Nguồn Client Unity NRO
Chuyên quét các Code Smells, GC Allocations, Rò Rỉ Đa Tab và Bẫy Hiệu Năng trong C# Client.
"""

import os
import re
import sys

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")
    sys.stderr.reconfigure(encoding="utf-8", errors="replace")

def scan_file(file_path):
    issues = []
    with open(file_path, "r", encoding="utf-8", errors="ignore") as f:
        lines = f.readlines()

    in_paint = False
    in_update = False
    current_method = ""
    switch_count = 0
    in_switch = False

    for idx, line in enumerate(lines, 1):
        stripped = line.strip()

        # Phát hiện bắt đầu hàm paint() hoặc update()
        if re.search(r'\bvoid\s+(paint|paint\w*)\s*\(', line):
            in_paint = True
            in_update = False
            current_method = "paint"
        elif re.search(r'\bvoid\s+(update|update\w*)\s*\(', line):
            in_update = True
            in_paint = False
            current_method = "update"
        elif re.match(r'^\s*public|private|protected\s+[\w<>]+\s+\w+\s*\(', line) and not ("paint" in line or "update" in line):
            in_paint = False
            in_update = False
            current_method = ""

        # 1. Cấp phát đối tượng mới (new) trong paint/update
        if (in_paint or in_update) and "new " in line:
            # Loại trừ new mGraphics, struct primitive nếu có
            if not any(x in line for x in ["new int", "new sbyte", "new byte", "new short", "new long"]):
                issues.append({
                    "line": idx,
                    "type": "GC_ALLOC_IN_HOTPATH",
                    "severity": "HIGH",
                    "msg": f"Cấp phát heap ('new') trong hàm {current_method}(): '{stripped}'"
                })

        # 2. Ghép chuỗi (+) trong hàm paint
        if in_paint and "+" in line and ('"' in line or "drawString" in line):
            if "mFont" in line or "drawString" in line or "drawStringWithBorder" in line:
                issues.append({
                    "line": idx,
                    "type": "STRING_CONCAT_IN_PAINT",
                    "severity": "MEDIUM",
                    "msg": f"Nối chuỗi (+) trực tiếp trong hàm vẽ paint(): '{stripped}'"
                })

        # 3. Sử dụng MyVector / MyHashTable thay vì Generic Collection
        if re.search(r'\b(MyVector|MyHashTable)\b', line) and not ("class MyVector" in line or "class MyHashTable" in line):
            if "new MyVector" in line or "new MyHashTable" in line or "public MyVector" in line or "private MyVector" in line:
                issues.append({
                    "line": idx,
                    "type": "NON_GENERIC_COLLECTION",
                    "severity": "LOW",
                    "msg": f"Sử dụng collection J2ME cổ hủ (Boxing/Unboxing): '{stripped}'"
                })

        # 4. Biến tĩnh trạng thái (Static State Leak) trong các class entity
        base_name = os.path.basename(file_path).lower()
        if base_name in ["char.cs", "player.cs", "gamescr.cs", "panel.cs", "session_me.cs"]:
            if re.match(r'^\s*public\s+static\s+(int|short|long|bool|string|Position|Char|Item)\s+\w+', line):
                if not any(const_kw in line for const_kw in ["const", "readonly"]):
                    issues.append({
                        "line": idx,
                        "type": "STATIC_STATE_MULTITAB_LEAK",
                        "severity": "HIGH",
                        "msg": f"Biến tĩnh có thể gây rò rỉ trạng thái giữa 6 tab: '{stripped}'"
                    })

    return issues

def main():
    target_dir = sys.argv[1] if len(sys.argv) > 1 else "."
    print(f"[*] Bắt đầu quét tĩnh Client Code Smells tại: {target_dir}")

    total_files = 0
    total_issues = 0
    results = {}

    if os.path.isfile(target_dir):
        if target_dir.endswith(".cs"):
            total_files = 1
            file_issues = scan_file(target_dir)
            if file_issues:
                results[target_dir] = file_issues
                total_issues = len(file_issues)
    else:
        for root, dirs, files in os.walk(target_dir):
            for file in files:
                if file.endswith(".cs"):
                    total_files += 1
                    full_path = os.path.join(root, file)
                    file_issues = scan_file(full_path)
                    if file_issues:
                        results[full_path] = file_issues
                        total_issues += len(file_issues)

    print(f"[+] Đã quét: {total_files} files C#. Phát hiện: {total_issues} vấn đề tiềm ẩn.")
    print("=" * 80)

    # In top 10 files có nhiều vấn đề nhất
    sorted_files = sorted(results.items(), key=lambda x: len(x[1]), reverse=True)
    for path, issues in sorted_files[:15]:
        rel_path = os.path.relpath(path, target_dir)
        highs = sum(1 for i in issues if i["severity"] == "HIGH")
        meds = sum(1 for i in issues if i["severity"] == "MEDIUM")
        lows = sum(1 for i in issues if i["severity"] == "LOW")
        print(f"-> {rel_path}: {len(issues)} vấn đề (High: {highs}, Med: {meds}, Low: {lows})")
        # In tối đa 3 mẫu lỗi
        for iss in issues[:3]:
            print(f"   Line {iss['line']}: [{iss['severity']}] {iss['msg']}")
        if len(issues) > 3:
            print(f"   ... và {len(issues) - 3} vấn đề khác.")
        print("-" * 80)

if __name__ == "__main__":
    main()
