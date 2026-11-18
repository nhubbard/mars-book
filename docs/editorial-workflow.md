# Editorial workflow

## Responsibilities and voice

AI drafts the instructional structure, explanations, examples, exercises, quiz prompts,
feedback, starter projects, and checks. The human author supplies experience, motivation,
transitions, and editorial judgment. Assume competent introductory programmers: explain
an unfamiliar assembly operation, its operands, its state changes, and its constraints;
avoid repeatedly explaining ordinary editor use or elementary programming vocabulary.

## Writing between instructional segments

Edit main.tex and includes/ directly. Chapter masters include passages, steps, fixtures,
and figures with ordinary LaTeX commands. Place a new author file before, after, or
within any segment. The include order is the publication order; no layout schema or
JSON anchor is required. The reserved native interludes under includes/interludes/
remain unused writing space. Their historical Markdown counterparts contain comments,
not authored prose, and are retained as records.

Package Markdown and reading.json continue as downloadable exercise/trainer drafts.
They are no longer the publication composition source. Never overwrite edited native
TeX by regenerating it from those drafts. The retired composition receipt is archived
under book/legacy-draft/.

Git records edits to both human and AI-authored source. book/editorial-log.json records
material authorship and review transitions. An AI revision of a human passage requires
an explicit editing request and a log entry with its scope and review status. Keep the
original in Git history. Keep editorial records out of the student-facing narrative.
Follow the [writing style](writing-style.md) during prose review.

## Lesson unit

Each step explains one concept, asks for a concrete edit or observation, supplies a
prediction target, and asks a question about its meaning. Instructions that are not yet
executable should say so. Checkpoint runs in the first lesson retain the exit scaffold,
so partial programs terminate. Describe syscall selectors and arguments separately.
A pseudo-instruction can expand to several machine instructions; distinguish source
steps from executed instruction counts.

Write a bounded contract before the solution. Name the input domain, output bytes,
termination, permitted side effects, and resource limit. State which properties are
pedagogical observations and which are submission requirements. Free-writing correctness
must not depend on matching the reference source, scratch registers, or private labels.

## Review and typesetting

Run package validation, tooling tests, behavioral checks, native LaTeX validation and compilation, and archive
export. Review correctness and pedagogy independently. A passing program does not prove
that an explanation teaches the right mental model. Review figures at intended print
size and in grayscale. Use the verification record to distinguish completed checks from
planned work.

MacTeX and the native template are tested. The publication source is native LaTeX,
including diagrams and future screenshot annotations.
SVGs are Markdown previews only. Follow [the publication requirements](latex-publication.md)
for semantic structure and accessibility validation. Preserve raw captures and receipt
coordinates so labels can be typeset by the template.
