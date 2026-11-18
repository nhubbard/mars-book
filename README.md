# Programming and computer architecture with MARS

Book source and student project packages for the IntelliJ MARS plugin. The text
assumes basic programming knowledge and teaches assembly details explicitly.

The first package, [Calculate twice an integer plus one](packages/001-console-calculation/assignment.md),
contains a barebones starter, 22 instructional microsteps, a multiple-choice question
and two hints at every step, 22 connective prose sections, ten teaching fixtures, two
vector diagrams, public executable checks, and five screenshot recipes.
Read [the first step](packages/001-console-calculation/steps/01.md) or compile the native LaTeX
book draft. The second composition, [Paint one cell and change a byte](packages/002-single-pixel/assignment.md),
adds addresses, word and byte stores, RGB packing, byte order, and memory preservation.
It contains eleven steps, nine prose sections, six teaching fixtures, and three capture recipes.
The third composition, [Draw an eight-byte sprite and its mirror](packages/003-eight-byte-sprite/assignment.md),
adds fixed-width bit reversal, loop invariants, delayed branches, and two-dimensional
addressing. It has twenty steps, twelve prose sections, nine teaching fixtures, and
four capture recipes. The default-workload instruction comparison is verified against
the same output contract for an explicit loop and a native bitswap alternative.
The fourth composition, [Inspect one word at two widths](packages/004-bit-inspector/assignment.md),
explains signed and unsigned interpretations, hexadecimal and binary notation, sign
and zero extension, word arithmetic, and full-width evidence. It contains sixteen
steps, twelve prose sections, nine teaching fixtures, and four planned IDE figures.
The fifth composition, [Count through hexadecimal glyphs on Digital Lab](packages/005-seven-segment-counter/assignment.md),
develops lookup tables, bounded windows, byte-port transactions, and execution-history
checks. It has seventeen steps, twelve prose sections, nine fixtures, and four planned
IDE figures.
The sixth composition, [Draw a square with coordinate feedback](packages/006-marsbot-square/assignment.md),
explains device commands, feedback polling, screen axes, instruction clocks, stopping
bounds, and pen-trail evidence. It has twenty steps, twelve prose sections, eight fixtures,
and four planned IDE figures.
The seventh composition, [Draw lines with an integer error accumulator](packages/007-bresenham-line/assignment.md),
develops canonical tie rules, paired coordinate decisions, error invariants, exact
framebuffer overlays, and scoped instruction-count comparisons. It has twenty-four
steps, thirteen prose sections, ten fixtures, and four planned IDE figures.
The eighth composition, [Evolve Life with a stable source generation](packages/008-life-generations/assignment.md),
explains synchronous updates, dead halos, alternating buffers, representation strides,
and public generation checkpoints. It has twenty-eight steps, thirteen prose sections,
ten fixtures, and four planned IDE figures.
The ninth composition, [Visualize insertion and selection sort](packages/009-sorting-visualizer/assignment.md),
explains sorted-prefix invariants, permutations, duplicate identities, a tested leaf
renderer, and native instruction/branch measurements. It has twenty-seven steps, thirteen
prose sections, ten fixtures, and four planned IDE figures.
The tenth composition, [Explore a maze with a breadth-first frontier](packages/010-maze-explorer/assignment.md),
teaches coordinate bounds, discovery-time marking, a bounded FIFO queue, predecessor
chains, and shortest-path reasoning. It has thirty microsteps, fourteen connective
passages, twelve teaching fixtures, two native diagrams, and four planned IDE captures.
Its checks accept different neighbor orders and shortest routes. The full [35-project roadmap](docs/roadmap.md) preserves the progression
from console programs to algorithms, bounded assignments, vector kernels, and codecs.

The eleventh composition, [Matrix transpose](packages/011-matrix-transpose/assignment.md),
adds 28 steps, thirteen verified code increments, five connected explanatory passages,
five teaching fixtures, a native tile diagram, and four planned IDE figures. Its
checks accept equivalent registers, stack usage, tile orders, and within-tile schedules
while verifying the stated matrix-access contract. Cache comparisons use the actual
shared core/IDE cache model and separate misses from completed instructions.

The twelfth composition, [Fixed-point ball](packages/012-fixed-point-ball/assignment.md),
adds thirty steps, sixteen verified code increments, seven connected explanatory passages,
six teaching fixtures, two native diagrams, and four planned IDE figures. It develops
signed scaled integers, overshoot-preserving reflection, exact contacts, display rounding,
coherent frame publication, and a separate word-overflow experiment. Its checks accept
equivalent branches, scratch registers, stack usage, and rendering store orders.

## Use the materials

The publication source is native LaTeX in main.tex and includes/. Exercise packages
retain Markdown and JSON for student guides and future guided lessons. Python 3.11 or
newer supports validation, packaging, and annotation tools. Running assembly checks additionally
requires JDK 25 and a built MARS CLI JAR.

```sh
python3 tools/course.py validate
python3 -m unittest discover -s tools -p 'test_*.py'
python3 tools/book.py validate
python3 tools/course.py build
python3 tools/course.py check --cli /path/to/mars-cli.jar --source /path/to/main.asm
```

Student ZIPs are under build/, each with a SHA-256 sidecar. Compile the book with
`python3 tools/book.py build` after following the [font and minted setup](docs/latex-publication.md).
The unmodified starter exits cleanly but intentionally does not pass the assignment.
See [verification](docs/verification.md) for the tested package behavior.

## Author the book

The active [composition goal](docs/composition-goal.md) is full instructional chapters.
The first fourteen chapters are now composed through ordinary LaTeX input/include files.
Edit them directly and insert author passages wherever they belong. Task metadata
does not constrain the publication.

- [Writing style](docs/writing-style.md) applies the requested Wikipedia editorial guidance.
- [Editorial workflow](docs/editorial-workflow.md) preserves human passages in the directly edited book.
- [Package format](docs/package-format.md) defines the structured lesson source.
- [Native LaTeX publication](docs/latex-publication.md) defines final typesetting and accessibility requirements.
- [Capture workflow](docs/capture-workflow.md) defines light-mode IDE captures and annotation generation.
- [Interactive design](docs/interactive-design.md) describes future guided and free-writing modes.
- [Publishing](docs/publishing.md) explains the public/student and private/instructor split.
- [Author a package](templates/package/README.md) provides the repeatable authoring checklist.

## Current delivery status

The first fourteen text packages, starters, public checks, deterministic student archive builder,
native LaTeX composition, IntelliJ-derived minted renderer, and annotation renderer are implemented. The private companion
repository contains reference and alternative implementations, quiz keys, private checks,
and teaching notes. This public repository has no dependency on private content to build.

IDE screenshot capture will be driven by a separate authoring plugin depending on MARS.
The separate plugin now exports actual IntelliJ language colors. Its screenshot capture
and lesson runner are still pending, and no lesson screenshots are included yet. The book uses labelled placeholders for the
planned figures in each chapter. Remote download, an in-plugin lesson runner, and instructor policy
controls are documented future capabilities. The tested template uses Minion Pro prose,
Noto Sans Math equations, and JetBrains Mono code. Frontmatter is editable; final author
review and PDF accessibility validation remain. Licensing must be selected before broader reuse;
this repository does not grant an additional license by implication.
