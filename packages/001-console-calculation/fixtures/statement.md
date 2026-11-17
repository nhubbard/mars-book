# Read an instruction statement

```asm
addu $t0, $v0, $v0  # double the input
```

| Part | Meaning |
| --- | --- |
| `addu` | Operation mnemonic |
| `$t0` | Destination register |
| First `$v0` | First source register |
| Second `$v0` | Second source register |
| Commas | Operand separators |
| `# double the input` | Comment; it produces no machine operation |

For input 20, the transition is `$t0 ← 20 + 20`. The arrow describes the
assignment to the destination; it is explanatory notation, not MARS syntax.
