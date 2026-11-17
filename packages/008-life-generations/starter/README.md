# Life generations starter

Use MIPS64 Release 6 and the Default little-endian memory layout. The source header
configures an 8 by 8 logical Bitmap Display, using one four-byte word per cell.
The immutable seed is 64 binary bytes. The requested step count is zero through four.
The two working boards have 100 bytes each: an 8 by 8 interior and a one-cell dead halo.

The starter exits without publishing a valid generation and intentionally fails checks.
Write in the student region. When the publication fragment introduces `generation_ready`,
remove the starter's `generation_ready`/`nop` placeholder so the label occurs only once.
Keep done and its exit. The public checkpoint is part of this lesson's interface;
other labels and scratch registers are implementation choices.

The ready checkpoint must follow a complete frame and generation-word publication.
A full-speed Run may skip visible intermediate repaints. Use Debug at that checkpoint
to inspect a stable generation. No fixed host-time animation delay is required.
