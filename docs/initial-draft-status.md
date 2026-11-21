# Initial draft completion record: 20 November 2026

The initial compositions for all 35 projects are complete. The compiled manuscript
has 1,819 pages. The packages contain 1,505 instructional steps and 144 verified
IDE figures. This is the first complete draft for substantive editing; it has not
been assessed as ready for ordinary students or publication.

## Editable publication and companions

`main.tex` and `includes/` are the publication authority. The current include walk
contains 2,345 files and 796 assembly blocks checked against source spans exported
from the actual IntelliJ MARS highlighter. Prose, diagrams, tables, equations,
captions and screenshot annotations use native LaTeX. Markdown and JSON remain
exercise and delivery metadata; they do not regenerate the publication.

The selected fonts are Minion Pro for prose, Noto Sans Math for mathematics and
JetBrains Mono for code. The minted bridge uses the verified IntelliJ spans and
print-adjusted light-theme colors. Font binaries are not distributed.

Every chapter has a student starter and public checks, plus a separate private
reference, equivalent alternative, quiz keys and grading material. Behavioral
checks accept equivalent implementations within each published contract. Checks
for a specific teaching checkpoint are distinguished from free-writing grading.
The private initial-draft distribution index pins the actual final student archive
embedded in each instructor archive. Earlier archives and grading receipts are
preserved; the final binding receipt records any manifest-only correction.

All 144 IDE figure entries have actual automated light-theme captures and native
LaTeX annotations. Their checked state, screenshots, semantic descriptions and
provenance are retained. The capture implementation is a separate authoring plugin
that declares a dependency on MARS. It is not included in the simulator plugin.

## Validation and remaining human work

All 35 package manifests validate. The final manuscript passes veraPDF 1.30.4's
PDF/UA-2 + Tagged PDF validation profile: 1,728 passed rules, 8,960,291 passed checks,
and no failures. The current CBOR chapter also passes, with 237,581 passed checks.
The private verification record retains raw reports, exact PDF and validator
hashes, invocation details and verification of the official validator download.
See the [official validator documentation](https://docs.verapdf.org/cli/validation/)
for the automated validation model.

These automated results do not replace human accessibility review, screen-reader
walkthroughs, educational review or student testing. The author has reserved
substantive rewriting and editing for a separate goal. The length, pacing and
quality of the exposition require that review. No substantive rewrite is included
in this completion pass.

Remote lesson download, student progression tracking, instructor hint policies and
production guided-session delivery remain documented future product work in
[interactive-design.md](interactive-design.md). Guided and free-writing content,
quizzes, hints and package contracts are authored; server enforcement is not a
prerequisite for this book draft.

## Final project

Chapters 33 and 34 were completed before the final H.263 implementation. Chapter 35
uses actual H.263 with a documented restricted baseline profile. Its input is
Python-generated planar QCIF pixels, at no more than 30 pictures per second and
no more than ten seconds. The default is eight pictures at 5000/1001 pictures per
second (1.6016 seconds including the final hold). MSA kernels build on earlier
chapters. Actual MIPS-produced streams are independently decoded; the chapter is
an encoder exercise, not an invented teaching bitstream. The incremental evidence
and limitations are recorded in [h263-profile.md](h263-profile.md).

## Chapter inventory

Figures count annotated IDE figures; native conceptual diagrams and tables are
additional material.

| Chapter | Project | Steps | IDE figures |
| --- | --- | ---: | ---: |
| 1 | Calculate twice an integer plus one | 22 | 5 |
| 2 | Paint one cell and change a byte | 11 | 3 |
| 3 | Draw an eight-byte sprite and its mirror | 20 | 4 |
| 4 | Inspect one word at two widths | 16 | 4 |
| 5 | Count through hexadecimal glyphs on Digital Lab | 17 | 4 |
| 6 | Draw a square with coordinate feedback | 20 | 4 |
| 7 | Draw lines with an integer error accumulator | 24 | 4 |
| 8 | Evolve Life with a stable source generation | 28 | 4 |
| 9 | Visualize insertion and selection sort | 27 | 4 |
| 10 | Explore a maze with a breadth-first frontier | 30 | 4 |
| 11 | Transpose a matrix and measure its memory accesses | 28 | 4 |
| 12 | Move a ball with fixed-point coordinates | 30 | 4 |
| 13 | Parse a bounded binary packet | 24 | 3 |
| 14 | Queue variable-length records in a ring | 30 | 3 |
| 15 | Validate UTF-8 across input chunks | 34 | 4 |
| 16 | Clip and copy a transparent sprite | 25 | 4 |
| 17 | Evaluate integer expressions | 45 | 4 |
| 18 | Run a tiny bytecode interpreter | 51 | 4 |
| 19 | Convert RGB888 and RGB565 pixels | 49 | 4 |
| 20 | Mix signed PCM samples | 40 | 4 |
| 21 | Filter samples with Q15 coefficients | 47 | 5 |
| 22 | Multiply matrices in clipped tiles | 50 | 4 |
| 23 | Encode changes with variable-length integers | 60 | 4 |
| 24 | Check files with CRC-32 | 34 | 4 |
| 25 | Compute a SHA-256 fingerprint | 47 | 4 |
| 26 | Build a ChaCha20 stream cipher | 42 | 4 |
| 27 | Build RSA arithmetic | 42 | 4 |
| 28 | Encode and decode Base64 | 43 | 4 |
| 29 | Read and write a binary PPM image | 42 | 4 |
| 30 | Encode and decode QOI images | 65 | 4 |
| 31 | Filter a PCM WAV file | 64 | 4 |
| 32 | Serialize CBOR telemetry | 56 | 4 |
| 33 | Decode PNG images | 100 | 5 |
| 34 | Build a small lossless video codec | 122 | 6 |
| 35 | Encode a restricted H.263 stream | 120 | 6 |
