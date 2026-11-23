# Frame-reversal interface

Implement `wav_reverse_frames(a0 PCM, a1 metadata prefix, a2 output)` in this
one continuing source. The supplied WAV helpers and caller are complete.
The prefix contains unsigned 32-bit sample rate at offset zero, unsigned
32-bit channels at four, and unsigned 64-bit frame count at eight. Its address
is eight-byte aligned. Accept rates 8000 through 48000, one or two channels,
and zero through 65536 frames. Read all 64 bits of the count. Validate the
prefix before accessing PCM or output.

On success, return v0=0 and v1=the original frame count. On an invalid prefix,
return v0=1 and v1=0 without PCM/output access or caller writes. Zero frames
succeed without PCM/output access; those two pointers need not be valid then.

Each signed 16-bit sample uses little-endian byte order. Preserve every sample's
bits. A frame contains one sample per channel. Reverse the frame order, retaining
channel order inside each frame. The selected PCM length is twice channels times
frames. PCM and output can be byte-unaligned; they are disjoint or exactly equal.
Other partial overlap is outside this contract. The prefix is disjoint from both.

Among caller-owned objects, read only the prefix and selected PCM/output ranges,
and write only selected output. Preserve all other caller bytes, including guard
bytes and the prefix. Your private workspace, constants and gaps remain available.
The checks observe accesses as well as final bytes. Preserve full s0–s7, fp and
sp, and return through the full 64-bit incoming ra. Incoming sp is eight-byte
aligned. Produce no console output. There is no exact loop, scratch-register,
stack-frame-size or instruction-count requirement beyond the declared finite
execution allowance.

Use Check this program in MARS Learning. The seven neighboring JSON files are
public native assessment data; editing their extracted copies does not change
retained lesson expectations. A final recheck covers all 35 cases after edits.
Original public CLI checks remain at revision/labs/checks/wav-reverse-frames.kts
in the book repository. Instructor-only solutions and additional checks are not
part of this student distribution.
