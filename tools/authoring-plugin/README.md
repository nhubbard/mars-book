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
establish completed highlighting; unrelated in-memory editors are ignored. The driver
waits for the expected source stop, visible values and stable rendered rows. It refuses
an edited fixture, ambiguous row match, clipped callout, changed capture window,
resumed session or existing output. Per-figure and batch deadlines bound failed waits;
owned sessions, breakpoints and analysis listeners are cleaned up on cancellation.

The raw PNG remains unannotated. Its receipt records native UI bounds, observed row
text, the source stop, actual locale and viewport, the IDE build, source provenance,
MARS archive digest and installed authoring JAR digest. Features Trainer's public
highlight utility resolves the UI component; its temporary highlight is removed before
capture so the print annotations can remain native LaTeX. The platform remote-driver
screenshot transport belongs to the external test process; this installed plugin uses
JDK screen capture restricted to its own IDE root instead of depending on that transport.

The first verified graphical smoke fixture uses video recipe `s05-1` and checks the
actual changed-word count at `video_group_counted`. It demonstrates the automatic
launch/assert/capture/receipt path. It does **not** complete the planned vector figure:
MSA lane views, final print layout and native annotation rendering remain necessary.
No figure plan is marked verified solely because this smoke batch succeeds. Run-only
console capture, bitmap/memory view selection, stdin handling, full chapter batches and
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
