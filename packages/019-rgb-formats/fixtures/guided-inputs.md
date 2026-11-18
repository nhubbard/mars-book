# Reproduce a construction observation

Start from the supplied caller and the code completed through the indicated step.
Set the count word to the scene's count. Replace only the first 3*count values of
rgb_a's existing 96-value byte initializer with the listed RGB bytes. Keep its
remaining values, following `.space 32`, guards and other data declarations. This
preserves both allocation sizes and the input/output arrangement. The count decides
which bytes are logical input; later initializer values are outside that slice.
For a byte above 127, use value minus 256 in the signed initializer: 247 is -9,
128 is -128, and 255 is -1. Unsigned byte loads recover their component values.

Run or debug main and pause before the scene's pack_ready or unpack_ready instruction.
For an expansion scene, main first packs the provided RGB preimage into exactly the
packed bytes being inspected, then calls expansion. The pack loop is already complete
before the expansion edits begin. The listed RGB preimage may differ in discarded low
bits from another original with the same RGB565 value; its expansion is unchanged.

A checkpoint is before the caller's count store. Temporary registers and vector lanes
still hold the function's construction values because the leaf return is jr and nop.
Partial stages can return the original count before output is complete. Compare only
the observation stated for that step. Do not apply final behavioral grading to a
partial function. Some steps list multiple scenes to compare zero, short, exact-group,
tail and repeated-group behavior. The accompanying JSON contains the same inputs for
a future lesson runner; it does not control the native book composition.

## s04-1: count 0, pack_ready

RGB bytes: None; count is zero.

## s05-1: count 1, pack_ready

RGB bytes: 123, 45, 67

## s06-1: count 1, pack_ready

RGB bytes: 123, 45, 67

## s07-1: count 1, pack_ready

RGB bytes: 123, 45, 67

## s08-1: count 1, pack_ready

RGB bytes: 123, 45, 67

## s09-1: count 1, pack_ready

RGB bytes: 123, 45, 67

## s10-1: count 1, pack_ready

RGB bytes: 123, 45, 67

## s11-1: count 1, pack_ready

RGB bytes: 123, 45, 67

## s11-2: count 2, pack_ready

RGB bytes: 123, 45, 67, 255, 128, 7

## s12-1: count 0, unpack_ready

RGB bytes: None; count is zero.

## s13-1: count 1, unpack_ready

RGB bytes: 123, 44, 66

## s14-1: count 1, unpack_ready

RGB bytes: 123, 44, 66

## s15-1: count 1, unpack_ready

RGB bytes: 123, 44, 66

## s16-1: count 1, unpack_ready

RGB bytes: 123, 44, 66

## s17-1: count 1, unpack_ready

RGB bytes: 123, 44, 66

## s18-1: count 1, unpack_ready

RGB bytes: 123, 44, 66

## s18-2: count 2, unpack_ready

RGB bytes: 123, 44, 66, 255, 255, 255

## s19-1: count 7, pack_ready

RGB bytes: 123, 45, 67, 255, 128, 7, 0, 255, 128, 1, 2, 3, 248, 252, 248, 31, 63, 95, 200, 100, 50

## s19-2: count 8, pack_ready

RGB bytes: 123, 45, 67, 255, 128, 7, 0, 255, 128, 1, 2, 3, 248, 252, 248, 31, 63, 95, 200, 100, 50, 127, 64, 255

## s20-1: count 8, pack_ready

RGB bytes: 123, 45, 67, 255, 128, 7, 0, 255, 128, 1, 2, 3, 248, 252, 248, 31, 63, 95, 200, 100, 50, 127, 64, 255

## s21-1: count 8, pack_ready

RGB bytes: 123, 45, 67, 255, 128, 7, 0, 255, 128, 1, 2, 3, 248, 252, 248, 31, 63, 95, 200, 100, 50, 127, 64, 255

## s22-1: count 8, pack_ready

RGB bytes: 123, 45, 67, 255, 128, 7, 0, 255, 128, 1, 2, 3, 248, 252, 248, 31, 63, 95, 200, 100, 50, 127, 64, 255

## s23-1: count 8, pack_ready

RGB bytes: 123, 45, 67, 255, 128, 7, 0, 255, 128, 1, 2, 3, 248, 252, 248, 31, 63, 95, 200, 100, 50, 127, 64, 255

## s24-1: count 8, pack_ready

RGB bytes: 123, 45, 67, 255, 128, 7, 0, 255, 128, 1, 2, 3, 248, 252, 248, 31, 63, 95, 200, 100, 50, 127, 64, 255

## s25-1: count 8, pack_ready

RGB bytes: 123, 45, 67, 255, 128, 7, 0, 255, 128, 1, 2, 3, 248, 252, 248, 31, 63, 95, 200, 100, 50, 127, 64, 255

## s26-1: count 8, pack_ready

RGB bytes: 123, 45, 67, 255, 128, 7, 0, 255, 128, 1, 2, 3, 248, 252, 248, 31, 63, 95, 200, 100, 50, 127, 64, 255

## s27-1: count 8, pack_ready

RGB bytes: 123, 45, 67, 255, 128, 7, 0, 255, 128, 1, 2, 3, 248, 252, 248, 31, 63, 95, 200, 100, 50, 127, 64, 255

## s28-1: count 8, pack_ready

RGB bytes: 123, 45, 67, 255, 128, 7, 0, 255, 128, 1, 2, 3, 248, 252, 248, 31, 63, 95, 200, 100, 50, 127, 64, 255

## s29-1: count 8, pack_ready

RGB bytes: 123, 45, 67, 255, 128, 7, 0, 255, 128, 1, 2, 3, 248, 252, 248, 31, 63, 95, 200, 100, 50, 127, 64, 255

## s29-2: count 9, pack_ready

RGB bytes: 123, 45, 67, 255, 128, 7, 0, 255, 128, 1, 2, 3, 248, 252, 248, 31, 63, 95, 200, 100, 50, 127, 64, 255, 7, 3, 1

## s29-3: count 16, pack_ready

RGB bytes: 123, 45, 67, 255, 128, 7, 0, 255, 128, 1, 2, 3, 248, 252, 248, 31, 63, 95, 200, 100, 50, 127, 64, 255, 67, 123, 45, 7, 255, 128, 128, 0, 255, 3, 1, 2, 248, 248, 252, 95, 31, 63, 50, 200, 100, 255, 127, 64

## s30-1: count 7, unpack_ready

RGB bytes: 123, 44, 66, 255, 130, 0, 0, 255, 132, 0, 0, 0, 255, 255, 255, 24, 60, 90, 206, 101, 49

## s30-2: count 8, unpack_ready

RGB bytes: 123, 44, 66, 255, 130, 0, 0, 255, 132, 0, 0, 0, 255, 255, 255, 24, 60, 90, 206, 101, 49, 123, 65, 255

## s31-1: count 8, unpack_ready

RGB bytes: 123, 44, 66, 255, 130, 0, 0, 255, 132, 0, 0, 0, 255, 255, 255, 24, 60, 90, 206, 101, 49, 123, 65, 255

## s32-1: count 8, unpack_ready

RGB bytes: 123, 44, 66, 255, 130, 0, 0, 255, 132, 0, 0, 0, 255, 255, 255, 24, 60, 90, 206, 101, 49, 123, 65, 255

## s33-1: count 8, unpack_ready

RGB bytes: 123, 44, 66, 255, 130, 0, 0, 255, 132, 0, 0, 0, 255, 255, 255, 24, 60, 90, 206, 101, 49, 123, 65, 255

## s34-1: count 8, unpack_ready

RGB bytes: 123, 44, 66, 255, 130, 0, 0, 255, 132, 0, 0, 0, 255, 255, 255, 24, 60, 90, 206, 101, 49, 123, 65, 255

## s35-1: count 8, unpack_ready

RGB bytes: 123, 44, 66, 255, 130, 0, 0, 255, 132, 0, 0, 0, 255, 255, 255, 24, 60, 90, 206, 101, 49, 123, 65, 255

## s36-1: count 8, unpack_ready

RGB bytes: 123, 44, 66, 255, 130, 0, 0, 255, 132, 0, 0, 0, 255, 255, 255, 24, 60, 90, 206, 101, 49, 123, 65, 255

## s37-1: count 8, unpack_ready

RGB bytes: 123, 44, 66, 255, 130, 0, 0, 255, 132, 0, 0, 0, 255, 255, 255, 24, 60, 90, 206, 101, 49, 123, 65, 255

## s38-1: count 8, unpack_ready

RGB bytes: 123, 44, 66, 255, 130, 0, 0, 255, 132, 0, 0, 0, 255, 255, 255, 24, 60, 90, 206, 101, 49, 123, 65, 255

## s39-1: count 8, unpack_ready

RGB bytes: 123, 44, 66, 255, 130, 0, 0, 255, 132, 0, 0, 0, 255, 255, 255, 24, 60, 90, 206, 101, 49, 123, 65, 255

## s40-1: count 8, unpack_ready

RGB bytes: 123, 44, 66, 255, 130, 0, 0, 255, 132, 0, 0, 0, 255, 255, 255, 24, 60, 90, 206, 101, 49, 123, 65, 255

## s41-1: count 8, unpack_ready

RGB bytes: 123, 44, 66, 255, 130, 0, 0, 255, 132, 0, 0, 0, 255, 255, 255, 24, 60, 90, 206, 101, 49, 123, 65, 255

## s42-1: count 8, unpack_ready

RGB bytes: 123, 44, 66, 255, 130, 0, 0, 255, 132, 0, 0, 0, 255, 255, 255, 24, 60, 90, 206, 101, 49, 123, 65, 255

## s42-2: count 9, unpack_ready

RGB bytes: 123, 44, 66, 255, 130, 0, 0, 255, 132, 0, 0, 0, 255, 255, 255, 24, 60, 90, 206, 101, 49, 123, 65, 255, 255, 255, 255

## s42-3: count 16, unpack_ready

RGB bytes: 123, 44, 66, 255, 130, 0, 0, 255, 132, 0, 0, 0, 255, 255, 255, 24, 60, 90, 206, 101, 49, 123, 65, 255, 123, 65, 255, 206, 101, 49, 24, 60, 90, 255, 255, 255, 0, 0, 0, 0, 255, 132, 255, 130, 0, 123, 44, 66

