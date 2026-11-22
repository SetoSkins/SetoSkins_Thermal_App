from PIL import Image, ImageDraw
a=Image.open(".artifacts/shots2/cmp_os3c.png").convert("RGB").crop((0,120,1156,700))
b=Image.open(".artifacts/shots2/fix4.png").convert("RGB").crop((0,120,1156,700))
pad=16
out=Image.new("RGB",(a.width*2+pad, a.height+64),(20,20,20))
out.paste(a,(0,64)); out.paste(b,(a.width+pad,64))
d=ImageDraw.Draw(out)
d.text((16,20),"OS3",fill=(255,255,255))
d.text((a.width+pad+16,20),"OS4 (fixed)",fill=(255,255,255))
# guide lines at title top 323 and card top 469 (crop offset 120)
for y,color in [(323-120+64,(0,255,0)),(469-120+64,(255,180,0))]:
    d.line([(0,y),(out.width,y)],fill=color,width=2)
out.save(".artifacts/shots2/os3_vs_os4.png")
print(out.size)
