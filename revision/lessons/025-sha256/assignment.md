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

The eight groups contain all 128 original cases plus seventeen construction
prefix cases. The final groups include split calls, two interleaved contexts,
reinitialization and changing file paths in one simulator. A new call must use
its supplied context and input. On a later read failure, an output may already
contain a successful digest rather than untouched poison bytes; preserve it.

## Use the caller to inspect your completed functions

After the function checks pass, follow the inline instructions to change only
`mode` in your existing source. Mode zero hashes the original bytes `abc` through
init, update and final. Mode one uses the file path. Debug at `main_done` before
the status store and `results_ready` afterward. Inspect the digest as bytes;
printing its hexadecimal representation is a separate operation.

The file observations install a deterministic read-only `fixture.bin`. Open
returns its actual descriptor; the wrapper must use that value rather than
assuming three. A positive short read contributes only its returned prefix and
requires another read. Zero permits finalization. A negative read requires a
close and status two, while preserving all output bytes. The successful and
failing observations use fresh launches; the reuse assessment separately verifies
those obligations across successive calls in one session.

Finish each Debug process before another launch or source edit. A normal Run has
an empty console because this caller stores results rather than printing them.
Use the checked state transitions and digest bytes to explain correctness; an
empty console alone establishes neither.
