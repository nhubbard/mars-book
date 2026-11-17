# Locate four doubleword outputs

| Field | Displacement from results | Bytes stored |
| --- | --- | --- |
| signed_value | 0 | 8 |
| unsigned_value | 8 | 8 |
| word_plus_one | 16 | 8 |
| wide_plus_one | 24 | 8 |
| results_guard | 32 | Outside the result block |

The four results occupy bytes 0 through 31 of the block. The canary starts
at byte 32. These displacements describe the supplied layout; a free-writing
solution may use other registers and address calculations.
