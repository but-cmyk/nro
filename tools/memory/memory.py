#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
NRO Episodic Memory & Reinforcement CLI.

Lessons live in tools/memory/LESSONS.jsonl (append-friendly, one JSON per line).
GEMINI.md modules only hold *distilled* rules promoted from proven lessons.

Commands:
  capture  --symptom "..." --cause "..." --rule "..." [--files a,b] [--tags x,y] [--module M]
  recall   "<query>" [--files a,b] [-k 5] [--no-touch]
  feedback <id> +1|-1 [--note "..."]
  promote  [<id>] [--force]          (no id = list promotion candidates)
  list     [--module M] [--status active|promoted|archived]
  digest                              (session summary: changed files, modules, related lessons)
  hook-pre                            (PreInvocation hook: stdin JSON -> inject relevant lessons)
  hook-stop                           (Stop hook: remind once to capture if code changed)
"""

import sys
import os
import json
import time
import hashlib
import argparse
import subprocess
from datetime import datetime
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from memlib import (PROJECT_ROOT, MEMORY_DIR, module_for_path, tokenize,
                    similarity, force_utf8_stdout)

LESSONS_FILE = MEMORY_DIR / "LESSONS.jsonl"
HOOK_STATE_FILE = MEMORY_DIR / ".hook_state.json"

PROMOTE_MIN_SCORE = 2          # net positive feedback needed to auto-qualify
ARCHIVE_SCORE = -2             # lessons at/below this are archived
CODE_PREFIXES = ("src/", "Client/Client/Assets/Scripts/")


# ── Storage ──────────────────────────────────────────────────────────
def load_lessons():
    lessons = []
    if LESSONS_FILE.exists():
        for line in LESSONS_FILE.read_text(encoding="utf-8").splitlines():
            line = line.strip()
            if line:
                try:
                    lessons.append(json.loads(line))
                except json.JSONDecodeError:
                    pass
    return lessons


def save_lessons(lessons):
    from guardrails import MemoryLock
    with MemoryLock():
        tmp = LESSONS_FILE.with_suffix(".tmp")
        tmp.write_text("".join(json.dumps(l, ensure_ascii=False) + "\n" for l in lessons), encoding="utf-8")
        os.replace(tmp, LESSONS_FILE)


def next_id(lessons):
    n = max((int(l["id"][1:]) for l in lessons if l.get("id", "L0")[1:].isdigit()), default=0)
    return f"L{n + 1:04d}"


def today():
    return datetime.now().strftime("%Y-%m-%d")


def split_csv(v):
    return [x.strip().replace("\\", "/") for x in (v or "").split(",") if x.strip()]


def lesson_text(l):
    return " ".join([l.get("symptom", ""), l.get("root_cause", ""), l.get("rule", ""),
                     " ".join(l.get("tags", [])), " ".join(l.get("files", []))])


# ── Git helpers ──────────────────────────────────────────────────────
def git(*args):
    try:
        r = subprocess.run(["git", *args], cwd=str(PROJECT_ROOT), capture_output=True,
                           text=True, encoding="utf-8", errors="ignore", timeout=10)
        return r.stdout
    except Exception:
        return ""


def changed_files():
    """Modified + untracked files (repo-relative, forward slashes)."""
    out = set()
    for line in git("status", "--porcelain").splitlines():
        if len(line) > 3:
            path = line[3:].strip().strip('"')
            if " -> " in path:
                path = path.split(" -> ", 1)[1]
            out.add(path.replace("\\", "/"))
    return sorted(out)


# ── Recall (retrieval) ───────────────────────────────────────────────
def rank_lessons(query="", files=None, k=5, include_promoted=True):
    lessons = load_lessons()
    files = [f.replace("\\", "/") for f in (files or [])]
    q_tokens = tokenize(query) if query else set()
    file_mods = {module_for_path(f) for f in files} - {None}
    file_names = {Path(f).stem.lower() for f in files}

    scored = []
    for l in lessons:
        if l.get("status") == "archived":
            continue
        if not include_promoted and l.get("status") == "promoted":
            continue
        l_tokens = tokenize(lesson_text(l))
        s = 0.0
        overlap = q_tokens & l_tokens
        s += sum(2.0 if "_" in t else 1.0 for t in overlap)
        if l.get("module") in file_mods:
            s += 2.0
        l_file_names = {Path(f).stem.lower() for f in l.get("files", [])}
        s += 5.0 * len(file_names & l_file_names)
        if s <= 0:
            continue
        s *= 1.0 + 0.25 * max(-3, min(l.get("score", 0), 8))   # reinforcement weighting
        scored.append((s, l))
    scored.sort(key=lambda x: x[0], reverse=True)
    return scored[:k]


def format_lesson(l, short=False):
    head = f"[{l['id']}] ({l.get('module', '?')}, score {l.get('score', 0):+d}, {l.get('status', 'active')})"
    if short:
        return f"{head} {l.get('rule', '')}"
    lines = [head,
             f"   Triệu chứng : {l.get('symptom', '')}",
             f"   Nguyên nhân : {l.get('root_cause', '')}",
             f"   Quy tắc     : {l.get('rule', '')}"]
    if l.get("files"):
        lines.append(f"   Files       : {', '.join(l['files'][:4])}")
    return "\n".join(lines)


# ── Commands ─────────────────────────────────────────────────────────
def cmd_capture(a):
    if not a.symptom or not a.rule:
        print("[ERROR] Bắt buộc có --symptom và --rule (nên có --cause).")
        return 1
    files = split_csv(a.files)
    module = a.module or next((m for m in (module_for_path(f) for f in files) if m), None)
    if not module:
        print("[ERROR] Không xác định được module. Truyền --module hoặc --files thuộc src/ / Client/.")
        return 1

    lessons = load_lessons()
    for l in lessons:
        if l.get("status") != "archived" and similarity(l.get("rule", ""), a.rule) >= 0.85:
            l["files"] = sorted(set(l.get("files", [])) | set(files))
            l["tags"] = sorted(set(l.get("tags", [])) | set(split_csv(a.tags)))
            l["score"] = l.get("score", 0) + 1         # re-discovered = reinforcement signal
            l["last_used"] = today()
            save_lessons(lessons)
            print(f"[MERGED] Bài học tương tự đã có → {l['id']} (score {l['score']:+d})")
            print(format_lesson(l))
            return 0

    lesson = {
        "id": next_id(lessons), "date": today(), "module": module,
        "tags": split_csv(a.tags), "files": files,
        "symptom": a.symptom.strip(), "root_cause": (a.cause or "").strip(), "rule": a.rule.strip(),
        "hits": 0, "score": 0, "status": "active", "last_used": today(),
    }
    lessons.append(lesson)
    save_lessons(lessons)
    print("[CAPTURED]")
    print(format_lesson(lesson))
    return 0


def cmd_recall(a):
    files = split_csv(a.files)
    results = rank_lessons(a.query or "", files, a.k)
    if not results:
        print(f"[RECALL] Không có bài học liên quan tới: '{a.query}'")
        return 0
    print(f"[RECALL] Top {len(results)} bài học cho: '{a.query}'")
    if not a.no_touch:
        lessons = load_lessons()
        ids = {l["id"] for _, l in results}
        for l in lessons:
            if l["id"] in ids:
                l["hits"] = l.get("hits", 0) + 1
                l["last_used"] = today()
        save_lessons(lessons)
    for s, l in results:
        print(format_lesson(l))
    print("\n→ Sau khi dùng: memory.py feedback <id> +1 (đúng/hữu ích) hoặc -1 (sai/lỗi thời)")
    return 0


def cmd_feedback(a):
    lessons = load_lessons()
    delta = 1 if a.delta.startswith("+") or a.delta == "1" else -1
    for l in lessons:
        if l["id"] == a.id:
            l["score"] = l.get("score", 0) + delta
            l["last_used"] = today()
            if a.note:
                l.setdefault("notes", []).append(f"{today()} {a.delta}: {a.note}")
            if l["score"] <= ARCHIVE_SCORE:
                l["status"] = "archived"
                print(f"[ARCHIVED] {l['id']} score {l['score']} ≤ {ARCHIVE_SCORE}")
            save_lessons(lessons)
            print(f"[OK] {l['id']} score → {l['score']:+d}")
            if l["status"] == "active" and l["score"] >= PROMOTE_MIN_SCORE:
                print(f"[HINT] Đủ điều kiện promote: memory.py promote {l['id']}")
            return 0
    print(f"[ERROR] Không tìm thấy {a.id}")
    return 1


def cmd_promote(a):
    lessons = load_lessons()
    if not a.id:
        cands = [l for l in lessons if l.get("status") == "active" and l.get("score", 0) >= PROMOTE_MIN_SCORE]
        if not cands:
            print(f"[PROMOTE] Chưa có bài học nào đạt score ≥ {PROMOTE_MIN_SCORE}.")
        for l in cands:
            print(format_lesson(l, short=True))
        return 0
    from dreaming_engine import apply_patch_to_module
    for l in lessons:
        if l["id"] == a.id:
            if l.get("score", 0) < PROMOTE_MIN_SCORE and not a.force:
                print(f"[DENY] {l['id']} score {l.get('score', 0)} < {PROMOTE_MIN_SCORE}. Dùng --force nếu User đã xác nhận.")
                return 1
            ok = apply_patch_to_module(l["module"], f"- {l['rule']} (ref {l['id']})",
                                       "## 3. Các Bất Biến Bắt Buộc", dry_run=False)
            if ok:
                l["status"] = "promoted"
                l["promoted_at"] = today()
                save_lessons(lessons)
                print(f"[PROMOTED] {l['id']} → {l['module']}")
                return 0
            return 1
    print(f"[ERROR] Không tìm thấy {a.id}")
    return 1


def cmd_list(a):
    lessons = load_lessons()
    for l in lessons:
        if a.module and l.get("module") != a.module:
            continue
        if a.status and l.get("status") != a.status:
            continue
        print(format_lesson(l, short=True))
    print(f"\n[TOTAL] {len(lessons)} bài học "
          f"(active {sum(l.get('status') == 'active' for l in lessons)}, "
          f"promoted {sum(l.get('status') == 'promoted' for l in lessons)}, "
          f"archived {sum(l.get('status') == 'archived' for l in lessons)})")
    return 0


def cmd_digest(_a=None):
    files = [f for f in changed_files() if f.startswith(CODE_PREFIXES)]
    print("=" * 60)
    print("SESSION DIGEST — đúc kết bài học")
    print("=" * 60)
    if not files:
        print("[INFO] Không có file code (src/, Client/) thay đổi chưa commit.")
        return 0
    by_mod = {}
    for f in files:
        by_mod.setdefault(module_for_path(f) or "(chưa map module)", []).append(f)
    for mod, fs in by_mod.items():
        print(f"\n* {mod}")
        for f in fs[:8]:
            print(f"    - {f}")
    related = rank_lessons(" ".join(Path(f).stem for f in files), files, k=5)
    if related:
        print("\n[BÀI HỌC LIÊN QUAN ĐÃ CÓ] — feedback +1/-1 nếu đã áp dụng:")
        for _, l in related:
            print("  " + format_lesson(l, short=True))
    print("\n[CÂU HỎI TỰ VẤN] Phiên này có:")
    print("  1. Bug nào mất >1 lần thử mới fix? Nguyên nhân gốc là gì?")
    print("  2. Giả định sai nào về code/asset/protocol đã bị phát hiện?")
    print("  3. User đã sửa/nhắc lại điều gì?")
    print("\nNếu có → ghi lại:")
    print('  python tools/memory/memory.py capture --files "<file>" --tags "<tag>" '
          '--symptom "<triệu chứng>" --cause "<nguyên nhân>" --rule "<quy tắc ngắn>"')
    print("Nếu không có bài học mới đáng giá → không cần ghi (tránh ô nhiễm bộ nhớ).")
    return 0


# ── Hooks (must never crash; always print valid JSON) ────────────────
def _read_stdin_json():
    try:
        raw = sys.stdin.read()
        return json.loads(raw) if raw.strip() else {}
    except Exception:
        return {}


def _load_state():
    try:
        return json.loads(HOOK_STATE_FILE.read_text(encoding="utf-8"))
    except Exception:
        return {}


def _save_state(state):
    try:
        # keep only the 20 most recent conversations
        items = sorted(state.items(), key=lambda kv: kv[1].get("ts", 0), reverse=True)[:20]
        HOOK_STATE_FILE.write_text(json.dumps(dict(items), ensure_ascii=False), encoding="utf-8")
    except Exception:
        pass


def _last_user_message(transcript_path):
    """Best-effort: last USER_INPUT content from the transcript JSONL."""
    try:
        p = Path(transcript_path)
        if not p.exists():
            return ""
        with open(p, "rb") as f:
            f.seek(0, 2)
            size = f.tell()
            f.seek(max(0, size - 400_000))
            tail = f.read().decode("utf-8", errors="ignore").splitlines()
        for line in reversed(tail):
            if "USER_INPUT" not in line:
                continue
            try:
                obj = json.loads(line)
            except Exception:
                continue
            content = obj.get("content") or ""
            if isinstance(content, (dict, list)):
                content = json.dumps(content, ensure_ascii=False)
            s, e = content.find("<USER_REQUEST>"), content.find("</USER_REQUEST>")
            if s != -1 and e != -1:
                content = content[s + len("<USER_REQUEST>"):e]
            return content.strip()[:2000]
    except Exception:
        pass
    return ""


def hook_pre():
    payload = _read_stdin_json()
    out = {}
    try:
        conv = payload.get("conversationId", "default")
        msg = _last_user_message(payload.get("transcriptPath", ""))
        files = changed_files()
        key = hashlib.md5((msg or "|".join(files)).encode("utf-8")).hexdigest()
        state = _load_state()
        st = state.get(conv, {})
        if st.get("pre_key") != key:                 # inject once per new user message
            st.update({"pre_key": key, "ts": time.time(), "reminded": False})
            state[conv] = st
            _save_state(state)
            results = rank_lessons(msg, [f for f in files if f.startswith(CODE_PREFIXES)], k=4)
            results = [(s, l) for s, l in results if s >= 3.0]
            if results:
                body = "\n".join(format_lesson(l, short=True) for _, l in results)
                out = {"injectSteps": [{"ephemeralMessage":
                       "[NRO MEMORY RECALL] Bài học liên quan từ các phiên trước "
                       "(áp dụng nếu phù hợp; sau đó `python tools/memory/memory.py feedback <id> +1|-1`):\n"
                       + body}]}
    except Exception:
        out = {}
    print(json.dumps(out, ensure_ascii=False))
    return 0


def hook_stop():
    payload = _read_stdin_json()
    out = {"decision": "stop"}
    try:
        if payload.get("terminationReason", "model_stop") != "model_stop" or not payload.get("fullyIdle", True):
            print(json.dumps(out)); return 0
        conv = payload.get("conversationId", "default")
        state = _load_state()
        st = state.get(conv, {})
        if st.get("reminded"):
            print(json.dumps(out)); return 0
        since = st.get("ts", 0)
        touched = [f for f in changed_files() if f.startswith(CODE_PREFIXES)
                   and (PROJECT_ROOT / f).exists() and (PROJECT_ROOT / f).stat().st_mtime > since]
        captured = LESSONS_FILE.exists() and LESSONS_FILE.stat().st_mtime > since
        if touched and not captured:
            st["reminded"] = True
            state[conv] = st
            _save_state(state)
            out = {"decision": "continue",
                   "reason": (f"[NRO MEMORY] Phiên này đã sửa {len(touched)} file code "
                              f"({', '.join(touched[:3])}). Chạy `python tools/memory/memory.py digest`; "
                              "nếu có bài học đáng giá (bug khó, giả định sai, User sửa lưng) thì `capture`, "
                              "nếu không thì kết thúc bình thường. Chỉ nhắc 1 lần.")}
    except Exception:
        out = {"decision": "stop"}
    print(json.dumps(out, ensure_ascii=False))
    return 0


# ── CLI ──────────────────────────────────────────────────────────────
def main():
    force_utf8_stdout()
    p = argparse.ArgumentParser(description="NRO episodic memory")
    sub = p.add_subparsers(dest="cmd")

    c = sub.add_parser("capture")
    c.add_argument("--symptom"); c.add_argument("--cause"); c.add_argument("--rule")
    c.add_argument("--files"); c.add_argument("--tags"); c.add_argument("--module")

    r = sub.add_parser("recall")
    r.add_argument("query", nargs="?", default="")
    r.add_argument("--files"); r.add_argument("-k", type=int, default=5)
    r.add_argument("--no-touch", action="store_true")

    f = sub.add_parser("feedback")
    f.add_argument("id"); f.add_argument("delta"); f.add_argument("--note")

    pr = sub.add_parser("promote")
    pr.add_argument("id", nargs="?"); pr.add_argument("--force", action="store_true")

    ls = sub.add_parser("list")
    ls.add_argument("--module"); ls.add_argument("--status")

    sub.add_parser("digest")
    sub.add_parser("hook-pre")
    sub.add_parser("hook-stop")

    a = p.parse_args()
    handlers = {"capture": cmd_capture, "recall": cmd_recall, "feedback": cmd_feedback,
                "promote": cmd_promote, "list": cmd_list, "digest": cmd_digest}
    if a.cmd == "hook-pre":
        return hook_pre()
    if a.cmd == "hook-stop":
        return hook_stop()
    if a.cmd in handlers:
        return handlers[a.cmd](a)
    p.print_help()
    return 0


if __name__ == "__main__":
    sys.exit(main())
