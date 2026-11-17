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

The native IDE driver is future work. Its repeatable job is to:

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
   a receipt. Retain the original PNG without annotations for later typesetting.
7. Invoke the annotation renderer, compare the result with the previous reviewed
   image, and request editorial review when a visible state or layout changes.

Automation must use native supported test/UI facilities or an external UI driver.
Do not add a simulator-specific screenshot framework to the plugin without inspecting
current IntelliJ facilities and establishing a gap. No automatic IDE launch or capture
has occurred for this initial book package.

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
explicit placeholders in the book. A later print build can convert SVG to PDF when the
MacTeX template is available.
