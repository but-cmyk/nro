import json
import os
import sys
import numpy as np

sys.stdout.reconfigure(encoding='utf-8')

def main():
    print("==================================================")
    print("  NAMEC BEHAVIORAL CLONING TRAINING PIPELINE")
    print("==================================================")

    telemetry_path = os.path.join('data', 'telemetry', '2026-10-30', 'trajectories.jsonl')
    if not os.path.exists(telemetry_path):
        print(f"[ERROR] Telemetry file not found: {telemetry_path}")
        sys.exit(1)

    print(f"Reading dataset: {telemetry_path}...")
    records = []
    with open(telemetry_path, 'r', encoding='utf-8') as f:
        for line in f:
            line = line.strip()
            if line:
                records.append(json.loads(line))

    print(f"Loaded {len(records)} raw records.")

    # 1. Filter for Namec player records
    namec_records = [
        r for r in records
        if r.get('state', {}).get('gender') == 1 and r.get('player_name') == 'paradjse'
    ]
    print(f"Filtered Namec records: {len(namec_records)} ({len(namec_records)/len(records)*100:.1f}%)")

    # 2. Extract Movement Parameters
    strides = []
    for r in namec_records:
        if r.get('action', {}).get('action_name') == 'MOVE':
            dx = abs(r.get('action', {}).get('delta_x', 0))
            if 10 <= dx <= 250:
                strides.append(dx)

    mean_stride = float(np.mean(strides)) if strides else 72.0
    median_stride = float(np.median(strides)) if strides else 70.0
    p25_stride = float(np.percentile(strides, 25)) if strides else 50.0
    p75_stride = float(np.percentile(strides, 75)) if strides else 80.0

    print(f"\n[Movement Policy Extracted]")
    print(f"  - Typical Stride: {median_stride:.1f} px (P25: {p25_stride:.1f}, P75: {p75_stride:.1f}, Mean: {mean_stride:.1f})")

    # 3. Extract Combat Policy Parameters
    skill_counts = {2: 0, 3: 0, 7: 0} # 2: Demon, 3: Masenko, 7: Tri Thuong
    attack_distances = []
    combo_lengths = []
    current_combo = 0

    for r in namec_records:
        act_name = r.get('action', {}).get('action_name')
        if act_name == 'ATTACK_MOB':
            s_id = r.get('action', {}).get('skill_id', 2)
            skill_counts[s_id] = skill_counts.get(s_id, 0) + 1
            current_combo += 1

            targets = r.get('state', {}).get('nearby_targets', [])
            if targets:
                attack_distances.append(targets[0].get('distance', 15.0))
        else:
            if current_combo > 0:
                combo_lengths.append(current_combo)
                current_combo = 0

    total_attacks = sum(skill_counts.values()) or 1
    demon_ratio = float(skill_counts.get(2, 0) / total_attacks)
    masenko_ratio = float(skill_counts.get(3, 0) / total_attacks)
    mean_combo = float(np.mean(combo_lengths)) if combo_lengths else 3.5
    median_attack_dist = float(np.median(attack_distances)) if attack_distances else 10.0

    print(f"\n[Combat Policy Extracted]")
    print(f"  - Skill Demon (ID 2) Ratio: {demon_ratio*100:.1f}%")
    print(f"  - Skill Masenko (ID 3) Ratio: {masenko_ratio*100:.1f}%")
    print(f"  - Trigger Distance to Attack: <= {median_attack_dist:.1f} px")
    print(f"  - Average Combo Length: {mean_combo:.1f} consecutive attacks")

    # 4. Extract Map Distribution (Heatmap for 100 bots)
    map_counts = {}
    for r in namec_records:
        m = r.get('state', {}).get('map_id')
        if m is not None:
            map_counts[m] = map_counts.get(m, 0) + 1

    total_map_ticks = sum(map_counts.values()) or 1
    # Allocate 100 bots proportionally
    bot_allocation = {}
    allocated_sum = 0
    # Top Namec maps
    key_maps = [30, 13, 29, 27, 11, 8, 32, 36, 9, 7]
    for m in key_maps:
        fraction = map_counts.get(m, 0) / total_map_ticks
        count = max(2, int(round(fraction * 100)))
        bot_allocation[m] = count
        allocated_sum += count

    # Adjust to exactly 100
    diff = 100 - allocated_sum
    bot_allocation[30] += diff

    print(f"\n[100 Bot Map Allocation]")
    for m, cnt in bot_allocation.items():
        print(f"  - Map {m}: {cnt} bots")

    # 5. Extract Timing Jitter (Inter-action timing)
    timestamps = [r['timestamp'] for r in namec_records]
    intervals = np.diff(timestamps)
    valid_intervals = intervals[(intervals > 50) & (intervals < 1500)]
    mean_latency = float(np.mean(valid_intervals)) if len(valid_intervals) else 330.0
    std_latency = float(np.std(valid_intervals)) if len(valid_intervals) else 80.0

    print(f"\n[Human Jitter Timing]")
    print(f"  - Mean Cadence: {mean_latency:.1f} ms")
    print(f"  - Std Deviation: {std_latency:.1f} ms")

    # 6. Build Policy Model Configuration
    policy_model = {
        "model_version": "1.0.0-namec-imitation",
        "trained_samples": len(namec_records),
        "source_player": "paradjse",
        "movement": {
            "mean_stride_px": round(mean_stride, 1),
            "median_stride_px": round(median_stride, 1),
            "min_stride_px": 45,
            "max_stride_px": 90,
            "approach_offset_px": 30
        },
        "combat": {
            "attack_trigger_range_px": 35,
            "demon_skill_id": 2,
            "demon_probability": round(demon_ratio, 3),
            "masenko_skill_id": 3,
            "masenko_probability": round(masenko_ratio, 3),
            "heal_skill_id": 7,
            "heal_hp_threshold": 0.35,
            "average_combo_hits": round(mean_combo, 1)
        },
        "timing": {
            "mean_delay_ms": int(mean_latency),
            "std_delay_ms": int(std_latency),
            "min_delay_ms": 150,
            "max_delay_ms": 450
        },
        "fleet_distribution_100": {
            str(m): cnt for m, cnt in bot_allocation.items()
        }
    }

    os.makedirs(os.path.join('data', 'config'), exist_ok=True)
    out_path = os.path.join('data', 'config', 'namec_policy.json')
    with open(out_path, 'w', encoding='utf-8') as f:
        json.dump(policy_model, f, indent=2, ensure_ascii=False)

    print(f"\n[SUCCESS] Namec Behavioral Policy exported to: {out_path}")

if __name__ == '__main__':
    main()

