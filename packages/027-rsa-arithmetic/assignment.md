# Build RSA arithmetic

Use MARS MIPS64 Release 6 with little-endian memory and the educational simulator ABI.
The native chapter is authoritative. Retain the supplied fixture labels and capacities:
left/right/modulus/exponent128 bytes each, output256 bytes, workspace1024 bytes, mode
and status words. Functions receive addresses selected independently of these labels.
Free-writing mode permits other helper labels, registers, frames, constants and private
workspace. The guided starter is one cumulative source, not a pair of start/end templates.

## Numerical interfaces

- rsa_gcd64(a0 unsigned64 A,a1 unsigned64 B) returns the full gcd bits in v0 and v1=0.
  The local convention is gcd(0,0)=0. It accesses no caller objects.
- rsa_pow32(a0 uint32 base,a1 uint32 exponent,a2 uint32 modulus,a3 output) receives
  zero-extended scalar operands and writes one little-endian word.
- rsa_mul128(a0 A,a1 B,a2 output) writes the complete eight-word unsigned256-bit product.
- rsa_addmod128(a0 A,a1 B,a2 N,a3 output) requires A<N and B<N, then writes (A+B) mod N.
- rsa_reduce128(a0 A,a1 N,a2 output) accepts arbitrary unsigned128-bit A and writes A mod N.
- rsa_mulmod128(a0 A,a1 B,a2 N,a3 output) accepts arbitrary unsigned128-bit inputs.
- rsa_pow128(a0 base,a1 exponent,a2 N,a3 output) uses the full unsigned128-bit exponent.

Every multiword numerical input occupies four32-bit limbs, least significant first;
all bytes within each limb are little-endian. Modular outputs occupy sixteen bytes.
Pointer objects require four-byte alignment; pointers and saved addresses remain64-bit.
The stack is eight-byte aligned. Each function preserves every bit of s0..s7, fp, sp,
returns through the full ra, and performs no console I/O. Temporary registers are volatile.

All successful entries except gcd return v0=0. For every modular entry, modulus zero
returns v0=1 before reading other caller inputs or accessing output, leaving caller
objects unchanged. The modulus object itself must be readable. Modulus one yields zero.
Exponent zero means1 mod N, including zero base; negative exponents are outside the domain.

Outputs are disjoint from inputs except addmod128, which permits its output to exactly
alias either or both operand objects. Its modulus remains disjoint from output. Exact
sharing among read-only inputs is allowed in the other multiword routines, including
squaring with one input object twice. Other partial overlap is outside the contract.
Write exactly the selected output bytes among caller-owned objects. Read only selected
input/output intervals among those objects. Candidate-owned gaps and constants are
permitted; do not use unrelated fixture bytes as scratch or hard-code argument labels.

## Guided and free-writing work

Guided mode replaces named BEGIN/END bodies and uses the step's construction script.
It checks the illustrated intermediate registers, private frame and numerical state.
Missing work returns development status three, including v1=3 for gcd. Nested nonzero
status must propagate through the appropriate restore before any output is published.
These development paths are not completed-interface success results. prepare.py selects
exact data from inputs.json without editing authored code or overwriting existing work.
Caller and direct-function checkpoint names are recorded separately in each recipe.

Free-writing mode uses every script in publicChecks in package.json. Invoke each with
java -jar cli.jar grade CHECK SOURCE under JDK25. Completed grading checks numerical
results, status, all result bytes, ownership, preserved high bits and repeated calls;
it permits equivalent algorithms, including opposite exponent scans. No exact instruction
count or private label is required. Keep the supplied caller if using caller.kts.
Instructor policy fields describe quiz/check requirements, hints and allowed mode
switching; a remote service enforcing those policies is still pending.

The four IDE figure slots are planned. This bounded arithmetic lab is not a secure RSA
key size, key generator, constant-time implementation, encryption scheme or signature
scheme. Read the native OAEP/PSS discussion after the arithmetic agrees.
