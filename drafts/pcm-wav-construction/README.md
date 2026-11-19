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

Metadata begins as a valid 16-byte prefix for bound/filter/encode and as a 64-byte a5
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

This is a native reading and safe-input preparation milestone. The earlier private
complete implementations have been verified, but they are not these cumulative starter
prefixes. Execution verification of all completed cumulative bodies, guided-check export,
completed public feedback and packaging are pending. Intended checkpoint observations
must not be advertised as executed student checkpoints yet. The four capture plans have
no actual IDE pixels or coordinate receipts. Remote progression/hint enforcement is
also pending. The substantial prose rewrite is deferred until all 35 chapter drafts.
