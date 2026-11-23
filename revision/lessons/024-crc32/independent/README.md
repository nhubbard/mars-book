# Check one bounded CRC chunk

Implement crc32_check_chunk in crc32-chunk.asm. Its body currently stops at a
break instruction. Keep the caller and data arenas so the supplied checks can
exercise the same interface. You may choose a bit-based or table-based design,
use different temporary registers, and arrange equivalent work differently.
Do not copy a source pattern merely to satisfy a check.

## Input and record layout

The integer arguments are full-width MARS64 values:

| Register | Meaning |
| --- | --- |
| a0 | Address of the selected record's first byte. |
| a1 | Selected byte count, valid only from 12 through 4096 inclusive. |
| a2 | Address of a prepared 256-entry table, if your design uses it. |

The selected record has this layout. The payload length and stored CRC use
big-endian byte order even though MARS data words in this environment are
little-endian. Byte order in a file is part of its interface, not an instruction
for how a native word must be laid out in simulator memory.

| Offset from a0 | Length | Contents |
| --- | --- | --- |
| 0 | 4 | Unsigned big-endian payload length L. |
| 4 | 4 | Type bytes, all of which belong to the CRC input. |
| 8 | L | Payload bytes. |
| 8 + L | 4 | Unsigned big-endian stored CRC. |

The total selected count must equal L + 12. A valid input therefore has payload
length at most 4084. The type is four supplied bytes; this task does not validate
PNG's type-name grammar or require ASCII letters. It checks a bounded checksum
record, not an entire PNG file.

For the supplied empty IEND record, the twelve selected bytes are:

```text
00 00 00 00  49 45 4e 44  ae 42 60 82
```

Its covered CRC input is only 49 45 4e 44. Start a raw update at 0xffffffff and
finalize its returned state once by XOR with 0xffffffff. The computed checksum
is 0xae426082. The encoded zero length and stored checksum are excluded from the
covered slice. A nonempty record covers the same four type bytes followed by
exactly its payload.

## Establish permission before reading

First check that a1 is in 12–4096 as a full-width count. If it is outside that
range, return the malformed result without reading input or table. The checks
may supply unusable pointers in this situation. Truncating a large or negative
64-bit count to a convenient 32-bit value does not validate it.

For an allowed count, you may read the four length bytes. Decode their unsigned
big-endian value and require it to equal a1 minus 12. If this equality fails,
return the malformed result without reading type, payload, stored CRC or table.
The equality establishes both the record's framing and the covered-slice length
without trusting an unchecked sum derived from the encoded length.

After both checks pass, your code may read the selected record bytes and the
1024-byte prepared table. It must not read bytes before or after those spans.
A bit-based design may ignore the table. Return paths for malformed framing
must not rely on readable table contents. Unaligned record starts and relocated
full-width data addresses are valid when their selected spans are valid.

## Results and preserved state

| Condition | v0 | v1 |
| --- | --- | --- |
| Valid framing, computed and stored CRC match | Finalized computed CRC, zero-extended | 0 |
| Valid framing, CRCs differ | Finalized computed CRC, zero-extended | 1 |
| Invalid selected count or declared length | 0 | 2 |

Return the computed checksum on a mismatch as well as on a match. A later bad
call must set v0 to zero rather than accidentally retaining an earlier result.
Do not publish an unfinished raw state as the computed checksum.

Preserve s0–s7, fp and the caller's sp, using the full 64-bit saved values. Keep
the required 16-byte stack alignment at call boundaries. An owned stack frame
below the incoming sp may hold saved state; this interface grants no caller
stack slots above sp. If you call another function, preserve the return path as
well as any live state that the call may overwrite. Return to the supplied caller.

The checker does not change input, table or other caller data, even by writing
the same value. Prepare any table in the caller, outside the checker. The provided
caller does this before its call, then records the returned values and exits.
Its shared result slot is named raw_result; in this chunk exercise it stores the
finalized v0 result. The return contract above determines the value, despite that
slot's shared name.

## Use the checks to investigate

The native Learn assessments retain the existing inputs and budgets. They contain
80 short-record cases, 32 larger-record cases and 23 framing rejections. Two
additional cases exercise the complete caller and repeated invocations, including
rejection, success, checksum mismatch and successful retry in the same session.
Each call has its own read permissions; a later call cannot excuse an earlier
forbidden read. The cases accept equivalent correct implementations rather than
matching private labels, register schedules or a fixed instruction count.

The file-reader regression has 27 cases / 28 calls on the complete helpers already
present in this starter. Keep those helpers working while adding your checker.
It covers both methods, positive short reads, empty/binary input, missing input,
injected errors and descriptor ownership. Every marked file call must release
its acquired descriptor before returning. This regression is not a substitute
for implementing and checking crc32_check_chunk.

Use the first failure to decide what to inspect. A memory-access failure calls
for a permission argument before a new XOR formula. A wrong result on a later
retry calls for inspecting every return path and its output initialization.
Hints are graduated and subject to instructor policy; edit and retry while
retaining an explanation of why the change addresses the observed failure.

The optional CLI checks are crc32-chunk.kts and crc32-files.kts. The latter checks
the retained file helpers. The original worked-update checks are supplied as
../worked/crc32-work.kts. Use the MARS CLI grade command with the check script and
this source path; the native Learn route does not execute downloaded Kotlin.
