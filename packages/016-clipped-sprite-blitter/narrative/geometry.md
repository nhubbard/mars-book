# Use half-open rectangles

A framebuffer cell is identified by integer column and row. A width-eight row has columns zero through seven. Write its horizontal extent as [0,8): the right endpoint belongs to the boundary, not to the image. The sprite occupies [x,x+SW) horizontally and [y,y+SH) vertically before clipping. Intersection chooses the greater left endpoint and the smaller right endpoint. Repeat that operation for the top and bottom.

Call the intersection edges L, T, R, and B. Its width is R minus L and its height is B minus T. Either nonpositive result means there are no cells to copy. Test this before constructing any pixel address. An off-screen placement may produce negative R or a left edge beyond the framebuffer; those values are valid intermediate geometry, not valid subscripts.

The input coordinates can be any signed 32-bit values. Adding a sprite width to the largest coordinate can exceed the signed word range even though this call copies nothing. Sign extension followed by full-width addition keeps that calculation representable. Signed comparisons are required for geometry; unsigned comparisons would treat a negative edge as a large positive value. After a nonempty intersection is established, all loop extents and byte offsets in this exercise are small and nonnegative.
