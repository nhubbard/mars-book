# Lesson package format version 1

## Files and ownership

A package lives in packages/PACKAGE_ID. package.json is the entry point. It describes
content for a book and for future interactive consumers; it is not a plugin extension
or executable script. The JSON contract is versioned independently of package content.
The current Python validator implements the supported version 1 subset. Additional
fields for future execution must be specified and validated before being used.

| Field | Meaning |
| --- | --- |
| formatVersion | Supported structural version, currently integer 1 |
| id | Stable lowercase package ID matching the directory name |
| version | Three-part content version; changing instructions, grading, or figures creates a new version |
| status | Editorial state, initially draft |
| title and audience | Student-facing purpose and expected background |
| prerequisites and learningObjectives | Prior knowledge and observable learning outcomes |
| environment | ISA, educational ABI, JDK, IDE release, and full upstream MARS commit |
| entry and assignment | Paths to the starter entry source and assignment Markdown |
| inputDomain | Bounded task contract; consumers must not infer unspecified behavior |
| modes | Guided and free-writing presentations of the same assignment |
| progression | Default order and proposed instructor controls; local implementation is stated explicitly |
| steps | Ordered learning steps with stable IDs and prerequisites |
| publicChecks | Instructor-authored Kotlin grading scripts made public for this package |
| figures | Path to the declarative capture plan |
| exports | Explicit allowlist used to build the student distribution |

Every path is relative to its package and must resolve inside it. The exporter rejects
symlinks, hidden paths, traversal, and instructor/reference/private directories. Do not
place secrets anywhere in the public repository: an export filter cannot remove them
from Git history. Builds sort archive entries and fix ZIP timestamps and permissions,
so identical sources generate identical student archives.

## A learning step

A step has id, title, content, prerequisites, kind, quiz, hints, and completion. The content
path points to Markdown with an explanation, an edit or inspection task, an expected
observation, and a comprehension question. The quiz stores a stable ID, prompt, and
choices with stable choice IDs. Correct choices and instructor explanations live in the
private repository. The Markdown repeats the visible quiz for book readers; edits must
keep it consistent with the machine-readable quiz. Hints have stable IDs and increasing
specificity. The second hint in this first package reveals the expected observation.

Prerequisites must precede a step and form an acyclic order. Completion in this initial
package is learner confirmation: the local tools do not claim to verify individual edits
or quizzes. The final-program CLI checks are executable now. A future semantic checker
must declare its state boundary and assertion vocabulary before a step can use it.

## Equivalent solutions and checkpoints

The guided explanation uses a known register convention to teach visible state. Reference
checkpoint scripts verify that the book's illustrated state is accurate. They are not
submission grading scripts. Public and private free-writing checks assert only bounded
input/output behavior and normal exit, accepting the reference and an alternative with
a different arithmetic order and scratch register. Future tasks can constrain calling
conventions, memory boundaries, vector state, or permitted effects when those are part
of the published contract.

When the current grader cannot express an essential property, add a supported checking
function to MARS with tests. Document its semantics and required MARS version here. Do
not implement a second simulator or weaken the assignment to avoid a missing check.
The console package needs no new grader API: input fixtures, runToExit, expectOutput,
runUntil, step, and expectRegister cover its actual contracts.

## Instructor companion

The private package shares the public ID and version. It includes reference/main.asm,
reference/alternative.asm, checks/private.kts, checks/guided-reference.kts, quiz-keys.json,
and teaching-notes.md. An instructor build records the public student archive digest,
private Git revision, MARS CLI digest, Java version, and check outcomes. Private quiz keys
must cover every public quiz exactly once and refer to an existing choice ID.

## Reading sequence and teaching fixtures

The reading field points to reading.json, which has formatVersion 1 and an ordered
segments array. Each segment has a stable id and a kind. A prose or fixture segment
names its Markdown content. A step names one existing step ID. A figure names one
capture-plan figure. The reading sequence contains every step once in prerequisite
order and every IDE figure once. Standalone step files remain the microstep guide;
the reading sequence adds the explanatory connective material around them.

A fixture also has a semantic role, such as worked-example, state-table, reference-table,
concept-box, diagnostic-example, diagnostic-table, exercise, or figure. Its typesetting
metadata gives a stable label, inline placement, and numbering intent. The Markdown
body contains the complete explanation and any table, code, or diagram. It can be read
without a LaTeX template. The directly edited LaTeX publication preserves these explanations, environments, and labels. SVGs are
Markdown previews; nativeLatex identifies existing TikZ sources where provided.
These fields describe content; they do not execute LaTeX or HTML.

The publication uses main.tex and native includes. Add author passages directly with
input/include wherever they belong. No JSON insertion list or generated Markdown
controls the publication. reading.json continues to describe the guided exercise order.

## Pixel address checks

The single-pixel chapter requires expectDataAddress(label, expectedLong), added in the
MARS revision pinned by that package. It verifies the destination agrees with the
configured display base, without prescribing a scratch register. An exact word by label
is insufficient if the label has moved away from that base. The check supports byte
addresses and addresses above 4 GiB. It does not verify an IDE frame or a source header.

This package representation supplies exercise/trainer drafts. Native LaTeX includes
control the publication independently.

## Execution-history contracts

The seven-segment task pins a CLI revision with `traceMemoryStores(address, byteLength)`
and its label/offset overload. Subscribe before running, then assert the complete byte
sequence with `expectByteStores(address, *values)`. Every observed store must have that
address and width one; repeats count separately. `expectNone()` rejects any overlapping
execution store of any width, even if its original data is restored. Reads and grader
setup edits are excluded. Close a trace to retain a prefix and stop observing; case
cleanup also unregisters it. The 65,536-record bound fails assertions on overflow.

Use a broad MMIO range when extra writes to other ports are forbidden. A final-memory
check alone cannot express an ordered protocol. Label-based protected ranges follow
actual allocations rather than hard-coded data addresses. These checks do not constrain
student scratch registers or require labels used only by illustrated Debug checkpoints.

## Device-model contracts

The MarsBot package pins a CLI with connectMarsBot(instructionsPerTick), which attaches
the same model used by the IDE before execution starts. A case owns at most one bot and
cleans up its memory observer, clock listener, and backstep checkpoint on completion.
The headless clock is chosen by the grading script; source-header settings configure
the IDE. Do not assume the CLI parses tool headers.

MarsBotCheck.expectState checks position, heading, moving and penDown controls.
expectTrail checks the complete continuous chronological pen trail using turn vertices;
straight forward strokes are coalesced by the model. Empty arguments require no trail.
Both accept a finite nonnegative tolerance (default 1e-9). Neither asserts UI rendering.
Use separate execution-store traces to protect input objects and feedback registers.

## Public observation boundaries

A lesson may require a named text checkpoint as part of its published interface when
an objective needs intermediate actual state. Life requires generation_ready after each
complete generation from zero through the requested count. This is a public contract
like a function entry point; it does not permit private register or label requirements
in free-writing checks. Use only the named state that the assignment promises there.

The existing runUntil pauses before that instruction. Step past the current occurrence
before waiting for the next one. Life creates a label-based execution-store trace on the
current allocation for that interval, asserts no writes at the next boundary, closes it,
and then follows the new current allocation. This expresses phase-dependent source
protection without requiring one physical array to remain permanently read-only.

## Multiple code regions and measured functions

A task step may use editTarget to name a BEGIN/END source region. Sorting fragments
append within setup, renderer, and algorithm regions rather than one flat student block.
Its instructor verifier reconstructs every cumulative program from those regions. This
is task metadata; it does not constrain the eventual editable LaTeX composition.

traceExecution counts completed native events; its label-range overload filters actual
PCs inclusively at the first label and exclusively at the second. Snapshot fields are
instructions, takenBranches, notTakenBranches, and derived branches. Closing freezes the
counter; case cleanup unregisters it. Forward replay adds events and backstep does not
subtract them. Native jumps are distinct from branch decisions. Never use reference
instruction totals as a universal correctness requirement.

setRegisterToDataAddress and setRegisterToTextAddress resolve actual full-width labels
for function arguments and continuations. Independent function checks can start at the
public entry label and vary caller state without fixed data addresses or main setup.

## Native file import

The MARS plugin now reads existing format 1 student distributions through Tools →
Import MARS Lab. The archive has one package root whose name matches its manifest
ID. The native importer validates identity/version/title, supported MIPS64 Release
6 or MSA target and educational ABI, and the entry/assignment files. It does not
interpret the package's step/check metadata as native lesson tasks or execute its
Kotlin grading scripts. See [lab-import.md](lab-import.md).

Preview and installation use a bounded private snapshot. The importer checks actual
compressed and expanded bytes, filenames, file counts and extracted hashes. The
platform's extended ZIP extractor handles archive attributes; symlinks, unsafe
paths, case collisions and instructor-only paths are rejected. The published
file-only archive layout is supported; explicit directory entries are not accepted.
Limits are 32 MiB compressed, 16 MiB per file, 128 MiB expanded and 4,096 files.

Working copies are separated by package ID, content version and full archive
SHA-256. An identical reimport retains edited files. A new version or changed bytes
at the same version produce a separate copy. Importing is an explicit file-delivery
operation, not a source merge, a native task interpreter or a declaration that all
public and private grading contracts have been validated by this plugin.
