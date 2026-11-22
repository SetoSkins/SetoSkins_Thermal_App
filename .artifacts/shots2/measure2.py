from PIL import Image
import sys
for p in [".artifacts/shots2/os3_home.png",".artifacts/shots2/home_os4b.png",".artifacts/shots2/os4_fixed1.png"]:
    im = Image.open(p).convert("RGB")
    rows=[]
    for y in range(250, 470):
        white=0
        for x in range(50,520):
            r,g,b=im.getpixel((x,y))
            if r>200 and g>200 and b>200: white+=1
        rows.append((y,white))
    # contiguous large block
    ys=[y for y,c in rows if c>3]
    print(p, "first", ys[0] if ys else None, "last-before-gap")
    prev=None
    blocks=[]
    for y in ys:
        if prev is None or y-prev<=3: 
            if blocks and blocks[-1][1]==prev: blocks[-1][1]=y
            else: blocks.append([y,y])
        else:
            blocks.append([y,y])
        prev=y
    print("  blocks", blocks[:6])
