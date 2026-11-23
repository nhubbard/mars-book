# Inspect an actual vector XOR, exact alias, scalar tail and zero length.
.data
.align 4
lesson_left: .word 0,0x00112233,0x10,0xffffffff,7
lesson_guard: .byte -91:16
.align 4
lesson_right: .word 0x80000000,0x00112233,0,0,6
.text
.globl main
main:
    dli $t0,-16
    and $sp,$sp,$t0
    dla $a0,lesson_left
    dla $a1,lesson_right
    move $a2,$a0
    li $a3,5
    balc video_xor_words_msa
    nop
    dla $t0,lesson_left
    lwu $s0,0($t0)
    lwu $s1,16($t0)
    dla $t0,lesson_guard
    lbu $s2,0($t0)
lesson_xor_result:
    nop
    li $a0,1
    li $a1,1
    li $a2,1
    move $a3,$zero
    balc video_xor_words_msa
    nop
lesson_xor_empty:
    nop
    li $v0,10
    syscall

.text
video_xor_words:
    move $t9, $zero
    bc deltaKernel_video_entry
    nop
video_xor_words_msa:
    li $t9, 1
deltaKernel_video_entry:
    li $v0, 1
    move $v1, $zero
    sltiu $t0, $a3, 4097
    beqzc $t0, deltaKernel_video_return
    nop
    beqzc $a3, deltaKernel_video_success
    nop
    or $t0, $a0, $a1
    or $t0, $t0, $a2
    andi $t0, $t0, 3
    bnezc $t0, deltaKernel_video_return
    nop
deltaKernel_video_admitted:
    nop
    beqzc $t9, deltaKernel_video_scalar
    nop
deltaKernel_video_vector:
    sltiu $t0, $a3, 4
deltaKernel_video_group_ready:
    nop
    bnezc $t0, deltaKernel_video_tail
    nop
    ld.w $w0, 0($a0)
    ld.w $w1, 0($a1)
    xor.v $w2, $w0, $w1
    st.w $w2, 0($a2)
deltaKernel_video_group_stored:
    nop
    copy_u.w $t0, $w2[0]
    sltu $t0, $zero, $t0
    daddu $v1, $v1, $t0
    copy_u.w $t0, $w2[1]
    sltu $t0, $zero, $t0
    daddu $v1, $v1, $t0
    copy_u.w $t0, $w2[2]
    sltu $t0, $zero, $t0
    daddu $v1, $v1, $t0
    copy_u.w $t0, $w2[3]
    sltu $t0, $zero, $t0
    daddu $v1, $v1, $t0
deltaKernel_video_group_counted:
    nop
    daddiu $a0, $a0, 16
    daddiu $a1, $a1, 16
    daddiu $a2, $a2, 16
    daddiu $a3, $a3, -4
deltaKernel_video_group_advanced:
    nop
    bc deltaKernel_video_vector
    nop
deltaKernel_video_tail:
    beqzc $a3, deltaKernel_video_success
    nop
deltaKernel_video_scalar:
    lwu $t0, 0($a0)
    lwu $t1, 0($a1)
    xor $t2, $t0, $t1
    sw $t2, 0($a2)
    sltu $t2, $zero, $t2
    daddu $v1, $v1, $t2
    daddiu $a0, $a0, 4
    daddiu $a1, $a1, 4
    daddiu $a2, $a2, 4
    daddiu $a3, $a3, -1
deltaKernel_video_scalar_stored:
    nop
    bnezc $a3, deltaKernel_video_scalar
    nop
deltaKernel_video_success:
    move $v0, $zero
deltaKernel_video_return:
    jrc $ra
    nop
deltaKernel_video_missing:
    li $v0, 4
    move $v1, $zero
    jrc $ra
    nop
