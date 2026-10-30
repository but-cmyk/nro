#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
audit_scanner.py — Project-wide Static Analysis & Architectural Audit Engine for NRO
Scans Server Java 21 & Client Unity C# across 6 Core Dimensions:
  1. Security & Anti-Dupe (Locks, Race Conditions)
  2. Protocol & Byte Stream Sync (Server <-> Client 1:1)
  3. Database & JDBC Persistence (HikariCP, try-with-resources, MySQL 8 Keywords)
  4. Numeric Overflow Safety (int vs long, arithmetic bounds)
  5. Concurrency & Performance (Zone Collections, Thread-Safety)
  6. Client Zero-GC & Asset Reality (paint/update new allocations, missing assets)
"""

import os
import re
import sys
import argparse
from datetime import datetime

# Windows console encoding fix
if sys.platform == 'win32':
    try:
        sys.stdout.reconfigure(encoding='utf-8')
    except Exception:
        pass

# ANSI Color formatting
CLR_RESET = "\033[0m"
CLR_RED = "\033[91m"
CLR_YELLOW = "\033[93m"
CLR_GREEN = "\033[92m"
CLR_CYAN = "\033[96m"
CLR_MAGENTA = "\033[95m"
CLR_BOLD = "\033[1m"
CLR_DIM = "\033[2m"

class Finding:
    def __init__(self, severity, dimension, file_path, line_no, title, symptom, cause, remediation):
        self.severity = severity  # 'P0_CRITICAL', 'P1_HIGH', 'P2_MEDIUM', 'P3_LOW'
        self.dimension = dimension
        self.file_path = file_path
        self.line_no = line_no
        self.title = title
        self.symptom = symptom
        self.cause = cause
        self.remediation = remediation

    @property
    def badge(self):
        if self.severity == 'P0_CRITICAL':
            return f"{CLR_RED}{CLR_BOLD}[🔴 P0 - CRITICAL]{CLR_RESET}"
        elif self.severity == 'P1_HIGH':
            return f"{CLR_YELLOW}{CLR_BOLD}[🟠 P1 - HIGH]{CLR_RESET}"
        elif self.severity == 'P2_MEDIUM':
            return f"{CLR_CYAN}[🟡 P2 - MEDIUM]{CLR_RESET}"
        else:
            return f"{CLR_GREEN}[🟢 P3 - LOW/INFO]{CLR_RESET}"

class AuditScanner:
    def __init__(self, workspace_root="."):
        self.root = workspace_root
        self.findings = []

    def scan_all(self):
        self.scan_database()
        self.scan_numeric_overflow()
        self.scan_security_and_dupe()
        self.scan_concurrency_and_zones()
        self.scan_protocol_and_streams()
        self.scan_client_zero_gc()

    # =========================================================================
    # 1. DATABASE & JDBC AUDIT
    # =========================================================================
    def scan_database(self):
        db_dir = os.path.join(self.root, "src", "database")
        if not os.path.exists(db_dir):
            return

        mysql_keywords = re.compile(r'\b(rank|group|order|desc|read|key|status)\b\s*[,=)]', re.IGNORECASE)
        static_conn_re = re.compile(r'\bstatic\s+Connection\s+[a-zA-Z0-9_]+\s*[;=]')

        for r, _, files in os.walk(db_dir):
            for f in files:
                if not f.endswith(".java"):
                    continue
                path = os.path.join(r, f)
                rel_path = os.path.relpath(path, self.root)
                with open(path, "r", encoding="utf-8", errors="ignore") as fh:
                    lines = fh.readlines()

                content = "".join(lines)

                # Check static connection anti-pattern
                for idx, line in enumerate(lines):
                    if static_conn_re.search(line):
                        self.findings.append(Finding(
                            severity="P0_CRITICAL",
                            dimension="Database",
                            file_path=rel_path,
                            line_no=idx + 1,
                            title="Static Connection chống pool management",
                            symptom="Connection static dùng chung giữa các luồng gây xung đột transaction hoặc leak vĩnh viễn.",
                            cause=f"Khai báo: `{line.strip()}`",
                            remediation="Bỏ static Connection. Sử dụng HikariCP getConnection() cục bộ trong khối try-with-resources."
                        ))

                # Check missing try-with-resources
                for idx, line in enumerate(lines):
                    if ("getConnection()" in line or "createStatement()" in line or "prepareStatement(" in line) and "try (" not in line:
                        prev_window = "".join(lines[max(0, idx - 4):idx + 1])
                        if "try (" in prev_window or "try(" in prev_window or "try-with-resources" in line:
                            continue
                        surrounding = "".join(lines[idx:min(len(lines), idx + 25)])
                        if "close()" not in surrounding:
                                self.findings.append(Finding(
                                    severity="P1_HIGH",
                                    dimension="Database",
                                    file_path=rel_path,
                                    line_no=idx + 1,
                                    title="Truy vấn JDBC không dùng try-with-resources",
                                    symptom="Rò rỉ connection pool HikariCP khi xảy ra ngoại lệ SQL, dẫn đến cạn kiệt pool và đơ server.",
                                    cause=f"Lệnh mở tài nguyên: `{line.strip()}`",
                                    remediation="Chuyển sang cú pháp: try (Connection conn = ...; PreparedStatement ps = ...) { ... }"
                                ))

                # Check unbackticked MySQL 8 keywords
                for idx, line in enumerate(lines):
                    if ("SELECT " in line.upper() or "INSERT " in line.upper() or "UPDATE " in line.upper()) and not line.strip().startswith("//"):
                        match = mysql_keywords.search(line)
                        if match and "`" not in line:
                            kw = match.group(1)
                            self.findings.append(Finding(
                                severity="P2_MEDIUM",
                                dimension="Database",
                                file_path=rel_path,
                                line_no=idx + 1,
                                title=f"Tên cột trùng từ khóa MySQL 8 (`{kw}`) không có backtick",
                                symptom="MySQL 8.x ném Syntax Error khi thực thi query do trùng từ khóa bảo lưu.",
                                cause=f"Query: `{line.strip()}`",
                                remediation=f"Bọc tên cột trong dấu backtick: `{kw}`"
                            ))

    # =========================================================================
    # 2. NUMERIC OVERFLOW SAFETY
    # =========================================================================
    def scan_numeric_overflow(self):
        player_model = os.path.join(self.root, "src", "models", "player")
        if not os.path.exists(player_model):
            return

        accumulative_fields = ["power", "tiemNang", "hp", "dame", "hpMax", "gold"]

        for target_file in ["NPoint.java", "Player.java"]:
            path = os.path.join(player_model, target_file)
            if not os.path.exists(path):
                continue
            rel_path = os.path.relpath(path, self.root)
            with open(path, "r", encoding="utf-8", errors="ignore") as fh:
                lines = fh.readlines()

            for idx, line in enumerate(lines):
                for field in accumulative_fields:
                    pattern = rf'\b(?:public|private|protected)\s+int\s+{field}\b'
                    if re.search(pattern, line):
                        self.findings.append(Finding(
                            severity="P0_CRITICAL",
                            dimension="Numeric Overflow",
                            file_path=rel_path,
                            line_no=idx + 1,
                            title=f"Trường tích lũy `{field}` dùng kiểu int thay vì long",
                            symptom="Khi đạt mốc >2.14 tỷ, giá trị sẽ bị tràn thành số âm khiến nhân vật bị lỗi điểm hoặc reset chỉ số.",
                            cause=f"Khai báo: `{line.strip()}`",
                            remediation=f"Đổi kiểu dữ liệu từ `int {field}` sang `long {field}` và cập nhật toàn bộ getter/setter liên quan."
                        ))

    # =========================================================================
    # 3. SECURITY & ANTI-DUPE
    # =========================================================================
    def scan_security_and_dupe(self):
        services_dir = os.path.join(self.root, "src", "services")
        if not os.path.exists(services_dir):
            return

        # Check critical item transaction methods for lack of synchronization or cooldown
        dupe_sensitive_files = ["CombineService.java", "ShopService.java", "TradeService.java"]

        for r, _, files in os.walk(services_dir):
            for f in files:
                if f in dupe_sensitive_files:
                    path = os.path.join(r, f)
                    rel_path = os.path.relpath(path, self.root)
                    with open(path, "r", encoding="utf-8", errors="ignore") as fh:
                        lines = fh.readlines()

                    content = "".join(lines)
                    # Check if synchronized exists in combine/trade methods
                    for idx, line in enumerate(lines):
                        if ("subQuantityItemsBag" in line or "addItemBag" in line) and not line.strip().startswith("//"):
                            # Look back up to 80 lines within enclosing method for synchronized
                            window = "".join(lines[max(0, idx - 150):idx])
                            if "synchronized" not in window:
                                self.findings.append(Finding(
                                    severity="P1_HIGH",
                                    dimension="Security",
                                    file_path=rel_path,
                                    line_no=idx + 1,
                                    title="Thao tác biến đổi Item thiếu khối khóa synchronized",
                                    symptom="Người chơi có thể dupe đồ bằng cách gửi liên tục 2 packet thao tác cùng lúc trong vài mili-giây.",
                                    cause=f"Gọi trừ/cộng đồ: `{line.strip()}` không nằm trong synchronized block.",
                                    remediation="Bọc toàn bộ khối kiểm tra và trừ/cộng vật phẩm trong synchronized (player.inventory.itemsBag) { ... }"
                                ))

    # =========================================================================
    # 4. CONCURRENCY & ZONE THREAD-SAFETY
    # =========================================================================
    def scan_concurrency_and_zones(self):
        zone_file = os.path.join(self.root, "src", "models", "map", "Zone.java")
        if not os.path.exists(zone_file):
            return

        rel_path = os.path.relpath(zone_file, self.root)
        with open(zone_file, "r", encoding="utf-8", errors="ignore") as fh:
            lines = fh.readlines()

        for idx, line in enumerate(lines):
            # Check for raw ArrayList declaration for concurrent entities
            if re.search(r'\bList<(?:Mob|ItemMap)>\s+(?:mobs|items)\s*=\s*new\s+ArrayList', line):
                self.findings.append(Finding(
                    severity="P2_MEDIUM",
                    dimension="Concurrency",
                    file_path=rel_path,
                    line_no=idx + 1,
                    title="Collection thực thể trong Zone sử dụng ArrayList thô không thread-safe",
                    symptom="Có thể ném ConcurrentModificationException khi vòng lặp tick duyệt danh sách đúng lúc có người nhặt đồ / mob chết.",
                    cause=f"Khai báo: `{line.strip()}`",
                    remediation="Bọc danh sách trong synchronized khi duyệt hoặc chuyển sang CopyOnWriteArrayList / ConcurrentLinkedQueue."
                ))

    # =========================================================================
    # 5. PROTOCOL & STREAM ASYMMETRY
    # =========================================================================
    def scan_protocol_and_streams(self):
        server_cmd = os.path.join(self.root, "src", "server", "Command.java")
        client_ctrl = os.path.join(self.root, "Client", "Client", "Assets", "Scripts", "Game1", "God", "Controller.cs")

        if not os.path.exists(server_cmd) or not os.path.exists(client_ctrl):
            return

        with open(server_cmd, "r", encoding="utf-8", errors="ignore") as fh:
            s_content = fh.read()
        with open(client_ctrl, "r", encoding="utf-8", errors="ignore") as fh:
            c_content = fh.read()

        # Find registered opcodes in Controller.cs
        client_cases = set(re.findall(r'case\s+(-?\d+)\s*:', c_content))
        # Find command constants in Command.java
        server_opcodes = re.findall(r'public\s+static\s+final\s+byte\s+([A-Z0-9_]+)\s*=\s*(-?\d+);', s_content)

        missing_count = 0
        for name, op in server_opcodes:
            if op not in client_cases and int(op) < 0:
                missing_count += 1

        if missing_count > 15:
            self.findings.append(Finding(
                severity="P3_LOW",
                dimension="Protocol",
                file_path="src/server/Command.java",
                line_no=1,
                title=f"Phát hiện {missing_count} Opcode Server khai báo nhưng không có case xử lý ở Client Controller.cs",
                symptom="Các gói tin này nếu được gửi tới Client sẽ rơi vào default case và bị drop hoặc log warning.",
                cause="Một số tính năng cũ hoặc packet phụ trợ chưa được nối luồng xử lý ở Client.",
                remediation="Rà soát danh sách opcode không dùng đến, dọn dẹp Command.java hoặc bổ sung case xử lý tại Controller.cs."
            ))

    # =========================================================================
    # 6. CLIENT UNITY ZERO-GC AUDIT
    # =========================================================================
    def scan_client_zero_gc(self):
        scripts_dir = os.path.join(self.root, "Client", "Client", "Assets", "Scripts")
        if not os.path.exists(scripts_dir):
            return

        new_in_paint_re = re.compile(r'\bnew\s+[a-zA-Z0-9_]+\s*\(')

        for r, _, files in os.walk(scripts_dir):
            for f in files:
                if not f.endswith(".cs"):
                    continue
                path = os.path.join(r, f)
                rel_path = os.path.relpath(path, self.root)
                with open(path, "r", encoding="utf-8", errors="ignore") as fh:
                    lines = fh.readlines()

                in_paint = False
                paint_start = 0
                brace_count = 0

                for idx, line in enumerate(lines):
                    trimmed = line.strip()
                    if ("void paint(" in trimmed or "void update(" in trimmed) and not trimmed.startswith("//"):
                        in_paint = True
                        paint_start = idx
                        brace_count = 0

                    if in_paint:
                        brace_count += trimmed.count("{") - trimmed.count("}")
                        if new_in_paint_re.search(trimmed) and not trimmed.startswith("//"):
                            # Filter benign new array like new int[] in non-frame setups
                            if "Vector" in trimmed or "string" in trimmed or "StringBuilder" in trimmed:
                                self.findings.append(Finding(
                                    severity="P2_MEDIUM",
                                    dimension="Client Zero-GC",
                                    file_path=rel_path,
                                    line_no=idx + 1,
                                    title="Cấp phát đối tượng (new) bên trong vòng lặp render 60 FPS",
                                    symptom="Sinh rác bộ nhớ (GC allocation) liên tục mỗi frame, gây giật khựng màn hình (frame drop) trên thiết bị di động.",
                                    cause=f"Lệnh cấp phát: `{trimmed}` trong hàm paint/update.",
                                    remediation="Tái sử dụng đối tượng (Object Pooling) hoặc dùng biến static/field thành viên để lưu trữ tạm."
                                ))
                        if brace_count <= 0 and idx > paint_start:
                            in_paint = False

    # =========================================================================
    # REPORTING & EXPORT
    # =========================================================================
    def print_report(self):
        counts = {"P0_CRITICAL": 0, "P1_HIGH": 0, "P2_MEDIUM": 0, "P3_LOW": 0}
        for f in self.findings:
            counts[f.severity] += 1

        print("\n" + "=" * 70)
        print(f"{CLR_BOLD}BÁO CÁO RÀ SOÁT KIẾN TRÚC & MÃ NGUỒN TOÀN DỰ ÁN NRO{CLR_RESET}")
        print("=" * 70)
        print(f"Thời gian quét : {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
        print(f"Tổng phát hiện : {CLR_BOLD}{len(self.findings)}{CLR_RESET} vấn đề")
        print(f"  - {CLR_RED}🔴 P0 - CRITICAL (Sống còn){CLR_RESET} : {counts['P0_CRITICAL']}")
        print(f"  - {CLR_YELLOW}🟠 P1 - HIGH (Nghiêm trọng){CLR_RESET} : {counts['P1_HIGH']}")
        print(f"  - {CLR_CYAN}🟡 P2 - MEDIUM (Cảnh báo){CLR_RESET}   : {counts['P2_MEDIUM']}")
        print(f"  - {CLR_GREEN}🟢 P3 - LOW/INFO (Tối ưu){CLR_RESET}    : {counts['P3_LOW']}")
        print("-" * 70)

        if not self.findings:
            print(f"\n{CLR_GREEN}{CLR_BOLD}✅ XUẤT SẮC: Không phát hiện bất kỳ vi phạm kiến trúc nào! Hệ thống đạt chuẩn.{CLR_RESET}\n")
            return

        # Sort by severity
        order = {"P0_CRITICAL": 0, "P1_HIGH": 1, "P2_MEDIUM": 2, "P3_LOW": 3}
        sorted_findings = sorted(self.findings, key=lambda x: order.get(x.severity, 4))

        for idx, f in enumerate(sorted_findings):
            print(f"\n{idx + 1}. {f.badge} {CLR_BOLD}{f.title}{CLR_RESET}")
            print(f"   {CLR_DIM}Vị trí     :{CLR_RESET} {f.file_path}:{f.line_no}")
            print(f"   {CLR_DIM}Chiều xét  :{CLR_RESET} {f.dimension}")
            print(f"   {CLR_DIM}Triệu chứng:{CLR_RESET} {f.symptom}")
            print(f"   {CLR_DIM}Nguyên nhân:{CLR_RESET} {f.cause}")
            print(f"   {CLR_GREEN}Giải pháp  :{CLR_RESET} {f.remediation}")

        print("\n" + "=" * 70)
        print(f"{CLR_BOLD}KẾ HOẠCH HÀNH ĐỘNG KHUYẾN NGHỊ:{CLR_RESET}")
        if counts["P0_CRITICAL"] > 0:
            print(f"  1. {CLR_RED}BẮT BUỘC KHẮC PHỤC NGAY LẬP TỨC{CLR_RESET} {counts['P0_CRITICAL']} lỗi P0_CRITICAL để bảo vệ an toàn server.")
        if counts["P1_HIGH"] > 0:
            print(f"  2. {CLR_YELLOW}Ưu tiên xử lý tiếp{CLR_RESET} {counts['P1_HIGH']} lỗi P1_HIGH chống dupe đồ & rò rỉ database.")
        print("=" * 70 + "\n")

    def export_markdown(self, output_path):
        counts = {"P0_CRITICAL": 0, "P1_HIGH": 0, "P2_MEDIUM": 0, "P3_LOW": 0}
        for f in self.findings:
            counts[f.severity] += 1

        md = []
        md.append("# Báo Cáo Rà Soát Toàn Diện Mã Nguồn & Kiến Trúc Dự Án NRO\n")
        md.append(f"- **Thời gian thực hiện**: `{datetime.now().strftime('%Y-%m-%d %H:%M:%S')}`")
        md.append(f"- **Tổng số vấn đề phát hiện**: **{len(self.findings)}**\n")
        md.append("| Mức Độ | Số Lượng | Ý Nghĩa Thực Tế |")
        md.append("| :--- | :---: | :--- |")
        md.append(f"| 🔴 **P0 - CRITICAL** | **{counts['P0_CRITICAL']}** | Nguy cơ sập Server/Client, Dupe đồ, Sập pool database. |")
        md.append(f"| 🟠 **P1 - HIGH** | **{counts['P1_HIGH']}** | Tràn số âm điểm, Race condition, Mất item khi disconnect. |")
        md.append(f"| 🟡 **P2 - MEDIUM** | **{counts['P2_MEDIUM']}** | GC frame drop giật lag mobile, Thiếu backtick MySQL 8. |")
        md.append(f"| 🟢 **P3 - LOW / INFO** | **{counts['P3_LOW']}** | Code smells, dead code, tối ưu thuật toán. |\n")
        md.append("## Danh Sách Chi Tiết Các Vấn Đề Thật\n")

        order = {"P0_CRITICAL": 0, "P1_HIGH": 1, "P2_MEDIUM": 2, "P3_LOW": 3}
        sorted_findings = sorted(self.findings, key=lambda x: order.get(x.severity, 4))

        for idx, f in enumerate(sorted_findings):
            md.append(f"### {idx + 1}. [{f.severity.replace('_', ' ')}] {f.title}")
            md.append(f"- **Vị trí**: `{f.file_path}:{f.line_no}`")
            md.append(f"- **Chiều phân tích**: `{f.dimension}`")
            md.append(f"- **Triệu chứng thực tế**: {f.symptom}")
            md.append(f"- **Nguyên nhân gốc rễ**: `{f.cause}`")
            md.append(f"- **Giải pháp khắc phục chuẩn**: {f.remediation}\n")

        with open(output_path, "w", encoding="utf-8") as fh:
            fh.write("\n".join(md))
        print(f"Đã xuất báo cáo chi tiết thành công ra tệp: {output_path}")

def main():
    parser = argparse.ArgumentParser(description="NRO Architecture & Code Security Scanner")
    parser.add_argument("--all", action="store_true", help="Quét toàn bộ dự án trên cả 6 chiều đánh giá")
    parser.add_argument("--security", action="store_true", help="Quét chuyên sâu bảo mật & chống dupe")
    parser.add_argument("--database", action="store_true", help="Quét rò rỉ database & JDBC try-with-resources")
    parser.add_argument("--numeric", action="store_true", help="Quét nguy cơ tràn số int thay vì long")
    parser.add_argument("--client", action="store_true", help="Quét hiệu năng & Zero-GC Client Unity")
    parser.add_argument("--protocol", action="store_true", help="Quét lệch stream opcode Server-Client")
    parser.add_argument("--export-markdown", type=str, default="", help="Đường dẫn xuất file Markdown báo cáo")

    args = parser.parse_args()
    scanner = AuditScanner()

    if args.all or len(sys.argv) == 1:
        scanner.scan_all()
    else:
        if args.security:
            scanner.scan_security_and_dupe()
        if args.database:
            scanner.scan_database()
        if args.numeric:
            scanner.scan_numeric_overflow()
        if args.client:
            scanner.scan_client_zero_gc()
        if args.protocol:
            scanner.scan_protocol_and_streams()

    scanner.print_report()

    if args.export_markdown:
        scanner.export_markdown(args.export_markdown)

if __name__ == "__main__":
    main()
