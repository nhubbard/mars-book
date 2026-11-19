# Encode one full video frame

This stage develops `video_encode_full` through steps 57–68. It emits one full MLV1
record from 4096 native `0x00RRGGBB` words, then commits the previous image and next
sequence. A record is not yet a complete animation file. Delta selection and file
framing follow this stage.

Use `attach.py --source seek-work.asm --output encoder-work.asm` to retain earlier
student instructions and add twelve unfinished regions. The new caller uses generated
native colors, encodes a full record and decodes those bytes again. No host video
file is needed for this demonstration. Prepare an observation with
`prepare.py s60-1 --source encoder-work.asm --output observe.asm`. Preparation changes
only source data and controls; all output helpers refuse existing files.

There are twelve quizzes, twenty-four hints and twenty-four observation recipes.
Use the named checkpoints while constructing the code. Partial development status
four differs from completed failure one: completed failure must not store output,
previous-image or context bytes, even for an invalid last color. The native chapter
states the complete context and memory-ownership contract. The previous image is
committed output history, not an arbitrary display frame.

`fixtures.py gradient --output colors.json` exports numeric source words for separate
inspection. It does not encode video. Source words have target-native byte order;
record headers and RGB triples have the fixed wire order in either target setting.
The student distribution contains no completed encoder or quiz key. Remote progression
and actual light-theme IDE captures remain pending.
