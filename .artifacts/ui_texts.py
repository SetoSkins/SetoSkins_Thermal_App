import re
import sys

for path in sys.argv[1:]:
    s = open(path, encoding='utf-8').read()
    print('==', path)
    for m in re.finditer(r'text="([^"]*)"[^>]*bounds="(\[[^"]*\])"', s):
        if m.group(1):
            print(m.group(1), m.group(2))
