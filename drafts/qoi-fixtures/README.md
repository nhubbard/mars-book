# QOI reading fixtures

Run `python3 fixture.py new-sprite.qoi` to write the known eight-by-eight sprite.
Use `--fixture trace` for the eight-pixel, thirty-eight-byte worked stream. The sprite
file is sixty-one bytes; its row decisions come from chapter three. Both retain RGBA.
`--format ppm` writes an RGB preview and discards alpha. Existing output is refused.

The helper writes fixed fixture streams, not arbitrary input images. It does not
implement the student assembly encoder or decoder. The native QOI reading is
`drafts/qoi.tex`. Cumulative programming steps and separate distributions remain pending.
