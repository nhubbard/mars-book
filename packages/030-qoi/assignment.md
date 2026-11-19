# Encode and decode QOI images

Build six callable routines and connect them through the supplied caller. The native
chapter text is in `native/`; the book repository's chapter includes remain the editable
publication source. Guided mode follows 65 cumulative steps, their quizzes and two hints
per step. Free-writing mode uses the same external contracts with an independent design.
Guided register/frame observations are not requirements for a completed free-writing
solution. Remote delivery and enforced instructor progression/hint policy remain pending.

Start with a copy of `starter/main.asm`. Keep the supplied data arenas, caller controls,
entry labels and function_return boundary so checks can supply inputs and regain control.
The 57 BEGIN/END regions are unfinished. Status 4 and count zero indicate development
work, outside the completed statuses below. Earlier regions may have written partial
results before that return. Follow the checkpoint named by the exact observation recipe.

From the package root, run `python3 prepare.py s02-1 --source /path/to/working.asm
--output /path/to/new-observation.asm` on one line. This prepares only data and controls,
preserves authored instructions and refuses an existing output. Then run the matching
`checks/construction/s02.kts` with the CLI grade command. The guided checks install their
own deterministic in-memory files; ordinary IDE runs need matching real host input.
`python3 fixtures/fixture.py input.qoi --fixture trace` creates the worked 38-byte file
at a fresh path. Select `--fixture sprite` for the final sprite; it is also the helper's
default. No command silently overwrites an existing fixture.

## Local format domain

Use QOI version 1.0 with dimensions 1..64 in each direction (at most 4096 pixels).
Accept all six chunk types. Channels 3 or 4 and colorspace 0 or 1 are descriptive fields;
retain all four reconstructed components, including alpha when channels is 3. The
initial previous color is (0,0,0,255). All 64 cache entries begin as (0,0,0,0). Insert
reconstructed colors into `(3*r + 5*g + 7*b + 11*a) & 63`, including RUN colors.
Reject consecutive INDEX chunks naming the same slot. DIFF and LUMA channel arithmetic
wraps modulo 256. A RUN denotes 1..62 pixels and must not exceed the remaining extent.

Require the exact pixel count, followed immediately by the eight-byte marker
00 00 00 00 00 00 00 01, with no extra payload or trailing bytes. Marker bytes cannot
complete a truncated chunk. File width and height are big-endian unsigned 32-bit fields.
The metadata descriptor is four little-endian unsigned words: width, height, channels,
colorspace. A framebuffer word is 0xAARRGGBB, stored as B,G,R,A on this simulator.
The RGB display does not establish stored alpha or perform the format's alpha checks.

## Completed interfaces

| Entry | Arguments | Successful v1 |
| --- | --- | --- |
| qoi_bound | a0 = height<<32 \| width | 22 + 5*width*height bytes |
| qoi_validate | a0 bytes, a1 unsigned byte count, a2 descriptor | Pixel count |
| qoi_decode | a0 bytes, a1 unsigned byte count, a2 words, a3 unsigned pixel capacity | height<<32 \| width |
| qoi_encode | a0 words, a1 descriptor pointer, a2 bytes, a3 unsigned byte capacity | Actual encoded byte count |
| qoi_load | a0 NUL-terminated path, a1 bytes, a2 unsigned capacity, a3 descriptor | Loaded byte count |
| qoi_save | a0 NUL-terminated path, a1 bytes, a2 unsigned count | Saved byte count |

All return v0=0 on success. Format/descriptor errors return 1; unsupported byte counts,
file capacities and insufficient output capacity return 2; file I/O failures return 3.
Every completed failure returns v1=0. qoi_bound returns 1 for unsupported dimensions.
Preserve all 64 bits of s0..s7, fp and sp; return through the incoming ra. Use no console
I/O. Caller objects are disjoint. Descriptor pointers are eight-byte aligned, pixel
pointers four-byte aligned, and byte/path pointers need not be aligned. Full-width
pointers, capacities and counts must not be truncated or compared as signed values.

Reject counts above 20502 before dereferencing caller pointers. Counts below 23 are
format errors for validation, decoding and saving. Validation publishes its 16-byte
descriptor only after the complete stream is accepted. Decode validates before checking
pixel capacity and publishing any pixels; format and capacity failures leave the output
untouched. If reconstruction uses a second pass, start it with a fresh predictor, cache and cursor.

Encode validates the descriptor first, then requires capacity at least 22+5*width*height
before reading pixels or writing output. Retain both descriptive flags. Emit exactly the
returned number of bytes, with no trailing terminator or padding. Equivalent legal chunk
sequences are accepted. An opaque-black uniform image must use at most 22+ceil(n/62)
bytes. Any other uniform image must use at most 27+ceil((n-1)/62) bytes.

Load requires capacity in 1..20502 before pointer access or file I/O. Loop on actual
read counts and establish EOF, using a private probe when the caller buffer is exactly
full. Close the descriptor before validating and publishing metadata. A failed load
may leave its accepted input prefix in the byte buffer, but metadata remains untouched.
Save rejects invalid counts and validates the selected stream before opening/truncating
output. Loop on positive short writes, advance by accepted bytes, and close every
acquired descriptor once on every return path. Opening/truncating a file is externally
visible and cannot be undone by a later error. The current educational write syscall
reads one extra byte beyond the requested interval, including on a short/error return:
keep a readable guard byte outside the returned file extent; never include it in the
file count. This is a simulator syscall constraint, not part of the QOI representation.

## Completed-interface feedback

Run `java -jar /path/to/cli.jar grade checks/decode.kts /path/to/working.asm`, substituting
bound, validate, decode, encode, format, files or caller. These seven standalone domains
check completed contracts. A safe unfinished routine is expected to fail them. The host
checking decoder compares exact dimensions, flags and RGBA; it does not require the
instructor's encoding choices. Private grading adds cases and repeated-session checks.

The archive contains four light-theme capture plans. Actual IDE pixels and annotation
coordinates are pending; a plan must not be presented as a completed screenshot. The
trainer/capture runner belongs in a separate plugin depending on MARS. The substantial
book editing pass remains deferred until all 35 chapter drafts are written.
