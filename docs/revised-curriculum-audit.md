# Revised curriculum audit

The native publications now include all 35 original project subjects. There are
27 paired main-book/lab-book chapters and ten additional companion assignments.
H.263 intentionally has two placements: prediction and reconstruction, followed
by stream publication. Functions and the stack has its own chapter without
claiming to be a separate original project.

Run the structural audit from the repository root:

```sh
python3 tools/audit_curriculum.py
```

`revision/project-locations.json` records where each original subject is taught
and the corresponding technical verification note. It is an audit index. The
ordinary includes in `revision/learning.tex` and `revision/lab-book.tex` determine
publication order and composition. The audit neither generates nor rewrites TeX.

The check reads both native include trees and verifies that:

- Main chapters and written lab chapters have the same order.
- Every original package ID has a revised location; unknown IDs and duplicate
  placements are rejected.
- Each indexed lab is actually included under its designated chapter.
- Every included companion assignment has an indexed project location.
- Each placement names an existing verification note.
- Each publication has unique labels and resolves its own literal references.
  Labels may recur in the other publication because these are separate books.
- Include cycles and paths outside the public repository are rejected.

The initial audit found 94 labels and 15 distinct referenced labels in the main
book, and 120 labels and 21 distinct referenced labels in the lab book. Neither
publication has duplicate labels or unresolved literal references. The checker
understands the literal include/reference forms used here; it is not a TeX
interpreter. Successful compilation remains a separate requirement.

## Original packages and revised exercises

A project subject's presence is not a claim that its old package can be started
at that point in the revised sequence. The original package manifests still
describe the original assignments, including their prerequisites and reading.
For example, `004-bit-inspector` loads a word from memory and originally follows
the sprite chapter. The revised second chapter instead uses a byte inspector and
an arithmetic-width experiment. Neither requires the sprite implementation.
Changing the original manifest's prerequisites to match the new chapter would
misrepresent what that original assignment requires.

Use the native revised chapter and its lab's named files when following the
revised books. The old distributions remain independently identified material.
A future revised lesson edition must bind its own source files, prerequisites,
questions and checks; importing an old package does not turn it into that lesson.

## What remains to review

Structural coverage does not establish that a learner can complete a chapter.
The [chapter prerequisite and assessment map](revised-learning-outcomes.md) now
identifies entry concepts and three assessable outcomes per chapter, linked to
existing written activities. It exposed a missing first-use explanation of logical
shifts, OR and XOR in Chapter 5; the native chapter now explains them. This map
requires author and learner review. It must not infer achieved learning from
file presence or package titles. Electives remain optional for the codec progression.

Native delivery now covers all ten companions and the twenty-seven-chapter
sequence through thirty-six downloadable units plus the separate built-in first
unit. Expressions and bytecode are the last companion deliveries. The current
student editions, explicit archive filenames and digests are indexed in
`revision/native-unit-index.json`; native TeX remains the composition authority.

The acceptance audit compares actual historical first/resume activity records
with current lesson identities, accounting explicitly for older driver naming,
retained unchanged-source credit and the byte inspector's three-process partial,
execution and completed-reopen sequence. FIR and H.263 stream distributions have
documented text-only changes after their IDE imports; current import tests and
unchanged execution/grading bytes are checked separately. That does not turn an
old IDE run into a new run of different archive bytes.

The [acceptance ledger](restructuring-acceptance.md) records the remaining
technical audit and the scope of its checks. Author walkthrough, representative
learner review and human accessibility review remain open. Successful structural,
execution and publication checks do not establish learning outcomes.

## Publication navigation

Both revised entries now include native title and usage pages, chapter contents,
a reference glossary and a subject index. The lab index also links directly to
all ten companion sections. Index entries deliberately identify their chapter or
companion section; they do not claim the exact paragraph where a term first appears.
These ordinary TeX includes remain directly editable. The historical first
structural counts above do not describe the current include and reference totals.
