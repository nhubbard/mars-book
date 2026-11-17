# The sample word is the input. Use the default little-endian environment.
.data
.align 3
sample:          .word -1
sample_guard:    .word 0x13579bdf
results:
signed_value:    .dword 0
unsigned_value:  .dword 0
word_plus_one:   .dword 0
wide_plus_one:   .dword 0
results_guard:   .word 0x2468ace0
signed_msg:      .asciiz "signed: "
unsigned_msg:    .asciiz "unsigned: "
hex_msg:         .asciiz "hex: "
binary_msg:      .asciiz "binary: "
newline:         .asciiz "\n"

.text
.globl main
main:
# BEGIN STUDENT PROGRAM
# END STUDENT PROGRAM

done:
    li $v0, 10
    syscall
