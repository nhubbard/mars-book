# Successful native function counts

All cases use capacity eight. Start is tail for push into an empty queue and head
for pop from a one-record queue. Counts exclude assembly, session creation, caller
setup, main, and exit, and stop before the supplied continuation.

| Call | L | Start | Forward | Reverse |
|---|---:|---:|---:|---:|
|Push|0|7|32|32|
|Push|3|0|61|73|
|Push|3|7|62|77|
|Push|7|4|102|134|
|Pop|0|7|30|30|
|Pop|3|0|59|71|
|Pop|3|7|60|75|
|Pop|7|4|100|132|

The demonstrated forward paths use 31+10L+W instructions for push and 29+10L+W
for pop, where W is one for a complete record advance reaching/crossing the physical
end. The reverse implementation calculates each payload index separately and is
an accepted equivalent, not a faster implementation. A two-span copy is a later
optimization exercise requiring derived span lengths and all behavioral checks.
These are native instruction counts, not elapsed time, processor cycles, or exact
count requirements for equivalent student submissions.
