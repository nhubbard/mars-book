# Decisions and repetition

Read the revised main book’s decisions and repetition chapter and its written
lab. Use MIPS64 Release 6, Default memory and little-endian byte order. The entry
file is the book’s worked remaining-count program. Its glyph table begins at
0x10010000; RIGHT_DISPLAY is the separate device address 0x00000000ffff0010.

The native lesson compares three unchanged source files: range-counter.asm
selects all sixteen entries, ranges/empty.asm selects no entries starting at
sixteen, and ranges/tail.asm selects entries fourteen and fifteen. Each Debug
activity opens its own file. Start Debug for the selected file, rather than
reusing another file’s saved launch configuration. Stop the active session before
starting the next one. Source pointers mark the next instruction, not its effect.

Use the named breakpoints and Step Over instructions in Learn. Register checks
verify observations at those boundaries. Inspect Digital Lab as well, but do not
infer the number or order of writes from its final glyph or redraw timing. A Run
with no console output establishes normal exit and an empty transcript; the
independent checker observes the actual port stores and bounded reads.

For the independent task, edit independent/end-pointer.asm. Its exit-only body
is intentionally incomplete. The public cases initialize the input block in a
fresh machine for each check, so read the supplied data rather than replacing it
with fixed values. Read independent/README.md for the exact layout and scope of
memory observations. Equivalent correct implementations can pass the behavior
checks; explain the end-pointer reasoning as part of the written exercise.

Edits invalidate earlier execution evidence for that file. Checks use separate
machines and retain your active simulator state. Instructor settings can control
access, prerequisites and hint release. Complete the written lab’s deliberate
fault investigations and explain what changes at each failed boundary.

This is a development edition. Automated CLI qualification, native IDE walkthrough
and publication capture status are recorded separately. A representative learner
has not reviewed this unit.
