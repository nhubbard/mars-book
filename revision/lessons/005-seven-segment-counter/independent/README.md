# Retain an exclusive end pointer

This is a preparatory independent exercise for the revised decisions-and-repetition
unit. The native lesson archive and walkthrough are still being developed; these
profiles and generated CLI checks do not yet constitute an importable lesson.

Use MIPS64 Release 6, Default memory, little-endian byte order and pseudoinstruction
support. Implement the main book's alternative loop: retain a current glyph pointer
and an exclusive end pointer, compare before the first load, and stop when they
are equal. Explain both the empty range and the last permitted entry. The automatic
checks verify observable behavior; your explanation must identify the pointers and
show why the stopping comparison is correct. They do not prove which loop design
you wrote or prescribe labels, registers or an exact instruction count.

The memory interface begins at 0x10010000. Sixteen glyph bytes occupy offsets
0 through 15; the guard word is at offset 16, first index at offset 20, count at
offset 24, and the second guard at offset 28. Input preconditions are first index
0 through 16 and count 0 through 16 minus first. The checker supplies this complete
32-byte block separately for each case, with its guard patterns and glyph designs.
Keep the interface addresses; you may rename every label. The starter's declarations
provide ordinary values for your own Run and Debug investigations.

For each selected glyph, write its byte exactly once to the right Digital Lab port,
0x00000000FFFF0010, in increasing table-index order. Issue no other stores in the
MMIO region 0xFFFF0000 through 0xFFFFFFFF. Do not modify any byte of the supplied
input block, even temporarily. A restored guard is still a write to protected data.
The program prints nothing and must exit normally within the 1000-instruction bound.
The bound catches nontermination; an exact instruction count is not a requirement.

For a nonempty range, table reads may use any width and may repeat, provided each
read remains wholly within the sixteen glyph bytes. An empty range must not read
the glyphs or their following guard at all. The read trace covers that twenty-byte
object, including accesses crossing its edges; it does not claim global protection
of every address. Other ordinary registers and scratch memory are your choice.

The ten public cases include empty ranges at zero and sixteen, the full range,
one-entry and tail ranges, repeated masks and an alternate table. Repeated masks
still require repeated stores. Their adjacent .cases.json gives every setup byte,
final byte, transaction sequence and permitted-read interval. The generated .kts
expresses the same contract using the existing MARS assignment DSL. No Python
emulates MIPS, and native assessment readers do not execute downloaded Kotlin.

A final F-shaped display cannot establish the preceding sequence. Use Debug to
follow the test, load, store and pointer update, then explain why each store is
selected and why a zero count prevents the first load. The public cases support
that reasoning; they do not establish the whole precondition domain.
