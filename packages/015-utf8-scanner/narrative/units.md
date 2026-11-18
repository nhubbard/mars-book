# Choose the unit being counted

UTF-8 uses sequences of one through four bytes to encode Unicode scalar values. A scalar is a Unicode code point from U+0000 through U+10FFFF excluding the surrogate range U+D800 through U+DFFF. A byte count and a decoded scalar count therefore need not agree. The scanner increments its scalar count only after accepting an entire encoding, never merely because it has seen a leading byte.

A scalar count is also not a count of displayed glyphs or user-perceived characters. For example, U+0065 followed by U+0301 contains two scalars; U+00E9 contains one. Both can represent an accented e, while default grapheme segmentation keeps the base and combining mark together. Grapheme boundaries have their own specification.[^1] This exercise neither segments text nor normalizes those sequences.

The default fixture contains these eleven hexadecimal bytes:

> `41 e2 82 ac f0 9f 99 82 00 c3 a9`

They encode U+0041, U+20AC, U+1F642, U+0000, and U+00E9: five complete scalars. The zero byte counts as U+0000. It is not a string terminator in this length-based interface. Code point labels name the values without requiring a particular font to display their glyphs in a debugger or in this book.

UTF-8’s code units are bytes, so a multi-byte sequence is not interpreted by loading a native little-endian word. Inspect consecutive bytes in their supplied order. The default MARS word byte order affects the state object’s words, not the order of bytes in an encoded sequence. An input chunk can end after any byte, including inside the encoding of one scalar.

The fixture declares explicit byte values. Console text settings do not decode those bytes for this function. A byte load supplies the stored bits; the scanner assigns their UTF-8 meaning.

[^1]: [Unicode Standard Annex 29, revision 47, Section 3](https://www.unicode.org/reports/tr29/tr29-47.html).
