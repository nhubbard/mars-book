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
Bitmap/memory tool selection and full chapter batches remain unfinished.

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
Only `video-vector-count` is verified; five other video captures remain planned.
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
