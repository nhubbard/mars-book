# File boundaries and binary images

Chapter 18 and its written lab revise original project `029-binary-ppm`.
The main proof has 118 pages, with this chapter on 112–118. The lab proof has 110 pages,
with this unit on 106–110. Both are directly edited
native LaTeX. The original manuscript remains preserved.

The main chapter connects a precise local P6 contract to bounded token scanning,
header/raster interpretation, deferred metadata publication, RGB words versus
little-endian bytes, educational file services, descriptors, partial transfers,
EOF probes, validation before truncation and complete host artifacts. Two native
concept tables explain the byte layout and pixel representations. The written
lab asks for predictions, observes return boundaries, diagnoses selected-extent
mistakes and finishes with a complementary-color serializer.

The supplied `labs/files/ppm-codec.asm` completes parse, decode, encode, load and
save. Its caller selects the individual operations or the complete pipeline.
Three ordinary/alternative/wide-address implementations each pass 104 original
public cases and 866 existing private completed-interface cases. These checks
include exact publication intervals, saved state, short transfers, failure
precedence and descriptor ownership. They do not establish every possible file.

The independent `labs/files/ppm-negative.asm` intentionally stops at an unfinished
`ppm_encode_negative`. The public `labs/checks/ppm-negative.kts` accepts equivalent
implementations through 167 behavioral cases. Direct serialization and reuse of
the supplied encoder both pass those cases and 4257 expanded cases each; a
wide-address direct implementation also passes the expanded set. That domain
covers all 4096 permitted dimension pairs, 160 selected dimension/offset cases
and a retry sequence. Its offsets are correlated rather than every independent
source/destination combination. Pixel data are deterministic varied values,
not exhaustive RGB combinations. The untouched starter fails all 167 cases.

`tools/run_revision_ppm.py` creates a new directory, generates raw input bytes,
executes the real MARS CLI and checks exact canonical output, including truncation
of a longer previous file. Six recorded successful runs include ordinary,
commented, maximum-size and both completed independent designs. FFmpeg/FFprobe
independently decode the canonical output and check its pixels and dimensions.
The comment-adjacent-magic input is accepted by the local contract but not by
that FFmpeg decoder; this limitation is explicit in the lab. Four actual failing
loads preserve an existing output file. These host checks and the native virtual
fixture checks have different, explicitly stated scopes.

Three actual light-theme Debug captures show the punctuation-fixture descriptor,
the twelve-byte 9-by-10 header and RGB samples, and the four decoded words after
the complete pipeline. The pipeline uses one deterministic virtual path with
seven-byte transfer limits and descriptor 19, verifying all thirteen operations,
exact output and closed descriptors. Actual host files are exercised separately
by the public runner. Native LaTeX supplies crops, boxes, titles and semantic
figure descriptions. Screenshot automation remains in the separate plugin.

Seven exact written probes pass. Fourteen deliberate semantic faults are rejected
by existing behavior suites. A fifteenth, ignoring a zero-progress write, is
rejected by four newly enabled checks. Those four cases pass for the complete
reference, alternative and wide-address implementations.

The missing grader facility was added upstream: `provideFiles` accepts an optional
`zeroWriteAfterBytes`, with per-descriptor accepted-byte accounting and reset on
reopen. Invalid requests, configured failures and file-size exhaustion retain
priority. The isolated implementation passed all 133 CLI Jupiter tests and was
committed independently before integrating only the tested changes into the
working checkout. Manual edits were preserved. The original CLI remains intact;
new zero-progress evidence identifies the separate newly built grader digest.

Two fresh actual IntelliJ highlight exports extend the prior 816 records without
altering them. The authoring suite passes 104 Jupiter tests; public tooling passes
391 tests. The original PPM package validates and exports. PDF font, rendering,
grayscale and final PDF/UA-2 results are retained in the private archive. No human
learner, instructor or assistive-technology validation is claimed. The PPM native
guided lesson and the broader curriculum/instructional-system goal remain open.

## Native grading foundation, 22 November 2026

The native PPM route is still being built. Its first implementation step preserves
all 166 direct complementary-encoder cases from the existing public Kotlin check:
160 dimension/capacity combinations and six invalid dimensions. An executable
export of the original Kotlin construction agrees with the new host author on
all dimensions, unsigned capacities, offsets, source bytes, complete framebuffer
initializations, status values and encoded results. This comparison is stronger
than comparing two descriptions of the same intended inputs.

`tools/ppm_negative_lesson_cases.py` authors 57 bounded function profiles containing
167 cases and 168 calls. The additional caller case observes the encoder return
before publication and then the status/count stores. The direct cases retain the
original seeded caller arenas, saved-register upper bits, workspace-relative stack,
invalid pointers on rejected input, exact result bytes and complete access-width
restrictions. Each original arena is watched separately so the private workspace
and unnamed alignment padding retain their original treatment.

The 160 direct grid cases and the caller retain their three-million-instruction
allowance; the six invalid-dimension cases retain their 100,000 allowance. This
requires MARS function-profile version 11. Older versions retain their existing
one-million limit, and each profile still has a ten-million combined limit. Cases
are partitioned into groups of at most three; the two 80-case width groups each
need 27 files and fit the existing 32-file assessment limit. This is a data
partition, not a requirement to give a student 57 separate activities.

The original script and the prepared native grader both accept the direct,
serializer-reusing and wide-address solutions. Both reject four retained faults
(source clobber, missing blue, signed capacity comparison and wrong width length)
and the unfinished starter. Seven host-author tests pass; the broader book tool
suite passes 520 tests. Private executable comparison and qualification evidence
is retained in `native-ppm-foundation-01` in the instructor repository.

This step does not claim a completed PPM native lesson, an imported student
archive or a new IDE walkthrough. The native caller checks cover return and
publication; delivery of the original three-million-budget normal-exit check,
the guided file-I/O observations, persistence, source-edit invalidation and fresh
light-theme IDE captures remains work for the complete unit. Existing verified
publication figures and native LaTeX prose have not been changed by this step.

The implementation regression passes 232 CLI and 1,245 plugin tests, both coverage
gates and the plugin build. A further focused Jupiter test reads all 57 exact
profiles through the production parser; every fixture member matches the public
author byte for byte. Both pinned plugins pass public API verification. The
41 manual MARS edits remain unchanged. These are grading-foundation checks, not
an IDE walkthrough or completion of the PPM native unit.

## Native PPM integration, 22 November 2026

The PPM unit now has its complete native route in `revision/lessons/029-binary-ppm`:
ten inline predictions, 23 ordered Debug observations across twelve complete
sources, three ordinary Runs and five independent assessments. Eleven complete
variations retain every worked function body. Their inputs select the extent,
header/raster, word/byte, capacity and real-file boundaries explained by the lab.
The first prediction includes an exercised wrong-answer recovery; students answer
in Learn beside the highlighted source without Find Action.

The native function groups retain all 167 cases / 168 calls qualified in the
foundation. Console-profile version 4 adds the caller's original complete silent
exit with its three-million allowance; older formats retain their prior ceiling.
The complete student archive passes the production importer in a focused Jupiter
test. The full implementation regression passes 232 CLI and 1,248 plugin tests,
both coverage gates and the plugin build. The exact-archive test is retained
separately from that full run. Book tooling passes 522 tests and the separate
publication plugin passes 130 Jupiter tests. Both exact plugins pass API checks
with 91 MARS / 20 authoring experimental usages and no internal API dependency.

Two actual light-theme IntelliJ processes exercised the route. Starter 0/80 and
correct unsaved serializer-reusing solution 80/80/6/1/1 were observed. The IDE's
real pipeline writes the exact 23 canonical bytes, and invalid source preserves
that existing output. Thirteen source edits clear twenty execution passes;
reopening retains eleven quiz attempts and all thirteen edited source hashes,
then renews the twenty checks. Assessment attempt counts advance from 2/1/1/1/1
to 3/2/2/2/2. Eleven installed JARs per process match the pinned archives; accepted
IDE logs contain no ERROR/SEVERE. Native project close and disposal precede
SIGTERM to the owned IDE process. The IDE child exits 143 and Gradle reports
failure after that deliberate cleanup; these are not natural runIde successes.

The newly composed native LaTeX companion explains observation order, the active
Debug frame versus the next source pointer, descriptor ownership, real-file
inputs and the limits of each checkpoint. It distinguishes ordinary host runs
from the existing seven-byte-transfer authoring figure. Three fresh standalone
host runs verify canonical output and truncation; FFmpeg independently decodes
exact pixels, including commented input and the complementary serializer. The
complete-exit check accepts three correct implementations and rejects an
unfinished routine, noisy exit and missing exit.

The revised lab proof has 209 pages. Chapter 18 occupies 128–134; those pages and
adjacent 127/135 were visually reviewed, and the three existing native-annotated
figures were reviewed in grayscale. PDF/UA-2 passes 1,728 rules / 982,205 checks
with zero failures. Minion Pro, Noto Sans Math and JetBrains Mono remain embedded.
The initial overlong path was corrected before the accepted proof. Public figure
pixels and native annotations are unchanged. All 41 manual MARS edits remain
unchanged. Private `native-ppm-unit-01` and `native-ppm-ide-01` retain the precise
source/build, artifact, GUI, progress and publication evidence. Human learner and
assistive-technology review remain open, as does the broader curriculum goal.
The next native unit is CRC state across bounded reads.
