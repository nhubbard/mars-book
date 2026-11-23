# Find the boundary of a nested value

Read Chapter 21 and its written lab, then open this unit in Learn. The complete
cbor-telemetry.asm entry selects a head read from the 52-byte example. Worked
copies select one nested item, a decoder, or the decode/bound/encode pipeline.
The independent cbor_sequence_count entry is the part you will implement.

A head describes a type and argument. Its extent does not establish the extent
of a string or container. A walker consumes one complete supported item; a
sequence loop must then decide whether more top-level items remain. Telemetry
decoding additionally requires the local schema and exactly one complete root.
Keep these boundaries separate before following individual instructions.

Answer the inline predictions. Their source pointers select the relevant worked
copy. During Debug, use one breakpoint at a time and inspect the selected paused
frame. The highlighted instruction has not executed. Learn can outline the next
requested boundary as soon as it recognizes the current pause. Resume between
observations and finish each process before opening another source. The repeated
after_k_child and after_k_key observations deliberately occur in different active
recursive invocations; the selected frame and saved state explain each visit.

Begin with b8 04. Split its prefix fields, derive major five, argument four and
head extent two, then compare a one-byte selection that cannot supply the argument.
The full-counter copy requires every argument bit: 01 02 03 04 05 06 07 08 is
assembled numerically before any later native storage decision.

Next follow the optional map within a ten-byte selection. Its first item occupies
nine bytes and leaves an integer sibling. Keep a continuation for each parent:
cursor, remaining bytes, outstanding children or pairs, inherited depth and map
key identity. A byte string's 80 ff payload is data, not two recursive values.
The helper may return a major type in a2, which no longer means the parent's depth.

Compare container admission with key identity. At inherited depth eight, an empty
array still opens a forbidden additional container, while a scalar needs no new
level. The depth-seven and primitive-eight worked copies are supplied for manual
comparison. Repeated numeric key zero is a duplicate even when its heads have
different widths. The same key in an inner map is independent and valid.

Then follow complete telemetry publication. Required fields are staged privately
until the root, required-field mask and selected extent are accepted. The late
counter copy offers only 51 bytes and leaves the whole caller record untouched.
A successful record preserves inactive tails instead of clearing its allocation.
Its identifier is binary bytes; its counter and signed halfwords are numeric
native fields with their own storage order.

The pipeline derives 37 preferred output bytes from this record. It drops the
validated optional field and chooses shorter admitted spellings, so its output
is an application projection rather than a byte-for-byte round trip. The
36-byte-capacity copy separates two contracts: decode has already published a
valid record, but encoding must leave its destination untouched on capacity failure.
These examples perform memory operations and normal exit, not host file I/O.

Implement only cbor_sequence_count in independent/cbor-sequence.asm. Read its
full contract in independent/README.md. Begin with disjoint top-level intervals
and keep their item count separate from byte extents. Retain inherited depth
across helper calls, prove positive in-range progress, and clear the aggregate
result on every malformed suffix. You may use remaining bytes or an end address,
reuse the complete helper or traverse equivalently, and choose your own frame
and temporary registers.

Seven native groups retain the original 203 cases and add two documented caller/
retry cases. Use their failures and graduated hints to choose an observation.
The unfinished body deliberately contains break. The optional CLI checker retains
the original 203-case task; the native supplements separately check actual caller
publication and repeated invocations in one session. Retain a parent continuation
trace, a source-permission argument and a failure that changed your reasoning.
Finite tests support those explanations without proving every possible nested input.
