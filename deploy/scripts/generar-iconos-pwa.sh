#!/usr/bin/env bash
# Regenera iconos PWA desde src/main/webapp/img/logo.png
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
export ROOT
LOGO="$ROOT/src/main/webapp/img/logo.png"
OUT="$ROOT/src/main/webapp/img/pwa"

if [[ ! -f "$LOGO" ]]; then
  echo "No se encontro $LOGO" >&2
  exit 1
fi

python3 - <<'PY'
from PIL import Image, ImageDraw

import os
ROOT = os.environ.get('ROOT')
LOGO = os.path.join(ROOT, 'src/main/webapp/img/logo.png')
OUT = os.path.join(ROOT, 'src/main/webapp/img/pwa')

BG_TOP = (59, 19, 107)
BG_BOTTOM = (91, 42, 134)
GOLD = (201, 162, 39)

def lerp(a, b, t):
    return tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(3))

def gradient_bg(size):
    img = Image.new('RGB', (size, size))
    px = img.load()
    for y in range(size):
        c = lerp(BG_TOP, BG_BOTTOM, y / max(size - 1, 1))
        for x in range(size):
            px[x, y] = c
    return img

def rounded_rect_mask(size, radius_ratio=0.18):
    mask = Image.new('L', (size, size), 0)
    d = ImageDraw.Draw(mask)
    r = int(size * radius_ratio)
    d.rounded_rectangle((0, 0, size - 1, size - 1), radius=r, fill=255)
    return mask

def fit_logo(logo, box):
    w, h = logo.size
    scale = min(box / w, box / h)
    return logo.resize((max(1, int(w * scale)), max(1, int(h * scale))), Image.Resampling.LANCZOS)

def compose_icon(size, logo_scale=0.72, maskable=False):
    draw = Image.new('RGBA', (size, size))
    draw.paste(gradient_bg(size))
    logo = Image.open(LOGO).convert('RGBA')
    box = int(size * (0.52 if maskable else logo_scale))
    fitted = fit_logo(logo, box)
    x = (size - fitted.width) // 2
    y = (size - fitted.height) // 2
    if not maskable:
        ring = ImageDraw.Draw(draw)
        pad = int(size * 0.06)
        ring.rounded_rectangle(
            (pad, pad, size - pad, size - pad),
            radius=int(size * 0.14),
            outline=GOLD + (180,),
            width=max(2, size // 64),
        )
    layer = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    layer.paste(fitted, (x, y), fitted)
    out = Image.alpha_composite(draw, layer)
    if not maskable:
        final = Image.new('RGBA', (size, size), (0, 0, 0, 0))
        final.paste(out, (0, 0), rounded_rect_mask(size))
        return final.convert('RGB')
    return out.convert('RGB')

os.makedirs(OUT, exist_ok=True)
for name, size, maskable in [
    ('icon-192.png', 192, False),
    ('icon-512.png', 512, False),
    ('icon-maskable-512.png', 512, True),
    ('apple-touch-icon.png', 180, False),
]:
    compose_icon(size, maskable=maskable).save(os.path.join(OUT, name), 'PNG', optimize=True)
compose_icon(32, logo_scale=0.68).save(os.path.join(OUT, 'favicon-32.png'), 'PNG')
print('Iconos PWA generados en', OUT)
PY
