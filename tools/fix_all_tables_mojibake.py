import sys
import os
import pymysql

sys.stdout.reconfigure(encoding='utf-8')

def fix_mojibake_str(val):
    if not isinstance(val, str) or not val:
        return val
    # Check if there are indicators of UTF-8 Mojibake in Latin-1
    moji_indicators = ['Ã', 'áº', 'á»', 'Ä', 'Æ°', 'Æ¡', 'á»‘', 'á»“', 'á»•', 'á»—', 'á»™', 'Ãª', 'Ã¢', 'Ã´', 'Ã ', 'Ã¡', 'Ã¨', 'Ã©', 'Ã¬', 'Ã­', 'Ã²', 'Ã³', 'Ã¹', 'Ãº', 'Ã½', 'Ä‘']
    if any(ind in val for ind in moji_indicators):
        try:
            fixed = val.encode('latin1').decode('utf-8')
            return fixed
        except Exception:
            return val
    return val

def main():
    print("=== STARTING FULL DATABASE MOJIBAKE CLEANUP ===")
    conn = pymysql.connect(
        host='127.0.0.1',
        port=3308,
        user='root',
        password='root',
        database='nro_data',
        charset='utf8mb4'
    )
    cur = conn.cursor()

    cur.execute("SHOW TABLES")
    tables = [r[0] for r in cur.fetchall()]

    total_fixed_cells = 0

    for tbl in tables:
        cur.execute(f"DESCRIBE `{tbl}`")
        cols_info = cur.fetchall()
        
        # Identify primary key or unique key
        pk_cols = [c[0] for c in cols_info if c[3] == 'PRI']
        if not pk_cols:
            # If no primary key, use all columns or skip
            pk_col = cols_info[0][0]
        else:
            pk_col = pk_cols[0]

        # Filter string/text columns
        text_cols = [c[0] for c in cols_info if any(t in c[1].lower() for t in ['char', 'text', 'blob', 'json'])]
        if not text_cols:
            continue

        # Select all rows
        col_list = f"`{pk_col}`, " + ", ".join([f"`{c}`" for c in text_cols])
        cur.execute(f"SELECT {col_list} FROM `{tbl}`")
        rows = cur.fetchall()

        table_fixed_count = 0
        for row in rows:
            pk_val = row[0]
            updates = {}
            for idx, col_name in enumerate(text_cols):
                orig_val = row[idx + 1]
                if orig_val is not None and isinstance(orig_val, str):
                    new_val = fix_mojibake_str(orig_val)
                    if new_val != orig_val:
                        updates[col_name] = new_val

            if updates:
                set_clause = ", ".join([f"`{k}` = %s" for k in updates.keys()])
                params = list(updates.values()) + [pk_val]
                cur.execute(f"UPDATE `{tbl}` SET {set_clause} WHERE `{pk_col}` = %s", params)
                table_fixed_count += len(updates)

        if table_fixed_count > 0:
            print(f"[OK] Table `{tbl}`: Fixed {table_fixed_count} cells!")
            conn.commit()
            total_fixed_cells += table_fixed_count

    print(f"\n>>> TOTAL FIXED CELLS ACROSS DATABASE: {total_fixed_cells}")
    conn.commit()
    conn.close()

if __name__ == '__main__':
    main()
