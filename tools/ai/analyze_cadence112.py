# -*- coding: utf-8 -*-
import json
import sys

sys.stdout.reconfigure(encoding='utf-8')
filepath = 'data/telemetry/2026-10-30/trajectories.jsonl'

cadences_112 = []
last_ts = None

with open(filepath, 'r', encoding='utf-8') as f:
    for line in f:
        line = line.strip()
        if not line:
            continue
        try:
            rec = json.loads(line)
        except Exception:
            continue
        state = rec.get('state', {})
        if state.get('map_id') == 112:
            ts = rec.get('timestamp', 0)
            if last_ts is not None and ts > last_ts:
                diff = ts - last_ts
                if diff <= 3000:
                    cadences_112.append(diff)
            last_ts = ts

if cadences_112:
    s = sorted(cadences_112)
    print(f"Map 112 Cadence: Count={len(s)}, Mean={sum(s)/len(s):.1f}ms, Median={s[len(s)//2]}ms, P90={s[int(len(s)*0.9)]}ms")
