#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
NRO Static Codebase Indexer (Stage 1 of Hybrid Map-Reduce Code Intelligence).
Deterministic structural analysis of 100% of the NRO Java Server, Unity C# Client,
SQL Database Schemas, Redis Config, and Tooling.

Outputs under docs/atlas/_index/:
  - 00_INDEX_SUMMARY.md
  - 01_code_inventory_and_slices.json / .md
  - 02_protocol_matrix.json / .md
  - 03_database_and_redis_map.json / .md
  - 04_dependency_matrix.json / .md
"""

import json
import os
import re
import sys
from collections import defaultdict
from datetime import datetime
from pathlib import Path

if sys.platform == "win32":
    try:
        sys.stdout.reconfigure(encoding="utf-8")
    except Exception:
        pass

PROJECT_ROOT = Path(__file__).resolve().parent.parent
OUT_DIR = PROJECT_ROOT / "docs" / "atlas" / "_index"

EXCLUDE_DIR_NAMES = {
    ".git", "Library", "Temp", "obj", "Logs", "Build", "builds",
    "__pycache__", "node_modules", "temp_assets", "unused_jars",
    "dist", "out", ".gradle",
}

MAX_SLICE_LINES = 6000
GOD_FILE_THRESHOLD = 2800
GOD_CHUNK_TARGET = 2800


# ---------------------------------------------------------------------------
# Comment / String stripper (line-preserving) + Brace/Method Extractor
# ---------------------------------------------------------------------------
def strip_comments_and_strings_preserve_lines(text: str) -> str:
    """Replace comments and string/char literal contents with spaces, keeping newlines and quotes."""
    out = []
    i = 0
    n = len(text)
    state = "CODE"  # CODE, LINE_COMMENT, BLOCK_COMMENT, STR, CHAR, VERBATIM_STR
    while i < n:
        ch = text[i]
        nxt = text[i + 1] if i + 1 < n else ""
        if state == "CODE":
            if ch == "/" and nxt == "/":
                out.append("  ")
                i += 2
                state = "LINE_COMMENT"
            elif ch == "/" and nxt == "*":
                out.append("  ")
                i += 2
                state = "BLOCK_COMMENT"
            elif ch == "@" and nxt == '"':
                out.append('"')
                i += 2
                state = "VERBATIM_STR"
            elif ch == '"':
                out.append('"')
                i += 1
                state = "STR"
            elif ch == "'":
                out.append("'")
                i += 1
                state = "CHAR"
            else:
                out.append(ch)
                i += 1
        elif state == "LINE_COMMENT":
            if ch == "\n":
                out.append("\n")
                state = "CODE"
            else:
                out.append(" ")
            i += 1
        elif state == "BLOCK_COMMENT":
            if ch == "*" and nxt == "/":
                out.append("  ")
                i += 2
                state = "CODE"
            elif ch == "\n":
                out.append("\n")
                i += 1
            else:
                out.append(" ")
                i += 1
        elif state == "STR":
            if ch == "\\" and i + 1 < n:
                out.append("  " if nxt != "\n" else " \n")
                i += 2
            elif ch == '"':
                out.append('"')
                i += 1
                state = "CODE"
            elif ch == "\n":
                out.append("\n")
                i += 1
                state = "CODE"
            else:
                out.append(" ")
                i += 1
        elif state == "VERBATIM_STR":
            if ch == '"' and nxt == '"':
                out.append("  ")
                i += 2
            elif ch == '"':
                out.append('"')
                i += 1
                state = "CODE"
            elif ch == "\n":
                out.append("\n")
                i += 1
            else:
                out.append(" ")
                i += 1
        elif state == "CHAR":
            if ch == "\\" and i + 1 < n:
                out.append("  ")
                i += 2
            elif ch == "'":
                out.append("'")
                i += 1
                state = "CODE"
            elif ch == "\n":
                out.append("\n")
                i += 1
                state = "CODE"
            else:
                out.append(" ")
                i += 1
    return "".join(out)


TYPE_DECL_RE = re.compile(
    r'\b(?:public|private|protected|internal|static|final|abstract|sealed|partial|new|\s)+'
    r'(class|interface|enum|struct)\s+([A-Za-z_][A-Za-z0-9_]*)'
)

METHOD_SIG_RE = re.compile(
    r'([A-Za-z_][A-Za-z0-9_<>\[\],\s\.\?]*?)\s+([A-Za-z_][A-Za-z0-9_]*)\s*\(([^;{}]*)\)\s*(?:throws\s+[^{;]+)?$'
)

CONTROL_KEYWORDS = {"if", "for", "foreach", "while", "switch", "catch", "using", "lock", "synchronized", "fixed", "else", "do", "try", "return", "new", "throw"}


def analyze_code_structure(text: str, lang: str):
    """Extract package/namespace, imports/usings, types, methods with (start, end), and constant counts."""
    clean = strip_comments_and_strings_preserve_lines(text)
    lines = clean.splitlines()
    total_lines = len(text.splitlines())

    pkg_or_ns = ""
    imports = []
    if lang == "java":
        m = re.search(r'^\s*package\s+([A-Za-z0-9_.]+)\s*;', clean, re.M)
        if m:
            pkg_or_ns = m.group(1)
        imports = re.findall(r'^\s*import\s+(?:static\s+)?([A-Za-z0-9_.*]+)\s*;', clean, re.M)
    elif lang == "cs":
        m = re.search(r'^\s*namespace\s+([A-Za-z0-9_.]+)', clean, re.M)
        if m:
            pkg_or_ns = m.group(1)
        imports = re.findall(r'^\s*using\s+(?:static\s+)?([A-Za-z0-9_.]+)\s*;', clean, re.M)

    const_count = len(re.findall(r'\b(?:public|private|protected|internal)?\s*(?:static\s+final|const)\b', clean))

    # Brace stack tracking to detect type bodies and method spans
    types = []
    methods = []
    brace_depth = 0
    scope_stack = []  # entries: (kind, name, depth_inside, start_line)
    # In C#, namespace {...} wraps types at depth 1; in Java, top-level type is at depth 0
    pending_header = []
    pending_start_line = 1

    # Token-level scan on clean text with line numbers
    i = 0
    n = len(clean)
    cur_line = 1
    buf = []
    buf_start_line = 1
    in_method = False

    def current_container_kind():
        for entry in reversed(scope_stack):
            if entry[0] in ("class", "interface", "enum", "struct", "namespace", "method"):
                return entry[0]
        return "root"

    while i < n:
        ch = clean[i]
        if ch == "\n":
            cur_line += 1
            buf.append(" ")
            i += 1
            continue

        if ch == ";":
            buf.clear()
            buf_start_line = cur_line
            i += 1
            continue

        if ch == "{":
            header = " ".join("".join(buf).split())
            buf.clear()
            brace_depth += 1

            # Check namespace (C#)
            ns_m = re.search(r'\bnamespace\s+([A-Za-z0-9_.]+)\s*$', header) if lang == "cs" else None
            type_m = TYPE_DECL_RE.search(header)

            if ns_m:
                scope_stack.append(("namespace", ns_m.group(1), brace_depth, buf_start_line))
            elif type_m and current_container_kind() in ("root", "namespace", "class", "struct", "interface"):
                t_kind, t_name = type_m.group(1), type_m.group(2)
                types.append({"kind": t_kind, "name": t_name, "start_line": buf_start_line, "end_line": None})
                scope_stack.append((t_kind, t_name, brace_depth, buf_start_line))
            elif current_container_kind() in ("class", "struct", "interface", "enum"):
                # Potential method / constructor / static block / property
                # Clean annotations / attributes like [SerializeField] or @Override
                hdr_no_attr = re.sub(r'@[A-Za-z_][A-Za-z0-9_]*(\([^)]*\))?', ' ', header)
                hdr_no_attr = re.sub(r'\[[^\]]+\]', ' ', hdr_no_attr).strip()
                first_tok = hdr_no_attr.split("(")[0].strip().split()[-1] if "(" in hdr_no_attr and hdr_no_attr.split("(")[0].strip() else ""
                if "(" in hdr_no_attr and first_tok not in CONTROL_KEYWORDS and "=" not in hdr_no_attr.split("(")[0]:
                    # Extract method name & compact signature
                    m_name = first_tok
                    sig = hdr_no_attr[:140]
                    methods.append({
                        "name": m_name,
                        "signature": sig,
                        "start_line": buf_start_line,
                        "end_line": cur_line,  # updated on close brace
                    })
                    scope_stack.append(("method", m_name, brace_depth, buf_start_line, len(methods) - 1))
                else:
                    scope_stack.append(("block", "block", brace_depth, buf_start_line))
            else:
                scope_stack.append(("block", "block", brace_depth, buf_start_line))

            buf_start_line = cur_line
            i += 1
            continue

        if ch == "}":
            buf.clear()
            while scope_stack and scope_stack[-1][2] == brace_depth:
                entry = scope_stack.pop()
                if entry[0] == "method":
                    methods[entry[4]]["end_line"] = cur_line
                elif entry[0] in ("class", "interface", "enum", "struct"):
                    for t in reversed(types):
                        if t["name"] == entry[1] and t["end_line"] is None:
                            t["end_line"] = cur_line
                            break
            brace_depth = max(0, brace_depth - 1)
            buf_start_line = cur_line
            i += 1
            continue

        if not buf and ch.isspace():
            buf_start_line = cur_line
        buf.append(ch)
        i += 1

    for t in types:
        if t["end_line"] is None:
            t["end_line"] = total_lines

    return {
        "package_or_namespace": pkg_or_ns,
        "imports": imports,
        "types": types,
        "methods": methods,
        "const_count": const_count,
        "total_lines": total_lines,
    }


# ---------------------------------------------------------------------------
# Domain Classification & Smart Slicing
# ---------------------------------------------------------------------------
def classify_java_domain(rel_path: str) -> str:
    p = rel_path.replace("\\", "/")
    if p.startswith(("src/network/", "src/server/", "src/interfaces/", "src/data/")):
        return "server_network_core"
    if p.startswith(("src/database/", "docker/")):
        return "server_database_persistence"
    if p.startswith(("src/models/player/", "src/models/skill/", "src/models/intrinsic/", "src/models/radar/")) or p == "src/services/SkillService.java" or p == "src/services/PetService.java" or p.startswith("src/services/player/"):
        return "server_player_combat"
    if any(k in p for k in ["ItemService", "CombineService", "Trade", "TransactionService", "ShopService", "ConsignShop", "LuckyRound", "ConsignItem", "src/models/item/", "src/models/combine/", "src/models/shop/", "src/services/func/"]):
        return "server_economy_items"
    if p.startswith(("src/models/map/", "src/models/mob/", "src/models/npc/", "src/managers/map/", "src/services/map/")):
        return "server_world_map_mob"
    if p.startswith(("src/models/boss/", "src/models/phoban/", "src/models/tournament/", "src/models/matches/", "src/managers/boss/", "src/services/phoban/", "src/services/tournament/")):
        return "server_boss_dungeon_pvp"
    if p.startswith(("src/models/task/", "src/models/clan/", "src/event/", "src/consts/")) or "TaskService" in p or "ClanService" in p or "Achievement" in p:
        return "server_task_clan_consts"
    if p.startswith(("src/bot/", "src/telemetry/", "src/utils/", "src/tools/", "src/testddos/", "src/managers/", "src/services/")):
        return "server_services_bot_telemetry"
    return "tooling_and_tests"


def classify_cs_domain(rel_path: str) -> str:
    p = rel_path.replace("\\", "/")
    name = Path(p).name
    if any(k in p for k in ["/Network/", "Controller.cs", "Controller2.cs", "Service.cs", "Session_ME", "Message.cs", "myReader.cs", "myWriter.cs", "NetworkOpcodes.cs", "Cmd.cs"]):
        return "client_network_protocol"
    if name in ("Panel.cs", "ChatPopup.cs", "InfoDlg.cs", "PopUp.cs", "Menu.cs", "Command.cs") or "/God/" in p:
        return "client_ui_god_mods"
    if name in ("Main.cs", "GameCanvas.cs", "MotherCanvas.cs", "mGraphics.cs", "Image.cs", "SmallImage.cs", "mFont.cs", "Sound.cs", "SoundMn.cs", "MyAudioClip.cs", "MainThreadDispatcher.cs", "RMS.cs", "mSystem.cs"):
        return "client_core_render_audio"
    return "client_gameplay_entities"


def slice_large_file(file_rec: dict, target_lines: int = GOD_CHUNK_TARGET) -> list:
    """Split a large file cleanly along non-overlapping method boundaries (or fixed chunks for data-only files)."""
    total = file_rec["lines"]
    methods = sorted(file_rec["methods"], key=lambda m: (m["start_line"], -m["end_line"]))
    if total <= GOD_FILE_THRESHOLD:
        return [{
            "path": file_rec["path"],
            "start_line": 1,
            "end_line": total,
            "lines": total,
            "is_partial": False,
            "methods_in_slice": [m["name"] for m in methods[:40]],
            "total_methods": len(methods),
        }]

    # Keep only outer non-overlapping method spans
    outer_methods = []
    max_end_seen = 0
    for m in methods:
        if m["end_line"] > m["start_line"] and m["end_line"] > max_end_seen:
            outer_methods.append((m["start_line"], m["end_line"], m["name"]))
            max_end_seen = m["end_line"]

    # Pure constant/data file with no large methods (e.g., ConstTask.java) -> slice by fixed line chunks
    if len(outer_methods) <= 2:
        chunks = []
        s = 1
        while s <= total:
            e = min(total, s + target_lines - 1)
            if total - e < 400:
                e = total
            chunks.append({
                "path": file_rec["path"],
                "start_line": s,
                "end_line": e,
                "lines": e - s + 1,
                "is_partial": total > e or s > 1,
                "methods_in_slice": [m["name"] for m in methods[:20]],
                "total_methods": len(methods),
            })
            s = e + 1
        return chunks

    chunks = []
    cur_start = 1
    cur_methods = []
    for ms, me, mname in outer_methods:
        cur_methods.append(mname)
        span = me - cur_start + 1
        remaining = total - me
        if span >= target_lines and remaining > 400:
            chunks.append({
                "path": file_rec["path"],
                "start_line": cur_start,
                "end_line": me,
                "lines": span,
                "is_partial": True,
                "methods_in_slice": cur_methods[:50],
                "total_methods": len(cur_methods),
            })
            cur_start = me + 1
            cur_methods = []

    if cur_start <= total:
        chunks.append({
            "path": file_rec["path"],
            "start_line": cur_start,
            "end_line": total,
            "lines": total - cur_start + 1,
            "is_partial": len(chunks) > 0,
            "methods_in_slice": cur_methods[:50],
            "total_methods": len(cur_methods),
        })

    return chunks


def build_worker_batches(file_records: list) -> list:
    """Pack all files (and large file chunks) into clean worker batches <= MAX_SLICE_LINES."""
    by_domain = defaultdict(list)
    for rec in file_records:
        for chunk in slice_large_file(rec):
            by_domain[rec["domain"]].append((rec["domain"], chunk))

    batches = []
    for domain in sorted(by_domain.keys()):
        items = [x[1] for x in by_domain[domain]]
        cur_items = []
        cur_lines = 0
        part_idx = 1
        for item in items:
            if cur_items and cur_lines + item["lines"] > MAX_SLICE_LINES:
                batches.append({
                    "batch_id": f"{domain}__p{part_idx:02d}",
                    "domain": domain,
                    "total_lines": cur_lines,
                    "chunk_count": len(cur_items),
                    "chunks": cur_items,
                })
                part_idx += 1
                cur_items = []
                cur_lines = 0
            cur_items.append(item)
            cur_lines += item["lines"]
        if cur_items:
            batches.append({
                "batch_id": f"{domain}__p{part_idx:02d}",
                "domain": domain,
                "total_lines": cur_lines,
                "chunk_count": len(cur_items),
                "chunks": cur_items,
            })
    return batches


# ---------------------------------------------------------------------------
# Protocol Opcode Extractor
# ---------------------------------------------------------------------------
def parse_network_opcodes(path: Path) -> dict:
    """Parse NetworkOpcodes.cs into {const_name: int_val} and {int_val: [names]}."""
    if not path.exists():
        return {"by_name": {}, "by_id": {}}
    text = path.read_text("utf-8", errors="ignore")
    by_name = {}
    by_id = defaultdict(list)
    for name, val in re.findall(r'public\s+const\s+sbyte\s+([A-Za-z0-9_]+)\s*=\s*(-?\d+)\s*;', text):
        ival = int(val)
        by_name[name] = ival
        by_id[ival].append(name)
    return {"by_name": by_name, "by_id": dict(by_id)}


def extract_switch_cases(path: Path, switch_pattern: str) -> list:
    """Extract top-level case labels + line numbers inside the target switch statement."""
    if not path.exists():
        return []
    text = path.read_text("utf-8", errors="ignore")
    clean = strip_comments_and_strings_preserve_lines(text)
    lines = clean.splitlines()

    m = re.search(switch_pattern, clean)
    if not m:
        return []

    # Find opening brace of switch
    idx = clean.find("{", m.end())
    if idx == -1:
        return []

    start_line = clean[:idx].count("\n") + 1
    # Walk braces to find matching switch close while capturing case statements at depth == 1
    depth = 1
    pos = idx + 1
    n = len(clean)
    switch_start = idx + 1
    switch_end = n
    while pos < n and depth > 0:
        if clean[pos] == "{":
            depth += 1
        elif clean[pos] == "}":
            depth -= 1
            if depth == 0:
                switch_end = pos
                break
        pos += 1

    switch_body = clean[switch_start:switch_end]
    # Track relative brace depth inside switch body (0 = direct child of switch)
    cases = []
    rel_depth = 0
    cur_line = start_line
    for offset, raw_line in enumerate(switch_body.splitlines()):
        ln = start_line + offset
        stripped = raw_line.strip()
        if rel_depth == 0:
            # Handle sbyte.MinValue (-128) and sbyte.MaxValue (127)
            for cm in re.finditer(r'\bcase\s+([^:]+):', stripped):
                lbl = cm.group(1).strip()
                opcode_val = None
                if lbl == "sbyte.MinValue":
                    opcode_val = -128
                elif lbl == "sbyte.MaxValue":
                    opcode_val = 127
                elif re.fullmatch(r'-?\d+', lbl):
                    opcode_val = int(lbl)
                cases.append({"label": lbl, "opcode": opcode_val, "line": ln})
        rel_depth += raw_line.count("{") - raw_line.count("}")
    return cases


def extract_protocol_matrix() -> dict:
    opcodes_cs_path = PROJECT_ROOT / "Client/Client/Assets/Scripts/Game1/Constants/NetworkOpcodes.cs"
    const_info = parse_network_opcodes(opcodes_cs_path)
    by_name = const_info["by_name"]

    server_handlers = defaultdict(list)
    client_handlers = defaultdict(list)
    server_senders = defaultdict(list)
    client_senders = defaultdict(list)

    # 1. Server Controller.java
    srv_ctrl = PROJECT_ROOT / "src/server/Controller.java"
    for c in extract_switch_cases(srv_ctrl, r'switch\s*\(\s*cmd\s*\)'):
        if c["opcode"] is not None:
            server_handlers[c["opcode"]].append(f"src/server/Controller.java:{c['line']}")

    # 2. ServerPacketDispatcher.java
    srv_disp = PROJECT_ROOT / "src/server/network/ServerPacketDispatcher.java"
    if srv_disp.exists():
        for idx, line in enumerate(srv_disp.read_text("utf-8", errors="ignore").splitlines(), 1):
            m = re.search(r'register\s*\(\s*(?:\(byte\)\s*)?(-?\d+)\s*,\s*([A-Za-z0-9_]+)', line)
            if m:
                server_handlers[int(m.group(1))].append(f"ServerPacketDispatcher({m.group(2)}):{idx}")

    # 3. Client Controller.cs & Controller2.cs
    cli_ctrl = PROJECT_ROOT / "Client/Client/Assets/Scripts/Game1/Controller.cs"
    for c in extract_switch_cases(cli_ctrl, r'switch\s*\(\s*msg\.command\s*\)'):
        if c["opcode"] is not None:
            client_handlers[c["opcode"]].append(f"Client/Controller.cs:{c['line']}")

    cli_ctrl2 = PROJECT_ROOT / "Client/Client/Assets/Scripts/Game1/Assets/src/f/Controller2.cs"
    for c in extract_switch_cases(cli_ctrl2, r'switch\s*\(\s*msg\.command\s*\)'):
        if c["opcode"] is not None:
            client_handlers[c["opcode"]].append(f"Client/Controller2.cs:{c['line']}")

    # 4. Client PacketDispatcher.cs
    cli_disp = PROJECT_ROOT / "Client/Client/Assets/Scripts/Game1/Network/PacketDispatcher.cs"
    if cli_disp.exists():
        for idx, line in enumerate(cli_disp.read_text("utf-8", errors="ignore").splitlines(), 1):
            m = re.search(r'Register\s*\(\s*([A-Za-z0-9_]+)\s*,\s*([^)]+)\)', line)
            if m:
                hvar = m.group(1)
                for arg in m.group(2).split(","):
                    arg = arg.strip()
                    op = None
                    if arg.startswith("NetworkOpcodes."):
                        op = by_name.get(arg.split(".", 1)[1])
                    elif re.fullmatch(r'-?\d+', arg):
                        op = int(arg)
                    if op is not None:
                        client_handlers[op].append(f"PacketDispatcher({hvar}):{idx}")

    # 5. Outgoing new Message(...) scan across Server & Client
    msg_re = re.compile(r'new\s+Message\s*\(\s*(?:\((?:s?byte)\)\s*)?(-?\d+|NetworkOpcodes\.[A-Za-z0-9_]+)\s*\)')
    for base, target_map in [(PROJECT_ROOT / "src", server_senders), (PROJECT_ROOT / "Client/Client/Assets/Scripts", client_senders)]:
        if not base.exists():
            continue
        for p in base.rglob("*"):
            if not p.is_file() or p.suffix not in (".java", ".cs"):
                continue
            rel = str(p.relative_to(PROJECT_ROOT)).replace("\\", "/")
            for idx, line in enumerate(p.read_text("utf-8", errors="ignore").splitlines(), 1):
                if line.lstrip().startswith("//"):
                    continue
                for m in msg_re.finditer(line):
                    tok = m.group(1)
                    op = by_name.get(tok.split(".", 1)[1]) if tok.startswith("NetworkOpcodes.") else int(tok)
                    if op is not None:
                        target_map[op].append(f"{rel}:{idx}")

    all_ops = sorted(set(server_handlers) | set(client_handlers) | set(server_senders) | set(client_senders) | set(const_info["by_id"]))
    matrix = []
    anomalies = []
    for op in all_ops:
        names = const_info["by_id"].get(op, [])
        sh = server_handlers.get(op, [])
        ss = server_senders.get(op, [])
        ch = client_handlers.get(op, [])
        cs = client_senders.get(op, [])
        flags = []
        if cs and not sh:
            flags.append("CLIENT_SENDS_BUT_NO_SERVER_HANDLER")
        if ss and not ch:
            flags.append("SERVER_SENDS_BUT_NO_CLIENT_HANDLER")
        if len(sh) > 1:
            flags.append("MULTIPLE_SERVER_HANDLERS")
        if len(ch) > 1:
            flags.append("MULTIPLE_CLIENT_HANDLERS")
        rec = {
            "opcode": op,
            "const_names": names,
            "server_handlers": sh,
            "server_senders_count": len(ss),
            "server_senders_sample": ss[:5],
            "client_handlers": ch,
            "client_senders_count": len(cs),
            "client_senders_sample": cs[:5],
            "flags": flags,
        }
        matrix.append(rec)
        if flags:
            anomalies.append(rec)

    return {
        "total_opcodes_observed": len(matrix),
        "network_opcodes_constants": len(by_name),
        "anomalies_count": len(anomalies),
        "matrix": matrix,
    }


# ---------------------------------------------------------------------------
# Database & Redis Extractor
# ---------------------------------------------------------------------------
def extract_database_and_redis() -> dict:
    tables = {}
    sql_files = sorted((PROJECT_ROOT / "docker").rglob("*.sql")) if (PROJECT_ROOT / "docker").exists() else []
    create_re = re.compile(
        r'CREATE\s+TABLE\s+(?:IF\s+NOT\s+EXISTS\s+)?[`"]?([A-Za-z0-9_]+)[`"]?\s*\((.*?)\)\s*(?:ENGINE\s*=\s*([A-Za-z0-9_]+))?',
        re.I | re.S,
    )
    for sf in sql_files:
        rel = str(sf.relative_to(PROJECT_ROOT)).replace("\\", "/")
        txt = sf.read_text("utf-8", errors="ignore")
        for m in create_re.finditer(txt):
            tname = m.group(1)
            body = m.group(2)
            engine = m.group(3) or "unknown"
            cols = []
            indexes = []
            for raw_l in body.splitlines():
                l = raw_l.strip().rstrip(",")
                if not l:
                    continue
                upper = l.upper()
                if upper.startswith(("PRIMARY KEY", "KEY ", "UNIQUE ", "INDEX ", "CONSTRAINT ", "FULLTEXT ")):
                    indexes.append(l[:140])
                else:
                    cm = re.match(r'^[`"]?([A-Za-z0-9_]+)[`"]?\s+([^\s,]+(?:\([^)]+\))?)', l)
                    if cm:
                        cols.append({"name": cm.group(1), "type": cm.group(2)})
            tables[tname] = {
                "defined_in": rel,
                "engine": engine,
                "column_count": len(cols),
                "columns": cols,
                "indexes": indexes,
                "referenced_by_java": [],
            }

    # Scan Java codebase for SQL table usage, JDBC calls, Redis usage
    jdbc_hotspots = defaultdict(lambda: {"getConnection": 0, "try_with_resources": 0, "executeQuery": 0, "executeUpdate": 0, "redis_refs": 0})
    table_names = set(tables.keys())
    sql_ref_re = re.compile(r'\b(?:FROM|INTO|UPDATE|JOIN)\s+[`"]?([A-Za-z0-9_]+)[`"]?', re.I)

    for jf in sorted((PROJECT_ROOT / "src").rglob("*.java")):
        rel = str(jf.relative_to(PROJECT_ROOT)).replace("\\", "/")
        txt = jf.read_text("utf-8", errors="ignore")
        has_jdbc = False
        for kw in ("getConnection", "executeQuery", "executeUpdate"):
            c = txt.count(kw)
            if c:
                jdbc_hotspots[rel][kw] += c
                has_jdbc = True
        if has_jdbc:
            jdbc_hotspots[rel]["try_with_resources"] = len(re.findall(r'try\s*\([^)]*getConnection', txt))
        redis_c = len(re.findall(r'\b(?:Jedis|Redis|redis)\b', txt))
        if redis_c:
            jdbc_hotspots[rel]["redis_refs"] = redis_c

        for tm in sql_ref_re.finditer(txt):
            tbl = tm.group(1)
            if tbl in table_names and rel not in tables[tbl]["referenced_by_java"]:
                tables[tbl]["referenced_by_java"].append(rel)

    return {
        "sql_files": [str(f.relative_to(PROJECT_ROOT)).replace("\\", "/") for f in sql_files],
        "table_count": len(tables),
        "tables": tables,
        "jdbc_and_redis_files": dict(sorted(jdbc_hotspots.items())),
    }


# ---------------------------------------------------------------------------
# Dependency Matrix
# ---------------------------------------------------------------------------
def build_dependency_matrix(file_records: list) -> dict:
    java_pkg_edges = defaultdict(int)
    for rec in file_records:
        if rec["lang"] != "java":
            continue
        src_pkg = rec["package_or_namespace"].split(".")[0] if rec["package_or_namespace"] else "root"
        for imp in rec["imports"]:
            if imp.startswith(("java.", "javax.", "io.netty.", "com.", "org.", "lombok.", "redis.")):
                ext = ".".join(imp.split(".")[:2])
                java_pkg_edges[f"{src_pkg} -> [ext]{ext}"] += 1
            else:
                dst_pkg = imp.split(".")[0]
                if dst_pkg != src_pkg:
                    java_pkg_edges[f"{src_pkg} -> {dst_pkg}"] += 1

    sorted_edges = sorted(java_pkg_edges.items(), key=lambda x: -x[1])
    return {
        "java_package_edges_top50": [{"edge": k, "import_count": v} for k, v in sorted_edges[:50]],
    }


# ---------------------------------------------------------------------------
# Main Runner & Markdown Writers
# ---------------------------------------------------------------------------
def collect_all_code_files() -> list:
    records = []
    targets = [
        (PROJECT_ROOT / "src", "java"),
        (PROJECT_ROOT / "Client" / "Client" / "Assets" / "Scripts", "cs"),
        (PROJECT_ROOT / "test", "java"),
        (PROJECT_ROOT / "build", "java"),
        (PROJECT_ROOT / "tools", "java"),
    ]
    seen = set()
    for base, lang in targets:
        if not base.exists():
            continue
        ext = f".{lang}"
        for p in sorted(base.rglob(f"*{ext}")):
            if any(part in EXCLUDE_DIR_NAMES for part in p.parts):
                continue
            rel = str(p.relative_to(PROJECT_ROOT)).replace("\\", "/")
            if rel in seen:
                continue
            seen.add(rel)
            txt = p.read_text("utf-8", errors="ignore")
            meta = analyze_code_structure(txt, lang)
            domain = classify_java_domain(rel) if lang == "java" else classify_cs_domain(rel)
            records.append({
                "path": rel,
                "lang": lang,
                "domain": domain,
                "lines": meta["total_lines"],
                "bytes": p.stat().st_size,
                "package_or_namespace": meta["package_or_namespace"],
                "imports": meta["imports"],
                "types": meta["types"],
                "methods": meta["methods"],
                "method_count": len(meta["methods"]),
                "const_count": meta["const_count"],
            })
    return records


def write_outputs(records, batches, proto, db_map, deps):
    OUT_DIR.mkdir(parents=True, exist_ok=True)

    # 1. JSON outputs
    (OUT_DIR / "01_code_inventory_and_slices.json").write_text(
        json.dumps({"files": records, "worker_batches": batches}, ensure_ascii=False, indent=2),
        encoding="utf-8",
    )
    (OUT_DIR / "02_protocol_matrix.json").write_text(
        json.dumps(proto, ensure_ascii=False, indent=2),
        encoding="utf-8",
    )
    (OUT_DIR / "03_database_and_redis_map.json").write_text(
        json.dumps(db_map, ensure_ascii=False, indent=2),
        encoding="utf-8",
    )
    (OUT_DIR / "04_dependency_matrix.json").write_text(
        json.dumps(deps, ensure_ascii=False, indent=2),
        encoding="utf-8",
    )

    # 2. Markdown human/agent-readable summaries
    java_src = [r for r in records if r["path"].startswith("src/")]
    cs_cli = [r for r in records if r["lang"] == "cs"]
    other_java = [r for r in records if r["lang"] == "java" and not r["path"].startswith("src/")]

    # 100% coverage audit
    covered_ranges = defaultdict(list)
    for b in batches:
        for c in b["chunks"]:
            covered_ranges[c["path"]].append((c["start_line"], c["end_line"]))

    uncovered = []
    for r in records:
        spans = sorted(covered_ranges.get(r["path"], []))
        if not spans or spans[0][0] != 1 or spans[-1][1] != r["lines"]:
            uncovered.append(r["path"])
        else:
            for i in range(1, len(spans)):
                if spans[i][0] != spans[i - 1][1] + 1:
                    uncovered.append(f"{r['path']} (gap {spans[i-1][1]}..{spans[i][0]})")

    god_files = sorted([r for r in records if r["lines"] > GOD_FILE_THRESHOLD], key=lambda x: -x["lines"])

    summary_lines = [
        "# NRO Project Static Index Summary (Stage 1)",
        f"- Generated: `{datetime.now().strftime('%Y-%m-%d %H:%M:%S')}`",
        f"- **Server Java (`src/`)**: `{len(java_src)}` files | `{sum(r['lines'] for r in java_src):,}` lines",
        f"- **Other Java (`test/`, `build/`, `tools/`)**: `{len(other_java)}` files | `{sum(r['lines'] for r in other_java):,}` lines",
        f"- **Client Unity C#**: `{len(cs_cli)}` files | `{sum(r['lines'] for r in cs_cli):,}` lines",
        f"- **Total Code Indexed**: `{len(records)}` files | `{sum(r['lines'] for r in records):,}` lines",
        f"- **Worker Reading Batches (<= {MAX_SLICE_LINES} lines/batch)**: `{len(batches)}` batches",
        f"- **100% Coverage Verification**: `{'PASS (0 uncovered)' if not uncovered else f'FAIL ({len(uncovered)} issues)'}`",
        "",
        "## God Files Auto-Sliced (> 2,800 lines)",
        "| File | Lines | Methods | Consts | Domain |",
        "| :--- | ---: | ---: | ---: | :--- |",
    ]
    for g in god_files:
        summary_lines.append(f"| `{g['path']}` | {g['lines']:,} | {g['method_count']} | {g['const_count']} | `{g['domain']}` |")

    summary_lines += [
        "",
        "## Worker Batches Plan (Stage 2 Fan-out)",
        "| Batch ID | Domain | Lines | Chunks | Files / Ranges |",
        "| :--- | :--- | ---: | ---: | :--- |",
    ]
    for b in batches:
        sample = ", ".join(
            f"{Path(c['path']).name}:{c['start_line']}-{c['end_line']}" if c["is_partial"] else Path(c["path"]).name
            for c in b["chunks"][:5]
        )
        if b["chunk_count"] > 5:
            sample += f" ... (+{b['chunk_count'] - 5} more)"
        summary_lines.append(f"| `{b['batch_id']}` | `{b['domain']}` | {b['total_lines']:,} | {b['chunk_count']} | {sample} |")

    (OUT_DIR / "00_INDEX_SUMMARY.md").write_text("\n".join(summary_lines), encoding="utf-8")

    # Protocol markdown
    p_lines = [
        "# NRO Protocol Matrix (Server <-> Client)",
        f"- Total opcodes observed: `{proto['total_opcodes_observed']}`",
        f"- Constants in `NetworkOpcodes.cs`: `{proto['network_opcodes_constants']}`",
        f"- Potential anomalies (mismatch / duplicate handlers): `{proto['anomalies_count']}`",
        "",
        "## Full Opcode Table",
        "| Opcode | Const Name(s) | Server Handler(s) | Srv Send | Client Handler(s) | Cli Send | Flags |",
        "| ---: | :--- | :--- | ---: | :--- | ---: | :--- |",
    ]
    for row in proto["matrix"]:
        names = ", ".join(row["const_names"]) or "-"
        sh = "<br>".join(row["server_handlers"]) or "-"
        ch = "<br>".join(row["client_handlers"]) or "-"
        fl = ", ".join(row["flags"]) or "OK"
        p_lines.append(
            f"| `{row['opcode']}` | `{names}` | {sh} | {row['server_senders_count']} | {ch} | {row['client_senders_count']} | `{fl}` |"
        )
    (OUT_DIR / "02_protocol_matrix.md").write_text("\n".join(p_lines), encoding="utf-8")

    # Database & Redis markdown
    d_lines = [
        "# NRO Database Schema & Redis Map",
        f"- SQL files parsed: `{', '.join(db_map['sql_files'])}`",
        f"- Tables defined: `{db_map['table_count']}`",
        "",
        "## Tables & Java References",
        "| Table | Engine | Cols | Referenced By Java Files |",
        "| :--- | :--- | ---: | :--- |",
    ]
    for tname, tinfo in sorted(db_map["tables"].items()):
        refs = ", ".join(f"`{Path(x).name}`" for x in tinfo["referenced_by_java"][:6])
        if len(tinfo["referenced_by_java"]) > 6:
            refs += f" (+{len(tinfo['referenced_by_java']) - 6})"
        d_lines.append(f"| `{tname}` | {tinfo['engine']} | {tinfo['column_count']} | {refs or '-'} |")

    d_lines += [
        "",
        "## JDBC & Redis Hotspots in `src/`",
        "| Java File | `getConnection` | `try(...)` | `executeQuery` | `executeUpdate` | Redis Refs |",
        "| :--- | ---: | ---: | ---: | ---: | ---: |",
    ]
    for jf, counts in db_map["jdbc_and_redis_files"].items():
        d_lines.append(
            f"| `{jf}` | {counts['getConnection']} | {counts['try_with_resources']} | {counts['executeQuery']} | {counts['executeUpdate']} | {counts['redis_refs']} |"
        )
    (OUT_DIR / "03_database_and_redis_map.md").write_text("\n".join(d_lines), encoding="utf-8")

    return len(uncovered), god_files


def main():
    print("=" * 64)
    print("NRO STATIC INDEXER (STAGE 1) - RUNNING")
    print("=" * 64)
    records = collect_all_code_files()
    batches = build_worker_batches(records)
    proto = extract_protocol_matrix()
    db_map = extract_database_and_redis()
    deps = build_dependency_matrix(records)
    uncovered_count, god_files = write_outputs(records, batches, proto, db_map, deps)

    java_src_count = sum(1 for r in records if r["path"].startswith("src/"))
    cs_count = sum(1 for r in records if r["lang"] == "cs")
    print(f"[1/4] Code Files Indexed : {len(records)} total (src Java: {java_src_count}, Client C#: {cs_count})")
    print(f"[2/4] God Files Sliced   : {len(god_files)} files > {GOD_FILE_THRESHOLD} lines")
    print(f"[3/4] Worker Batches Plan: {len(batches)} clean batches (<= {MAX_SLICE_LINES} lines/batch), Uncovered={uncovered_count}")
    print(f"[4/4] Protocol & DB Map  : {proto['total_opcodes_observed']} opcodes ({proto['anomalies_count']} flagged), {db_map['table_count']} SQL tables")
    print(f"Outputs written to: {OUT_DIR}")


if __name__ == "__main__":
    main()
