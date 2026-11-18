# Specify the two call results

Push first asks whether the record can ever fit this ring. Since $`N=L+1`$, that condition is $`L<C`$. If it fails, return `v0=-2`, `v1=0`. Only then compare the encoded size with current free space. Insufficient free space returns `v0=-1`, `v1=0`. Success returns `v0=0`, `v1=N`. An oversized record reports minus two even when the queue is also full. The caller cannot solve that error by merely waiting for this same ring to empty.

Pop first checks the occupied-byte count. An empty queue returns `v0=-1`, `v1=0`. Otherwise, read the oldest prefix. If the destination capacity is smaller than its payload length, return `v0=-2` and put the required payload length $`L`$ in `v1`. The record remains queued so a caller can retry with a larger destination. Success returns `v0=L`, `v1=N`. The destination receives exactly $`L`$ bytes, without a terminator or length prefix.

A successful empty record returns zero and one, not the empty-queue error. The record consumes a byte of queue space even though it copies no payload bytes. A destination capacity of zero is sufficient for that record. Error meanings are specific to each function; keep the called operation beside the returned values when logging a trace.

| Call | Outcome               |   `v0` |    `v1` |
|:-----|:----------------------|-------:|--------:|
| Push | Record too large      | $`-2`$ |       0 |
| Push | Not enough free space | $`-1`$ |       0 |
| Push | Success               |      0 | $`L+1`$ |
| Pop  | Queue empty           | $`-1`$ |       0 |
| Pop  | Destination too small | $`-2`$ |   $`L`$ |
| Pop  | Success               |  $`L`$ | $`L+1`$ |

All failures are free of caller-data writes. A function may load descriptor fields while checking them, but it may not reserve space by changing a cursor and then restore that cursor after detecting a failure. This action rule lets callers retry without exposing partially applied operations. It is checked through store history as well as final words.
