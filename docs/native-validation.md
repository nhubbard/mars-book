# Native publication validation: 17 November 2026

The first nine drafted chapters are editable LaTeX composed through main.tex and
includes/. The former Markdown book assembler has been removed. Structured exercise
packages remain separate. The source hashes and destinations from the one-time import
are recorded in book/migration-inventory.json. All 130 imported code/output blocks were
compared with their original package drafts and retained exactly.

## Tested toolchain and typography

- LuaHBTeX 1.24.0, TeX Live 2026; LaTeX2e 2026-11-01.
- minted 3.8.0; isolated latexminted 0.7.0 and Pygments 2.19.2.
- Minion Pro regular, italic, bold, and bold italic for prose.
- Noto Sans Math with an actual OpenType MATH table for equations.
- JetBrains Mono regular, italic, bold, and bold italic for code, with programming ligatures disabled.

Font inspection confirms embedded subsets with Unicode mappings. Minion font binaries
are read from the author's installed library and are absent from the repositories.
The supplied Minion Math 1.026 files failed the Unicode math test; the author selected
Noto Math. Noto Sans Mono was also trialled for code but lacked italic faces. JetBrains
Mono retains the actual editor's bold and italic styles without font substitution.

## Highlighting and tests

The separate authoring plugin uses the registered MARS lexer and IntelliJ semantic
markup, resolved against the default light scheme. The receipt identifies IDE build
IU-263.7307.200 and the exact MARS plugin archive SHA-256. It contains 116 distinct
sources for 121 chapter assembly blocks and one specimen block. Changes to a listing
fail validation until a new IntelliJ export is supplied.

The print style preserves compliant exported colors and darkens label blue from
4a86e8 to 4175cb and comment gray from 808080 to 767676. Their contrast against white
is approximately 4.53:1 and 4.54:1. The original values remain in the receipt.

Three Jupiter platform tests pass, including actual lexical/semantic roles, Unicode
coverage, unchanged application scheme, empty input, and rejection of another language.
The batch export also passed with all publication snippets. Thirteen package/native
composition/annotation tests and seven Pygments bridge tests pass. All nine exercise
packages validate and their student archives build. The migration changes no exercise
programs or grading contracts, so their existing behavioral verification remains applicable.

## Compiled PDF evidence and remaining review

The compiled manuscript has 238 pages. Its structure tree contains 171 table header
cells, 11 figures with alternatives, 247 mathematical structures, and 130 native code
blocks, including plain output. Literal assembly text extracts correctly. The two-page
specimen exercises all selected prose and code faces, literal operators, equations,
a headed table, a native diagram, and explanatory/exercise text.

Both PDFs compile with stable references and no missing glyphs, font substitutions,
content overflow, or deferred minted placeholders. The specimen and selected manuscript
pages were rendered and visually inspected. Build-local inspection.json records final
PDF digests, fonts, structure counts, and the checks performed.

This is a working manuscript. The PDF/UA-2 target and structure checks do not establish
conformance. Full-book visual and reading-order review, independent accessibility
validation, screen-reader testing, and author review remain. Actual light-theme IDE
screenshot capture and the interactive trainer are still pending. The current separate
plugin supplies highlighting export; it does not yet supply those capabilities.

## Maze chapter and layout revision

The native-first tenth chapter adds thirty microsteps, fourteen connective passages,
twelve teaching fixtures including two native diagrams, and seventeen assembly fragments.
The manuscript now has 279 pages; the maze chapter occupies physical pages 241–279.
All of those chapter pages were rendered and reviewed, with enlarged checks for the
repaired running headers and code starts. Short chapter marks keep running headers
inside the page. Code labels reserve space for their opening lines.

The current structure inspection finds 191 table header cells, thirteen figures with
alternatives, 274 mathematical structures, and 147 code/output structures. The book
has 138 highlighted assembly blocks, plus the specimen's separate block. All 116
previously exported distinct code sources remain unchanged; seventeen new sources
bring the highlighting receipt to 133. Selected fonts and Unicode mappings remain
embedded. No font substitutions, missing glyphs, overflow, or minted placeholders
remain in the compiled outputs.

Both maze implementations pass the public transitions, independent renderer cases,
416 private cases each, and measured workloads. Seventeen cumulative authoring programs
reconstruct the verified implementation. Seventeen warning-free faulty variants are
rejected, including a saved-register corruption after the observation label. The
renderer check inspects preservation at the observation boundary and again immediately
after the actual return, before executing the continuation. Student instructions use
public materials and source edits; the JSON fixture is not described as an implemented
IDE import feature. The final instructor report and archives pin the committed sources.

These checks do not replace the remaining full-book accessibility and author review,
and they do not claim that pending IDE screenshots have been captured.
