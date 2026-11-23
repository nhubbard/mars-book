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

Keep the supplied data layout, main routine and operation_ready marker. Replace
the two failing function bodies. The revised caller aligns the stack and uses
compact calls and branches. The functions may call helpers or inline their cursor
logic, but must preserve the incoming return link and full saved-register values.

## Public assessments

The three push-NN.cases.json files contain all 260 original push trials; the
three pop-NN.cases.json files contain all 260 original pop trials. Each function
can be checked independently of the other in separately bounded batches. Each trial has one call, a 5000-instruction allowance,
changed descriptor/ring selection, source/output offsets, guarded input arenas
and the original preservation contract. The policies observe every byte of each
access. A wider access must fit wholly within its permitted window. Post-return
policies retain those footprints through the supplied continuation and normal exit.
The original standalone kernels.kts remains available for comparison.

caller.cases.json retains the twelve original prefix lengths of the supplied
fourteen-command caller. Each case has a cumulative 20000-instruction allowance.
The program runs continuously between public operation_ready markers; no registers
are reset between commands. Every publication checks descriptor, source, ring,
output, status, auxiliary result and operation index. The writable byte set belongs
to that particular operation. A failed command cannot borrow a successful command's
write permissions. No caller-data store is permitted after the final publication.
The zero-command case permits only installation of the native ring pointer.
The generated caller.kts invokes the same publication checker as the native activity.

From this downloaded lesson folder, run the optional CLI checks with JDK 25:

```sh
java -jar /path/to/cli.jar grade independent/kernels.kts independent/ring.asm
java -jar /path/to/cli.jar grade independent/caller.kts independent/ring.asm
```

The public trials include empty and full queues, arbitrary capacities, a zero-length
front record, binary payloads, wrapping, impossible sizes, insufficient current space
and a short-output pop. They accept equivalent algorithms and correct successful
store orders. A public pass is evidence for these cases, not a proof over every queue
history or every address in the machine. Private course grading may add valid cases.
Explain why the invariant and failure decisions hold before generalizing the interface.
