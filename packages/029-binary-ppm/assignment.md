# Read and write a binary PPM image

Use MARS MIPS64 Release 6, little-endian memory and the educational syscall ABI.
The native chapter explains the local format and cumulative construction. Its LaTeX
files are authoritative; descriptors and Markdown do not regenerate the publication.

## Teaching subset

Accept one P6 image with width and height from 1 through 64, Maxval exactly 255,
canonical positive decimal tokens without leading zeros, a header of at most 256 bytes
and exactly three raster bytes per pixel. A header gap contains ASCII space, bytes
9 through 13 or a # comment terminated by LF. Gaps are required after magic and between
numeric tokens. Exactly one LF follows Maxval. Raster samples are uninterpreted bytes;
LF, space, # and zero have no lexical meaning there. Reject trailing or missing raster
bytes. The complete file bound is 12544 bytes. These limits are local exercise rules.

## Completed interfaces

- `ppm_parse(a0 bytes, a1 unsigned count, a2 metadata)`: validate the complete extent,
  then write u32 width at offset 0, u32 height at offset 4 and u64 raster offset at 8.
  Return pixel count in v1. Metadata is eight-byte aligned and unchanged on failure.
- `ppm_decode(a0 bytes, a1 unsigned count, a2 framebuffer, a3 unsigned pixel capacity)`:
  validate before checking capacity, then publish four-byte words 0x00RRGGBB. Return
  height in v1 bits 63..32 and width in bits 31..0. Framebuffer is four-byte aligned.
- `ppm_encode(a0 framebuffer, a1 packed dimensions, a2 destination, a3 byte capacity)`:
  validate dimensions before capacity, and both before reading or writing caller data.
  Emit `P6\nWIDTH HEIGHT\n255\n` and RGB triplets. Ignore each input word's high byte.
  Return exact byte length, with no NUL terminator or guard written into the result.
- `ppm_load(a0 path, a1 byte buffer, a2 unsigned capacity, a3 metadata)`: require capacity
  1..12544 before pointer or file access; read through EOF, close, then validate/publish.
  Return file byte count. A failure may leave a byte-buffer prefix, but not metadata.
- `ppm_save(a0 path, a1 serialized bytes, a2 unsigned count)`: validate before opening
  in truncate mode, then handle short writes until the selected count is accepted.
  Return the count. A write failure may leave a partial file.

All interfaces return v0=0 on success, 1 for malformed data or invalid dimensions,
2 for a count/capacity bound, and (file routines only) 3 for open/read/write failure.
Failures return v1=0. Parser, decoder and saver reject count >12544 unsigned before
pointer access; an empty file is malformed. Preserve all 64 bits of s0..s7, fp, sp and
the return path. Pointers, counts, capacities and packed dimensions are full-width.
Temporary registers are volatile. Perform no console I/O. Byte pointers may be unaligned;
input/output objects are disjoint. Every successful open must be closed on every return.
Paths are NUL-terminated. Current MARS write syscalls read one extra byte beyond a request;
retain one readable guard after serialized bytes, outside their selected count.

## Working modes and checks

Create the worked input with `python3 fixtures/fixture.py input.ppm` from the
package directory. The helper refuses to overwrite an existing file. Use that directory
as the successful file examples' working directory.

Guided mode fills 37 named regions across 42 steps. Keep the public caller, data arenas,
region markers and checkpoint labels. Development status 4 and zero count means a body
is unfinished; it is outside completed statuses. The supplied 128-byte nested frames
retain a private descriptor at 80..95 and EOF probe at 96. File bodies keep their live
descriptor in s3, initially -1, for the supplied temporary cleanup path.

Use `python3 prepare.py s02-1 --source your-main.asm --output new.asm`. Preparation
changes only fixtures and caller controls, retains authored code and refuses existing
output. `inputs.json` records direct and caller checkpoint names. Guided completion
checks live in `checks/construction`. They install their own fixtures and can run on the
current cumulative working file. Follow the chapter's observation before implementing
the next body. In-memory error/short-operation fixtures are not installed by an ordinary
IDE launch; normal successful file examples need the matching real input file.

Free writing may use equivalent algorithms, private constants, register allocations and
frames. Retain the published entries, fixture arenas and function_return test sentinel.
The six public completion checks in `checks` grade interfaces, exact bytes, caller
ownership, preserved state and file lifecycle. Run, for example,
`java -jar /path/to/cli.jar grade checks/files.kts your-main.asm`. Caller checks additionally
exercise the supplied main/results_ready boundaries. No exact instruction-count gate is used.

Quizzes and two progressive hints per step follow the recommended order. Metadata names
instructor controls for hints, checks, quizzes and mode switching; remote enforcement is
not implemented yet. Four light-theme figure plans are specifications, not screenshots.
Actual annotated IDE capture, independent accessibility/usability review and the author's
substantial prose rewrite remain pending. Rewriting is deferred until all 35 drafts exist.
