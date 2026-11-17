# Native LaTeX publication and accessibility

## Publication source

Markdown remains the initial authoring format. Once MacTeX and the author's template
are available, the publication source is native LaTeX. Text, headings, equations,
code listings, tables, captions, callout labels, and diagram text must remain typeset
content. Keep the semantic reading order and stable labels in the source.

The two first-lesson diagrams already have native TikZ source under fixtures/latex.
The SVG versions are draft previews for Markdown and are not the publication masters.
Their TikZ sources have not been compiled because the final TeX toolchain and template
are not available. The future composition adapter must map the other fixture roles
to native LaTeX environments and retain table-header information.

An IDE screenshot remains an original PNG image. Its annotation rectangles and labels
will be drawn in LaTeX/TikZ, with the legend, caption, and explanatory text outside the
image. Do not bake the final callout text into screenshot pixels or use an annotated
SVG/PDF as a substitute for native publication annotations. Keep the PNG and the
coordinate receipt so the template can control layout, type size, and placement.

## Accessibility requirements

The [LaTeX Tagging Project's current instructions](https://tagging-project.latex-project.org/documentation/usage-instructions)
recommend a current LaTeX release and LuaLaTeX for new documents. They describe enabling
tagging through DocumentMetadata before the document class, declaring figure alternatives,
identifying table header rows, and supplying structured mathematical information.
Select packages compatible with the template's tagging configuration.

Native LaTeX is the production requirement; accessibility is a property to verify in
the generated PDF. Check heading structure, reading order, table headers, text extraction,
mathematics, keyboard navigation, and figure descriptions. Test the actual document
with an accessibility checker and a screen reader. Record the toolchain and results.
A successful compilation alone is insufficient evidence.

## Authoring data to retain

Every figure needs a stable ID, caption, short alternative text, and a nearby explanation
of the state or relationship being shown. Tables need meaningful column names and header
rows. Code needs real text, a source-language designation, and an explanation of the
relevant register or memory effects. Equations need their mathematical structure rather
than a screenshot. Cross-references use stable labels instead of hard-coded page numbers.

A capture receipt must retain the original image dimensions and annotation rectangles.
The capture plan must supply the native figure's caption and alternatives. The separate
authoring plugin captures and verifies the IDE state; it does not flatten the entire
book page into an image.
