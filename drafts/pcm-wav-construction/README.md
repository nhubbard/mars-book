# PCM WAV cumulative construction draft

The native manuscript is `drafts/pcm-wav.tex`, composed directly from includes under
`includes/chapters/031-pcm-wav`. This directory has 55 unfinished authoring regions
across 64 reading steps, 64 quizzes, two progressive hints per step and 65 exact
observation recipes. Quiz keys and complete implementations remain private.

Run `python3 prepare.py s01-1 --source working.asm --output observation.asm` from this
directory, using an existing working copy and a fresh output name. Preparation changes
only data declarations and caller controls; the entire text section stays byte-for-byte
unchanged. Keep supplied region markers, data boundaries, scalar controls, routes and
frames. Mode order is bound, parse, filter, encode, load, save, pipeline, kernel.

Loader and pipeline observations begin with a sentinel source arena; their incoming
file bytes are supplied separately by the file fixture. Metadata begins as a valid 16-byte prefix for bound/filter/encode and as a 64-byte a5
sentinel for parse/load/save/pipeline/kernel. Filter input is raw interleaved PCM in
source. Encoder input is raw PCM in samples. Parser input is a complete file in source.
Kernel mode uses a seven-history-plus-new-samples window in kernel_window. Prefix
frames and byte controls retain all 64 bits; rate/channel controls retain all 32 bits.

Development status 4 with zero count identifies an unfinished WAV body. It is outside
completed statuses 0..3 and may follow partial output or accepted file bytes. The raw FIR
entry instead returns processed count in v0 and clips in v1; its unfinished return is
minus one and zero. The supplied wrapper checks the raw processed count before using
kernel results. Every after-label owns a separate nop; stop before the named checkpoint.

Recipe file settings describe the grader's separate in-memory files, short transfers,
errors, permissions and descriptors. Preparation alone does not install those in an IDE
launch or create host files. Use `../pcm-wav-fixtures/fixture.py input.wav --fixture trace`
for an ordinary trace file, or select listen for the two-second run. Its output must be
fresh. Run the pipeline in the directory containing that input.wav.

The private cumulative checks now verify all 65 recipes at both address layouts:
254 direct/caller checkpoint executions, including continuation through unfinished
regions and exact intermediate arrays, private state and descriptor ownership. Step
s01 observes the caller only; the earlier FIR observation uses function_return for a
direct invocation and kernel_return before the caller converts the raw kernel count.
Pipeline observations use the caller only. These boundaries are recorded separately.

Both complete private implementations pass 2744 completed-interface executions. Fifteen
assembled fault controls are rejected, and ten explicit nested-development cases check
propagation through bound, FIR and parser calls. Twelve actual MIPS output files decode
independently; four real host-file round trips pass and malformed input preserves old
outputs. The earlier prototype performance figures retain their original source and
measurement boundaries; the new construction contains additional routes and guards.

Public feedback exports and chapter distributions remain pending. The four capture
plans have no actual IDE pixels or coordinate receipts. Remote progression/hint
enforcement and independent accessibility validation are also pending. The substantial
prose rewrite remains deferred until all 35 chapter drafts exist.
