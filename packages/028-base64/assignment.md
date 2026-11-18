# Encode and decode Base64

Use MARS MIPS64 Release 6, little-endian memory and the educational simulator ABI.
The directly edited native chapter defines the interface and explains each construction
step. Retain source/destination arenas of 8192 bytes, the supplied caller controls and
4096-byte private workspace. Keep function_return as the direct-call return sentinel.

The six completed interfaces are:

- b64_encode_scalar and b64_encode_msa(a0 source, a1 unsigned byte count, a2 destination,
  a3 unsigned capacity): at most 4096 source bytes, with output length 4*ceil(count/3).
- b64_decode_scalar and b64_decode_msa with the same arguments: at most 5464 canonical
  standard-alphabet text bytes, producing at most 4098 binary bytes.
- b64_encode12_msa(a0 source, a1 destination): exactly 12 bytes to 16 characters.
- b64_decode16_msa(a0 source, a1 destination): exactly 16 valid unpadded alphabet
  characters to 12 bytes. Validation is a precondition of this direct kernel.

Whole-function statuses are v0=0 success, 1 count bound, 2 malformed decoder text,
3 insufficient capacity. v1 is the exact written byte count, or zero on failure.
No routine adds a NUL terminator. All pointers may be byte-unaligned; pointer arithmetic,
counts, capacities and return addresses retain all 64 bits. Preserve all bits of s0..s7,
fp and sp; temporary and vector registers are volatile. Perform no console I/O.

Whole encoders require disjoint buffers. Whole decoders and the direct decoder kernel
permit disjoint buffers or exact in-place operation. Other partial overlap is outside
the contract. Write exactly the output interval among caller objects; selected input
and output intervals may be read. Candidate-owned constants and private storage are
allowed. Every caller byte outside the selected result must remain unchanged, including
same-value writes to input, and kernels must not widen their caller footprints to 16 bytes.

Whole entries reject over-limit unsigned counts before pointer access. Empty input then
succeeds with zero count without pointer access. Encoders check capacity before reading
source. Decoders require a multiple-of-four text length, the standard +/ alphabet,
required canonical padding, no whitespace and zero unused bits. They validate the entire
text before checking capacity or storing output, so malformed text takes precedence
over insufficient capacity. Failures publish no bytes. Count limits precede malformed
text decisions. Capacity is unsigned, including values whose high bit is set.

Guided mode fills 37 regions in 43 steps. Unfinished whole routines return development
status 4; the classifier uses -2 because numerical 4 is a valid sextet and -1 means an
invalid character. Supplied nested guards propagate development signals. These signals
are outside the completed interface. Guided checks require the illustrated checkpoint
state. Free-writing checks accept equivalent algorithms and register choices with the
same completed interface; exact instruction counts are never a grade threshold.

Prepare recipes with `python3 prepare.py s05-1 --source your-main.asm --output new.asm`.
Preparation preserves authored code and refuses to overwrite an existing output. Stop at
the recipe’s callerCheckpoint when using main; direct-function checks use their own return
sentinel. Quizzes and progressive hints follow the recommended order. Instructor policy
fields describe intended progression controls; remote enforcement remains pending.

Run a completed check with `java -jar /path/to/cli.jar grade checks/encode.kts your-main.asm`.
Public domains cover encoding, decoding, canonical acceptance, count/capacity bounds,
exact-footprint kernels, supplied callers and same-session reuse. Actual light-theme IDE
captures, independent accessibility review and the author’s substantial prose edit remain
unfinished. The four capture plans are specifications, not screenshots.
