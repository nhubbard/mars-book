# MARS textbook restructuring proposal

Date: 20 November 2026  
Status: Implementation goal authorized on 20 November 2026. Complete the main book, written lab supplement and student instruction in MARS; review the first complete unit before scaling.

## Purpose and decision

Restructure the 1,819-page initial manuscript around a main learning book and a
companion collection of laboratories. Organize the explanation around machine
concepts and increasing student independence. Complete selected smaller projects
within the main book. Retain all 35 projects in the curriculum, with larger and
alternative applications available as companion laboratories.

Set no page target or ceiling. Decide whether the learning book needs more than
one physical volume after the revised material has a coherent reading sequence.
The laboratories have two complementary forms: interactive lessons integrated into
MARS and a separate native LaTeX lab book explaining their concepts, observations
and reasoning. These accompany the main learning book.

The first draft is source material for this revision. Existing prose, code,
checks and figures must earn their place through their contribution to learning.
Technical verification establishes observed program behavior; it does not establish
that an explanation teaches the reader to produce that behavior independently.

The author has authorized implementation as a new goal. Start with one complete
unit across the main book, written lab supplement and actual interactive lesson.
The initial draft completion record remains historical evidence; preserve its
sources and tested distributions on the recorded baseline commits.

## Intended reader and learning outcomes

Assume that the reader understands variables, expressions, conditionals, loops and
functions in another programming language. Assume no assembly experience. Briefly
recover a prerequisite when an example needs it; avoid repeated lessons in ordinary
computer use.

By the end of the foundational part, the reader should be able to translate a small algorithm
into assembly, explain its register and memory state, write and call a function,
reason about widths and bounds, use the debugger to investigate a fault, and test
an implementation against a stated contract.

By the end of the advanced part, the reader should be able to choose and measure a scalar
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

Separate the learning narrative, practical activities and instructor materials:

| Product | Purpose | Material it owns |
| --- | --- | --- |
| Main learning book | Explain concepts and develop independent reasoning, completing smaller projects in context | Connected exposition, selected worked programs, traces, conceptual figures, diagnostic examples and exercises |
| Written lab book and public companion files | Explain what interactive labs teach and provide extended and alternative project work | Starters, supported contracts, optional extensions, longer construction guides, public checks and fixture generators |
| Interactive labs in the main MARS plugin | Teach concepts through action and feedback, with guidance that decreases as students progress | Checkpoints, contextual hints, editor focus, per-step quizzes and recoverable progress |
| Private instructor distribution | Support assessment and teaching | References, alternatives, answer keys, private checks, rubrics and teaching notes |

All 35 projects remain available. Inclusion in the companion collection does not
require printing its complete implementation instructions. A project can supply a
worked example, a substantial assignment, an elective lab or several illustrations.

### Choosing the right home for an activity

Keep a complete activity in the main book when its construction is short enough
to support the explanation and its result makes the new concept concrete. The
first console program, a pixel update and a small loop are candidates. The reader
should regularly finish something useful within the learning narrative.

Use a companion lab when the activity primarily provides additional practice,
requires substantial integration, offers an alternative application, or involves
many implementation choices after the central concept has been explained. The
lab must still teach its project-specific ideas and provide enough context for
independent use; moving material is not an excuse to leave it unexplained.

Use an interactive lesson when action and immediate observation improve the
explanation: predicting a register value, stepping over a store, comparing signed
and unsigned loads, seeing vector lanes, or finding the first incorrect state.
Explain the prediction before the action and interpret the result afterward.
Feedback should diagnose the relevant misconception and lead to another attempt.
Avoid reproducing the old construction checklist as a sequence of dialogs.

The main book must supply the explanations needed to understand its subjects
without requiring completion of an interactive lesson. The lesson can deepen or
apply that explanation. The same concept may have a short book example and a
richer lab, with clear links between them.

### Pilot the laboratory delivery

Pair the first-program editorial pilot with a small interactive lesson storyboard.
Record its explanations, actual IDE actions, expected observations, useful failure
feedback, hint behavior and independent closing task. Review whether that sequence
teaches more effectively through interaction before expanding the implementation.
Existing verified capture tooling does not establish that a student-facing tutor
is already implemented.

Retain guided and free-writing modes, equivalent-solution checks, and the planned
instructor controls over progression and hints. Implement the student instructional system in the main MARS plugin. Keep screenshot
and publication-authoring automation in the separate plugin depending on MARS.
The written lab book is required: it prepares students for each activity, develops
the reasoning that benefits from sustained prose, explains the observations and
sets independent follow-up work. Plan production delivery, versioning and instructor
policy as part of the instructional system, with explicit behavior and tests.

Keep the publication as directly edited native LaTeX with ordinary includes.
Exercise metadata may link a passage to a task but must not determine the order
or granularity of the printed explanation. Human passages can be included at any
appropriate point in that sequence.

## Proposed teaching sequence and publication options

Use the existing readable typography, with native figures, listings and exercises.
Do not shrink the typography or omit needed explanations to reach a numerical
length. Review a chapter's length by asking what it teaches, whether it repeats
prior work and how much new reasoning it asks of the reader.

The two parts below describe prerequisite order, not a committed physical split.
They may fit in one main learning book or become separate volumes. The separate lab
book organizes practical assignments by their prerequisites and links to the main
text. The written lab book complements the interactive collection and remains useful
without replaying its IDE interactions. Each actual published volume should have its own contents,
relevant glossary and index, and stated prerequisites.

### Part I Programming the machine

Original project numbers in the final column identify existing materials, not
proposed chapter numbers. A companion lab remains a complete executable assignment.

| Chapter | Teaching purpose | Existing projects and treatment |
| --- | --- | --- |
| 1 Your first assembly program | Explain the execution model through input, arithmetic, output and one debugger trace | 01 console calculation as the worked program |
| 2 Registers and integer representation | Connect bit patterns, signedness, widths and arithmetic results | 04 bit inspector |
| 3 Addresses and memory | Distinguish an address from its stored bytes; explain loads, stores and byte order | 02 single pixel |
| 4 Decisions and repetition | Translate conditionals and loops; explain branch behavior for the selected target | 05 seven-segment counter as the worked program; 06 MarsBot as an elective lab |
| 5 Functions and the stack | Establish arguments, results, lifetimes, calls and register preservation before large programs | Extract short functions from the existing sprite and algorithm projects |
| 6 Arrays and images | Develop indexed and two-dimensional addressing, row layout and bounded copying | 03 eight-byte sprite; 16 clipped blitter as an end-of-volume companion assignment |
| 7 Integer algorithms | Explain invariants and dependent state updates through a visible algorithm | 07 Bresenham line drawing |
| 8 State and buffers | Explain snapshots, ownership of updates and state carried between operations | 08 Life as the worked program; 14 ring buffer as a companion lab |
| 9 Algorithms and their cost | Connect algorithm choices to observable work and data structures | 09 sorting as the worked comparison; 10 maze search as a companion lab |
| 10 Fixed-point arithmetic | Explain scaling, rounding, intermediate range and overflow | 12 bouncing ball |
| 11 Memory access and locality | Relate layout and traversal to the simulator's cache observations | 11 matrix transpose |
| 12 Bounded interfaces and independent implementation | Consolidate contracts, partial input, failure behavior and testing | 13 packet parser as the worked program; 15 UTF-8 scanner as a substantial assignment |

Bounds begin with the first indexed access; Chapter 12 consolidates those lessons.
Functions receive a small contract when first introduced in Chapter 5. Do not defer
these prerequisites simply because a later chapter treats them systematically.
Introduce full clipping and independent record-wrap problems only after the reader
has the required address and state reasoning.

### Part II Vector programming and structured data

| Chapter | Teaching purpose | Existing projects and treatment |
| --- | --- | --- |
| 1 Measuring a program | Establish correctness baselines, completed instructions, cache observations and comparable workloads | 22 blocked matrix multiplication |
| 2 Working with MSA | Teach lanes, signedness, loads, shuffles, alignment requirements and scalar tails | 19 RGB conversion |
| 3 Saturating arithmetic | Explain PCM representation and distinguish saturation from wrapping | 20 PCM mixing |
| 4 Filtering signals | Develop accumulation width, fixed-point coefficients and scalar/vector correspondence | 21 FIR filter |
| 5 Compact integer and byte encodings | Explain representations, size tradeoffs and partial groups | 23 delta/varint as the worked program; 28 Base64 as a companion lab |
| 6 File boundaries and simple formats | Establish file I/O, bounded parsing, headers and complete output artifacts | 29 binary PPM |
| 7 Checksums and streaming state | Derive an update rule, then compare equivalent implementations | 24 CRC-32 |
| 8 Decoding with a state machine | Develop a bounded image decoder and its encoder counterpart | 30 QOI |
| 9 Nested data and interpreters | Explain recursive state, explicit stacks and separation of syntax from evaluation | 32 CBOR as the worked format; 17 expression evaluator and 18 bytecode interpreter as elective labs |
| 10 Word operations and cryptographic algorithms | Explain a quarter-round, lane organization and the distinction between an arithmetic primitive and a complete scheme | 26 ChaCha20 as the worked kernel; 25 SHA-256 and 27 RSA arithmetic as elective labs |
| 11 An audio processing pipeline | Integrate file parsing, sample processing and playable output | 31 PCM WAV |
| 12 A PNG decoding pipeline | Connect chunks, checksums, decompression, reconstructed rows and pixel output | 33 PNG capstone |
| 13 A lossless video format | Develop frame dependencies, change encoding, indexing and seeking | 34 lossless video capstone |
| 14 Predicting video pictures | Explain reference pictures, motion search, residuals and local reconstruction | 35 H.263, first half |
| 15 Producing an H.263 stream | Connect transforms, quantization, syntax, bit output and independent decoding | 35 H.263, second half |

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
| 1 Audit and dependency map | Disposition inventory, concept prerequisites, final project-to-chapter map and revised contents | Author reviews the scope, elective choices and laboratory roles before chapter rewriting |
| 2 Three editorial pilots | Rewrite the first program, PCM mixing and the H.263 reconstruction-loop explanation; use the latter as a substantial excerpt | Author judges the prose and organization suitable; educational checks below pass before a bulk rewrite |
| 3 Foundational part | Revised foundational chapters, complete smaller activities, independent exercises and companion links | Prerequisites are introduced before use; chapter reviews pass |
| 4 Advanced part | Revised vector, format and capstone chapters with explicit elective paths | Required reading forms a complete path; capstones can be attempted without elective-only knowledge |
| 5 Integration and publication review | Main book, any chosen volume or lab-book split, cross-references, indexes, accessible figures and matching distributions | Technical, editorial, visual and accessibility reviews pass; unresolved issues are documented with an owner |

Review each chapter before starting a large batch. Use the pilots to establish the
editorial method before extrapolating effort or completion dates. The sustained implementation goal is authorized; retain author review of the
completed pilot unit before a bulk rewrite.

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
independent exercise, and its sectioning and length fit the agreed teaching purpose. Reader
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

New simulator performance work and unrelated conformance work remain separate tasks.
Remote delivery and instructor progression and hint policies are now part of the
student instructional-system goal; rosters, LMS integration and hosted grading
services require separate product decisions when their need is established.
The restructuring may reveal a genuine missing grading capability; implement and
test such a capability separately when the instructional contract requires it.
Do not expand the book revision into an unrelated plugin redesign.

## Decisions for author review

The concept-led approach and separation of explanation from extended construction
are accepted in principle. Review which smaller projects should be completed in
the main book, the roles of the remaining labs, and the three pilot selections.
Use the first interactive lesson storyboard to evaluate delivery. Defer physical
volume count until the revised sequence supports that decision. The main learning
book, written lab supplement and integrated interactive labs are the agreed products. There is no page target or ceiling;
clarity, coherent progression and independent student work govern the revision.

## First-unit figure milestone — 21 November 2026

The revised written lab now includes three native teaching figures with fourteen numbered explanations, using four unchanged actual light-theme IDE captures. They compare consecutive arithmetic pauses, correct output preparation and a wrong argument with a correct retained result. Captions, descriptions and callouts are editable LaTeX; geometric receipts retain provenance. The six-page proof passes automated PDF/UA-2 validation, and color/grayscale inspection is recorded privately. These checks establish publication integrity, not learner usability. Author, learner and assistive-technology review remain open; the complete 35-project goal is unchanged.


## First-unit progression milestone — 21 November 2026

The main plugin now supports instructor-selected guided activities before independent assessment. Each selected requirement uses current-edition questions and source-bound Run/Debug evidence. Edits after leaving a lesson, edits restored to the original text, offline changes and mid-assessment changes cannot reuse stale execution credit. Native Settings, all five actual observations and a fresh-process offline-edit check have been exercised. The written lab explains the separate independent file and these prerequisites. This remains a first-unit implementation; arbitrary course sequencing, retry policies, content delivery and the remaining curriculum are open. Author, learner and assistive-technology review remain pending.


## Author review and independent walkthrough — 21 November 2026

The author reviewed the revised first-unit and advanced pilot PDFs and accepted the explanatory direction for continued work. This is permission to expand the approach, not evidence of a completed learner trial. The primary end-to-end acceptance exercise is the author's manual walkthrough of the book using the book, its companion materials and MARS without outside explanations. Record stopping points and the information needed before supplying help, so an explanation in conversation cannot hide a defect in the publication. Distinguish productive difficulty from an unexplained concept or an unclear tool action. Representative beginner and assistive-technology reviews remain open.

A short opening orientation to MARS and its educational environment is a deferred editorial idea. It should distinguish assembly, architectural execution, supplied services and inspection tools; compare MARS with OS-hosted and bare-metal execution; and distinguish initial stack setup from a program's responsibility for function frames. Verify the actual implementation before describing defaults. No opening passage has been added.

## Written foundational sequence — 21 November 2026

The directly edited revision now covers original projects 001–016 across twelve
main chapters and five written companion labs: MarsBot, clipped sprites, ring
records, maze search and streaming UTF-8. Each companion includes explanatory
reasoning, an independent task, executable checks and verified native figures.
The combined review proofs are 78 main-book pages and 76 lab-book pages. This is
a composition milestone, not completion of phase 3 review or the overall goal.
Native student lessons beyond the first unit, the advanced sequence, production
content delivery/policies, final publication integration and human reviews remain
open. The planned MARS-versus-hardware opening remains deferred.


## Downloaded byte-inspector Debug activity — 22 November 2026

The revised byte-inspector package 0.3.0 now combines four inline predictions,
five source-bound Run observations and a three-stop Debug activity. Students
observe the input before the mask, the completed mask, and the completed sign
extension in full-width registers. The main plugin checks ordinary native Debug
pauses against assembler source metadata and actual consumed input. It accepts
only a coherent forward session; edits, resets, replaced sessions, register or
memory modifications and backsteps cannot substitute for the requested evidence.
Partial Debug sequences are repeated in one session. Completed observations
survive reopening while their source remains unchanged.

The separate authoring plugin exercised HTTPS import, inline controls, saved
source edits, failed Run feedback, source rechecking, a rejected Debug boundary,
the correct three stops and completion in a third fresh IDE process. All IDE
launches used the actual clean, cleanSandbox and runIde tasks. These screenshots
are development evidence, not newly annotated publication figures. The written
lab explains the checked sequence and keeps output tracing, deliberate faults,
the word-width experiment and independent variations explicit. Native LaTeX,
Minion Pro, Noto Sans Math and the IntelliJ-derived highlighting remain in use.
Automated PDF/UA-2 verification and the changed pages were checked again.

This is a second-unit runtime milestone. Downloaded independent-writing
assessment, the remaining interactive units, broader I/O contracts, finer
instructor progression policies and human learner/accessibility review remain
open. The general MARS-versus-hardware opening remains deferred.
