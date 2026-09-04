import struct

with open('data/map/item_bg_map_data/0', 'rb') as f:
    data = f.read()

count = struct.unpack('>h', data[:2])[0]
print(f"Total bg items in map 0: {count}")
offset = 2
for i in range(min(count, 30)):
    item_id, x, y = struct.unpack('>hhh', data[offset:offset+6])
    offset += 6
    print(f"[{i}] id={item_id}, x={x}, y={y}")
