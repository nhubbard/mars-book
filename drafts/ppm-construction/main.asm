.data
input_path: .asciiz "input.ppm"
    .space 118
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
.align 3
workspace: .space 4096
status: .word -1
.align 3
count: .dword -1
mode: .word 0
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
    beqz $t0, caller_parse
    nop
    li $t1, 1
    beq $t0, $t1, caller_decode
    nop
    li $t1, 2
    beq $t0, $t1, caller_encode
    nop
    li $t1, 3
    beq $t0, $t1, caller_load
    nop
    li $t1, 4
    beq $t0, $t1, caller_save
    nop
    b caller_load
    nop
caller_parse:
    dla $a0, source
    dla $t0, byte_count
    ld $a1, 0($t0)
    dla $a2, metadata
parse_call:
    jal ppm_parse
    nop
parse_return:
    b results_ready
    nop
caller_decode:
    dla $a0, source
    dla $t0, byte_count
    ld $a1, 0($t0)
    b caller_decode_args
    nop
pipeline_decode:
    dla $a0, source
    move $a1, $v1
caller_decode_args:
    dla $a2, framebuffer
    dla $t0, pixel_capacity
    ld $a3, 0($t0)
decode_call:
    jal ppm_decode
    nop
decode_return:
    bnez $v0, results_ready
    nop
    dla $t0, mode
    lw $t0, 0($t0)
    li $t1, 5
    beq $t0, $t1, pipeline_encode
    nop
    b results_ready
    nop
caller_encode:
    dla $t0, dimensions
    ld $a1, 0($t0)
    b caller_encode_args
    nop
pipeline_encode:
    move $a1, $v1
caller_encode_args:
    dla $a0, framebuffer
    dla $a2, destination
    dla $t0, byte_capacity
    ld $a3, 0($t0)
encode_call:
    jal ppm_encode
    nop
encode_return:
    bnez $v0, results_ready
    nop
    dla $t0, mode
    lw $t0, 0($t0)
    li $t1, 5
    beq $t0, $t1, pipeline_save
    nop
    b results_ready
    nop
caller_load:
    dla $a0, input_path
    dla $a1, source
    dla $t0, byte_capacity
    ld $a2, 0($t0)
    dla $a3, metadata
load_call:
    jal ppm_load
    nop
load_return:
    bnez $v0, results_ready
    nop
    dla $t0, mode
    lw $t0, 0($t0)
    li $t1, 5
    beq $t0, $t1, pipeline_decode
    nop
    b results_ready
    nop
caller_save:
    dla $a1, source
    dla $t0, byte_count
    ld $a2, 0($t0)
    b caller_save_args
    nop
pipeline_save:
    dla $a1, destination
    move $a2, $v1
caller_save_args:
    dla $a0, output_path
save_call:
    jal ppm_save
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
# BEGIN p_bound
    b ppm_development
    nop
# END p_bound
after_p_bound:
# BEGIN p_magic
    b ppm_development
    nop
# END p_magic
after_p_magic:
# BEGIN p_state
parse_limit_ready:
    b ppm_development
    nop
# END p_state
after_p_state:
# BEGIN p_gap
parse_gap:
parse_skip:
    b ppm_development
    nop
# END p_gap
after_p_gap:
# BEGIN p_leading
    b ppm_development
    nop
# END p_leading
after_p_leading:
# BEGIN p_space
parse_whitespace:
    b ppm_development
    nop
# END p_space
after_p_space:
# BEGIN p_comment
parse_comment:
parse_comment_byte:
    b ppm_development
    nop
# END p_comment
after_p_comment:
# BEGIN p_digits
parse_digits:
    b ppm_development
    nop
# END p_digits
after_p_digits:
# BEGIN p_dimensions
parse_number_done:
parse_height:
    b ppm_development
    nop
# END p_dimensions
after_p_dimensions:
# BEGIN p_extent
parse_maxval:
    b ppm_development
    nop
# END p_extent
after_p_extent:
# BEGIN p_publish
parse_publish:
    b ppm_development
    nop
# END p_publish
after_p_publish:
ppm_invalid:
    li $v0, 1
    move $v1, $zero
    jr $ra
    nop
ppm_bound:
    li $v0, 2
    move $v1, $zero
    jr $ra
    nop
ppm_development:
    li $v0, 4
    move $v1, $zero
    jr $ra
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
# BEGIN d_call
    b decode_development
    nop
# END d_call
after_d_call:
# BEGIN d_capacity
    b decode_development
    nop
# END d_capacity
after_d_capacity:
# BEGIN d_cursor
    b decode_development
    nop
# END d_cursor
after_d_cursor:
# BEGIN d_load
decode_pixel:
    b decode_development
    nop
# END d_load
after_d_load:
# BEGIN d_pack
    b decode_development
    nop
# END d_pack
after_d_pack:
# BEGIN d_store
    b decode_development
    nop
# END d_store
after_d_store:
# BEGIN d_return
    b decode_development
    nop
# END d_return
after_d_return:
decode_capacity:
    li $v0, 2
    move $v1, $zero
    b decode_restore
    nop
decode_development:
    li $v0, 4
    move $v1, $zero
    b decode_restore
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
    jr $ra
    nop

ppm_encode:
# BEGIN e_dimensions
    b ppm_development
    nop
# END e_dimensions
after_e_dimensions:
# BEGIN e_length
    b ppm_development
    nop
# END e_length
after_e_length:
# BEGIN e_capacity
    b ppm_development
    nop
# END e_capacity
after_e_capacity:
# BEGIN e_magic
    b ppm_development
    nop
# END e_magic
after_e_magic:
# BEGIN e_width
encode_width_digit:
    b ppm_development
    nop
# END e_width
after_e_width:
# BEGIN e_height
encode_height_digit:
    b ppm_development
    nop
# END e_height
after_e_height:
# BEGIN e_maxval
    b ppm_development
    nop
# END e_maxval
after_e_maxval:
# BEGIN e_rgb
encode_pixel:
    b ppm_development
    nop
# END e_rgb
after_e_rgb:
# BEGIN e_advance
    b ppm_development
    nop
# END e_advance
after_e_advance:

ppm_load:
# BEGIN l_bound
    b ppm_development
    nop
# END l_bound
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
# BEGIN l_open
    b load_development
    nop
# END l_open
after_l_open:
# BEGIN l_read
load_read:
    b load_development
    nop
# END l_read
after_l_read:
# BEGIN l_probe
load_probe:
    b load_development
    nop
# END l_probe
after_l_probe:
load_read_error:
    move $a0, $s3
    li $v0, 16
    syscall
load_io_error:
    li $v0, 3
load_failed:
    move $v1, $zero
    b load_restore
    nop
# BEGIN l_finish
load_eof:
    b load_development
    nop
# END l_finish
after_l_finish:
    b load_restore
    nop
load_development:
    bltz $s3, load_development_status
    nop
    move $a0, $s3
    li $v0, 16
    syscall
load_development_status:
    li $v0, 4
    move $v1, $zero
    b load_restore
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
    jr $ra
    nop

ppm_save:
# BEGIN sv_bound
    b ppm_development
    nop
# END sv_bound
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
# BEGIN sv_validate
    b save_development
    nop
# END sv_validate
after_sv_validate:
# BEGIN sv_open
    b save_development
    nop
# END sv_open
after_sv_open:
# BEGIN sv_write
save_write:
    b save_development
    nop
# END sv_write
after_sv_write:
save_write_error:
    move $a0, $s3
    li $v0, 16
    syscall
save_io_error:
    li $v0, 3
    move $v1, $zero
    b save_restore
    nop
# BEGIN sv_finish
save_done:
    b save_development
    nop
# END sv_finish
after_sv_finish:
    b save_restore
    nop
save_development:
    bltz $s3, save_development_status
    nop
    move $a0, $s3
    li $v0, 16
    syscall
save_development_status:
    li $v0, 4
    move $v1, $zero
    b save_restore
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
    jr $ra
    nop
