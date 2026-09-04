import struct
from PIL import Image

templates = {}
with open('docker/initdb/03_nro_data.sql', 'rb') as f:
    for line in f:
        if b'INSERT INTO `bg_item_template` VALUES' in line:
            s = line.decode('latin1').split('VALUES')[1].strip().rstrip(';\r\n').strip('()')
            parts = [int(p.strip()) for p in s.split(',')]
            templates[parts[0]] = {
                'image_id': parts[1],
                'layer': parts[2],
                'dx': parts[3],
                'dy': parts[4]
            }

print(f'Templates count: {len(templates)}')
with open('data/map/item_bg_map_data/0', 'rb') as f:
    d = f.read()

cnt = struct.unpack('>h', d[:2])[0]
print(f'Map 0 has {cnt} items:')
for i in range(cnt):
    it_id, x, y = struct.unpack('>hhh', d[2+i*6 : 2+(i+1)*6])
    tpl = templates.get(it_id)
    if tpl:
        img_id = tpl['image_id']
        try:
            im = Image.open(f'data/item_bg_temp/x1/{img_id}.png')
            print(f'[{i:2d}] item_id={it_id:3d} (x={x:2d}, y={y:2d}) -> image_id={img_id:3d}, size={im.size}, layer={tpl["layer"]}, dx={tpl["dx"]}, dy={tpl["dy"]}')
        except Exception as e:
            print(f'[{i:2d}] item_id={it_id:3d} (x={x:2d}, y={y:2d}) -> image_id={img_id:3d}, FILE NOT FOUND!')
    else:
        print(f'[{i:2d}] item_id={it_id:3d} -> NO TEMPLATE!')
