# Adapt the pixels for Bitmap Display
The conversion buffers are packed byte arrays. Bitmap Display instead interprets
one word per displayed cell as `00RRGGBB`. In little-endian memory that word
has bytes blue, green, red and zero. Neither input format can simply be treated as
that display layout. The supplied caller builds preview words explicitly after
both conversion functions return.

Its tool header selects a 512 by 64 pixel display with units sixteen pixels wide and
thirty-two pixels high, giving 32 columns and two rows of word cells. The preview starts at static data. The upper
row contains original pixels; the lower contains their expanded values. For shorter
counts, untouched preview cells remain zero in a fresh caller run. This display
adapter is outside the measured conversion functions.



See the native chapter for the described conceptual diagram.


At `pack_ready`, the packed bytes and return register are ready, but the
caller has not yet stored `packed_count`. At `unpack_ready`, the
expanded bytes are ready and `expanded_count` still contains 99. At
`preview_ready`, both count words and the two displayed rows are complete.
Capture recipes must name which of these boundaries they require. No actual IDE
screenshot for this chapter has yet been captured.
