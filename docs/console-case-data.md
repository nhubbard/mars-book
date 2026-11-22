# Public console-case data

The revised byte-inspector independent tasks declare public checks in
`revision/lessons/004-byte-inspector/independent/*.cases.json`. Each file has
exactly `formatVersion: 1` and `cases`. A case has exactly `name`, `input`,
`stdout` and `instructionLimit`. The data describes observable behavior; it
does not prescribe labels, scratch registers or an exact instruction count.

`tools/console_cases.py` validates this data and emits a `.kts` file for the
existing MARS assignment DSL. Python does not emulate the student program.
The tool generates check code only; native LaTeX remains the publication's
authority and no book prose is regenerated. The generated files are checked
against the data by the public tooling tests. For example:

```sh
python3 tools/console_cases.py \
  revision/lessons/004-byte-inspector/independent/plus-one.cases.json \
  --output revision/lessons/004-byte-inspector/independent/plus-one.kts \
  --check
```

The reader requires strict UTF-8, rejects duplicate fields and nonstandard JSON
numeric constants, and limits the document to 1 MiB. It admits one through 256
uniquely named cases. Names are printable single-line text up to 128 characters;
input and output each admit at most 64 KiB of UTF-8, including empty strings.
An instruction limit is an exact integer from 1 through 1,000,000, and the sum
of declared case limits cannot exceed 10,000,000. Boolean and fractional limits
are invalid. The target examples use 20,000 per case as a termination guard.
The renderer escapes Kotlin interpolation, quotes, backslashes, control characters
and non-BMP text. Actual CLI compilation of those literals is qualified privately.
It refuses to overwrite a handwritten check file.

The private instructor repository contains separate reference and alternative
programs and exhaustive checks for the admitted byte range. The public archive
exports only the two incomplete starters, stated public cases, generated checks
and student explanations. Its digest is pinned by the private distribution.
Importing these files never executes their checks. In native lesson format 3,
Learn records the worked program's predictions and observed Run/Debug sequence;
these independent checks are run separately through the existing CLI grader.
Connecting independent assessment to the downloaded lesson host remains open.
