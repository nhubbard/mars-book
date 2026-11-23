# Raw states and complete streams

Read Chapter 19 and its written lab before continuing in Learn. This unit starts
with complete programs. Use them to explain the recurrence and its interfaces
before writing the independent checker. Basic shifts, XOR, byte loads, function
calls and the educational file services are prerequisites.

The bit updater accepts a byte slice and an unfinished CRC state. The table
updater has the same state contract but replaces eight bit steps with a lookup.
The file reader keeps that raw state across positive reads and finalizes once
at EOF. A read error is a different result: the accepted prefix is not a
successful complete file. Keep these boundaries separate as you work.

Answer predictions in the inline controls. The source pointer identifies an
instruction discussed by the question. Debug activities open their selected
complete source. Use one breakpoint at a time, inspect the selected Debug frame,
and remember that a highlighted instruction has not executed yet. Resume between
the requested boundaries and finish each process before opening another source.
The worked register values describe these examples; they do not prescribe the
register allocation or instruction sequence of your independent solution.

The table variant prepares its table explicitly. The split variant passes the
actual returned state for the first four bytes into the last-five-byte update.
Compare those boundaries with the unsplit nine-byte program. The ordinary Run
activity exits silently because its caller stores a result instead of printing.
An empty console alone does not tell you which checksum was computed.

During the file Debug activities, Learn supplies virtual input under the name
fixture.bin. Each activity states whether it contains 123456789, fails after five
accepted bytes, or is missing. Transfers are limited to three bytes and the first
acquired descriptor is 19. Fresh launches start with independent input state.
Resuming a paused process continues that same process. These settings do not edit
the real file: worked/fixture.bin is supplied for ordinary execution outside the
activity. The normal Run/Debug working directory is the selected source's folder.

Then implement crc32_check_chunk in independent/crc32-chunk.asm. Read the exact
interface and memory permissions in independent/README.md. Begin with the supplied
empty IEND record, extend to nonempty payloads and checksum mismatches, then prove
that malformed counts and lengths cannot cause forbidden reads. The starter
contains a deliberate break instruction. Its complete file/update helpers are
available to use or inspect, but the checker itself is your work.

The six check groups cover short records, larger records, malformed framing,
caller/retry behavior, retained file-reader behavior and complete caller exit.
Use the first mismatch to choose a new observation. Keep a slice/permission
argument, a raw-state continuation argument and one concrete I/O failure trace
with your completed source. Finite passing checks support those arguments; they
do not prove correctness for every possible byte string or measure hardware speed.
