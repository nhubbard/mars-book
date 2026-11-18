# Complete native-call work

| Scene | Bitmap instructions | Byte-map instructions | Bitmap image reads | Byte-map image reads |
|---|---:|---:|---:|---:|
| Default budget zero | 794 | 659 | 28 | 28 |
| One halt | 94 | 101 | 3 | 3 |
| Default budget four | 964 | 829 | 34 | 34 |
| Default full | 2394 | 2259 | 70 | 70 |
| JMP zero budget 64 | 3321 | 3311 | 131 | 131 |
| 64-byte image, JMP 63 | 2478 | 2468 | 130 | 130 |
| Invalid default target, budget zero | 604 | 470 | 18 | 18 |

Counts cover prologue through restore, excluding caller setup, assembly, session creation and exit. They describe simulated instructions, not host time. The alternative changes both boundary representation and opcode classification, so the difference does not isolate one cause. Private map and native stack accesses are excluded from image-read counts. Fresh calls must validate current bytes even at the same pointer and length. No native work count is a grading requirement.
