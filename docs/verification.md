# Chapter verification

The initial package was checked with JDK 25.0.1 and the locally built MARS CLI
from the upstream checkout pinned in package.json. The private build records the
actual CLI SHA-256 and Java version, rather than assuming a JAR matches a branch name.

## Executed checks

- The illustrated reference passes three public and ten private input cases.
- An alternative using $t4 and computing (x + 1) + x passes the same thirteen cases.
- The illustrated register checkpoint passes for input 20: $v0 = 20, then $t0 = 40 and 41.
- Seventeen partial programs reconstructed from the published snippets assemble,
  produce their expected partial output, and exit normally.
- The reconstructed complete program passes all three public cases.
- A deliberate +2 error fails all three public cases.
- The original starter exits normally and fails all three assignment cases.
- All 22 quiz keys cover existing questions and valid choices.
- Twelve Python tooling tests check package validity, quiz consistency, path confinement,
  ordering, deterministic exports, insertion of author prose within a chapter, link
  rebasing, native diagram export, source preservation, and annotation validation.
- The Markdown book and student archive build successfully.

These cases establish the observed behavior for the supplied fixtures; the full bounded
input domain is specified by the task and explained algebraically in the text. They are
not an exhaustive execution of every possible input. The private verifier can be rerun
against any explicitly supplied compatible CLI build.

## Remaining publication work

The five IDE figures have recipes, captions, alt text, and annotation labels. Native
automated IDE capture and actual screenshots remain pending. The annotation renderer
is implemented and tested, but that is only the rendering stage of the capture process.
Interactive progression, downloaded lessons, and instructor policy enforcement are
design descriptions. The text needs author review and a student walkthrough. MacTeX
composition awaits the author-supplied template and frontmatter.

## Expanded chapter review

The 0.2.0 reading sequence places 22 connective prose sections and ten semantic
teaching fixtures around the original microsteps. Two diagrams have SVG draft
previews and native TikZ sources. The SVG previews were rendered and visually
inspected; their labels and arrows are readable without clipping. Native TikZ
compilation and PDF accessibility checks await MacTeX and the author's template.
The two diagrams explain concepts and are not IDE screenshots.

## Single-pixel chapter

The illustrated first word is 0x00336699; its little-endian bytes are 99 66 33 00.
After the byte store they are ff 66 33 00, and the final word is 0x003366ff.
The chapter's verifier checks the reference, an equivalent register allocation,
reference checkpoints, intermediate snippet programs, and deliberately incorrect
implementations. It also checks varied neighboring canaries. The new data-address
assertion is covered by packaged CLI tests for aligned, unaligned, and wide addresses,
as well as moved, missing, and incorrectly typed symbols.

The CLI checks memory state, not the visible Bitmap Display. The three pixel figures
remain planned. The chapter explains RGB numerically so its meaning does not depend on
recognizing a color in a screenshot.

## Eight-byte sprite chapter

The sprite reference and bitswap alternative pass the same coordinate-based oracle.
Public cases use the default image and walking bits. Private cases include all 256
possible byte patterns across 32 frames, plus empty, full, and mixed rows; this is not
an exhaustive test of every possible eight-byte image. The frame is poisoned before
each execution. Both canaries and the original source bytes must be preserved.

The illustrated checks observe the loaded row, completed reversal, first pixel pair,
and first completed row. Fifteen cumulative source segments are reconstructed from
the published steps and checked at their intermediate register or memory boundaries.
Deliberate early reversal termination, wrong stride, omitted black stores, a color
write in a delay slot, an overrun, and source modification must all be rejected.

The default workload completes 1,674 instructions in the explicit-loop reference and
1,210 in the bitswap alternative, including exit. The 464-instruction difference is
also explained by eight replacements of 59 reversal instructions with one bitswap.
The verifier requires warning-free assembly and exact counts for these fixtures.

The concept diagram's SVG preview was rendered and visually inspected. Its native
TikZ source awaits MacTeX and the template. All four IDE figures remain planned;
no screenshot or repaint assertion has yet been verified in an IDE capture.

## Bit-inspector chapter

The reference and wide-shift alternative use the same word-report and full-width-memory
contract. Public inputs include -1, zero, and the maximum signed word. Private inputs
add signed boundaries, walking-one and walking-zero patterns, and deterministic mixed
words. These are representative tests, not all 2^32 possible input words.

Each of the four result fields is checked across eight bytes. Poisoned high halves
expose narrow stores that a console-only or low-word-only test would miss. The source
and both varied guards must be preserved. Illustration checks observe both load
extensions and both addition widths for -1 and the positive signed-word boundary.

Eleven cumulative programs are reconstructed from the published code segments and
checked for intermediate registers, stored bytes, and their exact partial reports.
Deliberately wrong extension, arithmetic width, source interpretation, store width,
formatting, input writes,
and result overruns must be rejected. Both implementations must assemble without warnings.

The extension diagram preview was rendered and visually inspected. Its native TikZ
source and the other fixtures await the LaTeX template and compilation. All four
IDE figures remain planned; a console report alone is not evidence of the high halves.

## Seven-segment counter chapter

The count-loop reference and exclusive-end-pointer alternative are checked against the
same complete MMIO store history. Public windows cover the full table, the last two
entries, and the empty interval at its exclusive end. Private cases cover all 153
valid start/count pairs with varied byte tables, plus sixteen identical masks. This
covers the window domain, not all possible 128-bit fonts. High-bit masks are included.

The pinned CLI adds `traceMemoryStores` and `MemoryStoreTrace.expectByteStores` for
exact byte-port protocols. `expectNone` on label-based traces forbids execution stores
to protected objects, including temporary modifications restored before exit. Ordinary
application fixture edits and reads are excluded. Final table, inputs, guards, and
empty console output are also checked. A full-MMIO subscription rejects extra ports.
The trace is bounded at 65,536 writes and fails assertions if that limit is exceeded.

Ten cumulative programs are reconstructed from the chapter's assembly segments. Private
illustration checks observe the first two transactions and loop updates; free-writing
checks require no scratch registers or private labels. Mutants test missing transactions,
wrong strides and ports, wider stores, missing empty pre-tests, restored input/table
writes, an extra blanking write, and ignored starting indices.

The native segment-map source has a rendered, visually inspected SVG draft preview.
Its TikZ version awaits the actual LaTeX toolchain and template. Four light-theme IDE
captures remain planned. CLI histories do not establish panel refresh or screenshot
completion, and no wall-clock timing requirement is imposed on the counter.

## MarsBot square chapter

The core now owns the existing robot device and world model; the IDE imports the same
classes that the CLI grader uses. connectMarsBot advances that model at the same onStep
boundary as the IDE. No second movement approximation is used for grading. The model
retains its memory-access observer, instruction-clock phase, immutable pen trail, and
backstep checkpoints. Its existing tests now run in the core. Additional CLI tests
exercise real feedback code at multiple rates and cleanup across pauses and fresh cases.

Public checks cover side sixteen at rate 64, side one at rate 64, and side sixty-three
at rate 17. Private checks cover all 63 lengths at four rates (8, 17, 64, 97), for 252
cases per implementation. They check the complete four-stroke trail, final position,
heading, movement and pen state, silence, unchanged inputs/guards, and no CPU writes
to either feedback word. These samples do not exhaust the permitted rate interval;
the chapter separately derives the demonstrated loop's six-instruction stopping bound.

Twelve cumulative programs are reconstructed from the published assembly segments
and checked for their intermediate device controls, position, ink history, and input
preservation. Negative programs exercise missing ink, a wrong heading, the wrong axis,
a stale register, an omitted leg, enabled final movement, a lowered final pen, a fixed
input length, temporarily modified inputs/guards, coordinate writes, and extra ink.
Reference-only checks establish the named corner boundaries separately from free-writing
grading. Both implementations must assemble without warnings.

The screen-axis diagram preview was rendered and visually inspected. Native TikZ and
four planned IDE figures await the publication toolchain and separate capture plugin.
The fourth recipe backsteps the east-leg stop: model restoration is tested, while
visible IDE restoration still requires actual execution and capture verification.

## Bresenham line chapter

The incremental-error reference and rounded-rational alternative produce the same
canonical line image. Endpoint order is normalized before halfway choices. An independent
rational oracle checks every one of the 4,096 ordered endpoint pairs on the 8 by 8
canvas, covering both orders, all slope classes, all axis-aligned lines, exact ties,
and all coincident endpoints. Three additional color boundaries bring each private
suite to 4,099 cases. Colors and initial images are sampled rather than exhausted.

Every final cell word is checked against the selected color or its original poisoned
value. Inputs and guards also have execution-store traces, so restoration cannot hide
a forbidden write. This is a final-image contract; it does not claim to check every
intermediate framebuffer store. Reference-only checkpoints verify the illustrated
coordinate/error boundaries separately from equivalent free-writing submissions.

Thirteen cumulative source programs are reconstructed from the chapter and checked
for register meanings, exact frame prefixes, and protected data. Twelve faulty programs
exercise ordering, pair swaps, row stride, store width, endpoint timing, x-only termination,
changed error dependencies, mutually exclusive coordinate decisions, a wrong tie rule,
falling-line direction, an extra cell, and a restored input write. Both implementations
must assemble without warnings. Their default completed-instruction totals are checked
as 145 and 138; no latency or universal performance claim is inferred.

The native grid and decision equations await LaTeX compilation and template accessibility
verification. The SVG grid preview was rendered and inspected. Four real light-theme
IDE figures remain planned; CLI image checks do not establish visible panel rendering.

## Life generations chapter

Both the unrolled/Boolean reference and offset-loop/branching alternative are checked
at a required public generation_ready boundary. The checker observes the actual current
and previous padded boards, generation number, and all frame words at every generation
from zero through the requested count. It switches an execution-store trace between
physical arrays to forbid current-source writes during each phase, even when restored.
Seed, requested count, six guards, and both permanently dead halos are protected throughout.
After exit the final boards and frame are checked again. No new grader API is required.

Private coverage is 922 cases per implementation: all 512 interior center/neighborhood
patterns, 64 corner patterns, 256 edge-midpoint patterns, and eighteen whole-board
fixtures at each count zero through four (90 cases). This exhausts those local domains,
not all 2^64 complete boards. An independent unpadded scatter oracle contrasts with the
assembly's padded gather. Poisoned interiors and frames expose stale dead-cell values.

Fifteen cumulative source programs are reconstructed and checked for exact intermediate
board/frame contents, publication numbers, and illustrated register lifetimes. The ready
placeholder is removed at its documented relocation step. Fourteen negative programs
test rule, neighbor, stride, source aliasing, dead stores, publication, role exchange,
counting, halo writes, and restored source/input writes. Both completed implementations
must assemble without warnings. Measured default instruction totals are 7,007 and 14,197;
no wall-time or cycle-cost inference is made.

The two explanatory grid previews were rendered and visually inspected; native TikZ
sources await LaTeX compilation and accessibility verification. Four real light-theme
IDE captures remain planned. A generation boundary provides stable model/frame contents,
but actual panel rendering still needs the separate capture plugin and IDE verification.

## Sorting visualizer chapter

The indexed reference and insertion-cursor/difference-sign alternative run both
algorithms against actual published arrays and frames. The public protocol requires
initial, complete-exchange, and final observations, with strict adjacent insertion and
first-minimum selection. Final sorted values are independently compared with a sorted
input. Immutable metadata, guards, and inactive-tail stores are watched throughout.

There are 1,306 private sorting cases per implementation (653 arrays, two selectors),
plus twelve public program cases and seventeen independent renderer calls. Ternary
prefixes through length five and full-domain pairs are exhaustive; arbitrary length-eight
arrays are not. The renderer is entered without main setup, with changed pointers/lengths,
a label-resolved return continuation, and preserved 64-bit saved-register canaries.

The CLI now provides opt-in native instruction/branch counters, a half-open instruction-PC
range filter, and label-address register setters. Jupiter and standalone-JAR tests cover
branch outcomes, jumps, delays, range endpoints, replay, cleanup, wide addresses, argument
kinds, and return continuations. Six fresh reference workloads record whole/range instruction
counts and taken/not-taken branches. The range excludes callee bodies, with in-range call
and metadata work retained. No cycles, prediction misses, or host timings are inferred.

Fourteen cumulative multi-region source edits, twenty-seven quiz keys, and twelve faulty
programs are verified. Reference default whole counts are 26,197 and 26,206; the detailed
six-row measurements are also asserted. Student correctness does not require those totals.
Both implementations assemble without warnings. Native bar and call figures have inspected
SVG previews. Four actual light-mode IDE captures and PDF accessibility checks are pending.

## Maze explorer

The tenth package publishes a complete BFS contract for an 8 by 8 unit-cost grid.
Its eight public graphs check every initialized, processed, and final snapshot, while
five independent renderer cases seed caller state without running main. The checks
follow any valid permutation of newly discovered neighbors. Queue history, first
predecessors, shortest distances, and final masks are verified as relations rather
than matched to one reference path.

The private companion adds 416 cases per implementation: 256 eligible 3 by 3 wall-mask
and direction cases, 96 deterministic full-grid layouts, and 64 zero-edge paths. Its
verifier reconstructs seventeen authoring stages, rejects seventeen warning-free faulty
variants, and measures both implementations. Reference/alternative whole counts are
7,286/7,318 for the tiny room and 125,384/126,314 for the full room. Exact counts are
reference evidence and do not grade equivalent student solutions.

The package pins the upstream grader revision supplying readWord, readBytes, and
expectCondition. Those operations let the checks inspect student choices without
publishing simulator accesses or changing undo history. Three new tests brought the
complete CLI suite to 77 passing tests. The public memory ABI states four-byte array
alignment; byte entries still have byte stride. Capture recipes remain pending actual
light-theme IDE automation.
