# Measuring a complete kernel

Chapter 13 starts the advanced reading sequence with original project
`022-blocked-matmul`, before introducing MSA. Its prerequisites are scalar functions,
full-width pointers and frames, nested-loop invariants, and Chapter 11's cache model.
At the original chapter milestone, the main chapter began on page 79 of an
85-page proof and the lab on page 77 of an 81-page proof. Those totals describe
that historical milestone. The current integrated lab occupies pages 93–98 of
the 202-page supplement. Native sources are
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
remain open. That milestone used ordinary Run and Debug; its native integration follows
below. The rest of the instructional-system goal remains unfinished. Original
manuscript sources and unrelated MARS edits are preserved.


## Native interactive integration, 22 November 2026

The draft `lessons/022-measuring-programs` now connects nine inline predictions,
nineteen Debug boundaries, five complete worked sources/Runs and an independently
written matrix-vector reduction. The native LaTeX lab follows the actual student
sequence through register sums, wide stores, partial output, clipped bounds and
caller publication. It distinguishes Learn's next requested source highlight
from Debug's actual paused frame and preserves disposable fault experiments.

The native checker preserves the original input construction, full-width sums,
per-call output initialization and assembled caller access policy, including
unnamed padding. The unchanged standalone script retains nine checks. Native
data has ten repeated-call function cases, one complete-caller case and a
separate normal-exit check. Exact original-input comparison, accepted alternatives
and deliberate faults remain private finite qualification evidence, not a proof
of every matrix or implementation.

Two accepted light-theme IDE walkthroughs exercise the actual controls. The
starter fails and unsaved correct source passes. Six edits clear twelve execution
passes; a fresh process retains ten quiz attempts and re-earns all twelve passes,
with six source hashes unchanged. Both projects close normally. Eleven installed
JARs match pinned archives and neither accepted log has ERROR or SEVERE entries.
A partial earlier attempt awaited the wrong success text after the independent
check passed; it is retained as diagnostic evidence and was repeated from a
fresh project after the authoring correction.

The optional stopped-reset activity is now verified through the visible native
button in both accepted sessions. At matmul_call, one access/miss becomes zero;
at return, ordinary counters are 8744/5388 and blocked counters 12840/1068.
Guided launch observations remain 8745/5388 and 12841/1068; the later caller store
adds one access and one miss. This extends the earlier core-only reset proof.

The separate authoring plugin passes 123 Jupiter tests and all 481 public tooling
tests pass. The reused MARS checker has 217 CLI / 1212 plugin regression results
and required coverage gates, followed by an exact archive test. Both final
plugin archives pass API verification. No new production MARS code was needed
in this integration step, and all 41 manual MARS edits remain preserved.
The 202-page supplement passes 1728 PDF/UA-2 rules / 953365 checks with no failures.
Pages 93–98 and their neighbours were visually inspected, with both unit figures
also checked in grayscale. Established fonts and actual native annotated figures
remain intact. Human learner and assistive-technology reviews remain pending.
Later native units, companions and remaining system work stay open; the full goal
is not complete. The proposed opening explanation remains deferred.
