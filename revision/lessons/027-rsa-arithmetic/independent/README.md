# RSA arithmetic interface contracts

Use MARS MIPS64 Release 6, little-endian memory and the educational syscall ABI.
Implement the seven entries in `rsa-arithmetic.asm`. Keep the supplied caller and
these arenas; a check can select arguments at different offsets within them.

| Caller object | Capacity |
| --- | ---: |
| `left`, `right`, `modulus`, `exponent` | 128 bytes each |
| `output` | 256 bytes |
| `workspace` | 1,024 bytes |
| `mode`, `status` | One four-byte word each |

The argument registers, rather than particular labels, identify each call's
selected objects. You may introduce your own helper labels, frames, constants
and workspace. Do not use unrelated caller objects as scratch storage.

## Numerical inputs and results

| Entry | Arguments | Successful numerical result |
| --- | --- | --- |
| `rsa_gcd64` | `a0`: unsigned 64-bit A; `a1`: unsigned 64-bit B | Full unsigned gcd in `v0`; status zero in `v1` |
| `rsa_pow32` | `a0`: unsigned word base; `a1`: unsigned word exponent; `a2`: unsigned word N; `a3`: output address | One word containing base^exponent mod N |
| `rsa_mul128` | `a0`: A address; `a1`: B address; `a2`: output address | All eight limbs of the unsigned 256-bit product |
| `rsa_addmod128` | `a0`: A address; `a1`: B address; `a2`: N address; `a3`: output address | Four limbs containing (A+B) mod N; inputs satisfy A<N and B<N |
| `rsa_reduce128` | `a0`: A address; `a1`: N address; `a2`: output address | Four limbs containing A mod N |
| `rsa_mulmod128` | `a0`: A address; `a1`: B address; `a2`: N address; `a3`: output address | Four limbs containing A*B mod N |
| `rsa_pow128` | `a0`: base address; `a1`: exponent address; `a2`: N address; `a3`: output address | Four limbs containing base^exponent mod N |

A 128-bit object consists of four unsigned 32-bit limbs, least significant limb
first. Each limb uses little-endian byte order. `rsa_mul128` extends the same
representation to eight output limbs. Every pointer is a full 64-bit address,
aligned to four bytes. Scalar word arguments are zero-extended in their registers.

Except for `rsa_gcd64`, successful functions return zero status in `v0` and write
the numerical result to memory. The gcd accesses no caller objects; define
`gcd(A,0)=A`, including the local convention `gcd(0,0)=0`.

The reducer and modular multiplier accept arbitrary unsigned 128-bit operands.
The modular power accepts a full unsigned 128-bit exponent. Do not infer the
adder's reduced-input precondition for these other interfaces. For positive N,
exponent zero gives 1 mod N, including zero base; N=1 gives zero.

## Failure order and ownership

Every modular entry returns status one when N is zero. Validate N before reading
other caller inputs or accessing output. A pointer to a multiword N must itself
be readable. No caller byte changes on this failure. This order remains required
when another supplied pointer is invalid.

Outputs are disjoint from inputs except for `rsa_addmod128`. Its output may
exactly alias A, B, or both when A and B share an object; N remains disjoint from
output. Other routines permit exact sharing among read-only inputs, including
squaring. Other partial overlaps are outside the contract.

Read only the selected input/output intervals among caller-owned objects. Write
only the selected output interval. Candidate-owned constants, gaps and private
workspace remain available. The checks observe accesses as well as final bytes;
a forbidden store still violates the contract if it stores the original value.

Preserve the full values of `s0`–`s7`, `fp` and `sp`, and return through the complete
64-bit return address. The incoming stack pointer is aligned to eight bytes.
Use an owned stack frame for nested calls and restore it on every return path.
Do not produce console output.

## Guided work and free writing

All implementation stages use this one continuing source. The original starter
stops at `break` in unfinished entries. Implement the current interface before
running its staged assessment; unrelated unfinished entries need not work yet.
The worked programs in `../worked` are separate observation experiments and do
not prescribe your helper names or instruction sequence.

Use the native assessment actions in MARS Learning. The nine groups retain all
123 original public cases. The final assessment rechecks them on the completed
source after later edits. It accepts equivalent algorithms and different exponent
scans; it does not require an exact instruction count or private allocation.
Instructor rules can control required predictions, checks, hints and mode changes.

The nine neighboring `.kts` scripts retain the original public CLI checks. With
JDK 25 and the MARS CLI, an optional command-line check has the form
`java -jar cli.jar grade CHECK.kts rsa-arithmetic.asm`. Keep the supplied caller
when using `caller.kts`. This native package uses its declared case profiles for
the in-IDE assessments; it has no preparation script or source-replacement step.
