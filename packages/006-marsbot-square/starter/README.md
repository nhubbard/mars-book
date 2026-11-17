# MarsBot square starter

Use the Default memory layout and the MIPS64 Release 6 target. The source header
connects MarsBot and sets 64 completed instructions per movement tick. Keep that
setting for the illustrated Debug checkpoints. Use a fresh launch for each experiment;
the robot starts at (0, 0), stopped, facing north, with its pen up.

Fill the student region before the supplied `done` label. The starter exits without
moving and intentionally fails the drawing contract. It needs no console input.
A fast Run may show only the completed path. Debug pauses preserve the device state
because movement uses simulated instructions, not a host-time animation timer.
