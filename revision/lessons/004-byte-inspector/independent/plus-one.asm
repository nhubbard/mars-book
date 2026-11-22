# MIPS64 Release 6. Input precondition: an integer from 0 through 255.
# Implement the behavior described in README.md. Choose your own labels and registers.
.data
prompt:   .asciiz "Enter an integer from 0 to 255: "
unsigned: .asciiz "Unsigned byte: "
signed:   .asciiz "Signed byte: "
third:    .asciiz "Unsigned byte plus one: "
newline:  .asciiz "\n"

.text
.globl main
main:
    # Write input, calculation and output here. Keep all three results available
    # until they have been printed. This starter only exits; it does not pass.
    li $v0, 10
    syscall
