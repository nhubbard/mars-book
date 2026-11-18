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
    dla $s0, state_a
    dla $s1, chunks
    dla $t0, chunk_count
    lw $s2, 0($t0)
    move $s3, $zero
    beq $s2, $zero, done
    nop
next_chunk:
    move $a0, $s0
    lw $t0, 0($s1)
    dla $a1, input
    daddu $a1, $a1, $t0
    lw $a2, 4($s1)
    lw $a3, 8($s1)
    jal scan_utf8
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
    bne $t0, $zero, next_chunk
    nop
    beq $zero, $zero, done
    nop
function_return:
    nop
done:
    li $v0, 10
    syscall

scan_utf8:
# BEGIN gate
# END gate
scan_active:
# BEGIN load
# END load
scan_loop:
# BEGIN loop
# END loop
# BEGIN ascii
# END ascii
# BEGIN candidate
# END candidate
# BEGIN lead_min
# END lead_min
# BEGIN two_dispatch
# END two_dispatch
# BEGIN three_dispatch
# END three_dispatch
# BEGIN four_limit
# END four_limit
# BEGIN four_low
# END four_low
scan_four_high:
# BEGIN four_high
# END four_high
scan_two:
# BEGIN two_start
# END two_start
scan_three:
# BEGIN three_low
# END three_low
scan_three_high:
# BEGIN three_high
# END three_high
scan_tail:
# BEGIN tail_bounds
# END tail_bounds
# BEGIN tail_advance
# END tail_advance
scan_ascii:
# BEGIN complete
# END complete
scan_accept:
# BEGIN accept
# END accept
scan_end:
# BEGIN end_active
# END end_active
# BEGIN end_final
# END end_final
scan_bad:
# BEGIN bad
# END bad
scan_commit:
# BEGIN publish
# END publish
scan_return:
    jr $ra
    nop
