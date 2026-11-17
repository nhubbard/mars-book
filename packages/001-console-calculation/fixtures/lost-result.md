# A result overwritten during syscall setup

Consider this incorrect setup after the result has been computed in `$t0`:

```asm
move $v0, $t0
li $v0, 1
syscall
```

The first line copies the result into `$v0`. The second replaces it with the
selector 1. Neither supplies the required integer argument in `$a0`. Repair the
argument setup by putting the result in `$a0` before requesting service 1.
