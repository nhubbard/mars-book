# WAV pipeline unit

Chapter 23 revises original project `031-pcm-wav` as a connected main-book
explanation and written laboratory. Directly edited native sources are
`main/audio-files.tex` and `labs/audio-files.tex`, included by the revised
publication entries. The main proof has 154 pages, with this chapter on
146–154; the lab proof has 165 pages, with this lab on 162–165. The original
manuscript and earlier validated proofs remain preserved.

## Teaching sequence

The chapter distinguishes sample codes, channel frames, payload bytes and
complete file bytes before walking one 82-byte RIFF/WAVE image. Native tables
show descriptor widths, chunk boundaries and four stereo input/output frames.
The explanation derives odd-byte extent arithmetic, delayed descriptor
publication, per-channel FIR gathering and retained raw history, canonical
44-byte overhead, unsigned capacities, exact-EOF probing and file ownership.
It separates validation-before-truncation from a later write failure that may
leave partial output. Two short excerpts use actual IntelliJ syntax exports.

Four verified IntelliJ Light Debug observations show the descriptor, unchanged
record after a late error, filtered frames and encoded PCM with unchanged guard
bytes. PNGs retain native pixels; crops, outlines, captions and semantic
alternatives are editable native LaTeX. Capture receipts pin the actual
split-file caller and shared helper, installed MARS archive and separate
authoring driver. Before launch, actual clean cleanSandbox runIde tasks ran.
The owned IDE was closed after the completed capture batch.

The public worked caller includes the complete six-function implementation and
FIR helper from `labs/files/wav-functions.asm`. This is an intentional complete
main-book example. The independent `wav-reverse-frames.asm` reuses those helpers
and leaves only the new entry unfinished. Its caller filters, reverses whole
frames in place, then encodes and saves. The new function supports disjoint or
exactly equal PCM intervals, unaligned bytes, zero frames, full-width prefix
validation and saved machine state. Complete reversal implementations and
private cases remain in the instructor repository. Public behavioral feedback
does not require a particular loop or scratch-register arrangement.

## Finite verification

The worked program, equivalent implementation and wide-data implementation each
pass 813 original public/private cases: 2439 grading-case executions. Two
independent reversal implementations and a variant with both code and data above
32-bit addresses each pass 35 public and 181 expanded cases: 648 executions.
The unfinished independent entry fails all 35 public cases as intended. Counts
are case executions, not distinct source programs or exhaustive guarantees.

Fourteen assembled semantic faults are rejected against 157 passing matching
baseline cases. They cover narrowed frame counts, absent or wrapped padding,
descriptor writes on error, channel stride, history extent, missing rounding
bias, signed capacity comparisons, short-read handling, assumed descriptor
three, skipped pre-save validation, sample-wise reversal, unsafe aliasing and
PCM reads for zero frames. No MARS checking API change was required.

Four separate capture-boundary CLI probes verify exact expected memory bytes.
Fourteen ordinary host-file runs exercise the worked and equivalent pipelines
on the trace, generated two-second recording, empty recording, malformed late
padding and missing input, plus both independent reversal implementations on
the trace and two-second recording. Ten successful outputs agree byte-for-byte
with the independent Python arithmetic/container model and Python's WAV reader
and writer. Four failures preserve an existing output. No human listening or
real disk-write-failure test is claimed. Virtual-I/O grading separately covers
transfer and ownership behavior.

The four printed fixture/preparation/launch commands run in a fresh directory,
produce the exact output, retain instruction text and refuse replacement of
existing fixtures or prepared sources. Grading declares the include explicitly;
ordinary CLI and IDE use the neighboring physical helper. Both inputs and
helper must be retained when replaying a frozen submission.

The separate authoring plugin passes 104 Jupiter tests. Public publication and
package tooling passes 419 tests. Package validation and student export are
renewed. Two actual highlighter records append to all 827 earlier records.
Final proofs are visually reviewed, including grayscale code and figures, and
validated against PDF/UA-2 with zero automated failures. These checks do not
replace author, learner or assistive-technology review.

## Remaining work

Native interactive lessons for this advanced unit are not implemented by this
publication step. The older 64-step construction package remains available;
its historical capture plans do not become completed captures merely because
this revised chapter has new verified images. Instructor progression and hint
policies must be checked when advanced delivery is implemented. The full
curriculum goal remains active, with PNG, lossless-video and restricted actual
H.263 treatment and further student-system integration still outstanding.
All forty-one pre-existing MARS edits remain outside this work.
