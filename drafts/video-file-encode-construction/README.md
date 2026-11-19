# Encode and store a complete video file

Steps 84–101 add eighteen unfinished regions, eighteen quizzes and thirty-six
hints. Attach your preceding record encoder with `attach.py --source record.asm
--output file.asm`, then prepare an observation with `prepare.py s84-1 --source
file.asm --output observe.asm`. These helpers preserve your earlier instructions
and refuse existing output paths. `fixtures.py` exports numeric source frames;
it does not supply an encoder or quiz answers.

The native chapter defines both callable contracts. `video_encode_file` creates
one complete staged MLV1 file. The existing file arena and `file_encode_output`
are aliases, saving a second maximum-sized allocation; old file/index state is
invalid while encoding overwrites this storage. Packed source frames are separate.
A failure retains caller context but may change staging and working pixels.
Equivalent valid full/delta choices are accepted for completed grading.

`video_store` transports an admitted positive byte extent, with requests no larger
than 4096 and progress based on actual positive results. The current MARS write
service reads one lookahead byte, so retain a readable guard after the source;
that guard is not transferred. It closes its acquired
descriptor exactly once. It does not validate the container or guarantee atomic
replacement: truncate-write failure can leave a prefix. The provided caller
encodes, indexes, seeks, writes and reloads. Playback must validate the newly
loaded bytes before using a directory. Final playback, complete chapter packaging,
actual automated light-theme IDE figures and independent accessibility review
remain pending. H.263 remains planning only until chapters 33 and 34 are complete.
