import subprocess, struct, os
from PIL import Image

out = subprocess.check_output(['mysql', '-u', 'root', '-e', 'SELECT id, image_id, layer, dx, dy FROM nro_data.bg_item_template ORDER BY id ASC;']).decode('utf-8')
lines = out.strip().split('\n')[1:]
bg_templates = {}
for line in lines:
    parts = line.split()
    bg_templates[int(parts[0])] = {
        'image_id': int(parts[1]),
        'layer': int(parts[2]),
        'dx': int(parts[3]),
        'dy': int(parts[4])
    }

with open('data/map/item_bg_map_data/7', 'rb') as f:
    d = f.read()

cnt = struct.unpack('>h', d[:2])[0]
print(f'Map 7 has {cnt} items:')
for i in range(cnt):
    it_id, x, y = struct.unpack('>hhh', d[2+i*6 : 2+(i+1)*6])
    tmpl = bg_templates.get(it_id, None)
    img_id = tmpl['image_id']
    img_p = f'data/item_bg_temp/x1/{img_id}.png'
    sz = Image.open(img_p).size if os.path.exists(img_p) else 'MISSING'
    layer = tmpl['layer']
    dx = tmpl['dx']
    dy = tmpl['dy']
    print(f'[{i:2d}] id={it_id:3d} (x={x:2d}, y={y:2d}) -> image_id={img_id:3d}, size={sz}, layer={layer}, dx={dx:3d}, dy={dy:3d}, px_x={x*24+dx:4d}, px_y={y*24+dy:4d}')
