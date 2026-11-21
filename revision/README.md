# Main book and lab book revision pilots

These are directly edited native LaTeX drafts for the authorized restructuring goal.
The initial manuscript remains at the original `main.tex` and `includes/` baseline.

- `learning.tex` includes the first main-book chapter: a connected explanation of
  the complete console program, the machine state it changes and debugger reasoning.
- `lab-book.tex` includes the written supplement: predictions, observations,
  misconception diagnosis and independent variations for the interactive lab.

Build with `python3 tools/book.py build --entry revision/learning.tex` and
`python3 tools/book.py build --entry revision/lab-book.tex`. The normal publication
lock serializes these builds. `export-input` retains original manuscript and
specimen snippets and adds the revision entries for actual IntelliJ highlighting.

The first interactive lab is not yet integrated into the main MARS plugin.
The new progress service is an initial implementation increment. Its code and
Jupiter verification are documented upstream in `docs/student-instruction-system.md`.
The full goal includes the actual integrated lesson, further editorial pilots,
all 35 projects and human review where available. These two small PDFs do not
complete that goal and have not been validated with students.
