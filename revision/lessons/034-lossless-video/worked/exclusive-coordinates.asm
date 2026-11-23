# Two selected positions explain coordinates; this is not the bounds function.
.data
.align 3
lesson_dimensions: .dword 0x100000001,3
.text
.globl main
main:
    li $t0,2
    li $t1,1
    dmodu $s2,$t1,$t0
    ddivu $s3,$t1,$t0
    li $t1,4
    dmodu $s4,$t1,$t0
    ddivu $s5,$t1,$t0
lesson_coordinates:
    nop
    daddiu $s0,$s2,1
    daddiu $s1,$s5,1
    dmul $s6,$s0,$s1
    li $s7,2
lesson_rectangle_area:
    nop
    dla $t0,lesson_dimensions
    ld $s0,0($t0)
    lwu $s1,0($t0)
    sltiu $s2,$s0,65
lesson_full_width:
    nop
    li $v0,10
    syscall
