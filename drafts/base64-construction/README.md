# Base64 cumulative construction

This draft accompanies the directly edited native chapter at drafts/base64.tex.
The LaTeX includes control publication order. The metadata describes exercises and
observations; it never regenerates edited prose. The sequence has 43 steps, 37 authored
regions, 52 recipes, 43 quizzes and 86 hints. Private completed implementations and
quiz keys stay in the separate instructor repository.

Retain the BEGIN/END markers and replace only the named bodies in main.asm. The supplied
frames and restore paths preserve full-width saved registers. An unfinished routine
returns development status 4 with zero written bytes. The character classifier instead
returns -2, since numerical 4 is a valid sextet; -1 means an invalid character. Supplied
nested-call guards propagate unfinished work rather than misreporting malformed input
or publishing an incomplete conversion. These are construction signals, outside the
completed interface’s statuses 0–3.

Prepare an observation with `python3 prepare.py s05-1 --source /path/to/main.asm
--output /path/to/new-observation.asm` (on one command line). Use the cumulative source
through that edit. Preparation changes only the selected source/destination arenas and
caller controls. It preserves authored regions and refuses an existing output. Counts
and capacities use canonical unsigned 64-bit decimal strings; bytes use lowercase hex.
Stop at the recipe’s callerCheckpoint when running main. A function_return checkpoint
means the corresponding caller return label; direct-call checks use their own sentinel.

Checkpoint registers and private vector layout describe this guided implementation.
The completed-function contract accepts equivalent algorithms, registers and schedules.
Exact in-place decoding is permitted; encoding requires disjoint buffers. No routine
adds a NUL terminator. Read the native contract before interpreting output as text.

Four light-theme capture plans describe intended observations. No actual screenshots
or capture receipts exist for this chapter. Student/instructor exports and remote
progression remain pending. Substantial prose editing remains deferred until all
35 chapter drafts exist, as requested by the author.
