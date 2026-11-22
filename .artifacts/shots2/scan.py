import sys
from PIL import Image

for name in sys.argv[1:]:
    im = Image.open(name).convert("RGB")
    w, h = im.size
    row = []
    for y in range(0, 1100):
        left = right = False
        for x in range(0, 480):
            if max(im.getpixel((x, y))) > 200:
                left = True
                break
        for x in range(w - 480, w):
            if max(im.getpixel((x, y))) > 200:
                right = True
                break
        if left or right:
            row.append(y)
    ys = [y for y in row if y > 60]
    print(name, "left-control white y-range:", (min(ys), max(ys)) if ys else None, "count", len(ys))
