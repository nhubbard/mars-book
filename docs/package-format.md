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
without a LaTeX template. The native LaTeX typesetter will map roles to environments and labels. SVGs are
Markdown previews; nativeLatex identifies existing TikZ sources where provided.
These fields describe content; they do not execute LaTeX or HTML.

The chapter's insertions list in book/book.json adds author passages before or after
any segment by anchor ID. This replaces the fixed before/after slots. Invalid anchors
fail the book build. Local image links are rebased when composing the output Markdown.

## Pixel address checks

The single-pixel chapter requires expectDataAddress(label, expectedLong), added in the
MARS revision pinned by that package. It verifies the destination agrees with the
configured display base, without prescribing a scratch register. An exact word by label
is insufficient if the label has moved away from that base. The check supports byte
addresses and addresses above 4 GiB. It does not verify an IDE frame or a source header.

This package representation is temporary book-authoring infrastructure. On migration,
straight LaTeX includes will control the publication; task metadata may remain for the
trainer without defining the book's composition.
