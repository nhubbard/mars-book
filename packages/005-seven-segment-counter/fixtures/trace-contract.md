# A final F is not a sixteen-step counter

These two histories have the same final right-port byte:

```text
correct: 3f 06 5b 4f 66 6d 7d 07 7f 6f 77 7c 39 5e 79 71
wrong:   71
```

The contract distinguishes them. It also distinguishes sixteen byte stores from
sixteen word stores that touch adjacent ports. A student-maintained RAM log alone
cannot establish either transaction property.
