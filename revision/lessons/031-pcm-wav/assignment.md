# Follow the audio across its boundaries

Read Chapter 23 of the main book and “Follow the audio across its boundaries”
in the written lab. These native activities use separate worked experiments so
that debugger observations do not require edits to your continuing solution.
The examples include the complete public WAV helpers. They are teaching code,
not functions you must reconstruct for this independent exercise.

Start with `worked/chunk-offsets.asm`. Work through the inline predictions and
ordinary Debug launches in MARS Learning. The named labels mark the next
instruction, so inspect state before executing that instruction. Finish each
process before opening the next activity. The full Debug sequence must occur in
one unmodified forward session; after experimenting with edits or backsteps,
start a fresh session to record it. Use free-writing mode when released by your
instructor. Required checks and graduated hints follow the course policy.

The parser experiment first reads a valid 82-byte file held in memory, then
changes a trailing chunk length without extending the input. It fills the
metadata with a5 before the failing parse. Compare all four eight-byte groups:
a valid earlier data chunk cannot justify publishing a partial record.
The filter experiment keeps signed left and right histories separate. Derive
the first two frames on paper; use the remaining frames to check the same rule.
The encoder experiment first offers 59 bytes for a 60-byte image, then retries
with sufficient capacity. Identify which fields describe complete file bytes,
PCM bytes and bytes per second before comparing their values.

Write only the missing `wav_reverse_frames` entry in
`independent/wav-reverse-frames.asm`. Its neighboring README specifies the public
interface. The supplied helpers are included directly in this standalone source;
there is no preparation command or include dependency. The twelve mono cases,
twelve stereo cases, and eleven boundary/reuse/pipeline cases retain the original
35 public cases. The final full check rechecks this source after later edits.

The full-pipeline assessment supplies virtual input/output files and constrained
transfers. It does not require files in your working directory. A normal Run of
the independent source selects its default bound calculation and terminates
silently; that Run does not exercise your reversal or save a file. Use the final
native assessment to test the integrated reversal, and use the written lab's
fixture instructions for an additional host-file experiment.

Keep your implementation and reasoning together. For an additional input, write
the original frame pairs, reverse their order on paper and predict the exact
output bytes before running. Explain how the same argument covers zero frames,
one frame and a middle frame that stays in place.
