# Automated IDE figure capture

## Required visual result

Use IntelliJ Light for the IDE and a light editor color scheme in a dedicated test
instance. Light mode is required for print legibility. Use the pinned IDE release,
MARS revision, English locale, reproducible font sizes, and the viewport in each recipe.
Show relevant code, console output, and register state at a readable size. Avoid personal
paths, unrelated projects, notifications, hover popups, and credentials. Review both the
color result and a grayscale rendering at final print size.

The first package defines five figures: starter structure, completed console output,
before addu, after addu, and after addiu. Each has a stable ID, target step, caption,
alt text, expected state, and semantic callout labels. Coordinates are deliberately
unset until a real capture resolves the native UI bounds.

## Capture driver contract

The driver will be a separate authoring plugin depending on MARS and, where used, the
IDE Features Trainer. It is not part of the published MARS plugin. An opt-in Debug batch now implements the launch, assertion and raw capture path in
`tools/authoring-plugin`. Full chapter figure layouts and native print annotations
remain unfinished. The complete workflow is to:

1. Build and launch the dedicated upstream IDE instance with the actual Gradle tasks
   clean cleanSandbox runIde before every launch. Read upstream AGENTS.md first.
2. Select IntelliJ Light and the matching light editor scheme, set viewport and font
   sizes, and open only the fixture project for the package version.
3. Use the public starter for the starter image or an instructor-provided completed
   fixture for execution images. Keep the fixture outside the public Git source.
4. Run with the specified input, or Debug and stop before the arithmetic instruction.
   Step to the exact recipe boundary, accounting for source versus machine steps.
5. Assert the selected source location, input fixture, console output or register
   state, and visible light theme. Wait for the UI's ready state rather than relying
   on a fixed delay. Failure prevents figure publication.
6. Capture a native PNG, resolve callout rectangles from UI element bounds, and save
   a receipt. Retain the original PNG without annotations and the resolved coordinate receipt.
   The publication build must typeset annotations natively in LaTeX.
7. Invoke the annotation renderer, compare the result with the previous reviewed
   image, and request editorial review when a visible state or layout changes.

The authoring plugin should use native supported lesson and test/UI facilities.
The outer launch script can select the capture batch and output location.
Do not add a simulator-specific screenshot framework to the plugin without inspecting
current IntelliJ facilities and establishing a gap. The first video Debug smoke batch has exercised automatic launch, state assertions
and capture. It does not complete the original console-package figures or the full
PNG/video figure plans.

## Receipt and annotation interface

The implemented renderer takes an actual PNG plus a JSON receipt. Required fields are
figure, theme, assertionsPassed, pngSha256, viewport, and callouts. Each callout contains
a label and box with x, y, width, and height in PNG pixel coordinates. The full capture
driver should additionally record package version, upstream commit, IDE build, fixture
hash, captured state, resolved assertions, locale, scale, and capture-driver revision.
The renderer checks the image digest, dimensions, light-theme attestation, and box bounds.
It cannot itself prove that the driver observed the right machine state or theme.

```sh
python3 tools/annotate.py raw.png receipt.json figures/f03-before-add.svg
```

It embeds the original image in SVG, adds numbered boxes, and places explanations in a
white legend margin. Numbering makes the annotations usable in grayscale. It escapes
labels and rejects out-of-bounds callouts. Unit tests use a synthetic image only to
exercise this renderer; that test image is never a lesson screenshot.

Mark a capture verified only after checking its state and reviewing the rendered result.
Add the receipt path and generated artifact to the figure plan. The package validator
requires both for a verified figure. Uncaptured figures remain planned and appear as
explicit placeholders in the book. The SVG is a Markdown preview. The print build must use the original PNG with native
LaTeX/TikZ callouts, captions, and alternatives; see latex-publication.md.


## Current implemented boundary

The separate plugin's README documents its opt-in local plan and exact clean launch
command. Only the generated private fixture is trusted in the dedicated authoring
configuration when that explicit option is selected. Captures require source and
rendered debugger assertions, completed highlighting and stable native bounds; source,
window and suspension checks run again after the screen capture. A failed run retains
diagnostics and never reports batch completion. A successful raw capture still needs
editorial, grayscale and print-size review and native LaTeX annotation composition.

The video `s05-1` smoke proves the real changed-word count and source stop. Its raw
image is retained in the private instructor workspace. It does not show all vector
lanes, is not substituted for the planned chapter figure, and has not changed any
package figure from planned to verified. The main MARS plugin contains no capture
implementation or authoring-plugin dependency.


Earlier smoke images exposed a native analysis badge after a daemon-completion
event. The current driver also checks visible editor labels and notification balloons,
and the latest raw proof has neither. It does not draw a transient trainer highlight
while taking the raw image. Native debugger tree expansion and scrolling now capture the MSA word-lane view.
Full chapter batches remain unfinished. Native memory table selection and cell callouts are implemented.

## Native annotation includes

`tools/annotate_tex.py` creates a native, editable figure include from the original
PNG and verified receipt. Supply a caption and an alternative description. An optional
crop uses original PNG pixel coordinates and must retain every callout. For the current
1280-by-900 driver proof, the measured crop is `(40, 170, 740, 670)`:

```sh
python3 tools/annotate_tex.py raw.png receipt.json figure.tex \
  --caption "The changed-word count is two before the next instruction." \
  --alt "The debugger is paused; numbered callouts identify the count and next instruction." \
  --crop 40 170 740 670
```

The exporter refuses an existing native include, so regeneration cannot overwrite
human composition edits. Drawn boxes and numbered markers are TikZ; the legend and
caption are ordinary native text. The unchanged full PNG remains the source even when
LaTeX trims its displayed viewport. Crops use measured natural image dimensions, so
PNG resolution metadata cannot shift the coordinate mapping. The optional image
reference must resolve to that same verified PNG. The SVG exporter remains a draft
preview, not a print dependency.

A one-page private proof is compiled with the shared book font/style includes. Its
PDF has one described Figure and two native legend items. Checks compare every
embedded image pixel with the original and extract the typeset labels. Color and
grayscale renders are reviewed separately. These checks do not certify PDF/UA
compliance or complete the vector-lane teaching figure; no chapter figure plan has
been marked verified from this pipeline proof.


## Paired vector/count capture

The separate authoring plugin now captures both the integer changed-word count and
the expanded MSA word lanes for video step five. The fixture leaves later regions
unfinished. The two images come from separate Debug launches of the same source,
input recipe and checkpoint; receipts record the selected tree view and source digest.
The XOR lanes are zero, `0x88`, zero and `0x10000`, with a changed-word count of two.
The native renderer supplies the displayed labels used for exact tree selection.
No simulator state or MARS debugger implementation is changed by the authoring driver.

The paired views now form the native step-five chapter figure in
`includes/chapters/034-lossless-video/fig-vector-count.tex`. Its original PNGs and
receipts are retained under `book/captures/034-lossless-video`, alongside the reviewed
preview and crop metadata. The native figure has been reviewed in color and grayscale.
All six video capture plans are now verified; PNG and earlier chapter capture plans remain incomplete.
Completed fixture source remains private. MARS has no dependency on the book or
authoring plugin.


## Native multi-view receipts

A version-two `native-composite` receipt distinguishes the rendered LaTeX preview
from its original raw captures. Each part points to an unchanged PNG and its own
native capture receipt. Panels identify a part, one asserted callout and an integer
pixel crop that must contain that entire callout. All parts must share source,
checkpoint, IDE and plugin provenance; their readiness checks must pass. Export
validation checks raw and preview digests, native annotation bytes, path containment
and crop bounds. The package exporter relocates image references without composing
or rewriting native book prose. A changed native composition requires renewed review
and updated approval metadata. This gate complements visual review; it cannot
independently establish the truth of an observed UI state or PDF/UA conformance.


The authoring driver also supports the existing native Memory tab. Addresses and
expected rows are derived from the actual private fixture through the public grader,
then asserted against rendered table text. Optional cell callouts preserve readable
print crops without weakening the complete-row assertions. The dedicated frame must
fit its physical display, and the bottom pane is sized through the public native
splitter before capture. The successful step-36 batch includes directory, context
and register views; capture success alone does not promote its chapter figure.


The step-36 index batch is now composed in the native chapter. Its four numbered
details use three unchanged raw screenshots. The full-page plate is kept before
the quiz and following lesson. Panels that span several asserted native cells list
`additionalCallouts`; validation rejects duplicate indices and a crop excluding
any of those cells. This enlarges the word values without treating hidden model
text as visible data. The independent raw-batch, PDF and package checks remain
separate evidence.


## Bitmap fidelity

The separate authoring plugin can select the native Bitmap Display editor and
assert every displayed address and RGB value against an independently checked
RGB888 fixture. Tooltip values and native painted pixels must match exactly.
The screen photograph has a separate recorded check: this Mac's retained logical-
and native-resolution captures differed by at most 3/255 per channel, so the record
fixture explicitly permits that measured bound. The default remains zero; larger
differences fail. No screenshot pixels are corrected or replaced. Exact decoder
state comes from the independent grader and native UI assertions, rather than
inference from the photograph's colors. See the authoring-plugin README for the
plan fields and the distinction between these checks.


The step-23 record plate now combines source, bitmap, context and return details
from three actual Debug launches of the same source/input/checkpoint. Exact native
checks cover all 4096 displayed pixel addresses and RGB values. The context view
shows retained capacity/period/suffix and published next sequence, kind and active
bytes. Native LaTeX explains the initialized-picture requirement and the one-bit
red change that cannot be judged from print. The screen-color histogram is retained
separately in the raw receipt. Color/grayscale review and unchanged embedded-image
checks pass; other chapter captures and independent accessibility review remain open.


## Completed video capture set

The final batch captures ten native views for independent seek, mandatory full-record
selection, and completed playback. Each source has its own independent pixel and
publication oracle. The playback oracle uses scripted time; the actual IDE uses the
real clock. Its context view observed eight accepted pictures, eight late intervals,
132 ms maximum lateness and logical duration 320 ms. Other launches may differ.
The independent verifier checks those fields rather than assuming zero lateness.

The darker encoder-source palette measured a photograph difference of at most 6/255
per channel at both logical and native resolution. Only that fixture uses six; the
other video fixtures retain three. Exact tooltip addresses/RGB and native painted
pixels remain mandatory, and raw PNGs are never recolored. The plan's optional photo
bound is capped at eight, with zero as its default. The native plates have been
reviewed in color and grayscale. Original screenshot pixels are retained in the PDFs.

Print review exposed oversized-float warnings that the former build gate did not
reject. The gate now rejects them. The earlier record plate and the two affected
new plates were resized and rebuilt; their numeric panels and native explanations
remain readable. Full chapter integration and the independent accessibility review
are distinct checks. The capture plugin remains separate from MARS.


## Completed PNG capture set

The PNG batch has thirteen native Debug views for its five figure plans. It uses
the normal caller return for EOF and the lesson's final publication boundary.
The first is a real `dla`, so receipt verification checks the first instruction
after the unique source label instead of assuming every checkpoint is a `nop`.
Core file-service checks establish descriptor cleanup; a buffer photograph does not.

Memory pages start on sixteen-byte boundaries. The collector descriptor begins
four bytes into its first displayed row, and the decoder context begins eight
bytes into its row. Native legends identify the leading words and fields spanning
rows. Review both actual addresses and field offsets before composing a crop.
The compact final-image recipe changes only a supplied stride constant, retains
student instruction text and decodes the original worked PNG. The two-column,
eight-row view includes three unused rows explicitly identified in its description.

Five native plates and their color/grayscale proofs are reviewed. Each retains
its original raw screenshots, source/recipe/checkpoint receipts and native editable
annotations. All five PNG and six video plans are verified; earlier chapter plans
and independent accessibility review remain open. The authoring plugin is separate
from MARS and depends on the published MARS plugin.


## H.263 capture and native composition

The separate authoring plugin has captured sixteen native views for the six planned
H.263 figures: SAD, selected motion, prediction and residuals, block reconstruction,
coefficient bits, and accepted clip progress. Each private worked caller first passes
a CLI checkpoint oracle. The complete clip oracle also passes independent FFmpeg
decoding; both files written by the actual IDE match that oracle byte for byte.

The batch uses the current MARS plugin without adding authoring classes to it. Its
receipts retain the exact source stop, visible values, IntelliJ Light theme, original
PNG digest, resolved callout bounds and plugin provenance. Twenty-eight Jupiter tests
and dependency-resolved Plugin Verifier checks accompany the batch. Raw overview
inspection found no transient overlay or unrelated project in the retained views.

The six public figure plans now retain reviewed native LaTeX compositions. Their
22 numbered details use measured crops of the unchanged raw images. Captions,
legends and alternative descriptions explain the worked memory and register state.
Color and grayscale proofs and embedded-image checks pass; independent accessibility
review and the earlier chapter figure plans remain open. The SAD plate uses two
details from one actual screenshot; the other plates combine separately captured
views of the same source and checkpoint.
The private evidence records intentional termination of the dedicated IDE after
batch completion; the resulting Gradle process exit is not reported as a passed build.


## Completed first-program figure set

The console calculation chapter now has five reviewed native figures. One unchanged
starter-editor capture shows the two insertion regions and supplied exit. A normal
Run capture uses native console typing and Enter handlers; observed input 20 produces
exact stdout ending in answer 41, empty stderr and exit zero. Three Debug captures
show input 20 and actual Step Over transitions to 40 and 41. The two batches retain
their separate pinned capture-driver revisions and source digests.

The five editable LaTeX includes have eleven numbered details, captions and alternative
descriptions. Original images, native bounds, exact text/value observations and reviewed
color/grayscale previews are retained under `book/captures/001-console-calculation`.
The exporter copies those assets and changes only image-path prefixes in native figure
exports. It rejects changed compositions without renewed review and preserves edited
exports. Student validation also binds the raw-receipt digest and distinguishes an idle
editor, completed native Run and paused Debug state. These checks do not certify PDF/UA
compliance or finish the earlier chapter figure backlog.


## Completed single-pixel figure set

The second chapter has three reviewed native LaTeX figures, with nine numbered
details from six unchanged native IDE images. The source stop, increasing-address
byte row and display/register views establish the word store, byte update and
completed load separately. All eight original capture views remain in the private
evidence batch; the six used in print are exported with their exact raw receipts.

The display uses normal write-driven updates. Only the upper-left cell receives a
store. The other three cells remain black even though the adjacent word has a data
initializer. The legends give the RGB fields numerically, identify the neighboring
word and the trailing padding, and distinguish the current instruction from the
instruction that has just executed. Color and grayscale proofs were inspected.

The existing reviewed-figure exporter now accepts a validated package identity,
retains edited native exports and changes only the image-path prefix during export.
Chapter 2 validation binds every raw receipt and unchanged PNG. The authoring plugin
remains a separate plugin depending on MARS; no authoring implementation is included in
MARS. These print and structure checks do not certify PDF/UA compliance. Independent
accessibility review and the remaining chapter figure backlog are still open.


## Completed sprite-row figure set

Chapter 3 has four reviewed native LaTeX plates, with thirteen numbered details
from nine unchanged IDE images. The stops show the first unsigned row load, the
completed bit reversal before its byte store, the first drawn row, and the completed
eight-row frame. Both bitmap views verify all 128 cells against independent Python
models, including the seven unprocessed black rows in the first-row view. Native
paint, tooltip and actual photograph checks pass with zero channel tolerance.

The legends distinguish a memory-table row from a display row, identify the source
and output byte ranges and both canaries, and explain the display hover outline.
Color and grayscale proofs and all 29 chapter pages were inspected. Explicit
paragraph boundaries around the new floats preserve paragraph and section tags
after a comprehension-question list. Figure descriptions, native captions and text
legends remain editable in the chapter includes; source images and resolved bounds
are exported with the student package. Capture implementation remains in the separate
authoring plugin depending on MARS. Independent accessibility review remains open.


## Completed bit-inspector figure set

Chapter 4 has four reviewed native LaTeX figures with thirteen numbered details
from six unchanged light-theme IDE images. Five Debug views establish sign/zero
extension and full-width stores for the all-one and 0x7fffffff samples. A completed
native Run establishes the exact four-line report without keyboard input. The
separate authoring plugin now supports input-free Run captures while retaining
strict prompt/native-input evidence for interactive examples. Student validation
rejects an invented keyboard event on an input-free Run.

The first capture recipe left the fourth register below the viewport; it remains
private rejected evidence. The accepted batch reveals the last required register,
bringing the adjacent group into view. All actual state and source assertions are
unchanged. The independent verifier binds the full process output, successful exit,
console text and UTF-16 callout ranges as well as the Debug source/value observations.
Thirty-seven Jupiter tests and public API verification accompany the driver.

The editable captions and legends explain equal low words with different high
halves, complete eight-byte fields, the limits of word-formatting services and the
word/doubleword sum at bit 31. Color/grayscale proof review and native font, tag and
original-pixel checks pass. Capture implementation remains in the separate plugin
depending on MARS. Independent accessibility review remains open.


## Seven-segment counter figures

Chapter 5 uses nine native Debug views from the separate authoring plugin. The
four plates show the boundary before the first byte store, the first completed
zero glyph, the eleventh displayed boundary for A, and the completed default range
retaining F. A native conditional breakpoint selects `$s2 == 10`; the driver reads
that condition back, and composite validation binds it across both views.

Digital Lab checks the selected execution, both mask descriptions, sixteen painted
segment states including decimal points, the photographed states, disabled timer
and unpressed keypad. The independent core oracle checks the exact sixteen byte
transactions to the fixed right port and rejects writes to the protected table
and inputs, including writes later restored. Color similarity is used only to
classify each segment as on or off; the checks do not claim exact RGB equality.

The accepted capture batch and rejected viewport/selector diagnostic attempts are
retained privately. Forty-two Jupiter tests and public API verification accompany
the driver. Native annotations, descriptions and numbered legends are editable
LaTeX; original PNGs and state receipts are included with the student package.
Proofs are checked for embedded fonts, tagged descriptions, original image pixels
and single-page layout, then inspected in color and grayscale. The existing
captioned segment diagram remains figure 5.1, followed by the four IDE figures.
Independent accessibility review remains open.
