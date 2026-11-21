# Main book and lab book revision pilots

These are directly edited native LaTeX drafts for the authorized restructuring goal.
The initial manuscript remains at the original `main.tex` and `includes/` baseline.

- `learning.tex` includes the first main-book chapter: a connected explanation of
  the complete console program, the machine state it changes and debugger reasoning.
- `advanced-pilots.tex` includes the PCM mixing chapter and H.263 local
  reconstruction excerpt. They assume their stated prior knowledge and use worked
  reasoning and independent exercises, without reproducing the old lab checkpoints.
- `lab-book.tex` includes the written supplement: predictions, observations,
  misconception diagnosis and independent variations for the interactive lab.

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
restart preservation. Hints, instructor policies and full project reopen/resume
remain open. The progress service persists content-specific evidence. Its code and
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
