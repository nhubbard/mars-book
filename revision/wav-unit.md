# WAV pipeline unit

Chapter 23 revises original project `031-pcm-wav` as a connected main-book
explanation and written laboratory. Directly edited native sources are
`main/audio-files.tex` and `labs/audio-files.tex`, included by the revised
publication entries. At the original WAV editorial milestone, the main proof
had 154 pages, with this chapter on 146–154; the lab proof had 165 pages,
with this lab on 162–165. Those page counts describe that historical proof,
not the subsequently expanded publication. The original
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
PCM reads for zero frames. That original publication verification required no
MARS checking API change; the later native assessment foundation below adds
the capabilities needed for delivery through the student plugin.

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

The original publication step did not implement native interactive delivery
for this advanced unit. The later student-unit draft described below now supplies
its content and package validation; its IDE walkthrough remains pending. The older 64-step construction package remains available;
its historical capture plans do not become completed captures merely because
this revised chapter has new verified images. Instructor progression and hint
policies must be checked when advanced delivery is implemented. The full
curriculum goal remains active, with PNG, lossless-video and restricted actual
H.263 treatment and further student-system integration still outstanding.
All forty-one pre-existing MARS edits remain outside this work.

## Native assessment foundation (23 November 2026)

The public `tools/wav_reverse_lesson_cases.py` now expresses all 35 existing
public reversal cases as seven native function-assessment profiles. It retains
the original case budgets, 39 observed reversal invocations, the separate full
pipeline invocation, six calls sharing one machine, unaligned byte ranges,
exact aliasing, full-width invalid counts and the maximum 65,536-frame stereo
buffer. The unfinished student entry remains the same task: reverse whole PCM
frames using the supplied six-function worked pipeline.

Lossless repeated-byte fixtures keep the largest profile within the existing
serialized size limit. Checks still compare every expected byte; expansion
counts against the existing decoded memory bound. Native format 20 adds
bounded virtual input and output files, short reads and writes, injected
transfer failures, explicit absent-file expectations and descriptor closure.
These operations use the existing simulator file environment and never access
host files. A constant-memory operation counter detects file activity in
invocations that must perform none; no particular buffering strategy is
required. Format 19 introduced the repeated-byte representation.

For each of three existing correct assembly implementations, the original
public checker and the new model agree on case names and budgets, every
reversal argument and result, and snapshots of all 16 caller objects before
and after each reversal. The original checker retains its complete-byte
assertions. The native profiles pass all 35 cases on those implementations
and on a fourth implementation that uses private storage between caller
objects. They reject the three existing reversal faults and the unfinished
starter: 280 native case executions across eight sources. The pipeline case
checks the exact 60-byte saved WAV with reads limited to seven bytes and
writes to five, starting descriptors at 19. Private sources and raw receipts
remain in the instructor repository.

Five model tests and the broader 586-test public tooling run pass. These are
assessment-foundation checks, not a completed interactive WAV lesson, a new
IDE walkthrough, or renewed publication validation. The next delivery step
must compose the student activities, exercise them in the IDE and integrate
the verified experience into the native LaTeX lab. The historical 64-step
construction package is retained independently.

## Native student-unit draft (23 November 2026)

`revision/lessons/031-pcm-wav` now contains a standalone native lesson with ten
inline predictions, five Debug activities observing ten stops, one ordinary Run
and four assessment stages. The five worked experiments separate padded chunk
offsets, descriptor publication after complete validation, per-channel raw FIR
history, capacity-before-store and whole-frame addressing. All ten declared
register snapshots pass actual core execution probes, followed by normal silent
termination. In the failed parse, every eight-byte group of the 32-byte metadata
sentinel remains unchanged. The failure observation does not substitute for the
independent access checks.

The continuing independent source retains the original unfinished reversal
entry and caller. The complete public WAV helper is included directly, making
this source standalone without a preparation command. It is tested against the
original split-file source plus that exact supplied helper. The staged native
checks cover twelve mono cases, twelve stereo cases and eleven boundary/reuse/
pipeline cases. The final stage rechecks all 35 on the completed source after
edits. A normal Run of this source still performs its default bound calculation;
it does not prove that reversal or file saving works. The assignment makes this
distinction explicit.

Build the explicitly exported seventeen-file archive with:

```sh
python3 tools/native_lesson.py revision/lessons/031-pcm-wav \
  --output build/native-lessons/031-pcm-wav-0.1.0-student.zip
```

Four student-unit tooling tests join the passing 590-test public suite. Two
Jupiter tests import that real archive through the plugin, check all staged case
contracts and resolve every prediction/Debug anchor through IntelliJ PSI.
These tests pass without changing the production instructional system. The
assessment-foundation regression remains the preceding 285 CLI/1,307 plugin
test run; this content-only step does not claim a new complete plugin run.
The next step is the actual light-theme IDE first/resume walkthrough through the
separate authoring plugin, followed by directly edited native LaTeX lab integration.
No new IDE capture, PDF or accessibility validation is claimed at this point.
