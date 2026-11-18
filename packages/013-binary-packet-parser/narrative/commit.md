# Publish output only after validation

The caller initializes the output record before calling the parser. On success, replace its four words at offsets zero, four, eight, and twelve with flags, sequence, payload length, and payload-sum residue. On failure, make no writes to that record. This is stronger than leaving the same final bytes. Writing a partially decoded field and restoring the old word would still violate the rule.

Keep tentative values in caller-clobbered registers until the checksum passes. Then store the four fields and set the successful return values. The reference is a leaf function: it calls no other function, so it can return through the incoming `ra` without saving a nested call’s return address. It uses temporary registers and leaves `s0` through `s7` and `sp` unchanged. A different implementation may use a properly restored stack frame.

The four stores are separate machine instructions. The interface promises a completed coherent record when the call returns, and no record stores on failure. It does not promise a single hardware-atomic 16-byte update to concurrent readers. The stopped `parse_ready` boundary in supplied main occurs after return, before main records the status and payload pointer in its own data objects.

The payload pointer aliases the input arena. No payload copy is made. The caller must retain the input bytes while using that pointer. For an empty payload, the returned address is still input plus eight, but the payload has length zero. In that case the byte at the address is the checksum trailer; it must not be consumed as a payload byte. On every error, `v1` is zero and supplies no slice.
