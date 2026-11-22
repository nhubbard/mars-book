.data
.align 2
left_raw: .word -256
right_raw: .word 512
weight: .word 64
input_guard: .word 0x13579bdf
answer: .word -1
answer_guard: .word 0x2468ace0
.text
.globl main
main:
    dli $t0, -16
    and $sp, $sp, $t0
    lw $a0, left_raw
    lw $a1, right_raw
    lw $a2, weight
    balc interpolate_raw
interpolate_done:
    sw $v0, answer
interpolate_exit:
    li $v0, 10
    syscall

# a0/a1: sign-extended signed raw words; a2: integer weight 0..256.
# Return nearest raw word to ((256-weight)*left + weight*right)/256.
# Exact half ties go toward positive infinity. Preserve saved state and inputs.
interpolate_raw:
    break
