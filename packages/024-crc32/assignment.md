# Check files with CRC-32

Implement crc32_bit_update, crc32_table_init, crc32_table_update and crc32_file.
The reflected feedback mask is edb88320, initial message state is ffffffff and the
final XOR is ffffffff. CRC values are unsigned 32-bit bit patterns in full registers.
The check message is exactly nine ASCII bytes 123456789: raw 340bc6d9, finalized
cbf43926. No newline or terminating zero belongs to that message.

Both update entries receive a full byte pointer in a0, length 0..4096 in a1 and a
zero-extended raw state in a2. Return the zero-extended next raw state in v0. The table
entry also receives a word-aligned 1024-byte table in a3. The source can be unaligned.
Read only the logical input and prepared table, write no caller bytes, and return the
incoming state unchanged without source/table reads on empty input. Table initialization
takes its word-aligned destination in a0, writes exactly 256 words and returns 256.
Compute each entry by applying eight reflected steps to its index; old contents are
not an input. Only those 1024 caller bytes may change. Slices are disjoint.

The file entry receives a terminated path in a0, a scratch pointer in a1, capacity
1..256 in a2 and method zero/one in a3. Zero selects bit updates and one selects table
updates. Return finalized unsigned checksum in v0 and status zero in v1. Open failure
returns checksum zero/status one. Read failure returns checksum zero/status two.
Every successful open must be closed once, including after a read error. Do not close
an invalid open result. Only successful EOF finalizes the state; errors do not expose
a partial checksum as a successful complete-file result. No console output is allowed.

The file workspace is the selected scratch capacity and the first 1024 crc_table bytes.
Other caller bytes, including the path, remain unchanged. Each positive read request
may be any size up to capacity. Only the returned bytes contribute to the checksum.
A positive short read is not EOF. The next update continues the actual raw state;
initialize once per file and complement once at successful EOF.

Keep the supplied caller, labels and allocations: 4160 source bytes, 272 table words,
128 path bytes and 320 scratch bytes. Tests select independent offsets into these
backing arenas, including pointers above 32 bits. All functions preserve every bit of
s0..s7, fp and sp and return through supplied ra. Incoming sp is eight-byte aligned.
Private stack and read-only submission constants are allowed. Register allocation,
helper labels, equivalent arithmetic and buffering choices are not final correctness
requirements. Learning a requested bit/table strategy may be assessed separately.

## Cumulative construction and final checks

The native TeX chapter is the composition authority. Follow the 34 steps and replace
24 marked regions. The first bit/table stubs establish a defined raw return before
branching. The initializer's provisional zero and the file construction's development
status three are unfinished-state markers. Final checks accept only the completed
interface, including file statuses zero, one and two. Supplied provisional cleanup
closes a descriptor acquired by an unfinished file stage.

Run prepare.py with a recipe ID, --source and a new --output path to create a separate
observation program. It refuses existing outputs and the source path. Optional
--fixture-output writes recipe bytes to a new real file and embeds its absolute ASCII
path, which must fit 127 bytes plus terminator. Ordinary files do not reproduce forced
short reads, missing-file controls or injected errors. Guided CLI checks install those
in-memory fixtures explicitly. No grading file syscall opens a host file.

Guided checks under checks/construction inspect the illustrated stage and may name its
temporaries. Kernel callers pause at kernel_done and file callers at file_done before
result stores. s30 pauses on the second file_read visit before restoration. Run a check
with java -jar /path/to/cli.jar grade checks/construction/s30.kts /path/to/current.asm.
Use JDK 25 and the pinned MARS revision, which supplies provideFiles and file histories.

For completed free-writing solutions, use checks/kernel.kts, checks/file.kts,
checks/program.kts and checks/reuse.kts. They check independent offset functions, file
results/resources, the actual caller and actual raw state passed across methods.
The unfinished starter intentionally fails final checks. Exact reference instruction
counts, private labels and illustrated temporaries are not final correctness gates.

## Publication status

The chapter measures function-entry-to-return simulated instructions, charging table
construction separately. These counts establish neither elapsed-time nor hardware-cycle
ratios. PNG chunk reuse checks type plus data, excluding length and stored CRC; stored
CRC byte order is most significant first. This is not yet a full PNG validator.

Student/instructor distributions are separate. Remote delivery, enforced hint and
progression policies, actual light-theme IDE captures, the author's deferred rewrite
and independent accessibility validation remain pending. The four capture recipes are
plans, not captured pixels or completed annotation receipts.
