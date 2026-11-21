# Revised integer algorithms unit

The native main chapter is `main/integer-algorithms.tex`; its written supplement is
`labs/integer-algorithms.tex`. The current proofs contain 44 main pages and 26 lab
pages. Chapter seven begins on main page 37 and lab page 24. These are growing
revision proofs, not the completed curriculum.

The chapter derives an integer raster from a nearest-cell contract on an eight-by-eight
framebuffer. It explains endpoint ordering, halfway choices, a signed error
invariant, frozen decisions, independent coordinate updates and inclusive endpoints.
A native LaTeX grid and six-row trace connect the geometry to actual machine state.
The four printed listings compose the exact `labs/files/integer-line.asm` program.
It uses compact branches, a bounded leaf-function interface and a retained return count.

| Outcome | Written activity | Technical evidence |
| --- | --- | --- |
| Specify a discrete result before implementing it | Predict shallow, steep, reversed, tie and point cases | Independent rational oracle over all 4,096 endpoint tuples |
| Explain an accumulator invariant | Account for horizontal and vertical contributions | Exact six pre-store states and return/caller boundaries |
| Preserve the inputs of a decision | Trace the second doubled error across the first update | Stopped-session snapshot probe and rejected refreshed-error fault |
| Preserve an existing image | Prefill complete words outside the selected cells | Full-word final checks, protected inputs and relevant store traces |
| Adapt an algorithm independently | Paint alternate canonical visits | Public checks accept incremental, rational and relocated solutions |

Repeat the worked-program qualification with JDK 25:

```sh
python3 tools/check_revision_lines.py \
  --java /absolute/path/to/jdk-25/bin/java \
  --cli /absolute/path/to/cli-1.0-SNAPSHOT.jar \
  --output /new/report/directory
```

The printed routine and a rational alternative each pass every endpoint tuple in
the declared coordinate domain, plus the complete default caller. Colors and old
framebuffer words vary deterministically; those dimensions are sampled, not exhaustive.
Seven meaningful faulty variants are rejected. Author qualification also checks
canonical word-store order for these two worked implementations. That stricter
observation is not imposed on independent student submissions and is not a
whole-address-space write audit.

The separate `labs/files/dashed-line.asm` starter intentionally fails at its unfinished
body. Its public formative checks are `labs/checks/dashed-line.kts`. They require
alternating canonical raster cells, unchanged skipped/off-line words and inputs,
a correct painted-cell count and preserved saved/caller state. They do not prescribe
scratch registers, private labels, exact instruction counts or a frame layout.
Reference implementations remain private. No new grader primitive was required.

Four fresh IntelliJ highlighter exports preserve all 783 previous records (787 total).
The separate authoring plugin passes 104 Jupiter tests; public tooling passes 391
tests. The existing 007 package still validates and builds, and its seven public
raster cases pass with the new worked program. Both proofs have passing automated
PDF/UA-2 validation and visual review, including the native diagram in grayscale.
Original manuscript and unrelated MARS edits remain preserved.

Two actual light-theme Debug observations verify the first plotted cell and completed
line against independent RGB fixtures. These are private technical evidence; the
published explanation uses the native conceptual diagram instead of another IDE
figure. The stopped-session probe also confirms that the label-addressed count
store expands to eight completed instructions on this target before the result
is retained and the caller reuses the result register.

The seventh revised unit is not yet a native interactive lesson. Neither the automated
IDE observations nor core checks establish human learner usability or assistive-technology
usability. Those reviews and the remaining curriculum remain open. State and buffers,
through Life and the ring-buffer companion, are next. The proposed opening
MARS-versus-hardware explanation remains deferred.
