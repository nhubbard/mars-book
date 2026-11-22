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
