# Keep scalar progress separate from byte progress

The branch implementation uses `t0` for total and `t1` for complete scalars. `t2` is need; `t3` and `t4` are the next-byte bounds; `t5` is the sequence start. The full input cursor is `t6`, while `t7` counts accepted bytes in this call. `t8` holds one unsigned byte, and `t9` holds comparisons and constants. These are implementation choices, not extra arguments promised by the caller.

At each iteration, first compare the per-call accepted count with the chunk size. Only then load a byte. When need is positive, test that byte against the retained bounds. Otherwise classify it as ASCII, a possible lead, or an error. A valid lead sets need but does not increment the scalar count. ASCII and a completed last continuation share the scalar-completion path.

One common acceptance path advances total, the per-call count, and the full cursor. It is bypassed on an impossible byte. This placement makes the returned count point to the offending byte in the current chunk and keeps absolute total equal to its stream offset. A valid prefix ending at the chunk boundary instead reaches finality handling without fetching the next mapped byte.

The routines are leaf functions and preserve saved registers and the stack pointer. A correctly restored stack frame is also acceptable. Stateful tests call the same function repeatedly in one simulator session. The grader’s checked call operation sets entry and return addresses as external setup, preserves execution observations and the cumulative instruction limit, and refuses to interrupt pending branch delay or forbidden slots. It does not reset the stream between calls.
