#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
NRO Bug Diagnostic Tool — Automated root-cause analysis for NRO Java Server & Unity C# Client.

Usage:
  python .agents/skills/nro-bugfix/scripts/diagnose.py --symptom "mô tả triệu chứng"
  python .agents/skills/nro-bugfix/scripts/diagnose.py --stacktrace "paste stacktrace đây"
  python .agents/skills/nro-bugfix/scripts/diagnose.py --opcode -27
  python .agents/skills/nro-bugfix/scripts/diagnose.py --stream-audit src/services/Service.java Service.sendVersionRes
  python .agents/skills/nro-bugfix/scripts/diagnose.py --cs0117 "error CS0117: 'X' does not contain..."
"""

import sys
import os
import re
import json
import argparse
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parents[4]  # up to SRC_NRO_219
sys.path.insert(0, str(PROJECT_ROOT / "tools" / "memory"))

try:
    from memlib import tokenize, force_utf8_stdout
    force_utf8_stdout()
except ImportError:
    def tokenize(s): return s.lower().split()
    def force_utf8_stdout(): pass

# ─── Bug Pattern Database ──────────────────────────────────────────────────────

KNOWN_PATTERNS = [
    {
        "id": "STREAM_DESYNC",
        "symptoms": ["lệch byte", "stream", "readByte", "readInt", "readUTF", "readShort",
                      "corrupt", "garbage", "rác", "sai dữ liệu", "opcode sai", "crash packet"],
        "diagnosis": "Stream Read/Write desync giữa Server Java và Client Unity C#",
        "check": "So sánh thứ tự write trong Service.java với read trong Controller.cs cho cùng opcode",
        "files": ["src/server/Controller.java", "Client/Client/Assets/Scripts/Game1/Controller.cs",
                  "src/services/Service.java", "Client/Client/Assets/Scripts/Game1/Service.cs"],
        "fix_pattern": "Đảm bảo 1:1 byte-level: writeByte↔readByte, writeShort↔readShort, writeInt↔readInt, writeUTF↔readUTF, writeLong↔readLong"
    },
    {
        "id": "DUPE_BUG",
        "symptoms": ["dupe", "nhân bản", "giao dịch", "trade", "combine", "đập đồ",
                      "item trùng", "mất đồ", "thừa đồ", "số lượng sai"],
        "diagnosis": "Bug dupe đồ do thiếu lock đồng bộ trong Trade/Combine hoặc race condition",
        "check": "Kiểm tra synchronized/lock trên toàn bộ flow Trade.tradeItem() và CombineService.combine()",
        "files": ["src/services/CombineService.java", "src/services/func/Trade.java",
                  "src/services/ItemService.java"],
        "fix_pattern": "Bọc synchronized(player) hoặc lock item trước thao tác; DB save + RAM update phải atomic"
    },
    {
        "id": "OVERFLOW_INT",
        "symptoms": ["tràn số", "overflow", "int", "âm", "negative", "-2147483648", "2 tỷ",
                      "sức mạnh 0", "hp 0", "dame 0", "hiển thị sai"],
        "diagnosis": "Tràn số do dùng int thay vì long cho biến tích lũy (HP/dame/exp/tiềm năng/sức mạnh)",
        "check": "Grep 'int hp', 'int dame', 'int sucManh', 'int tiemNang' trong NPoint.java và Player.java",
        "files": ["src/models/player/NPoint.java", "src/models/player/Player.java",
                  "src/services/Service.java"],
        "fix_pattern": "Chuyển int → long cho mọi biến tích lũy; cast (long) trước phép nhân lớn"
    },
    {
        "id": "NPE_ZONE",
        "symptoms": ["NullPointerException", "zone null", "player.zone", "mob null",
                      "ConcurrentModificationException", "mobs null", "players null"],
        "diagnosis": "NPE do player/mob/boss rời zone nhưng vẫn bị tham chiếu trong vòng lặp Zone.update()",
        "check": "Kiểm tra null-check trước khi truy cập player.zone và sử dụng iterator-safe loop",
        "files": ["src/models/map/Zone.java", "src/models/mob/Mob.java", "src/models/boss/Boss.java"],
        "fix_pattern": "Luôn check != null trước truy cập; dùng CopyOnWriteArrayList hoặc snapshot trước khi iterate"
    },
    {
        "id": "UI_FREEZE",
        "symptoms": ["treo", "đơ", "freeze", "xoay tròn", "WaitDlg", "InfoDlg", "loading mãi",
                      "đen màn hình", "không vào game", "chờ mãi"],
        "diagnosis": "Client bị treo do InfoDlg.showWait() không được giải phóng hoặc changeMap sai",
        "check": "Kiểm tra flow sendThongBaoResetWait (opcode -99) và đường dẫn error/timeout",
        "files": ["Client/Client/Assets/Scripts/Game1/Controller.cs",
                  "Client/Client/Assets/Scripts/Game1/GameCanvas.cs",
                  "Client/Client/Assets/Scripts/Game1/Session_ME.cs"],
        "fix_pattern": "Mọi nhánh từ chối/lỗi phải gọi sendThongBaoResetWait trước sendThongBao; timeout phải có handler"
    },
    {
        "id": "GC_PAINT_ALLOC",
        "symptoms": ["giật", "lag", "fps thấp", "gc alloc", "new string", "substring", "paint",
                      "update 60fps", "cấp phát bộ nhớ", "stutter"],
        "diagnosis": "GC allocation trong vòng lặp paint()/update() 60 FPS gây micro-stutter",
        "check": "Grep 'new ', 'Substring', 'string.Format', 'ToString()' trong paint() và update()",
        "files": ["Client/Client/Assets/Scripts/Game1/GameCanvas.cs",
                  "Client/Client/Assets/Scripts/Game1/GameScr.cs"],
        "fix_pattern": "Pre-allocate và cache string/array ngoài loop; dùng StringBuilder pooled"
    },
    {
        "id": "DB_LEAK",
        "symptoms": ["Connection", "timeout", "HikariCP", "pool", "leak", "not available",
                      "30000ms", "rò rỉ kết nối", "connection pool exhausted"],
        "diagnosis": "Rò rỉ Connection do không dùng try-with-resources cho JDBC",
        "check": "Grep pattern 'getConnection()' rồi kiểm tra xem có nằm trong try-with-resources không",
        "files": ["src/database/AlyraManager.java", "src/database/daos/PlayerDAO.java",
                  "src/database/daos/NDVSqlFetcher.java"],
        "fix_pattern": "100% truy vấn DB phải dùng try(Connection c = getConnection(); PreparedStatement ps = ...) {...}"
    },
    {
        "id": "CS0117_UNITY",
        "symptoms": ["CS0117", "does not contain a definition", "error CS", "compile error",
                      "package version", "NavMesh", "cannot resolve"],
        "diagnosis": "Lỗi biên dịch Client Unity do package bị nâng version không tương thích",
        "check": "Kiểm tra Client/Client/Packages/manifest.json xem package version có khớp Unity 2022.3 không",
        "files": ["Client/Client/Packages/manifest.json", "Client/Client/Packages/packages-lock.json",
                  "Client/Client/ProjectSettings/ProjectVersion.txt"],
        "fix_pattern": "git checkout HEAD -- Client/Client/Packages/manifest.json packages-lock.json; xóa Library/PackageCache lỗi"
    },
    {
        "id": "BOSS_STUCK",
        "symptoms": ["boss mắc", "boss không chết", "boss bất tử", "boss không biến mất",
                      "boss lặp", "boss respawn sai", "boss ảo", "leaveMap"],
        "diagnosis": "Boss stuck do không gọi leaveMap() đúng cách hoặc phase change không reset HP",
        "check": "Kiểm tra Boss.leaveMap() có removePlayer + gửi packet biến mất; phase change reset HP",
        "files": ["src/models/boss/Boss.java", "src/managers/boss/BossManager.java"],
        "fix_pattern": "Trước zone = null phải removePlayer(this) + sendPacketBienMat; phase change set hp = hpMax"
    },
    {
        "id": "OPTION_DUPE",
        "symptoms": ["option trùng", "chỉ số nhân đôi", "option nhân bản", "parseItemSafe",
                      "cà chua", "cà rốt", "itemOptions"],
        "diagnosis": "Option item bị nhân bản mỗi lần save/load do không clear default options trước khi parse",
        "check": "Kiểm tra parseItemSafe có clear itemOptions trước khi duyệt nạp từ DB không",
        "files": ["src/services/ItemService.java"],
        "fix_pattern": "Trong parseItemSafe bắt buộc gọi item.itemOptions.clear() trước vòng for nạp options từ JSON"
    }
]


def match_patterns(text):
    """Score each pattern against the input text, return sorted matches."""
    tokens = set(tokenize(text))
    results = []
    for pat in KNOWN_PATTERNS:
        score = 0
        for sym in pat["symptoms"]:
            sym_tokens = set(tokenize(sym))
            overlap = tokens & sym_tokens
            if overlap:
                score += len(overlap) * 2
            # substring match for compound terms
            if sym.lower() in text.lower():
                score += 3
        if score > 0:
            results.append((score, pat))
    results.sort(key=lambda x: x[0], reverse=True)
    return results


def diagnose_symptom(symptom_text):
    """Match symptom text against known bug patterns."""
    print(f"\n{'='*60}")
    print(f"🔍 CHẨN ĐOÁN BUG: '{symptom_text}'")
    print(f"{'='*60}")

    matches = match_patterns(symptom_text)
    if not matches:
        print("\n⚠️  Không khớp pattern đã biết. Thực hiện chẩn đoán thủ công:")
        print("  1. python tools/memory/guardrails.py --test-routing \"<từ khóa>\"")
        print("  2. python tools/memory/memory.py recall \"<mô tả lỗi>\"")
        print("  3. Đọc GEMINI.md của module liên quan")
        return

    for i, (score, pat) in enumerate(matches[:3]):
        rank = "🥇" if i == 0 else ("🥈" if i == 1 else "🥉")
        print(f"\n{rank} [{pat['id']}] (Confidence: {score})")
        print(f"  📋 Chẩn đoán: {pat['diagnosis']}")
        print(f"  🔎 Kiểm tra:  {pat['check']}")
        print(f"  🔧 Fix:       {pat['fix_pattern']}")
        print(f"  📁 Files cần xem:")
        for f in pat["files"]:
            exists = "✅" if (PROJECT_ROOT / f).exists() else "❌"
            print(f"     {exists} {f}")


def diagnose_stacktrace(trace_text):
    """Extract class/method from Java or C# stacktrace and find relevant files."""
    print(f"\n{'='*60}")
    print("🔍 PHÂN TÍCH STACKTRACE")
    print(f"{'='*60}")

    # Extract Java patterns: at package.Class.method(File.java:123)
    java_matches = re.findall(r'at\s+([\w.]+)\.([\w$]+)\(([\w.]+):(\d+)\)', trace_text)
    # Extract C# patterns: at Namespace.Class.Method () [0x...] in /path/to/File.cs:123
    cs_matches = re.findall(r'at\s+([\w.]+)\.([\w<>]+)\s*\(', trace_text)

    # Detect exception type
    exc_match = re.search(r'((?:java\.lang\.|System\.)?(?:\w+Exception|\w+Error))\s*[:\(]?\s*(.*?)$',
                          trace_text, re.MULTILINE)

    if exc_match:
        exc_type = exc_match.group(1)
        exc_msg = exc_match.group(2).strip()
        print(f"\n  ⚡ Exception: {exc_type}")
        if exc_msg:
            print(f"  💬 Message:   {exc_msg}")

        # Auto-diagnose common exceptions
        if "NullPointerException" in exc_type:
            print("  🎯 Nguyên nhân phổ biến: Truy cập object null (player.zone, mob.target, item.template)")
            print("  🔧 Fix: Thêm null-check trước truy cập; kiểm tra lifecycle object")
        elif "ConcurrentModification" in exc_type:
            print("  🎯 Nguyên nhân phổ biến: Duyệt ConcurrentHashMap/ArrayList đồng thời modify")
            print("  🔧 Fix: Dùng CopyOnWriteArrayList hoặc snapshot trước khi iterate")
        elif "ArrayIndexOutOfBounds" in exc_type:
            print("  🎯 Nguyên nhân phổ biến: Index vượt array (item slot, skill list, task index)")
            print("  🔧 Fix: Bounds-check trước khi truy cập array[index]")
        elif "ClassCastException" in exc_type:
            print("  🎯 Nguyên nhân phổ biến: Cast (Pet)player khi player không phải Pet")
            print("  🔧 Fix: Kiểm tra instanceof trước khi cast")
        elif "IOException" in exc_type or "SocketException" in exc_type:
            print("  🎯 Nguyên nhân phổ biến: Client disconnect giữa chừng gửi/nhận packet")
            print("  🔧 Fix: Wrap I/O trong try-catch, kiểm tra channel.isActive() trước khi write")

    if java_matches:
        print(f"\n  📍 Call stack (Java):")
        seen_files = set()
        for pkg_cls, method, filename, line in java_matches[:8]:
            print(f"     → {pkg_cls}.{method}() [{filename}:{line}]")
            # Try to find actual file
            java_file = filename.replace(".java", "")
            for candidate in PROJECT_ROOT.rglob(filename):
                rel = candidate.relative_to(PROJECT_ROOT)
                if str(rel) not in seen_files:
                    seen_files.add(str(rel))
                    print(f"       📁 {rel}")

    if cs_matches:
        print(f"\n  📍 Call stack (C#):")
        for ns_cls, method in cs_matches[:8]:
            print(f"     → {ns_cls}.{method}()")


def audit_stream_opcode(opcode):
    """Find all Server write and Client read for a given opcode."""
    print(f"\n{'='*60}")
    print(f"🔍 STREAM AUDIT — Opcode {opcode}")
    print(f"{'='*60}")

    server_ctrl = PROJECT_ROOT / "src" / "server" / "Controller.java"
    client_ctrl = PROJECT_ROOT / "Client" / "Client" / "Assets" / "Scripts" / "Game1" / "Controller.cs"
    server_svc = PROJECT_ROOT / "src" / "services" / "Service.java"

    for label, fpath in [("Server Controller.java", server_ctrl),
                         ("Client Controller.cs", client_ctrl),
                         ("Server Service.java", server_svc)]:
        if not fpath.exists():
            print(f"\n  ❌ {label}: File không tồn tại")
            continue

        content = fpath.read_text(encoding="utf-8", errors="replace")
        lines = content.splitlines()

        # Search for case <opcode>: or case <opcode> : or == <opcode>
        pattern = re.compile(rf'case\s+{re.escape(str(opcode))}\s*:', re.IGNORECASE)
        hits = [(i+1, line.strip()) for i, line in enumerate(lines) if pattern.search(line)]

        if hits:
            print(f"\n  📄 {label}:")
            for line_num, line_text in hits:
                print(f"     Dòng {line_num}: {line_text}")
                # Show next 20 lines for context
                context_end = min(line_num + 20, len(lines))
                rw_ops = []
                for j in range(line_num, context_end):
                    l = lines[j].strip()
                    if re.search(r'(write|read)(Byte|Short|Int|Long|UTF|Boolean|SByte|UShort)', l, re.IGNORECASE):
                        rw_ops.append((j+1, l))
                    if l.startswith("break;") or l.startswith("case "):
                        break
                if rw_ops:
                    print(f"     Stream operations:")
                    for ln, op in rw_ops:
                        direction = "📤 WRITE" if "write" in op.lower() else "📥 READ"
                        print(f"       {direction} [{ln}]: {op[:100]}")


def diagnose_cs0117(error_text):
    """Parse CS0117 error and diagnose package compatibility."""
    print(f"\n{'='*60}")
    print(f"🔍 CHẨN ĐOÁN CS0117 UNITY")
    print(f"{'='*60}")

    # Extract type and member: 'TypeName' does not contain a definition for 'MemberName'
    match = re.search(r"'(\w+)'\s+does not contain a definition for '(\w+)'", error_text)
    if match:
        type_name = match.group(1)
        member_name = match.group(2)
        print(f"\n  ❌ Type '{type_name}' thiếu member '{member_name}'")

    # Check manifest.json
    manifest = PROJECT_ROOT / "Client" / "Client" / "Packages" / "manifest.json"
    version_file = PROJECT_ROOT / "Client" / "Client" / "ProjectSettings" / "ProjectVersion.txt"

    if version_file.exists():
        ver_content = version_file.read_text(encoding="utf-8")
        ver_match = re.search(r'm_EditorVersion:\s*(.+)', ver_content)
        if ver_match:
            print(f"  🎮 Unity Version: {ver_match.group(1).strip()}")

    if manifest.exists():
        try:
            data = json.loads(manifest.read_text(encoding="utf-8"))
            deps = data.get("dependencies", {})
            print(f"\n  📦 Package versions trong manifest.json:")
            for pkg, ver in deps.items():
                if pkg.startswith("com.unity.ai") or pkg.startswith("com.unity.ide") or \
                   pkg.startswith("com.unity.visual") or pkg.startswith("com.unity.multiplayer"):
                    print(f"     {pkg}: {ver}")
        except Exception as e:
            print(f"  ⚠️ Không đọc được manifest.json: {e}")

    print(f"\n  🔧 Cách sửa:")
    print(f"     1. git checkout HEAD -- Client/Client/Packages/manifest.json Client/Client/Packages/packages-lock.json")
    print(f"     2. Xóa Library/PackageCache/<package-lỗi>")
    print(f"     3. Mở Unity để re-resolve packages")


def stream_audit_method(java_file, method_name):
    """Parse a specific Java method and list all read/write operations in order."""
    print(f"\n{'='*60}")
    print(f"🔍 STREAM AUDIT — {java_file}::{method_name}")
    print(f"{'='*60}")

    fpath = PROJECT_ROOT / java_file
    if not fpath.exists():
        print(f"  ❌ File không tồn tại: {java_file}")
        return

    content = fpath.read_text(encoding="utf-8", errors="replace")
    lines = content.splitlines()

    # Find method start
    method_pat = re.compile(rf'\b{re.escape(method_name)}\s*\(')
    found = False
    brace_depth = 0
    method_lines = []

    for i, line in enumerate(lines):
        if not found:
            if method_pat.search(line):
                found = True
                brace_depth = 0
        if found:
            brace_depth += line.count('{') - line.count('}')
            method_lines.append((i + 1, line))
            if brace_depth <= 0 and len(method_lines) > 1:
                break

    if not method_lines:
        print(f"  ❌ Method '{method_name}' không tìm thấy trong {java_file}")
        return

    print(f"\n  📄 Method '{method_name}' (dòng {method_lines[0][0]}-{method_lines[-1][0]})")
    print(f"  Stream operations theo thứ tự:")

    rw_pattern = re.compile(r'\.(write|read)(Byte|SByte|Short|UShort|Int|Long|UTF|Boolean|Float|Double)\s*\(', re.IGNORECASE)
    seq = 1
    for ln, text in method_lines:
        for m in rw_pattern.finditer(text):
            direction = "📤 WRITE" if m.group(1).lower() == "write" else "📥 READ"
            data_type = m.group(2)
            print(f"     {seq:2d}. {direction} {data_type:8s} [{ln}]: {text.strip()[:90]}")
            seq += 1


def main():
    parser = argparse.ArgumentParser(description="NRO Bug Diagnostic Tool")
    parser.add_argument("--symptom", "-s", type=str, help="Mô tả triệu chứng bug")
    parser.add_argument("--stacktrace", "-t", type=str, help="Paste stacktrace để phân tích")
    parser.add_argument("--opcode", "-o", type=str, help="Audit stream cho opcode cụ thể (ví dụ: -27)")
    parser.add_argument("--cs0117", type=str, help="Phân tích lỗi CS0117 Unity")
    parser.add_argument("--stream-audit", nargs=2, metavar=("FILE", "METHOD"),
                        help="Audit read/write stream trong method cụ thể")
    parser.add_argument("--list-patterns", action="store_true", help="Liệt kê tất cả bug patterns đã biết")

    args = parser.parse_args()

    if args.list_patterns:
        print(f"\n{'='*60}")
        print(f"📚 TẤT CẢ BUG PATTERNS ĐÃ BIẾT ({len(KNOWN_PATTERNS)} patterns)")
        print(f"{'='*60}")
        for pat in KNOWN_PATTERNS:
            print(f"\n  [{pat['id']}]")
            print(f"    Chẩn đoán: {pat['diagnosis']}")
            print(f"    Từ khóa:   {', '.join(pat['symptoms'][:5])}...")
        return

    if args.symptom:
        diagnose_symptom(args.symptom)
    elif args.stacktrace:
        diagnose_stacktrace(args.stacktrace)
    elif args.opcode:
        audit_stream_opcode(args.opcode)
    elif args.cs0117:
        diagnose_cs0117(args.cs0117)
    elif args.stream_audit:
        stream_audit_method(args.stream_audit[0], args.stream_audit[1])
    else:
        parser.print_help()


if __name__ == "__main__":
    main()

