import re
import sys

for path in sys.argv[1:]:
    s = open(path, encoding='utf-8').read()
    print('==', path)
    for m in re.finditer(r'<node [^>]*>', s):
        tag = m.group(0)
        if 'clickable="true"' in tag:
            text = re.search(r'text="([^"]*)"', tag)
            bounds = re.search(r'bounds="([^"]*)"', tag)
            cls = re.search(r'class="([^"]*)"', tag)
            print((text.group(1) if text else ''), bounds.group(1) if bounds else '', cls.group(1) if cls else '')
