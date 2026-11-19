# Native LaTeX publication and accessibility

## Editable publication source

`main.tex` and its ordinary `includes/` files are the publication authority. Edit them
directly. Each chapter has a master include file and separate passages, microsteps,
worked fixtures, and figure sources. Insert an author's passage with `\input` anywhere
in that sequence; it needs no JSON anchor or layout schema. `includes/metadata.tex`
and `includes/frontmatter.tex` are editable MARS-specific frontmatter.

Markdown and `reading.json` remain the downloadable exercise/trainer drafts. They no
longer build the publication. `book/legacy-draft/book.json` is an archived composition
receipt. Do not regenerate edited publication TeX from those files. The one-time migration
inventory retains source hashes and native destinations; it is not a build input.
No file from the author's read-only Kotlin book is copied as prose or frontmatter.

## Fonts and compilation

The selected faces are **Minion Pro** for prose, **Noto Sans Math** for mathematics,
and **JetBrains Mono** for code. Minion Pro regular, italic, bold, and bold italic
are selected explicitly. MacTeX supplies the mono faces, including real italic and
bold italic faces. Programming ligatures are disabled. The initial Noto Sans Mono trial
was replaced because its missing italic face discarded IntelliJ styling. The build reads the locally
installed Minion Pro and Noto Sans Math OpenType files in `~/Library/Fonts`; set
`MARS_FONT_DIR` to another directory containing those files when necessary. No Minion
font binaries belong in either source repository. Minion Math 1.026 was tested locally:
its installed files lack an OpenType MATH table and fail the Unicode math test. The
author selected Noto Math instead. The installed Noto Sans Math passes that test.

Use MacTeX/TeX Live 2026 after its updates, LuaLaTeX, Python 3.11+, and Poppler. Create
an isolated minted helper environment once, or repeat the installation after modifying
the renderer package:

```sh
python3 -m venv build/latex-venv
build/latex-venv/bin/python -m pip install 'latexminted==0.7.0' 'Pygments==2.19.2' ./tools/pygments
python3 tools/book.py validate
python3 tools/book.py build --entry specimen.tex
python3 tools/book.py build
```

Output is `build/native/specimen.pdf` or `build/native/main.pdf`. The build uses ordinary
restricted shell escape for the pinned minted helper. It keeps TeX caches and auxiliary
files under `build/`, checks the selected math font, validates every assembly export,
invalidates highlighting caches when their inputs change, and repeats LuaLaTeX until
references stabilize. It rejects missing glyphs, content overflow, and deferred minted
placeholders. It never rewrites chapter or author files. The public CI validates the
native include graph and code exports without requiring the author's licensed fonts.

## IntelliJ highlighting and code text

Assembly blocks use native `minted` environments with the `mars-idea` lexer and the
`mars-idea-light` style. The [separate authoring plugin](../tools/authoring-plugin/README.md)
exports actual MARS lexer and semantic markup from IntelliJ platform fixtures. It records
the exact source hash, Unicode span offsets, color keys, light-scheme RGB values, font
attributes, IDE build, and MARS dependency digest. The Pygments bridge replays that export;
it has no independent assembly grammar or instruction list. A print-only contrast
normalization darkens the exported label blue and comment gray to at least 4.5:1 on
white. Other exported colors already meet that threshold and remain unchanged. Original
IDE values remain in the receipt; token roles and font styles are preserved. Literal output remains a
plain native text block. A changed assembly listing requires a fresh IntelliJ export.

The initial nine-chapter migration covered 121 assembly listings and one specimen listing.
The native-first maze chapter adds seventeen assembly listings; matrix transpose adds thirteen more, and fixed-point ball adds sixteen. Preserve source bytes and indentation. Do not enable math escapes,
TeX escapes, or code ligatures that change the meaning of a listing. Code must remain
extractable text rather than an image. Short chapter marks keep running headers within
the page; code labels reserve room for the opening lines of their listings.

## Native figures and accessibility

Diagram text, tables, equations, captions, and labels are native LaTeX/TikZ. Draft SVGs
remain package previews and are not compilation dependencies. Future IDE screenshots
must be original verified light-theme PNGs. Draw annotation rectangles and labels in
LaTeX/TikZ using the capture receipt; preserve the original pixels and dimensions.
Capture recipes are still labelled as pending figures in the manuscript.

The [LaTeX Tagging Project guidance](https://tagging-project.latex-project.org/documentation/usage-instructions)
informs the current LuaLaTeX configuration. `DocumentMetadata` precedes the document
class, enables tagging, declares English and PDF/UA-2 as a target, and requests structured
MathML. Figure alternatives and table header rows are explicit. Current tagged-table
support requires an actual table counter even for the imported captionless longtables;
the one-time Pandoc `none` sentinel was replaced accordingly.

The specimen was compiled, rendered, and checked for embedded fonts, headed table cells,
a figure alternative, mathematical structure, native code structure, and literal assembly
text extraction. These checks are evidence about specific features, **not PDF/UA
certification**. Full-book reading order, repeated table headers, links, visual layout,
screen-reader behavior, and an independent accessibility checker still require review.

## Reviewing a chapter independently

Matrix transpose has a separate native entry for isolated editorial and layout review:

```sh
python3 tools/book.py build --entry drafts/matrix-transpose.tex
```

It produces `build/native/matrix-transpose.pdf` with the same fonts, tagging, layout
checks, and ordinary includes as the main book. The chapter is also included in the
main manuscript. Its measured table uses the shared core/IDE cache model from upstream
commit `1f2de4ff48d5e70a76763edf489000fd6ca7fa3c`. Counts describe the measured private
reference and are not exact-count requirements for equivalent student solutions.
Short new assembly increments use minted's native `samepage` option with space reserved
for their label and complete listing. Quizzes reserve space for their choices. These
are editable LaTeX layout decisions, independent of exercise metadata. Actual IDE
captures and full publication accessibility review remain outstanding.

The complete fixed-point ball text draft can likewise be reviewed independently:

```sh
python3 tools/book.py build --entry drafts/fixed-point-ball.tex
```

It produces `build/native/fixed-point-ball.pdf` and is also included in the main book.
Its directly editable include sequence combines the existing numerical passages with
thirty steps, sixteen actual IntelliJ-highlighted listings, native coordinate diagrams,
and marked pending IDE figures. Font embedding, literal code extraction, headed tables,
mathematical structure, and figure alternatives are checked in the rendered output.
Full accessibility review, author review, and actual light-theme captures remain pending.

The binary packet parser chapter has a focused entry:

```sh
python3 tools/book.py build --entry drafts/binary-packet-parser.tex
```

It produces `build/native/binary-packet-parser.pdf` from the same native include
sequence used by the main manuscript. Its diagrams, headed tables, mathematics,
and measured comparison are native LaTeX. Run document builds sequentially: the
entries share chapter auxiliary files under `build/native`. Actual IDE screenshots
and independent accessibility certification remain pending.

The ring-buffer logger has a focused native entry:

```sh
python3 tools/book.py build --entry drafts/ring-buffer-logger.tex
```

It produces `build/native/ring-buffer-logger.pdf` using the same includes as the main
manuscript. The chapter supplies a native circular-slot diagram, headed tables for
descriptor fields, return values, command states and measured instruction counts,
and eighteen actual IntelliJ-highlighted assembly increments. Run native entries
sequentially because their auxiliary chapter files are shared. Actual screenshots,
author review, and independent accessibility validation remain pending.

The UTF-8 scanner can be reviewed independently:

```sh
python3 tools/book.py build --entry drafts/utf8-scanner.tex
```

It produces `build/native/utf8-scanner.pdf` using native includes, primary-standard
links, byte/state tables, a chunk-versus-encoding diagram, and twenty-two actual
IntelliJ-highlighted assembly increments. The mathematical example derives a scalar
from bit positions without requiring its glyph in the book font. The measured
branch/table comparison states both work and input scope. Run entries sequentially
because they share chapter auxiliary files. Four actual screenshots and final
author/independent accessibility reviews remain pending.

The PCM mixing chapter has a focused native entry:

```sh
python3 tools/book.py build --entry drafts/pcm-mixing.tex
```

It is also included in the main manuscript. Forty steps, thirty cumulative assembly
edits and two supplied excerpts use actual IntelliJ-derived minted highlighting.
The alias and mask diagrams, waveform, equations and tables are native LaTeX. The
student archive includes an exact native chapter snapshot alongside companion task
transcripts; the editable publication remains main.tex and includes. Run entries
sequentially because they share auxiliary chapter files. Four actual light-theme
captures, author review and independent accessibility validation remain pending.

The fixed-point FIR text can be reviewed separately:

```sh
python3 tools/book.py build --entry drafts/fixed-point-fir.tex
```

It is also included in main.tex. Forty-seven steps, thirty-six cumulative edits and
seven supplied excerpts use actual IntelliJ-derived minted highlighting. Native math,
window and waveform diagrams, exact impulse/rounding examples and whole-call tables
accompany the student package. Entries share auxiliary files and must build sequentially.
Five actual light-theme captures and independent accessibility validation remain pending.


The independent blocked-matrix chapter can be built with
`python3 tools/book.py build --entry drafts/blocked-matmul.tex`. It is directly edited
native LaTeX and is also included in the main manuscript as chapter twenty-two.

The matrix entry now includes fifty native teaching steps, forty-two cumulative edits
and two provided excerpts, for forty-four actual IntelliJ-highlighted listings. Keep
native prose as the composition authority. Structured draft recipes support verification
and future guided exercises; they do not regenerate the chapter.


Build the independent cumulative codec chapter with
`python3 tools/book.py build --entry drafts/delta-varint.tex`. It uses native equations,
tables, a described bit diagram and 55 listings exported through the actual IntelliJ
highlighter. Sixty steps interleave fifty edits with explanations, observations, quizzes
and hints. The chapter is included in main.tex as its twenty-third text package;
separate student/instructor archives retain exact native snapshots.
The four capture slots are plans; no screenshot is claimed for them.

Build the independent CRC-32 cumulative draft with
`python3 tools/book.py build --entry drafts/crc32.tex`. It has native state tables,
a PNG chunk-coverage figure and measured instruction comparisons. The two-row work
table identifies both header rows for tagging. Its file-grading controls require MARS
revision d41d95ec or later for explicit read-only fixture files; no IDE capture is
claimed. The native sequence now includes 34 steps, 24 edits and 31 actual
IntelliJ-highlighted listings. It is included in main.tex as the twenty-fourth text package, with separate student
and instructor archives. It remains a working draft pending actual IDE captures,
the author's deferred rewrite and independent accessibility validation.


Build the independent RSA arithmetic chapter with
`python3 tools/book.py build --entry drafts/rsa-arithmetic.tex`. Its native includes
remain directly editable. Its 51 assembly fragments use the actual IntelliJ light highlighter. The 42-step
cumulative sequence interleaves 35 edits with explanations, observations and quizzes;
native equations, headed tables and a described limb-layout figure support it.
The four IDE figure slots are plans without captured pixels. The chapter is also
included in main.tex as its twenty-seventh text package, with separate public/student
and private/instructor distributions.


Build the independent Base64 cumulative draft with
`python3 tools/book.py build --entry drafts/base64.tex`. Its directly edited native
includes cover scalar and MSA encoding/decoding with canonical padding and exact byte
footprints, woven through 43 steps and 37 authored regions. Fifty-seven assembly
fragments use actual IntelliJ highlighting, including supplied frames and restores.
Native equations, headed tables and a described 24-bit regrouping diagram remain editable.
The 65-page draft has 52 exact observation recipes, 43 quizzes, 86 progressive hints
and four explicitly planned IDE figures. It is included in main.tex as the twenty-eighth
text package, with separate student and instructor distributions. Actual IDE figures
remain subsequent work. Embedded fonts, tagged
structure and complete single-page listings are checked; independent accessibility
certification and student usability review remain pending.


Build the initial Binary PPM reading with
`python3 tools/book.py build --entry drafts/binary-ppm.tex`. The directly edited native
includes use Minion Pro, Noto Sans Math and JetBrains Mono, with a described native
pixel diagram and a headed byte-offset table. This reading foundation has no assembly
listings yet; it is not included in main.tex. The complete private image routines and their file/caller path are now checked at normal
and wide addresses, including independent decoding of actual saved image files. The
public reading defines interfaces and explains algorithms without importing instructor
implementation bodies. Cumulative construction and separate distributions remain next.


The same PPM entry now composes42 cumulative steps around the retained explanations.
Three actual-highlighted listings quote only the public starter’s supplied ABI and cleanup
scaffolding; complete instructor algorithm bodies remain private. Frames, checkpoint
recipes, quizzes and four unfulfilled capture plans are native includes. The current
standalone composition has 41 pages. Separate distributions and main.tex integration
remain pending.


The PPM distribution/integration step includes chapter029 in `main.tex`, bringing the
manuscript to 29 packaged chapters and 792 actual-IntelliJ-highlighted listings. The
standalone PPM composition remains 41 pages with three supplied scaffold listings and
one described native image diagram. It has 38 exported guided checks, six public
completion domains and an archived worked-image helper. File-error fixtures are explicit
grading fixtures; normal IDE Run/Debug provisioning and actual annotated captures remain
unfinished. The next roadmap chapter is QOI. The substantial prose rewrite remains
intentionally deferred until all35 chapter drafts exist.


Build the QOI reading foundation with `python3 tools/book.py build --entry drafts/qoi.tex`.
Its 20 directly edited native segments produce 12 pages with three headed tables, a
complete eight-pixel chunk trace and one described native sprite figure. No assembly
listing is present at this foundation stage; the future cumulative construction will
receive actual IntelliJ highlighting when its public code fragments are authored.
The current Minion Pro, Noto Sans Math and JetBrains Mono fonts remain embedded. Tagged
structure, table headers and figure descriptions are checked, without claiming external
PDF/UA certification. The main manuscript remains 29 packaged chapters until the QOI
MIPS companions, steps and distributions are ready for integration.


The verified QOI prototype step adds a twenty-first native reading segment with measured
whole-function counts and the shorter equivalent DIFF representation of the worked red
pixel. The current standalone reading is 13 pages, with four headed tables and one
native described sprite. Complete assembly companions remain private; cumulative
student construction, distributions and main-manuscript integration are still pending.
The full 35-chapter goal remains active, with the requested larger prose edit deferred.


The standalone QOI draft now weaves 65 construction and observation steps into the
existing explanatory passages. Build it with the same `drafts/qoi.tex` entry. Its four
new IDE figure callouts explicitly mark planned captures; they are not screenshots.
The new construction uses no minted listings. Existing Minion Pro text, Noto Sans Math
and JetBrains Mono settings and the IntelliJ-derived highlighting pipeline are retained.
The measured prototype instruction table describes the original prototypes, not the
new scaffold with its extra checkpoint instructions and supplied teaching routes.

The cumulative QOI PDF has 48 physical pages. Local inspection confirms embedded Minion
Pro, JetBrains Mono and Noto Sans Math fonts, a tagged PDF 2.0 structure and no reported
overfull boxes or missing characters. Rendered-page inspection covers all 48 pages.
These checks are not a PDF/UA certification or a substitute for assistive-technology
testing. The later author-requested substantive editing pass remains deferred.

The subsequent QOI completed-interface feedback passage adds one native include; the
standalone chapter now has 49 physical pages. The seven public checking domains remain
code assets, not generated book composition. Native prose explains invocation and the
scope of the observations. Earlier prose is retained for the later substantial edit.


QOI is included directly after Binary PPM in main.tex. Its package copies native chapter
fragments byte-for-byte for distribution; those copies do not control book composition.
Only working paths in the earlier native prose change to packages/030-qoi. The main
manuscript now has 30 packaged chapters and 792 highlighted listings. Four QOI IDE figures
remain planned with explicit pending-capture text. The publication still requires
independent accessibility review and the later author-requested prose edit.

The integrated build has 1286 physical pages; QOI occupies physical pages 1238..1286.
The standalone QOI entry remains 49 pages. Both PDFs have tagged structure and the
requested embedded fonts. All 49 integrated QOI pages were rendered and inspected;
this build reports no overfull boxes or missing characters. This is structural/layout
verification, not PDF/UA certification or assistive-technology testing.
