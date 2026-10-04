#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Shared helpers for NRO memory tools:
  - Vietnamese-aware text normalization (strip diacritics, đ -> d)
  - Tokenization with bigram joining ("dap do" -> "dap_do")
  - Bullet normalization for duplicate detection (timestamp-insensitive)
  - Module resolution from a file path (single source of truth)
"""

import re
import sys
import difflib
import unicodedata
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parent.parent.parent
MEMORY_DIR = Path(__file__).resolve().parent


def force_utf8_stdout():
    if sys.stdout.encoding and sys.stdout.encoding.lower() != "utf-8":
        try:
            sys.stdout.reconfigure(encoding="utf-8")
        except Exception:
            pass


# ── Module map: longest prefix wins ──────────────────────────────────
MODULE_MAP = {
    "src/services/": "src/services/GEMINI.md",
    "src/models/combine/": "src/services/GEMINI.md",
    "src/models/item/": "src/services/GEMINI.md",
    "src/models/shop/": "src/services/GEMINI.md",
    "src/models/intrinsic/": "src/services/GEMINI.md",
    "src/models/npc/": "src/services/GEMINI.md",
    "src/models/clan/": "src/services/GEMINI.md",
    "src/models/phoban/": "src/services/GEMINI.md",
    "src/models/radar/": "src/services/GEMINI.md",
    "src/event/": "src/services/GEMINI.md",
    "src/models/player/": "src/models/player/GEMINI.md",
    "src/models/skill/": "src/models/player/GEMINI.md",
    "src/models/map/": "src/models/map/GEMINI.md",
    "src/models/mob/": "src/models/map/GEMINI.md",
    "src/models/boss/": "src/models/boss/GEMINI.md",
    "src/models/tournament/": "src/models/tournament/GEMINI.md",
    "src/models/matches/": "src/models/tournament/GEMINI.md",
    "src/models/task/": "src/models/task/GEMINI.md",
    "src/network/": "src/network/GEMINI.md",
    "src/server/": "src/network/GEMINI.md",
    "src/database/": "src/database/GEMINI.md",
    "Client/": "Client/Client/Assets/Scripts/GEMINI.md",
}

ALL_KNOWN_MODULES = sorted(set(MODULE_MAP.values()))


def module_for_path(rel_path):
    """Return GEMINI.md module for a repo-relative path (longest prefix match) or None."""
    p = rel_path.replace("\\", "/").lstrip("./")
    best = None
    for prefix, mod in MODULE_MAP.items():
        if p.startswith(prefix) and (best is None or len(prefix) > len(best[0])):
            best = (prefix, mod)
    return best[1] if best else None


# ── Text normalization ───────────────────────────────────────────────
def strip_accents(text):
    text = text.replace("đ", "d").replace("Đ", "D")
    nfkd = unicodedata.normalize("NFD", text)
    return "".join(c for c in nfkd if unicodedata.category(c) != "Mn")


def tokenize(text, min_len=2):
    """Lowercase, strip diacritics, split to words, add bigrams joined by '_'."""
    base = strip_accents(text.lower())
    words = [w for w in re.findall(r"[a-z0-9]+", base) if len(w) >= min_len or w.isdigit()]
    tokens = set(words)
    for a, b in zip(words, words[1:]):
        tokens.add(f"{a}_{b}")
    # Also split camelCase / snake_case identifiers found in original text
    for ident in re.findall(r"[A-Za-z][A-Za-z0-9_]+", text):
        parts = re.findall(r"[A-Z]?[a-z0-9]+|[A-Z]+(?![a-z])", ident)
        for part in parts:
            if len(part) >= 3:
                tokens.add(part.lower())
        tokens.add(ident.lower())
    return tokens


TIMESTAMP_RE = re.compile(r"`?\[\d{4}-\d{2}-\d{2}\]`?")


def normalize_bullet(text):
    """Timestamp-insensitive, accent-insensitive canonical form for dedup."""
    t = TIMESTAMP_RE.sub("", text)
    t = strip_accents(t.lower())
    t = re.sub(r"^\s*-\s*", "", t)
    return re.sub(r"[^a-z0-9]+", " ", t).strip()


def similarity(a, b):
    return difflib.SequenceMatcher(None, normalize_bullet(a), normalize_bullet(b)).ratio()


def find_similar_line(lines, bullet, threshold=0.85):
    """Return the first existing bullet line that duplicates `bullet`, else None."""
    nb = normalize_bullet(bullet)
    if not nb:
        return None
    for line in lines:
        if not line.strip().startswith("- "):
            continue
        nl = normalize_bullet(line)
        if not nl:
            continue
        if nb == nl or nb in nl or similarity(line, bullet) >= threshold:
            return line
    return None
