# An eight-bit illustration of the carry beyond a multiword representation.
# Values are deliberately small enough for one register to expose the full sum.
.text
.globl main
main:
    li $t0, 250
    li $t1, 250
    li $t2, 251
    daddu $t3, $t0, $t1
full_sum:
    andi $t4, $t3, 255
    dsrl $t5, $t3, 8
    sltu $t6, $t4, $t2
wrapped_sum:
    dsubu $t7, $t3, $t2
reduced_sum:
    li $v0, 10
    syscall
