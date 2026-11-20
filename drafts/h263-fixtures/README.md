# Raw QCIF input for the H.263 chapter

Generate a new directory, without an image library or a host encoder:

```sh
python3 drafts/h263-fixtures/fixture.py --output-dir work/qcif
```

The default is eight pictures with one picture every six ticks of the
30000/1001 Hz baseline clock: 5000/1001 pictures/s, with a defined final picture
hold and total duration 1001/625 s (1.6016 s). `clip.yuv` is 304128 bytes.
`clip.json` records exact rational timing, plane geometry, whole-file and
per-picture hashes. Neither output is a MARS encoder result.

Each picture is a 38016-byte concatenation of these unsigned 8-bit planes:

| Plane | Byte offset | Width | Height | Byte stride |
|---|---:|---:|---:|---:|
| Y | 0 | 176 | 144 | 176 |
| Cb | 25344 | 88 | 72 | 88 |
| Cr | 31680 | 88 | 72 | 88 |

Rows run top to bottom; samples run left to right, with no padding or header.
Picture `f` starts at `f*38016`. The 4:2:0 chroma grid and 12:11 sample aspect
ratio belong to the selected baseline format; display scaling does not change
these bytes. Nominal Y range is 16..235; Cb/Cr range is 16..240, neutral 128.
The generated pattern stays inside these ranges. A comparison kernel still
needs to handle all byte values 0..255.

The luma background has stationary eight-sample tiles. A bright 32-by-32 square
moves in two-luma-sample increments, with corresponding Cb/Cr regions. Picture
six repeats picture five. This supplies texture, edges, moving chroma and an
unchanged picture while keeping input generation deterministic.

`--frames N --tick-step D` selects the count and cadence. Integers must satisfy
`1 <= N <= 299`, `1 <= D <= 255`, and `N*D*1001 <= 300000`. The last inequality
checks the complete duration against ten seconds. All accepted rates are below
30 pictures/s. This baseline fixture uses integer clock steps; it does not claim
to support every arbitrary frame rate below that ceiling. Invalid input is rejected
before the output directory is created, and an existing directory is refused.

For host inspection, provide the raw parameters explicitly:

```sh
ffplay -f rawvideo -pixel_format yuv420p -video_size 176x144 -framerate 5000/1001 work/qcif/clip.yuv
```

The chapter uses a restricted **actual H.263** elementary stream as its eventual
output. `headers.py stream.263` inspects only baseline picture headers. Its
success does not establish macroblock, motion, coefficient or reconstruction
conformance. An independent full decoder is required. Student encoding remains
under construction; no host-produced stream is a completed student solution.
