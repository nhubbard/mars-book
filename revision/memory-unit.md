# Revised addresses and memory unit

The directly edited publication sources are
`revision/main/addresses-and-memory.tex` and
`revision/labs/addresses-and-memory.tex`. At this unit’s first publication, the main proof had 16 pages and the lab had 12.
Later chapters extend those proofs; chapter 3 still begins on main page 12 and
lab 3 on lab page 10. The original manuscript remains preserved.

The chapter develops addresses, labels, effective byte addresses, word/byte access
width, little-endian layout and signed/unsigned loads through one complete pixel
program. The written supplement connects predictions to memory boundaries, old
register snapshots, display observations, wrong offsets, wrong widths, guards
and independent component/copy variations. It explains that this Release 6 target
supports unaligned ordinary word accesses; an incorrect byte offset need not trap.

`revision/labs/files/memory-pixel.asm` is an exact copy of the native listing. Use
MIPS64 Release 6 and the Default memory layout. The bitmap header maps static data
to four logical cells. The example changes the first word and preserves the next
word; its final eight reserved bytes supply the remaining display-cell storage.
The RGB display ignores the high byte rather than treating it as alpha.

Repeat the executable verification with JDK 25:

```sh
python3 tools/check_revision_memory.py \
  --java /absolute/path/to/jdk-25/bin/java \
  --cli /absolute/path/to/cli-1.0-SNAPSHOT.jar \
  --output /new/report/directory
```

The 272 runs cover the literal listing, four independently terminated prefixes,
all 256 replacement-byte values, four alternate guard patterns, one wider source
whose low byte is stored, and six deliberately faulty variants. Checks compare
the complete declared data image, the dump's zero tail, full t0..t7 values and
normal silent exit. The existing public pixel grader also accepts the complete
example in its two interface/value and caller-neighbor cases. No new checking
primitive was needed.

These are core/CLI observations. Terminated prefixes do not establish a native
IDE pause or a complete memory-write history. The third revised interactive
lesson, its quizzes/checkpoints and new annotated IDE figures remain unfinished.
The supplement is currently usable with ordinary Run and Debug. Existing
002-single-pixel materials remain available with their historical assignment.

The exact new listing uses IntelliJ-exported highlighting from the separate
101-test authoring plugin. All earlier snippet records are preserved. The 391
publication-tooling tests pass, and the associated student package validates and
builds. Final PDFs retain the chosen embedded fonts and pass automated PDF/UA-2
checks. Private records retain execution inputs, outputs, exact source snapshots,
color/grayscale review and accessibility bindings. Human editorial, learner and
accessibility review remain open. The proposed opening explanation of MARS versus
hardware remains deferred.
