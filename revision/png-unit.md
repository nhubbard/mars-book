# PNG decoding unit

Chapter 24 revises original project `033-png` as a connected main-book explanation
and written lab. Directly edited authority is `main/png-pipeline.tex` and
`labs/png-pipeline.tex`, included by the revised native entries. At the original PNG editorial milestone, the main proof
had 162 pages, with this chapter on 155–162; the lab had 170 pages, with this unit
on 166–170. Those are historical proof counts, not the expanded publication. Original manuscript and construction evidence remain preserved.

## Teaching sequence and independent work

The chapter follows one two-by-five image through container collection, one joined
zlib stream, exact inflation, row reconstruction and numerical framebuffer words.
It distinguishes five byte counts, derives the split-wrapper example, separates
bit fields from canonical code interpretation, explains self-overlapping matches,
and traces all five row predictors. Numerical colors, word byte order, stride,
active-row ownership and final publication have explicit meanings. The scalar/MSA
comparison is based on data dependencies and exact state before work measurements.
No historical execution timing is claimed as a new measurement.

The complete worked `labs/files/png-viewer.asm` retains independently callable
loader, collector, inflater, scalar/MSA reconstruction, packing and complete decode
entries. Source uses compact branch spellings and retains observation nops. Obsolete
full-line construction comments are removed with an explicit unchanged instruction/data
comparison and renewed full reference qualification. The original 100-step package and
unfinished full decoder remain available for the larger capstone assignment.

The new independent `png-filter-scanline.asm` emits one selector and RGB residuals
from original rows. Its settings are native full-width width, selector and unsigned
capacity; output is disjoint, input rows can coincide, null prior means top row,
and None/Sub never read the prior pointer. The written lab derives Average precision,
Paeth ties, modulo arithmetic, forward/backward traversal and an inverse proof.
Complete forward/backward implementations stay private. Public feedback compares
bytes, full saved state, exact accessed intervals and inverse reconstruction.

`png-from-rows.py` provides host compression and container envelopes without solving
the student's predictor. Simulator-produced five-row output is wrapped under stored,
fixed and automatic compression, independently read and decoded again by MARS.
The helper rejects bad dimensions, extents and selectors before creating output and
refuses existing output. The printed fixture and ordinary unmodified caller commands
are executed separately in a fresh directory and produce the expected console line.

Four new actual IntelliJ Light Debug images show inflated residuals, reconstructed
samples, packed words and published dimensions/count. Raw PNGs and receipts are retained;
crops, outlines, captions and semantic alternatives remain native editable LaTeX.
Both launches ran actual clean cleanSandbox runIde tasks. The initial final-view
expectation assumed an unaligned Memory row; the retained diagnostic establishes the
16-byte row layout, and the corrected complete four-image batch passes. The owned IDE
is closed. The capture fixture changes the path declaration to its deterministic input;
its instruction/data comparison records the remaining comment-only difference from
the final public caller. Screenshot automation remains in the separate authoring plugin.

## Finite qualification

Reference, equivalent and wide-code/data decoders each pass 452 public and 1944
private cases: 7188 positive grading-case executions. Test buffers occupy their
own mapped wide window; wide caller globals are placed at 0x1003f0000, disjoint
from them, with code at 0x100400000. The first attempted second wide window was
unmapped and is retained as setup diagnostic, excluded from semantic qualification.
The private suites use their declared individual harnesses; the initial wrong
shared harness name is likewise excluded.

Forward, backward and wide-code/data filter implementations each pass 266 public
and 880 expanded cases: 3438 executions. The unfinished entry fails all 266 public
cases as intended. Three additional five-row calls produce exact independently
generated filtered bytes. These are finite case executions, not exhaustive proofs
or a count of individual machine calls.

Eight decoder faults are rejected by suites with 414 unique passing reference
baseline cases. They cover ignored chunk CRC, the reserved type bit, exact inflated
count, match source progression, complete selector prevalidation, same-channel left
neighbors, native vector stores and failure publication. Six forward-filter faults
are also rejected by the passing 266-case public suite: narrowed width, wrong channel
stride, signed neighbors, byte-wrapped Average, signed capacity and wrong Paeth tie.
No new MARS checker API is needed.

Thirty-two real-file runs cover reference/alternative and scalar/MSA combinations
on three forward-filtered trace encodings, two seventeen-by-five images, the full
512-by-512 generated repeated-row image, a checksummed bad final filter and a missing
input. All 24 successes agree with Pillow 13.0.0 in every RGB sample and preserve
row padding; all eight failures retain framebuffer and context results. These
private ordinary-syscall host fixtures additionally zero the framebuffer before
invoking the unchanged decoder bodies so native memory export can inspect initialized
words on failure. Metadata and pixels are exported separately. The initial dump
through an uninitialized gap is retained as an export diagnostic, not a decoder fault.
The separately verified printed ordinary caller has no added initialization.

The authoring plugin passes 104 Jupiter tests; public publication/package tooling
passes 419 tests. The student package validates and builds. Two actual IntelliJ
highlighter exports append to all 829 earlier records, giving 831. All thirteen
new proof pages are visually reviewed, including grayscale code and figures. Both
final exact PDFs pass automated PDF/UA-2 validation with zero failures. Author,
learner and assistive-technology review remain separate open work.

## Remaining work

The original publication milestone did not implement a native interactive lesson
for the advanced PNG unit. The assessment foundation below prepares that delivery;
student activities and the IDE walkthrough are still to follow. Original construction material remains separate supporting detail, rather
than being expanded into a hundred sections in the main book. Advanced lesson
integration, author/learner review, lossless video and restricted interoperable
H.263 treatment remain outstanding. The full curriculum goal is active; all
forty-one pre-existing MARS edits are preserved.

## Native assessment foundation (23 November 2026)

`tools/png_filter_lesson_cases.py` models all 266 original public independent
forward-filter cases as six native profiles. They preserve both native settings
byte orders (133 cases each), all five predictors, unaligned pointers, boundary
widths, full-width invalid values, unsigned capacity, coincident read-only inputs,
explicit Paeth ties, and two six-call cases sharing one simulator each. Every
original case retains its 100,000-instruction allowance. There are 276 observed
calls; batching never divides a reused session into fresh machines.

Native format 21 adds an explicit per-case assembly byte order. The plugin uses
its existing build service and cache to assemble each requested order from the
current source, including unsaved edits. Initialized native constants therefore
have the correct representation; changing memory interpretation after assembling
would not suffice. The cache distinguishes byte orders. Ordinary Run/Debug
settings and existing lesson identity strings remain unchanged. The original
prepared-check signature and trailing cancellation lambda remain available.

The model compares the complete selector and residual byte string and protects
all six caller objects. All three setup objects are replaced before each call,
while the output and private machine state remain live between calls. Among
caller objects, output is writable but not readable; only the first 24 settings
bytes and the selected original input slices are readable. Private workspace and
gaps remain available. An invalid setting permits no RGB/output access. Neither
None nor Sub can dereference the deliberately invalid previous-row pointer.

An independent inverse recurrence over every expected valid result recovers the
original RGB bytes in the host checks. Native equality with those complete bytes
therefore also establishes that inverse result; no checksum substitutes for the
original byte assertions. The original public checker is executed separately
with its runtime inverse assertion intact. Its 266 cases and 276 calls agree with
the model on every argument, result, and before/after snapshot of all six caller
objects for reference, alternative and wide-address implementations.

Four model tests check the full grid, byte order, access permissions, Paeth tie
values, non-wrapping Average, exact inverse results, profile bounds and unchanged
checker binding. The exact six-profile archive is also a Jupiter parser fixture.
Generic grader tests cover separately assembled native constants, missing/wrong
images, detached variant maps, reused calls, current unsaved source, distinct
cached images and permission withdrawal before execution. Private executable
qualification and final regression reports are retained with
`native-png-foundation-01`; they are separate from the original decoder suites.

This foundation does not yet compose the PNG student activities, launch their
native lesson in the IDE or change the LaTeX publication. The independent task
remains the forward scanline filter; the complete decoder remains the worked
example. The original hundred-step decoder construction task is preserved.

The final foundation regression passes 287 CLI tests and 1,315 plugin tests,
with no failures, errors or skips. Both changed plugin packages (learning and
build) retain 100 percent line and branch coverage; generated-parser coverage
also passes. Public tooling passes 594 tests, and the original 100-step PNG
student package validates and rebuilds. Four correct native implementations each
pass all 266 cases, including private-gap and wide-address variants. All six
existing filter faults and the unfinished starter are rejected: 2,926 native
case executions across eleven sources. All 41 pre-existing manual edits remain
unchanged. These results complete the assessment foundation, not the later
student activity composition or IDE walkthrough.

## Native student-unit draft (23 November 2026)

`revision/lessons/033-png` now contains twelve inline predictions, six Debug
activities with eighteen ordered stops, one ordinary Run and seven native
assessment stages. The worked sequence begins with the exact embedded trace and
unchanged supplied decoder helpers. Its smaller caller uses fixed scratch buffers,
stride seventeen and a 280-byte framebuffer extent, so required observations do
not depend on host input or a path edit. Other experiments separate low-bit-first
fields from developed Huffman codes, distance-one self-overlap, wide Average and
Paeth tie arithmetic, sixteen-pixel packing and its tail, and native full-width
settings. None implements the independent forward-filter entry.

The first execution probe caught an incorrect reconstruction return expectation.
The function returns the 35-byte row-buffer extent, including five retained
selectors; that buffer contains thirty reconstructed RGB sample bytes. The
corrected observation explicitly distinguishes these counts. The initial failed
receipt remains private. All eighteen corrected stop snapshots and the subsequent
normal silent exits now pass core execution. They do not establish actual IDE
debugger behavior, which is the next delivery step.

Five unit tests preserve the actual decoder function bodies and format constants,
exact 115-byte fixture, byte-identical independent starter, all staged profiles,
and complete deterministic twenty-file export. They also execute the distributed
fixture and wrapper tools in fresh directories: all three compression choices
produce exact expected filtered rows and valid chunk checksums; repeated output
paths are refused. The standalone Python tools support the additional artifact
experiment without implementing the student's filter.

The first five assessment stages each preserve 44 original cases for one filter.
A sixth preserves all 46 boundary/reuse/tie cases, and a final stage rechecks the
same source against all 266. Required checks use the source-bound grading system
and both assembly byte orders. The assignment tells students to renew earlier
evidence when edits make it stale. Guided/free-writing availability and graduated
hints follow instructor policies.

Build the student archive with:

```sh
python3 tools/native_lesson.py revision/lessons/033-png \
  --output build/native-lessons/033-png-0.1.0-student.zip
```

The next milestone must exercise actual first use and fresh-process resume in
the light-theme IDE through the separate authoring plugin, then connect that
verified experience to the directly edited LaTeX lab. This student-unit draft
does not change the accepted publication PDF or claim new screenshots or
accessibility validation. Original decoder construction materials remain separate.

The final twenty-file archive passes two Jupiter tests through the real MARS
package importer and native PSI anchor resolution. These retain all seven stages,
266 final cases in both byte orders, all thirty prediction/Debug source references,
and full 128-bit vector expectations. The exact exported starter passes all 266
native cases with each of three existing instructor bodies, including wide
addresses; its unfinished entry fails all 266: 1,064 case executions. The latest
public tooling suite passes 599 tests. The production plugin is unchanged in this
content/test-only step; the preceding foundation retains the broad 287/1,315
regression and coverage evidence. All 41 manual edits remain preserved.


## Native IDE and written companion, 23 November 2026

The next delivery milestone is now verified in two actual light-theme IntelliJ
processes against pinned plugin binaries. First use records 38 observations;
fresh-process resume records 26. Twelve inline predictions retain thirteen
attempts, including the deliberately wrong first answer. Six ordinary Debug
activities reach eighteen checked boundaries, one silent Run exits normally,
and seven assessments pass 532 case placements in each process. The first None
assessment rejects all 44 unfinished cases before the private authoring fixture
is inserted into the independent editor buffer. The successful check uses that
unsaved buffer. No fixture or private implementation enters the student archive.

After harmless edits make the evidence stale, saved answers remain identical,
all fourteen execution checks are renewed, and seven edited source hashes survive
the process boundary. Both accepted logs contain no ERROR or SEVERE entries.
The native UI uses ordinary source pointers and debugger breakpoints; its next
source pointer is not proof that the simulator has executed that instruction.
Capture automation remains in the separate plugin.

The native LaTeX companion now names the interactive files, connects each small
experiment to its decoder boundary, and keeps the optional standalone viewer
and original construction capstone explicit. One actual public worked-source
photograph supplies three unchanged native crops: the paused frame, full vector
and four numerical pixel lanes. Its caption explains low-lane ordering. Source,
pixel digest, theme, geometry and pre-assessment placement are checked before
publication; six rejection tests preserve that boundary.

The accepted lab proof has 223 pages. PNG occupies pages 202–208 and its new
figure is on page 205. Ten changed or adjacent pages receive visual review,
including a grayscale figure review. The final capture replacement yields
identical reviewed page renders. The exact pinned PDF passes automated PDF/UA-2
validation: 1,728 rules and 1,049,342 checks, zero failures. Minion Pro, Noto Sans
Math and JetBrains Mono remain embedded. Public tooling passes 599 tests; the
separate authoring plugin passes 141 Jupiter tests. Human author, learner and
assistive-technology review remain pending.

The IDE trial exposed an upstream Build-view threading race. MARS now sends
build UI events on the event thread and allows these UI-only handoffs during
modal interactions, preserving cancellation cleanup. The 62 focused tests
cover both the original build cases and modal lesson Run paths. The final broad regression passes all 1,319 plugin tests and the required
coverage/parser gates. Learning and build packages retain 100% line and branch
coverage. The CLI task reuses its successful 287-test result; no new CLI
execution is claimed. Exact XML and reports are retained in private
native-png-ide-01. The broader curriculum goal remains
active, with retained lossless-video and H.263 instructional delivery next.
