# Compute pixel addresses from coordinates

Let A be the frame base. The address of column x in row y is:

```text
A + 4 × (16 × y + x)
```

| Position | Word index | Byte displacement |
| --- | --- | --- |
| Original row 0, column 0 | 0 | 0 |
| Mirrored row 0, its first column | 8 | 32 |
| Original row 1, column 0 | 16 | 64 |
| Final mirrored cell, row 7 | 127 | 508 |
| First word beyond the frame | 128 | 512 |

The last valid word occupies bytes 508 through 511. The address at displacement
512 is the boundary, not another cell.
