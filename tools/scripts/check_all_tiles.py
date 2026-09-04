import os
from PIL import Image

rms_dir = 'C:/Users/PC/AppData/LocalLow/Team/Nro6Tab/Game1'
for f in sorted(os.listdir(rms_dir)):
    if '$10' in f:
        p = os.path.join(rms_dir, f)
        try:
            im = Image.open(p).convert('RGBA')
            pxs = [px for px in im.getdata() if px[3] > 50]
            if pxs:
                r = sum(px[0] for px in pxs) // len(pxs)
                g = sum(px[1] for px in pxs) // len(pxs)
                b = sum(px[2] for px in pxs) // len(pxs)
                print(f'{f}: size={im.size}, avg rgb=({r},{g},{b})')
        except Exception as e:
            pass
