# Revised arrays and images unit

The native main chapter is `main/arrays-and-images.tex`; its written supplement is
`labs/arrays-and-images.tex`. The current combined revision proofs contain 36 main
pages and 23 lab pages. Chapter six begins on main page 31 and lab page 20. These
are growing revision proofs, not the completed curriculum.

The chapter builds a sixteen-by-eight framebuffer from eight packed source bytes.
It distinguishes source bits, byte-array elements, pixel words, horizontal offsets
and row stride. A complete row renderer and caller reuse the preceding chapter's
unchanged byte-reversal helper. A native one-row diagram explains why a horizontal
reflection changes source-bit selection while destination addresses still increase.
The worked source is `labs/files/sprite-rows.asm`; the printed listings and inherited
helper compose that exact file.

| Outcome | Written activity | Technical evidence |
| --- | --- | --- |
| Connect a packed bit to a pixel word | Derive the asymmetric 0x16 row and its reflection | Every low-byte value, varied upper bits and destination offsets |
| Calculate bounds from element sizes | Derive 4-byte steps, 64-byte stride and the final byte | Full-frame final state and relevant read/write histories |
| Coordinate caller and helper state | Distinguish the advanced argument from the saved row base | Stopped-session probes before and after calls and outer updates |
| Diagnose plausible-looking output | Try wrong stride, stale color and overlapping stores | Seven meaningful faulty variants are rejected |
| Implement a related transformation | Draw source rows in reverse vertical order | Public checks accept ordinary-call, inline and relocated implementations |

Repeat the worked-program qualification with JDK 25:

```sh
python3 tools/check_revision_arrays.py \
  --java /absolute/path/to/jdk-25/bin/java \
  --cli /absolute/path/to/cli-1.0-SNAPSHOT.jar \
  --output /new/report/directory
```

The literal program and a different branchless row renderer each pass 515 full
sprite fixtures and 768 direct row-helper calls. The author qualification checks
relevant source, framebuffer and mirrored-output access histories as well as final
state. It is not a whole-address-space audit. Its implementation-specific worked
program observations are not imposed on independent student implementations.

The separate independent starter is `labs/files/vertical-sprite.asm`; its failing
body is intentional. Public formative checks are `labs/checks/vertical-sprite.kts`.
They require the right-half vertical flip, unchanged left half/source/guards,
preserved saved registers and caller stack state, silent execution and normal
return. They accept another valid renderer and do not require a particular frame
layout, scratch register or private label. Instructor implementations remain
private. No new grader primitive was necessary.

The written lab uses two actual light-theme MARS Debug captures of the worked
program, stopped at the first completed row and at the completed image. Every one
of the 128 logical pixels matched independent RGB fixtures at both stops, including
native rendering, native tooltips and exact screen-center samples. The PNGs remain
unchanged; crops, labels and the midpoint divider are native LaTeX. Raw receipts
and exact plugin bindings are retained privately under arrays-chapter-01.

The three new code listings use actual IntelliJ highlighter exports, preserving
all 780 prior records. Public tooling passes 391 tests and the separate authoring
plugin passes 104 Jupiter tests. Existing sprite and clipped-blitter packages still
validate and build. Both final proofs pass automated PDF/UA-2 checks and page
inspection; representative diagrams and the capture figure are also checked in
grayscale. The original manuscript and unrelated MARS edits are preserved.

The sixth revised unit is not yet a native interactive lesson. These are ordinary
Run/Debug activities and automated publication captures, not representative learner
review. The clipped-blitter project remains a later companion extension; its existing
package is preserved. Integer algorithms are next in the agreed main-book sequence.
The proposed opening MARS-versus-hardware explanation remains deferred.
