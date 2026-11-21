# Revised decisions and repetition unit

The native sources are `revision/main/decisions-and-repetition.tex` and
`revision/labs/decisions-and-repetition.tex`. The combined main proof has 23 pages,
with chapter 4 starting on page 17; the lab proof has 15, with lab 4 starting on
page 13. The original manuscript remains preserved.

The chapter explains taken/not-taken paths, a two-way choice and shared join,
compact branches and forbidden slots, device-port interfaces, segment masks,
loop state, an invariant and bounded table access. Its complete range counter
uses the existing seven-segment project interface. The lab traces the first two
iterations, empty ranges, faults and an independent end-pointer version. The
fourth revised native interactive lesson and its new IDE figures remain unfinished.

Repeat the worked-code checks with JDK 25:

```sh
python3 tools/check_revision_branches.py \
  --java /absolute/path/to/jdk-25/bin/java \
  --cli /absolute/path/to/cli-1.0-SNAPSHOT.jar \
  --output /new/report/directory
```

The check covers all 153 valid start/count pairs with the standard glyph table,
plus 114 font/window combinations including repeated masks and the full byte
domain. It compares exact ordered byte-port writes, protected data, silence and
normal bounded exit. For the illustrated program it additionally checks one
bounded byte read per selected glyph, or no read for an empty range. Those stricter
read-shape assertions qualify the worked example; the student-facing grader does
not prescribe its registers, labels or exact instruction sequence.

Private verification runs the same 267 cases on an equivalent end-pointer
solution. Six assembled faulty variants are rejected. Direct probes confirm that
a control transfer after a conditional compact branch faults on the not-taken
path and is skipped on the taken path. Three flag values verify the literal
if/else fragment. Native stopped-session checks separately verify first/second
iteration registers, port history and compact branch outcomes. These are core
observations, not native IDE pause screenshots or repaint measurements.

The worked counter also passes the existing three public grading cases. The
associated student package validates and builds. No new grading primitive was
needed. Publication tooling passes 391 tests; the separate authoring highlighter
passes 101 and supplies exact spans for both new listings. Final native proofs
pass automated PDF/UA-2 checks and page/font review. Human accessibility, learner
and author walkthrough reviews remain open. The opening MARS-versus-hardware
explanation remains deferred.
