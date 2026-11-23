# Complete worked example for the native QOI lesson.
.data
input_path: .asciiz "input.qoi"
    .space 118
output_path: .asciiz "output.qoi"
    .space 117
source: .byte 113,111,105,102,0,0,0,8,0,0,0,1,4,0,-64,-2,-1,0,0,-64,53,127,-91,-105,-1,7,6,5,-128,4,0,0,0,0,0,0,0,1
    .byte -91:20506
source_end:
.align 3
metadata: .byte -91:64
framebuffer: .byte -91:16448
framebuffer_end:
destination:
    .byte -91:20544
.align 4
workspace: .space 8192
status: .word -1
.align 3
count: .dword -1
mode: .word 0
.align 3
dimensions: .dword 4294967304
byte_count: .dword 38
pixel_capacity: .dword 4096
byte_capacity: .dword 20502
.text
main:
    dla $sp, workspace
    daddiu $sp, $sp, 8192
    dla $t0, mode
    lw $t0, 0($t0)
    beqzc $t0, caller_bound
    nop
    li $t1, 1
    beqc $t0, $t1, caller_validate
    nop
    li $t1, 2
    beqc $t0, $t1, caller_decode
    nop
    li $t1, 3
    beqc $t0, $t1, caller_encode
    nop
    li $t1, 4
    beqc $t0, $t1, caller_load
    nop
    li $t1, 5
    beqc $t0, $t1, caller_save
    nop
    bc caller_load
    nop
caller_bound:
    dla $t0, dimensions
    ld $a0, 0($t0)
bound_call:
    balc qoi_bound
    nop
bound_return:
    bc results_ready
    nop
caller_validate:
    dla $a0, source
    dla $t0, byte_count
    ld $a1, 0($t0)
    dla $a2, metadata
validate_call:
    balc qoi_validate
    nop
validate_return:
    bc results_ready
    nop
caller_decode:
    dla $a0, source
    dla $t0, byte_count
    ld $a1, 0($t0)
    bc caller_decode_arguments
    nop
pipeline_decode:
    dla $a0, source
    move $a1, $v1
caller_decode_arguments:
    dla $a2, framebuffer
    dla $t0, pixel_capacity
    ld $a3, 0($t0)
decode_call:
    balc qoi_decode
    nop
decode_return:
    bnezc $v0, results_ready
    nop
    dla $t0, mode
    lw $t0, 0($t0)
    li $t1, 6
    beqc $t0, $t1, caller_encode
    nop
    bc results_ready
    nop
caller_encode:
    dla $a0, framebuffer
    dla $a1, metadata
    dla $a2, destination
    dla $t0, byte_capacity
    ld $a3, 0($t0)
encode_call:
    balc qoi_encode
    nop
encode_return:
    bnezc $v0, results_ready
    nop
    dla $t0, mode
    lw $t0, 0($t0)
    li $t1, 6
    beqc $t0, $t1, pipeline_save
    nop
    bc results_ready
    nop
caller_load:
    dla $a0, input_path
    dla $a1, source
    dla $t0, byte_capacity
    ld $a2, 0($t0)
    dla $a3, metadata
load_call:
    balc qoi_load
    nop
load_return:
    bnezc $v0, results_ready
    nop
    dla $t0, mode
    lw $t0, 0($t0)
    li $t1, 6
    beqc $t0, $t1, pipeline_decode
    nop
    bc results_ready
    nop
caller_save:
    dla $a1, source
    dla $t0, byte_count
    ld $a2, 0($t0)
    bc caller_save_arguments
    nop
pipeline_save:
    dla $a1, destination
    move $a2, $v1
caller_save_arguments:
    dla $a0, output_path
save_call:
    balc qoi_save
    nop
save_return:
results_ready:
    dla $t0, status
    sw $v0, 0($t0)
    dla $t0, count
    sd $v1, 0($t0)
    li $v0, 10
    syscall
function_return:
    nop
qoi_bound:
    dsll $t1, $a0, 32
    dsrl $t1, $t1, 32
    dsrl $t2, $a0, 32
after_b_unpack:
    nop
    beqzc $t1, qoi_invalid
    nop
    beqzc $t2, qoi_invalid
    nop
    sltiu $t0, $t1, 65
    beqzc $t0, qoi_invalid
    nop
    sltiu $t0, $t2, 65
    beqzc $t0, qoi_invalid
    nop
after_b_domain:
    nop
    mul $t3, $t1, $t2
    dsll $v1, $t3, 2
    daddu $v1, $v1, $t3
    daddiu $v1, $v1, 22
    move $v0, $zero
after_b_size:
    nop
    jrc $ra
    nop
qoi_invalid:
    li $v0, 1
    move $v1, $zero
    jrc $ra
    nop
qoi_capacity:
    li $v0, 2
    move $v1, $zero
    jrc $ra
    nop
qoi_development:
    li $v0, 4
    move $v1, $zero
    jrc $ra
    nop
qoi_validate:
    li $t0, 20502
    sltu $t0, $t0, $a1
    bnezc $t0, qoi_capacity
    nop
    sltiu $t0, $a1, 23
    bnezc $t0, qoi_invalid
    nop
after_v_count:
    nop
    move $a3, $zero
    move $t9, $zero
    bc qoi_walk
    nop
qoi_decode:
    daddiu $sp, $sp, -128
    sd $s0, 0($sp)
    sd $s1, 8($sp)
    sd $s2, 16($sp)
    sd $s3, 24($sp)
    sd $s4, 32($sp)
    sd $s5, 40($sp)
    sd $s6, 48($sp)
    sd $s7, 56($sp)
    sd $fp, 64($sp)
    sd $ra, 72($sp)
    move $s0, $a0
    move $s1, $a1
    move $s2, $a2
    move $s3, $a3
    daddiu $a2, $sp, 80
    balc qoi_validate
    nop
after_d_call:
    nop
    bnezc $v0, decode_restore
    nop
    sltu $t0, $s3, $v1
    bnezc $t0, decode_capacity
    nop
after_d_capacity:
    nop
    move $a0, $s0
    move $a1, $s1
    daddiu $a2, $sp, 80
    move $a3, $s2
    li $t9, 1
    balc qoi_walk
    nop
after_d_walk:
    nop
    bnezc $v0, decode_restore
    nop
    lwu $v1, 84($sp)
    dsll $v1, $v1, 32
    lwu $t0, 80($sp)
    or $v1, $v1, $t0
after_d_return:
    nop
    bc decode_restore
    nop
decode_capacity:
    li $v0, 2
    move $v1, $zero
    bc decode_restore
    nop
decode_development:
    li $v0, 4
    move $v1, $zero
    bc decode_restore
    nop
decode_restore:
    ld $s0, 0($sp)
    ld $s1, 8($sp)
    ld $s2, 16($sp)
    ld $s3, 24($sp)
    ld $s4, 32($sp)
    ld $s5, 40($sp)
    ld $s6, 48($sp)
    ld $s7, 56($sp)
    ld $fp, 64($sp)
    ld $ra, 72($sp)
    daddiu $sp, $sp, 128
    jrc $ra
    nop
qoi_walk:
    daddiu $sp, $sp, -384
    sd $s0, 0($sp)
    sd $s1, 8($sp)
    sd $s2, 16($sp)
    sd $s3, 24($sp)
    sd $s4, 32($sp)
    sd $s5, 40($sp)
    sd $s6, 48($sp)
    sd $s7, 56($sp)
    sd $fp, 64($sp)
    sd $ra, 72($sp)
    sd $a2, 80($sp)
    move $fp, $t9
    move $s5, $a3
    lbu $t0, 0($a0)
    li $t1, 113
    bnec $t0, $t1, walk_invalid
    nop
    lbu $t0, 1($a0)
    li $t1, 111
    bnec $t0, $t1, walk_invalid
    nop
    lbu $t0, 2($a0)
    li $t1, 105
    bnec $t0, $t1, walk_invalid
    nop
    lbu $t0, 3($a0)
    li $t1, 102
    bnec $t0, $t1, walk_invalid
    nop
after_w_magic:
    nop
    lbu $t4, 4($a0)
    dsll $t4, $t4, 24
    lbu $t0, 5($a0)
    dsll $t0, $t0, 16
    or $t4, $t4, $t0
    lbu $t0, 6($a0)
    dsll $t0, $t0, 8
    or $t4, $t4, $t0
    lbu $t0, 7($a0)
    or $t4, $t4, $t0
    lbu $t5, 8($a0)
    dsll $t5, $t5, 24
    lbu $t0, 9($a0)
    dsll $t0, $t0, 16
    or $t5, $t5, $t0
    lbu $t0, 10($a0)
    dsll $t0, $t0, 8
    or $t5, $t5, $t0
    lbu $t0, 11($a0)
    or $t5, $t5, $t0
    beqzc $t4, walk_invalid
    nop
    beqzc $t5, walk_invalid
    nop
    sltiu $t0, $t4, 65
    beqzc $t0, walk_invalid
    nop
    sltiu $t0, $t5, 65
    beqzc $t0, walk_invalid
    nop
after_w_dimensions:
    nop
    lbu $t6, 12($a0)
    daddiu $t0, $t6, -3
    sltiu $t0, $t0, 2
    beqzc $t0, walk_invalid
    nop
    lbu $t7, 13($a0)
    sltiu $t0, $t7, 2
    beqzc $t0, walk_invalid
    nop
    sw $t4, 88($sp)
    sw $t5, 92($sp)
    sw $t6, 96($sp)
    sw $t7, 100($sp)
after_w_flags:
    nop
    mul $s2, $t4, $t5
    move $s7, $s2
    daddiu $s0, $a0, 14
    daddu $s1, $a0, $a1
    daddiu $s1, $s1, -8
    li $s3, 255
    dsll $s3, $s3, 24
    li $s6, -1
    daddiu $s4, $sp, 112
    move $t0, $s4
    li $t1, 32
after_w_state:
    nop
walk_clear:
    sd $zero, 0($t0)
    daddiu $t0, $t0, 8
    daddiu $t1, $t1, -1
    bnezc $t1, walk_clear
    nop
after_w_clear:
    nop
walk_next:
    beqzc $s2, walk_marker
    nop
    sltu $t0, $s0, $s1
    beqzc $t0, walk_invalid
    nop
    lbu $t0, 0($s0)
    daddiu $s0, $s0, 1
after_w_next:
    nop
    li $t1, 254
    beqc $t0, $t1, walk_rgb
    nop
    li $t1, 255
    beqc $t0, $t1, walk_rgba
    nop
    dsrl $t1, $t0, 6
    beqzc $t1, walk_index
    nop
    li $s6, -1
    li $t2, 1
    beqc $t1, $t2, walk_diff
    nop
    li $t2, 2
    beqc $t1, $t2, walk_luma
    nop
after_w_dispatch:
    nop
walk_run:
    andi $t3, $t0, 63
    daddiu $t3, $t3, 1
    sltu $t1, $s2, $t3
    bnezc $t1, walk_invalid
    nop
after_w_run:
    nop
    bc walk_publish
    nop
walk_index:
    beqc $s6, $t0, walk_invalid
    nop
    move $s6, $t0
    dsll $t1, $t0, 2
    daddu $t1, $s4, $t1
    lwu $s3, 0($t1)
    li $t3, 1
after_w_index:
    nop
    bc walk_publish
    nop
walk_rgb:
    li $s6, -1
    dsubu $t1, $s1, $s0
    sltiu $t1, $t1, 3
    bnezc $t1, walk_invalid
    nop
    lbu $t4, 0($s0)
    lbu $t5, 1($s0)
    lbu $t6, 2($s0)
    dsrl $t7, $s3, 24
    daddiu $s0, $s0, 3
after_w_rgb:
    nop
    bc walk_pack
    nop
walk_rgba:
    li $s6, -1
    dsubu $t1, $s1, $s0
    sltiu $t1, $t1, 4
    bnezc $t1, walk_invalid
    nop
    lbu $t4, 0($s0)
    lbu $t5, 1($s0)
    lbu $t6, 2($s0)
    lbu $t7, 3($s0)
    daddiu $s0, $s0, 4
after_w_rgba:
    nop
    bc walk_pack
    nop
walk_diff:
    dsrl $t4, $s3, 16
    andi $t4, $t4, 255
    dsrl $t5, $s3, 8
    andi $t5, $t5, 255
    andi $t6, $s3, 255
    dsrl $t7, $s3, 24
    dsrl $t8, $t0, 4
    andi $t8, $t8, 3
    daddiu $t8, $t8, -2
    daddu $t4, $t4, $t8
    andi $t4, $t4, 255
    dsrl $t8, $t0, 2
    andi $t8, $t8, 3
    daddiu $t8, $t8, -2
    daddu $t5, $t5, $t8
    andi $t5, $t5, 255
    andi $t8, $t0, 3
    daddiu $t8, $t8, -2
    daddu $t6, $t6, $t8
    andi $t6, $t6, 255
after_w_diff:
    nop
    bc walk_pack
    nop
walk_luma:
    beqc $s0, $s1, walk_invalid
    nop
    lbu $t1, 0($s0)
    daddiu $s0, $s0, 1
    andi $t2, $t0, 63
    daddiu $t2, $t2, -32
    dsrl $t4, $s3, 16
    andi $t4, $t4, 255
    dsrl $t5, $s3, 8
    andi $t5, $t5, 255
    andi $t6, $s3, 255
    dsrl $t7, $s3, 24
    dsrl $t8, $t1, 4
    daddiu $t8, $t8, -8
    daddu $t8, $t8, $t2
    daddu $t4, $t4, $t8
    andi $t4, $t4, 255
    daddu $t5, $t5, $t2
    andi $t5, $t5, 255
    andi $t8, $t1, 15
    daddiu $t8, $t8, -8
    daddu $t8, $t8, $t2
    daddu $t6, $t6, $t8
    andi $t6, $t6, 255
after_w_luma:
    nop
    bc walk_pack
    nop
walk_pack:
    dsll $s3, $t7, 24
    dsll $t4, $t4, 16
    dsll $t5, $t5, 8
    or $s3, $s3, $t4
    or $s3, $s3, $t5
    or $s3, $s3, $t6
    li $t3, 1
after_w_pack:
    nop
walk_publish:
    dsrl $t4, $s3, 16
    andi $t4, $t4, 255
    dsrl $t5, $s3, 8
    andi $t5, $t5, 255
    andi $t6, $s3, 255
    dsrl $t7, $s3, 24
    li $t8, 3
    mul $t4, $t4, $t8
    li $t8, 5
    mul $t5, $t5, $t8
    li $t8, 7
    mul $t6, $t6, $t8
    li $t8, 11
    mul $t7, $t7, $t8
    daddu $t4, $t4, $t5
    daddu $t4, $t4, $t6
    daddu $t4, $t4, $t7
    andi $t4, $t4, 63
    dsll $t4, $t4, 2
    daddu $t4, $s4, $t4
    sw $s3, 0($t4)
after_w_hash:
    nop
walk_emit:
    beqzc $fp, after_w_emit
    nop
    sw $s3, 0($s5)
    daddiu $s5, $s5, 4
after_w_emit:
    nop
    bc walk_advance
    nop
walk_advance:
    daddiu $s2, $s2, -1
    daddiu $t3, $t3, -1
    bnezc $t3, walk_emit
    nop
after_w_advance:
    nop
    bc walk_next
    nop
walk_marker:
    bnec $s0, $s1, walk_invalid
    nop
    move $t0, $s0
    li $t1, 7
walk_marker_zero:
    lbu $t2, 0($t0)
    bnezc $t2, walk_invalid
    nop
    daddiu $t0, $t0, 1
    daddiu $t1, $t1, -1
    bnezc $t1, walk_marker_zero
    nop
    lbu $t2, 0($t0)
    li $t1, 1
    bnec $t2, $t1, walk_invalid
    nop
after_w_marker:
    nop
    ld $t0, 80($sp)
    lwu $t1, 88($sp)
    sw $t1, 0($t0)
    lwu $t1, 92($sp)
    sw $t1, 4($t0)
    lwu $t1, 96($sp)
    sw $t1, 8($t0)
    lwu $t1, 100($sp)
    sw $t1, 12($t0)
    move $v0, $zero
    move $v1, $s7
after_w_meta:
    nop
    bc walk_restore
    nop
walk_invalid:
    li $v0, 1
    move $v1, $zero
    bc walk_restore
    nop
walk_development:
    li $v0, 4
    move $v1, $zero
    bc walk_restore
    nop
walk_restore:
    ld $s0, 0($sp)
    ld $s1, 8($sp)
    ld $s2, 16($sp)
    ld $s3, 24($sp)
    ld $s4, 32($sp)
    ld $s5, 40($sp)
    ld $s6, 48($sp)
    ld $s7, 56($sp)
    ld $fp, 64($sp)
    ld $ra, 72($sp)
    daddiu $sp, $sp, 384
    jrc $ra
    nop
qoi_encode:
    daddiu $sp, $sp, -384
    sd $s0, 0($sp)
    sd $s1, 8($sp)
    sd $s2, 16($sp)
    sd $s3, 24($sp)
    sd $s4, 32($sp)
    sd $s5, 40($sp)
    sd $s6, 48($sp)
    sd $s7, 56($sp)
    sd $fp, 64($sp)
    sd $ra, 72($sp)
    move $s0, $a0
    move $s2, $a2
    move $s6, $a2
    lwu $t4, 0($a1)
    lwu $t5, 4($a1)
    lwu $t6, 8($a1)
    lwu $t7, 12($a1)
    beqzc $t4, encode_invalid
    nop
    beqzc $t5, encode_invalid
    nop
    sltiu $t0, $t4, 65
    beqzc $t0, encode_invalid
    nop
    sltiu $t0, $t5, 65
    beqzc $t0, encode_invalid
    nop
    daddiu $t0, $t6, -3
    sltiu $t0, $t0, 2
    beqzc $t0, encode_invalid
    nop
    sltiu $t0, $t7, 2
    beqzc $t0, encode_invalid
    nop
after_e_descriptor:
    nop
    mul $s1, $t4, $t5
    dsll $t0, $s1, 2
    daddu $t0, $t0, $s1
    daddiu $t0, $t0, 22
    sltu $t0, $a3, $t0
    bnezc $t0, encode_capacity
    nop
after_e_capacity:
    nop
    sw $t4, 88($sp)
    sw $t5, 92($sp)
    sw $t6, 96($sp)
    sw $t7, 100($sp)
    daddiu $s4, $sp, 112
    move $t0, $s4
    li $t1, 32
after_e_state:
    nop
encode_clear:
    sd $zero, 0($t0)
    daddiu $t0, $t0, 8
    daddiu $t1, $t1, -1
    bnezc $t1, encode_clear
    nop
after_e_clear:
    nop
    li $t0, 113
    sb $t0, 0($s2)
    li $t0, 111
    sb $t0, 1($s2)
    li $t0, 105
    sb $t0, 2($s2)
    li $t0, 102
    sb $t0, 3($s2)
    dsrl $t0, $t4, 24
    sb $t0, 4($s2)
    dsrl $t0, $t4, 16
    sb $t0, 5($s2)
    dsrl $t0, $t4, 8
    sb $t0, 6($s2)
    dsrl $t0, $t4, 0
    sb $t0, 7($s2)
    dsrl $t0, $t5, 24
    sb $t0, 8($s2)
    dsrl $t0, $t5, 16
    sb $t0, 9($s2)
    dsrl $t0, $t5, 8
    sb $t0, 10($s2)
    dsrl $t0, $t5, 0
    sb $t0, 11($s2)
    sb $t6, 12($s2)
    sb $t7, 13($s2)
    daddiu $s2, $s2, 14
after_e_header:
    nop
    li $s3, 255
    dsll $s3, $s3, 24
    move $s5, $zero
after_e_previous:
    nop
encode_pixel:
    lwu $t0, 0($s0)
    daddiu $s0, $s0, 4
    daddiu $s1, $s1, -1
after_e_pixel:
    nop
    dsrl $t1, $t0, 16
    andi $t1, $t1, 255
    dsrl $t2, $t0, 8
    andi $t2, $t2, 255
    andi $t3, $t0, 255
    dsrl $t4, $t0, 24
    li $t5, 3
    mul $t1, $t1, $t5
    li $t5, 5
    mul $t2, $t2, $t5
    li $t5, 7
    mul $t3, $t3, $t5
    li $t5, 11
    mul $t4, $t4, $t5
    daddu $t1, $t1, $t2
    daddu $t1, $t1, $t3
    daddu $t1, $t1, $t4
    andi $t1, $t1, 63
    dsll $t8, $t1, 2
    daddu $t8, $s4, $t8
    lwu $t9, 0($t8)
after_e_hash:
    nop
    beqc $t0, $s3, encode_run
    nop
    beqzc $s5, encode_lookup
    nop
    daddiu $t1, $s5, -1
    ori $t1, $t1, 192
    sb $t1, 0($s2)
    daddiu $s2, $s2, 1
    move $s5, $zero
after_e_repeat:
    nop
encode_lookup:
    beqc $t0, $t9, encode_index
    nop
    xor $t1, $t0, $s3
    dsrl $t1, $t1, 24
    bnezc $t1, encode_rgba
    nop
after_e_lookup:
    nop
    dsrl $t1, $t0, 16
    andi $t1, $t1, 255
    dsrl $t4, $s3, 16
    andi $t4, $t4, 255
    dsubu $t1, $t1, $t4
    daddiu $t1, $t1, 128
    andi $t1, $t1, 255
    daddiu $t1, $t1, -128
    dsrl $t2, $t0, 8
    andi $t2, $t2, 255
    dsrl $t4, $s3, 8
    andi $t4, $t4, 255
    dsubu $t2, $t2, $t4
    daddiu $t2, $t2, 128
    andi $t2, $t2, 255
    daddiu $t2, $t2, -128
    dsrl $t3, $t0, 0
    andi $t3, $t3, 255
    dsrl $t4, $s3, 0
    andi $t4, $t4, 255
    dsubu $t3, $t3, $t4
    daddiu $t3, $t3, 128
    andi $t3, $t3, 255
    daddiu $t3, $t3, -128
after_e_delta:
    nop
encode_diff_test:
    daddiu $t4, $t1, 2
    sltiu $t4, $t4, 4
    daddiu $t5, $t2, 2
    sltiu $t5, $t5, 4
    and $t4, $t4, $t5
    daddiu $t5, $t3, 2
    sltiu $t5, $t5, 4
    and $t4, $t4, $t5
    beqzc $t4, encode_luma_test
    nop
    daddiu $t4, $t1, 2
    dsll $t4, $t4, 4
    daddiu $t5, $t2, 2
    dsll $t5, $t5, 2
    or $t4, $t4, $t5
    daddiu $t5, $t3, 2
    or $t4, $t4, $t5
    ori $t4, $t4, 64
    sb $t4, 0($s2)
    daddiu $s2, $s2, 1
after_e_diff:
    nop
    bc encode_finish_pixel
    nop
encode_luma_test:
    daddiu $t4, $t2, 32
    sltiu $t4, $t4, 64
    beqzc $t4, encode_rgb
    nop
    dsubu $t4, $t1, $t2
    dsubu $t5, $t3, $t2
    daddiu $t6, $t4, 8
    sltiu $t6, $t6, 16
    beqzc $t6, encode_rgb
    nop
    daddiu $t6, $t5, 8
    sltiu $t6, $t6, 16
    beqzc $t6, encode_rgb
    nop
    daddiu $t6, $t2, 32
    ori $t6, $t6, 128
    sb $t6, 0($s2)
    daddiu $t4, $t4, 8
    dsll $t4, $t4, 4
    daddiu $t5, $t5, 8
    or $t4, $t4, $t5
    sb $t4, 1($s2)
    daddiu $s2, $s2, 2
after_e_luma:
    nop
    bc encode_finish_pixel
    nop
encode_index:
    dsubu $t1, $t8, $s4
    dsrl $t1, $t1, 2
    sb $t1, 0($s2)
    daddiu $s2, $s2, 1
after_e_index:
    nop
    bc encode_finish_pixel
    nop
encode_rgb:
    li $t1, 254
    sb $t1, 0($s2)
    dsrl $t1, $t0, 16
    sb $t1, 1($s2)
    dsrl $t1, $t0, 8
    sb $t1, 2($s2)
    sb $t0, 3($s2)
    daddiu $s2, $s2, 4
after_e_rgb:
    nop
    bc encode_finish_pixel
    nop
encode_rgba:
    li $t1, 255
    sb $t1, 0($s2)
    dsrl $t1, $t0, 16
    sb $t1, 1($s2)
    dsrl $t1, $t0, 8
    sb $t1, 2($s2)
    sb $t0, 3($s2)
    dsrl $t1, $t0, 24
    sb $t1, 4($s2)
    daddiu $s2, $s2, 5
after_e_rgba:
    nop
    bc encode_finish_pixel
    nop
encode_run:
    daddiu $s5, $s5, 1
    li $t1, 62
    beqc $s5, $t1, encode_flush
    nop
    bnezc $s1, encode_finish_pixel
    nop
after_e_run:
    nop
encode_flush:
    daddiu $t1, $s5, -1
    ori $t1, $t1, 192
    sb $t1, 0($s2)
    daddiu $s2, $s2, 1
    move $s5, $zero
after_e_flush:
    nop
encode_finish_pixel:
    sw $t0, 0($t8)
    move $s3, $t0
    bnezc $s1, encode_pixel
    nop
after_e_finish:
    nop
    li $t1, 7
encode_marker:
    sb $zero, 0($s2)
    daddiu $s2, $s2, 1
    daddiu $t1, $t1, -1
    bnezc $t1, encode_marker
    nop
    li $t1, 1
    sb $t1, 0($s2)
    daddiu $s2, $s2, 1
    dsubu $v1, $s2, $s6
    move $v0, $zero
after_e_marker:
    nop
    bc encode_restore
    nop
encode_invalid:
    li $v0, 1
    move $v1, $zero
    bc encode_restore
    nop
encode_capacity:
    li $v0, 2
    move $v1, $zero
    bc encode_restore
    nop
encode_development:
    li $v0, 4
    move $v1, $zero
    bc encode_restore
    nop
encode_restore:
    ld $s0, 0($sp)
    ld $s1, 8($sp)
    ld $s2, 16($sp)
    ld $s3, 24($sp)
    ld $s4, 32($sp)
    ld $s5, 40($sp)
    ld $s6, 48($sp)
    ld $s7, 56($sp)
    ld $fp, 64($sp)
    ld $ra, 72($sp)
    daddiu $sp, $sp, 384
    jrc $ra
    nop
qoi_load:
    beqzc $a2, qoi_capacity
    nop
    li $t0, 20502
    sltu $t0, $t0, $a2
    bnezc $t0, qoi_capacity
    nop
after_l_bound:
    nop
load_frame:
    daddiu $sp, $sp, -128
    sd $s0, 0($sp)
    sd $s1, 8($sp)
    sd $s2, 16($sp)
    sd $s3, 24($sp)
    sd $s4, 32($sp)
    sd $s5, 40($sp)
    sd $s6, 48($sp)
    sd $s7, 56($sp)
    sd $fp, 64($sp)
    sd $ra, 72($sp)
    li $s3, -1
    move $s0, $a1
    move $s1, $a2
    move $s2, $a3
    move $a1, $zero
    li $v0, 13
    syscall
    bltzc $v0, load_io_error
    nop
    move $s3, $v0
    move $s4, $zero
after_l_open:
    nop
load_read:
    beqc $s4, $s1, load_probe
    nop
    move $a0, $s3
    daddu $a1, $s0, $s4
    dsubu $a2, $s1, $s4
    li $v0, 14
    syscall
    bltzc $v0, load_read_error
    nop
    beqzc $v0, load_eof
    nop
    daddu $s4, $s4, $v0
after_l_read:
    nop
    bc load_read
    nop
load_probe:
    move $a0, $s3
    daddiu $a1, $sp, 96
    li $a2, 1
    li $v0, 14
    syscall
    bltzc $v0, load_read_error
    nop
    beqzc $v0, load_eof
    nop
    move $a0, $s3
    li $v0, 16
    syscall
    li $v0, 2
    bc load_failed
    nop
after_l_probe:
    nop
load_read_error:
    move $a0, $s3
    li $v0, 16
    syscall
load_io_error:
    li $v0, 3
load_failed:
    move $v1, $zero
    bc load_restore
    nop
load_eof:
    move $a0, $s3
    li $v0, 16
    syscall
    move $a0, $s0
    move $a1, $s4
    move $a2, $s2
    balc qoi_validate
    nop
    bnezc $v0, load_restore
    nop
    move $v1, $s4
after_l_finish:
    nop
    bc load_restore
    nop
load_development:
    bltzc $s3, load_development_status
    nop
    move $a0, $s3
    li $v0, 16
    syscall
load_development_status:
    li $v0, 4
    move $v1, $zero
    bc load_restore
    nop
load_restore:
    ld $s0, 0($sp)
    ld $s1, 8($sp)
    ld $s2, 16($sp)
    ld $s3, 24($sp)
    ld $s4, 32($sp)
    ld $s5, 40($sp)
    ld $s6, 48($sp)
    ld $s7, 56($sp)
    ld $fp, 64($sp)
    ld $ra, 72($sp)
    daddiu $sp, $sp, 128
    jrc $ra
    nop
qoi_save:
    li $t0, 20502
    sltu $t0, $t0, $a2
    bnezc $t0, qoi_capacity
    nop
    sltiu $t0, $a2, 23
    bnezc $t0, qoi_invalid
    nop
after_sv_bound:
    nop
save_frame:
    daddiu $sp, $sp, -128
    sd $s0, 0($sp)
    sd $s1, 8($sp)
    sd $s2, 16($sp)
    sd $s3, 24($sp)
    sd $s4, 32($sp)
    sd $s5, 40($sp)
    sd $s6, 48($sp)
    sd $s7, 56($sp)
    sd $fp, 64($sp)
    sd $ra, 72($sp)
    li $s3, -1
    move $s0, $a0
    move $s1, $a1
    move $s2, $a2
    move $a0, $a1
    move $a1, $a2
    daddiu $a2, $sp, 80
    balc qoi_validate
    nop
after_sv_validate:
    nop
    bnezc $v0, save_restore
    nop
    move $a0, $s0
    li $a1, 1
    li $v0, 13
    syscall
    bltzc $v0, save_io_error
    nop
    move $s3, $v0
    move $s4, $zero
after_sv_open:
    nop
save_write:
    beqc $s4, $s2, save_done
    nop
    move $a0, $s3
    daddu $a1, $s1, $s4
    dsubu $a2, $s2, $s4
    li $v0, 15
    syscall
    blezc $v0, save_write_error
    nop
    daddu $s4, $s4, $v0
after_sv_write:
    nop
    bc save_write
    nop
save_write_error:
    move $a0, $s3
    li $v0, 16
    syscall
save_io_error:
    li $v0, 3
    move $v1, $zero
    bc save_restore
    nop
save_done:
    move $a0, $s3
    li $v0, 16
    syscall
    move $v0, $zero
    move $v1, $s2
after_sv_finish:
    nop
    bc save_restore
    nop
save_development:
    bltzc $s3, save_development_status
    nop
    move $a0, $s3
    li $v0, 16
    syscall
save_development_status:
    li $v0, 4
    move $v1, $zero
    bc save_restore
    nop
save_restore:
    ld $s0, 0($sp)
    ld $s1, 8($sp)
    ld $s2, 16($sp)
    ld $s3, 24($sp)
    ld $s4, 32($sp)
    ld $s5, 40($sp)
    ld $s6, 48($sp)
    ld $s7, 56($sp)
    ld $fp, 64($sp)
    ld $ra, 72($sp)
    daddiu $sp, $sp, 128
    jrc $ra
    nop
