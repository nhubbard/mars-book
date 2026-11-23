# Compact integer encodings

Chapter 17 revises original project `023-delta-varint`. The main proof has
111 pages, with this chapter on 105–111; the lab proof has 100 pages, with the
new unit on 96–100. The advanced PCM/H.263 excerpt remains unchanged. The main composition
explains exact full-width differences, a 33-bit signed mapping, base-128 byte
groups, canonical spelling, candidate versus committed state, error precedence,
range validation and representation/work comparisons. The written lab uses
predictions, actual native Debug observations, deliberate faults and independent
read-only validation. Native LaTeX remains the publication authority.

The complete `labs/files/delta-codec.asm` uses compact branches and a retained
80-byte private frame. The ordinary caller uses sixteen-byte stack alignment;
functions also honor the original package's eight-byte entry alignment contract.
Only complete accepted values change the destination. Each call resets its
predecessor to zero. Splitting independent blocks is therefore different from
concatenating their bytes under one continuing predecessor.

The worked codec and an alternative using threshold lengths, multiply-accumulated
payloads and conditional inverse mapping each pass 1588 public, 30 caller,
4905 private and four same-session composition cases. The same suites pass at
addresses above 32 bits. Both algorithms also pass all 1588 public cases
with an incoming stack aligned to eight bytes but not sixteen. Private domains include 441 selected endpoint/width
boundary pairs under nine capacities, all 256 one-byte inputs under three
capacities, selected fifth bytes with three prefixes/four capacities, and four
redundant-zero lengths under three capacities. They are not all signed reading
sequences or arbitrary byte strings.

`labs/files/delta-scan.asm` is the independent failing starter. The public
`labs/checks/delta-scan.kts` has 297 cases: eight sequences under all 33 limits,
eight rejected-tail families under four limits, and the ordinary caller. Two
scanner algorithms pass public and 8865 expanded cases; a wide-address variant
passes the expanded set. The expanded domain covers every one-byte input under
three limits and eight correlated offsets, every output count under 32 offsets,
every fifth byte after a valid prefix under four limits, selected redundant
spellings, 512 deterministic byte strings and a repeated-call sequence. These
are explicit domains, not an exhaustive claim over streams or offset products.
The starter fails all 297 cases.

Eighteen distinct semantic faults are rejected: twelve codec and six scanner
faults. They include narrowed differences/mapping/reconstruction, logical sign
masks, skipped reservation, invalid width/termination/minimality, early commit,
retained state, wrong precedence and transient caller writes. Nine positive
written probes verify exact default bytes/publication, capacity-seven untouched
suffix, minus-one mapping, extreme transitions, scan versus committed counters,
syntax versus range, and capacity preventing inspection of malformed tail bytes.

The repeatable public `tools/measure_revision_delta.py` verifies five complete
32-reading datasets and decodes the actual encoder bytes in the same session.
It compares fixed words, independent signed varints and mapped deltas. Steady
values use 128/64/33 bytes; a spike uses 128/34/36, and alternating endpoints
128/160/160. Exact entry-through-return completed instruction counts include
frame operations and validation. They exclude caller setup/publication, assembly,
startup, host preparation/inspection, host time and hardware cycles. Sizes exclude
framing; no host or hardware speedup is claimed.

Three actual light-theme Debug captures show the unused capacity after a five-
byte prefix and candidate/committed registers at malformed rejection. Native
LaTeX supplies crops, labels, boxes and semantic descriptions. Exact PNG/source
hashes, geometry and eleven installed archive/JAR bindings are retained privately.
The separate authoring plugin ran the real `clean cleanSandbox runIde` tasks.
The completed instance was terminated with SIGTERM; no native close/restart or
native Run test is claimed for this unit.

Three literal fragments match executed operations and fresh actual IntelliJ
highlighting exports. Three new records preserve all prior 812, for 815 total.
The authoring suite passes 104 Jupiter tests, public tooling 391 tests, and the
compatible original package validates and builds. The revised scanner is not
yet a delivered native lesson package. Final proofs receive visual/grayscale
review and pinned automated PDF/UA-2 validation with exact PDF hash binding.
Human author, learner and assistive-technology reviews remain open.

The original manuscript and unrelated MARS edits remain preserved. No upstream
implementation change or missing grader capability was needed. The Base64
companion follows this chapter before the file-format sequence. Later chapters,
native lessons beyond the first unit, production policies/delivery and human
review remain incomplete. The proposed MARS-versus-hardware opening is deferred.


## Native compact-encoding integration, 22 November 2026

The seventeenth main unit now has ten inline predictions, 25 Debug boundaries
across eleven complete sources, three ordinary Runs and four independent
assessments. Concrete sources distinguish exact signed differences, a 33-bit
mapped value, canonical seven-bit groups, whole-value reservation, candidate
scan versus committed input, capacity precedence, fifth-byte syntax and signed
reading range. The independent task validates the same wire without writing
caller data. Every worked variation retains the complete executed codec bodies.

All 296 original function-trial constructions and expected results match an
independent export from the unchanged Kotlin script, including complete arena
bytes and seed-derived offsets. Native profiles retain all 297 original public
cases and add one five-call reuse case: 298 function cases, 303 calls and
separate silent exit. Twenty-four original/native qualification processes accept
ten correct routes and reject fourteen faulty routes as expected. Reference,
alternative, relocated and wide-address scanners remain accepted. Six known
semantic faults and the unfinished starter remain rejected.

The original one-million-instruction budgets require more than one bounded data
file. MARS commit d2592fa6 adds native lesson format 8: one student assessment
can reference up to 32 profiles. Each file keeps the existing strict reader and
grader limits; the combined activity is bounded to 512 cases and 4096 function
calls, with distinct names and safe paths. The existing background grader
executes the validated batches in order and records one source-bound attempt.
A later failure prevents completion; cancellation consumes no attempt and
closes earlier machines. The public IntelliJ lesson host remains unchanged.
The three function groups contain 264 sequence/limit cases, 32 rejected-tail
cases and two caller/reuse cases. Per-invocation policies forbid accesses during
zero-limit and empty work even after earlier nonempty invocations.

Complete regression passes 226 CLI / 1238 plugin tests with zero failures,
errors or skips and required coverage/parser gates. A later focused Jupiter
test imports the exact student archive and verifies all observation and batch
bindings; no second full regression is claimed for this fixture-only addition.
All 505 book-tool and 128 separate-authoring tests pass. The exact packaged
plugins pass paired public API verification with 91 MARS / 20 authoring
experimental notices on the pinned IntelliJ target.

Two actual light-theme IDE processes exercise every quiz, Debug boundary, Run
and assessment. The first includes wrong-answer recovery, the starter at 0/264
and a correct unsaved scanner at 264/32/2/1. Twelve source edits clear eighteen
execution passes. Reopening retains eleven quiz attempts and twelve unchanged
source hashes, then renews eighteen checks. Assessment counts advance from
2/1/1/1 to 3/2/2/2. Eleven installed JARs match the pinned archives in each
process; accepted logs contain no ERROR/SEVERE. Native close/disposal precede
stopping only the owned IDE process. Actual clean cleanSandbox runIde tasks
precede both launches; all capture automation remains in the separate plugin.

The directly edited LaTeX supplement supplies the route, named source variations,
first-mismatch investigations and saved-progress guidance. Final review kept
short classification and command blocks with their headings after finding
awkward page splits. The final 206-page lab proof places this unit on 115–120;
its existing Base64 companion is now section 17.8. All six changed pages and
adjacent boundaries were visually reviewed, including both annotated figures
in grayscale. The exact final PDF passes 1728 PDF/UA-2 rules / 970910 checks,
zero failures, with embedded Minion Pro, Noto Sans Math and JetBrains Mono.
The main learning proof is unchanged. All 41 manual MARS files remain intact.
Human learner and assistive-technology reviews remain pending. The full goal
remains active; the proposed opening stays deferred. Native Base64 delivery is
next, before bounded file I/O.
