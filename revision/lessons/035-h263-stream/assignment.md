# Inspect H.263 stream boundaries

Read Chapter 27 and its written lab alongside this package. The preceding unit established prediction and local reconstruction. This unit follows those numerical decisions into fields, block syntax and actual output files. The supplied programs are complete worked experiments. Your independent program is one bounded bit reader, retained through all its checks.

Import the student archive with **Import MARS Lesson Package**, then open **Inspect H.263 stream boundaries** in Learn. Answer the inline questions before choosing the offered Debug activity. Put breakpoints at its named stops and inspect the stated registers. The source pointer for a question identifies the instruction under discussion; the debugger's selected frame identifies the actual paused instruction. Follow the experiments in order when you first use the package. You can return to them while writing the reader.

The raw input is already present. A Python script generated eight QCIF pictures, each 176 by 144 samples of Y followed by the two 88 by 72 chroma planes. A picture occupies 38016 bytes, and eight occupy 304128. The six-tick clock step gives 5000/1001 frames per second and a duration of 1.6016 seconds. No host encoder generated the H.263 output: the MIPS program will do that work.

## Append one field before interpreting a stream

Open `worked/append-fields.asm`. Its selected two-byte window begins five bytes into a guard-filled allocation. The old bytes are hexadecimal `A5 A5`; only their first three bits are committed. The caller appends decimal 419 in nine bits. Write the prefix and new field as bits before Debug: the prefix is `101`, and the new field is `110100011`.

At `lesson_append9`, `s0` and `s1` contain bytes `BA 35`, and `s2` is the used count twelve. The four low bits of the second byte retain their previous values. They are storage, not syntax that this call accepted. The writer returns zero status and nine appended bits.

The next call explicitly appends four zeros. At `lesson_pad4`, the bytes become `BA 30`, and all sixteen bits are committed. A final one-bit request exceeds capacity. At `lesson_append_rejected`, status two and count zero leave the bytes and used count unchanged. Validation must cover the whole field before a first byte is modified. A correct first byte cannot compensate for a later failure that publishes only part of the field.

## Follow coefficient events through two legal spellings

`worked/coefficient-events.asm` uses the actual supplied coefficient emitter. Its INTRA block has DC code 255, level one at natural index one and minus one at index eight. Natural indices describe storage positions. RUN describes zeros between emitted nonzeros in scan order. Here both RUN values are zero; LAST is zero for the first AC event and one for the second.

At `lesson_coeff_compact`, the supplied emitter appended sixteen bits. The context started at bit three, so its used count is nineteen. The second caller writes the same known DC and AC events with explicit baseline escapes. It calls the bit writer for an eight-bit DC and two twenty-two-bit events; it is a fixed alternative spelling of this example, not another general coefficient emitter. At `lesson_coeff_escape`, `s0` is total payload length 52 and `s1` is used count 55. The last writer call returned 22 in `v1`; that register does not contain the total of all three calls.

Decode each valid payload to the same natural-order levels. Comparing byte strings would reject a permitted coding choice. Also check exact consumption: matching the decoded values while silently consuming a different number of bits can misalign the next block. The final call places invalid ordinary level 128 at the end of the array. Status one at `lesson_coeff_rejected` retains the earlier compact buffer and count nineteen.

## Distinguish presence, coded differences and actual motion

In `worked/macroblock-syntax.asm`, the first call writes six DC-only INTRA blocks. Ordinary AC presence is zero, but all six DC bytes are mandatory. At `lesson_intra_presence`, the supplied coding policy uses five header bits plus 48 DC bits, returning 53. This is an I-picture INTRA macroblock, not a P-picture skip.

The second call is INTER in a P-picture. The chosen horizontal component is minus 32 and its predictor is 31, both in half-sample units. Their difference is minus 63; wrapping into the coded interval minus 32 through 31 gives one. At `lesson_motion_wrap`, `s3` is this normalized difference. The actual selected motion remains minus 32. The eight-bit header uses complemented INTER CBPY; a direct Y-presence index would describe different syntax.

The third call sets actual motion to zero and leaves residuals zero while retaining the nonzero predictor. At `lesson_actual_zero_skip`, the emitter chooses its permitted one-bit skip. A skip means actual zero motion here. It does not mean “reuse the predictor,” which would move the block.

## Inspect a header without claiming to decode the picture

`worked/picture-header.asm` builds a real restricted fifty-bit header with the bit writer. At `lesson_header_prefix`, it has committed the twenty-two-bit numerical start code 32. The remaining field widths are 8, 13, 5, 1 and 1. At `lesson_header_fields`, direct shifts and masks recover start code 32, temporal reference zero, type field 4160 and quantizer eight in `s0` through `s3`.

Seven bytes provide 56 bits of storage. Only fifty are header syntax. At `lesson_valid_bits`, cursor fifty leaves zero valid bits; the final six storage bits retain decimal 37. This is why the reader's valid count must be independent of byte capacity. These direct masks inspect known header fields. They do not consume variable-length macroblock payloads, reconstruct pictures or verify transform accuracy.

## Run the complete encoder and inspect an accepted prefix

`worked/clip-progress.asm` runs the actual encoder on three bundled files. Relative input and output names are resolved from this source file's directory in ordinary MARS Run and Debug. Keep that working directory when creating your own configuration. The first request uses all eight pictures. The second file has one complete picture and sixteen bytes of the next. The third has two complete pictures followed by one extra byte.

At `lesson_clip_complete`, status/count is `0/8`; the supplied encoder accepted 2212 stream bytes. At `lesson_clip_truncated`, the pair is `3/1`, and 696 bytes belong to the accepted first picture. At `lesson_clip_trailing`, it is `3/2`, with 908 accepted bytes. Both failures have input status three. In the last request, the exact-EOF check fails after the requested pictures have been accepted. A count equal to the request does not override a nonzero status. `s5` is one when the progress record names the expected accepted reference buffer: B after eight pictures, A after one, B after two.

The caller deliberately prints all three status/count pairs and exits normally, so you can compare them in a single Run:

```text
0/8
3/1
3/2
```

The process exit describes this observation caller; it does not turn the two failed library requests into successes. Run writes `encoded.263` and `reconstructed.yuv`, plus separate `truncated` and `trailing` output pairs beside the source. Repeating this worked experiment replaces those six generated outputs. Copy them elsewhere before comparing a changed experiment.

From this imported package's root, check the actual files:

```sh
python3 tools/inspect_outputs.py worked --report work/stream-report.json
```

This needs FFmpeg on your command path. The checker reads the actual stream, checks the restricted syntax, independently decodes every accepted picture, and compares source quality and local reconstruction. It never creates encoded bytes for the MIPS program. Its report distinguishes the successful eight-picture request from the accepted output prefixes of the two failed requests. It refuses to replace an existing report. Run success and matching printed counts alone do not prove interoperability; inspect this report too. Finite clip quality checks also do not replace the separate full inverse-transform accuracy gate.

## Write one bounded reader and keep its state explicit

`worked/reader-boundaries.asm` isolates three arithmetic issues before you implement the reader. At `lesson_unsigned_field`, the signed word load is hexadecimal `ffffffff80000001`, while the unsigned load is `0000000080000001`. A thirty-two-bit field requires the latter. At `lesson_remaining_bits`, capacity is sixteen bits but valid count and cursor are twelve, leaving zero valid bits. At `lesson_full_width`, the low word of the request is nine, but the complete value `0000000100000009` is out of range.

Keep one implementation in `independent/h263-read-bits.asm`. Replace only the deliberate unfinished body at `h263_read_bits`. Its comments state the complete interface: an aligned native context contains input pointer, byte capacity, valid bits and cursor; `a1` supplies the full requested width. Return status/value in `v0/v1`. Validate metadata before input access, and admit the entire requested width before extracting a field. Read only intersecting bytes, keep the value unsigned, and publish the cursor only on nonempty success. Empty and failed reads must return a fresh zero value.

The staged checks cover short fields (128 placements), wide fields (136), little-endian metadata (264), boundaries (26), malformed metadata (30), maximum capacity (6), and reuse (2). Two final checks each contain 328 placements. Their union covers every one of the original 592 cases; shared boundary cases repeat. Both final checks must describe the same completed source revision. After editing, return and renew any earlier result that is stale. Hints develop preflight, extraction and publication separately; equivalent bit-at-a-time and byte-fragment implementations are accepted.

The ordinary reader demo starts at cursor three in bytes `BA 35`, with twelve valid bits. Before running a completed function, predict the nine-bit value 419 and final cursor twelve. Then use the same context for a rejected one-bit request and a successful empty request. These successive calls expose a stale accumulator that isolated successful calls might miss. Your reader can later inspect the actual picture headers, but completing that extension does not make it a complete H.263 decoder.
