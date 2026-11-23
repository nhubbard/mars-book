# Main book and lab book revision

These are directly edited native LaTeX drafts for the authorized restructuring goal.
The initial manuscript remains at the original `main.tex` and `includes/` baseline.

- `learning.tex` includes the twenty-seven main-book chapters: the complete console
  program, then integer representation through a byte inspector and a word-width
  experiment, addresses and memory through a complete pixel program, then decisions
  and repetition through a bounded seven-segment range counter. Functions and
  full-width stack frames lead into arrays and images through a complete sprite
  renderer and its horizontal reflection. Integer line drawing then develops
  an invariant, dependent state updates and a bounded raster contract. Life then
  extends snapshot reasoning to padded boards and complete generation transitions.
  Sorting then compares invariants, stability and measured routine versus whole-program work.
  Fixed-point motion then separates scaled state, display rounding and arithmetic range.
  Matrix transpose then develops address recurrences, cache-line reuse, clipped tiles
  and placement conflicts with repeatable core measurements. Bounded packet parsing
  then joins availability proofs, explicit byte order, ordered errors and delayed output.
  The advanced sequence begins with exact scalar matrix products, partial sums and
  scoped instruction/cache observations. RGB conversion then introduces MSA lanes,
  bounded overlapping loads, derived shuffle controls and scalar tails. PCM mixing
  then derives clipping evidence, mask reduction, exact aliases and tail behavior.
  FIR filtering follows with Q15 units, exact wide sums, one final rounding and
  explicit stream history. Compact encodings then derive full-width differences,
  canonical bytes, atomic prefixes and measured representation tradeoffs. PPM files
  then connect bounded headers, RGB representation, partial transfers, descriptor
  ownership and actual host artifacts. CRC-32 follows with raw versus finalized
  state, bit/table equivalence, preparation costs and streamed file checks. QOI then
  develops remembered decoder state, RGBA preservation, validated publication,
  equivalent encodings and reflected output placement. CBOR then separates head,
  item and schema validation through nested continuations and deterministic projection.
  ChaCha20 then connects dependent word arithmetic, column/diagonal selections,
  MSA lane arrangement, bounded byte output and immutable stream inputs.
  WAV, PNG and lossless video then develop complete pipelines and independent
  extensions. The final two H.263 chapters connect numerical prediction and
  reconstruction to real stream/file publication and independent decoding.
  The chapters use connected explanations
  and debugger reasoning.
- `advanced-pilots.tex` includes the PCM mixing chapter and H.263 local
  reconstruction excerpt. They assume their stated prior knowledge and use worked
  reasoning and independent exercises, without reproducing the old lab checkpoints.
- `lab-book.tex` includes the twenty-seven written lab supplements: predictions,
  observations, fault diagnosis and independent variations. The first seventeen main units have native interactive support. Later units and
  unfinished companion lessons currently use ordinary Run and Debug.

For the project-to-chapter coverage index and repeatable structural checks, see
[the revised curriculum audit](../docs/revised-curriculum-audit.md). It distinguishes
original package prerequisites from revised introductory exercises.

## Reading the current proofs

For restructuring review, open these local build outputs:

| Reading | PDF |
| --- | --- |
| Main-book chapters 1–27 (189 pages) | [build/native/learning.pdf](../build/native/learning.pdf) |
| Lab supplements 1–27 and companions (209 pages) | [build/native/lab-book.pdf](../build/native/lab-book.pdf) |
| PCM mixing and H.263 reconstruction pilots | [build/native/advanced-pilots.pdf](../build/native/advanced-pilots.pdf) |

These PDFs are generated locally; the native LaTeX files in this directory are the
publication sources. The author has accepted the current explanatory direction
for continued work. An independent walkthrough of the complete book remains an
acceptance exercise, not a completed review.

Build with `python3 tools/book.py build --entry revision/learning.tex` and
`python3 tools/book.py build --entry revision/lab-book.tex`, and
`python3 tools/book.py build --entry revision/advanced-pilots.tex`. The normal publication
lock serializes these builds. `export-input` retains original manuscript and
specimen snippets and adds the revision entries for actual IntelliJ highlighting.

The first prediction segment is integrated into the main MARS plugin and verified
in the actual IDE. Source-bound Run and Debug observations are also verified in
the actual IDE. A fourth segment now verifies the retained answer, output argument
and service selector through prediction and actual Debug pauses. Diagnosis and
repair are also verified in the IDE, using separate counterexample and repair
files. The repair accepts equivalent instruction spelling and survives restart.
Independent writing now uses a separate skeleton and five public behavioral cases,
with current editor text, cached assembly and a fresh machine per case. The IDE
check verifies starter rejection, an equivalent program, edit invalidation and
restart preservation. Graduated optional hints are implemented and tested;
native course controls now release hints, optionally require prediction quizzes,
and govern free-writing availability. Tools → MARS Learning explicitly offers
guided work or free writing, preserves source and hint history, and remembers the
last guided segment across a fresh IDE process. Broader instructor policies
remain open. The independent exercise is also verified
across native project save/close and fresh IDE processes, including an offline
source edit; fresh-process guided restoration now verifies all six guided segments. Unchanged work retains completion; an offline edit to the worked source requires new Run/arithmetic Debug/output observations while predictions and the separate diagnosis/repair histories survive. This restores completed evidence, not a running machine or an intermediate Debug trace. The progress service persists content-specific evidence. Its code and
Jupiter verification are documented upstream in `docs/student-instruction-system.md`.
The full goal includes the actual integrated lesson, further editorial pilots,
all 35 projects and human review where available. These pilot PDFs do not
complete that goal and have not been validated with students.

The advanced review PDF has twelve pages: the expanded PCM chapter on pages 1–7
and the H.263 reconstruction excerpt on pages 8–12. These are review compositions, not the final
chapter numbering. The literal PCM group listing is executable with:

```sh
python3 tools/check_revision_pcm.py --java /path/to/java --cli /path/to/mars-cli.jar --output /new/report/directory
```

That check runs 128 complete groups, verifies the clipping-count accumulation and
compares all data bytes, including guards and exact aliases. It does not claim to
check an unwritten scalar tail. Current public/private verification records bind
the actual code, highlighter export and PDF; author and learner review are open.

The integrated first unit now uses concise navigation names and fits an undersized side-docked Learn pane when opening its source. Wider user layouts are preserved. The separate authoring driver records native text/button geometry and rejects horizontal clipping at the captured viewport. Human readability and learner reviews remain open; this UI correction does not finish the instructional system.

The first unit also provides confirmed reset and recovery through Tools. A saved recovery copy survives native close and a fresh IDE process. Recovery preserves source edits and restores quiz/hint history while requiring execution checks again; an empty reopened activity cannot replace the copy. The written lab explains the controls. Its first-unit section has renewed automated accessibility verification; human review remains open.

The new integer chapter begins on page 6 of the main proof. Its written lab begins
on page 8 of the lab proof. See [integer-unit.md](integer-unit.md) for executable
verification, downloadable sources and the remaining native lesson work.

The addresses and memory chapter begins on page 12 of the main proof, and its
written lab begins on page 11 of the lab proof. See [memory-unit.md](memory-unit.md)
for the literal program, checks and remaining interactive work.

The decisions and repetition chapter begins on main page 17, and its written lab
begins on lab page 14. See [branch-unit.md](branch-unit.md) for the worked loop,
verification scope and remaining native learning work.

The functions and stack chapter begins on main page 24, and its written lab begins
on lab page 22. See [functions-unit.md](functions-unit.md) for the call contracts,
independent function and technical verification.

The arrays and images chapter begins on main page 31, and its written lab begins
on lab page 25. See [arrays-unit.md](arrays-unit.md) for the complete sprite renderer,
vertical-reflection exercise, two verified native bitmap captures and remaining
interactive work.

The integer algorithms chapter begins on main page 37, and its written lab begins
on lab page 35. See [lines-unit.md](lines-unit.md) for the complete compact rasterizer,
exhaustive endpoint checks, native diagram and independent dashed-line exercise.

The state and buffers chapter begins on main page 45, and its written lab begins
on lab page 38. See [state-unit.md](state-unit.md) for the complete Life program,
source-snapshot checks, native generation diagram and independent changed-cell count.
The revised ring-buffer companion follows in section 8.6 on pages 41–46.
See [ring-unit.md](ring-unit.md) for the independent queue assignment, compact
starter, wraparound experiment and verified annotated Memory observations.

The algorithms and cost chapter begins on main page 52, and its written lab begins
on lab page 47. The current proofs contain 98 main-book pages and 90 lab-book pages.
See [sorting-unit.md](sorting-unit.md) for the complete two-sort visualizer,
repeatable measurements, stable-partition exercise and remaining interactive work.

The revised maze companion follows as section 9.6 on lab pages 50–56. Chapter 9 of
the lab book is now titled “Sorting and search”. See [maze-unit.md](maze-unit.md)
for the coordinate experiment, BFS reasoning, independent search task and two
verified native route observations with native LaTeX annotations.

The fixed-point chapter begins on main page 59, and its written lab begins on
lab page 57. See [fixed-point-unit.md](fixed-point-unit.md) for the complete ball
program, signed interpolation exercise and three verified native IDE observations.
Unit verification notes retain proof sizes at their own milestones; the totals
in this README describe the latest combined proofs.

The memory access and locality chapter begins on main page 65, and its written lab
begins on lab page 61. See [locality-unit.md](locality-unit.md) for the complete
transpose comparison, cold-cache measurement tool, independent clockwise rotation
and annotated native cache observations.

The bounded interfaces chapter begins on main page 72, and its written lab begins
on lab page 66. See [bounded-unit.md](bounded-unit.md) for the complete packet
parser, independent binary delimiter search, bounded access checks and four
verified native observations presented in two annotated comparisons.

The UTF-8 companion follows as section 12.6 on lab pages 70–76. See
[utf8-unit.md](utf8-unit.md) for persistent-state reasoning, the independent
scanner starter, full scalar/pair domain checks and native annotated Memory
comparisons. The main-book proof is unchanged by this companion.

The clipped-sprite companion is section 6.6 on lab pages 28–34. See
[clipped-unit.md](clipped-unit.md) for the half-open interval experiment, descriptor
and stride reasoning, independent blitter, and native annotated copy observations.
The page references above follow the current combined proof; individual unit
verification notes retain their earlier milestone page numbers.

The MarsBot companion is section 4.7 on lab pages 16–21. See
[marsbot-unit.md](marsbot-unit.md) for the complete feedback-controlled east leg,
independent square task, all documented side/rate checks and native one-step
movement-state comparison. All original projects 001–016 now have revised written
treatment; advanced compositions and the remaining student instructional system
are still open. This is a written-foundation milestone, not goal completion.

Chapter 13 begins the advanced sequence on main page 79 and lab page 77. See
[measurement-unit.md](measurement-unit.md) for the complete scalar product comparison,
repeatable cold/warm/reset/launch measurements, independent matrix-vector reduction
and three verified native Debug captures with LaTeX annotations. This adds revised
written treatment of original project 022; the other advanced projects remain open.

Chapter 14 starts on main page 86 and lab page 82. See [msa-unit.md](msa-unit.md)
for the complete RGB888/RGB565 example, independent RGB555 exercise, equivalent
scalar/vector checks and two verified native lane observations. This adds revised
written treatment of original project 019; PCM mixing follows in Chapter 15.

Chapter 15 starts on main page 92 and lab page 86. See [pcm-unit.md](pcm-unit.md)
for the complete two-input mixer, independent saturated difference, exact
mask/count and in-place-tail observations, and renewed native proof validation.
The advanced review entry uses this same expanded PCM source. The five audited
PCM/RGB/maze leaf callers now establish the book's sixteen-byte stack alignment.

Chapter 16 starts on main page 99 and lab page 91. See [fir-unit.md](fir-unit.md)
for chronological eight-tap windows, widening before pair overflow, explicit
history, independent decimation and three verified native Debug observations.
The combined review proofs now have 104 main pages and 95 lab pages; the advanced
PCM/H.263 excerpt remains unchanged at twelve pages. Original project 021 now has
revised written treatment. Native delivery of later units remains open.

Chapter 17 starts on main page 105 and lab page 96. See [delta-unit.md](delta-unit.md)
for the complete delta codec, independent read-only scanner, candidate/prefix
observations and repeatable byte-size/instruction-work comparisons. The main
proof now has 111 pages and the lab proof 100. The advanced PCM/H.263 excerpt
remains unchanged. Base64 now follows as the encoding companion in section 17.7.

The Base64 companion is section 17.7 on lab pages 100–105. See
[base64-unit.md](base64-unit.md) for sextet/character reasoning, canonical padding,
whole-buffer validation, exact vector footprints, the independent unpadded
URL-safe encoder and three verified native Debug captures. The lab proof has
105 pages; the main proof and advanced excerpt are unchanged by this companion.
The next main chapter develops bounded file I/O and binary PPM.

Chapter 20 begins on main page 126 and lab page 116. See [qoi-unit.md](qoi-unit.md)
for all six QOI operations, independent reflected decoding, strict host-file
verification and five annotated native Debug observations. The combined proofs
have 132 main pages and 122 lab pages; the advanced pilot remains unchanged.
The QOI guided lesson and later curriculum work remain open.

Chapter 21 begins on main page 133 and lab page 123. See [cbor-unit.md](cbor-unit.md)
for bounded nested traversal, late-error publication, exact preferred sizing,
independent sequence scanning and four verified native Debug observations. Current
proofs have 139 main pages and 127 lab pages; the advanced excerpt is unchanged.
The expression/bytecode companions and later native lessons remain open.

The expression-evaluator companion is section 21.6 on lab pages 128–133. See
[expression-unit.md](expression-unit.md) for grammar-driven parsing, pending state,
checked arithmetic, procedural diagnostics and three verified native Debug observations.
The full evaluator remains an independent assignment; a separate complete arithmetic
example supports the explanation. The lab proof now has 133 pages. Main and advanced
proofs are unchanged. Original project 018's bytecode companion is next.

The bytecode-interpreter companion is section 21.7 on lab pages 134–141. See
[bytecode-unit.md](bytecode-unit.md) for the guest/native distinction, whole-image
validation, countdown state, failure boundaries, reproducible fixtures and four native
Debug observations. The lab proof now has 141 pages. Main and advanced proofs are
unchanged. Original projects 017 and 018 now have revised written companion treatment;
their native lessons remain open. The next main chapter develops ChaCha20 word operations.

The Chapter 22 scope and finite qualification are recorded in [chacha-unit.md](chacha-unit.md).

The SHA-256 companion is section 22.7, lab pages 146–153. It connects schedule and
round dependencies to incremental contexts, padding and file completion. The shared
lab chapter is now “Word transformations.” Its finite qualification and remaining
review are in [sha-unit.md](sha-unit.md); the main proof remains 145 pages.

The RSA-arithmetic companion is section 22.8, lab pages 154–161. It develops
unsigned products, explicit carries, modular invariants and full-width exponents,
with an independent seven-entry skeleton and four actual Debug figures.
[rsa-unit.md](rsa-unit.md) records the added public feedback case and finite evidence.
The main proof remains 145 pages; native advanced lessons and human reviews remain open.

Chapter 23 follows a complete WAV image through parsing, per-channel FIR filtering,
canonical encoding and file ownership. Its lab independently reverses whole frames,
including exact aliasing, after the worked filter. The main proof now has 154 pages
and the lab 165. [wav-unit.md](wav-unit.md) records the 3087 positive grading-case
executions, fourteen rejected faults, fourteen host observations, four actual native
Debug figures and remaining human/native-lesson review. PNG and video treatment follow.

Chapter 24 follows the PNG pipeline across five representations, with worked bit,
match, row-prediction and vector-packing reasoning. Its lab assigns an independent
forward filter and supplies a host wrapper for student-produced rows. The main proof
now has 162 pages and the lab 170. [png-unit.md](png-unit.md) records 10626 positive
qualification-case executions, fourteen rejected faults, thirty-two real-file
observations, four new native Debug figures and remaining review. Lossless video is next.

The revised lossless-video unit is described in [lossless-unit.md](lossless-unit.md).
It adds Chapter 25, its written lab, a complete worked codec/player and an
independent changed-pixel-bounds exercise. The main proof is now 171 pages and the
lab 175 pages. H.263 compositions and advanced native delivery remain open.

The first H.263 unit is described in [h263-prediction-unit.md](h263-prediction-unit.md).
Chapter 26 follows prediction, transform/quantization and local reconstruction; its
lab adds an independent signed reconstruction exercise. Both current proofs are
180 pages. Stream coding and independent decoding follow in Chapter 27.

The final narrative unit is described in [h263-stream-unit.md](h263-stream-unit.md).
The table above gives the current proof sizes; earlier unit notes retain their
publication-milestone counts. Coverage/prerequisite audit, advanced native lessons
and the remaining instructional-system requirements are still open.


A draft downloadable prediction asset for the second chapter is under
[lessons/004-byte-inspector](lessons/004-byte-inspector/README.md). Its four
questions use the existing byte-inspector program. The plugin validates the
versioned content before archive preview; native lesson rendering and resumption
are the next integration step. The draft does not replace the chapter, written
lab, execution investigations or independent variations, and does not alter
publication composition.


The sixteenth native unit now joins the FIR chapter and written lab. See
[fir-unit.md](fir-unit.md#native-fir-integration-22-november-2026) for the complete
route, invocation-scoped grading correction, exact regression and two real IDE
walkthroughs. The current 204-page lab proof places the FIR supplement on 109–114.
Human walkthrough and accessibility review remain open. The next native unit is
compact integer encoding, followed by its Base64 companion.


The seventeenth native unit now integrates compact integer encoding and its
independent read-only scanner. See
[delta-unit.md](delta-unit.md#native-compact-encoding-integration-22-november-2026)
for the complete original corpus, bounded assessment batches, two actual IDE
walkthroughs and final publication evidence. The current lab proof has 206 pages;
the delta supplement is on 115–120 and the existing Base64 companion is section
17.8. Human walkthrough and accessibility review remain open. Native Base64 is next.


The Base64 companion now has its complete native route and independent URL-safe
encoder assessment. See
[base64-unit.md](base64-unit.md#native-base64-integration-22-november-2026) for
complete original-case preservation, symbolic workspace setup, scalar/vector
observations, two actual IDE walkthroughs and the final native publication
review. The current lab proof has 207 pages; section 17.8 occupies 120–127.
Human walkthrough and accessibility review remain open. The next native main
unit is bounded file I/O and PPM images.


The PPM unit now joins the native route with ten inline predictions, 23 Debug
observations, three ordinary Runs and five assessments. The revised Chapter 18
companion explains publication boundaries and the distinct evidence from memory,
real files and the separate deterministic short-transfer figure. See
[ppm-unit.md](ppm-unit.md#native-ppm-integration-22-november-2026) for exact-corpus,
actual IDE first/resume, host-file and publication qualification. The current lab
proof has 209 pages; Chapter 18 occupies 128–134. All existing PPM annotations
remain native LaTeX. Human learner and assistive-technology review remain open.
CRC state across reads is the next native unit; the broader goal remains active.
