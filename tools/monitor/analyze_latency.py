#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Tool Phân Tích & Đánh Giá Độ Trễ API / Packet Cho Server NRO (analyze_latency.py)
- Đọc file logs/metrics/api_summary_*.csv và logs/metrics/slow_packets.log
- Tính toán ma trận tác động (Impact Matrix: Calls x Latency)
- Phát hiện các điểm nghẽn (Bottlenecks) và đưa ra gợi ý kỹ thuật
- Quản lý trạng thái giải quyết sự cố trong tools/memory/PERFORMANCE_ALERTS.json
"""

import os
import sys
import glob
import csv
import json
import argparse
from datetime import datetime

if sys.platform == "win32":
    try:
        sys.stdout.reconfigure(encoding="utf-8")
        sys.stderr.reconfigure(encoding="utf-8")
    except Exception:
        pass

WORKSPACE_ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
METRICS_DIR = os.path.join(WORKSPACE_ROOT, "logs", "metrics")
ALERTS_FILE = os.path.join(WORKSPACE_ROOT, "tools", "memory", "PERFORMANCE_ALERTS.json")

# Gợi ý tối ưu kỹ thuật theo từng phân vùng opcode
SUGGESTIONS = {
    -100: "Shop Ký Gửi: Kiểm tra vòng lặp nạp item hoặc câu SQL trong ConsignShopManager/ConsignShopService.",
    -44:  "Mở Shop: Kiểm tra kích thước gói tin gửi đồ hoặc logic lặp duyệt item trong ShopService.",
    -86:  "Giao Dịch: Kiểm tra tranh chấp khóa synchronized (Lock Contention) giữa 2 người chơi.",
    -20:  "Dùng Vật Phẩm: Kiểm tra hàm trừ item hoặc kiểm tra số lượng trong UseItem.java.",
    -7:   "Đánh Quái: Tần suất rất cao! Kiểm tra logic tìm quái hoặc spatial partition trong Mob.java.",
    -5:   "Di Chuyển: Tần suất cực cao! Cấm tuyệt đối cấp phát đối tượng mới hoặc ghi DB tại đây.",
    0:    "Đăng Nhập: Chậm do truy vấn nạp Player từ MySQL. Đẩy sang Virtual Thread hoặc tối ưu Index.",
}

def load_summary_csv(csv_path=None):
    """Đọc dữ liệu từ file CSV gần nhất hoặc file chỉ định."""
    if not csv_path:
        csv_files = glob.glob(os.path.join(METRICS_DIR, "api_summary_*.csv"))
        if not csv_files:
            return []
        csv_files.sort(reverse=True)
        csv_path = csv_files[0]

    if not os.path.exists(csv_path):
        print(f"[!] Không tìm thấy file CSV: {csv_path}")
        return []

    print(f"[*] Đang phân tích file CSV: {os.path.basename(csv_path)}")
    rows = []
    with open(csv_path, "r", encoding="utf-8") as f:
        reader = csv.DictReader(f)
        for r in reader:
            try:
                rows.append({
                    "timestamp": r.get("Timestamp", ""),
                    "cmd": int(r.get("CMD", 0)),
                    "name": r.get("CommandName", ""),
                    "calls": int(r.get("TotalCalls", 0)),
                    "avg_ms": float(r.get("AvgMs", 0)),
                    "max_ms": float(r.get("MaxMs", 0)),
                    "slow_calls": int(r.get("SlowCalls", 0)),
                    "context": r.get("SlowestContext", "")
                })
            except Exception:
                continue
    return rows

def analyze_and_print_report(rows, top_n=10):
    """Tính toán bảng xếp hạng và in phân tích chuyên sâu."""
    if not rows:
        print("[i] Chưa có dữ liệu CSV để phân tích. Hãy chạy Server một lúc để thu thập thông số.")
        return

    # Gom nhóm theo CMD lấy bản ghi mới nhất
    latest_by_cmd = {}
    for r in rows:
        latest_by_cmd[r["cmd"]] = r

    metrics_list = list(latest_by_cmd.values())

    # Tính Impact Score = calls * avg_ms
    for m in metrics_list:
        m["impact_score"] = m["calls"] * m["avg_ms"]

    # Sắp xếp theo Max Latency giảm dần
    metrics_list.sort(key=lambda x: (x["max_ms"], x["impact_score"]), reverse=True)

    print("\n" + "=" * 90)
    print("BẢNG XẾP HẠNG ĐỘ TRỄ & TỔNG TẢI API (BOTTLENECK RANKING)")
    print("=" * 90)
    print(f"{'HẠNG':<5} {'CMD':<6} {'TÊN LỆNH':<18} {'LƯỢT GỌI':<12} {'AVG (ms)':<10} {'MAX (ms)':<10} {'SLOW':<8} {'MỨC ĐỘ'}")
    print("-" * 90)

    rank = 0
    bottlenecks = []
    for m in metrics_list[:top_n]:
        rank += 1
        severity = "🟢 BÌNH THƯỜNG"
        if m["max_ms"] >= 100 or m["impact_score"] > 50000:
            severity = "🔴 NGUY HIỂM (P0)"
            bottlenecks.append(m)
        elif m["max_ms"] >= 50 or m["impact_score"] > 10000:
            severity = "🟠 CẢNH BÁO (P1)"
            bottlenecks.append(m)

        name_display = m["name"] if m["name"] else f"CMD_{m['cmd']}"
        print(f"{rank:<5} {m['cmd']:<6} {name_display:<18} {m['calls']:<12} {m['avg_ms']:<10.1f} {m['max_ms']:<10.0f} {m['slow_calls']:<8} {severity}")

    print("=" * 90)

    # Hiển thị gợi ý kỹ thuật cho các điểm nghẽn
    if bottlenecks:
        print("\n" + "=" * 90)
        print("KHUYẾN NGHỊ KỸ THUẬT CHO CÁC ĐIỂM NGHẼN:")
        print("=" * 90)
        for b in bottlenecks:
            cmd = b["cmd"]
            suggestion = SUGGESTIONS.get(cmd, "Cần kiểm tra log chi tiết trong logs/metrics/slow_packets.log.")
            print(f"👉 CMD {cmd} ({b['name']}) [Max: {b['max_ms']:.0f}ms]:")
            print(f"   * Ngữ cảnh chậm nhất: {b['context']}")
            print(f"   * Khuyến nghị: {suggestion}\n")
        print("=" * 90)

def show_slow_log(tail_lines=20):
    """Hiển thị các dòng log chậm gần nhất."""
    log_file = os.path.join(METRICS_DIR, "slow_packets.log")
    if not os.path.exists(log_file):
        print("[i] Chưa có file slow_packets.log (Không có gói tin nào bị chậm > 50ms).")
        return

    print(f"\n[*] {tail_lines} GÓI TIN CHẬM GẦN NHẤT TRONG logs/metrics/slow_packets.log:")
    print("-" * 80)
    with open(log_file, "r", encoding="utf-8") as f:
        lines = f.readlines()
        for line in lines[-tail_lines:]:
            print(line.strip())
    print("-" * 80)

def show_active_alerts():
    """Xem các cảnh báo hiện tại trong PERFORMANCE_ALERTS.json."""
    if not os.path.exists(ALERTS_FILE):
        print("[i] Chưa có file PERFORMANCE_ALERTS.json (Hệ thống không có sự cố mở).")
        return

    try:
        with open(ALERTS_FILE, "r", encoding="utf-8") as f:
            data = json.load(f)
        alerts = data.get("alerts", [])
        open_alerts = [a for a in alerts if a.get("status") == "OPEN"]
        if not open_alerts:
            print("[✅] Toàn bộ sự cố đã được giải quyết (All Clear).")
            return

        print(f"\n[🔴] CÓ {len(open_alerts)} SỰ CỐ ĐANG MỞ CẦN AI AGENT FIX KHẨN CẤP:")
        print("=" * 80)
        for a in open_alerts:
            print(f"• CMD {a.get('cmd')} ({a.get('command_name')}):")
            print(f"  - Độ trễ Max: {a.get('max_latency_ms')} ms | Avg: {a.get('avg_latency_ms')} ms")
            print(f"  - Số lần gọi: {a.get('call_count')} | Số lần chậm: {a.get('slow_count')}")
            print(f"  - Ngữ cảnh: {a.get('slowest_context')}")
            print(f"  - Thời điểm phát hiện: {a.get('detected_at')}\n")
        print("=" * 80)
    except Exception as e:
        print(f"[!] Lỗi đọc alerts file: {e}")

def resolve_alert(target_cmd):
    """Đánh dấu một sự cố đã được giải quyết."""
    if not os.path.exists(ALERTS_FILE):
        print("[i] File PERFORMANCE_ALERTS.json không tồn tại.")
        return

    try:
        with open(ALERTS_FILE, "r", encoding="utf-8") as f:
            data = json.load(f)
        updated = False
        for a in data.get("alerts", []):
            if a.get("cmd") == target_cmd and a.get("status") == "OPEN":
                a["status"] = "RESOLVED"
                a["resolved_at"] = datetime.now().isoformat()
                updated = True

        if updated:
            with open(ALERTS_FILE, "w", encoding="utf-8") as f:
                json.dump(data, f, indent=2, ensure_ascii=False)
            print(f"[✅] Đã đánh dấu sự cố CMD {target_cmd} thành RESOLVED!")
        else:
            print(f"[i] Không tìm thấy sự cố OPEN nào cho CMD {target_cmd}.")
    except Exception as e:
        print(f"[!] Lỗi cập nhật: {e}")

def main():
    parser = argparse.ArgumentParser(description="Tool Phân Tích Độ Trễ & Quản Lý Sự Cố Hiệu Năng NRO")
    parser.add_argument("--top", type=int, default=10, help="Số lượng API top hiển thị (mặc định: 10)")
    parser.add_argument("--csv", type=str, default=None, help="Đường dẫn file CSV cần phân tích")
    parser.add_argument("--slow-log", action="store_true", help="Xem chi tiết các gói tin chậm gần nhất")
    parser.add_argument("--alerts", action="store_true", help="Xem danh sách cảnh báo sự cố đang mở")
    parser.add_argument("--resolve", type=int, default=None, help="Đánh dấu giải quyết sự cố cho mã CMD cụ thể")

    args = parser.parse_args()

    if args.resolve is not None:
        resolve_alert(args.resolve)
        return

    if args.alerts:
        show_active_alerts()
        return

    if args.slow_log:
        show_slow_log()
        return

    # Mặc định phân tích bảng tóm tắt CSV
    rows = load_summary_csv(args.csv)
    analyze_and_print_report(rows, top_n=args.top)
    show_active_alerts()

if __name__ == "__main__":
    main()
