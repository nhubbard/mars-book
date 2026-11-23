# Checksums and streaming state

Chapter 19 and its written lab revise original project `024-crc32` after the PPM
unit. The main proof has 125 pages, with this chapter on 119–125. The lab proof
has 115 pages, with this unit on 111–115. Both are directly edited native LaTeX.
The original manuscript remains unchanged.

## Prerequisites and observable outcomes

The reader needs unsigned byte/word loads, logical shifts and XOR, full-width
saved state, bounded pointers and the educational file services from Chapter 18.

- Derive a reflected bit step from the old low bit and explain its arithmetic.
  The worked first-byte trace and executable probes cover all eight states.
- Distinguish a raw update from a finalized checksum and continue actual returned
  state across a split. A complete public caller switches from bit to table
  updating after four bytes; native Debug observes both boundaries.
- Derive the table index and shifted-state contribution, maintain zero extension,
  and charge for preparation. Prepared entries, an equivalent shift strategy and
  a repeatable public instruction benchmark provide checks and observations.
- Retain state across short file reads and report success only at EOF. Actual host
  files and deterministic descriptor-19 failures exercise different I/O domains.
- Implement a bounded chunk checksum check, proving length before field access and
  selecting exactly type plus payload. The independent task checks behavior rather
  than helper names, register allocation or a prescribed implementation strategy.

## Executable student materials

`labs/files/crc32.asm` completes bit update, table construction, table update and
file checksum. Its menu caller preserves the existing contract. The revision uses
compact Release 6 branches/calls with explicit nops retained. `crc32-stream.asm`
is a complete nine-byte demonstration using actual returned prefix state across
four/five slices and different update methods. It is not a generic file reader.

Reference, alternative and above-32-bit-address implementations each pass 100
original public cases and 3080 private cases, plus the revision's 26 public file
cases using descriptor 19. A fourth valid implementation uses full 64-bit arithmetic
right shifts and passes the original public/private suites: these agree with
logical shifts under the proved zero upper-half invariant. The incorrect narrowed
32-bit arithmetic shift is separately rejected. No new MARS implementation change
was needed for this unit.

`labs/files/crc32-chunk.asm` is an intentionally unfinished independent checker.
Its 136 public cases include fourteen payload lengths, four type patterns, correct
and mismatched stored CRCs, rejected counts, unsigned length mismatches and the
provided caller. Table-based and bit-based designs each pass these and 2305
expanded cases; an above-32-bit table design also passes the expanded set. The
expanded domain covers 32 source offsets across fourteen lengths, two types and
two checksum states, all 256 single-byte payloads with match/mismatch, and a
same-session retry sequence. Table offsets are correlated with source offsets;
these are not all possible payloads or every independent offset pair. The untouched
starter fails all 136 cases. Complete independent solutions remain private.

The chunk interface is deliberately narrower than PNG validation. It accepts
arbitrary four type bytes as checksum data, checks exact selected extent, covers
type plus payload and compares a big-endian stored checksum. It does not validate
chunk spelling/order, compressed contents or a whole image. The W3C Third Edition
Recommendation is the primary source for coverage and byte-order conventions.

`labs/checks/crc32-work.kts` reproduces all thirteen measurement rows: construction
plus two update methods over six datasets. Completed instructions are measured
from direct routine entry to return. Table construction costs 14348; the nine-byte
updates cost 519 bit/120 table, and the 4096-byte pattern costs 233206/53251.
The main table charges construction once separately, avoiding elapsed-time or
hardware-cycle claims. Exact inputs and measurements remain in the archive.

`tools/run_revision_crc.py` performs real file syscalls, compares a dumped result
with Python zlib, and retains source, binary input, dump and receipt in a new
directory. A separate nonexecuting grade query obtains the assembled result
addresses; no virtual file fixture is installed in the real-file run. Five
successful runs include both methods on the check message, empty input, an
8193-byte binary file and the previous chapter's actual PPM output. The original
input bytes remain unchanged. The tool's 65536-byte runner limit is not a bound
on the reusable file routine.

## Verification and publication scope

Eighteen deliberate semantic faults are rejected: signed source, narrowed shift,
late feedback decision, unscaled index, signed table entry, missing/old-seeded
table entry, lost streaming state, per-buffer finalization, request-count update,
short-read EOF, swallowed read error, descriptor leak, assumed descriptor three,
and four incorrect chunk coverage/finalization/byte-order variants. Seven exact
written cases include every first-byte state, illustrated entries, raw whole
result, empty methods, real split state and the five-byte read-failure timeline.

Three actual light-theme Debug observations show prefix raw state, complete raw
state and file failure. The error fixture forces read results 3, 2 and -1 on
descriptor 19, then verifies close. Native LaTeX supplies figure crops, labels,
boxes and semantic descriptions. The screenshots establish visible return states;
file-operation receipts separately establish cleanup. Actual host-file runs are
separate evidence. No native Run or restart experiment is claimed for this unit.

Two fresh actual IntelliJ highlight exports extend the previous 818 records without
altering them. Authoring passes 104 Jupiter tests; public tooling passes 391 tests;
the original CRC package validates and exports. Final font, page/grayscale and
PDF/UA-2 evidence is retained privately, including distinct earlier layout receipts.
Human author, learner and assistive-technology reviews remain pending. This unit's
native guided lesson and the broader curriculum/instructional-system goal remain
unfinished. Screenshot automation stays in the separate authoring plugin.

## Native grading foundation, 22 November 2026

The complete native CRC lesson remains in progress. Its chunk-checker data author,
`tools/crc_chunk_lesson_cases.py`, now retains all 135 original direct inputs:
112 payload/type/checksum trials, fifteen rejected counts and eight mismatched
unsigned lengths. An executable export of the original Kotlin checker agrees
with the new Python zlib author on every selected-count bit pattern, offset,
source byte, full table initialization, computed checksum and status. The finite
input construction is compared directly rather than inferred from matching names.

Sixteen bounded function profiles contain 137 cases / 142 calls: those 135 direct
cases, the original caller observed before and after publication, and an added
five-call state sequence. A separate console profile checks normal silent exit.
The original one-million and 100,000 instruction allowances remain intact. Caller
memory is watched across the complete original interval, including its unnamed
padding. Rejected counts permit no source/table access; mismatched lengths permit
only the four length bytes. Successful and checksum-mismatched chunks permit
only their selected chunk and prepared table reads. All stores remain forbidden
in the direct interface checks, including same-value stores.

Function-profile version 12 adds an explicit access-history allowance per policy.
The old native default is 65,536 events; this corpus retains the original script's
200,000-event allowance for each watched case or invocation. Older formats reject
the new field and retain their default. Each case-policy list and invocation-policy
list has a combined 4,194,304-event bound, matching the previous maximum of 64
default histories. Overflow fails instead of accepting a truncated trace. Tests
exercise larger histories, cumulative case limits, fresh per-call histories,
strict parsing and rejection before simulator creation.

The original and native graders accept table-based, bit-based and wide-address
implementations. Both reject four historical checksum faults, premature source
or table reads, same-value source writes and the unfinished starter. A deliberate
stale-error implementation passes all 136 original fresh-session cases but fails
the added native retry case: after a successful IEND checksum, a later length
error leaves v0=0xae426082 instead of zero. This is a test fault demonstrating a
coverage gap, not a simulator or worked-solution defect. The same-session case
also separates count rejection, checksum mismatch and successful retry, with
invocation-specific read permissions.

Seven new host-author tests pass, and the broader book-tool suite passes 529 tests.
A Jupiter fixture reads all sixteen exact function profiles and the exit profile
through production readers. Private `native-crc-foundation-01` retains the original
comparison, equivalent implementations, fault outcomes and source/build receipts.
The native file-check delivery, guided observations, first/resume IDE walkthrough
and revised written companion remain unfinished. Existing publication figures and
LaTeX compositions have not changed in this foundation step. The broader curriculum
goal remains active.

The completed implementation regression passes 234 CLI and 1,253 plugin tests
with zero failures, errors or skips. Both required coverage gates and the plugin
build pass. The exact fixture containing all sixteen function profiles plus the
exit profile is part of that full Jupiter run, and every member matches the
public author byte for byte. Paired public API verification passes with 91 MARS /
20 unchanged authoring experimental usages. All 41 manual MARS edits remain
unchanged. No IDE sandbox was launched and no publication proof was regenerated
for this grading-foundation step. Native file-check integration is next.

## Native read-only file grading, 22 November 2026

The prepared grader now reuses the existing in-memory file engine for native
checks. A case supplies an immutable read fixture and marks the invocations that
must open its exact path read-only, consume the available prefix through EOF or
an injected error, then close the acquired descriptor before returning. Different
positive read request sizes remain equivalent. Calls without the read marker
must produce no file operations; a later caller cannot hide a callee's leak.
Each case owns new file/cursor state. Fixture bytes and memory checks share the
existing setup/result memory allowance. Real host files are never opened by this
fixture mechanism.

Function-profile version 13 carries that data and the explicit per-call marker;
older formats reject them. It also adds a store-only memory policy so the file
checks retain their original padding rules: all writes in the caller span are
bounded to workspace, while unnamed-padding reads remain permitted. Named caller
objects retain their exact path/scratch/table read permissions. The global history
still includes reads and retains its original 100,000-event allowance.

`tools/crc_file_lesson_cases.py` preserves all 26 original file inputs, capacities,
methods, short-read settings, failure points, path/scratch offsets, seeded arenas,
checksums, statuses and effective transfer bounds. Its executable comparison uses
the unchanged Kotlin/JDK checker. Seven bounded profiles contain those 26 cases
and an added bit-then-table reopening case, for 27 cases / 28 invocations. The
original two-million and 100,000 instruction budgets remain; the added two-call
case uses a three-million cumulative allowance. Missing-file maximum transfer
settings have the same effective bound as the original unrestricted setting,
since all permitted requests are smaller and no read may follow a failed open.

The original and native graders agree on three correct implementations, including
an alternative and wide data addresses, and reject seven retained file faults:
per-buffer finalization, assumed descriptor three, leaked error descriptor,
request-count processing, lost prefix state, short-read EOF and swallowed error.
Twenty qualification processes have the expected result. Runtime Jupiter checks
also cover empty input, failure at zero/one/EOF, equivalent smaller requests,
double closes, wrong paths/descriptors, operations outside the marked call,
concurrent cases, host-file isolation, invalid data, cancellation and disposal.
The exact seven-profile fixture is read by the production parser. Six additional
host-author tests pass and the complete book-tool suite passes 535 tests.

This completes the native grading backend and case delivery, not the CRC unit.
The ordinary student Debug launch still needs its own task-bound read fixture
before the guided mid-read error can be reproduced without the authoring plugin.
That launch integration, connected inline lesson, first/resume IDE walkthrough
and LaTeX companion remain the next work. Existing publication figures and proof
are unchanged; no new IDE capture or human learner review is claimed here.

The full implementation regression passes 242 CLI and 1,258 plugin tests, both
coverage gates and the plugin build. A further focused seven-test run confirms
that an injected zero-byte-position error cannot hide reading descriptor three
instead of the acquired descriptor 19; the file result and returned status alone
would not catch that fault. This targeted assertion is recorded separately from
the full run. Paired public API checks pass with 91 MARS / 20 unchanged authoring
experimental usages. All 41 manual MARS edits remain unchanged. No IDE sandbox
was launched and no LaTeX proof was regenerated in this backend step.
