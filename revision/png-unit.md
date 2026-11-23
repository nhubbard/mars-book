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
