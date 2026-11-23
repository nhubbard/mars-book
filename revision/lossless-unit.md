# Lossless-video unit

Chapter 25 revises original project `034-lossless-video` as a connected main-book
explanation and written lab. Directly edited native authority is
`main/lossless-video.tex` and `labs/lossless-video.tex`, included by the revised
entries. The final main proof is 171 pages, with this chapter on 163–171; the lab
is 175 pages, with this unit on 171–175. The original manuscript, construction
sequence and its six original figures remain preserved.

## Teaching sequence

The chapter follows one moving eight-frame clip through native RGB words, XOR
prediction, bounded commands, record validation, encoding cost, complete indexing,
independent seeking and anchored playback intervals. It derives the eleven-byte
first delta and the directory's exact offsets. A late zero/zero command with a
repaired CRC separates structural validation from checksum acceptance. Additional
full records demonstrate why seeking cannot assume that the periodic boundary is
the latest independent image. Clock reasoning separates logical duration, actual
host lateness and screen presentation.

The complete `labs/files/lossless-video.asm` retains callable codec and player
entries in one compact worked file. The original full-codec construction assignment
remains available. Compact example arenas do not change accepted format limits;
maximum-size checks supply separate storage. The chapter describes each routine's
own publication guarantee rather than treating all failures as universal rollback.
The MLV1 format is a local teaching format; the following H.263 work targets an
actual restricted interoperable codec.

The independent `video-change-bounds.asm` compares original native words and
publishes minimal exclusive bounds and the changed count. The written lab explains
full-width dimensions, exact accessed windows, aliases, empty results, retained
inputs and saved state. A two-by-three example distinguishes two changed words
from a six-cell containing rectangle. One-pass and two-pass private implementations
both satisfy the public contract. The feedback's explicit finite budget admits
both approaches; it does not require source matching or an exact instruction count.

## Qualification and feedback repair

Reference, equivalent and high-code codec variants each pass 2088 public/private
cases: 6264 positive case executions across 87 domain runs. The bounds implementations
pass 2706 cases across two byte orders and ordinary/wide placement. Six additional
cases compare all seven real moving-frame transitions; input images remain intact.
Four core probes verify the exact boundaries selected for the Debug photographs.
Eight assembled codec faults and six bounds faults are rejected. The unfinished
bounds starter fails all 182 public cases as expected.

The initial invalid-dimension feedback combined an invalid dimension with a
misaligned input address. An implementation that narrowed dimensions could reject
for the latter reason and pass. The corrected cases use aligned guarded image
pointers, require rejection before reading them, and reject the same previously
accepted assembled fault. The before/after evidence is retained separately from
positive qualification. Initial too-small default instruction budgets are also
retained as diagnostics, excluded from final results.

Three actual host-file/clock caller runs retain encoded files, native framebuffer,
seek/context/directory dumps and console streams. Independent decoding agrees for
all eight frames and every seek target. The reference emits 24795 bytes; the
alternative and high-code callers emit 73911 bytes with additional valid full
records. Target six therefore starts at four in the former and five in the latter.
These individual lateness observations are not timing benchmarks. A separate
unmodified public-caller smoke run verifies the printed end-to-end behavior. The
printed Python fixture command produces eight PPM previews; Pillow reads their
pixels and agrees with an independent decoder. Existing output is refused.

The transport's documented readable guard byte follows the current educational
write service's one-byte prefetch. It is not transferred to the file. No core
accuracy behavior or simulator implementation was changed by this publication step.

## Native captures and publication checks

Four final IntelliJ Light Debug captures show accepted record publication, retained
pixels on late rejection, independent seek and playback result fields. A fifth
actual Run observation verifies the selected later global main entry. Before each
IDE launch the real clean cleanSandbox runIde tasks ran. The final batch binds one
separate authoring driver and the exact immutable MARS plugin archive. The owned
IDE is closed. Raw PNGs and receipts are retained; crops, annotations, diagrams,
captions and semantic alternatives are native editable LaTeX.

The authoring driver now selects main explicitly in both Run and Debug. Capture
wrappers export a later main because that launch option uses the global symbol
entry. It uses public run-configuration persistence, with no internal MARS option
classes. The main MARS plugin contains no publication-authoring code. All 105
Jupiter tests pass. Paired verification resolves both immutable local plugin
archives against IntelliJ IU-263.7307.200: both are compatible, with experimental
public uses and no internal or invalid override-only API findings. A single-plugin
Gradle verification attempt could not resolve unpublished MARS; a subsequent
verification/clean overlap is retained as a setup diagnostic. Final verification
uses an immutable copied archive.

The public tooling suite passes 419 tests. Two actual IntelliJ highlighter records
bring the catalog from 831 to 833 without changing its earlier prefix. All new
proof pages and representative grayscale code/figures are visually reviewed;
automated PDF/UA-2 checks bind the exact final proof hashes. These checks do not
constitute author, learner or assistive-technology review. The revised book has not
been validated with students. Advanced native lesson delivery and the remaining
H.263 compositions are still open; this unit does not complete the curriculum goal.


## Native assessment foundation, 23 November 2026

The continuing changed-pixel bounds task now has native function-case profiles,
without an assembly solution in the public materials. All 182 original cases
are retained: 91 per byte order, including 192 calls because the two reuse cases
each invoke the same machine six times. Eight bounded format-21 profiles retain
the original million-instruction case budgets, exact four/eight-byte alignment,
full-width dimensions, selected input intervals, minimal exclusive coordinates,
unchanged caller bytes and saved state. Compact repeated guard bytes reduce the
download size without removing the surrounding objects or access checks.

An instrumented execution of the original checker and the native model agree
on canonical case order, all arguments, status/count results and every complete
caller object before and after each call, for reference, equivalent two-pass and
wide-address implementations. The native prepared checker then passes all 182
cases for those three implementations and for a variant using private scratch
between caller objects. It rejects each of six assembled faults and the unfinished
starter; the starter fails all 182 cases. These are contract checks, not source
matching. No new grader capability or simulator change was needed.

Four new public model tests and the complete 603-test public tooling suite pass.
Two Jupiter tests through the actual plugin parser verify the eight profiles,
expanded guard extents, both byte orders, repeated calls and a combined final
assessment without reducing its budgets. Production plugin code is unchanged;
the preceding PNG milestone retains its broad 1,319-plugin-test regression and
coverage results. All 41 manual edits remain preserved.

This is an assessment foundation. The native student package, inline questions,
worked debugger experiments, first-use/resume IDE trials and renewed written-lab
integration remain to be completed. No new screenshot, PDF build or accessibility
validation is claimed by this step. The existing native LaTeX books and historical
lossless-video evidence remain unchanged. Private native-lossless-foundation-01
retains exact qualification, comparison and profile-parser receipts.


## Native student-unit draft, 23 November 2026

The draft `revision/lessons/034-lossless-video` now contains six worked sources,
twelve inline predictions, six Debug activities with eighteen boundaries, one
silent Run and five assessments on a continuing independent source. Four actual
helper experiments cover vector XOR/alias/tail/empty behavior, successful and
rejected record publication, encoding cost and committed reuse, and embedded
index/seek. Two explicitly named arithmetic models explain anchored deadlines
and selected exclusive coordinates. No required observation reads a host file,
waits on a host clock or needs a generated external fixture.

The supplied helper bodies and relevant static fixtures are retained exactly.
Removing unused complete-file examples and data reduces those four worked
sources from 2,676,699 to 283,188 bytes. This is a source-size comparison, not a
startup or execution-time measurement. Six core probes verify all eighteen stop
snapshots and normal silent exits. They also compare every image/guard and
context byte on late record rejection and invalid seek, and all previous-image,
record-storage and context bytes on capacity rejection. A correct CRC on the
malformed candidate is tested independently from its zero/zero command failure.

Stages retain 44 equality/alias, 66 edge/row-crossing, 44 mixed/full-shape and 28
boundary/reuse cases, followed by the final 182-case recheck. Both byte orders,
192 final calls and original budgets remain. The independent starter is
byte-identical to the existing public exercise. Its exact exported form passes
all 182 cases with three instructor bodies, including wide-address placement;
the unfinished form fails every case. These 728 qualification outcomes remain
private and do not add a solution to student material.

Six public unit tests check helper/fixture fidelity, CRC-correct late rejection,
complete deterministic twenty-one-file export, all profile stages, complete
vector values, dimension width, count-versus-area distinctions and the distributed
fixture tools. The full tooling suite passes 609 tests. Two Jupiter tests through
the actual package importer and native PSI retain all 24 activities, resolve
all thirty prediction/Debug anchors, and validate the final cases in both byte
orders. The tests exposed a Python bytecode cache written into the fixture
folder; the test now loads that script without writing there, and the explicit
export remains complete. Production plugin code is unchanged and all 41 manual
edits retain their hashes.

Build the student archive with:

```sh
python3 tools/native_lesson.py revision/lessons/034-lossless-video \
  --output build/native-lessons/034-lossless-video-0.1.0-student.zip
```

Actual first use, unsaved checking, edited-source invalidation, fresh-process
resume, verified light-theme capture and directly edited LaTeX integration remain
next. The accepted lab PDF is unchanged by this draft; no new screenshot or
accessibility validation is claimed. Private native-lossless-unit-01 retains the
source-bound probes, package tests, exported-starter qualification and diagnostics.
