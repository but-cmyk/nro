import sys
import json
import pymysql

sys.stdout.reconfigure(encoding='utf-8')
conn = pymysql.connect(host='localhost', port=3308, user='root', password='root', database='nro_data')
cursor = conn.cursor()
cursor.execute('SELECT id, name, mobs, waypoints FROM map_template WHERE id = 62')
r = cursor.fetchone()
print(f"Map {r[0]}: {r[1]}")
rep = r[2].replace('\"', '')
mobs = json.loads(rep)
print('Total mobs:', len(mobs))
for i, m in enumerate(mobs):
    print(f"  Mob {i}: tempId={m[0]}, level={m[1]}, hp={m[2]}, x={m[3]}, y={m[4]}")
print('Waypoints:', r[3])
conn.close()
