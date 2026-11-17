# Group a word into hexadecimal digits

```text
hexadecimal:  8    0    0    0    0    0    0    1
binary:      1000 0000 0000 0000 0000 0000 0000 0001
bit groups: 31..28                         ... 3..0
```

Each hexadecimal digit gives four binary digits. Spaces group the example for
reading; the actual binary syscall prints one continuous string of 32 digits.
The pattern's unsigned value is 2147483649 and its signed value is -2147483647.
