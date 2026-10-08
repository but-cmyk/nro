import pymysql, json, sys, io
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

conn = pymysql.connect(host='localhost', port=3308, user='root', password='root', database='nro_data')
cur = conn.cursor()

# 1. Update id = 3 (Masenko)
cur.execute("SELECT skills FROM skill_template WHERE id = 3")
row_masenko = cur.fetchone()[0]
skills_raw = json.loads(row_masenko)

masenko_new_dame = [115, 130, 150, 170, 190, 210, 230]
new_masenko_list = []
for idx, s_str in enumerate(skills_raw):
    s_obj = json.loads(s_str)
    s_obj["damage"] = masenko_new_dame[idx]
    new_masenko_list.append(json.dumps(s_obj, ensure_ascii=False))

new_masenko_json = json.dumps(new_masenko_list, ensure_ascii=False)
cur.execute("UPDATE skill_template SET skills = %s WHERE id = 3", (new_masenko_json,))
print("Updated Masenko (id=3) successfully!")

# 2. Update id = 5 (Antomic)
cur.execute("SELECT skills FROM skill_template WHERE id = 5")
row_antomic = cur.fetchone()[0]
skills_raw = json.loads(row_antomic)

new_antomic_list = []
for idx, s_str in enumerate(skills_raw):
    s_obj = json.loads(s_str)
    if idx == 0:
        s_obj["damage"] = 125
    new_antomic_list.append(json.dumps(s_obj, ensure_ascii=False))

new_antomic_json = json.dumps(new_antomic_list, ensure_ascii=False)
cur.execute("UPDATE skill_template SET skills = %s WHERE id = 5", (new_antomic_json,))
print("Updated Antomic (id=5) successfully!")

conn.commit()

# 3. Verify
print("\n--- VERIFICATION FROM DATABASE ---")
cur.execute("SELECT id, name, skills FROM skill_template WHERE id IN (3, 5) ORDER BY id")
for row in cur.fetchall():
    print(f"\nSkill ID {row[0]}: {row[1]}")
    s_list = json.loads(row[2])
    for idx, s_str in enumerate(s_list):
        s_obj = json.loads(s_str)
        print(f"  Lv {idx+1}: damage = {s_obj.get('damage')}%, coolDown = {s_obj.get('cool_down')}ms, mana = {s_obj.get('mana_use')}")

conn.close()
