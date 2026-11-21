# Measuring a complete kernel

Chapter 13 starts the advanced reading sequence with original project
`022-blocked-matmul`, before introducing MSA. Its prerequisites are scalar functions,
full-width pointers and frames, nested-loop invariants, and Chapter 11's cache model.
The main chapter begins on page 79 of the 85-page combined main proof. The written
lab begins on page 77 of the 81-page lab proof. Native sources are
`main/measuring-programs.tex`, `labs/measuring-programs.tex` and
`figures/measurement/`.

The connected example derives a row/column dot product, establishes its exact signed
width, follows a register accumulator, then carries partial sums across clipped
reduction blocks. It separates arithmetic correctness from observed work. The
comparison explicitly counts frame/descriptor overhead, native word data notices
and completed instructions. Cold function, retained-cache second call, reset replay
and whole-launch intervals have distinct definitions. Cache warmth here concerns
one simulated execution session; it does not measure JVM startup or hardware cycles.

`labs/files/matrix-measure.asm` is the complete worked scalar comparison, with
ordinary and blocked entries. Its two-by-three and three-by-two default matrices
produce `[58,64,139,154]`. The blocked default preserves `[25,28,73,82]` before its
second reduction interval. Inputs are signed sixteen-bit values stored in words;
outputs are exact signed doublewords. A separate example stores positive 2^31 as
low word `80000000`, high word zero. A signed word multiply is valid for these
bounded inputs; word accumulation need not be.

The public tool `tools/measure_revision_matmul.py` records source, CLI and tool
hashes, raw grader output, all 168 measurement rows from 84 passing cases, and
three placements at equal 256-byte capacity. It checks products, inactive output,
stack restoration and exact traffic accounting for the supplied source. It is a
reference measurement tool, not a generic acceptance rubric for other schedules.
At shape 16,16,16 and tile edge four, the ordinary/blocked cold functions execute
37475/54984 instructions, 8744/12840 native data notices and 5388/1068 fully
associative misses. Retained-cache second calls save five misses each; reset
reproduces the cold observations. The chapter exposes the instruction/miss tradeoff
with an explicitly assumed weighted-cost model, not a hardware speed claim.

The independent task is `labs/files/matrix-vector.asm`, with nine public cases in
`labs/checks/matrix-vector.kts`. It transfers the reduction to a new interface,
including zero dimensions, arbitrary output, interior windows and a supplied return
continuation. Equivalent schedules are accepted. The starter deliberately fails.
Original project 022's three public suites remain usable with the worked matrix
source and other conforming implementations; no exact traffic count is required.

Technical verification retained privately covers four matrix implementations,
including an outer-product schedule, signed word multiplication and wide pointers.
Each passes 178 kernel, 48 caller, two reuse and 9890 private cases. The private
set includes every supported dimension triple under both entries with deterministic
inputs and additional width probes; it is not exhaustive over all input values.
Three matrix-vector schedules pass all nine public cases and 289 expanded shape
cases each; a wide-address variant passes the expanded set. Eight matrix faults
and four vector faults are rejected. Separate probes execute the exact partial-sum
and 2^31 examples printed in the text.

Three actual light-theme Debug captures verify both launch cache observations and
the doubleword Memory result. The cache view includes the method-word read, giving
8745/12841 notices at the return boundary. Native LaTeX supplies crops, boxes and
semantic descriptions; original PNGs and coordinate/hash receipts are retained.
Only accepted captures are published; a transient first cache-view diagnostic stays
private. The exact installed plugin/authoring archives are bound by eleven JAR
hashes. Each capture launch used the real `clean cleanSandbox runIde` tasks in the
separate authoring plugin. The completed dedicated IDE was stopped with SIGTERM;
this is not a native close/restart persistence test. The written manual Reset
activity is supported by the core reset experiment but was not exercised through
the GUI in this milestone.

The literal listing uses a new actual IntelliJ highlighter export, preserving all
805 prior records (806 total). The authoring suite passed 104 Jupiter tests and the
public tooling passed 391 tests. Final proofs receive automated PDF/UA-2 checks
and page/grayscale review. Human author, learner and assistive-technology review
remain open. This unit currently uses ordinary Run and Debug; its native guided
lesson and the rest of the instructional-system goal remain unfinished. Original
manuscript sources and unrelated MARS edits are preserved.
