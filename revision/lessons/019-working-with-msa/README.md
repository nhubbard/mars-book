# MSA native unit foundation

This directory is a working source and checking-data draft for Chapter 14,
original project 019. It is not yet an importable native lesson archive. The
existing directly edited LaTeX chapter and supplement remain authoritative.

`rgb-formats.asm` preserves the complete written RGB565 example. The five
`worked/count-*.asm` files change only the requested count to 0, 7, 8, 9 or 17.
Measured stops cover loaded windows, gathered channels, placed fields, the next
vector group, scalar tails, the returned count and caller publication. Native
lesson format 7 adds complete 128-bit register observations, using the same
immutable snapshot as the debugger. Lesson composition and exact archive import
are still pending; these source files alone do not prove the student IDE route.

`independent/rgb555.asm` and `independent/rgb555.kts` are unchanged copies of the
published starter and its 34 public checks. `rgb555.cases.json` expresses the same
33 count trials and complete caller in native function-profile format 8. Each
count trial uses both continuations, for 67 calls in total. A separate console
profile checks normal silent exit. The selected source and destination slices,
full surrounding buffers, saved registers, stack restoration and assembled
read/write policies remain observable. Equivalent algorithms are accepted;
there is no required instruction schedule or MSA instruction count.

Reproduce the checking data with `python3 tools/rgb555_lesson_cases.py --help`.
All 33 original Kotlin input constructions were independently exported and
compared. Private qualification accepts scalar, vector, halfword and wide-address
alternatives, including relocation, and rejects the starter and selected faults.
Unaligned Release 6 halfword loads/stores are valid: the historical fixture whose
filename calls its tail store a fault is an accepted equivalent implementation.
It is not used as evidence of fault detection.

Exact intermediate vector expectations apply to the worked program only. They
must not become restrictions on independent RGB555 solutions. Vector values use
32 hexadecimal digits, with the most significant digit first; lane zero occupies
the least significant bits. A memory byte listing is a different representation.

No new IDE walkthrough, screenshot capture, PDF rebuild, human learner review or
accessibility review is claimed for this foundation step. The previous verified
LaTeX figures remain unchanged.

The foundation passes 484 book-tool tests, 217 CLI tests and 1220 plugin tests.
Required plugin package line/branch coverage and the generated-parser ledger
checks pass. The vector observation implementation is committed as MARS
`923e114f`. Full regression XML and exact archive hashes are retained privately.
All 41 manually edited upstream files match the preservation baseline.
