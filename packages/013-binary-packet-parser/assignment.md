# Parse a bounded binary packet

Implement `parse_packet(a0=input pointer, a1=available bytes, a2=output record pointer)`.
The native chapter and narrative companions specify the format and error priority.
Use MIPS64 Release 6, the default little-endian layout, and the MARS educational ABI.

The input pointer may select any byte offset in the 256-byte packet arena. The
available count is nonnegative and no greater than the remaining arena size.
Input and output never overlap. The selected output is record_a or record_b,
16 bytes long and word aligned. Its four words are flags, unsigned sequence,
payload length, and payload-sum modulo 256. Preserve s0..s7 and sp at full width.
Return through the supplied ra, with no console output. Keep the supplied layout,
main routine, marker, and return scaffold. Other internal labels/registers may vary.

Success: v0=9+L and v1=input+8, output record complete. Failure: v0 is the specified
negative status, v1=0, and no output writes occurred. No input or guard writes are
permitted. Every input access must fit wholly inside the supplied available bytes;
mapped padding is still outside the input. Only the first packet contributes to
the checksum and consumed count. Trailing available bytes are allowed.

Error priority: n<8 -> -1; wrong magic -> -2; version !=1 -> -3;
reserved flags -> -4; L>64 -> -5; n<9+L -> -1; checksum nonzero modulo 256 -> -6.
A valid empty payload still requires nine total bytes.

Guided mode appends code in the named starter regions. Free writing may replace
function internals while retaining the public interface and supplied data labels.
The checks accept equivalents; they do not compare the source or exact instruction
count. Instructor controls for hints and progression are metadata awaiting trainer
implementation. IDE screenshot recipes are planned, not completed screenshots.

Run the public checks with the pinned or newer compatible MARS CLI:

```sh
java -jar /path/to/cli.jar grade checks/parser.kts starter/main.asm
java -jar /path/to/cli.jar grade checks/program.kts starter/main.asm
```

An unfinished starter is expected to fail. The course tool can run both checks:
`python3 tools/course.py check --package 013-binary-packet-parser --cli /path/to/cli.jar --source /path/to/main.asm`.
