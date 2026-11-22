import sys
from PIL import Image

for name in sys.argv[1:]:
    im = Image.open(name).convert("RGB")
    w, h = im.size
    # left-most bright pixel per row, restricted to top area
    first_bright = {}
    for y in range(0, 1100, 2):
        for x in range(0, w):
            if max(im.getpixel((x, y))) > 180:
                first_bright[y] = x
                break
    xs = list(first_bright.values())
    xs.sort()
    print(name, "min first-bright x =", xs[:6], "median", xs[len(xs) // 2])
