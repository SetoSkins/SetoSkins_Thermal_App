import sys
from PIL import Image

def analyze(path):
    im = Image.open(path).convert("RGB")
    w, h = im.size
    title_rows = []
    card_top = None
    for y in range(140, 1000):
        white = False
        for x in range(50, 520):
            r,g,b = im.getpixel((x,y))
            if r>200 and g>200 and b>200:
                white = True
                break
        if white:
            title_rows.append(y)
        if card_top is None:
            reds = 0
            for x in range(60, 1100, 8):
                r,g,b = im.getpixel((x,y))
                if r>45 and r>g*1.8 and r>b*1.8:
                    reds += 1
            if reds > 90:
                card_top = y
    return (title_rows[0] if title_rows else None,
            title_rows[-1] if title_rows else None,
            card_top)

for p in sys.argv[1:]:
    t0,t1,c = analyze(p)
    print(f"{p}: title {t0}..{t1} card_top {c} gap {(c-t1) if c and t1 else None}")
