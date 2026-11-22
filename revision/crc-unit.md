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
