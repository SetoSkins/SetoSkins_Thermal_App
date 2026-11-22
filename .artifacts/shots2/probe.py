import sys
from PIL import Image

# usage: probe.py img1 img2 ...
# Reports: topmost red status-card pixel (content offset proxy) and whether the
# large white title glyph rows (y 320..410, x 60..500) are present.
for p in sys.argv[1:]:
    im = Image.open(p).convert("RGB")
    card_top = None
    for y in range(140, 1600):
        reds = 0
        for x in range(60, 1100, 6):
            r, g, b = im.getpixel((x, y))
            if r > 45 and r > g * 1.9 and r > b * 1.9:
                reds += 1
        if reds > 120:
            card_top = y
            break
    title = 0
    for y in range(300, 430, 2):
        for x in range(60, 520, 2):
            r, g, b = im.getpixel((x, y))
            if r > 210 and g > 210 and b > 210:
                title += 1
                break
    print(f"{p}: card_top={card_top} title_rows={title}")
