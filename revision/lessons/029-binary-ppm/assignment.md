# From bytes to an image file

Read Chapter 18 and its lab companion. Start with the complete ppm-codec.asm,
which parses an in-memory two-by-two P6 image without opening a file. Account
for its eleven header bytes and twelve sample bytes before starting Debug.

Follow a result across three boundaries: validation in registers, publication in
metadata, and publication in the caller's status/count objects. The complete
source variations expose rejected selected extents, punctuation in binary pixels,
word byte order and exact serialized capacities. Use one line breakpoint at a
time, before the requested instruction. Resume each program to normal exit after
its final observation. At the exit syscall, v0=10 selects the service; read the
earlier returned status from caller memory.

The pipeline reads worked/input.ppm and creates or replaces worked/output.ppm.
MARS resolves these relative paths from the source directory. Keep this small
image in the lesson folder and compare the files' bytes after the ordinary Run.
The native Run checkpoint observes normal silent termination. The preceding
Debug states and your actual file comparison provide different evidence. The
written figure with descriptor 19 and seven-byte transfers comes from a separate
authoring fixture; ordinary host files do not promise those transfer sizes.

Before missing-input.asm, check that worked/absent-input.ppm does not exist.
Before save-invalid.asm, retain the successful pipeline's output as the old file.
A malformed selected input must be rejected before a truncating open. A failure
after open has a different obligation: close the descriptor and report failure,
although partial output can remain. A nonempty zero-progress write is failure.

Complete ppm_encode_negative in independent/ppm-negative.asm, preserving the
supplied helpers and caller. Its exact interface is in independent/README.md.
The break starter deliberately fails. You may serialize complemented channels
directly or reuse the supplied serializer, then transform only the successful
raster. The checks accept either strategy and other equivalent implementations.
They do not require the worked routines' temporary registers or instruction order.

Keep a header-size derivation, an explanation of one publication boundary and
the actual complementary image. Use the first failed result or forbidden access
to choose a new observation. Passing the finite corpus does not prove every
pixel combination or the correctness of an untested file failure path.
