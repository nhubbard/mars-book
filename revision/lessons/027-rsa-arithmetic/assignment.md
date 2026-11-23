# Extend arithmetic beyond one register

Read the RSA arithmetic companion in the written lab. This unit builds seven
numerical interfaces, beginning with a greatest common divisor and ending with
an unsigned 128-bit modular power. Basic integer operations remain useful, but
the program must now represent values that exceed an instruction's operand width.
Keep the representation, the mathematical value and the function's observable
contract distinct as you work.

The worked programs isolate operations that you can inspect before writing the
complete functions. Use `independent/rsa-arithmetic.asm` for every implementation
stage. Its README gives the full contracts and its seven deliberate `break`
instructions mark unfinished entries. Retain the supplied caller and object
arenas; your helpers, register allocation and algorithm can differ from the
examples. The staged checks accept equivalent implementations.

## Start with the unsigned remainder

In `worked/remainders.asm`, the first division gives 13 mod 11 = 2. The next
pair is therefore 11,2, whose remainder is one. Euclid's recurrence keeps the
common divisors while decreasing the second operand. The full loop stops when
that operand is zero; it returns the other operand. This order also makes
`gcd(A,0)` work without attempting a division by zero.

The later all-one dividend exposes a separate question: what do these bits mean?
As an unsigned doubleword they denote 2^64-1, which is divisible by 17. As a
signed doubleword they denote -1. The instructions can produce different
remainders from the same register bits. The gcd contract requires the unsigned
operation and all 64 result bits.

Return the gcd in `v0` and zero status in `v1`. Most other interfaces in this
unit use `v0` for status and memory for the numerical result. Keep this exception
visible in your caller reasoning; a register name alone does not tell you the
meaning of its value.

## Retain a complete product until reduction

A limb is one digit in a positional representation with base 2^32. Its value
ranges from zero through 2^32-1. `worked/word-product.asm` loads the same limb
with `lw` and `lwu`: one sign-extends, the other zero-extends. Subsequent
64-bit operations consume that extension as part of their operand.

Two maximum unsigned words multiply to `fffffffe00000001`. Keeping only the low
word leaves one. Keeping all bits but applying signed remainder interprets the
product as a negative integer. For the scalar power interface, preserve the
whole unsigned product and then reduce it with the unsigned doubleword operation.
A small modulus does not make the dividend small.

Build `rsa_pow32` by processing exponent bits. At a loop boundary, let `r` be the
accumulated residue, `x` the current base residue and `y` the unprocessed exponent.
The product `r*x^y` must remain congruent to the requested power modulo N. An odd
bit contributes `x` to `r`; shifting the exponent and squaring `x` prepares the
next bit's weight. Begin with `r=1 mod N`, including when N is one.

## Give each limb and carry a place

`worked/limb-column.asm` adds two maximum limbs and an incoming carry of one.
The setup forms each unsigned limb by logically shifting an all-one doubleword
right by 32 bits. The sum `1ffffffff` has low limb `ffffffff` and outgoing carry one. A 64-bit
register holds this particular 33-bit intermediate, so both parts are visible.
A full multiplication must also account for several products contributing to
the same column. State where those contributions and their carries reside before
storing a finished output limb.

Four input limbs can require eight full-product limbs. Write every selected
output limb, including high zeros for small inputs. The checks begin with poison
bytes and also pass one read-only object as both operands. Squaring must work
without modifying that object.

## Keep the carry when choosing a modular subtraction

`worked/carry-reduction.asm` uses an eight-bit model so that the complete sum is
visible in one register. With N=251 and A=B=250, the sum is 500. Its low eight
bits are 244 and its outgoing carry is one. Comparing only 244 with 251 would
incorrectly skip reduction. The residue is 500-251 = 249.

The 128-bit addition has the same reasoning, but no scalar register holds its
complete 129-bit sum. Keep the extra carry explicitly and use it with a full
unsigned comparison. Because the contract supplies A<N and B<N, the sum is
below 2N and at most one subtraction is needed. Read a source limb before an
aliasing output store can destroy it. Exact aliases with either input are legal.

## Reduce a growing prefix

`worked/prefix-reduction.asm` processes four input bits, `1101`, modulo five.
Appending a bit to a binary prefix doubles the old value and adds the bit.
Reducing after each append gives residues 1,3,1,3. At the third bit the provisional
value is six, so subtracting five leaves one. The next bit then produces three.

If the previous residue is below N, the provisional `2r+bit` is at most 2N-1.
That bound explains the single subtraction. It does not justify losing a carry
when the representation is already full. Extend the reasoning to all 128 input
bits, including equality with the modulus and a small modulus beneath a large
input. A valid implementation may use a different scan if it establishes the
same result and ownership contract.

## Combine reduced contributions

In `worked/modular-product.asm`, multiplier binary `11` selects two contributions
from base 250 modulo 251. The first residue is 250. Doubling the base gives
500 mod 251 = 249, and adding that second contribution gives 499 mod 251 = 248.
The experiment writes these two contributions explicitly. Its intermediates fit
in one register; a general four-limb implementation needs its multiword helpers.

The modular multiplier accepts arbitrary input operands. Before relying on the
modular adder's reduced-input precondition, establish that the values supplied
to it are below N. A helper's precondition remains an obligation even when both
helpers belong to your own program. Exercise squaring and repeated calls before
using multiplication inside exponentiation.

## Move exponent bits across the whole representation

`worked/exponent-halves.asm` holds a supplied 128-bit exponent in two doublewords.
A logical right shift moves the low bit of the high half into bit 63 of the low
half. Inspect that transferred bit before treating a zero low half as evidence
that no exponent remains. The high half can still contain bits that select later
multiplications.

The final observation reduces one modulo one to zero. It explains the zero-
exponent result without a special assumption that every modulus is greater than
one. A full exponentiation must handle all 128 exponent bits, preserve its inputs
and survive nested helper calls with the complete return address intact.

## Validate before touching other caller objects

`worked/validation.asm` reads and combines four modulus limbs. They are all zero.
The other pointer arguments deliberately contain the invalid address one; the
helper returns status one without dereferencing them. Main then reads a separate
output word and finds its original `5a` bytes.

The unchanged word is useful evidence but does not prove absence of an earlier
access. The independent checks observe reads and writes as well as final bytes.
They also protect caller objects at varying offsets and addresses, while allowing
your own workspace and gaps. On a successful call, write only its selected output.
On a zero-modulus call, do not access that output at all.

## Finish in the same source

Complete the actual caller checks after all seven entries work. At
`gcd64_return`, `v0` still contains the numerical gcd; the caller next moves
`v1` to publish the status. Other arithmetic entries already return status in
`v0`. Publication by main occurs after the callee's observation ends.

The reuse cases change operations and argument offsets within one simulator.
They include an error followed by success and a forward power followed by its
inverse, then the original forward request again. Keep state in the current
arguments or an owned frame, and restore saved registers on every return path.

Later implementation edits can make earlier passes stale. Run all 123 original
cases again with the final assessment on the completed source. A normal Run uses
the supplied mode zero and terminates silently; silence does not establish
arithmetic correctness. The required Debug activities use unchanged worked files,
so observing them does not require mode edits in your independent implementation.
Finish each Debug process before another launch or source edit. Add a new input
and explain which invariant predicts its result before using execution to check it.
