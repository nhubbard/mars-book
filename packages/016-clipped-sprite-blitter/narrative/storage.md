# Separate dimensions from storage

Width describes how many pixel cells belong to a row. Stride describes the distance between the starts of adjacent rows. The default framebuffer has width eight and stride sixteen words. Its active row occupies 32 bytes; its next row begins 64 bytes later. The eight intervening words are padding, outside the logical image. The sprite has width five and stride seven, so its rows begin 28 bytes apart.

For a base address P, a row r, a column c, and word stride S, the pixel address is P + 4(rS + c). The row product selects a row start; the column term selects a cell within that row. Source and destination need separate strides even when their visible copy rectangles have the same dimensions. Clipping changes the first selected row and column and the number of selected cells; it does not repack either allocation.

The descriptor's address field occupies eight bytes, so load it with `ld`. Its dimensions and stride occupy words and are small nonnegative integers, so `lw` is suitable. Multiplication of the bounded row index by the bounded byte stride fits in a word here. Add that offset to the full address with `daddu`; a small offset does not justify truncating the base pointer.
