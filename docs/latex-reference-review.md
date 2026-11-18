# Read-only LaTeX reference review

The author supplied ~/learn-kotlin-the-hard-way as a read-only reference while installing
MacTeX on 17 November 2026. Its files were read in place; all conversion and build work
belongs in mars-book, never in the reference checkout.

## Structure to adapt

The reference selects LuaLaTeX, a 12-point openany book class, separate included frontmatter
and exercises, fontspec serif/monospace fonts, named inline-code/file-code macros, and
semantic information/warning boxes. Its book-wide options and typography are centralized.
The MARS publication should retain that editable include-based approach and permit human
passages anywhere, with new MARS-specific frontmatter and content.

The source's historical font-file paths, Kotlin-specific aliases, prose, sales placeholders,
old copyright date, and commented personal/publication metadata are not MARS inputs.
The selected typography now uses installed Minion Pro, Noto Sans Math, and distribution-provided JetBrains Mono.
No build is run inside the reference directory, and no reference file is modified.

## Current tagging work

The reference has older tagging/PDF-standard setup commented out. Use the current
[LaTeX Tagging Project guidance](https://tagging-project.latex-project.org/documentation/usage-instructions)
for the installed engine rather than merely uncommenting that old configuration. The
current guidance puts DocumentMetadata before documentclass, enables tagging, identifies
table header rows, and supplies graphic alternatives. A compiled tag tree and reading-order
review are still required; setting a PDF/UA target is not proof of conformance.

## Migration gate and plan

MacTeX updates are complete. The native specimen and nine-chapter manuscript compile
with LuaLaTeX. The specimen has been rendered and inspected for the selected fonts,
actual IntelliJ-derived minted code, a headed table, math structure, and TikZ alternatives.
Full-document layout and accessibility review continue.

Convert prose once into ordinary TeX files, then edit those files directly. Compose main.tex
with input/include; do not regenerate edited book TeX from JSON or Markdown. Retire the old
Markdown book assembler from the publication path. Task metadata and grading distributions
can remain machine-readable without controlling the publication's order or layout.
Preserve author interludes and authorship records during the migration. Screenshots remain
real PNG assets with native captions and annotations; draft diagram SVGs are not publication
substitutes for the existing native TikZ sources.
