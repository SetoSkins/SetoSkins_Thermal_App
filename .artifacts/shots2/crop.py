import sys
from PIL import Image

# usage: crop.py in.png out.png y0 y1 [scale]
src, dst = sys.argv[1], sys.argv[2]
y0, y1 = int(sys.argv[3]), int(sys.argv[4])
scale = float(sys.argv[5]) if len(sys.argv) > 5 else 1.0
im = Image.open(src).convert("RGB")
crop = im.crop((0, y0, im.width, y1))
if scale != 1.0:
    crop = crop.resize((int(crop.width * scale), int(crop.height * scale)), Image.LANCZOS)
crop.save(dst)
print("saved", dst, crop.size)
