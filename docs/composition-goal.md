# Full chapter composition goal

Author the complete instructional text for the increasingly difficult projects in the
35-project roadmap. The reader understands basic programming and is learning assembly
and machine representation. A chapter must contain its explanations and worked reasoning;
a sequence of tasks or a table of proposed sections is not a completed composition.

## Chapter acceptance

A complete draft introduces the problem, explains the relevant machine model, develops
the program in teachable increments, and connects the increments with substantive prose.
It includes concrete traces, worked examples, semantic teaching figures or tables,
comprehension questions, practice variations, and a clear account of the tested contract.
A reader should be able to explain why the demonstrated instructions produce the result.

Supply an opening project, a verified reference and equivalent alternative, meaningful
public and private checks, and instructor explanations. Keep the reference and private
checks in the instructor repository. Separate checks for a particular illustration from
behavioral grading of equivalent submissions. Add tested grader operations upstream when
an essential contract cannot be checked. State what remains a human or UI assessment.

Screenshots must come from verified light-mode IDE captures performed by the separate
authoring plugin. Keep capture plans explicit while implementation is pending. Do not
call a chapter illustrated or print-ready because its capture recipes exist.

## Composition after MacTeX installation

MacTeX is installed and the first nine chapters have been migrated once to ordinary
LaTeX. Use input/include directly for chapters, sections, worked examples, and teaching
fixtures. main.tex and includes/ are the editable publication authority. The old
JSON/Markdown book assembler has been retired from the publication path. The final book must not depend on rendering a JSON-defined document
model or on round-tripping edited LaTeX through Markdown.

Machine-readable task metadata may still serve the trainer and grading distribution.
It must not constrain the order, layout, prose, or inclusion structure of the book.
Migrate source labels and authorship records without overwriting human contributions.
Use native LaTeX for diagrams, tables, math, code, captions, and screenshot annotations.
Validate accessibility in the actual compiled PDF with the selected template.

## Working order

Complete chapters in prerequisite order, beginning with console state, memory-backed
pixels, sprite rows, and integer representation. Preserve the full roadmap and document
any needed prerequisite lessons before changing its progression. Prefer completing one
substantive, verified composition to producing many skeletal chapter packages. Review
instruction semantics and the explanatory text together before committing each increment.

The console, single-pixel, eight-byte-sprite, bit-inspector, seven-segment-counter,
MarsBot-square, Bresenham-line, Life, sorting-visualizer, maze-explorer, matrix-transpose,
fixed-point ball, binary packet parser, ring-buffer logger, UTF-8 scanner, clipped sprite blitter, and expression evaluator chapters are composed text drafts with verified companion programs.
They still need author review and actual IDE figures. The remaining projects stay
planned until their text and companions are written and checked. The next uncomposed project is the Q15 FIR filter.

Matrix transpose and fixed-point ball are composed through ordinary includes in the
main manuscript. Their independent entries, `drafts/matrix-transpose.tex` and
`drafts/fixed-point-ball.tex`, support focused editorial and layout review. The ball
chapter has thirty microsteps, sixteen cumulative code increments, public animation
and kernel checks, independent private oracles, and four planned light-theme figures.
Student and instructor distributions pin the same public version and archive digest.

Binary packet parser is composed in native includes and has an independent entry,
`drafts/binary-packet-parser.tex`. Its 24 steps include 15 cumulative code increments,
explicit error priority, byte order and checksum reasoning, a native input-window
diagram, and measured instruction/read comparisons. Its grader adds native read-width
observations to detect logical overreads into mapped padding. Three actual IDE figures
remain planned. The full roadmap remains the goal; eighteen text drafts do not complete it.

Ring-buffer logger has a complete native text draft and an independent entry,
`drafts/ring-buffer-logger.tex`. Thirty steps and eighteen cumulative code increments
develop separate push/pop contracts, an explicit occupied-byte invariant, arbitrary
bounded capacities, independent cursor wraps, zero-length records, exact fits, and
short-destination retries. A native slot diagram, complete command trace, measured
copy comparison, public/private grading and separate distributions accompany it.
Three actual IDE captures remain planned.

UTF-8 scanner is a full native composition with an independent entry,
`drafts/utf8-scanner.tex`. Thirty-four steps and twenty-two cumulative increments
develop scalar counting, prefix bounds across chunks, explicit finality, separate
start/detection positions, and terminal-state preservation. The programs are checked
against all scalar encodings and all two-byte inputs, with additional transition,
split, wide-address, and equivalent implementation tests. Four actual IDE figures
remain planned. The complete 35-project composition and capture goal remains active.

Clipped sprite blitter has a full native draft at `drafts/clipped-sprite-blitter.tex`.
Twenty-five steps and sixteen cumulative increments develop half-open intersection,
signed coordinate width, independent row strides, full-word key equality, no-store
transparency and full caller preservation. Native clipping/stride/count fixtures,
measured preclipped/per-cell work, public checks and private geometry checks accompany
separate student/instructor archives. Four actual light-theme captures remain planned.

Expression evaluator is a full native text draft at `drafts/expression-evaluator.tex`.
Forty-five steps and thirty-four cumulative edits develop bounded lexical reads,
checked arithmetic, literals, recursive groups/signs and left-associated precedence
loops. All edits pass forty-six intermediate observations and reconstruct the tested
reference exactly. The public package supports guided and free-writing modes; private
checks accept a precedence-climbing alternative and submission-owned constant reads.
Repeated calls verify fresh parser state in one session. The 76 grading runs record
23,887 positive case executions, including repeated variants and observations.
Nineteen faulty variants and the unfinished starter are rejected. Four actual IDE
captures, human review and independent accessibility validation remain pending. The
full 35-project book and capture objective remains active.

Tiny bytecode interpreter is a full native text draft at
`drafts/tiny-bytecode-interpreter.tex`, included in main.tex as chapter eighteen.
Fifty-one steps and forty cumulative assembly edits develop thirteen variable-length
opcodes, whole-image and target-validation priority, checked arithmetic, stack bounds,
precise retirement and fresh-call behavior. Six provided frame/exit excerpts, a native
instruction-boundary diagram, complete countdown prefixes, measured composite
implementations and guided/free-writing packages accompany the prose. All forty partial
programs have fifty-eight executable observations. Four actual light-theme captures,
human review and independent accessibility validation remain pending. Eighteen text
packages do not complete the full 35-project and capture objective, which remains active.

RGB888/RGB565 is a full native text draft at `drafts/rgb-formats.tex`, included in
main.tex as chapter nineteen. Forty-nine steps and thirty-nine cumulative assembly
edits develop unaligned scalar conversion first, then bounded MSA groups and scalar
tails. Three native diagrams, exact quantization/expansion formulas, error bounds,
selector derivations and complete-call measurements accompany it. Forty-seven
intermediate observations check scalar values and vector lanes both in isolated calls
and through the student caller, using published reproducible input recipes. Final grading also
accepts an equivalent scalar implementation. Four actual light-theme captures,
author review and independent accessibility validation remain pending. Nineteen
text drafts do not complete the full 35-project and capture objective, which remains active.

PCM mixing is a full native text draft at `drafts/pcm-mixing.tex`, included in main.tex
as chapter twenty. Forty steps and thirty cumulative edits develop signed decoding,
exact sums, strict saturation bounds, byte stores, exact in-place aliases, eight-lane
saturation, clipping masks and doubleword populations. Forty-eight reproducible
observations are checked in unaligned calls and through the actual program caller;
the completed scalar stage and final MSA stage share the behavioral contract.
Public per-step construction checks, final checks, twelve listening files and a
reproduction renderer accompany separate student/instructor distributions. Three native
diagrams and four verified numerical capture models support the text. Actual IDE
captures, interactive progression controls, author review and independent accessibility
validation remain pending. Twenty text packages do not complete the full 35-project
composition and capture objective, which remains active.

Fixed-point FIR is a full native text draft at `drafts/fixed-point-fir.tex`, included
in main.tex as chapter twenty-one. Forty-seven steps and thirty-six edits retain a
scalar comparison before developing widened MSA dot products. Seven supplied excerpts,
forty-seven quizzes, ninety-four hints, sixty-six reproducible observations, public
per-step checks and separate student/instructor distributions accompany the text.
The completed scalar and vector stages share the behavior and chunk-continuity checks.
Six listening fixtures can be reproduced with a separately authored public renderer;
private algorithms and hidden grading domains remain in the instructor repository.
Five numerical capture models are verified, but actual IDE figures, the interactive
runner, enforced progression, author review and independent accessibility validation
remain pending. Twenty-one text packages do not complete the full 35-project and
capture objective, which remains active. Blocked matrix multiplication is next.


Blocked matrix multiplication is a full native text draft at
`drafts/blocked-matmul.tex`, included in main.tex as chapter twenty-two. Fifty steps
and forty-two cumulative edits develop rectangular arithmetic, full-width addresses,
exact sums, empty-shape behavior, clipped tiles and retained partial sums. Two supplied
excerpts, fifty quizzes, one hundred progressive hints and fifty-eight public recipes
accompany the sequence. Public construction scripts verify the illustrated states;
final checks accept equivalent schedules and alternate supplied continuations. The
private companion adds every legal dimension triple at a deterministic tile, all tile
values on wide-sum boundary shapes, alternatives, quiz keys and exact archive receipts.
Forty-four listings retain actual IntelliJ highlighting. A native diagram and measured
tables keep retired instructions, notices and cache misses separate. Four numerical
capture models support the pending figures. Actual IDE pixels and annotations, remote
lessons, enforced progression, author review and independent accessibility validation
remain unfinished. Twenty-two text packages do not complete the full 35-project book
and capture objective, which remains active. Delta and variable-length encoding follows.


Delta and variable-length encoding has a directly edited native chapter at
`drafts/delta-varint.tex`. It defines signed-32-bit readings with exact 33-bit differences,
ZigZag mapping, minimal unsigned base-128 groups, a bounded fifth byte and item-atomic
capacity/error results. Worked bytes, a native payload diagram, malformed/range priorities,
framing limits, exercises and deterministic size/instruction tables accompany independent
encoder/decoder prototypes. Both pass independent-model checks; actual same-session
composition, zero-capacity priorities, wide pointers and meaningful fault rejection are
verified separately. The cumulative chapter now has 60 steps, 50 edits, five supplied
excerpts, 60 quizzes, 120 hints and 84 recipes. Its 58 grading runs cover 19,675 positive
case executions, including both offset isolated calls and the actual caller. Four native
capture slots remain explicitly planned. The student and private instructor packages
contain native sources, public construction/final checks, equivalent solutions and
additional private domains. The chapter has joined main.tex as the twenty-third text
package. Actual IDE captures, enforced progression, author review and independent
accessibility validation remain unfinished. The full objective remains active.

CRC-32 file checking has a directly edited native draft at `drafts/crc32.tex`.
It develops one message state through a bit update, a table update and a streaming file
reader, with exact width/ABI/ownership contracts. Worked byte transitions, raw/finalized
prefix states, a described native PNG chunk diagram and complete-call instruction
tables connect the implementations. A new upstream grader fixture API enables file
open/read/close only for explicitly supplied read-only memory files, records descriptor
history and supports forced short reads and injected errors. The existing simulator
syscall path remains authoritative. The tested prototype pair accepts different
feedback/load strategies and rejects arithmetic, boundary, resource and return faults.
The cumulative expansion contains 34 steps and 24 edits, with seven supplied excerpts,
34 quizzes, 68 hints and 52 recipes. It retains cleanup for incomplete file code and
checks the actual caller as well as isolated offset functions. Thirty-one native
assembly listings use exported IntelliJ colors. Four capture slots remain plans.
The student package and separate instructor distribution include native snapshots,
public guided/final checks, equivalent solutions, private domains and exact archive
receipts. The chapter is included in main.tex as the twenty-fourth text package.
Actual IDE captures, enforced progression, the deferred rewrite and independent
accessibility validation remain unfinished.

The SHA-256 foundation is directly edited at `drafts/sha256.tex`. Its connected
exposition follows byte order, word widths, block padding, schedule expansion, the
first compression round, streaming state and file result publication. It includes a
native padding diagram, headed tables and measured instruction-work comparisons for
two equivalent implementations. Private tests cover arbitrary-state compression,
split messages, independent contexts, full length fields, real files and deliberate
faults. The cumulative expansion has 47 steps,38 authored edits,six supplied entry/exit pairs,
47 quizzes,94 hints,58 exact recipes and four planned IDE figure slots. Every prepared
image is checked along with caller and offset-function execution. Separate public/student and private/instructor packages now contain safe guided
exports, completed-interface checks, private domains and explanatory quiz feedback.
The chapter is included in main.tex as the twenty-fifth text package. Actual IDE pixels,
remote progression, deferred author rewriting and independent accessibility work remain.

The ChaCha20 foundation is directly edited at `drafts/chacha20.tex`. Fourteen native
sections explain the quarter-round's dependent updates, full-width pointers, raw state,
column/diagonal selection, feed-forward, MSA row lanes and inverse shuffles, byte tails,
preflight counter exhaustion, in-place operation and nonce/authentication boundaries.
A native described row-selection figure and headed tables support the worked example.
Private scalar and MSA prototypes pass independent arithmetic and JDK comparisons,
including wide code/data and 19 deliberate faults. Its cumulative expansion has 42 steps, 36 authored regions, 42 quizzes,
84 progressive hints, 54 recipes and four planned figure slots. Prepared data, intermediate
register/memory states, caller and offset-function views, safe development returns and
complete reconstructions are verified. Separate student/instructor distributions now
include public guided and final checks, private domains, quiz feedback and exact archives.
The chapter is included in main.tex as the twenty-sixth text package. Actual IDE captures,
remote progression, deferred author rewriting and independent accessibility work remain.

## Author feedback and deferred revision

On 18 November 2026, the author reviewed excerpts and judged the draft too long and
insufficiently explanatory for a normal student. Complete all roadmap chapters before
starting the substantial rewrite. Executable checks establish behavior within their
tested domains; they do not establish student usability or teaching quality.

The later editing pass should reduce unnecessary fragmentation and repetition, connect
small code changes into an explained algorithm, state what each phase accomplishes and
why, and establish a coherent progression through each chapter. Preserve the native
include structure so the author can place and revise connecting prose freely. The
author has explicitly deferred this rewrite until the chapter sequence is complete.


The RSA arithmetic foundation is directly edited at `drafts/rsa-arithmetic.tex`. Seventeen
connected sections explain unsigned Euclid, the toy inverse, square-and-multiply, limb
representation, full schoolbook multiplication, 129-bit modular addition, reduction,
modular multiplication and the full 128-bit exponent. Worked numerical transitions, a
native described limb diagram and measured instruction-work counts accompany the prose.
Its cumulative expansion now has 42 steps, 35 authored regions, 42 quizzes, 84 hints
and 43 exact observations, woven around the retained explanations. Prepared programs,
intermediate values, caller/offset-function paths, wide-address complete implementations
and safe nested development returns are verified. The student package now includes guided exports and nine completed-interface scripts,
with separate private cases, solutions and quiz feedback. It is included in main.tex
as the twenty-seventh text package. The existing
chapters have not been rewritten; the author's substantial edit remains deferred.


The Base64 foundation is directly edited at `drafts/base64.tex`. Nineteen connected
sections explain byte/sextet regrouping, canonical padding, count/capacity preflight,
ASCII classification, transactional validation, exact in-place decoding, MSA gathers,
masked alphabet translation, compaction and scalar tails. A native described bit diagram,
headed tables and measured instruction-work comparisons support thirteen selected
assembly fragments. Two equivalent private implementations pass normal/wide checks and
24 deliberate faults. The manuscript now includes this twenty-eighth text package
with separate student and instructor distributions. Its cumulative expansion now has
43 steps, 37 authored regions, 52 exact recipes, 43 quizzes and 86 progressive hints.
Connected native includes weave the construction around the foundation, with supplied
frames and restore paths, explicit unfinished-call propagation and four planned IDE
figures. Intermediate state, equivalent completed interfaces and wide addresses are
checked independently. The package exports guided observations and seven public
completed-interface domains; private cases and quiz feedback remain separate. Actual
screenshot capture and remote progression remain pending.
The author's substantial exposition and length rewrite remains deferred.


The Binary PPM reading foundation is directly edited at `drafts/binary-ppm.tex`.
Ten sections explain header/raster boundaries, the local image subset, exact extents,
RGB bytes versus display words, short I/O, cleanup and independent checks. A numbered,
described native two-by-two image and byte-offset table support the worked fixture.
The fixture helper preserves a new output path, and an independent image reader checks
ten host-generated images. These are not assembly-writer results. The missing grader
output capability has been implemented upstream with declared paths, binary snapshots,
short writes and failure injection, preserving read-only defaults. A private transport
probe passes 54 executions across normal/wide addresses. Full image routines, cumulative
construction and separate distributions remain next; the main manuscript retains its
28 packages. Actual IDE captures and the deferred author rewrite remain pending.


Binary PPM now has five checked image interfaces and a complete private load/decode/
encode/save caller, with an equivalent mask/field-insertion/byte-oriented alternative.
The native reading adds metadata layout, packed dimensions, status precedence, bounded
scanner invariants, canonical header sizing and file-state explanations. Public prose
was written directly; instructor implementation bodies were not imported into it.
The two implementations are verified at ordinary and wide addresses, including actual
saved-file decoding by an independent reader and deliberate faults. Guided construction,
public completion checks and separate chapter distributions remain next. The manuscript
still contains 28 packaged chapters; the full goal remains active.


The PPM cumulative draft now has 42 native steps, 37 authored regions, 62 exact recipes,
42 quizzes,84 progressive hints and four explicitly planned light-theme figures.
The safe starter was authored independently of instructor algorithm bodies. Its generic
128-byte save/restore and descriptor cleanup are illustrated with actual IntelliJ
highlighting. The caller stores full 64-bit results. Independent checkpoint models cover
partial metadata, packed pixels, header prefixes and file state; complete variants retain
the interface checks. Step and recipe metadata does not regenerate native prose.
Separate distributions and main-manuscript integration remain next. The earlier prose
remains intact, with split includes preserving its publication order around the edits.


Public PPM completion feedback now covers parsing, decoding, encoding, malformed/count
boundaries, file transfers and connected callers with same-session recovery. A separately
authored regular-expression host model supplies exact metadata, RGB samples and file
expectations; caller-object traces and full-width register sentinels enforce the public
contract. Complete assembly, quiz feedback and negative-control construction remain
private. This is a grading increment before the PPM distribution/integration step.


Chapter029 is now included in the main native manuscript. The separate student package
contains 42 steps, 38 exported guided checks, six completed-interface check domains and
the worked-image helper. The private counterpart retains complete solutions, quiz
feedback and additional memory/file domains. Package verification checks exact archive
membership, safe unfinished bodies, native reading order and extracted-file execution.
The manuscript now has 29 packaged chapters; six roadmap chapters remain to be drafted.


The QOI reading foundation is now directly authored in 20 native include segments,
with an eight-pixel trace covering all six chunk types and the earlier eight-byte sprite
as a real QOI fixture. It explains initial/cache alpha, collisions, biased modulo changes,
runs, strict completion, validation before publication, encoder equivalence and uniform
compression objectives. Six proposed interfaces retain metadata and full-width state.
The private host oracle and independent upstream codec checks establish fixture facts;
they are not evidence of a completed MIPS program. Cumulative construction, complete
MIPS companions, quizzes/hints, distributions and manuscript integration remain next.
The main manuscript still has 29 packaged chapters; the full goal remains active.
