import sys
import subprocess
import os

if sys.stdout.encoding != 'utf-8':
    try:
        sys.stdout.reconfigure(encoding='utf-8')
    except Exception:
        pass

cp1252_special = {
    0x20ac: 0x80, 0x201a: 0x82, 0x0192: 0x83, 0x201e: 0x84,
    0x2026: 0x85, 0x2020: 0x86, 0x2021: 0x87, 0x02c6: 0x88,
    0x2030: 0x89, 0x0160: 0x8a, 0x2039: 0x8b, 0x0152: 0x8c,
    0x017d: 0x8e, 0x2018: 0x91, 0x2019: 0x92, 0x201c: 0x93,
    0x201d: 0x94, 0x2022: 0x95, 0x2013: 0x96, 0x2014: 0x97,
    0x02dc: 0x98, 0x2122: 0x99, 0x0161: 0x9a, 0x203a: 0x9b,
    0x0153: 0x9c, 0x017e: 0x9e, 0x0178: 0x9f
}

def decode_mojibake(s):
    if not s or not isinstance(s, str) or not any(ord(c) >= 128 for c in s):
        return s

    ba = bytearray()
    for ch in s:
        cp = ord(ch)
        if cp in cp1252_special:
            ba.append(cp1252_special[cp])
        elif cp < 256:
            ba.append(cp)
        else:
            ba.extend(ch.encode('utf-8'))
    try:
        res = ba.decode('utf-8')
        return res
    except Exception:
        return s

def run_mysql_query(sql):
    cmd = ['docker', 'exec', '-i', 'nro_mysql', 'mysql', '-uroot', '-proot', 'nro_data', '--default-character-set=utf8mb4', '-s', '-N', '-e', sql]
    res = subprocess.run(cmd, capture_output=True)
    if res.returncode != 0:
        err = res.stderr.decode('utf-8', errors='ignore')
        raise RuntimeError(f"MySQL Query Error: {err}")
    return res.stdout.decode('utf-8', errors='ignore')

def run_mysql_script(sql_content):
    cmd = ['docker', 'exec', '-i', 'nro_mysql', 'mysql', '-uroot', '-proot', 'nro_data', '--default-character-set=utf8mb4']
    res = subprocess.run(cmd, input=sql_content.encode('utf-8'), capture_output=True)
    if res.returncode != 0:
        err = res.stderr.decode('utf-8', errors='ignore')
        raise RuntimeError(f"MySQL Exec Error: {err}")

def fix_table(table, id_col, columns):
    print(f"[*] Dang quet kiem tra `{table}`...")
    cols_str = ", ".join([f"`{c}`" for c in columns])
    query = f"SELECT `{id_col}`, {cols_str} FROM `{table}`;"
    raw_data = run_mysql_query(query)
    
    lines = raw_data.split('\n')
    total_updated = 0
    sql_statements = []
    
    for line in lines:
        if not line.strip():
            continue
        parts = line.split('\t')
        row_id = parts[0].strip()
        vals = parts[1:]
        
        updates = []
        for i, col in enumerate(columns):
            if i < len(vals):
                val = vals[i]
                fixed_val = decode_mojibake(val)
                if fixed_val != val:
                    esc = fixed_val.replace("\\", "\\\\").replace("'", "\\'").replace('"', '\\"').replace('\r', '\\r').replace('\n', '\\n')
                    updates.append(f"`{col}` = '{esc}'")
        
        if updates:
            sql_statements.append(f"UPDATE `{table}` SET {', '.join(updates)} WHERE `{id_col}` = '{row_id}';")
            total_updated += 1
            
            if len(sql_statements) >= 100:
                run_mysql_script("\n".join(sql_statements))
                sql_statements = []

    if sql_statements:
        run_mysql_script("\n".join(sql_statements))
        
    print(f"    -> Bang `{table}`: Da cap nhat {total_updated} dong can sua.")

def main():
    print("===================================================================")
    print("       QUET & SUA TOAN DIEN MOJIBAKE TRONG DATABASE NRO_DATA       ")
    print("===================================================================")
    
    tables_to_fix = [
        ('task_main_template', 'id', ['name', 'detail']),
        ('task_sub_template', 'ducvupro', ['NAME', 'notify']),
        ('map_template', 'id', ['name']),
        ('tab_shop', 'id', ['tab_name']),
        ('npc_template', 'id', ['name']),
        ('mob_template', 'id', ['name']),
        ('item_template', 'id', ['name', 'description']),
        ('side_task_template', 'id', ['name']),
        ('clan_task_template', 'id', ['name']),
        ('notify', 'id', ['name', 'text']),
        ('item_option_template', 'id', ['name']),
        ('skill_template', 'id', ['name']),
        ('achievement_template', 'id', ['info1', 'info2']),
        ('radar', 'id', ['name', 'info']),
        ('intrinsic', 'id', ['name'])
    ]
    
    for table, id_col, cols in tables_to_fix:
        try:
            fix_table(table, id_col, cols)
        except Exception as e:
            print(f"    [!] Loi khi xu ly bang {table}: {e}")

    print("\n===================================================================")
    print("       HOAN TAT SUA TEXT MOJIBAKE TOAN BO DATABASE!")
    print("===================================================================")

if __name__ == '__main__':
    main()
