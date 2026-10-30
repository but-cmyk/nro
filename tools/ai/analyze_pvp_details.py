# -*- coding: utf-8 -*-
import json
import sys
from collections import Counter

sys.stdout.reconfigure(encoding='utf-8')
filepath = 'data/telemetry/2026-10-30/trajectories.jsonl'

pvp_records = []

with open(filepath, 'r', encoding='utf-8') as f:
    for line in f:
        line = line.strip()
        if not line:
            continue
        try:
            rec = json.loads(line)
        except Exception:
            continue
        act = rec.get('action', {})
        if act.get('action_name') == 'ATTACK_PLAYER':
            pvp_records.append(rec)

print(f"Tổng số bản ghi ATTACK_PLAYER: {len(pvp_records)}")
if pvp_records:
    sample = pvp_records[0]
    print("\nMẫu ATTACK_PLAYER đầu tiên:")
    print(json.dumps(sample, indent=2, ensure_ascii=False))

    maps_pvp = Counter([r['state']['map_id'] for r in pvp_records])
    print(f"\nPhân bố map của ATTACK_PLAYER: {maps_pvp}")

    skills_pvp = Counter([r['action'].get('skill_id') for r in pvp_records])
    print(f"\nKỹ năng ATTACK_PLAYER: {skills_pvp}")

    dists = [r['action'].get('delta_x', 0)**2 + r['action'].get('delta_y', 0)**2 for r in pvp_records]
    dists = [d**0.5 for d in dists]
    dists.sort()
    print(f"Khoảng cách PvP: Min={dists[0]:.1f}, Median={dists[len(dists)//2]:.1f}, Max={dists[-1]:.1f}")
