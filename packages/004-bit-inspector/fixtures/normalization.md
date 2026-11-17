# A word shift by zero can change the high half

At the positive signed-word boundary, a doubleword addition produces:

```text
0000000080000000
```

Applying `sll $s2, $s2, 0` keeps low word 80000000 and sign-extends that word:

```text
ffffffff80000000
```

The low-word bits do not move, but the complete 64-bit pattern changes. This
is a word-width normalization, not a general full-register no-op.
