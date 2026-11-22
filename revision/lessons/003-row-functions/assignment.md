# Functions and saved state

Use the revised main book’s functions and stack chapter and its written lab.
The entry file is their complete reverse-rows.asm worked program. Use MIPS64
Release 6, Default memory and little-endian byte order. The driver aligns sp
before its first call; each callee still restores the pointer it received.

Learn provides five predictions, eight ordered Debug observations, an ordinary
Run and a separate callable-function exercise. Break at main_call and use Step
Into for the outer call. Set later named breakpoints as the activity requests.
Use Step Into for the final jrc; Step Over a call can hide its frame construction.
The next-instruction highlight and the lesson’s pointer have different purposes.

At pair_frame_ready inspect the 32-byte frame beginning at 0x7fffefd0. Padding
occupies offsets zero through seven; incoming s1, s0 and ra occupy the following
three eight-byte slots. Learn checks the named register observations; the written
lab explains the memory-slot inspection and fault investigations. An empty console
is not a result or preservation check.

The independent starter supplies two ordinary caller continuations and the leaf
helper. Replace the break body, keeping its public function interface. Read the
adjacent README.md before checking: the function cases set arguments and a return
link and execute to the continuation, without running main. Ordinary Run remains
useful for the supplied two-call example. Native checks use separate machines and
current unsaved editor text. Edits invalidate previous execution evidence.

This is a development edition. CLI qualification and native import/checkpoint
tests are recorded separately from the forthcoming complete IDE walkthrough and
publication captures. No representative learner review is claimed.
