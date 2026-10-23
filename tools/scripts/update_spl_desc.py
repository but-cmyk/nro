import sys, io

sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

with open('docker/initdb/02_nro_data.sql', 'r', encoding='utf-8') as f:
    sql = f.read()

replacements = [
    # Cấp 1 (441 - 447)
    ("(441,30,3,'Sao pha lê','Ép vào đồ sao pha lê để tăng chỉ số',1,3887,-1,1,1,0,0,-1,-1,-1)",
     "(441,30,3,'Sao pha lê đỏ','Ép vào đồ sao pha lê để tăng 5% hút máu',1,3887,-1,1,1,0,0,-1,-1,-1)"),
    ("(442,30,3,'Sao pha lê','Ép vào đồ sao pha lê để tăng chỉ số',2,3888,-1,1,1,0,0,-1,-1,-1)",
     "(442,30,3,'Sao pha lê xanh dương','Ép vào đồ sao pha lê để tăng 5% hút KI',2,3888,-1,1,1,0,0,-1,-1,-1)"),
    ("(443,30,3,'Sao pha lê','Ép vào đồ sao pha lê để tăng chỉ số',3,3889,-1,1,1,0,0,-1,-1,-1)",
     "(443,30,3,'Sao pha lê đỏ hồng','Ép vào đồ sao pha lê để phản lại 5% sát thương nhận vào',3,3889,-1,1,1,0,0,-1,-1,-1)"),
    ("(444,30,3,'Sao pha lê','Ép vào đồ sao pha lê để tăng chỉ số',4,3890,-1,1,1,0,0,-1,-1,-1)",
     "(444,30,3,'Sao pha lê tím','Ép vào đồ sao pha lê để tăng 2% sát thương khi đánh quái',4,3890,-1,1,1,0,0,-1,-1,-1)"),
    ("(445,30,3,'Sao pha lê','Ép vào đồ sao pha lê để tăng chỉ số',5,3891,-1,1,1,0,0,-1,-1,-1)",
     "(445,30,3,'Sao pha lê cam','Ép vào đồ sao pha lê để tăng 5% may mắn nhặt vật phẩm từ quái',5,3891,-1,1,1,0,0,-1,-1,-1)"),
    ("(446,30,3,'Sao pha lê','Ép vào đồ sao pha lê để tăng chỉ số',6,3892,-1,1,1,0,0,-1,-1,-1)",
     "(446,30,3,'Sao pha lê vàng','Ép vào đồ sao pha lê để tăng 5% vàng từ quái',6,3892,-1,1,1,0,0,-1,-1,-1)"),
    ("(447,30,3,'Sao pha lê','Ép vào đồ sao pha lê để tăng chỉ số',7,3893,-1,1,1,0,0,-1,-1,-1)",
     "(447,30,3,'Sao pha lê xanh lá','Ép vào đồ sao pha lê để tăng 5% sức mạnh, tiềm năng',7,3893,-1,1,1,0,0,-1,-1,-1)"),
    # Cấp 2 (1352 - 1358)
    ("(1352,30,3,'Sao pha lê đỏ cấp 2','Ép vào đồ sao pha lê để tăng chỉ số',0,9878,-1,1,0,0,0,-1,-1,-1)",
     "(1352,30,3,'Sao pha lê đỏ cấp 2','Ép vào đồ sao pha lê để tăng 5% hút máu',0,9878,-1,1,0,0,0,-1,-1,-1)"),
    ("(1353,30,3,'Sao pha lê lam cấp 2','Ép vào đồ sao pha lê để tăng chỉ số',0,9879,-1,1,0,0,0,-1,-1,-1)",
     "(1353,30,3,'Sao pha lê lam cấp 2','Ép vào đồ sao pha lê để tăng 5% hút KI',0,9879,-1,1,0,0,0,-1,-1,-1)"),
    ("(1354,30,3,'Sao pha lê hồng cấp 2','Ép vào đồ sao pha lê để tăng chỉ số',0,9880,-1,1,0,0,0,-1,-1,-1)",
     "(1354,30,3,'Sao pha lê hồng cấp 2','Ép vào đồ sao pha lê để phản lại 5% sát thương nhận vào',0,9880,-1,1,0,0,0,-1,-1,-1)"),
    ("(1355,30,3,'Sao pha lê tím cấp 2','Ép vào đồ sao pha lê để tăng chỉ số',0,9881,-1,1,0,0,0,-1,-1,-1)",
     "(1355,30,3,'Sao pha lê tím cấp 2','Ép vào đồ sao pha lê để tăng 3% sát thương khi đánh quái',0,9881,-1,1,0,0,0,-1,-1,-1)"),
    ("(1356,30,3,'Sao pha lê cam cấp 2','Ép vào đồ sao pha lê để tăng chỉ số',0,9882,-1,1,0,0,0,-1,-1,-1)",
     "(1356,30,3,'Sao pha lê cam cấp 2','Ép vào đồ sao pha lê để tăng 6% may mắn nhặt vật phẩm từ quái',0,9882,-1,1,0,0,0,-1,-1,-1)"),
    ("(1357,30,3,'Sao pha lê vàng cấp 2','Ép vào đồ sao pha lê để tăng chỉ số',0,9883,-1,1,0,0,0,-1,-1,-1)",
     "(1357,30,3,'Sao pha lê vàng cấp 2','Ép vào đồ sao pha lê để tăng 5% vàng từ quái',0,9883,-1,1,0,0,0,-1,-1,-1)"),
    ("(1358,30,3,'Sao pha lê lục cấp 2','Ép vào đồ sao pha lê để tăng chỉ số',0,9884,-1,1,0,0,0,-1,-1,-1)",
     "(1358,30,3,'Sao pha lê lục cấp 2','Ép vào đồ sao pha lê để tăng 5% sức mạnh, tiềm năng',0,9884,-1,1,0,0,0,-1,-1,-1)")
]

count = 0
for old, new in replacements:
    if old in sql:
        sql = sql.replace(old, new)
        count += 1
    else:
        print(f"Warning: not found {old[:35]}...")

with open('docker/initdb/02_nro_data.sql', 'w', encoding='utf-8') as f:
    f.write(sql)

print(f"Successfully replaced {count}/14 entries in docker/initdb/02_nro_data.sql!")
