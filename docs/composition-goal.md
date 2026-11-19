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


The private QOI companions now implement the six interfaces and complete file caller.
Reference and equivalent bit-field/hash/literal variants each pass 1309 memory and 85
file/caller cases at ordinary addresses and above 4 GiB, for 5576 successful case
executions. Twenty-six faulty variants are rejected; a hidden-state variant first passes
an isolated call and then fails same-session checks. Eighty-four actual assembly images
are decoded by an independent strict model and freshly compiled, hash-pinned upstream
reference decoder. Three normal CLI host-file round trips preserve RGBA and metadata,
and a malformed input preserves an existing destination. These are finite prototype
checks, not a completed student package or usability certification.

A necessary grader extension, readRegister, returns all 64 bits without advancing execution,
notifications or undo edits. CLI commit 3f0f4608 has 109 passing Jupiter tests. It enables
checking variable encoder lengths without requiring one reference encoding; traces also
reject same-value writes beyond the returned interval. The book now explains measured
whole-function instruction counts for its public trace and sprite. Cumulative QOI
construction, public grading exports, quizzes/hints, distributions and actual IDE figures
remain subsequent work. The substantial prose rewrite stays deferred as requested.


The QOI cumulative construction now adds 65 native authoring/observation steps, 57
unfinished regions, 65 quizzes, 130 progressive hints and 65 exact observation recipes.
Preparation changes data only and preserves authored instructions. The scaffold has
independent invocation frames, guarded nested calls and development exits that close
live file descriptors. Separate checkpoint instructions avoid aliased-label observations.
Guided checks verify the illustrated intermediate state; completed-interface checks
accept equivalent implementations. Four light-theme IDE figures remain plans, with no
capture pixels or coordinate receipts. QOI is still a standalone chapter draft; public
grading exports, separate archives and manuscript integration remain to be completed.
All earlier chapter prose is retained for the later requested editing pass.


QOI now has seven standalone public completed-interface checking domains under
`drafts/qoi-construction/checks`. A new native passage explains invocation, intermediate
versus completed checks, caller memory ownership, file fixtures, alpha and equivalent
encoding. The host decoder is independently audited against the existing 607 private
valid/malformed fixtures. The complete reference and alternative each pass at both
normal and wide addresses. Guided exports and final chapter distributions remain the
next step; instructor algorithms and keys remain private.


QOI is now the thirtieth registered text package and native manuscript chapter. Its
student archive carries 65 guided checks (127 cumulative direct/caller executions),
seven completed-interface domains, 65 quizzes, 130 hints, 57 safe unfinished regions,
65 observation recipes and the fixed trace/sprite helper. The native reading order
and all 263 exported student files are checked against the exact ZIP. Private materials
retain both complete implementations and all 65 answer keys. The private archive embeds
the exact student ZIP and records its SHA-256 digest. Five representative unfinished
regions fail their guided check;45 execution cases run from extracted public check files.
Actual IDE captures, remote instructor controls, accessibility certification and the
later substantive editing pass remain pending. The next chapter is the PCM WAV filter.


The PCM WAV reading foundation is now directly authored in 22 native include segments
under 031-pcm-wav and built through drafts/pcm-wav.tex. It distinguishes frames, channel
samples, PCM bytes and complete file bytes; traces an 82-byte input with data at 56; and
reuses the exact eight-tap Q15 contract with separate zero histories for stereo channels.
A fixed four-frame trace and a two-second deterministic stereo input are public. The
private host oracle is checked against Python wave, NumPy and FFmpeg; the observed
zero-length-data/footer interpretation difference is documented separately. This is
host evidence, not MARS implementation or performance evidence. The full manuscript
remains 30 packaged chapters; assembly construction and packaging for WAV are next.
Earlier chapter prose remains unchanged for the later substantive editing pass.


The PCM WAV prototype step now verifies two complete private MIPS implementations:
block gathering with the earlier FIR kernel and direct interleaved convolution.
Independent Kotlin models are audited against 341 Python-derived vectors. Both methods
pass 477 memory and 209 file cases at normal and wide addresses, for 2744 simulator cases.
Fifteen assembled regressions are rejected. Twelve actual MIPS output files are decoded
independently, and four real CLI host-file round trips pass. The new observed-work
passage explains complete-program instruction counts and native memory traffic.
Bounded requests preserve output bytes while reducing repeated copy work under forced
short writes. This is not a wall-time or IntelliJ launch measurement. Student cumulative
construction, quizzes/hints, public feedback and separate WAV archives remain next.
The main manuscript is still 30 packaged chapters; prior chapter prose remains retained.


The WAV cumulative reading now adds 64 directly authored native step includes, 55 safe
unfinished regions, 64 quizzes, 128 progressive hints and 65 exact observation recipes.
The preparation helper preserves the entire instruction section, full-width controls
and existing output files. Its native block-boundary fixture gives exact results at
frames 63 and 64, alongside zero-frame and malformed-trailer observations. All 130
prepared files assemble at normal and wide addresses; 30 executed unfinished-entry and
caller checks establish safe development returns, register/stack retention and no I/O.
These are starter checks, not completed-prefix or guided-checkpoint verification. The
private quiz keys stay outside public history. Four new light-theme IDE capture plans
remain explicitly uncaptured. Cumulative completed-body execution, public feedback and
separate WAV distributions remain next; the main manuscript still has 30 packaged
chapters. Earlier exposition is retained apart from its updated scope statement, and
the requested substantive editing pass remains deferred until all chapter drafts exist.


Private cumulative WAV verification now covers 254 direct/caller executions across
all 65 recipes at normal and above-4-GiB addresses. Checks compare exact intermediate
arrays, declared private frame state, illustrated registers, descriptor ownership and
subsequent development/success returns. Both construction-compatible complete variants
also pass all 2744 memory/file cases. Fifteen deliberate accuracy/ownership regressions
are rejected; ten explicit unfinished nested-call cases preserve outputs, registers and
file cleanup. Twelve actual simulator WAV files match independently decoded prototype
outputs; four real host-file round trips pass and two malformed inputs preserve output.
All private generators reproduce their artifacts byte-for-byte. Public loader fixtures
now start with sentinel buffers, so seeded input cannot conceal missing reads. The raw
FIR direct-return observation is distinguished from caller result conversion. Public
feedback export and separate chapter distributions remain next; actual IDE captures,
remote controls and independent accessibility validation remain pending.


PCM WAV is now the thirty-first registered draft exercise package and included native
chapter. It retains 64 steps, 55 unfinished regions, 65 observation recipes and 64 quizzes
with 128 hints. Independently authored public feedback adds seven completed-interface
domains with 127 cases and 64 guided files with 127 checkpoint cases. Guided feedback
stops at the named observation, so earlier steps remain usable after later bodies are
complete. Both cumulative and completed sources pass all public checkpoints at normal
and wide addresses (508 executions); both complete algorithms pass all public final
checks at both layouts (another 508). The public host models match an independent
oracle for 74 containers and 144 filter vectors; 15 assembled regressions and six
unfinished guided controls are rejected.

The student archive contains 174 exact, deterministic, student-only files. Its native
assignment and reading fragments remain LaTeX; JSON indexes exercises and planned assets
without controlling book composition. The private instructor archive embeds that exact
student ZIP and records its SHA-256. Tests run 55 positive cases from extracted public
checks, reject 12 unfinished completed-interface cases, and run all 686 private cases
through the packaged instructor command. Quiz keys and full solutions remain private.
The main manuscript now contains 31 packaged chapters. The next chapter is CBOR telemetry;
four chapter drafts remain before the requested substantial editing pass. Actual IDE
captures, remote policy enforcement and independent accessibility validation remain
pending; four WAV capture plans are explicitly uncaptured.


The CBOR telemetry reading foundation adds 25 directly authored native passages and
five fixed public observation inputs. Its worked 52-byte record includes wider heads,
reordered fields, a binary identifier and nested optional metadata; its deterministic
required-field projection is 37 bytes. The local bounded profile distinguishes generic
CBOR syntax from exercise policy, unsigned counter bits from signed readings, inherited
container depth, per-map key identity and caller-visible atomic publication. Five entry
contracts define head reading, skipping, decode, exact sizing and encode.

Private host verification compares an independent model and cbor2 6.3.0 for 420 canonical
records and 5040 equivalent encodings, with all 256 initial bytes, 75 truncated heads,
78 rejected records, 5130 skip observations and exact input-budget boundaries. This is
host evidence only. MIPS implementations, cumulative construction, public feedback and
chapter032 archives remain next. The main manuscript retains 31 packaged chapters.
Earlier exposition is unchanged; substantial rewriting remains deferred as requested.


The CBOR head reader and generic bounded walker now have two tested private MIPS
prototypes: shift-accumulation with recursive traversal, and positional accumulation
with explicit container frames. Each passes 648 head and 1000 walker cases at normal
and above-4-GiB addresses (6592 simulator executions). Eight assembled regressions
are rejected, including lost high counter bits, premature argument reads, signed
count comparisons, duplicate keys, ninth containers and incorrect payload bounds.
Every checked source access stays within the selected head/item; caller input is
unchanged, saved state is retained and private stack accesses are bounded. A new native
worked passage follows both cursor and remaining count through the optional two-byte
identifier-like string. Telemetry decode, exact sizing, encode, cumulative construction,
feedback and chapter032 distributions remain next. The main manuscript still has 31
packaged chapters; earlier prose and human contributions remain unchanged.


Both complete CBOR telemetry prototypes now implement all five memory entries. The
reference uses a private candidate record, recursive walking and threshold-based sizing.
The alternative retains checked input slices, revisits samples after validation, uses
explicit container frames, byte-width sizing and reverse-address argument stores. Both
pass 648 head, 1000 walker and 2436 telemetry cases at normal and above-4-GiB addresses,
for 16336 simulator cases. The telemetry cases include 731 decode, 420 exact-bound, 1260
encode, 18 invalid-record and seven capacity observations. Twelve new assembled
regressions are rejected. Twelve actual MIPS encodings match independent cbor2 decoding
and canonical output. All generators reproduce their checked artifacts exactly.

New native passages inspect exact flat-record bytes, sentinel tails and publication
failure, then compare actual decoder work on three inputs. The 1024-byte optional-data
input projects to seven bytes; output length does not establish traversal work. Recorded
counts are not acceptance rules, isolated algorithm costs, elapsed timings or IDE
captures. Cumulative construction, public feedback, separate chapter032 distributions
and actual captures remain next. The main manuscript still has 31 packaged chapters;
prior chapter exposition and human writing remain unchanged for the later editing pass.


The CBOR reading now interleaves 56 initial native construction steps with the earlier
28 conceptual passages and a new construction convention include. There are 52 safe
unfinished regions, 56 multiple-choice quizzes, 112 progressive hints and 57 exact
recipes. Steps build a complete primitive walker before recursive child handling,
then candidate decoding, helper sizing, exact bounds, head writing and encoding.
The pipeline compares each result's own unit. A paired failure step has distinct
per-recipe encode/decode callers. The preparation helper preserves the entire text
section and full-width unsigned controls and refuses existing outputs. Native TeX
remains composition authority; no build regenerates prose from exercise metadata.

All 114 prepared recipe/layout combinations assemble with warnings treated as errors.
Thirty-two untouched direct-entry/caller cases retain saved state, caller arenas and
no I/O, including the active-frame head helper. These are starter safety checks only,
not verified completed cumulative checkpoints. Four new IntelliJ Light capture plans
are explicitly uncaptured. Private cumulative implementations, checkpoint verification,
public feedback and separate chapter distributions remain next. The main manuscript
retains 31 packaged chapters. Earlier prose and human contributions are unchanged;
the substantial rewriting pass remains deferred as requested.


Private cumulative CBOR execution now verifies 218 direct/caller checkpoint and
continuation observations across all 57 recipes at both address layouts. Checks cover
illustrated registers, private candidates, exact source/record/destination bytes,
retained full-width caller state and subsequent development or success returns. The
counter and identifier recipes now move those pairs first, making their observations
reachable before later field bodies are complete; native text gives the changed offsets.
The inline-size recipe names the successful exit that its branch actually reaches.

Both construction-compatible completed variants also pass all 16336 head/walker/telemetry
entry cases at both layouts. Twelve assembled wide-address accuracy/ownership faults
are rejected; six nested-development variants pass direct and caller checks at both
layouts (24 cases). Twelve actual construction output encodings decode independently
and match canonical host output. All private builders reproduce source, expected-byte
and checking artifacts byte-for-byte. Bounded groups keep the checker within the
existing assembly-work limit without changing simulator/grader semantics. Original
prototype performance figures retain their earlier sources and measurement boundaries.
Public feedback export and separate chapter032 distributions remain next. Actual IDE
captures, remote progression/hint enforcement and independent accessibility validation
remain pending; prior prose is retained for the later substantial edit.


CBOR telemetry is the thirty-second registered draft package and included native
chapter. It retains 56 steps, 52 unfinished regions, 57 recipes, 56 quizzes and 112
hints. Independently authored public feedback has seven completed domains (775 cases)
and 56 standalone checkpoint files (109 cases). Both complete algorithms pass the
completed feedback at both layouts (3100 cases); cumulative and completed guided
sources pass all public checkpoints at both layouts (436 cases). The host model matches
an independent oracle/library for 420 records and 5040 equivalent encodings. Twelve
assembled faults and six unfinished controls are rejected. A valid 4096-byte private
frame and separately placed constant pass another 1550 public cases, so final feedback
does not impose the illustrated frame layout.

The student archive contains 161 exact student-only files, including native assignment
and reading fragments. Package metadata indexes steps and assets without duplicating
the book prose or regenerating composition. Extracted checks pass 457 positive cases
and reject 150 unfinished completed-interface cases. The relocated instructor command
passes 4091 cases each on reference, alternative and larger-frame variants (12273
cases), and refuses existing reports. The private ZIP embeds the exact student ZIP
and pins its digest. The final native-only text refresh retains every tested executable
file hash. Original private keys and solutions remain separate.

The remaining chapter drafts are PNG, small lossless video and APNG. Actual light-theme
captures, remote progression/hint enforcement and independent accessibility validation
remain pending. Earlier chapter prose remains unchanged for the requested substantive
edit after all chapter drafts exist.


The PNG reading foundation is directly composed in nineteen native conceptual passages
at `drafts/png.tex`. It separates chunk admission/CRC, joined IDAT data, zlib/DEFLATE,
filter reconstruction and framebuffer publication. A fixed 115-byte PNG splits its
zlib header across two IDAT chunks and uses all five row filters. The reading traces
its 35 inflated bytes, 30 RGB bytes and ten display words, explains full-width address
retention, overlapping matches and the dependency that prevents a naive Sub vector loop.
One tagged native pipeline diagram, three header-tagged tables and four reasoning
exercises accompany the initial prose. Public input generation refuses existing paths.

All 93 public tests pass. Private host verification decodes 495 generated images with
Pillow 13.0.0, independently reverses the row filters, checks exact stream completion,
CRC domains, payload joining and packed word values, and checks 66265 Paeth triples
against a separate distance derivation. These are host observations, not verified new
MIPS interfaces or performance results. The scalar/MSA smoke decoder has been inspected
for adaptation; its 32-bit pointer assumptions and acceptance details remain explicit
audit work. Cumulative assembly, public/private feedback and chapter033 archives remain
next. The integrated manuscript retains 32 packaged chapters; PNG, video and APNG are
not complete. Actual IDE captures and independent accessibility review remain pending.
The requested substantial editing pass remains deferred; earlier prose is unchanged.


The PNG collector now has two private MIPS64 prototypes: immediate payload copying
and a validation-first rescan with branchless CRC steps. Both retain full-width
addresses and saved state, validate the bounded static RGB8 container, retain source
bytes and publish the descriptor only on success. A native interface passage gives
four argument registers, the five-doubleword descriptor and success/failure ownership.
Both prototypes pass 326 independently generated cases at normal and above-4-GiB
addresses (1304 simulator cases). The domain has 41 accepted and 285 rejected cases,
covering truncated windows, split/empty IDAT payloads, capacity boundaries, dimensions,
palette placement, unsupported and malformed types, CRCs and unsigned count limits.
Eight assembled faults are rejected, including narrowed pointers and failure metadata
stores. Generators reproduce the checked sources and cases from relocated paths.
This is chunk collection evidence only; new inflate/unfilter/blit assembly, cumulative
construction, feedback and separate PNG archives remain next. Actual captures and
independent accessibility review remain pending. The main manuscript retains 32
packaged chapters; earlier chapter prose remains retained for the later editing pass.


The PNG reading now adds three directly authored native passages: an exact-size
inflater interface, a complete small canonical alphabet and a forward match trace.
Two private inflater variants adapt the retained scalar smoke decoder into callable
MIPS64 entries with full-width saves, recoverable failure and per-call mutable tables.
They pass 324 cases each at normal and above-4-GiB addresses (1296 simulator cases),
including the actual PNG teaching streams, stored/fixed/dynamic blocks, 15-bit codes,
maximum distance 32768, overlapping matches, sparse distance alphabets, cross-alphabet
repeats, window bounds, mixed blocks and exact stream/output completion. Ten separately
assembled regressions are rejected. Relocated builders reproduce the checked sources,
Kotlin checks and host observations byte-for-byte.

An independent bit-position model provides expectations. Host zlib 1.2.12 is also
compared; its rejection of 32 unused distance lengths and acceptance of a match beyond
an advertised 256-byte window are recorded differences, not substituted format rules.
The exact-output regression exposed a missing case in the earlier domain: Adler-32
can match after 65521 repeated padding bytes. The new native explanation and public
numerical test distinguish this checksum observation from produced-byte accounting.
All 94 public tests pass. Row reconstruction/blitting, cumulative student construction,
public/private final feedback, chapter033 archives and actual captures remain next.
The integrated manuscript retains 32 packaged chapters; the later substantial editing
pass remains deferred. Independent PDF accessibility validation remains pending.


The PNG row and framebuffer stages now have two private complete prototype variants.
They retain full-width addresses, use a call-private zero row, prevalidate every filter
selector and preserve filter prefixes. Packing validates exact source bytes, native
word alignment, stride and the last active row's extent before publishing pixels.
Scalar/MSA checks cover both memory byte orders and both address layouts: 4400 row
cases plus 400 complete-PNG cases, including full 512-by-512 output and late failures.
Ten separately assembled faults are rejected. The upstream grader now accepts the
standard ByteOrder on each case before assembly (local MARS commit 118b85dc); all 110
CLI tests pass, including default/explicit endianness, native directives and MSA stores.
Unrelated MARS edits remain preserved.

Four new native passages define row and complete-call interfaces, derive the RGB
shuffle and report complete-call instruction counts. A public repeated-gradient helper
reproduces the tested 2854-byte PNG; independent decoding matches all pixels. On that
image, the reference scalar/MSA counts are 37870732 and 18167820, or 144.47 and 69.30
instructions per pixel. Counts include all four stages and exclude file loading,
assembly and external inspection; they are not elapsed timings or IDE measurements.
The private receipt pins the file, decoder, CLI and numerical text. The actual IntelliJ
fixture exporter supplies one new listing while preserving all 766 older exports.
All 95 public tests and three authoring-plugin exporter tests pass. Native PDF checks
remain separate from independent accessibility review. Cumulative student construction,
public/private final feedback, file-loading caller and chapter033 distributions remain
next. Actual screenshots are uncaptured; the integrated manuscript retains 32 packages.


The PNG draft now supplies the file-loading caller and the first ten cumulative
construction steps. Nine editable regions develop full-width capacity admission,
retained progress, short reads, exact-fit probing, EOF acceptance, descriptor cleanup
and result publication. Fourteen observation recipes preserve student instruction
edits; ten quizzes and twenty hints accompany directly authored native passages.
The public starter remains safe to run with its decoder unfinished. Completed private
variants accept equivalent read scheduling and are checked separately from the
illustrated checkpoint registers. All 100 public tests pass; the native PNG reading
builds as 25 pages with Minion Pro and Noto Sans Math. Its nine tables have 32 tagged
headers, and its native pipeline figure retains alternative text. These observations
do not complete an independent accessibility audit.

The new loader/caller domain passes 1088 simulator cases across byte orders and address
layouts, including direct and supplied-main checkpoints. Eight assembled faults are
rejected. Both complete algorithms also decode the original 512-by-512 smoke image
from an actual host file in scalar/MSA modes; every dumped pixel matches independent
Pillow output. The existing upstream PNG smoke suite passes unchanged. Old memory-entry
instruction measurements keep their original sources and boundary. Remaining decoder
construction, final feedback, package integration and actual IDE captures remain next.
The integrated manuscript still contains 32 packaged chapters. Earlier prose is retained
for the requested substantial editing pass after all chapter drafts are written.


The PNG draft now continues through seventeen collector edits after its ten loader
steps. Twenty public recipes and seventeen quizzes/hint pairs index directly authored
native passages. A native header-tagged cursor table tracks the exact 115-byte input;
connected boundary passages distinguish short compressed capacity, a bad final CRC and
a correctly checksummed metadata gap between IDAT chunks. The attachment helper preserves
student loader instructions and extra functions, refuses a changed decoder tail, and
adds a temporary collector-only caller without completing any student region.

The construction-compatible collector and original two-pass alternative pass 2608
completed-interface cases across both byte orders and address layouts. Cumulative direct,
helper and supplied-main observations add 168 cases, including an untouched control;
partial-wrapper bounds, modes and early-count checks add 52, for 2828 simulator cases.
Eight assembled faults are rejected. Relocated builders reproduce sources/checks and
receipts pin the public controls and private dependencies. The new 128-byte illustrated
frame and original 144-byte alternative both pass the same completed contract. Earlier
collector and memory-decoder receipts keep their original measurement boundaries.

All 106 public tests pass. This is still an unregistered PNG construction draft, outside
the 32 packaged chapters in the main manuscript. Inflater, row reconstruction, packing
and final-call construction, completed feedback, chapter distributions and actual IDE
captures remain pending. Earlier prose is retained for the later substantial editing
pass; remote progression/hint enforcement and independent accessibility auditing remain
unfinished. No MARS source or reference IntelliJ checkout was changed in this increment.


The PNG reading now continues through sixteen stored-inflater edits, steps 28–43.
Directly authored native passages explain separate compressed/output windows, reserved
trailer bytes, bit refills and zero-bit requests, stored alignment, LEN/NLEN, dual copy
bounds, final/nonfinal blocks, exact completion and Adler comparison. A native cursor
trace separates next-byte position, buffered bits and produced rows. Sixteen quizzes,
thirty-two hints and twenty-three public recipes accompany the prose. Attachment keeps
student loader/collector edits and extra functions while replacing only the supplied
temporary wrapper. Neither context outputs nor framebuffer words are published yet.

The stored-only construction, batched-Adler alternative, two additional valid frame
arrangements, and retained full inflater pass 2260 completed stored-domain cases across both byte
orders and address layouts. Cumulative direct/caller checkpoints add 192 cases, repeated
same-session bit probes add eight, and wrapper row-capacity/early-pointer checks add
52, for 2512 simulator cases. Eleven assembled faults are rejected. The exact-output
fault is rejected by the real Adler collision, in both byte orders. Relocated builders
reproduce the checked sources, expected streams and checking scripts; receipts retain
private adaptation provenance and exact hashes. No new grader operation was required:
existing same-session callFunction supports the bit-probe sequence.

All 113 public tests pass. Fixed/dynamic handlers remain explicit development exits in
this intermediate construction, so stored success is not a completed inflater grade.
The public fixture helper verifies the selected auto-compressed observation actually
has a dynamic first block, rather than assuming every auto stream does. Huffman and
row/packing construction, final feedback, chapter033 distributions and actual IDE
captures remain next. The integrated manuscript retains 32 packaged chapters; video
and APNG remain to be composed. The requested substantial prose revision stays deferred
until the chapter drafts exist. Independent accessibility auditing and remote
progression/hint enforcement remain unfinished. MARS and the reference IDE are unchanged.


The PNG construction now reaches step 70. Twenty-seven newly authored native passages
develop fixed lengths, canonical counts and symbol rank, code-bit arrival, literal and
match bounds, forward overlapping copies, dynamic alphabets and repeat expansion.
Native frame, canonical-rank and bit-cursor tables support the explanations; twenty-seven
quizzes, fifty-four hints and thirty-five recipes accompany the construction. Earlier
prose and measurements are retained. Fixed and dynamic development exits are replaced
only when students attach the new unfinished stage to their own work.

The comprehensive private gate passes 6868 simulator cases across byte orders and
ordinary/above-4-GiB layouts, rejects fourteen assembled faults, and passes ten actual
file runs. It includes five completed implementations/frame arrangements, all cumulative
direct/caller observations and six-call same-session success/failure sequences. Host
zlib, an independent bit-position oracle and image decoding verify the public fixtures.
All 120 public tests pass. Row/packing construction, final feedback, PNG distributions,
actual captures, video and APNG remain unfinished; 32 packaged chapters remain integrated.
The requested major prose editing pass remains deferred until all drafts exist.


PNG steps 71–83 now develop reconstruction through thirteen unfinished regions.
The directly authored native passages explain shape admission, complete selector
prevalidation, zero upper boundaries, row/sample cursors, predictors, wrapping
restoration and bounded MSA Up groups with tails. Thirteen quizzes, twenty-six
hints and nineteen public recipes accompany the prose. A native row-window table
separates scanline bytes from sample extents. The cumulative order was checked and
corrected so shared row progression and sample storage precede later predictors.

The reconstruction gate passes 2232 simulator cases across both byte orders and
address layouts, rejects nine assembled faults and passes twelve actual-file runs.
Three completed variants include an equivalent predictor formulation and a different
frame layout. Reuse sequences cover success, invalid late selectors and fresh
first-row boundaries in one session; full-size scalar/MSA callers compare all
786944 reconstructed bytes. The Average fault controls include sums above 255.
All 126 public tests pass. The temporary caller stops after reconstruction without
publishing pixels. Packing/publication construction, final PNG feedback and
distributions, actual IDE captures, video and APNG remain unfinished. The integrated
manuscript still has 32 packaged chapters. Earlier prose and instruction measurements
are preserved, with the substantial editing pass deferred as requested.


The PNG functional construction now reaches step 100. Seventeen native passages
complete scalar packing, immutable shuffle masks, bounded MSA packing, two diagnostic
return observations and final context publication. Fifteen new editable regions,
seventeen quizzes, thirty-four hints and twenty-seven public recipes accompany the
text. A native active-row/extent table distinguishes forty active bytes from the
worked destination's 280-byte minimal extent. A closing native section explains
submission evidence, equivalent free-writing solutions and success/failure reuse.
Earlier prose and instruction measurements are retained.

The comprehensive gate passes 4036 simulator cases, rejects thirteen assembled
faults and passes twelve actual-file runs. Packing domains cover both byte orders
and both address layouts; completed decoders include equivalent word construction
and a larger caller frame. Twenty-four six-call reuse cases (144 calls) preserve
earlier published results across failure. Ten successful file runs compare exact
framebuffers, including scalar/MSA full-size output; two malformed-file runs check
the failure result. Untouched sparse framebuffer memory has no initialized words
for the CLI dumper, so seeded native/cumulative/reuse checks supply failure-memory
evidence. Existing grader memory-access APIs were sufficient; no MARS change was
needed. All 133 public tests pass.

The working caller now reports successful PNG display after final result publication.
Final feedback packaging, separate distributions, manuscript integration and actual
annotated IDE captures remain unfinished. The integrated manuscript still contains
32 packaged chapters; video and APNG remain to be composed. The requested broad prose
rewrite remains deferred until all chapter drafts exist.


Chapter 34 now has a directly authored native reading composition. Its MLV1 exercise
format fixes 64-by-64 geometry and describes full RGB frames, XOR skip/change commands,
CRC coverage, bounds, periodic/optional full frames, validation before mutation,
indexing, seeking, encoder passes, logical deadlines and performance boundaries.
Worked native tables and a semantic timeline figure explain concrete payloads,
frame offsets and replay paths. Four public fixture bundles provide exact PPM
references without exposing a video decoder. The private independent host model
checks all 22 reference frames and every seek target, bitwise CRCs, equivalent
encodings, maximal payloads and 74 malformed files.

The first six assembly construction steps develop a native-word XOR/count kernel,
with six quizzes, twelve hints and twenty-six public recipes. Equivalent signed and
unsigned extraction implementations, both byte orders, normal/high addresses, exact
aliases, empty/invalid admission and cumulative direct/main calls pass 3944 simulator
cases. Eleven assembled faults are rejected; ten ordinary caller runs print the
correct counts. Reuse checks cover XOR involution and admission after earlier calls.
For 4096 words the measured construction interval is 53269 scalar instructions and
29721 MSA instructions, 44.2 percent fewer; file coding, CRC, printing and display
are excluded, and observation NOPs remain included. A zero-group count shortcut is
posed as a measured future experiment, not implemented or claimed as a speedup.

All 141 public tests pass. This is a verified reading foundation and first kernel,
not a completed assembly file encoder/decoder/seeker or playback implementation.
Those construction stages, package feedback/distributions, integration and actual
IDE captures remain open, as does APNG composition. The main manuscript still has
32 packaged chapters. Earlier prose and source exports remain intact; the requested
broad editing pass remains deferred.


Chapter 34 now reaches scalar record application at step 23. Seventeen additional
native passages connect early argument checks, retained context, exact source
windows, checksum recurrence, cursor bounds, validation-before-write, native RGB
packing and final publication. They add seventeen editable regions, seventeen
quizzes, thirty-four hints and thirty-seven public recipes. The late-skip input
has a valid checksum and a valid initial change, so its failure specifically
requires whole-image and context preservation. Guided checkpoints remain separate
from behavior-based free-writing checks.

The separate record gate passes 1012 simulator cases, rejects thirteen assembled
faults and verifies eight ordinary callers. Three valid implementations cover
conditional CRC, equivalent mask-based CRC and a different private stack size,
with both byte orders and normal/high addresses. Twelve persistent-call cases
make 108 calls, including failure between accepted frames. Work measurements show
the mask-based CRC requires about four to five percent more instructions on the
measured inputs; the conditional version is retained. The earlier isolated XOR
kernel measurements are unchanged. All 148 public tests and existing package
validation/build commands pass. This is a single-record routine; full-file
reader, index, seek, encoder and playback assembly remain to be constructed.
The main manuscript still has 32 packaged chapters; video/PNG final packaging,
APNG, actual annotated IDE captures and independent accessibility verification
remain unfinished. The author-requested broad rewrite remains deferred.


Chapter 34 now reaches complete-file index publication at step 36. Thirteen new
native passages develop bounded global-header parsing, a private scratch/context
interface for nested record calls, exact input completion and a second publication
pass over immutable bytes. The native index-context table makes input storage
promises, output metadata and retained fields explicit. Thirteen quizzes,
twenty-six hints and twenty-five recipes accompany the unfinished regions.
Attachment preserves the student's record instructions and gives the previous
example caller a distinct entry name.

The index gate passes 868 simulator cases, rejects eleven assembled faults and
verifies eleven ordinary callers. Conditional and equivalent mask-based record
validators and a larger private index frame pass both orders/layouts. Twelve
six-call reuse cases make 72 calls, including successful reindexing after late
rejection and shorter results retaining their unused directory suffix. All 155
public tests and 60 existing package validation/build commands pass. The ordinary
caller retains the full conservative file arena. Private grouped observations
compact unused file tails; above-four-GiB tests use a bounded 128-KiB fixture arena
within the simulator's fixed four-MiB wide-data window. Every supplied input byte,
semantic bound and ordinary full-arena run is retained.

This stage indexes a complete file already in memory. Actual file loading and EOF
probing, seeking, encoding and presentation are still to be constructed. The main
manuscript still contains 32 packaged chapters. PNG/video final packaging, APNG,
automated annotated IDE capture and independent accessibility verification remain
open. Earlier native prose and instruction-count measurements are preserved; the
author-requested broad prose rewrite remains deferred.


## Author clarification: final chapter and stopping boundary

Complete chapters 33 (PNG) and 34 (small lossless video codec) before beginning
chapter 35. Chapter 35 replaces the earlier APNG extension with an actual H.263
encoder using a restricted, explicitly documented interoperable feature set.
These are planning notes; no H.263 implementation is part of the current PNG/video
work. Preserve the earlier APNG mentions as historical planning, superseded here.

The encoder input is raw digital frames generated by a Python script, at QCIF
resolution (176 by 144), no more than 30 frames per second, and no more than ten
seconds. These are ceilings, not a required worst-case default. Choose a shorter,
lower-rate default to keep MARS execution practical, and measure its actual cost
before selecting the teaching workload. Document raw channel order, plane layout,
strides, chroma sampling, range and timing explicitly. The exact raw representation
and accepted H.263 syntax remain to be designed after chapters 33 and 34 are complete.

Students are assumed fluent in MSA by this point. Concentrate on codec algorithms,
vector block work, bounds and numerical accuracy; do not repeat introductory MSA
instruction lessons. Candidate vector work includes motion-estimation differences
and reductions, prediction/reconstruction, block transforms and quantization.
Use an independent H.263 decoder to check actual interoperability, and define
quantization/transform comparisons according to the chosen normative contract.
Do not label a teaching-only bitstream as H.263 or silently substitute a decoder
exercise for the requested encoder input/output direction.

Substantive editing is a separate future goal. Finish the initial chapter drafts
and the supporting work already agreed for this goal, then stop. Do not start
the substantive rewriting pass automatically or use it to extend this goal.


The chapter 34 file reader construction now reaches step 45. Nine native passages
explain partial transfers, bounded positive requests, signed service results,
private exact-fill EOF probes, ownership/cleanup and publication of a nonempty
loaded count. The combined actual caller loads then indexes and distinguishes
transfer failure from malformed video. A native result-context table and explicit
staging-buffer lifetime explanation prevent treating an earlier count as proof
that overwritten backing bytes still contain the previous file.

The repeatable loader gate passes 864 simulator cases, rejects ten assembled faults
and checks eleven real host-file runs. Three implementations vary request limits
and private frame size. Twelve five-call reuse cases make 60 calls. The full
7344152-byte capacity and one extra byte are tested in both orders for all three
implementations, comparing every loaded byte and monitoring probe guards. Native
full-capacity guided recipes run in the normal data layout; completed-function and
caller-integration checks also exercise bounded capacities above 4 GiB. All 162 public
tests pass. The existing grader APIs were sufficient; upstream MARS is unchanged.
Remaining chapter 34 construction is seek, encoder and playback. Complete PNG and
lossless video before implementing the newly planned QCIF H.263 encoder chapter.
Substantive editing remains outside this goal.

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

The exact relocated student archive contains 254 files and eleven public domains
with 258 cases. Private checks and 122 quiz keys remain in the instructor repository.
The completed package gate passes 6,264 cases across three implementations, including
one with submission-owned constants and additional 4,096-byte entry frames. It
rejects both unfinished starters and five assembled faults, including ignored path
arguments, source-stride errors, short-write progress and signed lateness ordering.
Extracted runners pass 92 public and six selected private cases and refuse existing
reports without changing them. Two ordinary real-clock callers produce files that
an independent host decoder recovers to all eight original frames.

All 215 public tooling tests, seven highlighter tests, 64 package validation/build
commands and seventeen native-composition workflow validations pass. The reading
has 101 pages and the assignment has three; the integrated manuscript has 1,569
pages. All 101 video body pages match the standalone text and coordinates. The
first 95 reading pages match the earlier rendered pages exactly; fresh contact sheets,
enlarged closing pages, the standalone assignment, contents and chapter boundary
were inspected. Fonts are embedded with Unicode maps. The chapter retains 122
observation passages, 22 tagged tables, 79 header cells and one described native
figure. These checks do not certify PDF/UA compliance or student usability.

Chapters 33 and 34 now have initial native text/packages in the manuscript. Their
five PNG and six video light-theme IDE figures remain uncaptured; independent
accessibility review remains pending. H.263 stays at the documented planning stage
until the preceding chapter work is complete. Substantive editing remains the
separate future goal requested by the author.
