# Read the physical Bitmap view

The supplied display recipe uses a 128 by 64 pixel display with eight-pixel units, producing sixteen word cells per row and eight rows. Its base is static data. The first displayed word, `bitmap_origin`, is a gray guard immediately before `frame_a`. Consequently the logical framebuffer begins in physical display column one, not column zero. With stride sixteen, its next logical row also starts in physical column one of the next displayed row.

The logical framebuffer has eight active columns and six active rows. Gray words elsewhere are allocation padding or the leading guard; they are not pixels available to the blitter. The view contains the leading guard and the first 127 words of the 128-word frame allocation. Its final padding word and trailing guard lie outside the displayed window and still require memory checks. The display alone cannot establish that all guards and padding survived.

At `blit_ready`, inspect `v0` and `v1`; the caller has not yet stored them into `copied` and `visible`. Continue past `blit_continue` to inspect those memory words. The capture recipes distinguish these two observation points. All IDE figures for this package remain planned until an automated light-theme capture and its state receipt have been produced.
