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

## Matrix transpose

The eleventh package checks both rectangular word-transpose functions with supplied
full-width pointers, not global-label assumptions. Twenty public cases cover empty,
one-row, one-column, square, rectangular, and clipped shapes. The private companion
exhausts all 289 shapes from zero through sixteen in both functions (578 cases),
varies active-window offsets, and uses arbitrary word patterns including signed
extremes. It protects input, guard, and inactive output writes, verifies full saved
registers and the restored stack pointer before the continuation, and checks memory
again after exit.

Both independent checkers decode native access histories to verify the published
one-word-read/one-word-write schedule. Ordinary histories follow source row-major
order; tiled histories complete clipped 4-by-4 tiles in any order. The verifier accepts
an alternative outer tile order, stack-backed saved state, and reversed rows inside
each tile. A full-address relocation above 4 GiB uses the same actual execution path. A legal
variant with 65,540 auxiliary stack reads per call verifies that grading histories
extend beyond the ordinary logger default and fit the instruction resource budget.
A second ordinary kernel posing as tiled is rejected even if its output is correct.

Thirteen cumulative programs are reconstructed from the student transcripts and
compared with all thirteen native assembly listings. Every partial result is checked
before the completed reconstruction runs the public, exhaustive, and measurement
checks. Twenty-eight quiz keys are private. Eleven warning-free faulty programs and
the intentionally incomplete starter fail the checks. Four planned capture model
states are independently checked through the actual complete main program; these
checks do not certify an IDE screenshot.

The shared core/IDE cache model and CLI observation API are pinned at upstream commit
`1f2de4ff48d5e70a76763edf489000fd6ca7fa3c`. The extension passed seven focused core
Jupiter tests, all 82 CLI tests, and three relevant plugin tests before this chapter.
Counts-only observation, bounded immutable access logs, explicit overflow, cold reset,
cleanup, external-inspection filtering, and full addresses are covered. No parallel
cache implementation is used by the grader.

For the measured aligned 16-by-16 reference, fully associative misses fall from 320
to 128 with tiling, while completed instructions rise from 2204 to 3117. Both perform
512 native matrix data accesses. Direct mapping produces 344 misses for each, and
two-way placement produces 320 for each at the same eight-line, four-word capacity.
The cold boundary excludes the caller's result store. Exact counts are reference
measurements, not equivalent-solution requirements or elapsed-time claims.

The chapter is native LaTeX with a semantic tile diagram, headed tables, actual
IntelliJ-derived minted spans, and separately compiled review output. Four real
light-theme IDE captures, author review, and full PDF accessibility validation remain
pending. No lesson screenshot has been fabricated or marked verified.

## Fixed-point ball

The twelfth package uses a signed raw/256 state and a bounded single-reflection rule.
Its public animation checks run six seeds, four requested counts, and both quantizers
(48 cases), inspecting the complete raw state, tick, ID, and frame at every ready marker.
Public kernels add twenty axis pairs, 288 render calls, and 64 signed-word sum pairs.
They vary destination pointers and return continuations, check full saved-register
canaries, protect caller-owned data, and inspect again after exit. A scoped return-marker
observation rejects a hardcoded continuation that would otherwise fall through to the
requested marker. A legal stack-backed implementation remains accepted.

Private axis coverage has 7937 cases: all velocities at three positions and all raw
positions with a varying velocity. This is not an exhaustive Cartesian product. An
independent periodic folding oracle contrasts with the assembly branches. Rendering
runs 3586 calls covering all raw x and y values in both modes through a permutation of
y values and alternating arenas. The 400 probe pairs combine signed extremes and
fraction-boundary values. Animation has 240 seed/count/mode cases, with every publication
checked. The actual renderer and public kernel checks also run above 4 GiB.

Sixteen cumulative programs are reconstructed from student transcripts and compared
with their native listings. Their intermediate trial, reflected, black-frame, pixel-ID,
initial-publication, and live-but-unpublished states are checked explicitly. The complete
reconstruction then runs all public and private scripts. Thirty quiz keys remain private.
Eighteen warning-free faulty programs and the incomplete starter fail grading; the
separate intentional ADD experiment produces the expected native overflow error.

The four planned capture model states are checked through complete main: initial floor,
initial nearest, tick-five reflection, and the standalone overflow results. These are
model-state checks, not verification of IDE pixels. Default reference whole counts remain
3901/3919 for floor/nearest and alternative counts 6165/6165. The proof of safe bounded
motion arithmetic and the broader overflow experiment are kept separate. Exact counts
are reference measurements, not equivalent-solution requirements or elapsed-time claims.

The chapter is native LaTeX with a reflection diagram and two-quantizer coordinate grids,
headed tables, extractable code, tagged mathematics, and actual IntelliJ-derived minted
spans. All 146 prior distinct highlight exports are unchanged. Actual IDE capture,
independent accessibility validation, and author review remain pending.

## Binary packet parser

Package 013-binary-packet-parser supplies 24 microsteps, 48 staged hints, 15 native
IntelliJ-highlighted code increments, a callable function starter, and separate
public function/main checks. The upstream grader is pinned to commit
`401d4fed76424179c28f4d414af4c8040acf3d1b`, whose native data-access trace records
complete read widths and full addresses, excludes external inspection/backstep,
retains replayed forward accesses, and fails explicitly on history overflow. Its
87 CLI Jupiter tests pass, including a packaged-JAR script integration test.

Public checks exercise 619 independent parser calls and 38 main-routine cases per
implementation. The private domain has 5016 calls covering all payload lengths,
all incomplete prefixes, unsigned sequence combinations, malformed header bytes,
reserved flags, length boundaries, suffixes, and maximum available windows. It is
not an exhaustive Cartesian product. Both a moving-pointer parser and an indexed
parser pass. A signed-payload-load variant also passes because final sums modulo
256 preserve its result. Both ordinary implementations pass with data above 4 GiB.

All fifteen cumulative programs assemble warning-free and produce checked intermediate
results. The final source reconstructed from student snippets matches the reference,
and runs both public scripts and the full private domain. Fourteen warning-free faulty
parsers and the unfinished starter are rejected. The verifier records 50 grading runs
with 32,168 positive case executions; repeated executions are not unique test inputs.
Output histories catch writes restored before an error return, and access histories
catch reads beyond logical input that would otherwise succeed in mapped memory.

Successful native function instruction counts for payload lengths 0/3/64 are
111/132/559 for the moving-pointer reference and 70/94/582 for the indexed alternative.
The corresponding byte-read counts are 17/20/81 and 9/12/73. Counts exclude assembly,
session creation, argument setup, main, and exit. They illustrate fixed/per-byte
tradeoffs and impose no exact instruction or load count on equivalent solutions.
The chapter states the actual generated payload pattern and measurement scope.

Three capture recipes have checked numerical model states; no IDE pixels have been
captured. The light-theme capture plugin runner, visual assertions, independent
accessibility review, and final author review remain outstanding.

## Ring-buffer logger

Package 014-ring-buffer-logger contains thirty microsteps, sixty staged hints, and
eighteen cumulative native assembly increments. Its descriptor tracks explicit
occupied bytes, permits capacities 1..32 without a power-of-two assumption, and
represents a zero-length payload with its own one-byte record prefix. Push and pop
have separate failure priorities and auxiliary results. Failed calls make no
caller-data writes, including writes restored later. Capacity and the stored full
buffer pointer remain read-only configuration; pop preserves ring bytes.

Public function grading runs 520 independent calls varying capacity, positions,
queue shape, request size, descriptors, rings, pointer offsets, return continuations
and full saved-register canaries. Twelve main-prefix cases inspect every completed
operation in the supplied fourteen-command FIFO demonstration. Main histories
check each operation's permitted stores separately, then reject caller-data writes
after the final marker. Native notices retain full read/store widths; source and
ring logical boundaries apply even when adjacent bytes are mapped padding.

The private domain has 37,477 cases. It includes every bounded capacity, head and
occupancy in simple queue states with derived requests, every fitting empty-queue
push length at every head, and additional multiple-record states. This is not an
exhaustive Cartesian product of payloads, requests or record partitions. The forward
reference and reverse-copy alternative pass the public/private scripts, including
the full private domain with data above 4 GiB. A legitimate restored stack frame
is also accepted. Eighteen partial programs have their intermediate bytes and
state words checked; the reconstructed complete source matches the reference and
passes all final scripts. Twenty-one warning-free faulty programs and the
unmodified starter are rejected. The complete verifier records sixty grading runs
and 226,520 positive case executions, including repeated inputs across variants.

Function-only native counts for a three-byte push are 61 forward/73 reverse without
wrap and 62/77 with a prefix at the last slot. The chapter also reports empty-record,
exact-fit, and pop counts, with the initial states and counting boundary specified.
The reverse copy is an accepted equivalent, not a claimed speed improvement. Counts
are not elapsed time, target cycles, or exact-count grading requirements.

Three planned light-theme capture recipes have verified model states. No screenshot
pixels or IDE interactions were captured. Native table headings, extractable assembly,
mathematics, fonts, and the explanatory diagram are inspected separately. Full
reading-order/screen-reader review, independent accessibility validation and author
review remain outstanding.

## UTF-8 scanner

Package 015-utf8-scanner has thirty-four steps, sixty-eight staged hints, twenty-two
code increments, versioned primary standards references, and separate streaming/main
checks. Unicode 17 Table 3-7 supplies the byte grammar; the state protocol and error
positions are this exercise's specified interface. It counts complete scalar values,
accepts well-formed noncharacters and unassigned values, and performs no normalization,
grapheme segmentation, replacement output, recovery, or U+FEFF stripping.

Public streaming grading has 426 cases, with multiple calls per case in one session.
It covers every one-byte input, selected scalar boundaries at every split, empty
chunks, pending prefixes, terminal reuse and counter positions at the stream limit.
Scratch and saved-register canaries, selected state/pointer variations, input-read
bounds, state-write ownership and after-return preservation are checked. Fifteen main
cases inspect each supplied checkpoint with valid input, an impossible F0 second byte,
or empty final input that diagnoses truncation.

A necessary upstream grader addition, `callFunction`, is pinned in commit
`f1ddaef8`: it uses native stopped-session ownership, sets entry/return as external
setup with a new undo baseline, retains memory/observations/instruction budget, and
refuses pending delayed transfers and forbidden slots. All 91 CLI Jupiter tests
pass, including packaged-JAR use and a shared-budget rejection. `startAt` retains
its initial-entry-only contract. No core instruction semantics were changed.

The private oracle tests prefix-value intervals against scalar bounds and the
surrogate exclusion, independently of the assembly branch tree and metadata table.
The transition suite has 4522 cases, including all next bytes for sixteen selected
reachable prefixes. Every two-byte input is tested (65,536 cases), supplied nonfinal
then followed by empty final input. Every scalar's canonical host encoding is tested:
1,112,064 values in 17,376 blocks, delivered in chunks of at most 64 bytes. This is
not all partitions of all scalars or all possible longer byte streams.

The branch reference and table alternative pass all domains, with additional wide
transition/main and restored-stack runs. Twenty-two partial programs pass twenty-nine
intermediate observations. Their complete reconstruction is byte-identical to the
exhaustively tested reference and also runs both public scripts. Twenty-five valid,
warning-free faulty scanners and the incomplete starter are rejected. The verifier
records 72 grading runs and 189,831 positive case executions; repeated executions
across variants are not unique inputs.

Complete-call instruction counts for ASCII/two-byte/four-byte 64-byte chunks are
991/1567/1631 for the branch reference and 991/1791/1583 for the table alternative.
The table adds one word read per multibyte lead and occupies 1024 bytes. Counts
exclude setup, assembly, session creation, main and exit. They illustrate the cost
of address/load/extraction work relative to different branch paths, without claiming
universal elapsed-time gains or imposing exact-count requirements on students.

Four capture recipes have verified full numerical state and return values, but no
IDE screenshot pixels have been captured. Native fonts, mathematical structure,
headed tables, diagram alternative text, assembly extraction, and actual color spans
are checked separately. Final author review, full reading-order/screen-reader work,
and independent accessibility validation remain outstanding.

## Clipped sprite blitter

Package 016 has 25 steps, sixteen authored increments and two provided balanced-stack
listings. Both preclipped and per-source-cell implementations pass 581 public kernel
cases, ten caller cases and 7220 private geometry cases each, plus wide-address kernel
and caller checks. The private suite covers every 0..8 width/height tuple for both
objects with varied placements, the -9..9 placement square for packed 8x8 images, every
legal width/stride pair in each role, and matching destination values. It does not
exhaust every coordinate, stride, color and key combination.

Each partial listing passes its own intermediate observation and restores full caller
state. The completed assembly is byte-identical to the reference. Same-value store
elision and bounded wrapping-edge equivalence are accepted; sixteen faulty assembled
variants and the unfinished starter are rejected. Writes into transparent pixels,
padding, source and guards are checked through native access history, even if restored.
Reads are validated byte by byte within selected descriptor fields and logical cells.
Equivalent load widths and traversal orders remain permitted; private stack is legal.

Five complete-function measurements use identical inputs: preclipped instruction
counts 227/308/126/170/44 versus 446/604/332/434/306 per-cell. Source/store notices agree
at 12/8, 20/13, 4/2, 8/6, 0/0. Setup, assembly, session creation, display and exit are
excluded; these are native work counts without elapsed-time claims. Exact counts are
not a free-writing grading requirement. Four capture models verify full frame cells,
return counters and physical RGB view expectations; actual IDE captures are pending.

## Expression evaluator

Package 017 is a complete native text draft with 45 steps, 34 authored region edits,
nine provided frame/wrapper/return listings and 90 hints. The temporary region returns
keep partial programs assemblable; their own observations are distinct from final
behavioral grading. All 34 cumulative programs pass 46 observations and reconstruct
the verified reference byte for byte. The provided native listings also match it.

Both recursive-descent and precedence-climbing implementations pass 540 public kernel,
20 caller and 10230 private cases each, plus wide-input kernel/caller runs. The short
private domain contains every string through length four over `01+-*/() ` (7381
strings). Further cases cover 1024 byte substitutions, 400 boundary operand pairs,
fourteen error-order inputs, 128 unary-depth inputs, 64 group-depth inputs, 1200 valid
chains checked by an independent operator-stack oracle and nineteen whitespace
expansions within the length limit. This is not every 64-byte input or every operand.

Each implementation also passes eleven rotated same-session sequences, eleven calls
per case, covering success after prior syntax, overflow and zero-division results.
The grader permits private stack and submission-owned constant reads, rejects all
caller-data stores even if restored, and checks every byte of observed reads against
the selected slice. Scratch-register choices, traversal order and load width are not
prescribed. Data above four GiB is executed; the attempted high text/stack addresses
are outside the pinned Default map and are not claimed as positive coverage.

The 76 grading runs record 23887 positive case executions across repeated variants,
illustrations and models. Nineteen faulty assembled implementations and the incomplete
starter are rejected. The final archives pin the CLI, scripts, source revisions and
student ZIP digest. Four numerical capture models verify checkpoint registers, input
preservation and pre-store memory values; actual IDE screenshot capture remains pending.

Measured function work varies by input shape. The separate-loop and threshold designs
use 1062/1050 native instructions for the default expression, 6281/6520 for the 31-plus
chain and 6194/5248 for 31 nested groups. The seven-scene table also records input-read
notices. Setup, assembly, session construction and exit are excluded. These are native
work counts without elapsed-time claims or exact-count grading requirements. Human
review and independent accessibility validation remain outstanding.

## Tiny bytecode interpreter

Package 018 has 51 steps, forty authored region replacements, six provided frame/exit
listings and 102 hints. Each of forty cumulative programs passes its stated partial
observations, 58 in total, before returning through the balanced frame restore. Their
assembly reconstructs the tested reference exactly; Markdown and native listings agree.
Public free-writing grading checks complete behavior without private labels, register
assignments or instruction-count requirements.

Both the 64-bit bitmap reference and private byte-map alternative pass 1247 public
kernel cases, 23 caller cases and 12720 private cases each, plus wide-data kernel/caller
checks. The private domain includes 4369 images through length three over a sixteen-byte
alphabet, 1280 byte substitutions, 512 late-opcode images, sixty wide-payload images,
4158 unreachable-target combinations, 800 random arithmetic pairs, 700 random encoded
images, 585 countdown capacity/budget pairs and 256 full-range target operands. These
do not exhaust all 64-byte images or all signed-word operands.

Ten same-session sequences per implementation make ten calls each, replacing bytes at
the same pointer and length and rotating success, structural failure and target failure.
The byte map with its clearing removed is rejected by all ten sequences. The complete verifier records 87 grading runs and 33134 positive case executions
across repeated variants, observations and models. Twenty-four
faulty assembled variants and the unfinished starter are rejected. Caller traces check
every byte of reads and writes against the selected image, descriptor and stack capacity;
a write followed by restoration still violates an immutable region. Inactive cells
inside capacity are unspecified workspace. Legal private stack and submission-owned
constant reads pass. Mapped data above four GiB is covered; high text and high stack
addresses are not claimed.

The seven-scene native-work fixture compares complete function calls, including validation
and restoration, excluding caller setup, assembly, session creation and exit. The default
full call retires 33 guest instructions and uses 2394 bitmap or 2259 byte-map native
instructions; zero-budget validation uses 794 or 659. The one-halt call uses 94 or 101.
The alternative also changes opcode classification, so those differences cannot be
attributed solely to boundary representation. Counts are not elapsed-time claims or
grading requirements. Four numerical checkpoint models are checked independently;
actual IDE screenshots and coordinate receipts remain pending.

## RGB888 and RGB565

Package 019 has 49 steps, 39 authored edits, four supplied leaf entry/return excerpts
and 98 hints. The sequence completes both scalar loops before vector blocks. All
39 cumulative programs pass 47 observations, including full scalar values, pointer
advances, controls, gathered lanes, fields, interleaved bytes and overlapping output
windows. Each Markdown fragment equals its native listing and reconstructs the verified
reference exactly after removing only region boundary comments.

Both MSA-block and scalar implementations pass 619 public function cases, 6656 private
cases, 33 caller cases and twelve repeated-call sequences each, plus wide-data function
and caller checks. The private suite covers every 16-bit RGB565 encoding for expansion,
every red/green byte pair with coupled blue for packing, and one input with nonzero
discarded bits for each RGB565 bucket. Each exhaustive domain contains 65536 pixels,
checked in groups of 32; another 512 scenes vary counts. This is not all 16777216
RGB888 colors or every multipixel arrangement.

The public checks rotate both source and destination allocations and byte offsets,
dirty outputs, full saved-register canaries, valid stack pointers and supplied return
continuations. Each access byte must fit the selected logical source or destination;
only destination bytes may change, even temporarily. Private stack, owned constants
and equivalent access widths are permitted. Twelve same-session sequences make twelve
calls each with changed pixels, operation and count. High mapped data is covered;
high text and high stack addresses are not claimed.

The full verifier records 83 grading runs and 17346 positive case executions across
repeated variants, observations and models. Twenty-one faulty assembled variants and
the untouched starter are rejected. Faults
target field widths, byte order, bit repetition, unaligned/overwide scalar output,
shuffle state, vector boundaries, zero count, return values, callee preservation and
full pointers. Four dedicated CLI capture models verify exact packed and expanded
bytes, all preview words, guard values, unchanged source and caller pre-store state.
Actual automated IDE pixels and annotation receipts remain pending.

Complete-call work includes setup, controls, groups, tails and return, excluding caller
preparation, assembly, session creation, preview and exit. At eight pixels scalar/MSA
packing takes 149/54 native instructions and expansion 205/55. At 32 it takes 581/120
and 805/148. Short inputs pay for the block guard; 31 pixels can take more work than
32 because of its scalar tail. Overlapping pack loads and expansion stores increase
observed bytes from 24 to 32 per group. The pinned core emits per-byte vector notices;
these are not host bus transactions. Counts neither establish elapsed-time gains nor
serve as exact grading requirements. Scalar and vector strategies share the public
behavioral contract; a separate vectorization objective may require instructor review.

The 47 construction observations are each checked twice: through an isolated function
call on unaligned slices and through the student caller using the published input
recipe. The caller checks also verify pre-store count words, exact scratch/lane values,
output prefixes and unchanged original input. Public recipes name the step, source
RGB values, count, focused function and checkpoint. Native instructions explain how
to replace only the initializer prefix while preserving arena size and guards.


## SHA-256 foundation

The independent native entry drafts/sha256.tex is checked separately from main.tex.
Two private implementations pass 878 main cases, 15 repeated/synthetic-context cases
and 9 measured cases each, plus 31 focused cases each with both code and data above 4 GiB.
The 35-run verifier records 1866 positive executions and 27 rejected fault controls.
Two separate register-allocation checks verify the first abc compression round.
Sixteen ordinary CLI runs use real files and compare output bytes with hashlib;
controlled short reads and errors are exercised through explicit fixtures separately.
A Python arithmetic oracle agrees with hashlib on 16 selected messages and supplies
worked schedule/round values. No enormous file is claimed from synthetic count tests.

The public measured-work receipt separates initialization, update and finalization;
one schedule call and one compression call are also measured. Each boundary starts
at function entry and stops at the supplied return, excluding preparation, sessions,
assembly and IO. Counts are simulated instructions, not host latency or grading limits.
The cumulative exercise distribution is unfinished. PDF tagging and embedded-font
checks do not certify PDF/UA or establish student usability.


## SHA-256 cumulative construction

The native draft now includes 47 steps with 38 authored regions and 58 exact recipes.
Private verification checks each prepared source image and both direct-offset and caller
execution, then checks the exact full reconstruction and equivalently framed alternative.
The 64 grading runs contain 1978 positive executions. A separate control run adds 10 grading
runs and 70 positive executions for six unedited exits, wide code/data and the first-round
illustration, plus six ordinary host-file runs through the public preparation helper.
These are 2048 positive grading executions in 74 runs. Earlier prototype fault controls
remain provenance; package-specific equivalence and fault controls are still pending.

The grader's readBytes(address,length) overload, added in MARS revision 0e1d26dd, inspects
full-address mapped bytes including private stack frames without simulated accesses or
backstep changes. All 100 CLI tests pass, including both byte orders, unaligned and wide
snapshots, detached copies, range failures and retained history. A large snapshot encoded
as a compact hex string keeps generated Kotlin checks below JVM method-size limits.

The work table now reports the current cumulative frame/return paths for both algorithms.
The source, recipes, CLI/JDK, normal returns and memory ownership are pinned in local receipts.
The independent draft still awaits public/instructor packages and main integration.
Four planned light-theme figures have verified numerical observations and no IDE pixels.
The substantial author rewrite and independent PDF accessibility work remain pending.


## SHA-256 distribution and main integration

Chapter 25 is now integrated in main.tex, with separate public and private packages.
The student ZIP contains 199 files, 43 guided checks and four final check scripts.
The exact native include sequence is retained as a source snapshot. The public
final arithmetic model uses separate Int wraparound logic; private prototypes use
masked Long arithmetic, and complete digests are compared with JDK SHA-256.

The package verification records 173 grading runs and 9,896 positive executions,
including cumulative construction and six ordinary host-file runs recorded separately.
Two complete implementations pass, as do controls with wide code/data, private readonly
constants, smaller file reads and different unused context contents. Thirty-three
fault variants and the unfinished starter are rejected. Private final checks include
3,341 cases per implementation plus two additional 4,096-byte file cases each.
These counts describe tested cases, not exhaustive correctness or student usability.

Repeated file calls test fresh hash state, descriptor cleanup and preservation of
an existing digest after failure. MARS revision 3f69e767 adds the explicit firstDescriptor
fixture option: descriptors 7 and 31 expose implementations that assume descriptor 3.
The default remains 3 and ordinary host IO is unchanged. All 101 CLI tests pass.
The book tooling passes 33 tests and the syntax renderer passes seven tests.

The standalone chapter builds to 69 pages with 51 assembly blocks. The manuscript
builds to 1,011 physical pages with 628 assembly blocks and 34 native described figures.
All assembly fragments extract from the PDFs; all SHA fragments fit single pages.
Embedded fonts remain Minion Pro, Noto Sans Math and JetBrains Mono. Sixty-four SHA
pages are pixel-identical to the previously reviewed cumulative draft; changed pages
4,5,6,7,69 were reviewed at full resolution, together with the manuscript chapter
opening, final page and updated contents. Existing chapters were reviewed in prior steps.
Tags, table headings and figure descriptions are inspected; PDF/UA certification and
independent accessibility review remain outstanding. Four SHA IDE figures remain plans.
The author requested substantial exposition and length edits after all 35 drafts exist.


## ChaCha20 native foundation

The independent entry drafts/chacha20.tex contains 14 native sections, 12 assembly
listings and one described native lane-selection figure. Main integration and the
cumulative exercise package remain pending. Separate scalar and MSA prototypes pass
217 cases each at normal addresses and with both code and data above 4 GiB: 868
positive executions in four grading runs. Nineteen deliberately faulty variants are
rejected. Each variant is warning-free assembled before grading.

An independent Int-arithmetic model checks arbitrary-state quarter-rounds and blocks;
JDK ChaCha20 checks actual whole-message transforms through 4096 bytes. The domains
include bit patterns, unaligned byte buffers, exact in-place operation, counter values
with bit31 set, the last counter, rejected requests and no-access zero length. Access
traces and snapshots inspect caller ownership; saved registers retain their high bits.
The Python masked-integer oracle verifies all four illustrated quarter-round states,
the published serialized block, shuffle inverses and counter-table rows. Synthetic
limits do not claim large messages were processed.

Measured normal-return block work is 2656 scalar instructions with word rotates,
3296 with shifts/ORs, and 510 with MSA rows and shuffles. This compares specific
prototypes, including scalar pointer setup and nested calls; it is not a host-time
or hardware-cycle speedup. Both block entries are checked against the same contract.
The real IntelliJ highlighter exports 624 distinct snippets after adding this chapter.
All three authoring-plugin tests, 33 book-tooling tests and seven renderer tests pass.
The native PDF builds with Minion Pro, Noto Sans Math and JetBrains Mono. PDF tags
and a visual review do not establish PDF/UA certification or student usability.
Actual IDE screenshots, cumulative packages and the deferred author rewrite remain.

## ChaCha20 cumulative construction

The native draft interleaves 42 steps and 36 authored edits with the retained foundation.
It has 42 quizzes, 84 progressive hints, 54 exact recipes and four planned light-theme
figure slots. The supplied scalar and wrapper frames distinguish unallocated and
allocated development paths. Post-call guards propagate incomplete nested routines.
The public preparation helper retains authored regions and refuses existing outputs.

The construction verifier warning-free assembles all 36 partial programs and all 54
prepared sources. Each recipe checks its exact prepared data, supplied caller path
and an offset-function path, including named intermediate registers/vectors and caller
ownership. It checks the exact full reconstruction, the complete alternative, four
unedited development paths and complete reference code/data above 4 GiB. Those 61
grading runs contain 825 positive executions. A separate control verifier adds the
current alternative at wide code/data addresses and six caller/direct checks proving
propagation from missing quarter-round, scalar-block and MSA-block bodies. The total
is 65 grading runs and 1048 positive executions. Previous 19 foundation fault controls
remain provenance; package-specific equivalence and fault controls are still pending.

The completed cumulative block counts are 2816 scalar rotates, 3456 scalar shifts/ORs,
and 510 MSA instructions. The scalar difference from the earlier prototypes is 160
instructions from the eighty retained branch/no-op status guards. Frame saves/restores
are included, setup/session/assembly/host time excluded. No exact count is a free-writing
grade requirement. All 34 book-tooling tests, seven renderer tests and three IntelliJ
highlighter tests pass; the actual exported corpus now contains 663 distinct snippets.
The native chapter has 53 highlighted listings. Actual IDE pixels, student/instructor
distributions, main integration, the deferred author rewrite and independent PDF
accessibility validation remain unfinished.

The expanded chapter builds to 58 pages. All 53 assembly listings extract literally
and each fits on one page. All 58 pages were visually reviewed in four-page sheets,
with full-resolution checks of pages 20, 21, 46, 55, 57, 58. After adding the additional
recipe table, pages 1 through 54 are pixel-identical to the reviewed render. No overflow,
font substitution or missing-glyph warning remains. Tagged headings, table headers,
math and the native figure description are inspected; PDF/UA certification remains
outstanding. Native diagrams and planned IDE capture slots are distinguished.


## ChaCha20 distribution and main integration

Chapter 26 is integrated in main.tex with separate public/student and private/instructor
packages. The deterministic student ZIP contains 176 files, 36 guided construction
checks and six completed-interface suites. Native snapshots retain the exact include
sequence; descriptors do not regenerate edited prose. Private quiz keys include
explanatory feedback. Instructor archives embed the exact student ZIP and verification
receipt. A default preparation retains the unfinished starter; a step-specific guided
check is run against a working copy completed through that step.

The final package verifier records 165 grading runs and 5,748 positive executions,
including 97 construction runs with 933 positives and four additional control runs
with 223 positives. The final domains include 1,930 private cases per implementation.
The public arithmetic model uses masked Long values and explicit quarter-round locals,
independent of the private prototype's Int wraparound/indexed updates; whole-message
comparisons use JDK ChaCha20. These are bounded finite checks, not exhaustive proof
or cryptographic certification. The 31-bit/32-bit boundary and rejected large counts
do not claim that enormous messages were read or transformed.

Two complete implementations pass public and private domains. Wide code/data, changed
scalar/vector allocation, larger private frames, an owned rotate constant and owned
temporary workspace also pass the public functional domains. Caller ownership protects
all supplied objects while allowing candidate-owned gaps. Thirty-five faulty variants
and the unfinished starter are rejected. The hidden advancing counter passes single
calls but fails repeated-call checks. Same-value source/state stores are detected by
access traces, even though byte snapshots alone would miss them.

All 35 book-tooling tests and seven renderer tests pass. No assembly listing changed
in this packaging step: the actual 663-snippet IntelliJ export remains unchanged.
The standalone chapter builds to 58 pages with 53 single-page assembly listings. The
manuscript builds to 1,069 physical pages with 681 literal assembly listings and 35
described native figures. Embedded fonts remain Minion Pro, Noto Sans Math and
JetBrains Mono. Fifty-five chapter pages are pixel-identical to the previously
reviewed cumulative draft; changed pages 3, 4, 58 were reviewed at 1500px along with the
manuscript chapter opening, final page and contents entry. Prior chapters were reviewed
in earlier steps. Tags, table headers, math and figure alternatives are inspected;
independent accessibility validation and PDF/UA certification remain outstanding.
Four ChaCha IDE figures remain plans. Remote progression and the substantial
author-requested exposition/length edit remain unfinished.


## RSA arithmetic native foundation, 18 November 2026

The directly edited standalone entry is drafts/rsa-arithmetic.tex. Seventeen connected
sections develop unsigned gcd, the inverse example, scalar modular power, four-limb
representation, full eight-limb multiplication, modular addition with its129th bit,
reduction, modular multiplication and full128-bit exponentiation. Native tables and a
described limb diagram accompany selected code. No earlier chapter was rewritten.

Two private implementations use opposite exponent scan directions and agree with
BigInteger. Each passes423 cases at ordinary addresses and423 with code/data above
4GiB:1,692 positive executions. Twelve faulty variants are rejected, covering signed
remainder, sign-extended limbs, truncated products, lost carry/borrow, uninitialized
output, exponent transfer, narrow pointers, preserved high bits and invalid-path reads.
A separate Python integer model verifies the numerical prose anchors. Checks exercise
four-byte-aligned offsets, poisoned private stack, shared inputs, permitted in-place
addition, zero-modulus preflight and sequential encryption/decryption in one session.
Instruction-work counts include ordinary function frames and nested calls; they are
neither host timings nor exact-count acceptance rules. No upstream grader change was
necessary for these interfaces.

All35 book-tooling tests, seven renderer tests and three actual IntelliJ highlighter
Jupiter tests pass. All26 existing student distributions validate and rebuild; the
ChaCha archive retains its previous digest. The existing663 highlight records are exact;
eight records were appended for this foundation. The standalone PDF has15 pages and
eight complete literal assembly fragments, each on one page. Embedded fonts are Minion
Pro, Noto Sans Math and JetBrains Mono. All15 rendered pages were reviewed, with the
interface table, limb/carry figure, product and final scheme explanation inspected at
1200px. Tags, formula structure, table headers and the figure alternative are present;
this is not independent accessibility validation or PDF/UA certification.

The manuscript still contains26 packaged chapters. RSA cumulative construction, student
starter and public checks, private package exports, quizzes and future capture recipes
remain next. Actual automated light-theme IDE pixels, remote progression, student
usability review and the author's deferred substantial rewrite remain unfinished.


## RSA cumulative construction, 18 November 2026

The native chapter now interleaves 42 steps with its retained exposition. It contains
35 authored regions, 42 quizzes, 84 progressive hints, 43 exact recipes, supplied frame
and restore excerpts, and four planned light-theme IDE figures. Recipe metadata names
both direct-function and supplied-caller checkpoints. Preparation changes declared data
arenas and mode while preserving authored source and existing output files.

The independent cumulative verifier assembles each intermediate program, checks every
prepared image, and checks its caller and offset-function views. It verifies intermediate
registers, private values, complete output bytes, sentinel ownership and full saved state.
Exact reconstruction is checked against the private reference. Both completed scan
variants pass 423 cases at ordinary addresses and again above 4 GiB. Seven starter
entries return safely. These 54 runs contain 1,842 positive case executions. A separate
10-run control suite adds 20 executions, testing zero-access starter paths and nested
unfinished-work propagation at wide addresses. The current total is 1,862 positive
executions across 64 runs. Earlier prototype mutation tests remain separate evidence;
no current mutation coverage is inferred from them.

The instruction-work table now uses the cumulative implementations and includes nested
status guards, normal frames and returns. For base 65, exponent 17, modulus 3233, the
right-to-left implementation executes 12,560 simulated instructions and the fixed
left-to-right implementation 47,153. These are neither host timings nor acceptance
thresholds. Completed-interface checks accept both implementations.

All 37 book-tooling tests, seven renderer tests and three actual IntelliJ highlighter
Jupiter tests pass. The previous 671 highlight records are exact; 41 records were
appended, with two of the new listings reusing existing exports. All 26 existing student
packages validate and rebuild without changing the previous ChaCha archive digest.
The RSA draft builds to 62 pages with 51 complete literal assembly fragments, each on
one page. All 62 rendered pages were reviewed. Embedded Minion Pro, Noto Sans Math and
JetBrains Mono, table headers, formulas and the native limb figure alternative are
present. Accessibility certification and student usability review remain outstanding.

The manuscript still contains 26 packaged chapters. RSA public guided and free-writing
exports, separate instructor distribution and main.tex integration remain next. Actual
IDE captures and annotation coordinates, remote progression and the author's deferred
substantial prose rewrite remain unfinished. Existing chapters were not rewritten.


## RSA student and instructor packages, 18 November 2026

Chapter 27 is included in main.tex and registered as 027-rsa-arithmetic. Separate public
and private distributions retain exact native snapshots, the safe cumulative starter,
42 quizzes with 84 hints, 43 observation recipes, 35 guided checks, nine public
completed-interface scripts and explanatory private quiz feedback. Private grading adds
312 cases combining seeded values and modulus edges. Public checks use independent
BigInteger results, explicit caller-object ownership, shifted arguments, preserved high
bits, error preflight, caller paths and repeated calls. Candidate-owned constants and
workspace in gaps between supplied objects remain permitted.

The package verifier records 163 runs and 3,912 expected passing case executions. This
includes 118 isolated-call controls for a deliberately stateful faulty variant; repeated
calls reject that variant. Twenty-five faulty variants and the unfinished starter are
rejected. Both complete algorithms pass public and private domains. Wide code/data,
different temporary and saved registers, larger frames, an owned constant and owned
scratch also pass all 122 public cases. Same-value input stores fail ownership traces,
while candidate-owned scratch writes pass. Guided construction remains specific to its
illustrated intermediate allocation and never defines completed free-writing correctness.

The deterministic student ZIP contains 190 files; a second export is byte-identical.
Its SHA-256 is 043637942a75e053c312a888a50b4de04dfe50dadfa91cb349ca8445f7c40524.
The private instructor ZIP embeds that exact student ZIP and verification.json. The
extracted student checks and default/explicit-source preparation paths pass; existing
student work remains intact. Private checks, completed assembly and quiz keys are absent
from the student archive. All 38 book-tooling tests and seven renderer tests pass.
The existing 712-record actual IntelliJ export is unchanged; no assembly listing changed
in this packaging step. All 27 package distributions validate and rebuild.

The standalone RSA chapter has 62 pages and 51 single-page literal assembly fragments.
The full manuscript has 1,131 physical pages, 732 literal assembly fragments and 36
described native figures. Embedded fonts remain Minion Pro, Noto Sans Math and JetBrains
Mono. Sixty chapter pages are pixel-identical to the reviewed cumulative draft. Changed
pages 2 and 62 were reviewed at 1200px; the manuscript contents entry, chapter opening
and final page were reviewed at 1500px. Previous chapters were reviewed in earlier
steps. Tagging, table headers, formulas and figure alternatives are present; independent
accessibility validation and PDF/UA certification remain outstanding. Four RSA IDE
figures are still plans, and remote progression, student usability review and the
author's substantial deferred rewrite remain unfinished.


## Base64 native foundation, 18 November 2026

The directly edited standalone entry is drafts/base64.tex. Nineteen connected sections
explain scalar bit regrouping, canonical padding, full-width count/capacity checks,
transactional validation, exact in-place decoding, MSA gathers and masked ASCII
translation, vector compaction and scalar tails. Thirteen selected assembly fragments,
headed tables and a native described24-bit diagram support the worked calculations.
The manuscript remains at27 packaged chapters; the Base64 cumulative exercise and
separate distributions are next. Earlier chapters were not rewritten.

The six interfaces include four scalar/vector whole-buffer entries and two exact
12-to16/16-to12 kernels. Two complete implementations pass1124 cases each at ordinary
addresses and again with code/data above4GiB:4496 positive executions. Twenty-four
faulty variants are rejected by focused controls. Independent JDK encode/decode with
canonical re-encoding checks, plus Python integer/vector models, verify the outputs.
The domain includes all256 text octets, all64 alphabet symbols, exact and short output
capacity, high-bit unsigned capacity/counts, byte-unaligned buffers, exact in-place
operation, preserved high bits, actual same-session round trips and error-then-success.
Read/write tracing detects oversized vector footprints and same-value input stores.
No upstream checking function was missing.

Instruction-work observations include full validation and ordinary frames. For4096
raw bytes, scalar/MSA encoding uses65598/43096 simulated instructions; decoding the
5464-character canonical result uses322560/189475. The direct vector kernels use
108/132 instructions. The table-classifier alternative's scalar decoder takes330576
instructions for that text, showing why a lookup is not assumed faster for this
fixture. Counts are neither host timings nor grading thresholds. The chapter describes
biased range tests and an aligned exact-word-copy path as further experiments; those
optimizations are not claimed as implemented.

All38 book-tooling tests, seven renderer tests and three actual IntelliJ highlighter
Jupiter tests pass. All27 existing student packages validate and rebuild. The previous
712 highlighter records are exact and13 new exports were appended. The standalone PDF
has16 pages and all13 literal assembly fragments remain on single pages. All16 rendered
pages were reviewed; the final bounded prose edit changed only pages15 and16, which
were reviewed individually. The interface table, bit diagram and vector-mask explanation
were also inspected at1200px. Embedded fonts remain Minion Pro, Noto Sans Math and
JetBrains Mono. Tags, headed tables, formulas and the figure alternative are present;
independent accessibility validation and PDF/UA certification remain outstanding.
Cumulative packages, actual automated light-theme IDE figures, remote progression,
student-usability review and the author's deferred substantial rewrite remain unfinished.


## QOI prototype interfaces and variable-length results

The private QOI verification covers bound, validate, decode, encode, load, save and the
connected caller. It audits 480 valid and 127 malformed host fixtures before running the
assembly cases. Complete reference/alternative programs pass 1309 memory cases and 85
file/caller cases each at ordinary and wide addresses. A separate isolated hidden-state
control passes once before same-session cases reject it. No exact instruction count or
encoder byte identity is a correctness gate. Uniform-image size promises retain the
specific run-compression objective while allowing other valid encodings.

MARS commit 3f0f4608 adds readRegister(name): Long to the assignment DSL and rejects empty
register names consistently. The 109-test CLI suite passes, including focused checks
for full-width numeric/named snapshots, invalid names, stopped execution and retained undo. Encoder output lengths are checked
as Long before conversion to a bounded byte count. Memory traces are validated against
that returned count, including same-value writes into unused destination capacity.
The full extra-readable-byte contract of the current write syscall is retained.

The repeatable private verifier also compiles the upstream QOI C decoder against the
pinned header, checks actual assembly-produced byte streams and exercises normal host
files. The earlier Pillow run-cache discrepancy remains documented separately; it does
not weaken the strict model or serve as an oracle for the assembly results. Full student
exports, actual light-theme captures, remote instructor policy and independent
accessibility/usability validation remain pending.

## Complete-file encoding and bounded video storage

The private whole-file gate passes 830 positive simulator cases and rejects nineteen
assembled faults. Three complete implementations vary record selection, write chunk
size and private stack allocation. Both native orders and normal/high addresses are
checked; maximum declared staging capacity, 256 source frames and 7,344,152 transported
bytes are normal-layout-only checks. High-address cases provide a real 262,144-byte
staging area rather than declaring a capacity larger than that compacted allocation.

Independent host CRC/command parsing checks every reconstructed color, sequence,
mandatory full frame, header field and exact file end. Two private source families
extend public fixtures. Repeated calls retain real state through a late invalid frame
and fresh successful files. Writer checks cover actual short progress, failed opens,
nondefault descriptors, high-bit counts, partial errors, immutable path/guards and
success-only context updates. The pinned MARS write service reads a lookahead byte;
checks allow exactly that readable suffix without transferring or changing it.

All eighteen native recipes plus an untouched source run directly and through the
caller in both orders, including owned-descriptor cleanup from unfinished regions.
Seven ordinary CLI callers produce four valid files and reject three invalid sources
before creating a target. Independent decoding and every seek target agree with the
original source frames. The pinned CLI SHA-256 is
`2a2df387c0515813f93d55c1c1f58294e037c555528a9e86f77a26b5580fd639`.
All 200 tooling tests and 62 package validation/build commands pass. Actual IDE
captures and independent accessibility review remain pending. Timed playback and
final video packaging are the next construction work; H.263 is still planning only.

## Reload validation and timed sequential playback

The private playback gate passes 648 simulator cases, rejects eighteen assembled
faults and checks six ordinary CLI callers using the actual clock/sleep services.
Three complete implementations run in both byte orders and normal/high layouts.
The alternative changes clock combination/deadline expressions and verifies that
unused upper sleep-register bits do not constrain the 32-bit syscall ABI.
A 256-frame fixture ends with distinct pixels; duration one/thousand, signed-bit
clock transitions, low-half carry, unsigned deadline wrap, suffix starts, bad late
CRCs and exact file end are checked. Repeated calls retain a real successful
image/context through rejected input and then a fresh suffix.

Every accepted picture and requested wait is checked at public syscall boundaries.
The deterministic timeline covers exact deadlines, remaining waits, early wakeup,
oversleep, catchup and backward observed time. No private labels/register layout or
instruction counts are required for completed grading. All twenty-one recipes plus
an untouched control pass, with the initial clock helper tested directly and the
player tested both directly and through its actual file-round-trip caller. Compacted
guided fixtures match both encoder and loader capacities to real 262,144-byte arenas.

The upstream grader extensions are tested in 117 CLI tests, including the packaged
script engine. The pinned CLI SHA-256 is
`64a2d909e8b15e32619086e3880f346c014076789e0e9f4be12baea95f4cbde2`.
All 208 public tooling tests and 62 package validation/build commands pass. Four
successful real-clock callers dump exact final images and consistent counters; two
initial failures retain display/result state. Recorded process time includes startup,
assembly, encoding, I/O, validation, decoding and sleep; it is not isolated playback
or an IDE latency measurement. Final chapter packaging/integration, actual IDE
captures and independent accessibility review remain pending.
