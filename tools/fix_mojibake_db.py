import sys
import subprocess
import re

# Đảm bảo stdout hỗ trợ UTF-8
if sys.stdout.encoding != 'utf-8':
    try:
        sys.stdout.reconfigure(encoding='utf-8')
    except Exception:
        pass

def fix_mojibake_str(s):
    if not s or not isinstance(s, str):
        return s
    # Nếu không có dấu hiệu ký tự lạ của mojibake UTF-8, giữ nguyên
    # Các ký tự điển hình của mojibake UTF-8 qua CP1252: Ã, á, Â, Ä, Æ, Ê, Ô, Ơ, Ư, Đ, ...
    mojibake_markers = ['Ã', 'á»', 'áº', 'Æ', 'Ä', 'áº¡', 'Â', 'á»™', 'á»§', 'áº£', 'áº½', 'á»‰', 'á» ', 'Ãª', 'Ã¡', 'Ã ', 'Ã³', 'Ã²', 'Ãº', 'Ã¹', 'Ã½', 'Ä‘', 'Ä']
    if not any(m in s for m in mojibake_markers):
        return s

    # Thử convert cp1252 -> utf-8
    try:
        # Thay thế một số ký tự đặc thù nếu cần
        fixed = s.encode('cp1252').decode('utf-8')
        return fixed
    except Exception:
        pass

    # Thử latin1 -> utf-8
    try:
        fixed = s.encode('latin-1').decode('utf-8')
        return fixed
    except Exception:
        pass

    # Thử từng phần tử hoặc replace bằng charmap linh hoạt
    try:
        # byte array map theo cp1252
        b = bytearray()
        for ch in s:
            cp = ord(ch)
            if cp < 256:
                b.append(cp)
            elif ch == '\u2122': # ™ trong cp1252 là 0x99
                b.append(0x99)
            elif ch == '\u201c': # “
                b.append(0x93)
            elif ch == '\u201d': # ”
                b.append(0x94)
            elif ch == '\u2018': # ‘
                b.append(0x91)
            elif ch == '\u2019': # ’
                b.append(0x92)
            elif ch == '\u2022': # •
                b.append(0x95)
            elif ch == '\u2013': # –
                b.append(0x96)
            elif ch == '\u2014': # —
                b.append(0x97)
            elif ch == '\u02dc': # ˜
                b.append(0x98)
            elif ch == '\u0161': # š
                b.append(0x9a)
            elif ch == '\u203a': # ›
                b.append(0x9b)
            elif ch == '\u0153': # œ
                b.append(0x9c)
            elif ch == '\u017e': # ž
                b.append(0x9e)
            elif ch == '\u0178': # Ÿ
                b.append(0x9f)
            else:
                b.extend(ch.encode('utf-8'))
        fixed = b.decode('utf-8')
        return fixed
    except Exception:
        return s

def run_mysql_query(sql):
    cmd = ['docker', 'exec', '-i', 'nro_mysql', 'mysql', '-uroot', '-proot', 'nro_data', '--default-character-set=utf8mb4', '-s', '-N', '-e', sql]
    res = subprocess.run(cmd, capture_output=True)
    if res.returncode != 0:
        err = res.stderr.decode('utf-8', errors='ignore')
        raise RuntimeError(f"MySQL Error: {err}")
    return res.stdout.decode('utf-8', errors='ignore')

def run_mysql_execute(sql):
    cmd = ['docker', 'exec', '-i', 'nro_mysql', 'mysql', '-uroot', '-proot', 'nro_data', '--default-character-set=utf8mb4', '-e', sql]
    res = subprocess.run(cmd, capture_output=True)
    if res.returncode != 0:
        err = res.stderr.decode('utf-8', errors='ignore')
        raise RuntimeError(f"MySQL Error: {err}")

def fix_table_column(table, id_col, target_col):
    print(f"[*] Dang kiem tra {table}.{target_col}...")
    # Lấy toàn bộ id và target_col
    data = run_mysql_query(f"SELECT `{id_col}`, `{target_col}` FROM `{table}`;")
    lines = data.split('\n')
    updated_count = 0
    sql_batch = []
    for line in lines:
        if not line.strip():
            continue
        parts = line.split('\t', 1)
        row_id = parts[0].strip()
        val = parts[1] if len(parts) > 1 else ""
        
        fixed_val = fix_mojibake_str(val)
        if fixed_val != val:
            # Escape cho SQL
            escaped = fixed_val.replace("\\", "\\\\").replace("'", "\\'").replace('"', '\\"').replace('\r', '\\r').replace('\n', '\\n')
            sql_batch.append(f"UPDATE `{table}` SET `{target_col}` = '{escaped}' WHERE `{id_col}` = '{row_id}';")
            updated_count += 1
            if len(sql_batch) >= 100:
                run_mysql_execute("\n".join(sql_batch))
                sql_batch = []
                
    if sql_batch:
        run_mysql_execute("\n".join(sql_batch))
        
    print(f"    [OK] Da sua {updated_count} dong trong {table}.{target_col}!")

if __name__ == '__main__':
    # Test thử trên task_main_template
    print("--- TEST CHUYEN DOI TEXT MOJIBAKE ---")
    s_test = "Cuá»™c dáº¡o chÆ¡i cá»§a XÃªn"
    print(f"Original: {s_test}")
    print(f"Fixed:    {fix_mojibake_str(s_test)}")
    
    s_test2 = "NÃ¢ng sá»©c Ä‘Ã¡nh gá»‘c lÃªn 10K"
    print(f"Original: {s_test2}")
    print(f"Fixed:    {fix_mojibake_str(s_test2)}")
    
    s_test3 = "ThÃ¡nh phá»‘ phÃ­a nam khu vá»±c 17"
    print(f"Original: {s_test3}")
    print(f"Fixed:    {fix_mojibake_str(s_test3)}")
    
    s_test4 = "Ã o váº£i 3 lá»—"
    print(f"Original: {s_test4}")
    print(f"Fixed:    {fix_mojibake_str(s_test4)}")
