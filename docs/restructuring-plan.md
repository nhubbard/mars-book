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


## Independent byte-inspector contracts — 22 November 2026

Student edition 0.3.1 supplies two incomplete program bodies for the Chapter 2
variations, exact public input/output contracts, generated existing-DSL checks,
and instructions for using the CLI grader. The ordinary sum and eight-bit
narrowed sum remain separate files. The main book and written lab explain the
interface, result lifetime, discriminating input 255, input echo and checking a
saved source file. No book prose is generated from the case data.

The existing MARS grader was sufficient: reference and alternative algorithms
pass every admitted input from 0 through 255. Public cases reject the incomplete
starters and expose missing narrowing, unwanted narrowing, lost sign extension
and a wrong output argument. Private instructor archives pin the exact student
archive digest and keep solutions and exhaustive checks separate. Bounded data
validation and actual Kotlin literal compilation support later data-only native
assessment without executing downloaded scripts at import.

The new edition was imported over HTTPS and resumed through its existing native
prediction, Run and Debug sequence in two fresh IDE processes. Added files were
compared byte for byte, and imported public checks were executed on the incomplete
starters. This verifies package delivery and CLI checking. Downloaded independent
assessment, its inline feedback, saved assessment counts and mode/progression
controls are still to be connected to Learn. Human learner and accessibility
review remain open; the general MARS-versus-hardware opening remains deferred.


### Native independent checks: byte-inspector edition 0.4.0

The first downloaded independent-writing activities follow the worked predictions,
Run observations and ordered Debug sequence in the byte-inspector unit. Learn opens
the incomplete ordinary-sum and narrowed-sum programs separately. An inline check
assembles the current editor text and evaluates the archived public cases in fresh
simulator sessions. Students retain their choice of labels, registers and correct
instruction sequences. The optional CLI scripts express the same case data.

This extends the working end-to-end unit through independent implementation. It
supports revised-source checks, stale-result rejection, resumable source-bound
passes, per-activity attempt counts and instructor control of access, preceding
predictions and graduated hints. It does not establish a secure examination system
or replace an explanation of the algorithm for the full admitted input range.
That downloaded console contract is single-source. The memory unit below adds
exact final-byte contracts; later file, vector and media objectives still need
appropriately specific public state contracts.

The lab's checking guidance is authored directly in native LaTeX. Screenshot and
walkthrough automation remains in the separate authoring plugin. Completed fixture
programs and exhaustive instructor checks remain private. Development captures and
machine-verified cases support review; representative learners must still establish
whether they can complete the sequence independently. The proposed opening account
of MARS versus hardware remains deferred for author review.


### Direct downloaded activity navigation

Students can choose guided work or an independent source directly through the
native Tools activity chooser. Continuing returns to the project's last selected
activity in the exact retained edition. Independent lessons use the same source,
cases, check identity, hints and assessment budget as the guided sequence.
Changing modes does not reset history, supply prerequisite answers or grant a
pass. Returning to guided work still checks its current source-bound evidence.

Native platform tests cover selection, cancellation, edition binding, serialized
state, live restrictions and both task routes. The separate authoring plugin
exercised a completed unit in two fresh light-theme IDE processes: an independent
source edit required rechecking, cancelled navigation preserved the destination,
and independent continuation returned to the selected source. Guided completion
then reused unchanged observations. These are development checks, with human
learner and accessibility review still open. Broader per-course prerequisite
selection and non-console contracts remain part of the active curriculum goal.


### Instructor-selected prerequisites for downloaded work

The instructor can configure each independent exercise using earlier predictions,
Run observations, Debug sequences and independent checks from the exact selected
edition. Dependencies of a required independent check also apply, so requiring
that check preserves its own prerequisites. The existing global access, preceding
prediction, hint and attempt controls continue to apply. The Settings page now
identifies the built-in first program's named requirements explicitly.

Saved rules live in shareable project policy; progress remains personal. Draft
choices survive switching exercises, Cancel leaves policy intact, and Save
applies the edition's rules atomically. Source changes, including offline edits,
cannot retain execution credit. A blocked or outdated check uses no attempt.
The student's written lab explains returning to listed guided activities.

Native tests cover ordering, transitive and shared dependencies, policy reload,
unknown-rule repair, missing source, grading races and long scrollable lists.
Actual light-theme IDE controls verify rule selection, visible checkbox access,
source-dependent blocking, fresh-process policy, selective release and current
source requirements. Remaining work includes the later units' non-console state
contracts, their native lesson content and human learner/accessibility review.
The proposed opening orientation remains deferred.


### Third native unit: addresses and memory

The revised single-pixel package, edition 0.2.0, accompanies the main book's
addresses-and-memory chapter and its directly authored LaTeX lab. Four inline
predictions distinguish addresses, values, byte order, extension and old register
copies. Four ordered pauses observe the worked store/load sequence in one fresh
Debug session. Ordinary Run is also exercised, but an empty console is not treated
as a memory-correctness check.

Independent red-component and memory-copy programs each declare six public RGB
inputs, normal termination, exact prompt output and final bytes including guard
words. Case-profile version 2 extends the same data-only native assessment path;
no downloaded Kotlin executes. Private byte-store and word-computation alternatives
pass, and starters and four store/copy faults are rejected by the CLI. The written
lab distinguishes final guards from write history and retains manual load/readback
reasoning that the final-state contract does not prove.

The actual light-theme walkthrough completed in two fresh IDE processes. Correct
predictions survived source edits; changed worked source required fresh Debug
observations, changed independent source required another check, and an unchanged
independent program retained its pass and count. The first trial exposed a lesson
pointer assuming a selected text editor while a non-text tab was active. The fix
uses native nullable selection and lifecycle-owned selection events without moving
focus from the student's view. A separate authoring-only write-context error was
corrected and both complete walkthroughs were repeated without either error.

The established separate publication driver captured the unchanged worked source
at both sides of the byte store, recording native address/byte cell coordinates.
The LaTeX lab composes readable details over those originals with editable native
callouts and alternative text. Main MARS contains no screenshot controller or
private solution fixture. The later units' instructional integration, representative
learner review and human accessibility review remain open. The proposed general
MARS-versus-hardware opening remains deferred.


### Fourth-unit preparation: input fixtures and actual histories

Case-profile version 3 connects the existing grader's store histories and permitted
read intervals to data-only native assessments. Per-case ordinary user-data setup
happens before execution; trace observers remain separate for every fresh case.
Version 1/2 behavior and existing generated scripts are retained. The native check
path rejects a missing repeated transaction even when final state agrees.

The preparatory seven-segment exercise now has ten public cases with complete
input/guard data, exact ordered byte stores and read boundaries. The count-based
and end-pointer implementations pass all public cases and all 153 admitted ranges
for each of three glyph tables: 938 positive executions. Eight assembled faults
and the incomplete starter are distinguished. Empty, one-entry and repeated-mask
cases appropriately pass some faulty programs; no one case is represented as a
proof of the whole algorithm.

This is grader and exercise groundwork, not completion of the fourth native unit.
The guided full/empty/tail observations need versioned per-source Run/Debug binding
and corresponding prerequisite-source tracking. Do not weaken source invalidation
to accommodate edits between those variants. The lesson archive, actual IDE flow,
new chapter-four captures and native LaTeX integration remain pending. Historical
publications and the completed memory unit are unchanged by this step.


### Fourth-unit source-specific lesson draft

Native lesson format 5 adds explicit Run/Debug source selection while retaining
per-source invalidation and instructor prerequisite tracking. The new lesson
archive has the published full-range program and separate empty and tail variants,
four explanatory predictions, ten ordered register observations, a no-console-output
Run and the end-pointer independent task. Actual CLI execution matches all ten
register observations across the three variants. The 450 book-tool tests pass,
including publication/source consistency and exact public export boundaries.

This step supplies the instructional draft and package, not a completed native
walkthrough. The new IDE interaction, light-theme chapter-four captures, native
LaTeX integration and human learner review remain outstanding. The current PDF
editions and historical validation evidence are unchanged.


### Fourth native unit: verified source switching and continuation

The loop unit's actual light-theme walkthrough completed with four inline
predictions, all ten full/empty/tail Debug observations, an ordinary Run, rejection
of a wrong-source Run, and independent transaction/read checks. The exit-only
starter passed three empty cases out of ten. The unsaved end-pointer fixture
passed all ten. After comment edits to the worked and independent sources and a
normal project close, a fresh IDE process retained five quiz attempts and the
unchanged empty/tail passes. It required new full-range Debug and Run and a third
independent assessment. Eleven installed JARs matched the pinned archives in both
processes. Both final logs contain zero ERROR/SEVERE entries.

The first authoring trial waited for an absent introductory sentence before
checking; it is retained as incomplete evidence. The corrected driver was tested
and both complete processes started independently of that trial. All controller
and private-fixture code remains in the separate authoring plugin. The packaged
API verdicts remain Compatible with 91/20 public experimental usages.

Three real publication captures verify the selected executions and photographed
segment states before/after the first store and at the empty exit. Native LaTeX
callouts and semantic descriptions compose those unchanged images into the
written lab. The lab now explains source/configuration selection, retained device
views, the exact admitted inputs and observation scope, and why an empty-case
pass cannot establish a complete loop. The elective MarsBot companion remains.
Later units, representative learner review and human accessibility review are
still open; the general MARS-versus-hardware opening remains deferred.


The integrated lab PDF is 190 pages. The revised loop supplement occupies pages
17–21, including the native display figure on page 19; its preserved MarsBot
companion begins on page 22. Changed pages and grayscale callouts were rendered
and reviewed. The exact final PDF passed the pinned PDF/UA-2 profile: 1,728 rules,
892,967 checks, zero failures. Minion Pro, Noto Sans Math and JetBrains Mono remain
embedded. Automated tagged-PDF validation does not substitute for human reading
order, assistive-technology or learner review. The main learning PDF is unchanged.


### Fifth-unit grading foundation: callable contracts

The functions-and-stack unit already promises all 256 low-byte inputs, helper
execution, arbitrary upper argument bits, full-width saved registers, stack
restoration, caller-owned bytes and correct incoming continuations. A whole-program
console or final-memory check would not preserve that contract. MARS now provides
PreparedFunctionChecks, a bounded data-only counterpart of the existing assignment
DSL's function-call and observation operations. Each case owns a fresh machine;
repeated calls retain its memory and cumulative budget. Cancellation propagates
between bounded calls and owned sessions close on success, failure and cancellation.

The full CLI suite passes 151 tests. A saved-link wrapper and an equivalent tail
transfer pass all 256 byte inputs with alternating continuations. Deliberate result,
upper-half preservation, pointer, caller-byte, output, helper and return failures
are rejected. Image isolation uses byte-order-sensitive reads on both targets.
The existing public chapter text and script remain the educational contract.

This is the grading foundation, not completion of the fifth native lesson.
Its versioned function-case reader, native assessment wiring, explanatory
interactive activities, actual IDE walkthrough and publication captures remain
next. No new native content is represented as importable yet; no PDF or figure
changed in this step. All 41 unrelated MARS files retain their prior content hashes.


### Fifth-unit native integration and instructional draft

Native lesson format 6 now selects explicit function assessments. Strict bounded
function data is parsed into immutable definitions before import preview; the
existing source-bound checkpoint dispatches to the prepared function checker.
Console assessments and formats 1–5 retain their behavior. Unsaved correct code
and an equivalent tail transfer pass through the native checkpoint; correct
arithmetic with damaged full-width saved state fails. The full plugin suite passed
1,177 tests and all required meaningful coverage/notice checks. Two additional
regressions import the actual public profile/archive. The packaged API verifier
reports Compatible with 91 public experimental usages.

The development 003-row-functions archive now has five explanatory predictions,
eight measured call/frame/return Debug boundaries, a silent Run and the independent
function exercise. It extracts operations from original project 003 without
replacing that project's later sprite work. A repeatable tool authors the public
three-case grid and matching standalone DSL script: 256 calls per case, varied
upper bits/saved values/stack locations, alternating caller continuations. Both
private implementations pass all 1,536 calls across their two qualifications;
the incomplete starter and six assembled result/preservation/helper/return faults
are rejected. The worked program's eight boundaries and final words/guard also
pass actual CLI execution. The 453 book-tool tests verify source consistency,
expected data, generated-file freshness and exact student exports.

This supplies an importable development lesson, not a completed native on-screen
walkthrough. The fifth unit still needs its authoring-only driver, actual IDE
Run/Debug/check/resume experience, new light-theme frame figures and direct LaTeX
lab integration. No PDF changed in this step and no human learner review is
claimed. Complete independent fixtures and private checks stay outside the public
book repository. All 41 unrelated MARS file hashes remain unchanged.


### Fifth native unit: actual IDE and publication verification

The separate authoring plugin now drives the functions unit through actual inline
choices, eight ordered debugger boundaries, ordinary Run, starter rejection and an
unsaved implementation check. All 113 authoring tests pass. A second fresh IDE
process retained all six quiz attempts (one deliberate wrong answer and five
correct answers), skipped those completed predictions, and required new Debug,
Run and function-check evidence after source edits. The independent attempt count
advanced from two to three. Both student walkthrough projects closed and disposed
normally; all eleven installed plugin JARs matched the pinned archives in both
processes. The verifier reports both plugins Compatible, with 91 MARS and 20
authoring public experimental usages and no internal-API exception.

Three verified light-theme publication captures show the saved frame, restored
results/state and the caller's later stores. The initial capture recipe assumed
eight-byte row addresses; it was corrected to the native table's sixteen-byte
rows, and its observations are retained separately. Raw PNGs are unchanged;
publication crops and numbered annotations live in native LaTeX. The lab now
explains the actual interactive sequence, two ordinary caller continuations,
768 direct checked returns, source-bound progress and the limits of after-return
memory comparisons. The broader curriculum and representative learner review
remain unfinished. Detailed receipts remain in the private verification archive.

The final lab PDF is 192 pages, with the fifth unit on pages 27–31 and its two
annotated figures on pages 28–29. Those pages were rendered and visually checked;
the figure pages were also checked in grayscale. The final PDF SHA-256 is
`00709a9c2fc3c5a5142312620812d630f7f9328d326fdb8956b15088dac26d20`. Automated PDF/UA-2 validation passes
1,728 rules and 899,942 checks with zero failures. All embedded fonts
retain Unicode mappings, including Minion Pro, Noto Sans Math and JetBrains Mono.
This automated result does not claim human assistive-technology review.


### Sixth native unit: memory-contract foundation and instructional draft

The sprite exercise requires memory setup, label-relative data pointers and
memory comparisons at function returns. MARS now supplies these through the
existing prepared function checker and bounded function-case profile version 2;
native lesson format 6 retains the same assessment controls. Register-only
version-1 profiles retain their strict field set. Results can be declared in
memory without inventing a v0 contract. Setup and return-byte comparisons use
the existing grading DSL; no separate interpreter or simulator path was added.

The CLI regression passes 156 tests. The full plugin regression passes 1,183
and required meaningful coverage checks; the function reader covers all 75 lines
and 104 branches. One additional exact-archive regression passes separately.
Real sprite qualification accepts both helper-calling and inline implementations
at two data locations and rejects seven faulty programs as complete profiles.
The skip-black fault correctly passes the all-white case, where that fault is
unobservable; mixed and all-black cases expose it. Qualification retained two
initial authoring mistakes (a duplicate fault label and the overly strict
all-white expectation) separately before the corrected run.

The new development archive `003-sprite-rows` has five explanatory predictions,
eight ordered Debug stops, silent Run and an independent vertical-flip activity.
Five public patterns exercise two incoming continuations each, prefilled frame
words, source/guard preservation and full-width saved state. An actual CLI trace
matches every authored register boundary. The generated public DSL checks accept
both equivalent implementations. All 456 book-tool tests pass. Archive SHA-256:
`66193918c851eb2d4573e34e1c7aa5873ac82a2f4f14fc2e8f0ef80bb52b55f0`. Private implementations and qualification faults remain
outside the public book. The main chapter and native LaTeX lab remain authoritative;
this step does not regenerate or change either PDF.

The sixth unit still needs its actual native IDE walkthrough and fresh-process
resume verification, renewed light-theme publication evidence where appropriate,
and direct LaTeX lab integration. Existing sprite figures remain historical
capture evidence. Human learner and accessibility reviews are not claimed. All
unrelated manual MARS edits are preserved, including in the assignment file where
only the authorized new setup overload was staged and committed.


### Sixth native unit: actual IDE, resume and publication integration

The separate authoring plugin completes the sprite unit through real inline
choices, eight ordered debugger boundaries, ordinary Run, 0/5 starter rejection
and 5/5 acceptance of an unsaved inline renderer. Source comments then invalidate
its execution evidence. A new IDE process retains all six quiz attempts and
requires new Debug, Run and independent checks; the independent attempt count
advances from two to three. Both student walkthrough projects close and dispose
normally. All eleven installed plugin JARs match the pinned archives in each
accepted process. The native host now reopens the prediction's source after a Run
selects Bitmap Display; an editor-selection regression and the actual walkthrough
verify that fix.

Earlier interrupted attempts remain separate. One exposed unavailable screen
capture, another documented a macOS permission prompt, and the restored capture
showed the source-tab transition clearly. The author handled OS permissions.
Lesson, Debug and presentation captures now reject uniform gray/black/white
photographs rather than treating a visible Swing model as enough evidence. All
116 authoring tests pass. This check supplements each figure's semantic and pixel
assertions and still requires visual review.

Two new light-theme sprite photographs verify all 128 logical pixels each, with
zero RGB error. The source and expected color hashes match the earlier captures;
the new provenance binds the current plugin and authoring builds. Raw PNGs stay
unchanged and LaTeX supplies the crops, divider, labels and semantic alternative.
Both plugin archives pass the packaged API verifier (91 MARS and 20 authoring
public experimental usages). No internal-API exception was used.

The revised lab PDF has 194 pages. The core sprite lab occupies pages 32–36,
with its figure on page 34; the clipping companion begins on page 37. Those pages
were rendered and checked, and the figure was reviewed in grayscale. Final PDF
SHA-256: `9868a708e87beb769f72cc8817b6db2dca7902e9300fb4402ff744cf19e2fe31`. PDF/UA-2 passes all
1,728 rules and 905,642 checks with zero failures. Fonts remain embedded
with Unicode mappings. Automated validation does not claim human accessibility
or representative learner review. The main learning PDF is unchanged.

The final source-restoration regression completes all 1,184 plugin tests and
required coverage checks. The 456 book-tool tests also pass after the final
LaTeX and figure integration. All unrelated manual MARS edits are preserved.


### Seventh native unit: integer raster (22 November 2026)

The seventh revised unit now has five inline predictions, eight ordered Debug
observations, ordinary Run and an independent dashed-raster function check.
Sixteen public cases use two continuations each, prefilled full-word frames,
varied colors and preserved caller state. Painted halfway cases expose a choice
that a skipped midpoint could hide. The direct color argument differs from its
global word, enforcing the function interface without prescribing its algorithm.

An essential missing grader check was added upstream: protected input ranges
reject execution stores even when they write the same value or restore it later.
Setup occurs before tracing; final-frame and caller-stack comparisons retain
their separate meaning. Version-3 profiles state these ranges explicitly.
Two correct private algorithms pass all cases in ordinary and relocated layouts;
the unfinished starter and ten deliberate faults are rejected. The worked
source's eight boundary states were measured, rather than inferred from prose.

The separate authoring plugin exercised actual inline answers and source
pointers, Debug, Run, failing starter feedback, unsaved correct source and edits
that invalidate prior checks. A fresh IDE process retained the quiz history and
required fresh execution checks. Both projects closed normally, the installed
plugin JARs matched their pinned builds, and no IDE errors occurred. The
authoring code remains separate from MARS. Raw screenshots are verification
evidence; no new screenshot figure is claimed for this publication step.

The written raster supplement is now connected to that native lesson. The lab
PDF has 196 pages, with the revised unit on pages 44–48; all five pages were
rendered and inspected. Fonts retain embedding and Unicode mappings. PDF/UA-2
passes 1728 rules and 914963 checks with zero failures. PDF SHA-256:
`5d7f7a179809a1aa6596ee1eac896fae6679d813f9768d7734ae43e4e056f10d`.
The main learning PDF is unchanged. Automated checks passed 161 CLI tests,
1188 plugin regression tests with required coverage, a subsequent six-test exact
archive importer run, 459 book tests and 117 authoring-plugin tests. These checks
do not replace representative learner or human accessibility review. The
remaining curriculum work continues without claiming completion of all 35 projects.


### Eighth native unit: Life snapshots (22 November 2026)

Original project 008 now has five inline predictions, eight Debug observations,
ordinary Run and an independent changed-cell function exercise. The lesson
connects a stable source, a complete destination, exchanged pointer roles and a
published generation. Fourteen public cases use seven patterns in both physical
allocation orders. Repeated calls keep the same old source, so changing output
storage cannot change the returned difference count. The existing version-3
protection model watches source and halo stores; no new grader API was needed.

Two correct algorithms pass all cases in ordinary and relocated layouts, and
nine faults plus the unfinished starter are rejected. A direct probe measures
every authored state, complete boards, generation words and framebuffer values.
Host oracle tests cover all 512 local center/neighbor combinations; that is
separate from sampled simulated execution and does not prove all possible boards.

The actual light-theme IDE walkthrough and fresh-process resume pass inline
answers, source pointers, all Debug stops, Run, starter failure, unsaved correct
source and source-edit invalidation. Quiz history is retained. Both projects
close normally, eleven installed JARs match their pins and IDE logs have no
errors. Authoring remains in the separate plugin. Raw captures are UI evidence;
this publication step adds no new annotated screenshot figure.

All 462 book tests, seven native importer tests and 118 authoring tests pass.
The MARS production ZIP is unchanged from the raster build with its complete
1188-test regression and required coverage checks; this step adds focused exact
archive evidence rather than claiming a new complete regression. Both plugin
archives pass the packaged API verifier (91 and 20 public experimental usages).

The native lab PDF now has 197 pages; Life occupies pages 49–52 and the
ring-buffer companion starts on 53. Final rendered pages were inspected. Fonts
retain embedding and Unicode maps. PDF/UA-2 passes 1728 rules and 920586 checks
with zero failures. Final PDF SHA-256:
`daea77730b36406190d84f9ac90e523969875d85fe9d2ba6949ce2c666e70bc2`.
The earlier build and validation remain as evidence of the sequence clarification.
The main learning PDF is unchanged. Human accessibility and learner reviews,
the later native units and companion exercises remain outstanding.


### Ninth unit checking foundation (22 November 2026)

The sorting lab's stable partition requires observed active-span access bounds
and a prohibition on caller-frame stores, including later-restored writes.
Those essential checks are now available through version-4 native function
profiles. Older profiles reject the new fields and retain their original
semantics. The existing native data/store observers are reused; simulator
instruction behavior and IntelliJ integration mechanisms are unchanged.

The complete MARS regression passes 168 CLI and 1194 plugin tests, with required
coverage checks. The public foundation has 56 cases, two continuations each,
signed extremes, every valid offset/length, inactive words and stable group order.
Two private correct algorithms pass all cases in ordinary and relocated layouts
through both the public script and prepared API. Both routes reject six faults
and the unfinished starter. All 465 book-tool tests pass. The sorting measurement
tool reproduces the chapter's existing instruction and observation-work counts.
The packaged plugin passes the API verifier with 91 public experimental usages.

This is checking groundwork, not a completed ninth native lesson. Its teaching
sequence, manifest, exact archive import, real IDE/resume walkthrough and LaTeX
lab integration remain. No new screenshot or PDF validation is claimed, and the
verified 197-page lab PDF remains unchanged. Later units, companion integration,
human accessibility and learner review also remain part of the active goal.


### Ninth native unit: sorting exchanges (22 November 2026)

The checking foundation is now connected to a complete native lesson: five
predictions, eight Debug observations, ordinary Run and the 56-case stable
partition. The second-store anchor makes an intermediate duplicate visible
without adding labels to the worked program. A new execution probe verifies
all states, complete arrays, publication metadata and framebuffer contents.
The exact archive passes eight importer tests. The book suite passes 466 tests;
the separate authoring plugin passes 119. Production MARS is the same ZIP as
the foundation with its full 168 CLI / 1194 plugin regression and required
coverage; this step adds a focused archive test rather than claiming a new
complete regression. Both plugins pass the packaged API verifier.

The light-theme IDE walkthrough and fresh-process resume pass actual inline
links, source pointers, the half-exchange breakpoint, all other pauses, Run,
starter failure, an unsaved correct implementation and source-edit invalidation.
Quiz history survives. Both projects close normally, the eleven installed JARs
match their pins and IDE logs have no errors. Authoring remains separate.

The native lab now explains those controls and the checking scope. It adds a
figure from one actual automated photograph: one detail shows the pending store,
the other t2 retaining the displaced key. The raw PNG is unchanged; LaTeX owns
crops, headings and outlines. Color and grayscale render review is complete.
Sorting occupies pages 59–63, its figure page 60, and the maze companion starts
on 63. The final lab PDF has 198 pages. Fonts retain embedding and Unicode maps.
PDF/UA-2 passes 1728 rules and 926302 checks with zero failures. Final PDF SHA-256:
`c2edf4616b81c13e0a6ebb7389d4610a54fae0aeab0aabd957cbeed3bacedd31`.
The main learning PDF is unchanged. Learner and human accessibility review,
later native units and companion integration remain outstanding.


### Tenth native unit: fixed-point motion (22 November 2026)

The development package `012-fixed-point` now preserves the worked motion source
and the complete original 3368-call interpolation corpus. Five inline predictions,
eight ordered Debug boundaries and silent Run distinguish scaled values, display
quantization, partly updated registers and complete published frames. Both upper
reflections retain overshoot. The guided route stays in floor mode; nearest mode
and the overflow probe belong in separate copies, as the revised lab explains.

Independent writing has two checks on the same source. Seven bounded groups
exercise all scalar triples through alternating continuations, varying saved
state and protecting inputs and caller-stack stores. An eighth case observes the
complete caller's answer store before the exit selector overwrites the return
register. A separate console check requires silent normal exit. Three correct
arithmetic forms pass both checking routes in ordinary and relocated layouts;
eight faults and the starter fail. The two added caller faults demonstrate the
need to check the stored result and subsequent exit separately.

A small public grader improvement supplies the failed call number, entry,
continuation and actual supplied scalar inputs. It preserves cancellation and
existing execution semantics. Full required checks passed 171 CLI and 1195 plugin
tests; the exact new archive test passed afterward. All 469 book-tool and 120
separate authoring Jupiter tests pass. Both exact plugin builds pass the public
API verifier. Full regression reports were frozen before focused tests ran.

The native lab supplement now occupies 199 pages, with this unit on pages 70–74.
Those pages and the existing contact figure in grayscale were visually inspected.
The final PDF passes automated PDF/UA-2 with 1728 rules and 932432 checks, no
failures. Minion Pro, Noto Sans Math and JetBrains Mono remain embedded. Native
annotations and unchanged source PNGs are retained. Human learner and assistive
technology reviews remain pending. The original manuscript, historical chapter
checks and all 41 manual MARS source edits are preserved.

The first authoring attempt observed all eight Debug states but stalled on its
own ordinary-Run text expectation. The separate automation was corrected and
retested; the incomplete attempt remains labeled in private evidence. Completed
desktop/resume evidence is recorded below when verified, rather than inferred
from command-line checks. Later native units and companion integration remain
open. The proposed MARS-versus-hardware opening remains deferred.


Two completed light-theme IDE walkthroughs now verify actual inline choices and
source pointers, all eight Debug snapshots, ordinary Run, failing starter
feedback, unsaved correct source, both independent checks, source-edit
invalidation and fresh-process continuation. Both projects closed normally;
all eleven installed JARs match their pins, and both IDE logs contain no ERROR
or SEVERE entries. Saved progress retains the same six quiz attempts. Function
assessment attempts increase from two to three and exit assessment attempts
from one to two. Old Debug, Run and both assessment passes are empty after the
edit and are freshly earned in the resumed process. This is automation evidence;
it does not claim a human learner completed the unit unaided.


### Memory-locality native foundation (22 November 2026)

The next native draft is `011-memory-locality`, preserving original project 011.
Its source and independent task retain the existing mapping, bounded window,
caller preservation and one-read/one-write contracts. A second complete worked
source changes only METHOD to one, allowing separate ordinary and tiled Debug
routes without editing the first route in place. Six predictions use different
answer positions. Eight proposed stops were measured through actual execution,
including the alternate program's real main entry and aligned caller stack.

A public grader gap would otherwise have weakened the native rotation task:
final memory and access bounds do not prove exactly one read/write per cell or
read-before-corresponding-write. `FunctionTransferCheck` adds a bounded data-only
permutation map, optional in native function-profile version 5. It observes
existing forward execution notices, retains only visit state and the first
failure, and resets per call. It accepts arbitrary valid traversal orders and
private stack frames. Final bytes, inactive allocation accesses and protected
stores remain separate checks. Existing simulator execution is unchanged.

The public rotation profile has 21 shape cases, two continuations each, and a
complete caller-result case; normal exit is checked separately. Qualification
ran 36 processes: three correct algorithms in ordinary and relocated layouts,
plus a wide-address case, pass both grading routes (14 positive processes).
Ten faults and the starter are rejected through both routes (22 rejections).
The script route preserves the original ten shapes; the prepared route checks
the expanded 22 cases. These are deliberately different stated finite corpora.
All 473 book-tool tests pass, including coordinate bijection/round-trip checks
for all 289 dimension pairs and complete archive dependencies.

This is native checking and lesson foundation, not a completed eleventh unit.
Its exact native import passed, binding both execution sources and all transfer
profiles. Full MARS checks pass 184 CLI and 1198 plugin tests, including required
coverage gates; the focused exact-archive test passes afterward. The function
profile parser covers all 109 lines and 131 branches. The final packaged build
passes the API verifier. Authoring walkthrough, real IDE/resume, written lab
integration and renewed publication checks remain to be performed.
Human learner review remains pending. No new photograph or PDF validation is
claimed by this foundation step; earlier chapter evidence remains preserved.


### Eleventh native unit: locality IDE and written supplement (22 November 2026)

The `011-memory-locality` development unit now completes two actual light-theme
IDE walkthroughs. Six inline predictions use real source highlights and quiz
links. Both four-stop Debug routes verify the measured registers and displayed
native cache counters, selecting the corresponding ordinary/tiled execution.
Both complete Runs exit silently. Independent writing checks the failing starter,
unsaved correct source, all 22 rotation cases and normal exit. Three source edits
clear all six execution passes. A fresh process retains seven quiz attempts and
rechecks both Debug routes, both Runs and both assessments. Projects close
normally; all eleven installed JARs match, with no ERROR or SEVERE log entries.

The separate authoring plugin passes 121 Jupiter tests; all 473 book tests pass.
The existing fully tested transfer build is reused without new MARS source
changes. Exact MARS and authoring archives pass the API verifier. The written
native LaTeX supplement explains source switching, complete observation intervals,
cache counts, the pending caller store and per-call independent access evidence.
It remains usable without replaying the IDE controls. Existing actual cache
photographs and native annotations are retained.

The final supplement has 200 pages, with locality on pages 75–80. All six pages
were visually inspected, including the cache figure in grayscale. A command
initially split across pages was grouped and the corrected PDF revalidated.
It passes 1728 PDF/UA-2 rules / 938435 checks with zero failures and established
embedded fonts. Human learner/assistive-technology review remains pending.
All 41 manual MARS source edits and historical evidence remain preserved.
Later native units, companion integration and remaining instructional-system
work are open; the full goal is not complete. The MARS-versus-hardware opening
remains deferred.


### Bounded-interfaces native foundation (22 November 2026)

The next draft `013-bounded-interfaces` preserves original project 013 and its
complete packet parser. Two complete variants change only available count or
checksum trailer. Seven predictions and twelve measured Debug states distinguish
permitted reads, ordered checks, tentative values, accepted fields and the
caller's later status/pointer stores. Three Runs precede independent LF-delimited
binary parsing. These are proposed native activities with actual CLI state
bindings; no new IDE or screenshot proof is inferred from them.

Native checking now preserves previously missing script assertions: returned and
stored full data pointers, caller-wide store windows resolved from assembly
labels, and a forbidden continuation interval. This detects truncated pointers,
temporary/restored padding writes and an incorrect return that falls through to
the requested stop. Function-profile version 6 remains bounded data. Older formats
reject new fields and retain their earlier behavior. No simulator accuracy or
execution semantics changed, and authoring code remains separate.

The unmodified Kotlin public input construction exports all 393 function inputs;
every name, byte sequence, count and offset matches the native generator. Profiles
have 139 prefix/boundary/caller cases and 256 first-byte cases, with repeated
continuations giving 789 checked returns. The original standalone script retains
394 checks. Four correct methods pass both routes in ordinary/relocated layouts,
plus a wide-address fixture: 18 positive JVM processes. Nine faults and the starter
are rejected through both routes: 20 negative processes. These are finite corpora.

The complete checker passes 194 CLI and 1203 plugin tests and required coverage
checks. Parser coverage is 126/126 lines and 142/142 branches. The exact student
archive passes native import afterward. All 477 book-tool tests pass, and the
final exact plugin build passes API verification. Full reports were frozen before
focused tests. A superseded partial build was explicitly canceled after finding
the continuation gap; it is not reported as passing. All 41 manual MARS edits and
historical evidence are preserved.

The twelfth unit is not yet complete: its separate authoring walkthrough, real
IDE/resume, written lab integration and renewed publication verification remain.
Human learner review is open; no new photograph or PDF validation is claimed.
The full curriculum goal stays active, including later units and companions.
The MARS-versus-hardware opening remains deferred.


### Twelfth native unit: bounded interfaces (22 November 2026)

The foundation above is now integrated with the written lab and actual IDE
controls. A separate authoring walkthrough clicks seven inline predictions,
including one incorrect answer and recovery, observes all twelve Debug boundaries
and executes three ordinary Runs. The independent starter fails; unsaved corrected
source passes 139 bound/caller cases, 256 byte cases and normal exit. Source edits
clear all nine execution passes. Reopening in a fresh IDE process retains eight
quiz attempts and re-earns every pass; the four source hashes remain unchanged.
Both projects close normally. All eleven installed JARs match the pinned builds,
and neither verified IDE log has ERROR or SEVERE entries. Both launches use the
actual clean cleanSandbox runIde tasks.

The separate authoring companion passes 122 Jupiter tests and all 477 book tests
pass. The exact MARS and authoring archives pass the API verifier. This stage
reuses the preceding checker build and its full regression/coverage results;
it makes no further production MARS changes. All 41 manual MARS edits match their
preserved content. Grading qualification remains finite input evidence, with
independent algorithms and deliberate faults retained privately.

The directly edited LaTeX lab explains the three complete sources, all requested
pauses, tentative values, publication stores, next-stop highlighting, repeated
checks and full-width pointers. The seven-byte guided header failure is now
explicitly distinguished from the existing eight-byte photograph, which passes
the header bound before failing the full-packet bound. Existing real photographs
and native annotations are retained. The complete supplement is 201 pages;
Chapter 12 occupies pages 81–92, including its unchanged UTF-8 written companion.
All twelve pages were visually inspected, with both packet figures also checked
in grayscale. Fonts remain embedded Minion Pro, Noto Sans Math and JetBrains Mono.
The final exact PDF passes 1728 PDF/UA-2 rules / 947016 checks with zero failures.

This completes automated integration of the twelfth main unit. Human learner
and assistive-technology reviews are still pending. Later native units, native
companion integration and remaining instructional-system work remain open; the
full curriculum goal stays active. The MARS-versus-hardware opening stays deferred.


### Measuring-programs native foundation (22 November 2026)

The thirteenth main unit, preserving original project 022, now has a draft native
package with nine predictions, nineteen CLI-measured Debug boundaries, five
complete worked sources and an independent matrix-vector reduction. Its topics
are exact signed doubleword sums, initialized partial output, clipped reduction
ends, caller publication and explicitly scoped cache observations. Exact counts
belong to the worked sources; independent algorithms are graded by results and
access contracts. The native LaTeX chapter/lab remain authoritative and unchanged
at this foundation stage.

The native checker adds assembled read/write policies rather than omitting reads
from alignment padding. The original Kotlin input construction independently
matches all eight original input families. Native data retains those families and
the default caller, adding a direct default-input function case and positive
2^31. Four correct algorithms under ordinary/relocated layouts and a wide-address
variant pass both routes (18 actual JVM processes). Nine faults and the starter
fail both routes (20). The native profile has ten repeated-call function cases
and one complete-caller case, plus a separate normal-exit profile. The standalone
script retains its nine original checks.

All 481 public tooling tests pass. The checker passes 217 CLI / 1212 plugin tests
and required coverage gates; function-profile parsing covers 144/144 lines and
150/150 branches. The exact student archive passes import afterward. All nineteen
register bindings match actual probes and source hashes; the two larger examples
reproduce 8745/5388 and 12841/1068 launch accesses/misses before caller publication.
These are native cache observations, not host time or hardware cycle predictions.

The first exit-profile draft used the wrong input field and was rejected during
exact import. That data was corrected and the passing archive rechecked; the
rejected diagnostic remains private. No new screenshot, GUI reset, IDE resume or
PDF validation is inferred from CLI evidence. Authoring automation, real student
controls and resume, written lab integration and publication verification are
still required before this unit is integrated. Human learner and accessibility
review remain open, and the full curriculum goal stays active. The proposed
MARS-versus-hardware opening stays deferred.


### Thirteenth native unit: measuring programs (22 November 2026)

The measurement foundation is now integrated with the written lab and real IDE
controls. Two accepted light-theme walkthroughs exercise nine inline predictions,
nineteen guided Debug stops, five ordinary Runs and both independent assessments.
The starter fails; unsaved correct source passes. Six source edits clear twelve
execution passes. A fresh process retains ten quiz attempts and re-earns every
execution pass while all six source hashes remain unchanged. Both projects close
normally; all eleven installed JARs match pinned builds and neither accepted IDE
log has ERROR or SEVERE entries. Actual clean cleanSandbox runIde tasks precede
each launch, including a retained partial diagnostic run.

Both accepted sessions click the visible native Reset button at the stopped
call boundary under each method. One access/miss becomes zero; return counters
then show 8744/5388 or 12840/1068. Guided launch observations and the caller's later
store remain separate. This is actual GUI reset evidence, extending the older
core-only proof. The separate authoring companion follows pinned lesson content
and uses real student controls; no progress setters or new MARS authoring code
are introduced. Its 123 Jupiter tests and all 481 book-tool tests pass. The exact
MARS and corrected authoring archives pass paired API verification.

The native LaTeX lab now explains the observed route and independent evidence.
It retains original real photographs and native annotations. Its six pages
93–98 and both adjacent boundaries were visually inspected, with both figures
also reviewed in grayscale. The complete supplement has 202 pages and passes
1728 PDF/UA-2 rules / 953365 checks with zero failures. Embedded Minion Pro,
Noto Sans Math and JetBrains Mono remain in use. All 41 manual MARS edits and
historical verification are preserved. Human learner and assistive-technology
reviews, later native units, companion integration and remaining system work
remain open. The full goal stays active and the proposed opening remains deferred.

## Native MSA integration, 22 November 2026

The fourteenth main unit now has an exact importable student archive, nine
inline predictions, 31 Debug stops across six complete sources, six ordinary
Runs and independent RGB555 function/caller plus normal-exit assessments.
The complete vector observations use native lesson format 7 and the same
immutable register snapshot as Variables. Every original count trial remains
in the independent profile, with two continuations and a complete caller
(34 cases, 67 calls); no specific MSA schedule is required for acceptance.

Both actual light-theme IDE walkthroughs complete every guided observation and
assessment. The first includes an incorrect prediction followed by recovery,
the failing starter and correct unsaved source. Seven source edits clear all
fourteen execution passes; a fresh process retains ten quiz attempts and
re-earns every pass with all seven source hashes unchanged. Eleven installed
JARs match pinned archives and accepted IDE logs contain no ERROR/SEVERE
entries. Both projects close normally before their owned processes stop.
Actual clean cleanSandbox runIde tasks precede both launches. Capture automation
remains in the separate dependent authoring plugin.

All 486 book-tool and 125 separate-authoring Jupiter tests pass. The previous
checker regression remains 217 CLI / 1220 plugin tests and passing coverage
gates; the exact student archive import test ran afterward. Paired plugin API
verification reports compatible public APIs, with the existing 91 MARS and
20 authoring experimental notices. No new full regression is claimed for the
additional archive-only test. All 41 manually edited MARS files are preserved.

The directly edited LaTeX supplement explains the guided route, caller stores,
independent checks, useful failure investigation and progress after edits.
It retains existing actual photographs and native annotations. Pages 99–102
and adjacent boundaries 98/103 were visually inspected; page 100's figure was
reviewed in grayscale. The complete supplement remains 202 pages and passes
1728 PDF/UA-2 rules / 955669 checks with zero failures. Embedded Minion Pro,
Noto Sans Math and JetBrains Mono remain in use. Human learner and assistive-
technology reviews, later native units and remaining system work stay open.
The full curriculum goal stays active; the proposed opening remains deferred.

## Native PCM integration, 22 November 2026

The fifteenth main unit now has an exact importable student archive, eight
inline predictions, 22 guided Debug boundaries across seven complete sources,
seven ordinary Runs and six independent assessments. Two additional complete
count variants (63/64) support independent extension. The explanations connect
signed recovery, exact endpoints, saturation versus wrapping, mask units, tail
accumulation, exact aliases and returned versus published counts. The in-place
B variant explicitly warns that its later wrapping call receives changed input.

All 331 original function-trial constructions match independently exported
Kotlin initialization. Native profiles preserve the complete 332 public cases
and add one four-call reuse case: 333 cases, 337 calls and separate silent exit.
Private qualification records 30 original/native processes, accepting 14 and
rejecting 16 as expected. Equivalent scalar, vector, signed-halfword, relocated
and wide-address implementations remain accepted. The native reader's bounded
limits are respected by splitting five groups rather than dropping trials or
reducing the original per-trial instruction budget.

The exact archive exposed an anchor grammar gap for underscored MSA mnemonics.
MARS commit 53dac9e7 accepts lexical names such as copy_s.d, retains strict
punctuation rejection and uses the existing native PSI operation-head resolver.
Full regression passes 217 CLI / 1224 plugin tests and required coverage gates.
All 491 book-tool and 126 separate-authoring Jupiter tests pass. Both exact
plugin archives pass paired public API verification, retaining 91 MARS and
20 authoring experimental notices on the pinned IntelliJ target.

Two actual light-theme IDE walkthroughs exercise all questions, Debug boundaries,
Runs and assessments. The first includes wrong-answer recovery, the failing
starter and a correct unsaved implementation. After edits to eight sources
clear twenty execution passes, a fresh process retains nine quiz attempts and
re-earns all twenty passes. Eight source hashes remain unchanged across reopening.
Each session's eleven installed JARs match exact pinned archives; accepted IDE
logs have no ERROR/SEVERE entries. Both projects close normally before their
owned Java processes stop. Actual clean cleanSandbox runIde tasks precede both
launches. Screenshot automation stays in the separate dependent plugin.

The directly edited LaTeX lab now explains the complete route, independent
check groups, useful failure investigation and saved progress after edits.
Original real photographs and native annotations remain unchanged. The lab
supplement has 203 pages, with this unit on 103–108. Those six pages and adjacent
boundaries were visually inspected; both annotated figures on page 105 were
reviewed in grayscale. The exact final PDF passes 1728 PDF/UA-2 rules / 961004
checks with zero failures and retains embedded Minion Pro, Noto Sans Math and
JetBrains Mono. All 41 manual MARS files are preserved. Human learner and
assistive-technology reviews remain pending. Later native units and remaining
system work stay open; the full curriculum goal remains active and the proposed
opening remains deferred. The next main unit develops fixed-point filtering.


## CRC native delivery completed, 22 November 2026

Chapter 19 now has its connected native student package and matching written lab:
13 inline predictions, 8 Debug activities / 22 stops, one ordinary Run and six
assessment groups. The original chunk/file case profiles, budgets and permissions
are retained, with 164 function cases / 170 calls and one ordinary exit case.
Production import and native PSI resolve the exact shipped archive and anchors.
Eight value probes and 25 retained-correct/fault qualification processes verify
the examples and equivalent implementations. All 539 book-tool tests pass.

The accepted first/resume desktop walkthrough verifies actual inline controls,
source pointers, deterministic read fixtures supplied by MARS, ordinary Run/Debug,
failed-starter feedback, unsaved-source assessment, source-edit invalidation,
persisted quiz history, rechecking and cleanup. Eleven installed JARs match their
pins in both processes and accepted logs contain no ERROR/SEVERE entries. A separate
authoring-only zero-operand assertion failure was corrected and retained as a
diagnostic; the 133-test authoring suite passes. Screenshot automation remains
outside MARS. All 41 manual MARS edits are preserved.

The directly edited LaTeX lab proof is 210 pages, with CRC on 135–140. Three actual
light-theme capture assets have native annotations and semantic descriptions.
Color/adjacent-page and grayscale review are complete; PDF/UA-2 passes 1,728 rules
and 986,035 checks with zero failures. The main learning proof remains unchanged.
Human author, learner and assistive-technology review are not claimed. The goal
remains active; next is Chapter 20's QOI guided/free-writing delivery, beginning
with its current sources, original contracts and publication/error boundaries.


### SHA-256 companion: native lesson and written workflow verified

The SHA companion now has its 0.2.0 student lesson: sixteen inline predictions,
nine Debug activities/32 stops, eight prerequisite construction groups, a final
full assessment and one Run. Eight limited public experiments support one
continuing independent six-function implementation. Seventeen update-only cases
allow context feedback before finalization works. All 128 original cases remain
unchanged; the final assessment reuses them to recheck the completed revision.

Actual first-use and fresh-process resume pass with all seventeen quiz attempts,
nine edited sources and nineteen renewed execution checks accounted for. Both
processes use the same eleven packaged JARs and have no ERROR/SEVERE log entries.
Screenshot automation remains in the separate dependent authoring plugin. The
full authoring suite passes 137 tests; both packaged archives remain compatible
with the pinned IntelliJ target. All 572 public tooling tests pass.

The directly edited SHA companion includes one new actual light-theme image with
native crops, A/B annotations and a semantic description that distinguishes the
paused instruction from the next lesson pointer. The accepted lab proof is 216
pages and passes PDF/UA-2's 1,728 rules and 1,024,284 checks. Reviewed changed pages
and adjacent transitions are recorded in the private verification receipts.
Minion Pro, Noto Sans Math and IntelliJ-derived minted highlighting remain.

See revision/sha-unit.md and private native-sha-ide-01 for exact scope. Human
learner/assistive-technology review is not claimed. The curriculum goal remains
active; the next native companion is RSA arithmetic, followed by the remaining
book/lab progression and delivery work.


### WAV native lesson and written companion, 23 November 2026

Project `031-pcm-wav` now has the native student lesson and verified written lab
connection recorded in `revision/wav-unit.md`. Its continuing independent task is
whole-frame reversal; the six-function pipeline remains a complete worked example.
Ten predictions, five Debug activities, one Run and staged/final behavioral checks
complete in actual first-use and fresh-process resume trials. A public inline-quiz
capture has native annotations in the lab. The separate authoring plugin owns all
capture and fixture injection. Native PNG and the retained video sequence remain
next; this milestone does not close the broader curriculum goal or replace human
learner and accessibility review.


### PNG native lesson and written companion, 23 November 2026

Project `033-png` now connects the complete decoder to six limited native
experiments and one continuing independent forward-filter task. The actual
first-use and fresh-process resume trials verify twelve inline predictions,
eighteen Debug boundaries, one Run, seven assessment stages, source invalidation
and saved progress. The written companion distinguishes the native embedded
trace from the optional standalone viewer and original construction capstone.
A public vector-packing photograph has native annotations and a numerical lane
explanation. See `revision/png-unit.md` for publication and verification scope.
Retained lossless-video and H.263 delivery and human reviews remain open; this
milestone does not complete the broader goal.


### Lossless-video native assessment foundation, 23 November 2026

The continuing minimal-exclusive-bounds task retains all 182 original cases in
eight native profiles, both byte orders and 192 calls. Original/model state
comparison and native qualification preserve equivalent two-pass implementations,
wide addresses and private scratch while rejecting the existing assembled faults.
The actual profile parser and combined-assessment checks pass. This foundation
does not deliver the native lesson yet; bounded worked experiments, inline
predictions, a student package, actual IDE first/resume and written integration
remain next. See `revision/lossless-unit.md` and the private qualification receipts.


### Lossless-video native student-unit draft, 23 November 2026

The scoped student unit now has six worked experiments, twelve inline predictions,
eighteen Debug boundaries, one Run and staged/final bounds assessments. It uses
the actual supplied helpers with relevant static data, plus explicit arithmetic
models for time and coordinate reasoning. Core probes, exact exported-starter
qualification, package import and native PSI checks pass. Required work remains
actual IDE first/resume and directly edited written-lab/capture integration.
See `revision/lossless-unit.md`; this draft does not replace the accepted PDF
or close the broader curriculum goal.


### Lossless-video native IDE and written companion, 23 November 2026

Actual first-use and fresh-process resume now verify all 24 native activities,
unsaved checking, source invalidation and retained progress. The written lab
connects six bounded experiments to the supplied codec and continuing bounds
implementation, distinguishes record-size/history cases and keeps optional
file/player work explicit. A real public XOR capture has native annotations and
an explanation of unsigned lane counting. Exact plugin compatibility, installed
binaries, final visual/grayscale review and PDF/UA-2 evidence are retained.
See `revision/lossless-unit.md`. Native H.263 delivery and course-wide acceptance
work remain; this milestone does not close the full goal or claim human review.


### H.263 reconstruction native assessment foundation, 23 November 2026

The independent eight-by-eight reconstruction task retains all 138 public cases,
both byte orders and 146 calls in five native profiles. Exact original/model
comparison and native qualification preserve signed correction ranges, strict
clipped counts, active-row ownership and failure publication under an exact
prediction alias. Existing valid alternatives pass and all seven assembled
faults are rejected. Actual profile parsing and combined assessment tests pass.
This is the grading foundation; the native numerical lesson and actual IDE/
written integration remain next, followed by native stream delivery. Existing
full inverse-accuracy and restricted H.263 interoperability evidence remains
separate. See `revision/h263-prediction-unit.md` for scope.


### H.263 native numerical student-unit draft, 23 November 2026

Six bounded experiments now connect unchanged SAD, motion, prediction, block
transform/reconstruction and six-block coordination helpers to the continuing
independent reconstruction task. Twelve predictions, eighteen Debug boundaries,
one Run and staged/final assessments form the native draft. Complete-byte/core
probes, exact exported-starter qualification and actual package/PSI tests pass.
First/resume desktop verification and direct native LaTeX integration remain,
followed by the separate restricted H.263 stream unit. Numerical example success
is not a renewed inverse-accuracy or interoperability claim. See
`revision/h263-prediction-unit.md` for the exact evidence scope.


### H.263 native numerical IDE and written integration, 23 November 2026

The numerical unit now passes actual first-use and fresh-process resume trials in
IntelliJ. Its twelve inline predictions, eighteen Debug stops, one Run and six
assessments are connected directly to the written lab. The accepted trials retain
thirteen quiz attempts and seven edited sources, renew thirteen execution checks,
and each pass 276 overlapping case placements. The continuing function remains
one editable file; no solution is added to the public archive. The display title
is now “Predict and reconstruct H.263 blocks” after the native layout guard found
that the longer title exceeded the Learn viewport. All fifteen other archived
files, including lesson activities and executable cases, are byte-identical to
the previously qualified draft.

A real public-source SAD stop supplies the new native annotated figure: actual
frame line 185, full w4 and eight halfword lanes of 8160. The caption distinguishes
the actual frame from the editor's earlier source pointer. The text explains
adjacent byte-pair widening across rows, function-specific return units and the
limited scope of the six-block metadata rejection. Raw QCIF generation is an
optional whole-picture extension after the bounded experiments.

The separate authoring plugin passes 143 Jupiter tests, public tooling 618, and
six publisher guards. Both pinned plugins pass compatibility verification against
IntelliJ 2026.3, with no internal API use reported. The accepted 226-page lab proof
was visually reviewed on pages 215–224, with figure 217 also in grayscale; after
a paragraph-flow correction, changed pages 218–219 were reviewed again and all
other reviewed pages are pixel-identical. Exact final PDF/UA-2 validation passes
1728 rules and 1,067,167 checks with zero failures. Private
`native-h263-prediction-ide-01` retains the rejected initial layout trial, both
accepted trials, exact pins, installed-JAR comparisons and saved progress.

This completes native numerical delivery and written integration. Native
restricted H.263 stream delivery and course-wide acceptance remain. No renewed
inverse-accuracy gate, stream conformance run, human learner review or
assistive-technology review is claimed by this milestone.


### H.263 native bounded-reader assessment foundation, 23 November 2026

The native reader model retains all 592 original public cases and 598 calls,
including every field width zero through 32, every starting phase, both native
byte orders, full-width invalid metadata, maximum capacity and retained-cursor
success/failure/empty sequences. Eight format-22 profiles retain the original
100000-instruction case allowances. Two final checks each select 328 placements;
their union covers all 592 original cases, with the shared boundary/metadata/reuse
cases repeated. This fits existing file, assessment and instruction quotas.

A small upstream grading extension initializes native pointers from relocated
public data labels before a call. It does not require an assembly initialization
helper or fix a student's data address. The public starter adds only
`reader_bytes_tail`, a symbol within the unchanged final 64-byte allocation;
its unfinished function remains unchanged. The full first MiB has an interval
access policy, while an always-present caller object guards the final tail.
Together they reject input writes and reads outside the selected field over the
entire allocation, without increasing grader quotas. Context/caller retention,
zero-width no-store behavior and cumulative reused-call state remain checked.

The original checker and native model agree on every original before/after input
window, 48-byte context, status/value object, argument and result for 598 calls in
each of three implementations. Four native valid variants, including wide data/
text and a full private stack frame, pass 2368 placements. The starter and all
eight known faulty readers are rejected across 5328 further placements; the
stale-result fault is rejected by the two reused-cursor cases. Actual plugin
parsing retains all eight profiles and combines both bounded final checks.

Upstream implementation commit `6d50e406` passes the full 293 CLI / 1329 plugin
regression, 100% plugin package line/branch coverage and generated-parser ledger
gates. Two additional actual-profile parser tests pass in test-only commit
`34e7db91`. Public tooling passes 622 tests. All 41 unrelated MARS edits are
preserved. Exact pinned CLI, failed private fixture attempts, original comparisons
and qualification receipts are retained in private
`native-h263-reader-foundation-01`.

This is the stream unit's independent grading foundation. The native stream
experiments, student archive, actual IDE first/resume delivery and written-lab
integration remain next. No new whole-clip interoperability, inverse-accuracy,
PDF/accessibility or learner-review result is claimed by this step. Existing
stream and publication evidence above remains historical and separate.


### H.263 native stream student-unit draft, 23 November 2026

`revision/lessons/035-h263-stream` now contains a complete native draft with
28 activities: twelve inline predictions, eighteen Debug stops across six worked
programs, one ordinary Run and nine staged/final reader assessments. Small actual
writer, coefficient, macroblock and header experiments precede the complete clip
caller. A bounded arithmetic experiment prepares the independent reader without
publishing its implementation. The supplied helpers and coding tables are exact
copies of the existing worked encoder; no codec instruction body changed.

The actual clip caller runs eight generated QCIF pictures, then separately exposes
truncated and trailing-byte requests. Return status/count pairs are `0/8`, `3/1`
and `3/2`. The final pair distinguishes accepted count from whole-request success.
Relative paths work from the assembly directory in ordinary MARS Run/Debug. The
bundled Python-generated raw data has rate 5000/1001 fps and duration 1.6016 seconds.
The public inspection tool verifies the actual full output and accepted prefixes
using strict profile parsing and FFmpeg, refuses an existing report, and explicitly
retains the failed status of the latter requests.

Twelve core probes verify all eighteen boundaries in both byte orders, exact
payloads and guards, forbidden buffer access on rejection, accepted reference
pointers, closed descriptors and complete output bytes. An actual fresh CLI run
uses real files and matches the virtual-file observations byte for byte. Independent
decoding checks eleven pictures / 33 plane observations: maximum local/decoder
difference two and minimum finite PSNR about 45.865 dB. This limited default-clip
evidence does not renew the separate full inverse-accuracy gate.

The exact exported reader starter passes all 592 cases with three private bodies,
including relocated wide data/text; its unfinished form fails all 592. The public
archive contains no completed reader. Six public artifact tests bring the full
tooling suite to 628 passing tests. Two actual importer/native-PSI Jupiter tests
preserve all activities and thirty source anchors. The explicit 31-file archive
is approximately 78 KiB. Test-only MARS commit `7b28cb6d` adds its archive and tests;
production simulator/plugin code and all 41 unrelated edits remain unchanged.

This completes the student-unit draft and executable qualification. First/resume
IDE walkthroughs, new native publication capture and direct written-lab integration
remain next, followed by course-wide acceptance. No new graphical IDE launch,
publication PDF, accessibility validation or human learner review is claimed by
this draft. Exact sources, tools, process records and output reports are retained
in private `native-h263-stream-unit-01`.
