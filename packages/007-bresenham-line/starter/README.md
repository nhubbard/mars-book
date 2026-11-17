# Integer line drawing starter

Use MIPS64 Release 6, the Default memory layout, and the supplied Bitmap Display header.
An 8 by 8 logical canvas is shown with 8 by 8 screen units. Each cell is one four-byte
word. The frame is first in static data, at 0x10010000, and occupies 256 bytes.

The endpoint block is four consecutive words: x0, y0, x1, y1. Each coordinate is zero
through seven. The separate color word is 0x00RRGGBB. Keep these inputs and their guards
unchanged. The starter exits without painting and intentionally fails the assignment.

Write in the student region before done. Keep experiments with another tie rule or a
larger canvas in separate files because they change this package's contract.
