# PNG reading observations

Generate the fixed two-pixel, five-row file at a fresh destination:

```sh
python3 drafts/png-fixtures/fixture.py /tmp/first-image.png
```

Use `--part zlib`, `--part filtered`, `--part rgb`, or `--part words` to write the
corresponding intermediate bytes instead. `words` is a tightly packed **little-endian**
word image, not a 512-word-stride framebuffer dump. Expected numerical words are
independent of target byte order. Existing destinations are refused.

The default stored stream has 46 bytes, split into payloads of 1 and 45 bytes. The
115-byte PNG intentionally splits the two-byte zlib header across IDAT chunks. The
filtered image has 35 bytes; RGB has 30; packed words have 40. Rows use None, Up, Sub,
Average and Paeth, in that order. The helper's literal `TRACE_FILTERED` records the
worked observation separately from its general input generator.

Dimensions 1..512 are available through `--width` and `--height`. The `(2,5)` fixture
uses the reading's fixed samples; other dimensions use a deterministic sample formula.
`--compression stored`, `fixed`, or `auto` selects a zlib encoder strategy. `auto`
does **not** promise a dynamic Huffman block: block choice depends on input and zlib.
`--split` selects the first IDAT payload length; zero and end-of-stream are legal.

These are input-generation and reading fixtures. They are not the student assembly
starter, a decoder implementation, a completed grader, a performance measurement or
an IDE capture. The native entry is `drafts/png.tex`; chapter 33 is not integrated
into `main.tex` until its executable teaching package is verified.
