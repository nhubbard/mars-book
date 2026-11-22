.data
.align 3
matrix_guard: .word 0x13579bdf
matrix: .word 1,2,3,4,5,6
    .space 1000
matrix_end: .word 0x2468ace0
vector_guard: .word 0x31415926
vector: .word 7,8,9
    .space 52
vector_end: .word 0x27182818
.align 3
output: .dword -1:16
output_end: .word 0x12345678
shape: .word 2,3
completed: .word -1
.text
.globl main
main:
    dli $t0, -16
    and $sp, $sp, $t0
    la $a0, matrix
    la $a1, vector
    la $a2, output
    la $a3, shape
    balc matvec
    nop
matvec_done:
    nop
    sw $v0, completed
    bc done
    nop
function_return:
    nop
done:
    li $v0, 10
    syscall

# Implement the independent matrix-vector product from Chapter 13.
matvec:
    break
