# -*- coding: utf-8 -*-
import json
import sys
import math
from collections import Counter, defaultdict

sys.stdout.reconfigure(encoding='utf-8')

filepath = 'data/telemetry/2026-10-30/trajectories.jsonl'

total = 0
action_types = Counter()
skills_used = Counter()
target_types = Counter()
maps_visited = Counter()
players = Counter()

attack_mob_distances = []
pvp_attack_distances = []
move_strides = []
eat_pea_hp_ratios = []
eat_pea_mp_ratios = []

timestamps = []
cadences = []
last_ts = None

damage_dealt_list = []
damage_taken_list = []
hp_at_damage_taken = []
counter_actions_after_hit = Counter()

prev_action = None
action_transitions = defaultdict(Counter)

# PvP / Boss combat specific tracking
pvp_skills = Counter()
mob_skills = Counter()
damage_reaction_hp = []

with open(filepath, 'r', encoding='utf-8') as f:
    for line in f:
        line = line.strip()
        if not line:
            continue
        try:
            rec = json.loads(line)
        except Exception:
            continue

        total += 1
        players[rec.get('player_id', 0)] += 1
        ts = rec.get('timestamp', 0)
        timestamps.append(ts)
        if last_ts is not None and ts > last_ts:
            diff = ts - last_ts
            if diff <= 5000:
                cadences.append(diff)
        last_ts = ts

        state = rec.get('state', {})
        action = rec.get('action', {})
        reward = rec.get('reward', {})

        act_name = action.get('action_name', 'UNKNOWN')
        action_types[act_name] += 1
        if prev_action:
            action_transitions[prev_action][act_name] += 1
        prev_action = act_name

        map_id = state.get('map_id', -1)
        maps_visited[map_id] += 1

        skill_id = action.get('skill_id')
        if skill_id is not None:
            skills_used[skill_id] += 1
            if act_name == 'ATTACK_PLAYER':
                pvp_skills[skill_id] += 1
            elif act_name == 'ATTACK_MOB':
                mob_skills[skill_id] += 1

        target_type = action.get('target_type')
        if target_type:
            target_types[target_type] += 1

        # Calculate distance to target from delta_x and delta_y
        dx = action.get('delta_x', 0)
        dy = action.get('delta_y', 0)
        dist = math.sqrt(dx*dx + dy*dy)

        if act_name == 'ATTACK_PLAYER':
            pvp_attack_distances.append(dist)
        elif act_name == 'ATTACK_MOB':
            attack_mob_distances.append(dist)
        elif act_name == 'MOVE':
            if dist > 0:
                move_strides.append(dist)

        if act_name == 'EAT_PEA':
            eat_pea_hp_ratios.append(state.get('hp_ratio', 1.0))
            eat_pea_mp_ratios.append(state.get('mp_ratio', 1.0))

        if act_name == 'DAMAGE_REACTION':
            damage_reaction_hp.append(state.get('hp_ratio', 1.0))

        dmg_d = reward.get('damage_dealt', 0)
        if dmg_d > 0:
            damage_dealt_list.append(dmg_d)

        dmg_t = reward.get('damage_taken', 0)
        if dmg_t > 0:
            damage_taken_list.append(dmg_t)
            hp_at_damage_taken.append(state.get('hp_ratio', 1.0))

def calc_stats(arr):
    if not arr:
        return {'count': 0, 'mean': 0, 'min': 0, 'max': 0, 'median': 0, 'p90': 0}
    s = sorted(arr)
    n = len(s)
    mean = sum(s) / n
    median = s[n // 2]
    p90 = s[int(n * 0.9)]
    return {
        'count': n,
        'mean': round(mean, 2),
        'min': s[0],
        'max': s[-1],
        'median': median,
        'p90': p90
    }

print("\n=== TỔNG QUAN TELEMETRY ===")
print(f"Tổng số bản ghi: {total:,}")
print(f"Người chơi: {dict(players)}")
if timestamps:
    duration_sec = (max(timestamps) - min(timestamps)) / 1000.0
    print(f"Tổng thời gian ghi log: {duration_sec:.1f} giây ({duration_sec/60.0:.2f} phút)")

print("\n=== PHÂN BỔ HÀNH ĐỘNG (ACTION DISTRIBUTION) ===")
for act, count in action_types.most_common():
    pct = (count / total) * 100.0
    print(f"  - {act:18s}: {count:6d} ({pct:5.2f}%)")

print("\n=== KỸ NĂNG TỔNG QUAN ===")
for sk, count in skills_used.most_common():
    print(f"  - Skill ID {sk}: {count} lần")

print("\n=== KỸ NĂNG PVP (ATTACK_PLAYER) ===")
for sk, count in pvp_skills.most_common():
    print(f"  - Skill ID {sk}: {count} lần ({count/max(1, len(pvp_attack_distances))*100:.1f}%)")

print("\n=== KỸ NĂNG PVE (ATTACK_MOB) ===")
for sk, count in mob_skills.most_common():
    print(f"  - Skill ID {sk}: {count} lần ({count/max(1, len(attack_mob_distances))*100:.1f}%)")

print("\n=== PHÂN BỔ MỤC TIÊU (TARGET TYPES) ===")
for tt, count in target_types.most_common():
    print(f"  - Target {tt}: {count} lần")

print("\n=== BẢN ĐỒ HOẠT ĐỘNG (TOP MAPS) ===")
for m, count in maps_visited.most_common(10):
    print(f"  - Map {m}: {count} ticks")

print("\n=== CHỈ SỐ KHÔNG GIAN & VẬN ĐỘNG ===")
print(f"Khoảng cách Đòn đánh Quái (ATTACK_MOB): {calc_stats(attack_mob_distances)}")
print(f"Khoảng cách Đòn đánh PvP (ATTACK_PLAYER): {calc_stats(pvp_attack_distances)}")
print(f"Bước chạy (MOVE stride): {calc_stats(move_strides)}")
print(f"Nhịp độ thao tác (Cadence ms): {calc_stats(cadences)}")

print("\n=== HÀNH VI HỒI PHỤC (PEA & HEALING) ===")
if eat_pea_hp_ratios:
    print(f"HP Ratio khi ăn đậu: {calc_stats(eat_pea_hp_ratios)}")
    print(f"MP Ratio khi ăn đậu: {calc_stats(eat_pea_mp_ratios)}")
else:
    print("Không có lần ăn đậu trực tiếp nào.")

print("\n=== SÁT THƯƠNG & CHỐNG CHỊU ===")
print(f"Sát thương gây ra (Damage Dealt): {calc_stats(damage_dealt_list)}")
print(f"Sát thương nhận vào (Damage Taken): {calc_stats(damage_taken_list)}")
print(f"HP Ratio khi chịu đòn (Damage Reaction): {calc_stats(damage_reaction_hp)}")

print("\n=== CHUỖI CHUYỂN TRẠNG THÁI HÀNH ĐỘNG (ACTION TRANSITIONS - TOP 3) ===")
for act_from, trans in action_transitions.items():
    top3 = trans.most_common(3)
    top3_str = ", ".join([f"{to}: {c}" for to, c in top3])
    print(f"  {act_from:18s} -> [{top3_str}]")
