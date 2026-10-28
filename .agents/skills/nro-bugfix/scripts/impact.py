#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
NRO Impact Analyzer — Traces callers/callees of a method to assess blast radius of a change.

Usage:
  python .agents/skills/nro-bugfix/scripts/impact.py <file_pattern> <method_name>
  python .agents/skills/nro-bugfix/scripts/impact.py NPoint.java calPoint
  python .agents/skills/nro-bugfix/scripts/impact.py ItemService.java addItemBag
  python .agents/skills/nro-bugfix/scripts/impact.py Controller.cs onMessage
"""

import sys
import os
import re
import argparse
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parents[4]

# Force UTF-8 stdout on Windows
if sys.stdout.encoding and sys.stdout.encoding.lower() != 'utf-8':
    sys.stdout.reconfigure(encoding='utf-8', errors='replace')
if sys.stderr.encoding and sys.stderr.encoding.lower() != 'utf-8':
    sys.stderr.reconfigure(encoding='utf-8', errors='replace')

JAVA_EXTENSIONS = {".java"}
CS_EXTENSIONS = {".cs"}
ALL_CODE_EXTENSIONS = JAVA_EXTENSIONS | CS_EXTENSIONS


def find_source_file(pattern):
    """Find a source file matching the pattern."""
    # Try exact match first
    for ext_set, dirs in [(JAVA_EXTENSIONS, ["src"]), (CS_EXTENSIONS, ["Client"])]:
        for d in dirs:
            base = PROJECT_ROOT / d
            if base.exists():
                for f in base.rglob(f"*{pattern}"):
                    if f.suffix in ext_set:
                        return f
    # Fallback: any match
    for f in PROJECT_ROOT.rglob(pattern):
        if f.suffix in ALL_CODE_EXTENSIONS:
            return f
    return None


def find_method_definition(file_path, method_name):
    """Find the line range of a method definition."""
    content = file_path.read_text(encoding="utf-8", errors="replace")
    lines = content.splitlines()

    # Pattern: access_modifier return_type method_name(
    method_pat = re.compile(rf'\b{re.escape(method_name)}\s*\(')

    for i, line in enumerate(lines):
        if method_pat.search(line):
            # Verify it's a definition (has access modifier or return type before it)
            stripped = line.strip()
            if any(kw in stripped for kw in ['public', 'private', 'protected', 'static', 'void',
                                              'int', 'long', 'boolean', 'String', 'byte', 'short',
                                              'Player', 'Item', 'Boss', 'Mob', 'Zone', 'Map',
                                              'Message', 'Session', 'List', 'void']):
                return i + 1
    return None


def find_callers(method_name, exclude_file=None):
    """Find all files that call a given method."""
    callers = []
    call_pattern = re.compile(rf'\b{re.escape(method_name)}\s*\(')

    for ext in ALL_CODE_EXTENSIONS:
        for d in ["src", "Client"]:
            base = PROJECT_ROOT / d
            if not base.exists():
                continue
            for f in base.rglob(f"*{ext}"):
                if exclude_file and f == exclude_file:
                    continue
                try:
                    content = f.read_text(encoding="utf-8", errors="replace")
                    lines = content.splitlines()
                    for i, line in enumerate(lines):
                        if call_pattern.search(line):
                            # Skip comments and definitions
                            stripped = line.strip()
                            if stripped.startswith("//") or stripped.startswith("*"):
                                continue
                            callers.append({
                                "file": str(f.relative_to(PROJECT_ROOT)),
                                "line": i + 1,
                                "text": stripped[:100]
                            })
                except Exception:
                    continue

    return callers


def find_callees(file_path, method_name):
    """Find methods called FROM a given method."""
    content = file_path.read_text(encoding="utf-8", errors="replace")
    lines = content.splitlines()

    # Find method body
    method_pat = re.compile(rf'\b{re.escape(method_name)}\s*\(')
    start = None
    brace_depth = 0
    method_body = []

    for i, line in enumerate(lines):
        if start is None:
            if method_pat.search(line):
                start = i
                brace_depth = 0
        if start is not None:
            brace_depth += line.count('{') - line.count('}')
            method_body.append(line)
            if brace_depth <= 0 and len(method_body) > 1:
                break

    # Extract method calls from body
    call_pattern = re.compile(r'(?:[\w.]+\.)?(\w+)\s*\(')
    java_keywords = {'if', 'for', 'while', 'switch', 'catch', 'return', 'new', 'throw',
                     'synchronized', 'try', 'case', 'super', 'this', 'else', 'break'}

    callees = set()
    for line in method_body:
        stripped = line.strip()
        if stripped.startswith("//") or stripped.startswith("*"):
            continue
        for m in call_pattern.finditer(stripped):
            name = m.group(1)
            if name not in java_keywords and name != method_name and len(name) > 1:
                callees.add(name)

    return sorted(callees)


def analyze_impact(file_pattern, method_name):
    """Full impact analysis for a method."""
    print(f"\n{'='*70}")
    print(f"🎯 IMPACT ANALYSIS — {file_pattern}::{method_name}")
    print(f"{'='*70}")

    # Find source file
    source = find_source_file(file_pattern)
    if source is None:
        print(f"  ❌ Không tìm thấy file: {file_pattern}")
        return

    rel = source.relative_to(PROJECT_ROOT)
    defn_line = find_method_definition(source, method_name)
    print(f"\n  📄 File: {rel}")
    if defn_line:
        print(f"  📍 Định nghĩa tại dòng: {defn_line}")

    # Find callers (WHO calls this method?)
    print(f"\n  📞 AI GỌI TỚI '{method_name}' (Callers — Blast Radius):")
    callers = find_callers(method_name, exclude_file=source)
    if callers:
        # Group by file
        by_file = {}
        for c in callers:
            by_file.setdefault(c["file"], []).append(c)

        for f, calls in sorted(by_file.items()):
            print(f"     📁 {f} ({len(calls)} lần gọi)")
            for c in calls[:3]:
                print(f"        [{c['line']}]: {c['text']}")
            if len(calls) > 3:
                print(f"        ... và {len(calls) - 3} chỗ nữa")
        print(f"     📊 Tổng: {len(callers)} chỗ gọi trong {len(by_file)} file")
    else:
        print(f"     (Không tìm thấy caller nào ngoài file gốc)")

    # Find callees (WHAT does this method call?)
    print(f"\n  📤 '{method_name}' GỌI TỚI (Callees — Dependencies):")
    callees = find_callees(source, method_name)
    if callees:
        for c in callees[:20]:
            print(f"     → {c}()")
        if len(callees) > 20:
            print(f"     ... và {len(callees) - 20} method nữa")
    else:
        print(f"     (Không phân tích được callees)")

    # Risk assessment
    risk = "🟢 LOW" if len(callers) <= 3 else ("🟡 MEDIUM" if len(callers) <= 10 else "🔴 HIGH")
    print(f"\n  ⚠️ Risk Level: {risk} ({len(callers)} callers)")
    if len(callers) > 5:
        print(f"  💡 Khuyến nghị: Thay đổi method này ảnh hưởng tới {len(callers)} chỗ.")
        print(f"     Cân nhắc thêm overload mới thay vì sửa signature cũ.")


def main():
    parser = argparse.ArgumentParser(description="NRO Impact Analyzer")
    parser.add_argument("file_pattern", type=str, help="Tên file hoặc pattern (VD: NPoint.java)")
    parser.add_argument("method_name", type=str, help="Tên method cần phân tích")
    args = parser.parse_args()
    analyze_impact(args.file_pattern, args.method_name)


if __name__ == "__main__":
    main()
