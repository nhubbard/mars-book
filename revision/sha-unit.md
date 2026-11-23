# SHA-256 companion to word transformations

Section 22.7 revises original project `025-sha256`. It occupies lab pages 146–153
in the 153-page proof, sharing page 146 with the preceding ChaCha20 exercise.
The shared lab chapter is now titled “Word transformations”; the main book is
unchanged at 145 pages. Native LaTeX remains the directly edited composition.

## Teaching sequence and student work

Seven conceptual subsections distinguish message bytes, numeric schedule words
and serialized digest bytes; derive four schedule predecessors and the two small
sigmas; trace a compression round before feed-forward; define the context
invariant; prove one-block versus two-block padding; and connect successful read
prefixes to final publication at EOF. The final subsection asks the reader to
build and defend independent end-to-end evidence. Two short instruction excerpts,
two native tables and four actual Debug figures support the explanation.

The student implements all six interfaces in `labs/files/sha256.asm`. That file
supplies only caller/data arenas, fixed constants and intentional break entries.
Full solutions and alternatives remain private. Completed grading permits
independent private labels, frames, allocation, helpers and equivalent algorithms.
The original package's cumulative construction source and 47 steps remain
available separately; the printed companion does not repeat them as sections.

The existing `packages/025-sha256/prepare.py` creates deterministic observations.
The printed command is executed against the new skeleton: exact input bytes are
checked, instruction text is preserved, and repeat invocation refuses existing
outputs without changing either output or source. The prose names the supplied
caller defaults required by the tool. An ordinary host file is not described as
forcing short reads or injected failures.

## Finite execution evidence

Reference, equivalent alternative and above-32-bit-data variants each pass 54
public kernel, 40 file, 12 caller/program, 22 reuse, 3341 private and two additional
4096-byte file cases: 10,413 positive grading-case executions. These are case
executions, not a count of distinct programs or function calls. The unfinished
independent skeleton fails all 54 kernel cases as intended.

Fourteen assembled semantic faults are rejected. Their exact wide-address
reference baseline passes the same 54 kernel and 40 file cases. Faults cover
arithmetic sigma shifts, wrong predecessors/constants, overwritten round state,
missing feed-forward, narrowed pointers, both padding thresholds, missing marker,
word-sized length operations, hashing a requested rather than returned count,
read error treated as EOF and descriptor leakage.

Three additional exact written observations verify the first compression round,
a 63-plus-two-byte update and the synthetic full-width bit-length field. Four
capture probes verify the schedule row, complete abc digest, final padding length
and unchanged destination after a read error. A separate Python integer model
agrees with hashlib for sixteen complete messages and supplies the worked trace.
The large-count example tests serialization of a synthetic context; no huge file
was read. Both implementations also pass eight real host-file observations each:
seven existing files including padding boundaries and 4096 bytes, plus one missing
file. These ordinary syscall runs are independent of virtual file fixtures.

## Publication evidence and limits

The separate authoring plugin passes 104 Jupiter cases and is launched through
actual `clean cleanSandbox runIde` tasks. Four IntelliJ Light Debug captures are
byte-identical to the published PNGs and retain native coordinate receipts.
The read-error capture also records actual virtual operations: open descriptor 19,
read three, read two, fail a read and close. Its closed-session receipt confirms
no descriptor remains open. The exact I/O fixture stays in the authoring plugin;
no authoring code or new source changes were added to MARS.

Native LaTeX owns figure titles, outlines, crops, captions and alternative text.
The caption distinguishes what the image shows from separately checked status
and closure. Both printed excerpts reuse exact existing actual IntelliJ export
records; the 825-record manifest is unchanged. Minion Pro, Noto Sans Math and
JetBrains Mono remain in the embedded font set. All twelve pages affected by the
shared heading and companion receive color review; pages 147–152 receive grayscale
review. The exact final tagged PDF and passing automated PDF/UA-2 report are
retained privately, together with earlier proof/report pairs from layout review.

These finite checks establish observed behavior and publication structure. They
do not establish NIST certification, hardware timing, author approval, teaching
effectiveness or assistive-technology usability. Author, representative learner
and assistive-technology reviews remain pending. The native guided lesson and
enforced progression/hint policies for this advanced companion remain open.

The overall goal remains active. The RSA companion is next, followed by WAV,
PNG and the retained lossless/H.263 video sequence. The deferred opening
explanation of MARS versus hardware has not been added.


## Native assessment conversion, 23 November 2026

The public `tools/sha_lesson_cases.py` model now represents all 128 original
correctness cases in 33 bounded function-case profiles (format 16). The original
286 observed calls remain in their original sessions; twelve additional native
calls execute actual main dispatch before the real caller windows. All cases
retain their original 100,000/3,000,000 instruction budgets and actual exit.
Profiles split at the existing ten-million aggregate instruction allowance.

Expected memory covers the specified schedule/state words, context fields and
active tail, or final digest. Reserved bytes, inactive context buffer and private
workspace values remain unspecified. The observation history and store-only
policy cover the whole assembled `data` through `status + 4` span, including
unnamed gaps; object policies retain the original read permissions. File cases
retain exact fixture contents, short-read and error settings, actual descriptor
ownership and successive calls in one machine.

Private qualification compares every input, argument, specified result interval,
permission and file setting against the original runtime observations in four
implementations, including a variant that clears reserved context bytes. The
native checks accept those implementations, reject the fourteen historical
injected faults and unfinished starter, and retain the gap-store and later-error
output-preservation boundaries. The production plugin parser reads the exact 33
profiles in a Jupiter fixture test. Public host-model tests bind the four
original check-script digests and compare normal-message arithmetic/padding with
Python's independent SHA-256 engine.

This completes assessment preparation only. The new native lesson package,
student-facing activities, source pointers, saved/resume IDE walkthrough and
integration into the written companion still need to be completed. Existing
LaTeX and earlier capture evidence remain authoritative; no new PDF or screenshot
is claimed by this step.


## Native student lesson draft, 23 November 2026

`revision/lessons/025-sha256` now contains the native lesson draft. Its 34 activities
include sixteen inline predictions, nine Debug activities with 32 stops, eight
function-check groups and one Run. Six new public observation sources show a
schedule prefix, one compression round, three padding boundaries and a block
filled just before compression. They explicitly describe their limits and do not
supply the six-function independent implementation. Students keep that
implementation in one continuing `independent/sha256.asm` source.

The staged data retains every original completed case unchanged and adds seventeen
update-only prefix cases, allowing returned context state to be checked before
finalization works. There are 145 cases and 357 calls in 42 bounded profiles.
All correct qualification implementations pass these cases; the fourteen existing
faults and unfinished starter remain rejected. The worked experiments' 26 stops
and the completed-student caller's six stops pass actual core register/memory
probes. The caller sources used for qualification remain private.

The exact student archive imports in the production plugin reader, and all source
pointers resolve through native MIPS PSI in two passing Jupiter tests. All 572
public tooling tests pass. These checks do not substitute for the first/resumed
IDE walkthrough, which remains to be exercised. New light-theme captures and
integration of this native workflow into the directly edited LaTeX companion
also remain. The existing publication and capture receipts have not been renewed
by this draft step.


## Source-stable native revision 0.2.0

The initial 0.1.0 draft required editing the independent caller’s mode word for
three observations. Source edits invalidate earlier execution evidence, so that
mandatory mode-switch sequence has been removed. Its archive and initial test
receipts remain private history. Optional experiments with the student’s own
caller remain documented after the required sequence.

Version 0.2.0 has 35 activities, eight public worked sources, nine Debug activities
with 32 stops, sixteen predictions, nine assessment groups and one Run. The added
serialization example is given its final state and does not calculate it. The
file-prefix example publishes a byte count only at EOF; it illustrates ownership
without supplying the six-function SHA implementation. Required Debug activities
all use unchanged worked sources and deterministic fixtures.

The final assessment reuses the 36 original-case profiles and rechecks all 128
original cases on the completed independent source. There are still 42 distinct
profiles and 145 distinct cases including construction prefixes. Including the
final recheck, the lesson performs 273 case executions and 655 calls. Every
current public Debug observation passes actual core register/memory probes, and
the revised archive passes its two importer/PSI tests and all 572 public tooling
tests. Actual first/resume IDE exercise, light captures and direct LaTeX
integration remain pending; no renewed publication evidence is claimed yet.
