# Independent streaming UTF-8 scanner; see the Chapter 12 companion lab.
.data
.align 2
state_a: .word 0,0,0,128,191,-1,0,-1
state_a_guard: .word 0x13579bdf
state_b: .word 0,0,0,128,191,-1,0,-1
state_b_guard: .word 0x2468ace0
input_guard: .word 0x31415926
input: .byte 65,-30,-126,-84,-16,-97,-103,-126,0,-61,-87
    .space 85
input_end: .word 0x27182818
chunks:
    .word 0,2,0
    .word 2,3,0
    .word 5,2,0
    .word 7,4,1
chunk_count: .word 4
last_status: .word -99
last_used: .word -99
chunk_index: .word -1
state_end: .word 0x76543210
.text
.globl main
main:
    daddiu $sp, $sp, -8
    dla $s0, state_a
    dla $s1, chunks
    dla $t0, chunk_count
    lw $s2, 0($t0)
    move $s3, $zero
    beqzc $s2, done
    nop
next_chunk:
    move $a0, $s0
    lw $t0, 0($s1)
    dla $a1, input
    daddu $a1, $a1, $t0
    lw $a2, 4($s1)
    lw $a3, 8($s1)
    balc scan_utf8
    nop
    dla $t0, last_status
    sw $v0, 0($t0)
    dla $t0, last_used
    sw $v1, 0($t0)
    dla $t0, chunk_index
    sw $s3, 0($t0)
chunk_ready:
    nop
chunk_continue:
    daddiu $s1, $s1, 12
    addiu $s3, $s3, 1
    sltu $t0, $s3, $s2
    bnezc $t0, next_chunk
    nop
    bc done
    nop
function_return:
    nop
done:
    li $v0, 10
    syscall

# Replace the failing body. Keep the public state/caller interface.
scan_utf8:
    break
