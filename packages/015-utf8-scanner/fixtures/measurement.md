# Native complete-call measurements

Each call starts with an empty active state and one final chunk. Counts exclude
setup, assembly, session construction, main and exit, stopping before continuation.

|Fixture|Branch instructions|Table instructions|Branch reads|Table reads|
|---|---:|---:|---:|---:|
|64 copies of 41|991|991|71|71|
|32 copies of c3 a9|1567|1791|71|103|
|16 copies of f0 9f 99 82|1631|1583|71|87|
|Malformed e0 9f|64|64|9|10|
|Final truncated f0 90|91|88|9|10|

Byte values are hexadecimal. Reads are simulator data notices, including seven
state word reads, input byte reads, and optional table word reads. They exclude
instruction fetch and external inspection and are not byte totals or cache misses.
The 256-word table occupies 1024 bytes. Its extra address/load/extraction work hurts
the short two-byte path while saving instructions on the more complex four-byte
path. ASCII bypasses the table. No universal elapsed-time or target-cycle speedup
is claimed, and equivalent submissions have no exact-count requirement.
