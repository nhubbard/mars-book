# Arithmetic observations only; this is not the independent reconstruction function.
.data
.align 1
lesson_inverse: .half -129,-128,-127,0,126,127,128,255
.align 3
lesson_settings: .dword 0x100000008,16,8
.text
.globl main
main:
    dla $t0,lesson_inverse
    lh $s0,0($t0)
    lhu $s1,0($t0)
    li $t1,128
    daddu $s2,$s0,$t1
    slt $s3,$s2,$zero
lesson_signed_sum:
    nop
    lh $t2,10($t0)
    daddu $s4,$t1,$t2
    li $t2,255
    sltu $s5,$t2,$s4
    lh $t2,12($t0)
    daddu $s6,$t1,$t2
    li $t2,255
    sltu $s7,$t2,$s6
lesson_clipped_endpoints:
    nop
    dla $t0,lesson_settings
    ld $s0,0($t0)
    lwu $s1,0($t0)
    sltiu $s2,$s0,4097
lesson_native_stride:
    nop
    li $v0,10
    syscall
