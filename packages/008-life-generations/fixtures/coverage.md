# What the verification exhausts

| Fixture family | Number | What varies |
| --- | --- | --- |
| Interior 3×3 neighborhood | 512 | Center and eight binary neighbors |
| Four corner neighborhoods | 64 | Four in-board bits at each corner |
| Four edge-midpoint neighborhoods | 256 | Six in-board bits at each midpoint |
| Eighteen full-board patterns, counts 0..4 | 90 | Whole-board evolution and buffer reuse |
| Total private cases per implementation | 922 | Actual ready checkpoints in each case |

This does not exhaust 2^64 complete boards or unbounded time. The whole-board patterns
include twelve deterministic mixed fixtures plus empty, full, oscillator, block,
and two glider-shaped inputs, one near an edge. The oracle uses unpadded scatter counts.
