# Source formatting

This is a source-readability pass. Book exposition and curriculum review are separate.
Native LaTeX includes remain the composition authority.

Kotlin production code, tests, Gradle scripts and grading scripts use KTFMT 0.65
with its default META style. Python tools use Black 26.10.1 with Python 3.12 syntax.
LaTeX uses latexindent 4.0.3 with the checked-in `.latexindent.yaml`, two-space
indentation and no prose reflow. Literal code environments are preserved exactly.

From this repository root:

```sh
python3 -m pip install black==26.10.1
python3 tools/format_sources.py --language kotlin
python3 tools/format_sources.py --language python
python3 tools/format_sources.py --language latex
```

Add `--apply` to format, and `--report /absolute/path/report.json` to retain the audit.
Kotlin formatting needs JDK 25 and Gradle dependency access for its first run;
subsequent runs reuse the pinned formatter and cached results. LaTeX formatting needs
latexindent and its Perl dependencies. On MacTeX, `File::HomeDir` and `YAML::Tiny`
may need installation in a local Perl library; set `PERL5LIB` to that library.
The formatter reports missing dependencies and never silently skips a failed file.

The same commands accept `--root /absolute/path/to/mars-book-instructor`.
In MARS itself, run `./gradlew spotlessApply spotlessCheck` from `mars5`.
The separate authoring plugin also runs Spotless through its Gradle `check` task.
Its normal test/build command still requires the packaged MARS dependency.

The four partial `.kts` templates under `tools/{cbor,png,video,wav}-feedback/`
retain imports until their generated cases are appended. Those imports may appear
unused in the fragment while being required by the complete script. The source-audit
tool names these four fragments explicitly; complete source files use normal KTFMT
import cleanup.

Grading generators format their output before writing it. Their reproducibility
tests compare the complete generated files, while structural tests allow formatter
whitespace and retain their exact numbers, names, budgets and rejection checks.

Directories beginning with `verification` contain frozen evidence. Their source
files are inventoried and hash-checked, but remain in the format actually used for
those recorded executions and captures. Current editable sources are formatted.
Build products and vendored dependencies are excluded by Git's ignore rules.

Formatting changes source hashes. Renew current source/export bindings and archive
indexes after verifying equivalence; retain original screenshots, raw capture receipts
and historical runtime hashes. A fresh source edition does not constitute a new IDE
capture or a new learner review. Private instructor distributions remain separate.
