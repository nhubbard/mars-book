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


## MarsBot square figures

Chapter 6 uses eight native Debug views from the separate authoring plugin. Four
plates explain the east corner, south corner, completed square and the real reverse
transition through the first movement-stop store. The unchanged reference uses a
16-unit side and 64 instructions per tick on the native 512-unit board. Verified
detail regions enlarge original screenshot pixels in LaTeX; the complete original
board image is retained, and no substitute plot is rendered for a screenshot.

The independent core oracle checks corner states, complete continuous trails,
registers, protected inputs and read-only coordinate feedback. Native UI checks
bind the selected execution, position, heading, pen, movement and clock fields.
Photographic checks require visible trail coverage and reject unexpected dark ink,
with the marker occlusion and outer two-pixel border exclusions recorded explicitly.

A reverse capture supplies `backstepFrom` and its final `checkpoint`. The driver
uses the public toolbar action list and `ActionManager.tryToExecute` to invoke the
actual Backstep Instruction action, waits for completion, and verifies the returned
source position. Receipts preserve both source boundaries. Student validation
rejects missing, invented or inconsistent reverse-action evidence. No manual action
group expansion or internal debugger implementation is used.

Forty-nine Jupiter tests and 377 public tooling tests accompany the capture path.
Rejected API and coordinate-translation attempts remain private diagnostic evidence.
Native figure tags, embedded fonts and unchanged original image pixels are checked,
then all color and grayscale proofs are reviewed. Captions, legends and annotations
remain directly editable LaTeX. The existing coordinate diagram remains figure
6.1, followed by the four IDE figures. Independent accessibility review remains open.


## Integer line figures

Chapter 7 uses eight native Debug views from the unchanged line reference: a
register/source view and a complete eight-by-eight bitmap view at each of four
boundaries. The first painted sample and the first completed update deliberately
have the same framebuffer. The third sample is selected by the native condition
`$s0 == 3`; the terminal scratch value is retained rather than described as a new
endpoint calculation.

The independent core oracle checks every framebuffer word, exact sparse word-store
prefixes, input/guard preservation and phase-specific error/register values. Native
tooltips bind all 64 cell coordinates, addresses and RGB values. Native paint and
photographic centers are exact, with zero screen-channel tolerance. Altered pixels,
metadata and conditions are rejected independently. External coordinate labels are
editable LaTeX annotations; the original screenshots are neither redrawn nor changed.

No authoring code or plugin binary changed. Exact source and archive hashes bind
the capture to the existing 49 passing Jupiter tests and compatible public API
report; this reuse is recorded explicitly. The new dedicated launch still runs
the actual clean cleanSandbox runIde tasks, and the rebuilt installed driver must
match that tested binary. Implementation remains in the separate authoring plugin
depending on MARS. Native tags, fonts, original embedded pixels, and complete color
and grayscale proofs are checked before publication. Independent accessibility
review remains open.


## Life generation figures

Chapter 8 uses twelve native Debug captures from an unchanged reference: register,
published-generation memory and full eight-by-eight bitmap views at each of four
boundaries. The first boundary is seed_copied. Three visits to generation_ready
use native conditions `$s3 == 0`, `$s3 == 1` and `$s3 == 2`. A condition is bound
to every part of each composite, so the repeated source line and the repeated
horizontal image cannot substitute for a generation identity.

The independent core oracle checks both complete padded boards, all 64 frame
words, guards and protected inputs at eight boundaries, including the completed
sweeps and pointer swaps between publications. Its exact ordered store prefixes
reject even temporary or same-value protected stores. The last prefix has 387
stores. Native tooltips, paint and photographic centers independently verify every
cell with zero screen-channel tolerance. Altered conditions and photos are rejected.

Four native LaTeX figures provide sixteen numbered explanations and native coordinate
labels outside unchanged original screenshots. The 36-page chapter preserves its
two existing diagrams. Student exports retain original images and state receipts
alongside editable native annotations. Minion Pro, Noto Sans Math and the actual
IntelliJ-derived listing highlighter remain unchanged. The capture implementation
remains in the separate authoring plugin depending on MARS. The actual clean
cleanSandbox runIde launch used the same exact driver bound to the retained 49-test
and public API evidence; these unchanged tests were reused, not rerun. Independent
accessibility review remains open after automated tag/font/pixel and print checks.


## Sorting exchange figures

Chapter 9 uses twelve actual Debug views: three raw parts for each of four native
figures. Values and publication metadata are separate memory views, with a full
eight-by-eight bitmap. The next instruction is the native leaf return at sort_ready.
Conditions select the initial publication (`$s6 == 0`), first completed exchange
(`$s6 == 1`) and final insertion publication (`$s2 == 8`). The last condition
distinguishes completion from the preceding seventeenth-exchange frame. Every
composite binds all three parts to one exact source hash, line and condition.

Insertion uses the unchanged reference. Selection changes only its documented
algorithm data word from zero to one. The independent model verifies all seventeen
insertion and five selection exchanges, both the pre-redraw and completed-frame
boundaries, all 64 frame words and ordered stores to the declared data image.
Array updates do not immediately change the published count or old frame. Native
tooltip coordinates/addresses/colors, native paint and photo centers are all checked
with zero screen-channel tolerance. Altered images and conditions are rejected.

Four native editable LaTeX figures have sixteen numbered explanations, signed-word
examples, native coordinate labels and semantic descriptions. The two existing
chapter diagrams remain native. Accepted one-page proofs and full chapter pages
are reviewed in color and grayscale. Rejected tall proofs are retained privately;
original PNG pixels are unchanged. The source/state receipts and original images
are exported with student-safe native annotations. Screenshot code remains only
in the separate authoring plugin depending on MARS; no plugin source changed. Its
previous 49-test/API evidence is reused with exact unchanged-source/binary hashes.
Independent accessibility review remains open after automated tag/font/pixel checks.


## Maze search and route figures

Chapter 10 uses twelve actual native Debug views for four figures. Small-room
views combine state/parent/distance memory, publication metadata and the complete
bitmap. The full-room figure instead shows all 64 queue-history bytes beside its
bitmap. Native conditions select initial phase zero, the first expansion, and
final phase two. The existing word(phase) evaluator read distinguishes the final
route publication from the preceding goal-expansion frame with equal saved
registers. No new instructions, observation labels or plugin mechanisms were added.

The small reference is unchanged. Full-room inputs change only walls and the goal.
Independent Python and core models agree on all 72 publications; exact stores,
complete byte arrays, immutable inputs, metadata and all 64 frame words are checked.
All native tooltip coordinates/addresses/colors and native paint values are exact.
The explicit three-level photographic allowance reflects the established macOS
compositor bound; actual maxima are zero/one, and retained-batch tests require at
most one. Altered classification photos and conditions are rejected independently.

Four editable native figures contain nineteen numbered explanations, semantic
descriptions, coordinate labels and numeric equivalents for color classifications.
They distinguish parent bytes from distances, route cells from edges, and queue
storage from the active frontier. One-page proofs are reviewed in color and
grayscale; actual chapter pages are inspected at print size. Tags, fonts and
original embedded pixels are checked. Callout numbers avoid address digits and crops exclude adjacent ASCII
fragments. Existing figure labels remain aliases. Screenshot code stays only in
the separate authoring plugin depending on MARS; no driver or MARS source changed.
Exact unchanged hashes bind reuse of prior 49-test/API evidence. Independent
accessibility review remains open.

### Matrix transpose: native cache geometry and counters

The chapter 11 batch contains twelve actual light-theme Debug panels for four figure
boundaries. Separate register, memory and cache panels share the same source hash,
checkpoint and condition. A native `$t1 == 4` condition selects the second `next_tile`
visit. All runs start cold and stop before the caller stores the count.

The separate authoring plugin observes native cache settings and counters through
public Swing controls. Exactly one rendered execution item must identify the capture's
owned process, even when the panel contains additional settings dropdowns. Disclosure
accessibility expands Cache Settings. Labeled controls verify placement, block count,
words per block, blocks per set and replacement policy; native text and progress controls
verify capacity, accesses, hits, misses and rounded percentage. Logging and connection
state are checked. The driver never applies settings, resets history or reads MARS models.

Independent host traversal/LRU models agree with every observed read/write address,
width and direction. Complete arrays and guards are checked. Four native plates retain
unchanged original pixels and twenty numbered explanations, including hexadecimal
register interpretation, target versus completed count, and debugger-line versus matrix-row
grouping. Every drawn frame is checked against the original receipt and final crop.
The PDF gate rejects heading/panel intersections; retained rejected proofs test it.
Color and grayscale reviews remain distinct from independent accessibility review.
The exact installed driver passed 63 Jupiter tests and the pinned compatibility check;
MARS contains no authoring classes and its unrelated edits are unchanged.

### Fixed-point ball: published motion and native register bases

The chapter 12 batch contains eight actual panels: raw-state memory and a complete
bitmap for each of floor, nearest and tick-five reflection, plus hexadecimal and
signed-decimal overflow registers. Native `$s5 == 0` or `$s5 == 5` conditions select
the exact complete publication before its supplied marker executes. Independent
folded-coordinate and quantization models agree with both full histories, all frame
words and exact ordered stores. The overflow path has no animation-data accesses.

An optional `valueBase` selects a dedicated register view through the existing native
Hexadecimal MARS Values toggle. The separate authoring plugin inspects supported
materialized DefaultActionGroup children and uses public ToggleAction state APIs.
It neither expands dynamic action groups nor calls override-only action methods.
After a format change it waits for rebuilt native views and matching displayed rows.
The native toggle is observed before/after capture and recorded in the receipt.
MARS implementation classes and simulator-state mutation are unnecessary.

The exact installed driver is bound to 67 passing Jupiter tests and compatible pinned
API verification. All three bitmap receipts check every logical cell against exact
native tooltip, paint and photograph values. Altered white-cell photos, tick conditions
and display-toggle observations are rejected by retained tests. Four native plates
retain original pixels, source labels, numeric/coordinate equivalents and twelve
explanations; complete color/grayscale proofs and frame/heading placement are checked.
Independent accessibility review remains open.

### Packet parser: logical input windows and output preservation

The accepted chapter 13 batch contains twelve actual native panels for three figures.
Each combines hexadecimal arguments/results, signed-decimal results, mapped input and
output-record bytes at parse_ready. Default source is unchanged; variants change only
available 12 to 11 or trailer 26 to 27. No instruction bodies change.

An independent model checks wire fields and checksum residues. The oracle checks all
324 data bytes before execution, after parser return and after exit. Reads stay within
the logical input prefix or the caller's available word. The exact store history is
four record words on success and empty on failure, followed only by caller result
stores. The doubleword store is represented by two four-byte notifications; complete
bytes are also checked. Silent mapped overreads and restored failed writes are rejected.

The authoring source/build identity is unchanged from the 67-test native display-base
batch. Exact hashes bind reuse of its tests/API reports, while the accepted graphical
run has its own real clean/cleanSandbox/runIde log and verification. Rejected preparation
attempts remain private and are not accepted screenshots. Three native editable plates
have fifteen explanations, complete descriptions, paired native bases and explicit
address/byte interpretations. Color/grayscale proofs, fonts, unchanged pixels and all
annotation frames are checked. Independent accessibility review remains open.

## Ring-buffer publication captures

Chapter 14 replays the unchanged reference to operation_ready with native conditional breakpoints at command indices 3, 8 and 10. Each plate combines source, native signed-decimal returns, descriptor bytes, ring bytes and output bytes. The descriptor pointer is a full doubleword; only the first eight ring bytes belong to this queue. The captions identify stale free bytes, explicit occupied count, prefix versus payload size and preservation on a short destination. An independent deque model verifies every complete data image and per-command reads/stores across all fourteen operations, including the subsequent successful retry.

The first register-view attempt is retained privately because revealing the distant operation-index register scrolled a return register off screen. The accepted plan keeps both returns visible; the conditional breakpoint and core oracle bind the index. All twelve accepted captures have before/after native receipts. The separate authoring plugin remains unchanged, with exact source/driver bindings to 67 Jupiter tests and compatible API verification. Every IDE launch runs the actual clean cleanSandbox runIde tasks. No authoring implementation is added to MARS.

## UTF-8 publication captures

Chapter 15 uses four native LaTeX plates assembled from twelve original IntelliJ Light panels. Conditional breakpoints select chunk indices 1 and 3 in the unchanged default reference, index 2 after changing only input byte five to hexadecimal 8f, and index 3 after changing only the final chunk length to zero. The source, signed-decimal return registers, eight state words and input bytes share the same verified publication boundary within each plate.

The independent oracle checks every one of 244 data bytes at initialization and all four call boundaries in each of the three fixtures. Scalar-interval feasibility rejects the earliest impossible prefix; Python strict decoding verifies completed scalars. Python incremental decoding alone can defer some invalid-prefix errors, so it is not used as the sole prefix oracle. Tests compare every one- and two-byte prefix with prefixes derived from all legal scalar encodings, test all 1,024 partitions of the example, and reject native overreads and restored terminal writes. Per-call read windows distinguish an inspected detecting byte from its rejected suffix, zero-length input and terminal refusal.

The separate authoring plugin remains unchanged and depends on MARS. Exact source, build/wrapper and installed JAR evidence bind reuse of its 67 passing Jupiter tests and compatible API report; no new Jupiter invocation is claimed. The accepted graphical batch independently runs the actual clean cleanSandbox runIde tasks and verifies before/after native state. Original pixels are unchanged in the PDF and all four color/grayscale proofs are reviewed.

The native build helper now uses LuaLaTeX's recorder file to fingerprint only reference files read or written by the current document. This fixes demonstrated false rebuilds caused by another document's changing auxiliary files in the shared output folder. Tests require included-chapter and contents changes to invalidate convergence while unrelated document outputs do not. Existing builds started before this fix retain the former behavior; future builds use the recorder.

## Clipped-blitter publication captures

Chapter 16 combines source, native signed returns, a complete sixteen-by-eight physical Bitmap view and destination memory into four native LaTeX plates. The eight-by-six logical framebuffer begins at physical column one and is marked by a native dashed outline. The leading guard and mapped row padding are distinct from active image cells. The display omits one final padding word and the trailing guard; every one of 2212 data bytes is independently checked at initialization, call entry, return and exit. Source-coordinate membership derives visible cells and exact full-word opaque stores. Native mutation tests reject clipped source reads, restored padding stores and RGB-masked transparency.

The original source annotation omitted initial=true. The native bitmap therefore displayed an unwritten initialized guard as black, and the first batch was rejected. The separate authoring plugin now reports native bitmap/editor mismatch diagnostics; 68 Jupiter tests pass. Public Swing contracts provide sizes, tooltips and painted pixels; no simulator internals are accessed by the capture driver. The supported source annotation option is enabled in student and both instructor programs, then all grading and capture models are rerun. Paired API verification uses an immutable copy of the authoring ZIP and the local MARS ZIP, resolves the actual dependency, and reports compatibility with nineteen experimental public API usages and no internal or override-only issues.

With initial pixels enabled, all 128 native cells match exactly. A separate rejected screenshot records macOS ICC conversion changing green/cyan samples by three channel levels. The accepted plan uses the already-supported screenshot-only tolerance of three; core memory, native tooltip values and native painted cells remain exact. Raw original PNGs and all rejection evidence are retained. Every IDE attempt runs the actual clean cleanSandbox runIde tasks. The automation remains only in the separate authoring plugin that depends on MARS.

## Expression evaluator publication captures

Chapter 17 replays four input-only fixtures to expression_ready. Each plate combines the source return marker, signed-decimal return registers, input bytes and caller result words. Sixteen native explanations distinguish successful values from error offsets, whitespace-inclusive byte positions, eager operation checks, missing group close versus zero denominator, positive-literal overflow before unary minus and caller publication after return. The conceptual call diagram remains identified as a diagram.

The independent host oracle uses Pratt binding powers and unbounded arithmetic with eager signed-word checks. All 224 data bytes are checked initially, at call entry, return and exit. Evaluator reads remain within the supplied slice and no caller-data stores occur before return; only the caller subsequently publishes status and the second result. Four model tests cover precedence, associativity, signed division, boundary/error priority, accepted whitespace and retained observations; two native mutations reject same-value input writes and suffix overreads.

The separate authoring driver is unchanged from the 68-test chapter 16 version. Exact source inventory, build/wrapper content and installed JAR identity bind reuse of its Jupiter and complete paired API evidence. The new twelve-panel batch independently runs the actual clean cleanSandbox runIde tasks and checks native state before and after each photograph. Original image pixels, exact annotation coordinates, embedded fonts, semantic descriptions and all four color/grayscale proofs are verified. No MARS implementation changes are introduced.

## Chapter 18 VM observations

The actual twenty-capture batch uses the separate authoring plugin and the real clean cleanSandbox runIde tasks. Four fixtures reach vm_ready after halt, budget exhaustion, capacity failure and invalid-target validation before a zero budget. Registers, bytecode, descriptor, physical operand stack and caller result words are captured separately. Native LaTeX composes six panels and six explanations per figure; source and register panels share one unchanged original photograph.

An independent decoded-record host interpreter verifies the public states with an operand list and unbounded checked arithmetic. All 448 caller bytes are checked initially, at call entry, return and exit. Exact selected-reference store history distinguishes legal stack work and descriptor publication from protected-data writes; selected image/descriptor/stack read bounds exclude padding and other arenas. Inactive cells are fixture-specific observations, not a free-writing requirement. Five host tests include all 65,536 two-byte images at zero budget; three native mutations reject same-value protected stores and suffix overreads.

Driver source inventory, build and wrapper match the retained 68-test and complete paired API evidence. The actual installed JAR has identical class/resource bytes and differs only in its manifest Build-JVM field; the exact comparison and both binaries are retained privately. No new Jupiter invocation or whole-JAR hash equality is claimed. MARS code and unrelated edits remain unchanged. All four color/grayscale proofs and native pixel, coordinate, font and tagging checks pass. Independent accessibility review remains open.

## Chapter 19 color-format observations

The actual eighteen-capture batch uses the separate authoring plugin and clean cleanSandbox runIde. Four figures stop at pack_ready, unpack_ready and preview_ready, including a fresh count-nine caller. Five- and six-panel native plates distinguish the pixel-count return, low-byte-first RGB565, expanded RGB triples, caller result publication and separately constructed display words. Original PNGs remain unchanged.

An independent bit-string model selects retained fields and repeats them to eight bits. Every one of 848 data/constant bytes is checked at initialization, pack entry/return, unpack entry/return, preview completion and exit. Logical function access bounds protect suffixes, guards, masks and other buffers; exact caller traces distinguish count publication from display creation. Four host tests cover all 65,536 RGB565 words and discarded-low-bit buckets, channel isolation and exact repetition, all caller counts and retained histories. Four native mutations reject same-value source/destination-suffix stores and both input suffix reads.

Every native bitmap cell and tooltip matches the independent model exactly. The previously documented three-level ICC tolerance applies only to photograph comparison; core values, native paint and guards have no tolerance. Driver sources, build/wrapper and class/resource bytes match retained 68-test and paired API evidence, and the installed binary matches the previous capture exactly. No new Jupiter invocation is claimed. All color/grayscale proofs and original pixel, coordinate, embedded-font and semantic-description checks pass. Independent accessibility review remains open.

## Chapter 20 PCM observations

The actual eighteen-capture batch runs through the separate authoring plugin with clean cleanSandbox runIde. Four plates stop at mix_ready, wrap_ready and comparison_ready, including a fresh nine-sample call. Twenty-two native explanations distinguish strict signed-halfword clipping, wrapping, current return registers, saved saturation counts and the vector/scalar tail boundary.

An independent host model uses unbounded sums, median saturation and signed modulo interpretation. Complete 820-byte images are verified initially, at mix entry/return, wrap entry/return, comparison and exit. Function traces constrain logical sources and destinations, while exact caller traces verify publication of processed, clipped and wrapped_count. Four host tests cover all 131,071 possible exact sums, all sample encodings, clipping masks and retained histories. Four native mutations reject same-value protected stores and suffix reads. Full grading separately exercises the permitted in-place aliases and all caller counts.

Driver source/build and identical class/resource evidence bind reuse of 68 Jupiter tests and complete paired API verification; no new Jupiter invocation is claimed. The installed JAR matches the prior captured binary exactly. All original PNG pixels, native coordinates, fonts, descriptions and color/grayscale proofs pass. The already-published native student chapter requires an illustration-only migration: four exact planned placeholders and two precise status passages are updated, while other native prose and all graded exercises/audio are compared byte-for-byte with the actual graded archive. Independent accessibility review remains open.

## Chapter 21 FIR observations

The separate authoring plugin captures 24 native light-mode panels after the actual clean cleanSandbox runIde launch. Five native LaTeX plates carry thirty explanations. Complete 488-byte histories distinguish the internal fir_round stop from filter_ready return and results_ready caller publication. The Fraction oracle rounds by quotient and remainder before saturation, independently of the assembly bias/shift implementation. A wide fixture shows scalar 2^33 and two 64-bit MSA lanes of 2^32; the native row is hexadecimal and the explanations supply the integer interpretation.

Logical reads are bounded to the selected input history, sixteen coefficient bytes and the selected output range. Metadata views include eight mapped zero bytes beyond the checked allocation solely as display padding. Four host tests and five native mutation tests pass. Driver source/build and identical class/resource bytes bind reuse of 68 Jupiter tests and complete paired API evidence; no new Jupiter invocation is claimed. All five color/grayscale proofs, original pixels, fonts, coordinates and descriptions pass. Named LaTeX saveboxes avoid collisions among details. The isolated bootstrap reproduces the initial fixtures and refuses edited native compositions. Student archive publication permits only five planned figures, two exact status corrections and capture assets; other native prose, graded exercises, quizzes and audio remain protected. Independent accessibility review remains open.

## Native comparison receipts and chapter 22

Format-two native-composite figures retain one source-bound state across their parts. Format-three native-comparison figures explicitly declare two or more named source states. Every raw part must match its declared state, every state must be used, and the parts must share IDE, plugin, source revision, locale and scale provenance. Both formats retain original PNG digests, readiness checks, callout crop bounds, native-source/export hashes and preview hashes. Six new tests cover accepted comparison states and rejected missing, unknown, incomplete, unused, changed or downgraded state declarations and runtime mismatches.

Chapter 22 captures nineteen native panels through the separate authoring plugin after the actual clean cleanSandbox runIde launch. Fifteen original panels enter four native figures with eighteen explanations. The paired scalar/tiled figure retains separate source states and common cold full-associative geometry. Complete 4508-byte snapshots and independent OrderedDict LRU histories match every notice address, width, direction and hit, including aligned frame traffic. The fixture freezes only the supplied caller's selection so its method read cannot enter the native cache; function instruction bodies remain unchanged. Four host tests and five native mutation tests pass. The two metadata rows include four mapped zero display bytes beyond the checked data allocation.

All four color/grayscale proofs, original pixels, fonts, coordinates and descriptions pass. A guarded bootstrap reproduces initial native fixtures in isolation and refuses a second invocation. Grading uses a fixed pre-illustration public checkout to prevent concurrent native edits from changing its source snapshot. Publication permits only four planned native figures, two exact status passages in sources.tex and capture assets; other graded bytes remain protected. The unchanged source/build/binary inventory binds reuse of 68 Jupiter tests and complete paired public API evidence. Independent accessibility review remains open.

## Chapter 23 delta codec observations

The actual clean cleanSandbox runIde batch verifies twenty-one native light-mode captures. Ten mathematical fixtures cover atomic capacity, an internal scan, malformed and reconstructed-range failures, and all six published measurement datasets. Complete 424-byte images are checked at initialization, entry, optional dec_terminal, return, publication and exit. Exact caller-data access order includes reads of candidates that cannot be committed and forbids same-value uncommitted writes and mapped suffix reads. Only the caller stores input_used, output_used and status after return.

Four host tests cover every signed-halfword round trip, signed-word endpoint transitions, all octets and fifth-byte gates, capacity cuts, scan/commit separation and retained histories. Five native mutation tests reject silent protected writes, suffix reads, noncanonical commitment and premature narrowing. All six byte-size and whole-function encoder measurements match the published arithmetic models; work remains separate from elapsed time.

The four native plates contain twenty-six explanations. Direct native LaTeX edits separate status from counts and omit unrelated argument rows; the full native receipts retain those assertions. The initial guarded bootstrap remains historical and refuses edited source. Failure and size comparisons declare separate fixture states while sharing runtime provenance. All four final color/grayscale proofs, original pixels, coordinates, fonts and tagged descriptions pass. Final student publication permits four planned figures, one exact status passage and capture assets; other graded bytes remain protected. The unchanged authoring source/build/class inventory binds reuse of 68 Jupiter tests and complete paired API evidence. Independent accessibility review remains open.

## Chapter 24 CRC observations and remaining integration

The actual clean cleanSandbox runIde batch verifies eighteen native light-mode captures. Fourteen original panels support three native plates and sixteen explanations. The missing-path preparation remains private and does not complete the planned paired failure figure. f04 stays explicitly planned.

Independent GF(2) long division in forward representation, with explicit bit reversal, checks complete 5860-byte images and exact caller-data access sequences. This differs from the illustrated reflected shift recurrence. The initializer writes exactly 256 words; logical table suffixes and the separate physical guard remain protected. Streaming uses actual buffer_capacity three, not a forced short-read policy, so ordinary host reads match the intended first three bytes. Full ordinary-host dumps match all 5860 final bytes for success and missing-path preparations. In-memory file receipts separately verify guest descriptor lifetime and closure.

Four host tests compare every octet at three seeds with zlib, all table-pair linearity identities, every split of the check message and retained histories. Six native mutations reject same-value input and suffix stores, mapped overreads, an omitted last entry, missing guest close and intermediate complementation. Fresh full grading also passes eight ordinary-host-file runs across both implementations. The first construction receipt omitted exported-check executions; construction was rerun with private output checks and all 31 generated checker hashes match the published checks.

All three native color/grayscale proofs, pixel identity, coordinates, fonts and tagged descriptions pass. Direct native edits improve the table layout and state how students reproduce the real-file capacity. The initial bootstrap refuses edited source. Exact source/build/class identity binds reuse of 68 Jupiter tests and complete paired API verification; no new Jupiter invocation is claimed.

The fourth comparison needs an IDE read error after four bytes and guest descriptor cleanup. The CLI fixture supports it, but the IDE creates its environment inside an internal process handler. The separate authoring plugin cannot replace it through a supported public API. A reviewable, unapplied public MARS customization proposal is retained privately and awaits the user's scope decision. Capture, tutor, fixture implementation and instructor material stay outside MARS. No manipulated register values or altered simulation instructions substitute for that missing capture.

## CRC failure comparison through the separate authoring plugin

The user authorized a general public MARS I/O hook. Its locally committed implementation passes eight targeted tests, all 895 plugin tests, required coverage gates and IntelliJ 2026.3 API verification. Capture, file fixtures, receipts and tutor behavior remain in the separate authoring plugin. The actual MARS archive contains no authoring classes or trainer dependency.

The separate plugin now passes 78 Jupiter tests. The first local API attempt could not resolve MARS; complete paired verification supplies immutable copies of both plugins and reports compatibility with nineteen experimental API usages and no internal or override-only problems. The native run uses the actual clean cleanSandbox runIde tasks. Installed driver JAR bytes exactly match the tested and paired-verified artifact.

Six additional native views compare missing-path status one with a read failure after four consumed bytes and status two. The fixture records open, three-byte and one-byte reads, a failed read, and guest close. Before capture the driver requires that exact history and no open descriptors. After stopping its process it records lifetime disposal separately, which cannot stand in for a guest close. Both full 5860-byte core oracles, four assembly mutation checks and four forged-receipt rejection tests pass. No simulated instruction or register value is changed by the driver.

The fourth native plate uses five originals and six explanations, with full-width memory details for printed legibility. All four CRC figures together use nineteen original screenshots and twenty-two native explanations. Color/grayscale proof inspection, unchanged pixel verification, coordinate checks, embedded fonts and semantic descriptions pass. The guarded initial bootstrap remains historical and refuses edited native source. Whole-chapter/manuscript integration and final archive receipts supply their own evidence; independent accessibility review remains open.

## SHA-256 native figure set

The actual clean cleanSandbox runIde batch verifies eighteen native light-theme views at schedule_done, the first compress_round_done, sha_fin_compress and main_done. Four native plates provide twenty-two explanations. Independent arithmetic derives IV and round constants from integer roots of primes, rotates through explicit bit permutations and evaluates Boolean functions per bit. Full 6320-byte images include backing suffixes, controls and constants. Exact access histories protect read-only source and caller-only status publication. The real 65-byte host file uses ordinary reads with capacity seven; no forced short-read policy is claimed. Its full final data dump and digest match independent host calculations. Five model tests and nine assembly mutation tests pass.

The separate authoring plugin and MARS dependency are unchanged from the 78-test CRC integration. Exact source inventory, build/wrapper comparisons, archive and installed JAR identity bind retained Jupiter and complete paired API evidence. No new Jupiter invocation is claimed. Original PNG pixels, native coordinates, embedded fonts and semantic descriptions pass verification; color and grayscale layouts are reviewed. The schedule callouts were shortened to fit the page without reducing image or type size. A corrected caption distinguishes compression from later digest serialization. The chapter's existing padding diagram is Figure 25.1, so the four independent proofs and previews use actual chapter numbers 25.2–25.5.

Fresh grading uses a detached public worktree and exclusive report locations. All 38 cumulative programs and 43 exported guided checks pass, together with 9,896 positive cases across 173 runs. Exact archive comparison protects all graded bytes while permitting only explicit figure placeholders, capture metadata and one factual status passage. The native bootstrap refuses edited TeX. Whole-chapter/manuscript and release verification retain separate receipts; independent accessibility review remains open.


## ChaCha20 native figure set

The actual clean cleanSandbox runIde batch verifies seventeen native light-theme views at after_qr_group4, the first after_scalar_call8, the first after_msa_select and main_done. Four native plates provide twenty-one explanations. Independent arithmetic uses explicit bit-permutation rotations and RFC8439 selections, tested against the published quarter-round, block and complete cipher vectors. Six model tests and twelve assembled fault controls pass. Complete 8564-byte initial/checkpoint/return/publication/exit images and exact public-data histories protect gaps, source, state and destination suffixes; stack-private access is outside that trace. All thirty-two lanes of the working and retained input vectors are checked.

The unchanged separate plugin reuses exact source/build/class/dependency-bound 78-test Jupiter and paired API evidence. No new Jupiter invocation is claimed. Initial incomplete batches showed that debugger group expansion persists across sessions; final recipes explicitly establish both CPU and vector group layouts. They are retained as presentation evidence. All authoring code remains separate from MARS.

Native review caught a memory-table header overlapping a register detail. The layout verifier now reconstructs full cropped-image bounds from native frames and original receipts, rejecting that initial plate. Corrected panels, semantic descriptions, fonts and unchanged screenshot pixels pass. Color and grayscale proofs are reviewed. Vector annotations were shortened to fit without reducing their type or vector image sizes. The existing lane-selection diagram remains Figure 26.3; the IDE figures are 26.1, 26.2, 26.4 and 26.5. The guarded bootstrap refuses edited native source. Full integration and exact illustrated archive comparison retain separate receipts; independent accessibility review remains open.


## RSA arithmetic native figure set

The actual clean cleanSandbox runIde batch verifies sixteen native views at the first after_product_partial, after_add_limbs, after_reduce_accumulate and after_power_publish. Four native plates provide twenty-three explanations. Independent unbounded integers supply complete product, modular sum, remainder and power results. Seven model tests and twelve assembly fault controls pass. The complete 776-byte caller image, all caller/workspace notices, declared private arithmetic values, protected workspace and restored full registers are checked. Active unused stack padding and saved return-address bytes are not claimed as a complete expected stack image. The first power schedule's interleaved copies were rejected; retained diagnostics identify the correct load-load-store-store ordering.

The separate plugin is unchanged. Exact source/build/class and dependency evidence binds reuse of 78 Jupiter tests and compatible paired API verification; no new Jupiter invocation is claimed. All sixteen real captures pass closed-batch verification, and MARS contains no authoring classes or trainer dependency. All four native proofs pass image, coordinate, full-crop overlap, font and tagged-description checks. Color/grayscale review clarifies private values crossing row boundaries and keeps the equation together. The guarded bootstrap refuses native edits. Fresh package grading and exact archive comparison retain separate provenance; independent accessibility review remains open.


## Base64 native figure set

The actual clean cleanSandbox runIde batch verifies seventeen views at after_enc_pair, validate_bad, after_enc12_right and after_dec16_compact. Four native plates provide twenty-two explanations. Contiguous bit strings compute sextets and octets independently of the assembly shifts; expected alphabet/permutation tables are derived from group positions. Six model tests include RFC 4648 examples, library comparisons, nonzero padding bits, all 64 sextets and 256 differential groups. Twelve assembly fault controls pass. Complete 16424-byte caller images and 280 constant bytes, all asserted vector byte lanes and exact caller/workspace/constant histories are checked. Vector scratch images, protected workspace and restored registers are checked separately; active saved-frame slots are not a complete expected-stack-image claim.

The separate driver is unchanged. Exact source/build/class and dependency evidence binds reuse of 78 Jupiter tests and paired public API verification; no new Jupiter invocation is claimed. All native proofs pass original-pixel, full-crop-bound, coordinate, font and semantic-description checks, with color/grayscale review. The decoder explanation was shortened without reducing type or screenshot size. Native TeX control names use letters, while stable public figure IDs remain base64-f01 through base64-f04. The guarded bootstrap refuses edited source. Fresh grading and exact illustrated archive comparison retain separate evidence; independent accessibility review remains open.


## Binary PPM native figure set

Four native figures use sixteen unchanged light-theme IDE captures and twenty explanations. Fixtures reconstruct the exact cumulative stages through s12, s19, s28 and s40 rather than executing the final reference at every stop. The decoder has stored its four words but retains its unfinished return; the encoder has written only the eleven-byte header; the saver has accepted twenty-three bytes but still retains the completion stub. Native screenshots use ordinary host writes and descriptor three. The independent core recipe separately forces three-byte short writes and descriptor nine, checks an owned descriptor at the checkpoint and verifies the guest close after continuation. The final native host file is checked separately; no per-launch native file history or guest-close claim is made.

Seven independent grammar, byte-order, bounds, reconstruction and Pillow tests pass. Twelve assembled capture faults are rejected. Complete caller images cover both paths, all arenas, controls and alignment gaps. Exact caller write schedules, logical read footprints, private metadata, protected workspace and restored saved registers are checked. Active saved-frame slots are outside the complete-image claim. Input provenance omitted from the first metadata receipt was explicitly added with its original retained; no source, oracle, plan or capture bytes changed. The unchanged separate authoring plugin reuses exact source/build/class/dependency-bound 78-test Jupiter and paired API evidence; no new Jupiter invocation is claimed.

Fresh grading against a frozen public revision passes 4,263 positive cases across 160 runs, 37 intermediate programs, 62 recipes, 38 exported guided checks and 42 quiz keys. Nine mutants and four unfinished entry points produce 181 expected failing cases. Guided exports regenerate into a private directory and match the frozen public bytes. All four executed verification scripts are retained and hash-bound to their reports. New optional output paths refuse existing reports and retain earlier evidence.

All four native proofs pass original-pixel, coordinate, full-image-bound, font and tagging checks and color/grayscale review. Review corrected an escaped apostrophe in one alternative description. Public IDs ppm-f01 through ppm-f04 remain unchanged; the existing logical fixture diagram is Figure 29.1. Native snapshots relocate only literal asset prefixes. The guarded bootstrap refuses edited source. Independent accessibility review and later substantive editing remain open.
