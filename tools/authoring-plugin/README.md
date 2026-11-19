# MARS Book Authoring

This is a separate IntelliJ plugin depending on the packaged MARS plugin. It does not
add a trainer to the student plugin. The first utility captures actual language colors;
an opt-in Debug capture batch is now implemented. Interactive lesson progression and the complete book-figure layout workflow remain unfinished.

`BookHighlightExporter.capture` reads a committed, analyzed MIPS editor document. It uses
the registered IntelliJ editor highlighter for lexical colors and the document's additional
syntax markup for semantic colors. It resolves both against the named default light scheme
without changing the active editor or application scheme. Diagnostic, selection, breakpoint,
and caret layers are excluded. Public IntelliJ APIs supply all production behavior.

Offsets are Unicode code points so the Python renderer can reproduce the exact spans,
including comments containing supplementary characters. The export records the complete
source, SHA-256, attribute keys, RGB colors, and bold/italic attributes. The batch test adds
the IDE build, MARS plugin version, and exact plugin archive digest.

After editing native assembly listings:

```sh
python3 tools/book.py export-input
cd tools/authoring-plugin
./gradlew test buildPlugin \
  -PmarsPluginArchive=/absolute/path/to/plugin-1.0-SNAPSHOT.zip \
  -PhighlightInput="$PWD/../../build/highlight-input" \
  -PhighlightOutput="$PWD/../../book/highlighting/intellij-light.json"
```

Use JDK 25. Jupiter platform fixtures load the real dependency plugin and run IntelliJ's
highlighting before exporting each snippet. Tests also cover semantic roles, Unicode
coverage, scheme preservation, empty input, and rejection of another language. This command
runs tests and builds the authoring plugin; it does not launch the graphical IDE.
Before every graphical test IDE launch, run the actual Gradle tasks `clean cleanSandbox runIde`.
Use a dedicated test instance and a light theme for all future screenshot captures.

The Python package in `../pygments` registers a `mars-idea` lexer and `mars-idea-light`
style for minted. The lexer replays these exact exported spans. It does not contain a
second MIPS grammar, instruction catalog, symbol resolver, or independently chosen palette. The print style darkens only exported colors below
4.5:1 contrast on white; original RGB values remain in the receipt.
A changed listing fails until its export is refreshed. Code text remains native LaTeX
source; highlighting metadata cannot change the chapter's composition.

API contracts were checked in the read-only IntelliJ 2026.3 reference checkout:
EditorHighlighterFactory, HighlighterIterator, DocumentMarkupModel, RangeHighlighter,
HighlighterLayer, and EditorColorsManager. The implementation uses the public named-scheme
lookup rather than the internal default-scheme convenience method. The Gradle dependency
uses the supported [localPlugin API](https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-dependencies-extension.html).


## Automated Debug capture batches

`BookCaptureActivity` is installed only in this authoring plugin, which declares
MARS and IDE Features Trainer dependencies. An ordinary project launch does nothing:
the activity requires `mars.book.capture.plan` and an exact matching fixture project.
It does not add classes, extension points or a trainer dependency to MARS.

A local plan identifies the package/version, public and upstream commits, any dirty
upstream source digests, an absolute fixture root, a new output directory and figures.
Each figure supplies a relative source, its SHA-256, one unique checkpoint label,
viewport dimensions and expected rendered debugger rows. Each row assertion names
its callout and literal text fragments that must occur together in exactly one visible
row. Keep completed fixtures and plans in the private instructor workspace.

```sh
./gradlew test buildPlugin verifyPlugin -PmarsPluginArchive=/absolute/path/to/plugin.zip
./gradlew clean cleanSandbox runIde \
  -PmarsPluginArchive=/absolute/path/to/plugin.zip \
  -PbookCapturePlan=/absolute/private/path/plan.json \
  -PbookCaptureProject=/absolute/private/path/project \
  -PbookCaptureTrustFixture=true
```

The last property is optional and explicitly prepares only that generated fixture's
trusted location in the disposable authoring IDE configuration. It refuses an external
configuration directory. It neither changes the user's regular IDE configuration nor
disables project trust. Every graphical launch still executes the actual three tasks.

The driver selects IntelliJ Light and its light editor scheme, hides the project pane,
opens the source with the current nonblocking editor API, installs the native MARS
line breakpoint and launches its normal Debug configuration. Public daemon events
observe completed analysis passes; unrelated in-memory editors are ignored. The driver
waits for the expected source stop, visible values and stable rendered rows. It refuses
an edited fixture, ambiguous row match, clipped callout, changed capture window,
resumed session or existing output. Per-figure and batch deadlines bound failed waits;
owned sessions, breakpoints and analysis listeners are cleaned up on cancellation.

The raw PNG remains unannotated. Its receipt records native UI bounds, observed row
text, the source stop, actual locale and viewport, the IDE build, source provenance,
MARS archive digest and installed authoring JAR digest. Native Swing bounds resolve the callouts. Trainer highlights are cleared without drawing a temporary overlay during capture, so the PNG stays unannotated and the print annotations remain native LaTeX. The platform remote-driver
screenshot transport belongs to the external test process; this installed plugin uses
JDK screen capture restricted to its own IDE root instead of depending on that transport.

The current graphical fixture uses video recipe `s05-1` and fills only the first five
construction regions. The next pointer-advance region remains unfinished. Two normal
Debug launches stop at `video_group_counted`: one captures the integer count; the other
expands the actual MSA register group, `$w2` and its 32-bit lane row. Receipts identify
the common source/checkpoint and the separate views. The native print composition is now integrated as the verified step-five book
figure, with the original captures retained alongside editable LaTeX annotations.
No figure plan is marked verified solely because this smoke batch succeeds. Run-only
console capture, stdin handling, full chapter batches and
interactive quizzes/progression remain later work in this same separate plugin.

API contracts were inspected in the pinned read-only checkout: `ProjectActivity`,
`FileEditorManager.openFile(FileEditorOpenRequest)`, `DaemonCodeAnalyzer.DaemonListener`,
`ConfigurationType`, `ExecutionEnvironmentBuilder`, `XLineBreakpointType`,
`XBreakpointManager`, `XDebugSession`, `LafManager.installedThemes`,
`SimpleColoredComponent` and `LearningUiHighlightingManager`. Jupiter tests cover
fixture identities, checkpoint ambiguity, row assertions, coordinate bounds and both
native and in-memory file notifications. The real graphical trial separately exercises
the installed MARS debugger and native screen capture.


For complete dependency resolution, verify the authoring and local MARS ZIPs together.
The Gradle `verifyPlugin` task accepts one archive and can report the unpublished
MARS dependency as missing. The verifier's supported `@file` input accepts a text
file containing the two archive paths, one per line:

```sh
java -jar /path/to/verifier-cli-all.jar check-plugin @plugins-to-verify.txt \
  /path/to/IDE -verification-reports-dir /path/to/new-reports
```

Check the authoring plugin's own verdict and dependency report, not only the process
exit code. Both current local plugins are compatible with the pinned IDE; the authoring
plugin has seven experimental API uses and no internal or invalid override-only API
uses. Experimental public APIs are permitted on the pinned target. Metadata inspection
uses `PluginDetailsService`; the activity receives its own descriptor through
`PluginAware`. The paired input follows the official
[Plugin Verifier input contract](https://github.com/JetBrains/intellij-plugin-verifier#plugin-path-specification).


The current driver also waits for the native editor's visible “Analyzing” and
“Indexing” labels to disappear. It checks ordinary Swing labels rather than the
platform's internal analyzer-status classes. Notification balloons in the dedicated
capture instance are hidden through the public notification API without expiring the
notifications. The gate checks for their return before and after capture. The latest
private proof has a settled checkmark, a white register view and no overlay or balloon.
Twenty-eight Jupiter tests and the actual vector and three-view index batches cover the current
implementation. Full chapter capture batches remain incomplete. The step-five native composition
has passed color/grayscale review; independent accessibility review remains pending.


### Native debugger view selection

An optional `debuggerView` selects displayed tree paths relative to the debugger's
hidden root. It uses the existing native renderer, expansion and scrolling APIs:

```json
{
  "collapse": [["Integer registers"]],
  "expand": [["MSA vector registers"], ["MSA vector registers", "$w2"]],
  "reveal": ["MSA vector registers", "$w2", "32-bit integer lanes"]
}
```

Paths must identify one native tree and one exact displayed name at each level.
Missing asynchronous children are retried within the figure deadline; duplicate
matches and contradictory collapse/expand paths fail. A model object's diagnostic
`toString()` is not its displayed label. The driver reads the public cell renderer
rather than depending on the debugger's internal node classes. Revealed rows must be
fully visible before the normal value and stable-layout checks can pass.

A batch waits for each owned Debug process to terminate before launching the next
view. It also requests a new public daemon analysis pass for each view, including
when two figures use the same unchanged source. Progress files contain diagnostic
observed rows; only the final receipts and `batch-complete.json` attest completion.


### Native memory capture

An optional `memoryView` selects MARS's existing Memory tab for the owned Debug
process. Its `address` is an unsigned hexadecimal value resolved from the actual
prepared fixture; `minimumHeight` defaults to 400 pixels. Row assertions still match
one complete visible rendered row. An optional `column` of Address, Bytes, ASCII or
Words resolves the annotation rectangle to that cell. The row's source address and
bytes can therefore be checked while a print panel enlarges only its word cells.
The driver does not read simulator memory directly or import MARS implementation
classes. The current private fixture derives addresses and expected bytes through
the existing public grading API before launching the IDE.

`RunContentManager.findContentDescriptor` identifies the owned process. Its public
`RunnerLayoutUi` selects the existing MARS content, and the standard public Variables
content ID returns to the register view. This avoids the obsolete session UI and
run-content getters. Native bundle escaping is handled when matching the combined
Threads & Variables title used by another platform layout. Captures must fit entirely
inside their actual screen; requested frames are centered before capture.

The public `ToolWindow` interface has no setter for a docked pane's height. The
authoring plugin operates the existing public `ThreeComponentsSplitter` containing
the bottom tool window, retaining room for the source editor. It changes no platform
implementation and references no internal pane class. Table rows must be fully
visible horizontally and vertically; clipped values cannot pass their assertions.
The native layout and API contracts were inspected in the read-only pinned checkout,
and the exact installed plugin passed dependency-resolved Plugin Verifier checks.
The actual index batch captures directory rows, context rows and a return to the
register tab. Those views now form the native index-publication figure. Other book captures and
independent accessibility review remain pending.


### Native bitmap capture

A dedicated `bitmapView` panel selects the native Bitmap Display editor opened by
ordinary source-header settings. It supplies `columns`, `rows`, a hexadecimal
`address`, a relative `expectedRgb` file, its `rgbSha256`, and a callout `label`.
The file contains row-major RGB888 pixels from the independently checked fixture.
Bitmap panels have no debugger-row assertions or memory/tree navigation request;
other raw panels show the same checkpoint's register state.

The authoring plugin uses public `FileEditor` and Swing contracts. Native tooltip
hit testing locates the actual painted viewport without reproducing MARS layout
code. Every cell must expose the expected coordinates, address, and exact RGB value.
A native offscreen paint must also match every expected pixel exactly. The plugin
then captures its own IDE root and checks the corresponding screen pixels. Owned
bitmap tabs are closed between launches, including those opened during register
captures. No simulator state is changed by this driver.

`screenChannelTolerance` defaults to zero. The arm64 Mac record fixture explicitly
uses three after retained logical- and native-resolution diagnostic captures both
showed differences of at most 3/255 per channel. This applies only to the screen
photograph; native tooltip and rendered-pixel checks remain exact. Receipts record
the exact native pixel counts, screenshot error histogram, maximum channel error,
and sampled screenshot RGB digest. Larger differences reject the image and retain
it as failed diagnostic evidence. The PNG is never recolored to match the fixture.
This distinction avoids presenting a screen photograph as a numeric decoder oracle.

The ordinary MARS bitmap feature used by this fixture first appears at upstream
commit `76edb47b728f72d4669269d56f20320d6d50f4cd`: custom framebuffer addresses and
an optional `initial=true` source-header setting. Initialized pixels are captured
before execution without simulated memory-access events. The default remains
write capture. These are general display features; screenshot orchestration and
all book-specific classes remain solely in this dependent authoring plugin.
