# Follow video history and exclusive bounds

Read Chapter 25 and its written lab. This unit follows the complete supplied MLV1
codec through six bounded experiments, then asks you to implement one independent
image primitive. MLV1 is a local teaching format. The following H.263 chapters
address an actual interoperable video format.

Begin with `worked/xor-alias-tail.asm`. Answer predictions inline in MARS Learning,
then use ordinary Debug and breakpoints at the named labels. Inspect state before
executing the anchored instruction. Finish each process before starting the next
activity. If you backstep or alter state while exploring, start a fresh forward
Debug session to record the required sequence. A yellow source pointer identifies
a lesson location; it does not establish that the simulator has executed there.

Four experiments retain the actual supplied helper bodies and the static
fixtures they need. Their short callers appear first. The complete file/player
example remains in the book as an optional extension.
The other two sources are explicitly arithmetic models of deadlines and selected
coordinates. You do not need to rewrite or memorize the supplied helper bodies.
The required native observations need no host input, file path or Python command.

## Compare original words before interpreting pixels

The XOR experiment uses five words and exactly aliases output with the left
input. Inspect the full w2 vector and its 32-bit lanes at
`deltaKernel_video_group_counted`. The numerical differences are hexadecimal
80000000, zero, 10 and ffffffff. Three lanes differ. A high-bit-only difference
still counts: the criterion is nonzero, not signed-positive.

After returning from the helper, inspect the fifth word and retained guard at
`lesson_xor_result`. The scalar tail raises the changed count to four; the first
guard byte remains hexadecimal A5. Exact aliasing works because both input
vectors were loaded before their output store. It does not establish support
for arbitrary partial overlap. The following zero-length call uses unusable
pointers and succeeds before accessing them. Explain why a zero-length contract
can differ from a positive-length alignment contract.

## Separate checksum acceptance from publication

In `worked/record-publication.asm`, the first 27-byte delta record changes one
pixel, reaches the end with its final skip, and commits the next expected
sequence. At `lesson_record_committed`, identify the numerical first pixel,
expected sequence two, delta kind one and 16384 published image bytes.

The second record has sequence two and a correctly recomputed CRC over its first
twelve header bytes followed by its payload. Its last command is zero/zero.
At `lesson_record_before_retry`, compare the context sequence with the candidate
header and inspect that final command. At `lesson_record_rejected`, explain why
status one and count zero accompany the retained successful pixel and context.
A CRC can detect changed bytes without proving that commands advance or cover
exactly 4096 pixels. The implementation validates the complete command walk
before the first image store. The original codec checks cover full publication
and memory guarantees; one visible retained pixel is evidence only for that pixel.

`worked/late-record.bin` is the exact malformed, CRC-correct candidate embedded
in this experiment. It is supplied for inspection, not as an external input
that you must load before Debug.

## Include command and record overhead

In `worked/encoding-cost.asm`, the original image pair differs at pixels zero,
325, 326 and 4095. Those changes occupy three runs. Derive `4k + 3m` with three
commands and four changed pixels: 24 payload bytes. The sixteen-byte record
header makes forty bytes. Compare those quantities at `lesson_encoded`.

The successful encoder commits its current image as previous. The second call
encodes that same current image at the next nonperiodic sequence. It needs one
four-byte unchanged command, so the complete record is twenty bytes. At
`lesson_encoded_reuse`, explain what changed in the comparison history.

The caller then sets capacity to nineteen. At `lesson_encoding_rejected`, the
previously published length remains twenty and the next sequence remains three.
Capacity nineteen is an explicit caller input; failure does not restore that
input to its former value. Name each object's guarantee rather than promising
that all memory rolls back. Additional full records are valid MLV1 encodings;
this experiment's compact result is not the only valid encoder strategy.

## Reconstruct from admitted history

`worked/directory-seek.asm` indexes the embedded 24795-byte moving fixture.
Eight native directory offsets occupy 64 bytes. The file's key interval is four,
and its frame interval is forty milliseconds. An offset identifies a byte
position relative to the same admitted file base. It is not a pixel index or an
absolute address that survives arbitrary relocation.

The target-six seek begins with an unrelated destination image. At
`lesson_sought`, inspect target six, replay start four and three applied records.
Explain why records five and six are insufficient without the full image at
four. The written lab's additional-full fixture can start at five instead.
Neither strategy may assume that the destination already holds expected pixels.

A subsequent target eight is out of range. At `lesson_seek_rejected`, the last
successful result fields remain. A complete image-preservation claim requires
complete memory checks; the result fields alone cannot prove it. The ordinary
Run activity executes this same silent, embedded experiment. A normal exit
records process completion, not all codec correctness guarantees.

## Keep time arithmetic and geometry precise

`worked/anchored-deadlines.asm` models the player's arithmetic with explicit
readings. It does not call the host clock or measure frame presentation. For
anchor 1000 and interval forty, compare deadline 1040 with readings 1010 and
1035: waits thirty and five. A late reading 1047 gives lateness seven while the
next anchored deadline remains 1080. Eight intervals end at 1320, including the
final frame interval. The optional actual player and scripted-clock checks
remain in the written lab.

`worked/exclusive-coordinates.asm` examines only two selected positions, not the
independent function's complete scan. In a two-by-three image, indices one and
four give coordinates (1,0) and (0,2). Exclusive ends two and three enclose area
six even though only two words differ. The final stop compares a full native
width 4294967297 with its low word one. Validate dimensions before narrowing or
using them in address arithmetic.

## Write one continuing implementation

Implement only `video_change_bounds` in `independent/video-change-bounds.asm`.
Its README states the complete interface. Keep this same file through four
assessment stages: equality/aliases, single edges/row crossings, mixed/full
shapes, and boundaries/reuse. The final assessment checks all 182 original cases
on that completed source. Every stage assembles native data and your source in
both byte orders. The per-case budgets admit equivalent one- and two-pass
implementations; the checks do not require a private label or scratch register.

Edits can make earlier execution evidence stale. Follow MARS's renewal prompts;
quiz attempts and revealed hints remain saved. Guided/free-writing availability
and hint release follow the instructor's policy. Add an example of your own
with two sparse corners and predict its count, exclusive coordinates and area
before execution. Then repeat an unchanged pair to check that no old extrema
remain.

For optional host artifacts, the supplied `tools/fixture.py` creates the original
planned clips and PPM previews in a fresh directory. The written lab explains
the full codec's separate checks and player. Keep those files separate from this
bounds function's feedback. No instructor implementation is in this archive.
