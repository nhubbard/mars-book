.data
input_path: .asciiz "absent-input.ppm"
    .space 111
output_path: .asciiz "output.ppm"
    .space 117
source: .byte 80,54,10,50,32,50,10,50,53,53,10,-1,0,0,0,-1,0,0,0,-1,-1,-1,-1
    .byte -91:12553
source_end:
.align 3
metadata: .byte -91:64
framebuffer: .byte 0,0,-1,0,0,-1,0,0,-1,0,0,0,-1,-1,-1,0
    .byte -91:16432
framebuffer_end:
destination: .byte -91:12576
.align 4
workspace: .space 4096
status: .word -1
.align 3
count: .dword -1
mode: .word 3
.align 3
byte_count: .dword 23
pixel_capacity: .dword 4096
dimensions: .dword 8589934594
byte_capacity: .dword 12544
.text
main:
    dla $sp, workspace
    daddiu $sp, $sp, 4096
    dla $t0, mode
    lw $t0, 0($t0)
    beqzc $t0, caller_parse
    nop
    li $t1, 1
    beqc $t0, $t1, caller_decode
    nop
    li $t1, 2
    beqc $t0, $t1, caller_encode
    nop
    li $t1, 3
    beqc $t0, $t1, caller_load
    nop
    li $t1, 4
    beqc $t0, $t1, caller_save
    nop
    bc caller_load
    nop
caller_parse:
    dla $a0, source
    dla $t0, byte_count
    ld $a1, 0($t0)
    dla $a2, metadata
parse_call:
    balc ppm_parse
    nop
parse_return:
    bc results_ready
    nop
caller_decode:
    dla $a0, source
    dla $t0, byte_count
    ld $a1, 0($t0)
    bc caller_decode_args
    nop
pipeline_decode:
    dla $a0, source
    move $a1, $v1
caller_decode_args:
    dla $a2, framebuffer
    dla $t0, pixel_capacity
    ld $a3, 0($t0)
decode_call:
    balc ppm_decode
    nop
decode_return:
    bnezc $v0, results_ready
    nop
    dla $t0, mode
    lw $t0, 0($t0)
    li $t1, 5
    beqc $t0, $t1, pipeline_encode
    nop
    bc results_ready
    nop
caller_encode:
    dla $t0, dimensions
    ld $a1, 0($t0)
    bc caller_encode_args
    nop
pipeline_encode:
    move $a1, $v1
caller_encode_args:
    dla $a0, framebuffer
    dla $a2, destination
    dla $t0, byte_capacity
    ld $a3, 0($t0)
encode_call:
    balc ppm_encode
    nop
encode_return:
    bnezc $v0, results_ready
    nop
    dla $t0, mode
    lw $t0, 0($t0)
    li $t1, 5
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
    balc ppm_load
    nop
load_return:
    bnezc $v0, results_ready
    nop
    dla $t0, mode
    lw $t0, 0($t0)
    li $t1, 5
    beqc $t0, $t1, pipeline_decode
    nop
    bc results_ready
    nop
caller_save:
    dla $a1, source
    dla $t0, byte_count
    ld $a2, 0($t0)
    bc caller_save_args
    nop
pipeline_save:
    dla $a1, destination
    move $a2, $v1
caller_save_args:
    dla $a0, output_path
save_call:
    balc ppm_save
    nop
save_return:
results_ready:
    dla $t0, status
    sw $v0, 0($t0)
    dla $t0, count
    sd $v1, 0($t0)
function_return:
    li $v0, 10
    syscall

ppm_parse:
    li $t0, 12544
    sltu $t0, $t0, $a1
    bnezc $t0, ppm_bound
    nop
    sltiu $t0, $a1, 3
    bnezc $t0, ppm_invalid
    nop
after_p_bound:
    lbu $t0, 0($a0)
    li $t1, 80
    bnec $t0, $t1, ppm_invalid
    nop
    lbu $t0, 1($a0)
    li $t1, 54
    bnec $t0, $t1, ppm_invalid
    nop
after_p_magic:
    move $t1, $a1
    sltiu $t0, $t1, 256
    bnezc $t0, parse_limit_ready
    nop
    li $t1, 256
parse_limit_ready:
    li $t0, 2
    move $t5, $zero
after_p_state:
parse_gap:
    move $t4, $zero
parse_skip:
    sltu $t8, $t0, $t1
    beqzc $t8, ppm_invalid
    nop
    daddu $t8, $a0, $t0
    lbu $t2, 0($t8)
    li $t8, 35
    beqc $t2, $t8, parse_comment
    nop
    li $t8, 32
    beqc $t2, $t8, parse_whitespace
    nop
    addiu $t8, $t2, -9
    sltiu $t8, $t8, 5
    bnezc $t8, parse_whitespace
    nop
after_p_gap:
    beqzc $t4, ppm_invalid
    nop
    addiu $t8, $t2, -49
    sltiu $t8, $t8, 9
    beqzc $t8, ppm_invalid
    nop
    move $t3, $zero
    bc parse_digits
    nop
after_p_leading:
parse_whitespace:
    li $t4, 1
    daddiu $t0, $t0, 1
    bc parse_skip
    nop
after_p_space:
parse_comment:
    li $t4, 1
parse_comment_byte:
    daddiu $t0, $t0, 1
    sltu $t8, $t0, $t1
    beqzc $t8, ppm_invalid
    nop
    daddu $t8, $a0, $t0
    lbu $t2, 0($t8)
    li $t8, 10
    bnec $t2, $t8, parse_comment_byte
    nop
    bc parse_whitespace
    nop
after_p_comment:
parse_digits:
    sltu $t8, $t0, $t1
    beqzc $t8, ppm_invalid
    nop
    daddu $t8, $a0, $t0
    lbu $t2, 0($t8)
    addiu $t9, $t2, -48
    sltiu $t8, $t9, 10
    beqzc $t8, parse_number_done
    nop
    li $t8, 10
    dmulu $t3, $t3, $t8
    daddu $t3, $t3, $t9
    sltiu $t8, $t3, 256
    beqzc $t8, ppm_invalid
    nop
    daddiu $t0, $t0, 1
    bc parse_digits
    nop
after_p_digits:
parse_number_done:
    li $t8, 2
    beqc $t5, $t8, parse_maxval
    nop
    sltiu $t8, $t3, 65
    beqzc $t8, ppm_invalid
    nop
    bnezc $t5, parse_height
    nop
    move $t6, $t3
    li $t5, 1
    bc parse_gap
    nop
parse_height:
    move $t7, $t3
    li $t5, 2
    bc parse_gap
    nop
after_p_dimensions:
parse_maxval:
    li $t8, 255
    bnec $t3, $t8, ppm_invalid
    nop
    li $t8, 10
    bnec $t2, $t8, ppm_invalid
    nop
    daddiu $t0, $t0, 1
    dmulu $v1, $t6, $t7
    dsll $t8, $v1, 1
    daddu $t8, $t8, $v1
    dsubu $t9, $a1, $t0
    bnec $t8, $t9, ppm_invalid
    nop
after_p_extent:
parse_publish:
    sw $t6, 0($a2)
    sw $t7, 4($a2)
    sd $t0, 8($a2)
    move $v0, $zero
    jrc $ra
    nop
after_p_publish:
ppm_invalid:
    li $v0, 1
    move $v1, $zero
    jrc $ra
    nop
ppm_bound:
    li $v0, 2
    move $v1, $zero
    jrc $ra
    nop
ppm_development:
    li $v0, 4
    move $v1, $zero
    jrc $ra
    nop

ppm_decode:
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
    move $s1, $a2
    move $s2, $a3
    daddiu $a2, $sp, 80
    balc ppm_parse
    nop
    bnezc $v0, decode_restore
    nop
after_d_call:
    sltu $t0, $s2, $v1
    bnezc $t0, decode_capacity
    nop
after_d_capacity:
    ld $t0, 88($sp)
    daddu $t0, $s0, $t0
    move $t1, $v1
    move $t2, $s1
after_d_cursor:
decode_pixel:
    lbu $t3, 0($t0)
    lbu $t4, 1($t0)
    lbu $t5, 2($t0)
after_d_load:
    sll $t3, $t3, 16
    sll $t4, $t4, 8
    or $t3, $t3, $t4
    or $t3, $t3, $t5
after_d_pack:
    sw $t3, 0($t2)
    daddiu $t0, $t0, 3
    daddiu $t2, $t2, 4
    daddiu $t1, $t1, -1
    bnezc $t1, decode_pixel
    nop
after_d_store:
    lwu $t0, 80($sp)
    lwu $t1, 84($sp)
    dsll $v1, $t1, 32
    or $v1, $v1, $t0
    bc decode_restore
    nop
after_d_return:
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

ppm_encode:
    dsll $t0, $a1, 32
    dsrl $t0, $t0, 32
    dsrl $t1, $a1, 32
    beqzc $t0, ppm_invalid
    nop
    beqzc $t1, ppm_invalid
    nop
    sltiu $t2, $t0, 65
    beqzc $t2, ppm_invalid
    nop
    sltiu $t2, $t1, 65
    beqzc $t2, ppm_invalid
    nop
after_e_dimensions:
    dmulu $t2, $t0, $t1
    dsll $t3, $t2, 1
    daddu $t3, $t3, $t2
    daddiu $t3, $t3, 11
    sltiu $t4, $t0, 10
    xori $t4, $t4, 1
    daddu $t3, $t3, $t4
    sltiu $t4, $t1, 10
    xori $t4, $t4, 1
    daddu $t3, $t3, $t4
after_e_length:
    sltu $t4, $a3, $t3
    bnezc $t4, ppm_bound
    nop
    move $v1, $t3
after_e_capacity:
    move $t3, $a2
    li $t4, 80
    sb $t4, 0($t3)
    li $t4, 54
    sb $t4, 1($t3)
    li $t4, 10
    sb $t4, 2($t3)
    daddiu $t3, $t3, 3
after_e_magic:
    li $t4, 10
    ddivu $t5, $t0, $t4
    beqzc $t5, encode_width_digit
    nop
    addiu $t5, $t5, 48
    sb $t5, 0($t3)
    daddiu $t3, $t3, 1
encode_width_digit:
    dmodu $t5, $t0, $t4
    addiu $t5, $t5, 48
    sb $t5, 0($t3)
    li $t5, 32
    sb $t5, 1($t3)
    daddiu $t3, $t3, 2
after_e_width:
    ddivu $t5, $t1, $t4
    beqzc $t5, encode_height_digit
    nop
    addiu $t5, $t5, 48
    sb $t5, 0($t3)
    daddiu $t3, $t3, 1
encode_height_digit:
    dmodu $t5, $t1, $t4
    addiu $t5, $t5, 48
    sb $t5, 0($t3)
    li $t5, 10
    sb $t5, 1($t3)
after_e_height:
    li $t5, 50
    sb $t5, 2($t3)
    li $t5, 53
    sb $t5, 3($t3)
    sb $t5, 4($t3)
    li $t5, 10
    sb $t5, 5($t3)
    daddiu $t3, $t3, 6
after_e_maxval:
    move $t0, $a0
encode_pixel:
    lwu $t4, 0($t0)
    srl $t5, $t4, 16
    sb $t5, 0($t3)
    srl $t5, $t4, 8
    sb $t5, 1($t3)
    sb $t4, 2($t3)
after_e_rgb:
    daddiu $t0, $t0, 4
    daddiu $t3, $t3, 3
    daddiu $t2, $t2, -1
    bnezc $t2, encode_pixel
    nop
    move $v0, $zero
    jrc $ra
    nop

after_e_advance:

ppm_load:
    beqzc $a2, ppm_bound
    nop
    li $t0, 12544
    sltu $t0, $t0, $a2
    bnezc $t0, ppm_bound
    nop
after_l_bound:
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
    bc load_read
    nop
after_l_read:
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
    balc ppm_parse
    nop
    bnezc $v0, load_restore
    nop
    move $v1, $s4
after_l_finish:
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

ppm_save:
    beqzc $a2, ppm_invalid
    nop
    li $t0, 12544
    sltu $t0, $t0, $a2
    bnezc $t0, ppm_bound
    nop
after_sv_bound:
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
    balc ppm_parse
    nop
    bnezc $v0, save_restore
    nop
after_sv_validate:
    move $a0, $s0
    li $a1, 1
    li $v0, 13
    syscall
    bltzc $v0, save_io_error
    nop
    move $s3, $v0
    move $s4, $zero
after_sv_open:
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
    bc save_write
    nop
after_sv_write:
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
