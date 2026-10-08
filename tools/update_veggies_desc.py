import sys, io
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
import pymysql

# 1. Update MySQL
conn = pymysql.connect(host='localhost', port=3308, user='root', password='root', database='nro_data')
cur = conn.cursor()
cur.execute("UPDATE item_template SET description = 'Tác dụng trong 10 phút' WHERE id IN (191, 192, 2047)")
conn.commit()
print("Updated item_template in MySQL database for 191, 192, 2047 successfully!")

# Verify MySQL
cur.execute("SELECT id, name, description FROM item_template WHERE id IN (191, 192, 2047)")
for r in cur.fetchall():
    print(f"  ID {r[0]}: {r[1]} -> Description: {r[2]}")
conn.close()

# 2. Update src/sql/nro_data.sql
with open('src/sql/nro_data.sql', 'r', encoding='utf-8') as f:
    sql_content = f.read()

sql_content = sql_content.replace(
    "INSERT INTO `item_template` VALUES (191, 27, 3, 'Cà chua', 'Biến 5% tấn công quái thành HP trong 10 phút'",
    "INSERT INTO `item_template` VALUES (191, 27, 3, 'Cà chua', 'Tác dụng trong 10 phút'"
)
sql_content = sql_content.replace(
    "INSERT INTO `item_template` VALUES (192, 27, 3, 'Cà rốt', 'Biến 5% tấn công quái thành KI trong 10 phút'",
    "INSERT INTO `item_template` VALUES (192, 27, 3, 'Cà rốt', 'Tác dụng trong 10 phút'"
)
sql_content = sql_content.replace(
    "INSERT INTO `item_template` VALUES (2047, 27, 3, 'Chuối', 'Tăng 20% tiềm năng sức mạnh khi đánh quái trong 10 phút'",
    "INSERT INTO `item_template` VALUES (2047, 27, 3, 'Chuối', 'Tác dụng trong 10 phút'"
)

with open('src/sql/nro_data.sql', 'w', encoding='utf-8') as f:
    f.write(sql_content)
print("Updated src/sql/nro_data.sql successfully!")

# 3. Update docker/initdb/02_nro_data.sql
with open('docker/initdb/02_nro_data.sql', 'r', encoding='utf-8') as f:
    d_content = f.read()

d_content = d_content.replace(
    "(191,27,3,'Cà chua','Biến 5% tấn công quái thành HP trong 10 phút'",
    "(191,27,3,'Cà chua','Tác dụng trong 10 phút'"
)
d_content = d_content.replace(
    "(192,27,3,'Cà rốt','Biến 5% tấn công quái thành KI trong 10 phút'",
    "(192,27,3,'Cà rốt','Tác dụng trong 10 phút'"
)
d_content = d_content.replace(
    "(2047,27,3,'Chuối','Tăng 20% tiềm năng sức mạnh khi đánh quái trong 10 phút'",
    "(2047,27,3,'Chuối','Tác dụng trong 10 phút'"
)

with open('docker/initdb/02_nro_data.sql', 'w', encoding='utf-8') as f:
    f.write(d_content)
print("Updated docker/initdb/02_nro_data.sql successfully!")
