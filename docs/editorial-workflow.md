# Editorial workflow

## Responsibilities and voice

AI drafts the instructional structure, explanations, examples, exercises, quiz prompts,
feedback, starter projects, and checks. The human author supplies experience, motivation,
transitions, and editorial judgment. Assume competent introductory programmers: explain
an unfamiliar assembly operation, its operands, its state changes, and its constraints;
avoid repeatedly explaining ordinary editor use or elementary programming vocabulary.

## Human writing between instructional units

The chapter list in book/book.json references before and after interludes. The first
chapter reserves book/interludes/001-before.md and 001-after.md for the author. These
initial files contain only comments. The book assembler reads them without rewriting
them. Add more interludes between steps when a chapter needs them; extend the explicit
chapter sequence before changing the assembler rather than embedding human prose in a
regenerated output file.

The author may also edit AI-authored lesson files directly. The editable source is
always the Markdown in packages and book, never build/book.md. Git records actual edits.
book/editorial-log.json records material authorship and review transitions, rather than
attempting unreliable sentence-level AI detection. An AI revision of a human passage
requires an explicit editing request and should be recorded there with scope and review
status. Keep the original in Git history. Do not insert machine provenance into the
student-facing narrative unless the author wants a published disclosure.

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

Run package validation, tooling tests, behavioral checks, the book assembler, and archive
export. Review correctness and pedagogy independently. A passing program does not prove
that an explanation teaches the right mental model. Review figures at intended print
size and in grayscale. Use the verification record to distinguish completed checks from
planned work.

MacTeX installation, template, and frontmatter are pending author inputs. Keep semantic
headings, code fences, captions, and stable figure IDs in Markdown. Keep annotations as
SVG plus the original PNG and receipt. A later typesetting adapter can convert figures
to PDF and map semantic styles into the supplied template. Do not add LaTeX layout
commands to each lesson or assume a paper size or final publisher style now.
