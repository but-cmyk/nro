#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
NRO Stream Diff Tool — Compare write sequence (Server Java) vs read sequence (Client C#)
for a given opcode to detect protocol desync bugs.

Usage:
  python .agents/skills/nro-bugfix/scripts/stream_diff.py <opcode>
  python .agents/skills/nro-bugfix/scripts/stream_diff.py -27
  python .agents/skills/nro-bugfix/scripts/stream_diff.py -27 --verbose
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

# Type size mapping for detecting size mismatches
TYPE_SIZES = {
    "Byte": 1, "SByte": 1, "Boolean": 1, "Bool": 1,
    "Short": 2, "UShort": 2,
    "Int": 4,
    "Long": 8,
    "Float": 4, "Double": 8,
    "UTF": "variable",
}

# Java write → C# read equivalence map
JAVA_TO_CS = {
    "writeByte": "readByte",
    "writeShort": "readShort",
    "writeInt": "readInt",
    "writeLong": "readLong",
    "writeUTF": "readUTF",
    "writeBoolean": "readBoolean",
    "writeFloat": "readFloat",
    "writeDouble": "readDouble",
}

CS_TO_JAVA = {v: k for k, v in JAVA_TO_CS.items()}
# Also handle C# naming variants
CS_TO_JAVA.update({
    "readSByte": "writeByte",
    "readUShort": "writeShort",
    "readBool": "writeBoolean",
})


def extract_rw_ops_from_case(file_path, opcode, direction="write"):
    """Extract read/write operations from a case block for given opcode."""
    if not file_path.exists():
        return None, f"File not found: {file_path}"

    content = file_path.read_text(encoding="utf-8", errors="replace")
    lines = content.splitlines()

    # Find case <opcode>:
    case_pattern = re.compile(rf'case\s+{re.escape(str(opcode))}\s*:')
    ops = []
    start_line = None

    for i, line in enumerate(lines):
        if case_pattern.search(line):
            start_line = i
            break

    if start_line is None:
        return None, f"Opcode {opcode} not found in {file_path.name}"

    # Parse from case to break/next case
    rw_pattern = re.compile(
        r'\.(write|read)(Byte|SByte|Short|UShort|Int|Long|UTF|Boolean|Bool|Float|Double)\s*\(',
        re.IGNORECASE
    )

    brace_depth = 0
    for i in range(start_line, min(start_line + 200, len(lines))):
        line = lines[i]
        brace_depth += line.count('{') - line.count('}')

        for m in rw_pattern.finditer(line):
            op_dir = m.group(1).lower()
            op_type = m.group(2)
            ops.append({
                "line": i + 1,
                "direction": op_dir,
                "type": op_type,
                "text": line.strip()[:120]
            })

        # Stop at break or next case (but not nested)
        stripped = line.strip()
        if stripped == "break;" and brace_depth <= 1:
            break
        if i > start_line and case_pattern.search(line) and brace_depth <= 0:
            break

    return ops, start_line + 1


def normalize_type(type_name):
    """Normalize C#/Java type names for comparison."""
    mapping = {
        "SByte": "Byte",
        "UShort": "Short",
        "Bool": "Boolean",
    }
    return mapping.get(type_name, type_name)


def diff_streams(opcode, verbose=False):
    """Compare Server write sequence vs Client read sequence for an opcode."""
    print(f"\n{'='*70}")
    print(f"🔍 STREAM PROTOCOL DIFF — Opcode {opcode}")
    print(f"{'='*70}")

    # Files to check for WRITE (Server side)
    server_files = [
        PROJECT_ROOT / "src" / "services" / "Service.java",
        PROJECT_ROOT / "src" / "server" / "Controller.java",
    ]
    # Also check handler subdirectories
    for handler_dir in [PROJECT_ROOT / "src" / "services" / "func",
                        PROJECT_ROOT / "src" / "services" / "player",
                        PROJECT_ROOT / "src" / "services" / "tournament"]:
        if handler_dir.exists():
            server_files.extend(handler_dir.glob("*.java"))

    # Files to check for READ (Client side)
    client_files = [
        PROJECT_ROOT / "Client" / "Client" / "Assets" / "Scripts" / "Game1" / "Controller.cs",
    ]
    # Also check handler subdirectories
    handlers_dir = PROJECT_ROOT / "Client" / "Client" / "Assets" / "Scripts" / "Game1" / "Network" / "Handlers"
    if handlers_dir.exists():
        client_files.extend(handlers_dir.glob("*.cs"))

    # Extract Server writes
    server_ops = None
    server_source = None
    for sf in server_files:
        ops, info = extract_rw_ops_from_case(sf, opcode)
        if ops is not None:
            write_ops = [op for op in ops if op["direction"] == "write"]
            if write_ops:
                server_ops = write_ops
                server_source = sf.relative_to(PROJECT_ROOT)
                print(f"\n  📤 Server WRITE — {server_source} (dòng {info})")
                break

    # Extract Client reads
    client_ops = None
    client_source = None
    for cf in client_files:
        ops, info = extract_rw_ops_from_case(cf, opcode)
        if ops is not None:
            read_ops = [op for op in ops if op["direction"] == "read"]
            if read_ops:
                client_ops = read_ops
                client_source = cf.relative_to(PROJECT_ROOT)
                print(f"  📥 Client READ  — {client_source} (dòng {info})")
                break

    if server_ops is None:
        print(f"\n  ⚠️  Không tìm thấy Server write cho opcode {opcode}")
    if client_ops is None:
        print(f"\n  ⚠️  Không tìm thấy Client read cho opcode {opcode}")

    if server_ops is None or client_ops is None:
        return

    # Compare sequences
    print(f"\n  {'#':>3s}  {'Server WRITE':^30s}  {'Client READ':^30s}  {'Status':^10s}")
    print(f"  {'─'*3}  {'─'*30}  {'─'*30}  {'─'*10}")

    max_len = max(len(server_ops), len(client_ops))
    mismatches = 0

    for idx in range(max_len):
        s_type = server_ops[idx]["type"] if idx < len(server_ops) else "—"
        c_type = client_ops[idx]["type"] if idx < len(client_ops) else "—"

        s_norm = normalize_type(s_type) if s_type != "—" else "—"
        c_norm = normalize_type(c_type) if c_type != "—" else "—"

        s_line = f"[{server_ops[idx]['line']}]" if idx < len(server_ops) else ""
        c_line = f"[{client_ops[idx]['line']}]" if idx < len(client_ops) else ""

        if s_type == "—" or c_type == "—":
            status = "⚠️ MISSING"
            mismatches += 1
        elif s_norm == c_norm:
            status = "✅ OK"
        else:
            status = "❌ MISMATCH"
            mismatches += 1

        s_display = f"write{s_type} {s_line}"
        c_display = f"read{c_type} {c_line}"
        print(f"  {idx+1:3d}  {s_display:^30s}  {c_display:^30s}  {status:^10s}")

    print(f"\n  {'='*70}")
    if mismatches == 0:
        print(f"  ✅ PASS — Opcode {opcode}: {len(server_ops)} writes ↔ {len(client_ops)} reads — Đồng bộ hoàn hảo")
    else:
        print(f"  ❌ FAIL — Opcode {opcode}: {mismatches} lỗi desync!")
        print(f"  🔧 Cần sửa để đảm bảo thứ tự write↔read khớp 1:1 từng byte")

    if verbose:
        print(f"\n  📋 Chi tiết Server writes:")
        for op in server_ops:
            print(f"     [{op['line']:4d}] {op['text']}")
        print(f"\n  📋 Chi tiết Client reads:")
        for op in client_ops:
            print(f"     [{op['line']:4d}] {op['text']}")


def main():
    parser = argparse.ArgumentParser(description="NRO Stream Protocol Diff Tool")
    parser.add_argument("opcode", type=str, help="Opcode cần audit (ví dụ: -27, 5, -96)")
    parser.add_argument("--verbose", "-v", action="store_true", help="Hiển thị chi tiết code")
    args = parser.parse_args()
    diff_streams(args.opcode, args.verbose)


if __name__ == "__main__":
    main()
