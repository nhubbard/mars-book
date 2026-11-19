# Choose and encode a video record

This stage adds steps 69–83: fifteen unfinished regions, fifteen quizzes, thirty
hints and thirty-three observation recipes. Attach your full-encoder working file
and earlier XOR working file with `attach.py --source encoder.asm --kernel-source
xor.asm --output record-encoder.asm`. Local kernel labels are namespaced; public XOR
entries remain callable. The guided importer accepts ordinary identifier labels
and preserves instruction names, registers, quoted constants and comments.

The native chapter defines the 96-byte context, immutable inputs, scratch work and
success-only publication. The provided `crc.asm` is an independently supplied library
for the earlier checksum recurrence, not a completed record encoder. Prepare with
`prepare.py s74-1 --source record-encoder.asm --output observe.asm`. Controls replace
frame-pair data and context fields only. Output commands refuse existing files.

The illustration uses maximal zero/nonzero runs, chooses full on a payload-size tie,
and admits the selected record before writing. General completed grading accepts
valid alternative full/delta choices and command boundaries. With capacity at least
12,304, a valid input must succeed; smaller buffers may fail if the selected record
does not fit. Such failure does not prove that no possible encoding could fit.
Required periodic full frames remain mandatory. Guided observations check the named
illustrated policy, rather than imposing its private registers on free writing.

Scratch may change on a completed failure; record output, previous image and final
context must not receive stores. A full boundary ignores old previous-image contents,
but still admits its pointer/capacity. For nonperiodic frames, previous colors must
have zero high bytes. The caller saves an old decoder image before encoding, so the
round trip verifies the actual prediction chain. No completed encoder or quiz key is
included here. Complete-file encoding, timed playback and final distributions follow.
Actual light-theme IDE figures and remote progression remain pending.
