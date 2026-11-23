import json
import os
import sys
import gzip
import numpy as np

sys.stdout.reconfigure(encoding='utf-8')

def open_telemetry(path):
    if not os.path.exists(path):
        gz_path = path + '.gz'
        if os.path.exists(gz_path):
            return gzip.open(gz_path, 'rt', encoding='utf-8'), gz_path
        return None, path
    if path.endswith('.gz'):
        return gzip.open(path, 'rt', encoding='utf-8'), path
    return open(path, 'r', encoding='utf-8'), path

def main():
    print("==================================================")
    print("  NAMEC BEHAVIORAL CLONING TRAINING PIPELINE")
    print("==================================================")

    telemetry_path = os.path.join('data', 'telemetry', '2026-10-30', 'trajectories.jsonl')
    f, actual_path = open_telemetry(telemetry_path)
    if f is None:
        print(f"[ERROR] Telemetry file not found: {telemetry_path} (nor .gz)")
        sys.exit(1)

    print(f"Reading dataset: {actual_path}...")
    records = []
    with f:
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

    # 3. Extract Combat & Skill Selection
    demon_count = 0
    masenko_count = 0
    attack_distances = []

    for r in namec_records:
        act = r.get('action', {})
        if act.get('action_name') in ('ATTACK_MOB', 'ATTACK_PLAYER'):
            sk_id = act.get('skill_id')
            if sk_id == 2:
                demon_count += 1
            elif sk_id == 3:
                masenko_count += 1

            dx = abs(act.get('delta_x', 0))
            dy = abs(act.get('delta_y', 0))
            dist = np.hypot(dx, dy)
            if dist > 0:
                attack_distances.append(dist)

    total_attacks = max(1, demon_count + masenko_count)
    demon_ratio = demon_count / total_attacks
    masenko_ratio = masenko_count / total_attacks
    median_attack_dist = float(np.median(attack_distances)) if attack_distances else 20.0

    print(f"\n[Combat Policy Extracted]")
    print(f"  - Demon (Melee Punch) Ratio:   {demon_ratio*100:.1f}% ({demon_count} attacks)")
    print(f"  - Masenko (Ki Blast) Ratio:    {masenko_ratio*100:.1f}% ({masenko_count} attacks)")
    print(f"  - Engagement Distance:         {median_attack_dist:.1f} px")

    # 4. Extract Map Hotspots
    map_counts = {}
    for r in namec_records:
        m_id = r.get('state', {}).get('map_id')
        if m_id is not None:
            map_counts[m_id] = map_counts.get(m_id, 0) + 1

    sorted_maps = sorted(map_counts.items(), key=lambda x: x[1], reverse=True)
    top_maps = [m for m, _ in sorted_maps[:10]]
    print(f"\n[Map Routing Policy Extracted]")
    print(f"  - Top 10 Namec Maps: {top_maps}")

    # 5. Extract Timing & Motor Latency Jitter
    timestamps = [r.get('timestamp') for r in namec_records if r.get('timestamp')]
    timestamps.sort()
    delays = []
    for i in range(1, len(timestamps)):
        diff = timestamps[i] - timestamps[i-1]
        if 50 <= diff <= 2000:
            delays.append(diff)

    mean_delay = float(np.mean(delays)) if delays else 350.0
    std_delay = float(np.std(delays)) if delays else 180.0
    print(f"\n[Human Motor Jitter Policy Extracted]")
    print(f"  - Mean Action Cadence: {mean_delay:.1f} ms (Std Dev: {std_delay:.1f} ms)")

    # 6. Save Model Parameters to Config
    policy_config = {
        "model_version": "1.0.0-namec-imitation",
        "movement": {
            "mean_stride_px": round(mean_stride, 1),
            "median_stride_px": round(median_stride, 1),
            "p25_stride_px": round(p25_stride, 1),
            "p75_stride_px": round(p75_stride, 1)
        },
        "combat": {
            "demon_probability": round(demon_ratio, 3),
            "masenko_probability": round(masenko_ratio, 3),
            "attack_trigger_range_px": 35,
            "heal_hp_threshold": 0.35
        },
        "timing": {
            "mean_action_delay_ms": int(mean_delay),
            "std_action_delay_ms": int(std_delay)
        },
        "map_distribution": top_maps
    }

    out_path = os.path.join('data', 'config', 'namec_policy.json')
    os.makedirs(os.path.dirname(out_path), exist_ok=True)
    with open(out_path, 'w', encoding='utf-8') as f:
        json.dump(policy_config, f, indent=2, ensure_ascii=False)

    print(f"\n[SUCCESS] Namec Behavioral Policy exported to: {out_path}")

if __name__ == '__main__':
    main()
