# Complete one-dimensional clipping experiment; no pixel storage is accessed.
.data
position: .word 2147483647
source_width: .word 5
frame_width: .word 8
clipped_left: .word -1
clipped_right: .word -1
.text
.globl main
main:
    dli $t0, -16
    and $sp, $sp, $t0
    lw $a0, position
    lw $a1, source_width
    lw $a2, frame_width
    balc clip_interval
    nop
interval_ready:
    nop
    sw $v0, clipped_left
    sw $v1, clipped_right
    bc done
    nop
function_return:
    nop
done:
    li $v0, 10
    syscall

# a0 signed word position; a1 source width 0..8; a2 frame width 0..8.
# Return [left,right), or (0,0) for an empty intersection.
clip_interval:
    move $v0, $zero
    move $v1, $zero
    move $t0, $a0
    bgezc $t0, interval_left_ready
    nop
    move $t0, $zero
interval_left_ready:
    daddu $t1, $a0, $a1
    slt $t2, $a2, $t1
    beqzc $t2, interval_right_ready
    nop
    move $t1, $a2
interval_right_ready:
    slt $t2, $t0, $t1
    beqzc $t2, interval_return
    nop
    move $v0, $t0
    move $v1, $t1
interval_return:
    jrc $ra
