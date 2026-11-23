# Native sequence to compose and exercise

This plan connects the existing LaTeX teaching sequence to measured debugger
states. It is not the lesson itself and supplies no independent solution.
Predictions should appear beside the relevant source. Each explanation should
name what to inspect, explain its relation to the source, and ask the student to
account for the result before moving on.

1. **Derive one packed pixel.** Begin with the first RGB triple, 123,45,67.
   Work through discarded low bits and field positions. Ask for the RGB565
   numerical word and then its two little-endian bytes as separate reasoning
   tasks. A changed preview alone cannot establish which operation was wrong.
2. **Read a vector through two views.** Explain that byte and halfword rows show
   the same 128 bits. Lane zero is the low end of the register; its position in a
   printed hexadecimal value differs from the order of bytes at rising memory
   addresses. Have students identify a red lane and its zero high byte.
3. **Account for both input windows.** At the first loaded stop, identify the
   16-byte windows beginning at input offsets zero and eight. Their overlap
   covers 24 selected bytes without requiring a 32-byte source range. Ask which
   window supplies the final pixel's channels before deriving controls.
4. **Trace the gather rather than memorizing a mask.** Relate selectors to the
   instruction's rightmost source and its second source. Follow one selector
   for an early pixel and one for a late pixel; explain zero-producing selectors
   independently. At gathered, inspect all eight red halfwords and the original
   saved control. A matching first lane does not establish a correct gather.
5. **Place fields, then combine.** At fields, compare the three shifted channels
   and the combined word. Connect the first halfword back to the scalar
   derivation. Explain why the zero high byte was needed before shifting.
6. **Follow the next group.** Revisit loaded and gathered for the second group.
   w4 still held the preceding packed results at entry; it must receive fresh
   controls before the next gather. w16 preserves the reusable control. Ask why
   a routine that looks correct for eight pixels can fail for sixteen.
7. **Separate return from publication.** At pack_ready, the routine has returned
   its count and packed output, but packed_count is still the caller's sentinel.
   At unpack_ready, packed_count is published and expanded_count is still its
   sentinel. At preview_ready, both results are published. Relate the quantized
   expanded triple to discarded bits; expansion cannot recover lost precision.
8. **Establish bounds around the vector width.** Use complete source variants
   for counts zero, seven, eight, nine and seventeen. Predict vector groups and
   remainder before Debug. At the tail, connect remaining count and advanced
   pointers to selected intervals. Count zero accesses neither selected buffer;
   seven uses only the scalar path; eight has no scalar pixels; nine and
   seventeen combine complete groups with one scalar pixel. Inspect returned
   counts and caller publication as well as the branch decision.
9. **Transfer the reasoning to RGB555.** State its different green width and
   zero top bit before editing the independent starter. Derive the first result
   and the white-pixel limit. Invite a scalar baseline, a vector strategy and a
   tail, while accepting equivalent correct algorithms. The 34 public cases
   check interface behavior, offsets, untouched bytes and restoration. Their
   bounds are evidence for these inputs, not a proof over every possible pixel.
10. **Use failure as a debugging question.** On an incorrect result, ask whether
    fields, byte order, vector group reuse, tail bounds or caller publication is
    responsible. Offer graduated hints about the failed contract and selected
    interval. Preserve edits and retries; do not replace student work with a
    reference body. Finish with an explanation of the chosen strategy and the
    evidence that supports correctness.

Before acceptance, bind these stages to exact source anchors and measured states,
validate the exact student archive, then exercise inline predictions, all Debug
stops, Runs, an incorrect starter, an equivalent correct unsaved implementation,
source edits, retries and progress after reopening in the IDE. Update the native
LaTeX lab route by hand after the interaction is established. Capture automation
stays in the separate dependent authoring plugin.
