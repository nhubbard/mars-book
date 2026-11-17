# Programming and computer architecture with MARS

Book source and student project packages for the IntelliJ MARS plugin. The text
assumes basic programming knowledge and teaches assembly details explicitly.

The first package, [Calculate twice an integer plus one](packages/001-console-calculation/assignment.md),
contains a barebones starter, 22 instructional microsteps, a multiple-choice question
and two hints at every step, 22 connective prose sections, ten teaching fixtures, two
vector diagrams, public executable checks, and five screenshot recipes.
Read [the first step](packages/001-console-calculation/steps/01.md) or build the combined
book draft. The second composition, [Paint one cell and change a byte](packages/002-single-pixel/assignment.md),
adds addresses, word and byte stores, RGB packing, byte order, and memory preservation.
It contains eleven steps, nine prose sections, six teaching fixtures, and three capture recipes.
The third composition, [Draw an eight-byte sprite and its mirror](packages/003-eight-byte-sprite/assignment.md),
adds fixed-width bit reversal, loop invariants, delayed branches, and two-dimensional
addressing. It has twenty steps, twelve prose sections, nine teaching fixtures, and
four capture recipes. The default-workload instruction comparison is verified against
the same output contract for an explicit loop and a native bitswap alternative. The full [35-project roadmap](docs/roadmap.md) preserves the progression
from console programs to algorithms, bounded assignments, vector kernels, and codecs.

## Use the materials

The source is Markdown and JSON. Python 3.10 or newer is sufficient for validation,
book assembly, packaging, and annotation rendering. Running assembly checks additionally
requires JDK 25 and a built MARS CLI JAR.

```sh
python3 tools/course.py validate
python3 -m unittest discover -s tools -p 'test_*.py'
python3 tools/course.py book
python3 tools/course.py build
python3 tools/course.py check --cli /path/to/mars-cli.jar --source /path/to/main.asm
```

The generated Markdown and student ZIP are under build/. The ZIP has a SHA-256 sidecar.
The unmodified starter exits cleanly but intentionally does not pass the assignment.
See [verification](docs/verification.md) for the tested package behavior.

## Author the book

The active [composition goal](docs/composition-goal.md) is full instructional chapters.
JSON and Markdown are temporary; straight LaTeX with input/include will become the
composition source once MacTeX and the author template are available for testing.

- [Writing style](docs/writing-style.md) applies the requested Wikipedia editorial guidance.
- [Editorial workflow](docs/editorial-workflow.md) keeps human interludes separate from generated assembly of the book.
- [Package format](docs/package-format.md) defines the structured lesson source.
- [Native LaTeX publication](docs/latex-publication.md) defines final typesetting and accessibility requirements.
- [Capture workflow](docs/capture-workflow.md) defines light-mode IDE captures and annotation generation.
- [Interactive design](docs/interactive-design.md) describes future guided and free-writing modes.
- [Publishing](docs/publishing.md) explains the public/student and private/instructor split.
- [Author a package](templates/package/README.md) provides the repeatable authoring checklist.

## Current delivery status

The first three text packages, starters, public checks, deterministic student archive builder,
Markdown book assembler, and annotation renderer are implemented. The private companion
repository contains reference and alternative implementations, quiz keys, private checks,
and teaching notes. This public repository has no dependency on private content to build.

IDE screenshot capture will be driven by a separate authoring plugin depending on MARS.
That plugin is not implemented yet, and
no lesson screenshots are included yet. The book uses labelled placeholders for the
five planned figures. Remote download, an in-plugin lesson runner, and instructor policy
controls are documented future capabilities. MacTeX template, frontmatter, and final PDF
production will be added by the author. Licensing must be selected before broader reuse;
this repository does not grant an additional license by implication.
