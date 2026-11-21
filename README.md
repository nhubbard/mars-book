# Programming and computer architecture with MARS

Book source and student project packages for the IntelliJ MARS plugin. The text
assumes basic programming knowledge and teaches assembly details explicitly.

The first package, [Calculate twice an integer plus one](packages/001-console-calculation/assignment.md),
contains a barebones starter, 22 instructional microsteps, a multiple-choice question
and two hints at every step, 22 connective prose sections, ten teaching fixtures, two
vector diagrams, public executable checks, and five screenshot recipes.
Read [the first step](packages/001-console-calculation/steps/01.md) or compile the native LaTeX
book draft. The second composition, [Paint one cell and change a byte](packages/002-single-pixel/assignment.md),
adds addresses, word and byte stores, RGB packing, byte order, and memory preservation.
It contains eleven steps, nine prose sections, six teaching fixtures, and three capture recipes.
The third composition, [Draw an eight-byte sprite and its mirror](packages/003-eight-byte-sprite/assignment.md),
adds fixed-width bit reversal, loop invariants, delayed branches, and two-dimensional
addressing. It has twenty steps, twelve prose sections, nine teaching fixtures, and
four capture recipes. The default-workload instruction comparison is verified against
the same output contract for an explicit loop and a native bitswap alternative.
The fourth composition, [Inspect one word at two widths](packages/004-bit-inspector/assignment.md),
explains signed and unsigned interpretations, hexadecimal and binary notation, sign
and zero extension, word arithmetic, and full-width evidence. It contains sixteen
steps, twelve prose sections, nine teaching fixtures, and four planned IDE figures.
The fifth composition, [Count through hexadecimal glyphs on Digital Lab](packages/005-seven-segment-counter/assignment.md),
develops lookup tables, bounded windows, byte-port transactions, and execution-history
checks. It has seventeen steps, twelve prose sections, nine fixtures, and four planned
IDE figures.
The sixth composition, [Draw a square with coordinate feedback](packages/006-marsbot-square/assignment.md),
explains device commands, feedback polling, screen axes, instruction clocks, stopping
bounds, and pen-trail evidence. It has twenty steps, twelve prose sections, eight fixtures,
and four planned IDE figures.
The seventh composition, [Draw lines with an integer error accumulator](packages/007-bresenham-line/assignment.md),
develops canonical tie rules, paired coordinate decisions, error invariants, exact
framebuffer overlays, and scoped instruction-count comparisons. It has twenty-four
steps, thirteen prose sections, ten fixtures, and four planned IDE figures.
The eighth composition, [Evolve Life with a stable source generation](packages/008-life-generations/assignment.md),
explains synchronous updates, dead halos, alternating buffers, representation strides,
and public generation checkpoints. It has twenty-eight steps, thirteen prose sections,
ten fixtures, and four planned IDE figures.
The ninth composition, [Visualize insertion and selection sort](packages/009-sorting-visualizer/assignment.md),
explains sorted-prefix invariants, permutations, duplicate identities, a tested leaf
renderer, and native instruction/branch measurements. It has twenty-seven steps, thirteen
prose sections, ten fixtures, and four planned IDE figures.
The tenth composition, [Explore a maze with a breadth-first frontier](packages/010-maze-explorer/assignment.md),
teaches coordinate bounds, discovery-time marking, a bounded FIFO queue, predecessor
chains, and shortest-path reasoning. It has thirty microsteps, fourteen connective
passages, twelve teaching fixtures, two native diagrams, and four planned IDE captures.
Its checks accept different neighbor orders and shortest routes. The full [35-project roadmap](docs/roadmap.md) preserves the progression
from console programs to algorithms, bounded assignments, vector kernels, and codecs.

The eleventh composition, [Matrix transpose](packages/011-matrix-transpose/assignment.md),
adds 28 steps, thirteen verified code increments, five connected explanatory passages,
five teaching fixtures, a native tile diagram, and four planned IDE figures. Its
checks accept equivalent registers, stack usage, tile orders, and within-tile schedules
while verifying the stated matrix-access contract. Cache comparisons use the actual
shared core/IDE cache model and separate misses from completed instructions.

The twelfth composition, [Fixed-point ball](packages/012-fixed-point-ball/assignment.md),
adds thirty steps, sixteen verified code increments, seven connected explanatory passages,
six teaching fixtures, two native diagrams, and four planned IDE figures. It develops
signed scaled integers, overshoot-preserving reflection, exact contacts, display rounding,
coherent frame publication, and a separate word-overflow experiment. Its checks accept
equivalent branches, scratch registers, stack usage, and rendering store orders.

## Use the materials

The publication source is native LaTeX in main.tex and includes/. Exercise packages
retain Markdown and JSON for student guides and future guided lessons. Python 3.11 or
newer supports validation, packaging, and annotation tools. Running assembly checks additionally
requires JDK 25 and a built MARS CLI JAR.

```sh
python3 tools/course.py validate
python3 -m unittest discover -s tools -p 'test_*.py'
python3 tools/book.py validate
python3 tools/course.py build
python3 tools/course.py check --cli /path/to/mars-cli.jar --source /path/to/main.asm
```

Student ZIPs are under build/, each with a SHA-256 sidecar. Compile the book with
`python3 tools/book.py build` after following the [font and minted setup](docs/latex-publication.md).
The unmodified starter exits cleanly but intentionally does not pass the assignment.
See [verification](docs/verification.md) for the tested package behavior.

The [expression evaluator](packages/017-expression-evaluator/assignment.md) develops
precedence, unary recursion, checked signed-word arithmetic and ordered diagnostics
through forty-five steps and thirty-four verified assembly edits. Its native chapter
includes stack/grammar/diagnostic fixtures, equivalent parsing strategies, measured
workloads and four planned light-theme captures.

The [tiny bytecode interpreter](packages/018-tiny-bytecode-interpreter/assignment.md)
uses 51 steps and 40 cumulative edits to develop whole-image validation, instruction
boundaries, a bounded operand stack, checked arithmetic and precise guest retirement.
It includes native state/encoding fixtures, two measured implementations, executable
intermediate observations and four planned light-theme captures.

The [RGB888/RGB565 chapter](packages/019-rgb-formats/assignment.md) develops exact
component conversion through 49 steps and 39 cumulative edits, beginning with scalar
loops and adding bounded MSA groups. Three native diagrams explain fields, overlapping
windows and reconstructed colors. Lane observations, measured whole-call work and four
planned capture models accompany the guided and free-writing package.

The [PCM mixing chapter](packages/020-pcm-mixing/assignment.md) develops signed
sixteen-bit saturation through 40 steps and 30 cumulative edits. It includes per-step
construction checks, exact in-place aliases, scalar and MSA behavior, reproducible
input recipes, native mask/waveform diagrams and twelve verified listening files.
Four light-theme IDE captures remain planned.

The [fixed-point FIR chapter](packages/021-fixed-point-fir/assignment.md) develops
exact eight-tap filtering through 47 steps and 36 edits. It includes explicit history,
wide partial sums, signed rounding, public construction/final checks, six reproducible
listening files and five planned light-theme capture models.

The [blocked-matrix chapter](packages/022-blocked-matmul/assignment.md) develops
rectangular products through 50 steps and 42 cumulative edits. It includes exact
accumulator bounds, clipped reduction tiles, 58 observation recipes, public
construction/final checks and four planned light-theme capture models. Its whole-call
measurements distinguish simulated instructions, data notices and cache misses.

The [delta/variable-length chapter](packages/023-delta-varint/assignment.md) is included in the main manuscript.
It explains exact 33-bit deltas, signed mapping, canonical seven-bit groups, item-atomic
capacity handling, malformed-input priorities and measured size/work tradeoffs. Its 60 steps
contain 50 cumulative edits, five supplied excerpts, 60 quizzes, 120 hints and 84 observation
recipes, public intermediate/final checks and separate student/instructor distributions.
Actual IDE figures remain in progress.

The [CRC-32 chapter](packages/024-crc32/assignment.md) explains raw
state, reflected bit updates, lookup construction, chunk composition, file cleanup
and PNG chunk reuse. Its prototype checks use explicit in-memory files, short reads
and read failures. Its cumulative lesson has 34 steps, 24 edits, seven supplied
excerpts, 34 quizzes, 68 hints and 52 exact recipes. It is included in the
main manuscript as chapter 24, with public guided/final checks and separate archives.
All four planned IDE figures now have reviewed native annotations.

The [SHA-256 chapter](drafts/sha256.tex) includes 47 steps, 38 cumulative code edits,
six supplied entry/exit pairs, 47 quizzes, 94 hints and 58 exact recipes. Its student
package has 43 guided checks and completed-interface checks; private materials retain
implementations, additional grading cases and explanatory quiz feedback. It is included
in the main manuscript as chapter 25. Four reviewed IDE figures now distinguish source
bytes, schedule words, working state, final padding and ordinary file output.

## Author the book

The active [composition goal](docs/composition-goal.md) is full instructional chapters.
The first twenty-eight chapters are now composed through ordinary LaTeX input/include files.
Edit them directly and insert author passages wherever they belong. Task metadata
does not constrain the publication.

- [Writing style](docs/writing-style.md) applies the requested Wikipedia editorial guidance.
- [Editorial workflow](docs/editorial-workflow.md) preserves human passages in the directly edited book.
- [Package format](docs/package-format.md) defines the structured lesson source.
- [Native LaTeX publication](docs/latex-publication.md) defines final typesetting and accessibility requirements.
- [Capture workflow](docs/capture-workflow.md) defines light-mode IDE captures and annotation generation.
- [Interactive design](docs/interactive-design.md) describes future guided and free-writing modes.
- [Publishing](docs/publishing.md) explains the public/student and private/instructor split.
- [Author a package](templates/package/README.md) provides the repeatable authoring checklist.

## Current delivery status

The first twenty-eight text packages, starters, public checks, deterministic student archive builder,
native LaTeX composition, IntelliJ-derived minted renderer, and annotation renderer are implemented. The private companion
repository contains reference and alternative implementations, quiz keys, private checks,
and teaching notes. This public repository has no dependency on private content to build.

IDE screenshot capture will be driven by a separate authoring plugin depending on MARS.
The separate plugin now exports actual IntelliJ language colors. Its screenshot capture
and lesson runner are still pending, and no lesson screenshots are included yet. The book uses labelled placeholders for the
planned figures in each chapter. Remote download, an in-plugin lesson runner, and instructor policy
controls are documented future capabilities. The tested template uses Minion Pro prose,
Noto Sans Math equations, and JetBrains Mono code. Frontmatter is editable; final author
review and PDF accessibility validation remain. Licensing must be selected before broader reuse;
this repository does not grant an additional license by implication.

The [ChaCha20 chapter](drafts/chacha20.tex) interleaves 42 cumulative steps with the
retained quarter-round, scalar/MSA block and bounded-byte transformation exposition.
Its [student package](packages/026-chacha20/assignment.md) includes the safe starter,
54 recipes, 36 exported construction checks and six completed-interface suites.
Instructor solutions, hidden domains and quiz feedback remain in the private repository.
The chapter is included in main.tex as the twenty-sixth text package. Four light-theme
IDE figures remain plans; remote progression, the deferred author rewrite and independent
accessibility validation are still pending.


The [RSA arithmetic chapter](drafts/rsa-arithmetic.tex) now has 42 cumulative steps,
35 authored regions, 43 exact recipes and 51 IntelliJ-highlighted listings. Its
[student package](packages/027-rsa-arithmetic/assignment.md) includes public guided
checks and nine completed-interface scripts. Separate private material supplies two
complete implementations, 312 additional cases and quiz feedback. The chapter is
included in main.tex as the twenty-seventh text package. Actual IDE figures, remote
progression, the deferred substantial author rewrite and independent accessibility
validation remain pending.


Chapter 28, Base64, now has a native cumulative composition with 43 steps, 37 authored
regions, 52 exact recipes, 43 quizzes and 86 progressive hints. Package `028-base64`
includes public guided checks and seven completed-interface domains covering canonical
text, exact capacities and footprints, caller boundaries and same-session reuse. Private
solutions, seeded cases and quiz feedback remain in the instructor repository. The four
light-theme figure plans are unfulfilled; actual IDE captures, remote progression,
independent accessibility review and the author’s substantial prose edit remain pending.


The PCM WAV filter is now the thirty-first native chapter and draft exercise package.
`packages/031-pcm-wav` keeps its assignment and reading text in LaTeX, with JSON used
only to index exercises and planned assets. It provides 64 checkpoint checks and seven
completed-interface domains. Guided checks remain usable after later regions are
completed; free-writing checks accept equivalent exact implementations. Instructor
solutions and keys are separate and private. The four IDE capture plans are uncaptured.
Substantive prose editing remains deferred until all 35 chapter drafts are complete.

The PNG chapter has a native reading foundation at `drafts/png.tex`, with 143 native
reading and construction files, a pipeline figure, native tables, an actual IntelliJ-highlighted shuffle
excerpt and reproducible RGB8 inputs. Two complete private prototypes now decode
through framebuffer publication. Row checks cover scalar/MSA paths in both byte orders
and address layouts (4400 cases); complete decoding adds 400 cases, including full
512-by-512 output and late-failure preservation. Ten assembled faults are rejected.
Measured complete calls on the repeated-row image fall from 37870732 to 18167820
simulated instructions in MSA mode; the text records the input and boundary. All 133
public tests pass. The MARS grader's byte-order option is committed separately with
110 passing CLI tests. The supplied file caller and first ten construction steps now
include nine editable loader regions, ten quizzes, twenty hints and fourteen observation
recipes. Seventeen further collector edits add twenty observation recipes, an exact
chunk-cursor trace and connected boundary explanations. Both collector algorithms and
the cumulative caller/checkpoints pass 2828 byte-order/address-layout cases; eight
assembled regressions are rejected. Sixteen further edits develop the stored-block
inflater, with a native byte/bit trace and twenty-three recipes. Its scalar and batched
Adler variants, two additional valid frame arrangements, the retained full inflater
comparison and cumulative
checks pass 2512 cases; eleven assembled faults are rejected. Twenty-seven more steps
complete fixed and dynamic Huffman construction, including canonical tables, bounded
bit decoding, overlapping matches and all three length-repeat operations. Thirty-five
public recipes accompany native frame, rank and bit-cursor tables. Completed variants,
cumulative checkpoints and same-session checks pass 6868 simulator cases; fourteen
assembled faults are rejected and ten actual-file runs pass. Thirteen row-reconstruction steps now add all-selector prevalidation, scalar
predictors, bounded MSA Up groups and their tails. Nineteen public recipes and
a native row-window table accompany the text. Three completed implementations,
cumulative direct/caller observations, same-session reuse and full-size callers
pass 2232 cases; nine assembled faults are rejected and twelve actual-file runs
pass. Seventeen final steps add scalar/MSA packing, a native destination-extent
table, two diagnostic observations and final context publication, reaching 100
construction steps. Fifteen new editable regions and twenty-seven recipes accompany
the prose. Completed domains, cumulative calls and session reuse pass 4036 cases;
thirteen assembled faults are rejected. Twelve real-file runs cover ten exact
framebuffer comparisons, including both full-size modes, and two rejected inputs.
The final caller reports `PNG displayed.` after success. Final feedback packaging,
PNG distributions, integration and actual IDE captures remain next. The main manuscript
retains 32 packaged chapters; the substantial author editing pass remains deferred.


The small lossless-video chapter now has a native reading draft at
`drafts/lossless-video.tex`. It defines the MLV1 exercise format and explains full
frames, XOR skip/change commands, record CRCs, validation before mutation, indexing,
seeking, encoder choices and presentation deadlines. Four reproducible 64-by-64
animations include unchanged frames, an all-pixel change and optional extra full
frames. An independent host model checks 22 reference frames and every seek target,
checks bitwise CRCs and rejects 74 malformed files.

Six cumulative kernel edits, six quizzes, twelve hints and twenty-six recipes develop
scalar/MSA native-word XOR and nonzero-word counting, including exact output aliases.
Completed variants, cumulative calls and reused sessions pass 3944 simulator cases;
eleven assembled faults are rejected and ten ordinary caller runs pass. For 4096
words, these construction sources use 53269 scalar and 29721 MSA instructions; the
text preserves that isolated boundary and does not claim a whole-codec speedup.
All 141 public tests pass. Assembly file-codec construction, final feedback and
distributions, integration and actual IDE captures remain unfinished.

The video construction now continues through step 23 with a scalar exact-record
validator and applier. Seventeen native lessons, seventeen quizzes, thirty-four
hints and thirty-seven public recipes explain bounded input windows, native
context state, bytewise big-endian reads, CRC coverage, complete command validation
before mutation, RGB word construction, delta application and publication. Public
scaffolds and fixture controls are in `drafts/video-record-construction`; completed
algorithms and keys remain private. The private gate passes 1012 simulator cases,
rejects thirteen assembled faults and verifies eight ordinary caller runs.
Equivalent CRC and private-frame implementations are checked. On the measured
records, a mask-based CRC uses about 4–5% more simulated instructions than the
conditional version; the native text records the complete-call measurement
boundary. All 148 public tests and the existing package validation/build commands
pass. The whole-file codec and playback construction, video distributions, actual
IDE captures, independent accessibility audit and APNG remain unfinished.

The video construction now reaches step 36. Thirteen additional native lessons
explain complete in-memory file validation, nested record calls on scratch, exact
file completion, file-relative offsets and atomic directory/context publication.
The student attachment preserves the record implementation; twenty-five controls
retain instruction text. Its separate gate passes 868 simulator cases, rejects
eleven assembled faults and verifies eleven ordinary callers. Three implementations
are checked in both byte orders and normal/high layouts. Twelve six-call reuse
cases preserve earlier public results across failure. All 155 public tests and
60 existing package validation/build commands pass. The later file loader must
still establish actual filesystem EOF; file I/O, seek, encoder/playback, final
video packaging/integration, APNG and real annotated IDE captures remain pending.

The author has replaced the planned APNG chapter with a restricted, interoperable
H.263 encoder capstone. Its raw digital input will be Python-generated QCIF
(176 by 144), at most 30 fps and at most 10 seconds, with a shorter/lower-rate default
chosen after measurement. Students are assumed fluent in MSA. Complete chapters 33
and 34 before implementing chapter 35; current H.263 work is limited to recorded
planning. The exact raw layout and bitstream feature set remain to be designed.
Substantive editing is a separate goal and will not begin automatically here.

The video reader construction now reaches step 45. Nine native lessons cover
bounded file loading, short reads, private exact-fill EOF probes, descriptor
ownership and loading-before-indexing. Nine quizzes, eighteen hints and twenty-three
recipes accompany the regions. Its gate passes 864 simulator cases, rejects ten
assembled faults and verifies eleven actual-host-file runs. Boundary tests check
every transferred byte at the full 7344152-byte capacity, including one-byte
overflow, in both native orders. All 162 public tests pass. Seeking, encoding and
playback remain unfinished; the final H.263 encoder is planning only until both
PNG and lossless-video chapters are complete.

The PNG initial chapter is now included in `main.tex` and exported as student package
`033-png`. Its 90-page native reading has 100 steps, 97 unfinished regions, 100 quizzes,
200 hints, six attachable construction stages and eight completed-interface feedback
domains. Public feedback contains 452 cases; the instructor companion adds 1,944
private cases, with separate test storage and no prescribed private frame offsets.
Both complete implementations pass the extracted 2,396-case grading set. The public
starter remains unfinished and the completed decoder and quiz keys remain private.
All 168 tooling tests and 62 CI package validation/build commands pass. Tagged tables,
figure descriptions, checkpoint text, embedded fonts and the rendered reading have
been checked. These checks do not certify accessibility or student usability. Five
actual IDE captures, remote progression/hint enforcement and independent accessibility
review remain pending. Chapter 34 seeking, encoding, playback and final packaging are
next. The H.263 capstone remains documented planning until chapters 33 and 34 are
complete; the substantive edit remains a separate future goal.

The lossless-video construction now reaches step 56. Eleven new native lessons
explain nearest-full seeking, bounded directory offsets, private record-sequence
state, scratch replay and final image publication. Eleven quizzes, twenty-two hints
and twenty observation recipes accompany the unfinished student regions. Its gate
passes 1,028 simulator cases across four implementations, both native orders and
ordinary/high addresses, rejects ten assembled faults and checks eight actual-file
callers. Every recipe and an unfinished control are checked directly and through the
load/index/seek caller. Sixteen four-call reuse cases preserve earlier images and
metadata through rejection. With the rest of the implementation held constant, an
MSA final copy saves 21,504 simulated instructions, about two percent of the measured
complete seeks. The native text preserves the entry/return boundary and excludes
loading, initial indexing and presentation. All 175 public tests pass. Encoder and
playback construction, final video feedback/archives and manuscript integration,
actual IDE figures and independent accessibility verification remain unfinished.
H.263 stays at planning until chapters 33 and 34 are complete. Substantive editing
remains a separate future goal.

The lossless-video construction now reaches step 68 with a full-record encoder.
Twelve new native lessons explain complete source admission, numeric RGB extraction,
fixed wire order, noncontiguous CRC coverage and success-only prediction-state
publication. Twelve quizzes, twenty-four hints and twenty-four recipes accompany
unfinished student regions and a caller that decodes the newly encoded bytes again.
Two gates pass 712 simulator cases, reject twelve assembled faults and verify eleven
ordinary encode/decode callers. Three implementations run in both native orders and
normal/high address layouts. Private seeded frames and channel boundaries extend
the known fixtures. All 183 tooling tests pass. Delta selection, complete-file
encoding, timed playback, final video feedback/distributions and manuscript integration
remain unfinished. Actual IDE captures and independent accessibility review remain
pending; H.263 is still planning only until chapters 33 and 34 are complete. The
substantive editing pass remains a separate future goal.

The lossless-video construction now reaches step 83 with full/delta record selection.
Fifteen native lessons add zero/changed run measurement, exact record admission,
variable-length emission and prediction-state publication. Fifteen quizzes, thirty
hints and thirty-three recipes accompany the student scaffold, which imports earlier
XOR work while preserving its data and local references. The gate passes 1,144
simulator cases, rejects fourteen assembled faults and checks fourteen ordinary
callers. Completed grading independently parses records, checks host CRCs and
compares reconstructed colors while accepting valid alternative choices; guided
observations retain the illustrated greedy policy. A controlled scalar/MSA XOR
comparison saves 23,548 simulated instructions when XOR runs, about ten percent for
the measured sparse records and two percent for full fallback; required full frames
bypass it. All 192 tooling tests pass. Complete-file encoding, timed playback,
final video distributions/feedback and manuscript integration remain unfinished.
Actual IDE figures and independent accessibility review remain pending. H.263 remains
planning only until chapters 33 and 34 are complete; substantive editing is separate.


The lossless-video construction now reaches step 101 with complete-file encoding
and bounded file storage. Eighteen native lessons, eighteen quizzes, thirty-six hints
and eighteen recipes connect packed source frames to a global header and variable
records, then to descriptor ownership, short-write progress and success-only result
publication. The attachment reuses the existing staging arena; old file/index state
is invalid while encoding overwrites it. Caller context survives encoding failure,
while temporary staging and working images may change. The pinned write service's
one-byte lookahead is documented and checked without transferring that guard.

The gate passes 830 simulator cases, rejects nineteen assembled faults and verifies
seven ordinary callers. It accepts equivalent representations across three complete
implementations, both native orders and normal/high address layouts. Private generated
animations extend the published fixtures; exact maximum allocation/transfer boundaries
use the larger normal layout. All eighteen recipes and an untouched control run
directly and through the example caller. Successful host files are independently
parsed, fully decoded and sought through at every target. All 200 tooling tests and
62 course validation/build commands pass. The native reading copy has 101 observation
passages and nineteen tagged tables. Revalidation after reload, timed playback,
final video feedback/distributions and manuscript integration remain unfinished.
Actual automated light-theme IDE figures and independent accessibility review remain
pending. H.263 remains planning only until chapters 33 and 34 are complete; the
substantive editing pass remains a separate future goal.


The lossless-video construction now reaches step 122 with reload validation and timed
sequential playback. Twenty-one new native lessons, twenty-one quizzes, forty-two
hints and twenty-one recipes explain clock halves, independent initial seek,
anchored deadlines, positive remaining waits, early wakeup, lateness and streaming
failure. A player validates current loaded bytes before clock/display effects and
returns actual accepted progress on failure while retaining final context outputs.

The gate passes 648 simulator cases, rejects eighteen assembled faults and checks
six ordinary real-clock callers. Completed grading inspects every accepted image
at public syscall boundaries, using scripted 64-bit time and nonblocking sleep.
Three complete implementations run in both orders and normal/high layouts, including
256 frames with a distinct final picture, duration bounds and persistent-state
retries. All twenty-one recipes plus an untouched control are checked; the first
two helper recipes run directly and player recipes run directly/through the caller.
Real-clock runs dump final pixels and counters; their lateness observations are
separate from logical timing and do not measure IntelliJ repaint or startup latency.

The required MARS grader extensions provide scripted timing, public syscall-boundary
inspection and nonexecuting function entry for repeated-call checks. All 117 CLI
tests pass. All 208 public tooling tests and 62 course validation/build commands
pass. Final video feedback/distributions and manuscript integration remain pending,
as do actual automated light-theme IDE captures and independent accessibility review.
H.263 remains planning only until chapters 33 and 34 are complete. Substantive
editing remains a separate future goal.


The lossless-video initial text and distribution draft is now registered as
`034-lossless-video` and included as chapter 34 in `main.tex`. Its nine construction
stages supply 122 steps, 244 hints, published numeric fixtures, a cumulative unfinished
starter and a smaller entry-only starter for free writing. The closing native
assignment explains how the two early threads join, how to retain prior edits,
which observations belong to each stage, and how to run completed feedback.
Data-only example compaction preserves instruction text and supported input limits.

The relocated student archive now contains 308 files and eleven public domains
with 258 cases. Private checks and 122 quiz keys remain in the instructor repository.
The completed package gate passes 6,264 cases across three implementations, including
one with submission-owned constants and additional 4,096-byte entry frames. It
rejects both unfinished starters and five assembled faults. Extracted runners pass
92 public and six selected private cases and protect existing reports. Two ordinary
real-clock callers produce files that an independent decoder recovers to all eight
original frames.

All 241 public tooling tests and 28 authoring-plugin Jupiter tests pass. The native
video reading has 112 pages, with 122 observations, 22 tagged tables, 79 header cells
and seven described native figures. Six are actual automated light-theme IDE plates;
the other is the native seek diagram. Source images remain unannotated, with native
LaTeX callouts and semantic descriptions. The build rejects oversized floats as well
as overflows, missing glyphs and font substitutions. These checks do not certify
PDF/UA compliance or student usability.

Chapters 33 and 34 have initial native text/packages in the manuscript. All five
PNG and all six video figure plans are verified, with original light-theme IDE
screenshots, native LaTeX annotations and retained receipts. The PNG chapter has
97 pages, 100 construction observations, 17 tagged tables, 68 header cells and six
figures including its pipeline diagram. All 97 body pages match the 1587-page
manuscript in text and position. Its 253-file student package and separate private
materials pass 7188 grading cases across three valid implementations; the unfinished
starter and three assembled faults are rejected.

The screenshot implementation remains in the separate authoring plugin depending
on MARS. Earlier chapter capture plans and independent accessibility review remain
open. H.263 has an opening initial chapter draft; its restricted interoperable
profile and Python-generated QCIF input constraints remain documented. Substantive editing
is the separate future goal requested by the author.


Chapter 35 now has an opening native draft in `drafts/h263.tex`, with thirteen
pages covering raw QCIF input, exact timing, reconstructed references and ten SAD
construction steps/quizzes. The Python fixture generates eight planar Y/Cb/Cr
pictures at 5000/1001 pictures/s, with a defined duration of 1.6016 seconds. A strict
independent host baseline H.263 encode/decode preflight recovered all eight pictures
from 2194 coded bytes; it is not a student encoder implementation.

The first scalar/MSA SAD kernel has a runnable unfinished caller, instruction-
preserving input preparation and 32 public completed-entry feedback cases. Larger
private checks pass 104 cases per implementation and reject four wrong kernels.
Public feedback accepts two references and a valid independent 4-KiB-frame layout,
rejecting the starter and three arithmetic/access faults. The MSA listing uses the
actual IntelliJ highlighter; all 767 prior records remain identical and one is added.
Motion search, transforms, quantization, reconstruction, bitstream emission and
full chapter grading/distribution/captures remain unfinished. The complete book
goal remains active; substantive editing has not begun.

The next tested draft step extends chapter 35 to 22 pages and twenty construction
observations/quizzes. Bounded full-pixel search has its own unfinished caller,
data-only preparation and 56 public completed-entry cases. Two traversals pass
160 private cases each; public feedback accepts three layouts and rejects five
wrong searches and the unfinished starter. All 247 public tooling tests pass.
The verified worked result is displacement (-2,-2), SAD 2976 and 25 candidates.
This extends the opening material; the complete H.263 encoder is still unfinished.

Chapter 35 now extends to 32 native pages and thirty construction observations
and quizzes. The new prediction/residual stage explains exact baseline rounding,
signed chroma origins, eight-sample access footprints and signed halfword output.
It includes an unfinished Run/Debug caller and 96 public feedback cases. Two
primary bodies pass 184 private cases each; a safe coalesced-read variant and
an independent stack layout are also accepted. Thirty real six-block windows
cover five signed motion vectors. All 253 public tooling tests and 28 authoring
Jupiter tests pass; the new listing uses the actual IntelliJ highlighter. Full
encoder orchestration, transform/quantization, reconstruction and output remain open.

The six-block target-side prediction step extends chapter 35 to 41 pages and
forty construction observations/quizzes. It derives all luma/chroma origins in
assembly, preserves independent plane strides and stages six records before
publishing the complete result. Two bodies pass 212 private cases each; public
feedback accepts three layouts across 64 cases. Eight later-child failure probes
retain caller output after private writes. Twenty completed ordinary callers
exercise five vectors in both modes. All 259 public tooling tests pass. The
ordinary entry explicitly aligns its stack, matching the grading precondition.
Transform/quantization, local reconstruction and full H.263 output remain unfinished.

The transform step extends chapter 35 to 52 native pages and fifty construction
observations/quizzes. It explains DCT normalization, signed fixed-point widths,
unrounded transposed intermediates, final descaling and inverse accuracy. Both
precisions, modes and byte orders passed 480000 actual target inverse blocks plus
48 zero calls; a deliberately wrong rounding bias failed every full cohort.
Two bodies pass 156 private finite cases each, and three layouts pass the 96
public cases. All 267 public tooling tests and 28 authoring Jupiter tests pass.
The new listing uses actual IntelliJ highlighting; prior exports are unchanged.
Quantization, local reconstruction and complete H.263 output remain unfinished.


## H.263 quantization and coefficient reconstruction

The native chapter now has 62 pages and sixty construction observations/quizzes.
Steps 51–60 distinguish a reproducible forward level-selection policy from the
normative decoder reconstruction, derive the dead zone and parity adjustment,
explain separate INTRA DC codes and counts, bound scalar/MSA lane widths, and
prepare the connection to the local inverse. Three new tagged tables bring the
chapter to seventeen tables/63 header cells; the four described native figures
remain. The new prose is ordinary editable LaTeX, not generated metadata.

Both forward and reverse traversal implementations pass 2044 private cases each,
covering all 31 quantizer parameters, both block types/modes/byte orders, signed
extremes and validation/publication boundaries. Three implementation layouts pass
204 public cases each. Five wrong implementations and the unfinished starter are
rejected. Eight completed ordinary callers pass with warnings treated as errors,
and a complete prepared working source passes public feedback with protected
reports. All 276 public tooling tests pass. The independent host model is checked
for every ordinary coefficient/parameter pair and every INTRA DC input.

Exact forward output is a kernel teaching contract, not a restriction on complete
encoders' legal level choices. Full-stream grading must retain equivalent solutions.
No MARS or authoring-plugin source changed, no IDE launch occurred and no new
screenshot is claimed. Closed-loop target reconstruction, syntax/output, whole-encoder
quality/performance grading and chapter distributions/captures remain unfinished.
Independent accessibility review and substantive editing remain separate.


## Integrated H.263 block reconstruction

The native chapter now has 71 pages and seventy construction observations/quizzes.
Steps 61–70 join signed input gathering, forward transform, level selection, exact
inverse quantization, accurate inverse transform and scalar/MSA signed pixel
addition/clipping. Worked constant blocks show the loss explicitly: 130 predicted
from 128 reconstructs 131 at Q=8; the next current 132 must predict from that
reconstruction. Three new native tables bring the chapter to twenty tables and
72 tagged header cells. The four described figures remain native, not screenshots.

Both integrated bodies pass 368 private cases, covering five Q values, both block
types/modes/endians, mixed signs, both sample clippers and independent strides.
Three layouts pass 144 public cases. Eight named repeated-call cases copy actual
prior pixels into the next prediction; 24 implementation-specific injected child
failures retain every caller-result byte after private child work. Five wrong
bodies and the unfinished parent fail. Eight completed composed ordinary callers
pass warnings-as-errors assembly and CLI execution; public feedback accepts a prepared full
working source and protects existing reports. All 286 public tooling tests pass.

The checker permits prior one-unit transform accuracy, checks coefficient
reconstruction exactly from actual selected levels, independently checks the
inverse of that block and requires exact clipped final sums. Equivalent accurate
precisions, iteration orders and private frames remain valid. Student-owned
composition/preparation helpers retain instructions and refuse existing files;
they supply no solution. Screenshot automation stays in the separate plugin that
depends on MARS. No MARS/plugin source changed and no GUI capture is claimed.

This completes the callable eight-by-eight local path. Picture ownership,
macroblock/bitstream syntax, file output, whole-encoder quality/interoperability/
performance checks, chapter distributions and actual IDE figures remain unfinished.
Independent accessibility review and later substantive editing remain separate.


## Bounded H.263 bit output

The native chapter now has 79 pages and eighty construction observations/quizzes.
Steps 71–80 develop field width/value admission, MSB-first order, exact partial-byte
replacement, complete-field capacity preflight, empty writes, cursor commit,
repeated appends, byte-fragment optimization and explicit syntax-layer alignment.
Two additional native tables bring it to twenty-two tables/79 tagged header cells.
The four described native figures remain; no chapter screenshot is claimed.

Both per-bit and byte-fragment bodies pass 1632 private cases covering every width
0..32, every starting offset, zero/one/mixed patterns and both byte orders. Three
layouts pass 160 public cases. Four repeated-chain cases test crossing, zero padding,
empty writes, capacity failure and a retry using the retained context. Five wrong
bodies and the unfinished starter are rejected. Twelve completed/failure ordinary
caller variants plus the unfinished caller pass their expected outcomes. A prepared
working source passes feedback with report protection; all 294 public tooling tests
pass. Four additional target header-packing cases agree with the independent
baseline header inspector; these inspect headers, not complete-stream conformance.

The named unaligned all-one 32-bit append measures 656 entry-to-return instructions
per bit versus 189 per byte fragment in either byte order. This is function work,
not host time or whole-encoder throughput. Different schedules and private frames
remain valid. No MSA is required for serial bit output. Multi-field structures still
need their own capacity preflight or staging; field-level atomicity is not enough.

No MARS or authoring-plugin source changed and no GUI launch occurred. The authoring
plugin remains separate and depends on MARS. Picture ownership, coefficient and
macroblock syntax, file output, whole-encoder quality/interoperability/performance,
chapter distributions and actual captures remain unfinished. Independent accessibility
review and substantive editing remain separate.


## Complete baseline H.263 coefficient payloads

The native chapter now has 89 pages and ninety construction observations/quizzes.
Steps 81–90 develop natural-to-zigzag addressing, complete level admission,
LAST/RUN formation, separate INTRA DC, all 102 compact codes, legal baseline
escapes, chosen-plan capacity admission, private whole-block publication and
independent payload decoding. Five new native tables bring the chapter to
27 tables/101 tagged header cells; the four described native figures remain.
Normative numeric data retains official-source provenance and all 102 records
were cross-checked against an independent codec's numeric code data.

Compact and escape-only implementations pass 916 private cases each. Every compact
entry is exercised with both signs, block types and byte orders. Three layouts
pass 100 public cases. Four repeated-block cases retain the actual context; twelve
implementation-specific child-failure probes retain caller bytes/count after
private child work. Five wrong emitters and the unfinished starter fail. Sixteen
completed ordinary callers and prepared full-source feedback pass, with report
protection. All 304 public tooling tests pass. Grading decodes the emitted payload
back to every natural-order level, accepting different valid syntax choices.

Sixteen controlled target work cases vary code selection and bit packing independently.
With the same byte-fragment child, compact versus escape-only costs 3206/2952 guest
instructions for the worked two-event block and 12861/18550 for a dense alternating
AC block. Sizes are 16/52 and 199/1394 bits. Sparse linear lookup can cost work while
saving bits; the dense case reduces both. These are complete finite emitter work
observations, not host time or whole-encoder throughput, and not grading thresholds.

No MARS or authoring-plugin source changed and no GUI launch/capture occurred.
The authoring plugin remains separate and depends on MARS. Macroblock/motion/type
syntax, picture-reference ownership/publication, file output, whole-encoder quality,
independent decoding/performance, chapter distributions and actual figures remain
unfinished. Independent accessibility review and later substantive editing remain separate.


## Complete restricted-baseline H.263 macroblocks

Steps 91–100 now compose macroblock type, presence flags, normalized half-sample
motion differences and all required block payloads. Native chapter 35 has 98 pages,
one hundred observations/quizzes, 31 tagged tables/116 header cells and four
described native figures. The new workflow carries the student's completed
coefficient emitter and writer forward without publishing target solutions.

Two instructor implementations pass 8638 private cases each, including all 4096
selected/predictor component pairs in both byte orders. Three layouts pass 116
public cases. Eight endpoint cases, four retained-context chains and twenty
forced child-failure cases pass. Five incorrect bodies and the unfinished entry
are rejected. Fourteen ordinary prepared/composed callers pass expected execution;
all 316 public tooling tests pass. Numeric facts for 92 MCBPC/CBPY/MVD entries
match an independent codec's tables. Semantic grading accepts legal differences
in compact/escape coding and skip selection.

Picture ownership/publication, file output, independent whole-stream decoding,
quality and performance, final chapter archives and actual IDE figures remain
unfinished. MARS and authoring-plugin source are unchanged in this increment.
The separate authoring plugin declares its MARS dependency; no GUI launch or
capture is claimed. Independent accessibility review and later substantive editing
remain separate.


## Complete H.263 picture controller and actual MIPS stream proof

Native steps 101–110 join the tested kernels into raster-ordered QCIF pictures,
with explicit row GOB headers, reconstructed references, half-sample motion syntax,
private complete-picture staging and final output publication. The chapter now has
107 pages and 110 observations/quizzes. A 54-case public checker independently
decodes every macroblock, repeats the search and checks selected levels and accurate
reconstruction, accepting equivalent entropy codes, skip choices and transforms.

Two actual MIPS encoders independently decode all eight Python-generated pictures.
The reference/alternative produce 2212/5101 bytes and execute 93429640/95888083 guest
instructions. Maximum local-versus-independent-decoder difference is two sample
values for each. One initial reference fresh CLI run takes about 6.7 seconds,
including JVM, assembly, simulation and real file I/O. The eight-picture default
is retained; these are whole-process observations, not isolated core throughput.
All 328 public tooling tests pass. Instructor implementations, late-child probes
and complete real-file driver evidence remain private.

Student clip-driver construction, file-failure grading, chapter archives and
actual IDE figures remain unfinished. No MARS or authoring-plugin source changes
or graphical IDE launches occur in this increment. The separate authoring plugin
continues to declare its MARS dependency. Independent accessibility review and
later substantive editing remain open.


## Complete H.263 clip driver and file-error grading

Native steps 111–120 complete the student-facing clip loop: bounded short reads,
guarded short writes, accepted-reference publication, exact EOF and owned-descriptor
cleanup. The new exclusive-directory workflow generates raw samples, runs the
actual student MIPS source and independently decodes its output. The chapter has
117 pages and 120 observations/quizzes. All 340 public tooling tests pass.

Both drivers pass 74 public cases; an independent layout and focused private probes
exercise real late child failures and incorrect I/O/publication behavior. Targeted
write failures in either output use a backward-compatible grader addition in MARS
commit 6c66f101, tested by all 122 CLI Jupiter tests. This is a grader capability;
no simulator arithmetic or instruction semantics changed. The commit is local and
is not pushed as part of the book publication.

Actual eight-picture encoders produce 2212/5101 bytes and execute
95583213/98072067 guest instructions, including guarded I/O and cleanup. Both
independently decode with maximum local difference two; whole fresh CLI observations
are about 7–8 seconds. A 44-picture 8.8088-second clip also independently decodes,
including temporal-reference wraparound. The eight-picture default is retained.
Final chapter distributions and actual IDE figures remain unfinished. Capture
implementation stays in the separate authoring plugin depending on MARS; no plugin
source or GUI launch changes occur here. Independent accessibility review and later
substantive editing remain separate.


## H.263 chapter distributions

Package 035-h263 is registered alongside the preceding 34 projects. It exports
all 120 native construction steps, twelve retained working stages, thirteen
callable entries, a free-writing shell and an actual generated-input run workflow.
The student archive contains 1196 public cases and no completed target assembly
or answer keys. The instructor distribution adds 31 public/private domains with
15636 cases per implementation, 120 explanations and 240 progressive hints.

Relocated archives verify 32468 positive cases across reference, alternative and
independent frames, reject both unfinished starters, and independently decode
both actual eight-picture runs. Thirty-two extracted helper entrypoints resolve
their local imports. The full inverse-accuracy runner and exact prior reference
body/constants/core provenance remain separate mandatory evidence; new submitted
transforms must run the full gate. All 355 public Python tests pass.

The course runner supports a matching harness per check, retaining the simulator's
existing assembly-work and wide-data limits. Native export preserves the directly
edited book source and cross-reference labels, refuses to replace edited exports,
and retains reviewed capture state. The chapter is 118 pages, its assignment two
pages, and the complete manuscript 1705 pages. The existing Minion Pro/Noto Sans
Math/IntelliJ-exported code setup is unchanged.

Six H.263 IDE figures remain explicitly planned; capture tooling stays in the
separate authoring plugin depending on MARS. No graphical IDE launch or main-plugin
source change occurred in this packaging step. Independent accessibility review
and later substantive editing remain open. The required grader revision 6c66f101
is still a local MARS commit and is not pushed by book publication.


## Illustrated Life chapter

The Life chapter now contains four reviewed native LaTeX IDE figures from twelve
verified light-theme screenshots, with sixteen numbered explanations. The complete
chapter is 36 pages and retains its halo and oscillator diagrams. It connects
source location, current/destination pointers, working counters, the memory
publication and the displayed frame at each boundary. The same horizontal image
at generations zero and two is explicitly distinguished by its execution state.

Both equivalent implementations, fifteen cumulative programs and twenty-eight
quiz keys pass. Grading records 1,894 passing cases and fifty-six expected
rejections. All 377 public tooling tests pass. Student materials include the
original images, native source and bound receipts; instructor checks and keys
remain private. Capture tooling stays in the separate authoring plugin depending
on MARS. The unchanged driver reuses its exact 49-test/API evidence; this increment
changes no MARS or authoring-plugin source. Independent accessibility review and
later substantive editing remain open.


## Illustrated sorting chapter

The sorting chapter has four reviewed native LaTeX IDE figures from twelve verified
light-theme screenshots, with sixteen numbered explanations. Its 36 pages retain
the leaf-call and idealized bar diagrams. The figures explain signed array bytes,
height `value+5`, the two different first exchanges and the final phase/count.
The last insertion frame repeats the sorted bars while changing phase to completion;
its hexadecimal count 0x11 is explained as seventeen exchanges.

Both completed implementations pass public/private sorting, independent renderer
and measured-workload checks. Fourteen cumulative programs and twenty-seven quiz
keys pass. Grading records 3,594 passing cases and 583 expected rejected cases;
all 377 public tooling tests pass. Student materials retain original images and
state receipts alongside native annotations. Instructor solutions and keys remain
private. Capture tooling stays in the separate authoring plugin depending on MARS.
Independent accessibility review and later substantive editing remain open.


## Illustrated maze chapter

The maze chapter contains four reviewed native LaTeX IDE figures from twelve
verified light-theme captures, with nineteen numbered explanations. Its 42 pages
retain the shortest-route and active-queue diagrams. Numeric states, parent links,
distances and route coordinates accompany the colored images. The full-room view
shows all 64 queue-history bytes and explains why equal head/tail values still
mean an empty active frontier. The selected route has fourteen edges and fifteen
cells; alternative shortest routes remain valid under the grading contract.

Both implementations pass public/private transitions, independent renderer calls
and measured workloads. Seventeen cumulative programs and thirty quiz keys pass.
Grading records 936 passing cases and 97 expected rejected cases; all 377 public
tooling tests pass. Original images and state receipts accompany directly editable
native annotations in student materials. Instructor checks and keys stay private.
Screenshot automation remains in the separate authoring plugin depending on MARS.
Independent accessibility review and later substantive editing remain open.


Chapter 11 now has four reviewed native LaTeX IDE figures from twelve actual light-theme
captures, with twenty numbered explanations. They connect the input/output dimensions,
the first clipped tile and two cache mappings at unchanged 128-byte capacity. Native
register values, output bytes and cache settings accompany the counters. The first-tile
caption distinguishes the target count in `$v0` from the twelve cells already stored.

Independent host and core checks agree on every memory access and all output words.
Full grading records 3,655 passing cases and 120 expected rejected cases across the
starter and eleven faulty variants; thirteen intermediate programs and 28 quiz keys pass.
The separate authoring plugin's native cache observer passes 63 Jupiter tests and the
pinned IntelliJ compatibility check. Independent accessibility review and later substantive
editing remain open.


Chapter 12 now contains four reviewed native LaTeX figures from eight actual light-mode
panels, with twelve numbered explanations. They compare floor/nearest display choices
for unchanged raw state, preserve the overshoot in the first vertical reflection, and
show the overflow results in both native hexadecimal and signed-decimal register views.
The chapter is 42 pages and retains its reflection and quantization diagrams.

Independent host/core checks agree on both nine-publication histories, every frame word
and all 639 stores per run. Complete grading passes 42,218 valid-program cases, rejects
812 cases across the starter and eighteen faulty variants, checks sixteen intermediate
programs and thirty quiz keys, and confirms the expected native ADD overflow exception.
The separate authoring plugin passes 67 Jupiter tests and pinned compatibility checks.
Original images, numeric receipts and editable native annotations accompany the student
materials; instructor keys and checks remain private. Independent accessibility review
and later substantive editing remain open.


Chapter 13 now has three reviewed native LaTeX figures from twelve actual light-mode
panels, with fifteen numbered explanations. Its 31 pages retain the input-window diagram
and connect native signed statuses/full pointers, mapped bytes, wire field order and
output-record layout. The short-prefix figure shows why a visible mapped checksum byte
is still unavailable to the parser. The checksum failure explains zero record stores,
which is stronger than preserving the final bytes.

Complete grading records 32,168 positive cases and 2,004 expected rejected cases across
the starter and fourteen faulty variants, plus fifteen intermediate programs and 24 quiz
keys. Both implementations, full-width pointers and an equivalent signed-payload-load
variation pass. Independent capture checks cover all 324 data bytes at three boundaries,
logical read windows and exact stores. The unchanged separate authoring driver retains
its exact 67-test/API binding. Independent accessibility review and later editing remain open.

Chapter 14 now includes three actual light-mode IDE figure plates: wrapped record order, an exact-fit full queue and a short-destination failure. Twelve original captures support fifteen native LaTeX explanations. An independent FIFO model checks all 472 data bytes at all fourteen operation boundaries, exact stores and logical read bounds. Complete grading passes 226,520 successful-case checks, 18 cumulative programs and 30 quiz keys; the starter and 21 faulty variants produce 2,862 expected failures. Independent accessibility review and later substantive editing remain open.

Chapter 15 now includes four actual light-mode IDE plates explaining pending UTF-8 state, successful finality, an impossible continuation and empty-final truncation. Twelve unchanged captures support sixteen native LaTeX explanations. Complete grading passes 189,831 successful-case checks, 22 cumulative programs and 34 quiz keys; the starter and 25 faulty variants produce 3,364 expected failures. Both implementations cover all 65,536 two-byte inputs and all 1,112,064 Unicode scalar values. Independent accessibility review and later substantive editing remain open.

Chapter 16 now includes four actual light-mode IDE plates for left, full, top-right and bottom-left sprite placements. Twelve original captures support sixteen native explanations and logical-frame outlines. Full grading passes 31,275 successful-case checks, 16 cumulative programs and 25 quiz keys; the starter and 16 faulty variants yield 1,709 expected failures. The teaching source enables initial pixels for the guard. Native pixels remain exact; a documented screenshot-only ICC tolerance is bounded at three channel levels. Independent accessibility review and later substantive editing remain open.

Chapter 17 now includes four actual light-mode IDE plates explaining precedence, syntax priority, literal overflow and zero-divisor diagnostics. Twelve unchanged screenshots support sixteen native LaTeX explanations. Complete grading passes 23,887 successful-case executions, 34 cumulative programs with 46 intermediate observations and 45 quiz keys; the starter and nineteen faulty variants yield 3,112 expected failures. Independent accessibility review and later substantive editing remain open.

Chapter 18 now includes four actual light-mode IDE plates explaining countdown halt, budget exhaustion, stack-capacity failure and invalid-target validation before a zero budget. Twenty unchanged screenshots support twenty-four native LaTeX explanations. Complete grading passes 33,134 successful-case executions, 40 cumulative programs with 58 intermediate observations and 51 quiz keys; twenty-five rejection runs yield 7,182 expected failing cases. Independent accessibility review and later substantive editing remain open.

Chapter 19 now includes four actual light-mode IDE plates explaining packed RGB565, expanded components, separate display words and a nine-pixel vector-plus-tail run. Eighteen unchanged screenshots support twenty-two native LaTeX explanations. Complete grading passes 17,346 positive case executions, 39 cumulative programs with 47 intermediate observations, 47 caller scenes and 49 quiz keys; twenty-two rejection runs yield 5,140 expected failing cases. Independent accessibility review and later substantive editing remain open.

Chapter 20 now includes four actual light-mode IDE plates explaining saturation, wrapping, saved caller results and a nine-sample vector-plus-tail run. Eighteen unchanged screenshots support twenty-two native LaTeX explanations. Fresh full verification passes 26,278 successful checks, 30 incremental programs, 48 observations in two launch contexts and 40 quiz keys. The starter and 22 faulty variants produce 12,268 expected failing cases. All twelve simulator-derived WAV files match the extracted student distribution. Independent accessibility review and later substantive editing remain open.

Chapter 21 now includes five actual light-mode IDE plates for the impulse return, caller publication, a negative halfway value, a clipped endpoint and an exact wide MSA reduction. Twenty-four original captures support thirty native LaTeX explanations. Fresh full verification passes 26,014 successful checks, 36 incremental programs, 66 observations in two contexts and 47 quiz keys. The starter and 26 faulty variants produce 10,222 expected failing cases. The extracted student renderer reproduces all six published WAV files for both implementations. Independent accessibility review and later substantive editing remain open.

Chapter 22 now has four annotated light-mode IDE figures for scalar completion, zero reduction, retained reduction segments and paired cache observations. Nineteen native captures are verified; fifteen unchanged originals appear in eighteen explanatory details. Fresh full grading passes 24,846 successful cases, 42 intermediate programs, 58 published observations in two contexts and fifty quiz keys. Twenty-one rejection runs yield 2,019 expected failures. The standalone chapter has 64 pages. Independent accessibility review and later substantive editing remain open.

Chapter 23 now has four annotated light-mode IDE figures explaining item-atomic capacity exhaustion, private scan versus commitment, malformed encoding versus reconstructed range, and all six published dataset sizes. Twenty-one unchanged captures support twenty-six native explanations. Fresh full grading passes 40,703 successful cases, fifty intermediate programs, 84 published observations in two contexts and sixty quiz keys; 22 rejection runs produce 11,236 expected failures. The standalone chapter has 74 pages. Independent accessibility review and later substantive editing remain open.

Chapter 24 now has four reviewed native IDE figures explaining a changed input bit, complete 256-entry table initialization, raw state between real three-byte file reads, and missing-path versus later read-failure cleanup. Twenty-four raw views are verified across two batches; nineteen originals appear in twenty-two numbered details. The 50-page chapter retains 7,855 successful grading cases, 24 intermediate programs, 52 observations in two contexts and 34 quiz keys. Twenty-six rejection runs yield 784 expected failures, and both implementations pass eight ordinary-host-file runs. The failure comparison uses explicit in-memory I/O fixtures in the separate authoring plugin, with exact guest operation history and separate post-execution disposal receipts. Independent accessibility review and later substantive editing remain open.

Chapter 25 now has four reviewed native IDE figures, using eighteen unchanged light-theme screenshots and twenty-two numbered explanations. Fresh verification passes 9,896 positive cases across 173 grading runs, 38 cumulative programs, 58 observation recipes, 43 exported guided checks and 47 quiz keys. The starter and 33 faulty variants produce 2,311 expected failing cases in 34 rejection runs; six ordinary host-file controls also pass. The separate unchanged authoring plugin reuses exact source/build/class-bound 78-test Jupiter and complete paired API evidence. Independent accessibility review and later substantive editing remain open.


Chapter 26 now has four reviewed native IDE figures explaining scalar quarter-round publication, the first double-round, vector lane selection and a 65-byte XOR across two counters. Seventeen unchanged screenshots support twenty-one native explanations. Fresh grading passes 5,748 positive case executions across 165 runs, 36 intermediate programs, 54 recipes, 40 exported guided checks and 42 quiz keys. The starter and 35 faulty variants yield 972 expected failures in 36 rejection runs. The standalone chapter has 61 pages. Independent accessibility review and later substantive editing remain open.


Chapter 27 now has four reviewed native IDE figures explaining the full product accumulator and carry, the 129th sum bit, private reduction before publication and modular-power output before frame restoration. Sixteen unchanged screenshots support twenty-three explanations. Fresh grading passes 3,912 positive cases across 163 runs, 35 intermediate programs, 43 recipes, 35 exported guided checks and 42 quiz keys. The starter and 25 faulty variants produce 542 expected failures across 26 rejection runs. The standalone chapter has 64 pages. Independent accessibility review and later substantive editing remain open.


Chapter 28 now has four reviewed native IDE figures explaining scalar alphabet lookup, nonzero unused padding bits, numerical vector sextets and exact twelve-byte decoder publication. Seventeen unchanged screenshots support twenty-two explanations. Fresh grading passes 16,270 positive cases across 174 runs, 37 intermediate programs, 52 recipes, 37 exported guided checks and 43 quiz keys. The starter and 26 faulty variants yield 1,162 expected failures across 27 rejection runs. The standalone chapter has 69 pages. Independent accessibility review and later substantive editing remain open.


Chapter 29 now has four reviewed native IDE figures for the parsed descriptor, four completed pixel stores before the return edit, the canonical header prefix and accepted file bytes before completion. Sixteen unchanged light-theme screenshots support twenty native explanations from exact cumulative lesson stages. Fresh grading passes 4,263 positive cases across 160 runs, 37 intermediate programs, 62 observation recipes, 38 exported guided checks and 42 quiz keys. Nine faulty implementations and four unfinished entry points produce 181 expected failing cases. The separate capture checks also reject twelve assembled faults. The standalone chapter has 45 pages. Independent accessibility review and later substantive editing remain open.


Chapter 30 now has four reviewed native IDE figures explaining validation pixel count, decoded alpha and packed dimensions, a shorter lossless QOI stream and the first three accepted file bytes. Eighteen unchanged captures support twenty-two explanations from exact cumulative lesson stages. Fresh verification passes 6,667 execution cases across 201 grading runs, plus 607 host-oracle checks and 65 prepared-fixture checks. Twelve mutants, six unfinished domains and five unfinished guided regions yield 310 expected failures. All 65 quiz keys, 57 cumulative programs and 65 guided checks are covered. The standalone chapter has 53 pages. Independent accessibility review and later substantive editing remain open.
