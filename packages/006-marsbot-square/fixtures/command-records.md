# One possible command table

| Heading | Coordinate offset | Target |
| --- | --- | --- |
| 90 | 0x30 (x) | L |
| 180 | 0x40 (y) | L |
| 270 | 0x30 (x) | 0 |
| 0 | 0x40 (y) | 0 |

A record can encode the final column as a flag: one chooses L, zero chooses zero.
Three four-byte fields require a twelve-byte stride. Prepare the heading, feedback
address, and target before enabling motion. Keep the inner polling loop short.
