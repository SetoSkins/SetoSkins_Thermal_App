import re, math

paths = [
"M115.98,49.09h17.36c3.75,0,6.75-3,6.75-6.75s-3-6.75-6.75-6.75h-17.36c-3.75,0-6.75,3-6.75,6.75s3,6.75,6.75,6.75ZM115.98,23.36h34.73c3.75,0,6.75-3,6.75-6.75s-3-6.75-6.75-6.75h-34.73c-3.75,0-6.75,3-6.75,6.75s3,6.75,6.75,6.75ZM150.71,61.34h-34.73c-3.75,0-6.75,3-6.75,6.75s3,6.75,6.75,6.75h34.73c3.75,0,6.75-3,6.75-6.75s-3-6.75-6.75-6.75ZM75.5,181.26c-6,3.25-12.74,5.12-19.74,5.12h-1.25c-10.62-.37-20.61-4.62-28.23-12.12-7.62-7.38-12.13-17.39-12.62-27.98-.37-6.87,1-13.49,3.87-19.74,2.75-5.87,6.87-11.12,11.99-15.24l2.5-2V37.1c0-12.99,10.62-23.61,23.74-23.61s23.74,10.62,23.74,23.61v54.47s.87-1.12,1.37-1.62l9.12-9.12c.87-1,2-1.75,3.12-2.5v-41.22c0-20.49-16.74-37.1-37.23-37.1S18.54,16.61,18.54,37.1v65.96c-5.62,5-10.12,11.12-13.37,17.86C1.42,129.17-.33,137.91.05,147.03c.62,13.99,6.62,27.11,16.61,36.85,10,9.82,23.34,15.48,37.35,15.87h1.75c7.12,0,13.99-1.25,20.49-3.87,1.25-.5,2.37-1.25,3.5-1.75-2.25-4-3.75-8.37-4.37-12.87h.12Z",
"M62.39,114.3v-56.84c0-3.75-3-6.75-6.75-6.75s-6.75,3-6.75,6.75v56.84c-13.9,3.16-23.79,15.48-23.86,29.73,0,16.74,13.74,30.48,30.61,30.48s30.61-13.74,30.61-30.48c0-14.49-10.24-26.61-23.86-29.73ZM55.64,160.9c-9.37,0-16.99-7.62-16.99-16.99s7.62-16.99,16.99-16.99,16.99,7.62,16.99,16.99-7.62,16.99-16.99,16.99Z",
"M112.4,104.02c2.31,0,4.19,1.88,4.19,4.19v27.11c8.17,2.32,12.91,10.81,10.59,18.98-1.45,5.13-5.46,9.13-10.59,10.59v15.94c0,2.31-1.88,4.19-4.19,4.19s-4.19-1.88-4.19-4.19v-15.94c-8.16-2.32-12.9-10.81-10.59-18.98,1.45-5.13,5.46-9.13,10.59-10.59v-27.11c0-2.31,1.88-4.19,4.19-4.19ZM154.28,94.3c2.31,0,4.19,1.88,4.19,4.19h0s0,22.52,0,22.52c8.16,2.32,12.9,10.81,10.59,18.98-1.45,5.13-5.46,9.13-10.59,10.59v51.24c0,2.31-1.88,4.19-4.19,4.19s-4.19-1.88-4.19-4.19v-51.24c-8.16-2.32-12.9-10.81-10.59-18.98,1.45-5.13,5.46-9.13,10.59-10.59v-22.52c0-2.31,1.88-4.19,4.19-4.19ZM112.4,143.11c-3.86,0-6.98,3.13-6.98,6.98s3.13,6.98,6.98,6.98,6.98-3.13,6.98-6.98-3.13-6.98-6.98-6.98h0ZM154.28,128.81c-3.86,0-6.98,3.12-6.98,6.98,0,3.86,3.12,6.98,6.98,6.98h0c3.86,0,6.98-3.13,6.98-6.98,0-3.85-3.13-6.98-6.98-6.98Z",
]

SX, SY, TX, TY = 0.20, 0.20, 37.0, 33.0

def lex(d):
    return re.findall(r'[a-zA-Z]|[-+]?(?:\d+\.?\d*|\.\d+)(?:[eE][-+]?\d+)?', d)

NARGS = {'M':2,'L':2,'H':1,'V':1,'C':6,'S':4,'Q':4,'T':2,'A':7,'Z':0}

def to_abs(d):
    tk = lex(d)
    cur = [0.0,0.0]; sub=[0.0,0.0]
    prev = None
    last_cmd_curve = None
    last_ctrl = None
    out=[]  # (cmd, args) with cmd in uppercase absolute
    i=0
    while i < len(tk):
        t = tk[i]
        if re.match(r'[a-zA-Z]', t):
            c = t; i+=1
        else:
            c = prev
        U = c.upper()
        if U == 'Z':
            cur = sub[:]
            prev = c
            out.append(('Z',[]))
            last_cmd_curve = 'Z'
            continue
        n = NARGS[U]
        args = [float(tk[i+k]) for k in range(n)]
        i += n
        if U == 'M':
            if c=='M':
                cur = args[:]
            else:
                cur[0]+=args[0]; cur[1]+=args[1]
            sub = cur[:]
            out.append(('M', cur[:]))
            prev = 'L' if c=='M' else 'l'
            last_cmd_curve = 'M'
        elif U == 'L':
            if c=='L': cur = args[:]
            else: cur[0]+=args[0]; cur[1]+=args[1]
            out.append(('L', cur[:]))
            prev = c
            last_cmd_curve = 'L'
        elif U == 'H':
            if c=='H': cur[0]=args[0]
            else: cur[0]+=args[0]
            out.append(('L', cur[:]))
            prev = c
            last_cmd_curve = 'H'
        elif U == 'V':
            if c=='V': cur[1]=args[0]
            else: cur[1]+=args[0]
            out.append(('L', cur[:]))
            prev = c
            last_cmd_curve = 'V'
        elif U == 'C':
            if c=='C':
                x1,y1,x2,y2,x,y = args
            else:
                x1=cur[0]+args[0]; y1=cur[1]+args[1]
                x2=cur[0]+args[2]; y2=cur[1]+args[3]
                x=cur[0]+args[4]; y=cur[1]+args[5]
            out.append(('C',[x1,y1,x2,y2,x,y]))
            last_ctrl = (x2,y2)
            cur=[x,y]
            prev=c
            last_cmd_curve='C'
        elif U == 'S':
            if last_cmd_curve in ('C','S'):
                cx1 = 2*cur[0]-last_ctrl[0]; cy1 = 2*cur[1]-last_ctrl[1]
            else:
                cx1,cy1 = cur[0],cur[1]
            if c=='S':
                x2,y2,x,y = args
            else:
                x2=cur[0]+args[0]; y2=cur[1]+args[1]
                x=cur[0]+args[2]; y=cur[1]+args[3]
            out.append(('C',[cx1,cy1,x2,y2,x,y]))
            last_ctrl=(x2,y2)
            cur=[x,y]
            prev=c
            last_cmd_curve='S'
        else:
            raise Exception('unhandled cmd '+c)
    return out

def fmt(v):
    return ('%.4f' % v).rstrip('0').rstrip('.')

all_bounds = [1e9,1e9,-1e9,-1e9]
def transform_and_print(d):
    global all_bounds
    abs_cmds = to_abs(d)
    segs=[]
    for (cmd, args) in abs_cmds:
        if cmd=='Z':
            segs.append('Z')
            continue
        a2=[]
        for k,v in enumerate(args):
            if k%2==0:
                a2.append(SX*v+TX)
            else:
                a2.append(SY*v+TY)
        # track bounds
        for k,v in enumerate(a2):
            if k%2==0:
                all_bounds[0]=min(all_bounds[0],v); all_bounds[2]=max(all_bounds[2],v)
            else:
                all_bounds[1]=min(all_bounds[1],v); all_bounds[3]=max(all_bounds[3],v)
        nums = ','.join(fmt(v) for v in a2)
        segs.append(cmd+nums)
    return ''.join(segs)

for idx,d in enumerate(paths):
    print('PATH%d_BEGIN' % (idx+1))
    print(transform_and_print(d))
    print('PATH%d_END' % (idx+1))

print('BOUNDS x:[%s, %s] y:[%s, %s]' % (fmt(all_bounds[0]), fmt(all_bounds[2]), fmt(all_bounds[1]), fmt(all_bounds[3])))