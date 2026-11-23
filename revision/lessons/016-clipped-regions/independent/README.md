# Independent clipped image copy

Replace blit_sprite in blit.asm. The complete caller initializes the frame and descriptor pointers. The Chapter 6 companion defines both descriptors and the signed placement interface. Keep public allocation and observation labels. Choose private labels, scratch registers, row traversal and an equivalent clipping algorithm freely.

Use the three native checks in sequence: 474 empty intersections, 107 overlaps and ten complete caller placements. The kernel union retains all original 581 trials. Source reads may include logical source cells later clipped away, but cannot enter padding or guards. Destination reads must stay in logical frame cells. Stores belong only to visible opaque destinations; transparent pixels permit no store, even of the same value or with later restoration. A private correctly restored stack frame is permitted. Preserve full s0–s7, fp, sp and the incoming continuation.

The caller's native profile first checks initialization at blit_call, then checks actual v0/v1 at blit_ready before count publication, and finally checks copied/visible after normal exit. The source's Bitmap Display settings do not change descriptor bounds. High-byte differences in a key can be invisible in low-RGB rendering, so inspect Memory too.

Optional CLI checks run from this directory with JDK 25:

    java -jar /path/to/cli.jar grade kernel.kts blit.asm
    java -jar /path/to/cli.jar grade caller.kts blit.asm

caller.kts uses the same public observation engine as the native assessment and therefore needs the MARS edition supporting console format 8. The original kernel checker remains intact. Exact instruction sequences and counts are not graded; bounded budgets stop runaway execution.
