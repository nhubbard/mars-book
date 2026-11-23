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

## Input data and execution histories (case-profile version 3)

Version 3 preserves the version 1/2 fields and adds optional `initialMemory`,
`stores` and `reads` arrays. Older versions reject these additions. These are
bounded data declarations for the existing grader, not downloaded programs.
The native lesson format remains independent of the case-profile version.

`initialMemory` uses the same exact address/hexadecimal-byte ranges as `memory`.
It supplies input bytes to ordinary user data before any instruction executes.
Each range is validated before it changes data; surrounding bytes and the image's
byte order are preserved. Only addresses classified as user data by the session layout are admitted;
text, MMIO and kernel storage are not fixture inputs. Full addresses above 4 GiB retain their high bits. Setup is an external
stopped-session edit and does not enter execution histories or backstep history.
Overlapping setup ranges, more than 64 ranges or more than 1 MiB of setup bytes
per case are rejected. The existing whole-document bound also applies.

A `stores` entry has one of two exact shapes:

```json
{"kind":"none","address":"0x0000000010010000","length":32}
```

```json
{"kind":"bytes","address":"0x00000000ffff0000","length":65536,
 "port":"0x00000000ffff0010","bytes":"070706"}
```

The first forbids every execution store overlapping that region, including a
write subsequently restored. The second requires exactly the declared ordered
byte stores at its port, with no additional store in the traced region. Repeated
bytes remain separate transactions. A wider store fails even when its low byte
matches. An empty sequence requires zero stores in the region. The port must lie
inside the region. A trace region has one through 1048576 bytes and cannot overflow
its nonnegative 64-bit address. Expected store data is at most 65536 bytes per case.

A `reads` entry declares a traced region and a permitted subrange:

```json
{"address":"0x0000000010010000","length":20,
 "allowedAddress":"0x0000000010010000","allowedLength":16}
```

Every execution data read overlapping the traced region must fit wholly within
the permitted interval. Access widths and repeated reads are unrestricted within
that interval. `allowedLength: 0` permits no such reads. A crossing access retains
its complete address and width and therefore cannot hide a boundary violation.
Instruction fetch, external setup/inspection and backstep are not forward data
accesses. Writes have their separate store contracts. The checker does not claim
anything about accesses outside its declared observation regions.

There may be at most 64 combined store/read checks per case. Each observation has
a 65536-event capacity; overflow fails explicitly rather than accepting a truncated
history. Existing instruction, text, total-work and document bounds still apply.
Definitions are snapshotted before the first case machine is created. Each case
owns fresh simulator, I/O, data and trace state, and closes its observers on success,
failure or cancellation. The grader never uses the student's active Run/Debug
machine. A source-dependent mapping error is a failed case, not permission to write
to a different region.

Generated CLI scripts initialize bytes with `setBytes(address: Long, ...)`, attach
`traceMemoryStores`/`traceMemoryAccesses`, execute normally, then apply the same
`expectNone`, `expectByteStores` and `expectReadsWithin` assertions as native checks.
The setup writer uses the existing stopped-session word-edit policy after calculating
and validating each complete byte-range update; it adds no alternative memory model.

The preparatory range-counter contract is in
`revision/lessons/005-seven-segment-counter/independent/`. Its ten public cases
include empty and tail ranges, repeated transactions and alternate glyph data.
Its native lesson archive is still pending; the existing written lab is not yet
claimed as an interactive unit. Final display pixels alone do not prove its output
history, and an unchanged exit guard does not prove the absence of a write.


## Maze publications (case-profile version 6)

Version 6 retains the earlier optional fields and all version 4–5 work limits.
It adds an optional `maze` object with either of these exact shapes:

```json
{"kind":"search","walls":"<64 zero/one bytes as 128 hex digits>",
 "start":0,"goal":63,"fixtureIndex":0}
```

```json
{"kind":"renderer","fixture":0}
```

For a search, every byte pair must be `00` (open) or `01` (wall). Start and goal
are integral cell IDs from zero through 63 and must both be open. The fixture
index is an integer from zero through 65535; it varies the public guard values.
For a renderer, the fixture is an integer from zero through four, selecting the
five original renderer inputs. Unknown kinds/fields, missing fields, nonintegral
numbers, stringified integers and invalid endpoints are rejected. Earlier
profile versions reject `maze`. No downloaded predicate or executable class name
is accepted.

This is a task-specific semantic check of the documented 8×8 ABI in
`revision/labs/files/maze-search.asm` and the maze lab, rather than a general
replacement for memory or function checks. The program must publish initialization,
every completed dequeue/expansion, and the terminal result at `maze_ready`.
The checker validates each publication before executing its first instruction:
queue history and zero unused suffix, state, first-discovery parents, shortest
edge distances, path mask, metadata, guards and all 64 rendered pixels. The actual
submitted enqueue order determines the expected parent tree; any ordering of
exactly the newly discovered orthogonal neighbors is accepted. A separate host
breadth-first search verifies shortest distances and reachability. Coordinate
bounds forbid row wrap and diagonals. The goal is dequeued but not expanded;
start equal to goal produces a one-cell path of length zero.

The checker poisons output before execution, writes input as bytes independent
of image byte order, and forbids execution stores to walls, endpoints and guards,
including writes restored before a snapshot. The frame and its first guard have
the documented fixed addresses; other public labels may move. The final state
is checked again after normal exit. Counting executions at `maze_ready` also
rejects extra terminal publications. Instruction limits remain cumulative over
all stops; cancellation is checked at every inspected publication. Case cleanup
owns all observation subscriptions.

The independent renderer check starts at `render`, with a return continuation at
`done`. It verifies wall/path/state color priority and every output pixel at
`maze_ready`, then at `done` and after normal exit. All input arrays, metadata and
guards are read-only. Full-width `s0`–`s7` and the stack pointer must already be
restored at publication. Exactly one publication is required; helper labels,
scratch registers and decomposition remain the student's choice.

The eight original public search inputs and five renderer inputs are exported
as `revision/labs/checks/maze-{search,render}.cases.json`. Their generated `.kts`
files import `ConsoleMazeCheck` and invoke its public `verify(this)` method, so
native and generated assessments use the same checker. Original handwritten
project checks remain retained as historical evidence. This foundation alone
does not complete a downloadable maze lesson or its live IDE walkthrough.
