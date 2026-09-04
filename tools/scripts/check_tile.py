from PIL import Image

p = "C:/Users/PC/AppData/LocalLow/Team/Nro6Tab/Game1/x21" + chr(36) + "10"
im = Image.open(p).convert("RGBA")
pxs = [px for px in im.getdata() if px[3] > 50]
r = sum(px[0] for px in pxs) // len(pxs)
g = sum(px[1] for px in pxs) // len(pxs)
b = sum(px[2] for px in pxs) // len(pxs)
print("x21$10: size=", im.size, "avg rgb=", (r, g, b))
