# MARS Book Authoring

This is a separate IntelliJ plugin depending on the packaged MARS plugin. It does not
add a trainer to the student plugin. The first utility captures actual language colors;
lesson execution and automated screenshot capture remain to be implemented.

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
second MIPS grammar, instruction catalog, symbol resolver, or independently chosen palette.
A changed listing fails until its export is refreshed. Code text remains native LaTeX
source; highlighting metadata cannot change the chapter's composition.

API contracts were checked in the read-only IntelliJ 2026.3 reference checkout:
EditorHighlighterFactory, HighlighterIterator, DocumentMarkupModel, RangeHighlighter,
HighlighterLayer, and EditorColorsManager. The implementation uses the public named-scheme
lookup rather than the internal default-scheme convenience method. The Gradle dependency
uses the supported [localPlugin API](https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-dependencies-extension.html).
