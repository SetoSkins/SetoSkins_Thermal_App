"""Kotlin 注释剥离器：按词法状态机识别注释，避免误伤字符串/字符字面量与 URL。

保留规则：文件开头（package 之前）连续的许可/版权注释块整体保留。
"""
import os
import re
import shutil
import sys


def strip_comments(src: str) -> str:
    out = []
    i = 0
    n = len(src)
    # 词法状态：normal / line_comment / block_comment / string / raw_string / char
    state = "normal"
    depth = 0  # 块注释嵌套深度（Kotlin 允许嵌套块注释）

    while i < n:
        ch = src[i]
        nxt = src[i + 1] if i + 1 < n else ""

        if state == "normal":
            if ch == "/" and nxt == "/":
                state = "line_comment"
                i += 2
                continue
            if ch == "/" and nxt == "*":
                state = "block_comment"
                depth = 1
                i += 2
                continue
            if src.startswith('"""', i):
                out.append('"""')
                state = "raw_string"
                i += 3
                continue
            if ch == '"':
                out.append(ch)
                state = "string"
                i += 1
                continue
            if ch == "'":
                out.append(ch)
                state = "char"
                i += 1
                continue
            out.append(ch)
            i += 1
            continue

        if state == "line_comment":
            if ch == "\n":
                out.append(ch)
                state = "normal"
            i += 1
            continue

        if state == "block_comment":
            if ch == "/" and nxt == "*":
                depth += 1
                i += 2
                continue
            if ch == "*" and nxt == "/":
                depth -= 1
                i += 2
                if depth == 0:
                    state = "normal"
                continue
            if ch == "\n":
                out.append(ch)  # 保留换行，避免行号剧烈变化
            i += 1
            continue

        if state == "raw_string":
            if src.startswith('"""', i):
                out.append('"""')
                state = "normal"
                i += 3
                continue
            out.append(ch)
            i += 1
            continue

        if state == "string":
            if ch == "\\" and nxt:
                out.append(ch)
                out.append(nxt)
                i += 2
                continue
            if ch == '"':
                out.append(ch)
                state = "normal"
                i += 1
                continue
            if ch == "\n":  # 容错：不合法字符串则退出
                state = "normal"
            out.append(ch)
            i += 1
            continue

        if state == "char":
            if ch == "\\" and nxt:
                out.append(ch)
                out.append(nxt)
                i += 2
                continue
            if ch == "'":
                out.append(ch)
                state = "normal"
                i += 1
                continue
            out.append(ch)
            i += 1
            continue

    return "".join(out)


def find_header_end(lines):
    """返回文件开头许可/版权块的结束行索引（含）。无版权头返回 -1。"""
    idx = 0
    # 跳过前导空行
    while idx < len(lines) and lines[idx].strip() == "":
        idx += 1
    start = idx
    saw_copyright = False
    last = -1
    while idx < len(lines):
        s = lines[idx].strip()
        if s.startswith("//"):
            if re.search(r"Copyright|SPDX-License-Identifier|Licen[cs]e", s, re.I):
                saw_copyright = True
            last = idx
            idx += 1
            continue
        break
    if not saw_copyright:
        return -1, -1
    return start, last


def process(path, dry_run=True):
    with open(path, encoding="utf-8") as fh:
        src = fh.read()

    lines = src.split("\n")
    hstart, hend = find_header_end(lines)

    if hstart >= 0:
        header = "\n".join(lines[hstart:hend + 1])
        body = "\n".join(lines[hend + 1:])
        stripped_body = strip_comments(body)
        new = "\n".join(lines[:hstart]) + ("\n" if hstart > 0 else "") + header + "\n" + stripped_body
        if not header.endswith("\n") and body.startswith("\n"):
            pass
    else:
        new = strip_comments(src)

    # 清理因删注释产生的多余空行
    new = re.sub(r"\n{3,}", "\n\n", new)
    # 删除行尾残留空白
    new = "\n".join(l.rstrip() for l in new.split("\n"))
    # 块注释消失后可能留下 "{\n\n\n" 或 "}\n\n\n" 之类的连续空行，再压一次
    new = re.sub(r"\n{3,}", "\n\n", new)
    # 去掉 "{" 之后紧跟的空行、以及 "}" 之后紧跟的多余空行
    new = re.sub(r"\{\n\s*\n", "{\n", new)
    new = re.sub(r"\n\s*\n(\s*\})", r"\n\1", new)
    new = re.sub(r"\n{3,}", "\n\n", new)
    if not new.endswith("\n"):
        new += "\n"

    if not dry_run:
        with open(path, "w", encoding="utf-8", newline="\n") as fh:
            fh.write(new)
    return len(src) - len(new)


if __name__ == "__main__":
    root = sys.argv[1]
    apply = "--apply" in sys.argv
    files = []
    for dirpath, _, names in os.walk(root):
        for nm in names:
            if nm.endswith(".kt"):
                files.append(os.path.join(dirpath, nm))
    total = 0
    for p in sorted(files):
        saved = process(p, dry_run=not apply)
        total += saved
        print(f"{'SAVED' if apply else 'DRY'} {saved:7d}  {os.path.relpath(p, root)}")
    print(f"\n{'Applied' if apply else 'Would remove'} {total} bytes across {len(files)} files")
