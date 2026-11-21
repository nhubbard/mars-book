# MARS textbook restructuring proposal

Date: 20 November 2026  
Status: Proposed editorial plan. Implementation is a separate task.

## Purpose and decision

Restructure the 1,819-page initial manuscript into two teachable volumes with a
combined working budget of 660 pages. Organize the explanation around machine
concepts and increasing student independence. Retain all 35 projects in the
curriculum, with selected projects developed in print and the remaining full
construction sequences available as companion laboratories.

The first draft is source material for this revision. Existing prose, code,
checks and figures must earn their place through their contribution to learning.
Technical verification establishes observed program behavior; it does not establish
that an explanation teaches the reader to produce that behavior independently.

This plan authorizes no chapter rewrite, deletion, package change or new simulator
feature. The initial draft completion record remains a historical record. The
current planning task produces this proposal only.

## Intended reader and learning outcomes

Assume that the reader understands variables, expressions, conditionals, loops and
functions in another programming language. Assume no assembly experience. Briefly
recover a prerequisite when an example needs it; avoid repeated lessons in ordinary
computer use.

By the end of Volume I, the reader should be able to translate a small algorithm
into assembly, explain its register and memory state, write and call a function,
reason about widths and bounds, use the debugger to investigate a fault, and test
an implementation against a stated contract.

By the end of Volume II, the reader should be able to choose and measure a scalar
or MSA implementation, handle structured input and files, explain a codec pipeline,
and integrate independently tested functions into a larger program. Familiarity
with a worked example should lead to an unfamiliar but related implementation.

## Editorial diagnosis

The manuscript has 1,505 instructional steps and 144 verified IDE figures. The
current first chapter interleaves step, prose and fixture includes. The H.263
chapter includes construction sequences whose individual steps use numbered
sections, observations and multiple-choice questions. These structures support
fine-grained interaction but interrupt sustained explanation when printed in full.

The resulting problems are excess sectioning, repeated procedural language,
implementation detail preceding motivation, and advanced work that retains the
same degree of guidance as the first program. Shortening sentences alone will not
correct that organization. The revision must change the unit of teaching from an
editor action to an idea, an algorithm or a design decision.

## Publication architecture

Maintain four distinct products with explicit relationships:

| Product | Purpose | Material it owns |
| --- | --- | --- |
| Two printed volumes | Explain concepts and develop independent reasoning | Connected exposition, selected worked programs, traces, conceptual figures, diagnostic examples and exercises |
| Public companion laboratories | Supply executable project work | Starters, supported contracts, optional extensions, longer construction guides, public checks and fixture generators |
| Guided tutor content | Support students who need smaller increments | Checkpoints, contextual hints, editor focus, per-step quizzes and recoverable progress |
| Private instructor distribution | Support assessment and teaching | References, alternatives, answer keys, private checks, rubrics and teaching notes |

All 35 projects remain available. Inclusion in the companion collection does not
require printing its complete implementation instructions. A project can supply a
worked example, a substantial assignment, an elective lab or several illustrations.

Keep the publication as directly edited native LaTeX with ordinary includes.
Exercise metadata may link a passage to a task but must not determine the order
or granularity of the printed explanation. Human passages can be included at any
appropriate point in that sequence.

## Proposed volumes and page budgets

Budget pages using the existing readable typography, with figures, listings and
exercises included. Do not achieve the budget by reducing font size, compressing
spacing or hiding essential explanations in footnotes. The figures below are
editorial allocations to test in the pilots, not completed pagination estimates.

| Allocation | Volume I | Volume II | Total |
| --- | ---: | ---: | ---: |
| Main chapters | 276 | 326 | 602 |
| Frontmatter, glossary, selected references and index | 24 | 34 | 58 |
| Working total | 300 | 360 | 660 |

Use 720 pages as the combined review ceiling. Exceeding it requires an explicit
scope decision: remove repetition, move a complete activity to the companion,
reduce the number of printed applications, or revise the volume organization.
A page overrun is not permission to omit a necessary explanation. Each volume
needs its own contents, glossary, index and stated prerequisite knowledge.

### Volume I Programming the machine

Original project numbers in the final column identify existing materials, not
proposed chapter numbers. A companion lab remains a complete executable assignment.

| Chapter | Teaching purpose | Pages | Existing projects and treatment |
| --- | --- | ---: | --- |
| 1 Your first assembly program | Explain the execution model through input, arithmetic, output and one debugger trace | 18 | 01 console calculation as the worked program |
| 2 Registers and integer representation | Connect bit patterns, signedness, widths and arithmetic results | 20 | 04 bit inspector |
| 3 Addresses and memory | Distinguish an address from its stored bytes; explain loads, stores and byte order | 20 | 02 single pixel |
| 4 Decisions and repetition | Translate conditionals and loops; explain branch behavior for the selected target | 24 | 05 seven-segment counter as the worked program; 06 MarsBot as an elective lab |
| 5 Functions and the stack | Establish arguments, results, lifetimes, calls and register preservation before large programs | 24 | Extract short functions from the existing sprite and algorithm projects |
| 6 Arrays and images | Develop indexed and two-dimensional addressing, row layout and bounded copying | 24 | 03 eight-byte sprite; 16 clipped blitter as an end-of-volume companion assignment |
| 7 Integer algorithms | Explain invariants and dependent state updates through a visible algorithm | 24 | 07 Bresenham line drawing |
| 8 State and buffers | Explain snapshots, ownership of updates and state carried between operations | 24 | 08 Life as the worked program; 14 ring buffer as a companion lab |
| 9 Algorithms and their cost | Connect algorithm choices to observable work and data structures | 24 | 09 sorting as the worked comparison; 10 maze search as a companion lab |
| 10 Fixed-point arithmetic | Explain scaling, rounding, intermediate range and overflow | 24 | 12 bouncing ball |
| 11 Memory access and locality | Relate layout and traversal to the simulator's cache observations | 22 | 11 matrix transpose |
| 12 Bounded interfaces and independent implementation | Consolidate contracts, partial input, failure behavior and testing | 28 | 13 packet parser as the worked program; 15 UTF-8 scanner as a substantial assignment |

Bounds begin with the first indexed access; Chapter 12 consolidates those lessons.
Functions receive a small contract when first introduced in Chapter 5. Do not defer
these prerequisites simply because a later chapter treats them systematically.
Introduce full clipping and independent record-wrap problems only after the reader
has the required address and state reasoning.

### Volume II Vector programming and structured data

| Chapter | Teaching purpose | Pages | Existing projects and treatment |
| --- | --- | ---: | --- |
| 1 Measuring a program | Establish correctness baselines, completed instructions, cache observations and comparable workloads | 20 | 22 blocked matrix multiplication |
| 2 Working with MSA | Teach lanes, signedness, loads, shuffles, alignment requirements and scalar tails | 22 | 19 RGB conversion |
| 3 Saturating arithmetic | Explain PCM representation and distinguish saturation from wrapping | 20 | 20 PCM mixing |
| 4 Filtering signals | Develop accumulation width, fixed-point coefficients and scalar/vector correspondence | 22 | 21 FIR filter |
| 5 Compact integer and byte encodings | Explain representations, size tradeoffs and partial groups | 20 | 23 delta/varint as the worked program; 28 Base64 as a companion lab |
| 6 File boundaries and simple formats | Establish file I/O, bounded parsing, headers and complete output artifacts | 18 | 29 binary PPM |
| 7 Checksums and streaming state | Derive an update rule, then compare equivalent implementations | 20 | 24 CRC-32 |
| 8 Decoding with a state machine | Develop a bounded image decoder and its encoder counterpart | 24 | 30 QOI |
| 9 Nested data and interpreters | Explain recursive state, explicit stacks and separation of syntax from evaluation | 24 | 32 CBOR as the worked format; 17 expression evaluator and 18 bytecode interpreter as elective labs |
| 10 Word operations and cryptographic algorithms | Explain a quarter-round, lane organization and the distinction between an arithmetic primitive and a complete scheme | 22 | 26 ChaCha20 as the worked kernel; 25 SHA-256 and 27 RSA arithmetic as elective labs |
| 11 An audio processing pipeline | Integrate file parsing, sample processing and playable output | 18 | 31 PCM WAV |
| 12 A PNG decoding pipeline | Connect chunks, checksums, decompression, reconstructed rows and pixel output | 28 | 33 PNG capstone |
| 13 A lossless video format | Develop frame dependencies, change encoding, indexing and seeking | 24 | 34 lossless video capstone |
| 14 Predicting video pictures | Explain reference pictures, motion search, residuals and local reconstruction | 22 | 35 H.263, first half |
| 15 Producing an H.263 stream | Connect transforms, quantization, syntax, bit output and independent decoding | 22 | 35 H.263, second half |

The capstone chapters explain architecture and selected difficult components. They
must provide enough information to understand and complete the assignment, while
long syntax tables, full listings and exhaustive construction checkpoints live in
the companion. H.263 retains actual interoperability, the restricted documented
profile, Python-generated QCIF input, at most 30 frames per second and at most ten
seconds. Keep the small default clip for ordinary development.

Cryptography and interpreter electives must name their prerequisites. They are not
silent requirements for the codec sequence. This arrangement accounts for every
original project from 01 through 35; H.263 intentionally spans two printed chapters.

## Standards for a usable chapter

Start with an understandable problem and an observable result. Explain the machine
model needed for that problem before presenting its implementation details. A
worked example should connect values, source instructions and resulting state,
including the reason for significant choices. End with a task that requires the
reader to apply that reasoning with less assistance.

Each chapter proposal must identify three to five learning outcomes and its
prerequisites. Give each outcome an exercise or observation that can demonstrate
it. A list of commands the student has followed does not demonstrate understanding.

Use four to eight principal numbered sections as a planning range. Review any
chapter that needs more than ten. Number sections for concepts, algorithms or
design stages. Routine edits and individual questions usually need no numbered
heading. These limits trigger editorial review rather than mechanical splitting.

Use instruction-level explanation when an instruction's behavior is new or has a
surprising consequence. Group familiar instructions into readable units afterward.
Reintroduce a concept briefly when a later use changes its meaning or assumptions;
link to the earlier explanation for the full treatment.

### Worked examples and recovery

Select complete examples that students can run without privately supplied code.
When a listing is partial, identify its context and provide the corresponding
complete public source. Introduce new notation and symbols before relying on them.

For each central idea, include a worked trace or comparable concrete explanation,
a likely misconception, and a way to investigate it. Useful debugger questions
include which instruction last changed a value, which bytes a load read, and which
assumption caused a wrong branch. Explain how to locate the fault; a picture of the
expected final result alone does not teach diagnosis.

### Exercises and decreasing guidance

| Stage | Worked material | Independent work |
| --- | --- | --- |
| First programs | Short complete listings, explicit state traces and explanations of unfamiliar instructions | Predict a value, change an expression, repair a simple fault |
| Core programming | Explain a representative function or loop and its invariant | Implement a related function or extend the algorithm |
| Structured data and MSA | Explain the data model and difficult kernel; establish an equivalent scalar baseline | Choose implementation details and test boundary cases |
| Capstones | Explain architecture, contracts and selected difficult components | Integrate modules, diagnose interactions and justify design choices |

Use short questions at meaningful conceptual boundaries. Preserve multiple-choice
questions in the tutor where useful, but include prediction, explanation, debugging
and implementation exercises in print. A successful guided checkpoint must be
followed, at suitable intervals, by a related task without its construction hints.

### Figures and listings

Prefer a native diagram or trace when it explains the idea more directly than an
IDE screenshot. Keep an IDE figure when the reader needs to recognize an actual
view, action or machine observation. Combine adjacent observations when one figure
can explain their relationship clearly. Reuse verified originals only when their
source, state and caption still match the revised lesson.

Long listings require a specific reading purpose. Print enough code to reason about
the example; put complete routine collections and repeated variants in the companion.
Every omitted detail needed to run an exercise must remain available in public files.

## Content audit and disposition

Before rewriting, record a disposition for each substantial passage, listing,
figure and exercise: keep, rewrite, merge, move to companion, move to instructor
material, or retire. Record its original stable identifier, proposed location,
learning purpose and reason for the decision. Keep this as an editorial inventory;
it must not become a document generator.

Keep a concept introduction where its first substantial use requires it. Merge
repeated explanations that make the same point. Move exact editor procedures,
routine construction checkpoints, implementation inventories and exhaustive grading
coverage to their appropriate companion products. Preserve necessary error reasoning,
width semantics and architecture conditions in the teaching text.

Do not preserve a passage merely because it was expensive to produce. Do not remove
one merely because it is long. Allocate space according to explanatory value and
prerequisite need. Retain historical verification receipts as evidence of the
versions actually tested.

## Implementation phases and review gates

| Phase | Deliverable | Condition for moving on |
| --- | --- | --- |
| 0 Preserve the baseline | Record the current public/private commits, PDF digest, package versions and archive bindings; create a separate revision branch when implementation begins | Original draft and tested companions can be recovered without ambiguity |
| 1 Audit and dependency map | Disposition inventory, concept prerequisites, final project-to-chapter map and revised contents | Author reviews the scope, elective choices and page allocations before chapter rewriting |
| 2 Three editorial pilots | Rewrite the first program, PCM mixing and the H.263 reconstruction-loop explanation; use the latter as a substantial excerpt | Author judges the prose and organization suitable; educational checks below pass before a bulk rewrite |
| 3 Volume I | Revised foundational chapters, independent exercises and updated companion links | Prerequisites are introduced before use; chapter and volume reviews pass |
| 4 Volume II | Revised vector, format and capstone chapters with explicit elective paths | Required reading forms a complete path; capstones can be attempted without elective-only knowledge |
| 5 Integration and publication review | Both complete PDFs, cross-volume references, indexes, accessible figures and matching distributions | Technical, editorial, visual and accessibility reviews pass; unresolved issues are documented with an owner |

Review each chapter before starting a large batch. Use the pilots to establish the
editorial method before extrapolating effort or completion dates. Do not open a
sustained rewrite goal until the author accepts the pilot approach.

### Pilot review tasks

For the first program, a programming-competent assembly beginner should be able to
explain the input-to-output data flow, identify what two arithmetic instructions
read and write, change the expression, and investigate an incorrect result.

For PCM mixing, a reader with the stated prerequisites should explain a sample's
representation, predict saturation and wrapping for a concrete pair, describe the
lane operation, and implement or repair an unfamiliar scalar tail.

For the H.263 excerpt, a reader who knows MSA and the preceding material should
explain the difference between an original and reconstructed reference, trace one
prediction/residual/reconstruction example, and explain why the encoder and decoder
must agree on the reconstructed picture.

Start review with the author, then seek walkthroughs by representative learners
before treating the method as validated. Use several learners, ideally three to
five per pilot audience when available. This is a formative review, not a statistical
study. Record where they need an explanation, which misconception caused difficulty
and whether they complete the independent task. AI review cannot substitute for
those observations. If learners are unavailable, retain that review as open and
avoid claiming that the chapter has been validated for its intended audience.

## Acceptance and technical verification

A chapter is editorially ready for author review when its prerequisites are
explicit, unfamiliar terms are explained, the worked reasoning supports the
independent exercise, and its sectioning and length fit the agreed role. Reader
confusion calls for a diagnosis and revision, rather than an automatic extra hint.
The author decides whether the explanation is acceptable; page counts and checks
support that judgment but do not make it.

For revised executable material, assemble and run the public and private checks
against both the reference and a valid alternative. Keep exact teaching-state checks
separate from free-writing acceptance. Preserve stable project, step and quiz IDs
where their meaning is unchanged; version changed contracts and record migration
links when an identifier's meaning changes. Do not silently rewrite old receipts.

Validate native references, literal code extraction, IntelliJ-derived minted
highlighting, fonts and output layout. Recapture a screenshot if its demonstrated
code or state changes. Keep the capture implementation in the separate plugin that
depends on MARS. Before any new IDE capture launch, run the actual upstream Gradle
tasks `clean cleanSandbox runIde`.

Build through the existing serialized LaTeX path. Inspect changed pages and adjacent
page breaks, rerun automated PDF/UA-2 validation on final PDFs, and perform human
reading-order and assistive-technology reviews. The current draft's validation
result does not transfer automatically to a revised PDF.

## Work outside this revision

Production remote course delivery, enforced instructor progression and hint policy,
new simulator performance work and unrelated conformance work remain separate tasks.
The restructuring may reveal a genuine missing grading capability; implement and
test such a capability separately when the instructional contract requires it.
Do not expand the book revision into an unrelated plugin redesign.

## Decisions for author review

Review the proposed two-volume boundary, the 660-page working budget, which projects
receive full printed treatment, and the three pilot selections. These are the first
editorial decisions needed when implementation is requested. Page allocations may
change after the pilots; the limits on repeated construction detail and the need
for independent student work remain.
