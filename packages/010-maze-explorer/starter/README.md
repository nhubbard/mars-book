# Maze explorer starter

Open main.asm with the MARS plugin. The unmodified file exits but does not implement
the search. Append each author fragment to its named BEGIN/END region in lesson order.
Several steps extend the same region. Keep the supplied exit and renderer return.

The tiny room contains four open cells and two equally short routes. The final required
program must also accept every valid 64-cell input described in assignment.md. Public
fixtures/full-room.json describes the larger stepping and capture workload.

Run only the independent renderer check after completing the renderer, while main's
search is still unfinished:

```sh
java -jar /absolute/path/to/mars-cli.jar grade \
  /absolute/path/to/checks/render.kts /absolute/path/to/your-main.asm
```

Once the complete search and final publication are implemented, run checks/public.kts
with the same command shape. From the book source checkout, course.py check with
--package 010-maze-explorer runs both public scripts. Use JDK 25 and the CLI revision
pinned in package.json. Student materials contain public checks; private cases and
instructor variants remain in the separate instructor distribution.
