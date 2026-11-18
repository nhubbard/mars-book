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

The initial migration has 121 chapter assembly listings and one specimen listing, with
116 distinct sources. Preserve source bytes and indentation. Do not enable math escapes,
TeX escapes, or code ligatures that change the meaning of a listing. Code must remain
extractable text rather than an image.

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
