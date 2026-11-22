from PIL import Image
for p in [".artifacts/shots2/cmp_os3c.png",".artifacts/shots2/fresh_c.png",".artifacts/shots2/os4_fixed1.png"]:
    im=Image.open(p).convert("RGB")
    # find leftmost bright pixel in rows 330..400 (large title)
    best=9999; top=None
    for y in range(320,410):
        for x in range(0,700):
            r,g,b=im.getpixel((x,y))
            if r>200 and g>200 and b>200:
                if x<best: best=x
                if top is None: top=y
                break
    print(p,"title left x",best,"top",top)
