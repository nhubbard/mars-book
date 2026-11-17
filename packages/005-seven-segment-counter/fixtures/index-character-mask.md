# Three different meanings for the value A

| Value | Role | Segments if sent directly |
| --- | --- | --- |
| Decimal index 10, byte `0a` | Selects table entry ten | b and d |
| ASCII A, byte `41` | Character encoding | a and g |
| Glyph mask `77` | Default display shape A | a, b, c, e, f, and g |

The peripheral does not infer which role was intended. It applies the byte's
bits directly to its segments.
