# UTF-8 state across chunks

Read the Chapter 12 UTF-8 companion. This unit assumes bounded byte access and callable-function reasoning. It asks you to validate and count scalar values, not draw characters or normalize text.

Begin with worked/range.asm and worked/lower-bound.asm. They are complete experiments comparing 9F and A0 with the inclusive interval A0–BF. Predict, inspect the unsigned comparisons and compare the returned Boolean. The helper isolates one decision; it is not a complete decoder. worked/range.kts checks all byte values against seven relevant intervals and the complete caller.

Implement scan_utf8 in independent/stream.asm by replacing its one break body. Keep the caller, allocation labels, chunk_ready marker and interface. Choose your own private labels, register allocation, state-store order and equivalent algorithm. Preserve saved registers and the stack pointer. Use the selected state passed in a0 and full-width pointer arithmetic; a small chunk length does not imply a small address.

The companion defines all eight state words and result meanings. Accept a byte only when the saved rule permits it. Nonfinal boundaries retain valid prefixes; finalization succeeds or reports truncation. An impossible byte records its absolute offset without accepting it. Later calls to a terminal state return (-3,0) without reading input or writing caller data.

Check the same continuing source with three inline assessments: 426 original stream trials, twelve interleaved-state trials and fifteen caller variants. Read the first failing case and locate the first incorrect decision before changing code. Hints are available subject to instructor policy. The native case profiles are data, not downloaded executable predicates.

Debug the supplied four-chunk caller at chunk_ready. Set the breakpoint condition to $s3 == 0, then change it to 1, 2 and 3 while resuming the same session. Compare the state with the companion's chunk table; the caller stores chunk_index, last_status and last_used before this marker. Finish with an ordinary silent Run.

Keep a pending-prefix trace, a malformed-versus-truncated comparison and two interleaved stream states with your reports. Explain why changing chunk boundaries preserves the final validity and scalar count. Human interpretation of that reasoning remains part of the exercise.
