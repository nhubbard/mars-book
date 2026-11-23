# Carry a message through SHA-256

Read the SHA-256 companion in the written lab first. The main-book chapters on
functions, files and word transformations supply the prerequisites. A digest
summarizes a counted sequence of bytes; a usable implementation must also carry
unfinished input correctly between calls. Keep those two responsibilities in
view while building the compression arithmetic.

## Read bytes and words according to their interfaces

Begin with `worked/schedule.asm`. The three original bytes are `61 62 63`.
This example already includes the marker and length for their padded block.
Its first four bytes therefore decode to numeric word `61626380`. The byte
loads and shifts give the first byte the most significant position. An ordinary
little-endian word load from the input would mean something different.

Once decoded, the schedule is an array of numeric words. The store of this word
uses ordinary simulator byte order, so memory contains `80 63 62 61`. Explain
both observations before calling either one a byte-order error. The experiment
expands only W16 through W19. Derive their predecessor addresses from a destination
pointer, then implement all 64 words in the independent source.

## Preserve the old round state until its last use

`worked/round.asm` performs one round from the initial state and the first
padded `abc` schedule word. It computes T1 and T2 before changing a through h.
New a consumes T1 and T2; new e consumes old d and T1. The remaining positions
shift old values into their new roles. Inspect `round_state` after these moves,
and distinguish it from both the unchanged input state and a completed digest.

The independent compression must perform all 64 rounds and then add the original
input state words. Adding the initial constants on every call would fail once
an earlier block had changed that state. The scalar register display is 64 bits;
word additions and rotations still operate at 32-bit width and may sign-extend
their results in that display.

## Make unfinished input an explicit state

`worked/fill-block.asm` begins with 63 pending bytes and supplies two more.
It counts the original two-byte request once, then copies the one byte that fits.
At `block_ready`, used is temporarily 64 and one source byte remains. Compression
has not happened. This is a deliberately unfinished internal boundary; the
program must not be mistaken for an implementation returning a valid context.

A real update compresses that full block before reusing its buffer, resets used,
and copies the remaining byte. Its returned state describes all complete blocks,
its total counts every original byte exactly once, and its active tail contains
what has not yet been compressed. The prefix checks let you verify that returned
state before implementing finalization. Inactive buffer and workspace bytes
need not match a worked implementation.

## Construct the suffix before asking compression to consume it

Compare `worked/padding-55.asm` and `worked/padding-56.asm`. A 55-byte tail leaves
one position for the marker and eight for the length in one block. A 56-byte tail
does not. Follow the marker, chosen final size, start of the length field and
completed bytes. These experiments construct padding; they do not calculate a
hash or claim that padding bytes are original input.

`worked/padding-high-length.asm` supplies a synthetic total of `000000010000003f`
bytes and a 63-byte unfinished tail. The program has not read a multi-gigabyte
message. The checkpoint lets you inspect the full-width shift and the resulting
big-endian suffix `00000008000001f8`. A word-width shift could appear correct on
small examples while losing these high bits.

## Continue in one independent source

Use `independent/sha256.asm` for every implementation group. Its README specifies
the six interfaces, context layout, caller ownership and file statuses. Keep your
functions in this file as you progress through the staged checks. The worked
sources are short observation experiments, not replacement start/end templates.
Use graduated hints when a boundary is unclear; use free writing when you prefer
to derive the implementation from the complete contracts.

The first eight groups contain all 128 original cases plus seventeen construction
prefix cases; a final group rechecks the original cases on the completed source. The final groups include split calls, two interleaved contexts,
reinitialization and changing file paths in one simulator. A new call must use
its supplied context and input. On a later read failure, an output may already
contain a successful digest rather than untouched poison bytes; preserve it.

## Separate file ownership from hash arithmetic

`worked/serialize.asm` is given the final numeric state for `abc` and writes its
32 big-endian digest bytes. It does not calculate that state. Compare the numeric
input word `ba7816bf`, its native stored bytes, and the output bytes `ba 78 16 bf`.
Your finalization must obtain the correct state first, then serialize it.

`worked/read-prefixes.asm` counts file bytes and publishes one numeric count only
at EOF. It supplies a smaller example of file ownership without a hash loop.
The successful observation returns count three despite two-byte read limits.
The error observation returns status two and preserves the original output word.
Your SHA wrapper applies the same ownership rule to all 32 digest bytes.

These two Debug activities use the same unchanged public source and different
deterministic read-only fixtures. Open returns the actual descriptor; the helper
uses that value rather than assuming three. Positive reads contribute their
returned prefix, zero permits publication, and negative results require cleanup
without publication. No host file needs to be created for these observations.

## Recheck the completed source after construction

Edits to later functions or shared helpers can make an earlier check result stale.
The final assessment therefore runs all 128 original cases again on the completed
independent source. It reuses the existing profiles; the additional update-only
prefix cases remain construction feedback. A normal Run is silent because the
supplied caller stores results. Silence alone does not establish correctness.

You can also Debug your own implementation after completing the lesson. Mode
zero hashes the original bytes `abc` through init, update and final; mode one
selects files; the supplied mode two selects schedule work. Edit only the existing
mode word, save, and launch afresh. `main_done` is before the status store and
`results_ready` is after it. Re-run the final assessment after further edits.

Finish each Debug process before another launch or source edit. Keep an explanation
of the earliest boundary that distinguishes byte-order, state, length and ownership
errors. Use that explanation to investigate a new input beyond the finite checks.
