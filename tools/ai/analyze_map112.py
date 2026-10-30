# -*- coding: utf-8 -*-
import json
import sys
from collections import Counter

sys.stdout.reconfigure(encoding='utf-8')
filepath = 'data/telemetry/2026-10-30/trajectories.jsonl'

map_112_records = []

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
            map_112_records.append(rec)

print(f"Tổng số bản ghi tại Map 112 (Võ Đài Sinh Tử): {len(map_112_records)}")

actions_112 = Counter([r['action'].get('action_name') for r in map_112_records])
skills_112 = Counter([r['action'].get('skill_id') for r in map_112_records])
target_types_112 = Counter([r['action'].get('target_type') for r in map_112_records])
xs_112 = [r['state'].get('pos_x', 0) for r in map_112_records]
ys_112 = [r['state'].get('pos_y', 0) for r in map_112_records]

print(f"Hành động tại Map 112: {actions_112}")
print(f"Kỹ năng tại Map 112: {skills_112}")
print(f"Mục tiêu tại Map 112: {target_types_112}")
print(f"Tọa độ X tại Map 112: Min={min(xs_112)}, Median={sorted(xs_112)[len(xs_112)//2]}, Max={max(xs_112)}")
print(f"Tọa độ Y tại Map 112: Min={min(ys_112)}, Median={sorted(ys_112)[len(ys_112)//2]}, Max={max(ys_112)}")
