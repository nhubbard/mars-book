#!/usr/bin/env python3
"""Render numbered callouts around an actual PNG capture using its verified receipt."""
import argparse
import base64
import hashlib
import html
import json
from pathlib import Path
import struct


def validate_capture(png, receipt):
    raw = png.read_bytes()
    if raw[:8] != b'\x89PNG\r\n\x1a\n' or raw[12:16] != b'IHDR':
        raise ValueError('Expected a captured PNG')
    width, height = struct.unpack('>II', raw[16:24])
    if width < 1 or height < 1:
        raise ValueError('Invalid capture dimensions')
    if receipt['theme'] != 'IntelliJ Light' or receipt['assertionsPassed'] is not True:
        raise ValueError('Capture must attest to light theme and checked state')
    if receipt['pngSha256'] != hashlib.sha256(raw).hexdigest():
        raise ValueError('Receipt does not match the screenshot')
    if receipt['viewport'] != {'width': width, 'height': height}:
        raise ValueError('Receipt dimensions do not match the screenshot')
    for callout in receipt['callouts']:
        box = callout['box']
        values = [box[key] for key in ('x', 'y', 'width', 'height')]
        if any(type(value) is not int for value in values):
            raise ValueError('Capture coordinates must be integer pixels')
        x, y, w, h = values
        if min(x, y) < 0 or min(w, h) <= 0 or x + w > width or y + h > height:
            raise ValueError('Callout is outside the screenshot')
        if not isinstance(callout['label'], str) or not callout['label'].strip():
            raise ValueError('A callout needs a description')
    return raw, width, height


def render(png, receipt, output):
    raw, width, height = validate_capture(png, receipt)
    labels = []
    boxes = []
    for index, callout in enumerate(receipt['callouts'], 1):
        box = callout['box']
        x, y, w, h = [box[key] for key in ('x', 'y', 'width', 'height')]
        boxes.append(f'<rect x="{x}" y="{y}" width="{w}" height="{h}" fill="none" stroke="#004b87" stroke-width="3"/>')
        cy = max(12, y)
        boxes.append(f'<circle cx="{x + 12}" cy="{cy}" r="12" fill="#004b87"/><text x="{x + 12}" y="{cy + 5}" text-anchor="middle" fill="white" font-size="15">{index}</text>')
        # Wrap long labels into separate lines in a dedicated white legend margin.
        import textwrap
        lines = textwrap.wrap(f'{index}. {callout["label"]}', width=max(20, width // 10))
        labels.extend(lines)
    legend_height = 28 * len(labels) + 30
    texts = [f'<text x="16" y="{height + 28 + i * 28}" font-size="20" fill="#111">{html.escape(line)}</text>' for i, line in enumerate(labels)]
    encoded = base64.b64encode(raw).decode()
    title = html.escape(receipt['figure'])
    svg = f'<svg xmlns="http://www.w3.org/2000/svg" width="{width}" height="{height+legend_height}" viewBox="0 0 {width} {height+legend_height}"><title>{title}</title><rect width="100%" height="100%" fill="white"/><image width="{width}" height="{height}" href="data:image/png;base64,{encoded}"/>{"".join(boxes)}{"".join(texts)}</svg>\n'
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text(svg)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('png', type=Path)
    parser.add_argument('receipt', type=Path)
    parser.add_argument('output', type=Path)
    args = parser.parse_args()
    render(args.png, json.loads(args.receipt.read_text()), args.output)


if __name__ == '__main__':
    main()
