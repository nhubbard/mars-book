# Editorial workflow

## Responsibilities and voice

AI drafts the instructional structure, explanations, examples, exercises, quiz prompts,
feedback, starter projects, and checks. The human author supplies experience, motivation,
transitions, and editorial judgment. Assume competent introductory programmers: explain
an unfamiliar assembly operation, its operands, its state changes, and its constraints;
avoid repeatedly explaining ordinary editor use or elementary programming vocabulary.

## Writing between instructional segments

The package reading.json lists prose, steps, teaching fixtures, and figures in reading
order. It is the chapter's composition source. Prose is already written between the
instruction segments; it is not a request for the human author to fill all the gaps.
The author may edit these passages directly, add a new passage, or change the order.

book/book.json can insert a separate author passage before or after any stable segment
ID. An insertion names an anchor, a position, and a content path. For example:

```json
{
  "anchor": "s10",
  "position": "after",
  "content": "book/interludes/a-memory-from-teaching.md"
}
```

Create the named Markdown file before adding that insertion. Several passages can share
an anchor; their order follows the insertion list. The previous opening and closing
files remain as unused writing space. They do not constrain chapter composition.
The assembler reads source files without modifying them.

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

Run package validation, tooling tests, behavioral checks, the book assembler, and archive
export. Review correctness and pedagogy independently. A passing program does not prove
that an explanation teaches the right mental model. Review figures at intended print
size and in grayscale. Use the verification record to distinguish completed checks from
planned work.

MacTeX installation, template, and frontmatter are pending author inputs. The final
publication source will be native LaTeX, including diagrams and screenshot annotations.
SVGs are Markdown previews only. Follow [the publication requirements](latex-publication.md)
for semantic structure and accessibility validation. Preserve raw captures and receipt
coordinates so labels can be typeset by the template.
