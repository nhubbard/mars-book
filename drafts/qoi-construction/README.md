# QOI cumulative construction

The editable native composition is `drafts/qoi.tex`. This directory supplies a safe
unfinished starter with 57 authored algorithm regions across 65 steps. Each step has
one exact observation recipe, one quiz and two progressive hints. Four light-theme IDE
figures are explicitly planned; no capture pixels or annotation-coordinate receipts
have been produced for them.

Run `python3 prepare.py s02-1 --source /path/to/working.asm --output /path/to/new.asm`.
Preparation changes fixture arenas and caller controls, preserves authored regions and
refuses an existing output. Keep the BEGIN/END markers, arena end labels, supplied routes
and scalar control declarations. The default caller is the bound helper; modes then
select validate, decode, encode, load, save and the connected pipeline.

A supplied after-label has its own nop instruction so a jump to a shared handler cannot
falsely look like completion of another body at the same address. Stop before that nop
at the recipe's named checkpoint. A direct function_return corresponds to the named
entry's caller return through main. The eight-byte result slot retains complete packed
dimensions as well as byte counts. The relevant interface determines its unit.

Development status 4 with zero count identifies an unfinished body. Earlier work may
already have produced a header prefix, changed private cache state or accepted file
bytes. The development return is outside completed statuses 0..3. Supplied invocation
frames retain all saved registers, fp and ra. Nested-call guards propagate unfinished
or error results; descriptor cleanup closes a nonnegative live handle once.

Preparing assembly does not provide file fixtures to an ordinary IDE launch. The grading
recipes declare their own files, permissions, chunks, injected errors and descriptors.
For an ordinary successful run, put the matching input.qoi in its configured working
directory. From this construction directory, `python3 ../qoi-fixtures/fixture.py input.qoi
--fixture trace` on one line creates the worked file at a fresh path. The helper defaults
to the sprite, which is the final observation's separate input. It refuses existing files.

Guided checkpoint states describe the illustrated register and frame choices. Completed
free-writing checks accept equivalent algorithms, frames and encodings with the same
published contracts and uniform-image size promises. Complete solutions and quiz keys
remain private. Seven public completed-interface check files are in checks/: bound, validate, decode,
encode, format, files and caller. Run each with the CLI grade command against the
working source; each file is standalone. These use the supplied data/caller labels and
function_return only. Intermediate checkpoint-check exports and separate distributions
remain next;
remote instructor progression/hint enforcement and actual automated IDE capture are
also subsequent work. The substantial prose rewrite remains deferred until all 35 drafts.

## Completed-interface feedback

Run `java -jar /path/to/cli.jar grade checks/validate.kts /path/to/working.asm` from
this directory, substituting the desired check. These are completed-interface tests;
a safe development return is expected to fail them. The grader supplies file fixtures
in memory and does not open host files. Keep the supplied caller/data labels and
function_return boundary for invocation. Internal labels and register choices are free.

| Check | Cases | Main observations |
| --- | ---: | --- |
| bound | 9 | Full packed dimension fields, supported domain and conservative capacity |
| validate | 17 | All chunk types, dimensions, descriptive flags and accepted metadata |
| decode | 11 | Exact color words including alpha, capacity and packed dimensions |
| encode | 42 | Independent decoding, flags, output bounds and uniform compression |
| format | 66 | Truncated commands, bad headers/markers, runs, INDEX adjacency and counts |
| files | 42 | Short transfers, denied opens, injected errors, EOF probes and cleanup |
| caller | 4 | Connected pipeline, actual output length, alpha/flags and error publication |

The host decoder is audited privately against 480 valid and 127 malformed fixtures.
Both private complete implementations pass these 191 cases at ordinary and wide
addresses (764 implementation executions). Twelve assembled fault controls are rejected,
including omitted RUN cache insertion, alpha loss and incorrect capacity signedness.
The six tested unfinished domains are rejected as incomplete. Public tests are feedback;
private grading exercises additional inputs, repeated calls and ownership boundaries.
