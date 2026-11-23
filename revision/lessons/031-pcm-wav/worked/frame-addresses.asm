# Inspect the last stereo frame; this is not a reversal implementation.
.data
pcm: .half 12000,-12000,0,16000,-8000,0,32767,-32768
.text
.globl main
main:
    li $t0, 4
    li $t1, 2
    dsll $t2, $t1, 1
    daddiu $t3, $t0, -1
    dmul $t4, $t3, $t2
last_frame_offset:
    dla $t5, pcm
    daddu $t5, $t5, $t4
    lh $t6, 0($t5)
    lh $t7, 2($t5)
channel_pair:
    li $v0, 10
    syscall
