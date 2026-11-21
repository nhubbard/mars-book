# Main book and lab book revision pilots

These are directly edited native LaTeX drafts for the authorized restructuring goal.
The initial manuscript remains at the original `main.tex` and `includes/` baseline.

- `learning.tex` includes the first three main-book chapters: the complete console
  program, then integer representation through a byte inspector and a word-width
  experiment, followed by addresses and memory through a complete pixel program.
  The chapters use connected explanations and debugger reasoning.
- `advanced-pilots.tex` includes the PCM mixing chapter and H.263 local
  reconstruction excerpt. They assume their stated prior knowledge and use worked
  reasoning and independent exercises, without reproducing the old lab checkpoints.
- `lab-book.tex` includes the first three written lab supplements: predictions,
  observations, fault diagnosis and independent variations. The first unit has
  native interactive support; the second and third currently use ordinary Run and Debug.

## Reading the current proofs

For restructuring review, open these local build outputs:

| Reading | PDF |
| --- | --- |
| Main-book chapters 1–3 (16 pages) | [build/native/learning.pdf](../build/native/learning.pdf) |
| Lab supplements 1–3 (12 pages) | [build/native/lab-book.pdf](../build/native/lab-book.pdf) |
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

The advanced pilot PDF has ten pages: PCM mixing on pages 1–5 and the H.263
reconstruction excerpt on pages 6–10. These are review compositions, not the final
chapter numbering. The literal PCM group listing is executable with:

```sh
python3 tools/check_revision_pcm.py --java /path/to/java --cli /path/to/mars-cli.jar --output /new/report/directory
```

That check runs 128 complete groups, verifies the clipping-count accumulation and
compares all data bytes, including guards and exact aliases. It does not claim to
check an unwritten scalar tail. Current public/private verification records bind
the actual code, highlighter export and PDF; author and learner review are open.

The integrated first unit now uses concise navigation names and fits an undersized side-docked Learn pane when opening its source. Wider user layouts are preserved. The separate authoring driver records native text/button geometry and rejects horizontal clipping at the captured viewport. Human readability and learner reviews remain open; this UI correction does not finish the instructional system.

The first unit also provides confirmed reset and recovery through Tools. A saved recovery copy survives native close and a fresh IDE process. Recovery preserves source edits and restores quiz/hint history while requiring execution checks again; an empty reopened activity cannot replace the copy. The written lab explains the controls. Its current six-page proof has renewed automated accessibility verification; human review remains open.

The new integer chapter begins on page 6 of the main proof. Its written lab begins
on page 7 of the lab proof. See [integer-unit.md](integer-unit.md) for executable
verification, downloadable sources and the remaining native lesson work.

The addresses and memory chapter begins on page 12 of the main proof, and its
written lab begins on page 10 of the lab proof. See [memory-unit.md](memory-unit.md)
for the literal program, checks and remaining interactive work.
