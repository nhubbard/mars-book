# Queue variable-length records in a ring

Implement push_record and pop_record using the descriptor and interface in the
native chapter. A record is one unsigned length byte followed by L payload bytes,
where 0<=L<=31. Descriptor capacity C is 1..32 bytes, not necessarily a power of
two. Caller state is valid: 0<=head,tail<C, 0<=used<=C,
tail=(head+used)%C, and the occupied circular bytes contain whole valid records.
The functions do not validate corrupted descriptors. Buffers and descriptors are
separate, nonoverlapping objects. Descriptors are eight-byte aligned; data pointers
may be byte unaligned. Use default little-endian MIPS64 Release 6 and MARS ABI.

Descriptor offsets: head word 0, tail word 4, used word 8, capacity word 12,
full buffer pointer doubleword 16. Both calls receive a0=descriptor, a1=source
or destination, a2=payload length or destination capacity. Counts are 0..31.
The caller provides enough source/destination storage within the supplied arenas.

Push error priority: L>=C returns v0=-2,v1=0; insufficient free bytes for L+1
returns -1,0; success returns 0,L+1. It appends a copied prefix and payload,
advances tail modulo C, adds L+1 to used, and preserves head.
Pop: used==0 returns -1,0; destination too small returns -2,L; success returns
L,L+1 and copies exactly L payload bytes without prefix or terminator. It advances
head by L+1 modulo C, subtracts that amount from used, and preserves tail. A zero
payload record succeeds with 0,1. Pop never writes ring bytes, including old data.

Failures make no caller-data writes, including writes restored later. Successful
calls may write the first twelve descriptor bytes, but final state must match the
operation. Capacity and full buffer pointer are read-only. Push may write only its
new circular record slots; pop may write only its L returned destination bytes.
Every ring access fits in the first C bytes. Push source reads fit wholly in its
L supplied bytes. Preserve full s0..s7 and sp, return through the supplied ra,
and produce no console output. Copy direction, state-store order on success,
scratch registers and exact instruction counts are not grading requirements.

Keep the supplied data layout, main routine and marker. Guided mode appends code
in named regions; free writing may replace the functions while retaining callable
labels. Progression and hint controls remain metadata pending trainer support.
Three planned figures are capture recipes, not completed screenshots.

Run both public checks on the finished source:

```sh
java -jar /path/to/cli.jar grade checks/kernels.kts starter/main.asm
java -jar /path/to/cli.jar grade checks/program.kts starter/main.asm
```

The unmodified starter exits normally but should fail behavioral grading. Use
`python3 tools/course.py check --package 014-ring-buffer-logger --cli /path/to/cli.jar --source /path/to/main.asm`
from the public repository to run all package checks.
