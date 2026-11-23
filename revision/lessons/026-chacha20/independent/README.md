# Implement four word-transformation interfaces

Complete `chacha20.asm` using the contracts below. Its missing regions currently return development status three; the independent groups deliberately fail until they are complete. Retain the caller objects, controls and public entry labels. Your register allocation, frames and helpers may differ from the worked source. The six native groups use bounded data rather than source matching. The original six Kotlin CLI checks remain available alongside them.

## Public contracts


`chacha_quarter(a0,a1,a2,a3)` receives four distinct word-aligned addresses, in a,b,c,d
order. They may be in any order and need not be adjacent or evenly spaced. Replace
exactly those four words with one ChaCha quarter-round. Return v0=0. Read/write only
the selected words among caller-owned objects; do not access the gaps between words.

`chacha_block_scalar(a0 input,a1 output)` and `chacha_block_msa(a0 input,a1 output)`
receive a word-aligned 64-byte input and disjoint word-aligned 64-byte output. The input
is already a raw sixteen-word state and can contain arbitrary bit patterns. Preserve
all input bytes. Perform ten column/diagonal double-rounds, add corresponding original
words modulo 2^32 and serialize all sixteen sums in little-endian order. Write exactly
64 output bytes and return v0=0. The MSA construction uses word lanes and row selections.

`chacha_xor(a0 state,a1 source,a2 destination,a3 unsignedCount)` reads an unchanged,
word-aligned 64-byte valid IETF state: constants at 0, key bytes at 16, uint32 counter
at 48 and nonce bytes at 52. Accept counts 0..4096. Source/destination can be unaligned
and may be identical or disjoint; other overlap is outside the interface. State is
disjoint from both. Check the full unsigned count first. Return 1 for a count above 4096,
with no supplied-pointer access or caller writes. Zero returns 0 without any supplied
pointer access. For a positive bounded count, all required state/buffer intervals are
mapped. The last required counter is c+ceil(count/64)-1. Return 2 if it exceeds
0xffffffff, before writing output. Otherwise transform exactly count bytes using
successive counter blocks and return 0. The last available counter may be used for its
last block. Do not carry into the nonce. On failure preserve all caller memory.

Successful XOR writes only the selected destination interval among caller objects.
For exact in-place operation, load each source byte before storing its replacement.
The input state never advances. Repeating a call with the same state restarts at that
stream position. Do not keep a hidden counter across independent calls. Buffers and
states can move between calls; honor supplied addresses rather than fixed data labels.

All entries preserve every bit of s0..s7, fp, sp and return through the full supplied ra.
The stack is eight-byte aligned; temporary scalar registers are volatile. The illustrated
MSA allocation clobbers w0..w9 and their floating-point aliases as stated in the chapter.
Candidate-owned constants and private workspace are permitted; caller-owned source,
destination, state, quad and controls remain protected outside each selected interval.
The explicit caller data labels are fixture surfaces, not fixed function arguments.


The native caller group also runs the real main dispatch, requires actual exit, and checks stored status. Same-session fixture setup changes caller bytes while preserving private machine state. A passing scalar implementation and a passing MSA implementation must each satisfy the independent expected block, not merely agree with one another.
