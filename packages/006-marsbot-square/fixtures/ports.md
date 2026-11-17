# The Default MarsBot register map

| Offset from 0xffff8000 | Full address | Word meaning |
| --- | --- | --- |
| 0x10 | 0xffff8010 | Heading in degrees, normalized modulo 360 |
| 0x20 | 0xffff8020 | Pen: zero up, one down; other values do not change pen state |
| 0x30 | 0xffff8030 | Published x coordinate; CPU must only read it in this task |
| 0x40 | 0xffff8040 | Published y coordinate; CPU must only read it in this task |
| 0x50 | 0xffff8050 | Movement: zero stopped, nonzero moving |

The examples use full word stores for controls and word loads for feedback.
Coordinate values are integer publications of the device position. Cardinal movement
in this chapter stays on integer coordinates. This map is specific to the Default
layout; changing layout requires different addresses.
