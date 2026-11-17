# Observe the first default iteration

| Boundary | Current/next index register | Remaining register | Port history |
| --- | --- | --- | --- |
| Before the first lookup | 0 | 16 | Empty |
| `port_write`, before `sb` | 0 | 16 | Empty |
| `displayed`, just after `sb` | 0 | 16 | `3f` |
| `iteration_done`, after updates | 1 | 15 | `3f` |
| Second `iteration_done` | 2 | 14 | `3f 06` |
| done, before exit | 16 | 0 | All sixteen requested bytes |

The raw right-display value is the last byte in the history. The register index
at `iteration_done` names the next entry, not the digit already `displayed`.
