# Count complete top-level items

Implement cbor_sequence_count in cbor-sequence.asm. Keep the caller and complete
helpers. Its body intentionally stops at break. The public checks accept equivalent
implementations; they do not prescribe a frame size, private label, register
allocation, helper-call count or exact instruction count.

| Argument | Meaning |
| --- | --- |
| a0 | First byte of the selected interval; it may be unaligned. |
| a1 | Full-width unsigned selected byte count. |
| a2 | Full-width unsigned inherited open-container depth. |

First reject a1 above 1024 with status two and zero count, without using a0.
Then reject inherited depth above eight with status one and zero count, also
without a source access. A valid selected count of zero succeeds with zero items
and must not use a0. This order applies even when both count and depth are invalid.
Large or negative 64-bit bit patterns must not become small admitted values through
truncation or a signed comparison.

For a nonempty admitted selection, recognize consecutive complete values under
the main chapter's local supported-item profile. It supports unsigned and negative
integer heads, byte strings through 32 bytes, arrays through 64 elements, and maps
through eight pairs with unsigned keys zero through seven. Each map has its own
numeric key-identity mask. Nonpreferred head widths are admitted where otherwise
valid; indefinite lengths, unsupported major types and reserved forms are not.
A container may open only below inherited depth eight, even when empty. Scalar
values and byte strings do not open another container. This generic task does
not apply the telemetry record's required-field schema.

| Condition | v0 | v1 |
| --- | --- | --- |
| Complete selected sequence, including empty | 0 | Number of top-level items |
| Malformed item, incomplete suffix or invalid inherited depth | 1 | 0 |
| Selected count above 1024 | 2 | 0 |

Every successful item must consume a positive byte extent within the remaining
selected interval. Advance the cursor by that extent and count one top-level item,
regardless of its nested child count. Apply the original inherited depth to every
sibling. cbor_skip accepts a0/a1/a2 and returns status/byte extent in v0/v1; it may
replace argument registers, including a2 with a major type. Keep any live loop state
across the call. A malformed suffix clears the whole result, even after valid items.

Read only the selected source bytes. Do not write source, record or destination
bytes, even by storing the same value. Do not use those caller objects as scratch.
Private owned stack space is available below incoming sp; no caller slots above
sp are granted. Preserve s0–s7, fp and incoming sp at their full 64-bit values,
keep required stack alignment at call boundaries and retain the incoming return
path. Do not print or perform file operations. Return to the supplied continuation.

All original cases retain their four-million-instruction budget, complete seeded
state and 500,000-notice enclosing access history, including stack traffic. The
permissions on caller objects are separate from that history bound. Complete
source/record/destination images are checked after the call. The cases cover
inherited depth and independent maps, lengths through 1024, complete nested items,
truncations and malformed suffixes, early unsigned errors, and selected prefixes
that exclude physical bytes beyond their end.

The native caller supplement uses the actual supplied main and trace, expects one
top-level item, and checks its stored status/count after real termination. The
retry supplement shares one session through a rejection, a successful empty-array
item, a valid empty selection at depth eight, and a sibling depth failure. Expected
status/count pairs are (1,0), (0,1), (0,0), (1,0). Result registers are not guaranteed
to begin at zero. Each call must establish its own result and traversal state.

The optional cbor-sequence.kts checker retains the original 203 cases. The native
corpus has 205 cases / 208 calls after the two supplements. These are finite tests,
not all possible structures or pointer combinations. Use a failing case to distinguish
an incorrect item extent, stale inherited depth, lost parent state or uncleared
failure result before changing several parts of the loop at once.
