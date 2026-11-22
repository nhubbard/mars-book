# Public console-case data

The revised byte-inspector independent tasks declare public checks in
`revision/lessons/004-byte-inspector/independent/*.cases.json`. The existing files use
`formatVersion: 1` and `cases`. A version 1 case has exactly `name`, `input`,
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
Importing these files never executes their checks. Native lesson format 4 binds
an independent activity to an exported assembly source and case profile. Learn
opens the source and offers an inline Check this program control. The host
assembles current editor text and evaluates the immutable archived cases in
fresh sessions through the existing console grader. It does not run downloaded
Kotlin scripts. The adjacent scripts remain optional CLI checks of the same
public behavior. Instructor limits, source-bound saved results, retries and hints
apply independently to each activity and package digest.

## Exact final memory (case-profile version 2)

Case-profile version 2 retains all version 1 fields and bounds. It permits an
optional `memory` array on each case. Omitting the array or supplying an empty
array adds no memory check. Version 1 rejects this additional field. The native
lesson format remains version 4: its `caseProfile` points to either profile version.

```json
{
  "formatVersion": 2,
  "cases": [{
    "name": "initial word and neighboring guard",
    "input": "",
    "stdout": "",
    "instructionLimit": 20000,
    "memory": [{
      "address": "0x0000000010010000",
      "bytes": "99663300df9b5713"
    }]
  }]
}
```

Each range has exactly `address` and `bytes`. The address is a string with `0x`
and exactly 16 hexadecimal digits, with a leading digit from 0 through 7. It
represents a nonnegative full-width address; addresses above 4 GiB retain their
high bits. Each pair in `bytes` represents one byte in increasing address order.
Uppercase and lowercase hexadecimal digits are accepted. Whitespace, an empty
byte string, partial pairs, negative addresses and overflowing ranges are rejected.
There may be at most 64 nonoverlapping ranges and 1 MiB of expected bytes per case;
the existing 1 MiB document bound also applies. Adjacent ranges are allowed.

The example expects the little-endian words `0x00336699` and `0x13579bdf` at the
stated interface. A different byte order requires different expected bytes. The
interface addresses are part of the task contract; labels and scratch registers
remain the student's choice. Unmapped memory fails the check instead of being
silently treated as zero. Inspection does not execute simulated load instructions,
notify simulated memory observers, or add backstep entries.

The grader requires normal termination within the instruction limit, then exact
console output and the declared final bytes. It reports the first differing byte
in a range with its full address, expected value and actual value. A failed case
does not prevent later cases from running, and every case owns a fresh session.
The checker snapshots range definitions before starting any session.

These are final-state assertions. A guard that has its initial value at exit
might still have been overwritten and restored during execution. The checks do
not establish the absence of intermediate writes or require a particular load or
store instruction. Tasks needing those properties must add an explicit trace
contract rather than infer them from final bytes.

Generated CLI scripts use the same `expectBytes(address: Long, vararg expected: Int)`
check as native assessments. Large byte arrays are decoded at runtime, and long
text literals use runtime string chunks to stay within JVM constant-pool limits.
Short version 1 scripts retain their existing generated contents. These changes
add no downloaded code execution to the native lesson reader.
