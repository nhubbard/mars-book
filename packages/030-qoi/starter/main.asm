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
.align 3
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
    beqz $t0, caller_bound
    nop
    li $t1, 1
    beq $t0, $t1, caller_validate
    nop
    li $t1, 2
    beq $t0, $t1, caller_decode
    nop
    li $t1, 3
    beq $t0, $t1, caller_encode
    nop
    li $t1, 4
    beq $t0, $t1, caller_load
    nop
    li $t1, 5
    beq $t0, $t1, caller_save
    nop
    b caller_load
    nop
caller_bound:
    dla $t0, dimensions
    ld $a0, 0($t0)
bound_call:
    jal qoi_bound
    nop
bound_return:
    b results_ready
    nop
caller_validate:
    dla $a0, source
    dla $t0, byte_count
    ld $a1, 0($t0)
    dla $a2, metadata
validate_call:
    jal qoi_validate
    nop
validate_return:
    b results_ready
    nop
caller_decode:
    dla $a0, source
    dla $t0, byte_count
    ld $a1, 0($t0)
    b caller_decode_arguments
    nop
pipeline_decode:
    dla $a0, source
    move $a1, $v1
caller_decode_arguments:
    dla $a2, framebuffer
    dla $t0, pixel_capacity
    ld $a3, 0($t0)
decode_call:
    jal qoi_decode
    nop
decode_return:
    bnez $v0, results_ready
    nop
    dla $t0, mode
    lw $t0, 0($t0)
    li $t1, 6
    beq $t0, $t1, caller_encode
    nop
    b results_ready
    nop
caller_encode:
    dla $a0, framebuffer
    dla $a1, metadata
    dla $a2, destination
    dla $t0, byte_capacity
    ld $a3, 0($t0)
encode_call:
    jal qoi_encode
    nop
encode_return:
    bnez $v0, results_ready
    nop
    dla $t0, mode
    lw $t0, 0($t0)
    li $t1, 6
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
    jal qoi_load
    nop
load_return:
    bnez $v0, results_ready
    nop
    dla $t0, mode
    lw $t0, 0($t0)
    li $t1, 6
    beq $t0, $t1, pipeline_decode
    nop
    b results_ready
    nop
caller_save:
    dla $a1, source
    dla $t0, byte_count
    ld $a2, 0($t0)
    b caller_save_arguments
    nop
pipeline_save:
    dla $a1, destination
    move $a2, $v1
caller_save_arguments:
    dla $a0, output_path
save_call:
    jal qoi_save
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
# BEGIN b_unpack
    b qoi_development
    nop
# END b_unpack
after_b_unpack:
    nop
# BEGIN b_domain
    b qoi_development
    nop
# END b_domain
after_b_domain:
    nop
# BEGIN b_size
    b qoi_development
    nop
# END b_size
after_b_size:
    nop
    jr $ra
    nop
qoi_invalid:
    li $v0, 1
    move $v1, $zero
    jr $ra
    nop
qoi_capacity:
    li $v0, 2
    move $v1, $zero
    jr $ra
    nop
qoi_development:
    li $v0, 4
    move $v1, $zero
    jr $ra
    nop
qoi_validate:
# BEGIN v_count
    b qoi_development
    nop
# END v_count
after_v_count:
    nop
    move $a3, $zero
    move $t9, $zero
    b qoi_walk
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
# BEGIN d_call
    b decode_development
    nop
# END d_call
after_d_call:
    nop
    bnez $v0, decode_restore
    nop
# BEGIN d_capacity
    b decode_development
    nop
# END d_capacity
after_d_capacity:
    nop
# BEGIN d_walk
    b decode_development
    nop
# END d_walk
after_d_walk:
    nop
    bnez $v0, decode_restore
    nop
# BEGIN d_return
    b decode_development
    nop
# END d_return
after_d_return:
    nop
    b decode_restore
    nop
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
# BEGIN w_magic
    b walk_development
    nop
# END w_magic
after_w_magic:
    nop
# BEGIN w_dimensions
    b walk_development
    nop
# END w_dimensions
after_w_dimensions:
    nop
# BEGIN w_flags
    b walk_development
    nop
# END w_flags
after_w_flags:
    nop
# BEGIN w_state
    b walk_development
    nop
# END w_state
after_w_state:
    nop
walk_clear:
# BEGIN w_clear
    b walk_development
    nop
# END w_clear
after_w_clear:
    nop
walk_next:
# BEGIN w_next
    b walk_development
    nop
# END w_next
after_w_next:
    nop
# BEGIN w_dispatch
    b walk_development
    nop
# END w_dispatch
after_w_dispatch:
    nop
walk_run:
# BEGIN w_run
    b walk_development
    nop
# END w_run
after_w_run:
    nop
    b walk_publish
    nop
walk_index:
# BEGIN w_index
    b walk_development
    nop
# END w_index
after_w_index:
    nop
    b walk_publish
    nop
walk_rgb:
# BEGIN w_rgb
    b walk_development
    nop
# END w_rgb
after_w_rgb:
    nop
    b walk_pack
    nop
walk_rgba:
# BEGIN w_rgba
    b walk_development
    nop
# END w_rgba
after_w_rgba:
    nop
    b walk_pack
    nop
walk_diff:
# BEGIN w_diff
    b walk_development
    nop
# END w_diff
after_w_diff:
    nop
    b walk_pack
    nop
walk_luma:
# BEGIN w_luma
    b walk_development
    nop
# END w_luma
after_w_luma:
    nop
    b walk_pack
    nop
walk_pack:
# BEGIN w_pack
    b walk_development
    nop
# END w_pack
after_w_pack:
    nop
walk_publish:
# BEGIN w_hash
    b walk_development
    nop
# END w_hash
after_w_hash:
    nop
walk_emit:
# BEGIN w_emit
    b walk_development
    nop
# END w_emit
after_w_emit:
    nop
    b walk_advance
    nop
walk_advance:
# BEGIN w_advance
    b walk_development
    nop
# END w_advance
after_w_advance:
    nop
    b walk_next
    nop
walk_marker:
# BEGIN w_marker
    b walk_development
    nop
# END w_marker
after_w_marker:
    nop
# BEGIN w_meta
    b walk_development
    nop
# END w_meta
after_w_meta:
    nop
    b walk_restore
    nop
walk_invalid:
    li $v0, 1
    move $v1, $zero
    b walk_restore
    nop
walk_development:
    li $v0, 4
    move $v1, $zero
    b walk_restore
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
    jr $ra
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
# BEGIN e_descriptor
    b encode_development
    nop
# END e_descriptor
after_e_descriptor:
    nop
# BEGIN e_capacity
    b encode_development
    nop
# END e_capacity
after_e_capacity:
    nop
# BEGIN e_state
    b encode_development
    nop
# END e_state
after_e_state:
    nop
encode_clear:
# BEGIN e_clear
    b encode_development
    nop
# END e_clear
after_e_clear:
    nop
# BEGIN e_header
    b encode_development
    nop
# END e_header
after_e_header:
    nop
# BEGIN e_previous
    b encode_development
    nop
# END e_previous
after_e_previous:
    nop
encode_pixel:
# BEGIN e_pixel
    b encode_development
    nop
# END e_pixel
after_e_pixel:
    nop
# BEGIN e_hash
    b encode_development
    nop
# END e_hash
after_e_hash:
    nop
# BEGIN e_repeat
    b encode_development
    nop
# END e_repeat
after_e_repeat:
    nop
encode_lookup:
# BEGIN e_lookup
    b encode_development
    nop
# END e_lookup
after_e_lookup:
    nop
# BEGIN e_delta
    b encode_development
    nop
# END e_delta
after_e_delta:
    nop
encode_diff_test:
# BEGIN e_diff
    b encode_development
    nop
# END e_diff
after_e_diff:
    nop
    b encode_finish_pixel
    nop
encode_luma_test:
# BEGIN e_luma
    b encode_development
    nop
# END e_luma
after_e_luma:
    nop
    b encode_finish_pixel
    nop
encode_index:
# BEGIN e_index
    b encode_development
    nop
# END e_index
after_e_index:
    nop
    b encode_finish_pixel
    nop
encode_rgb:
# BEGIN e_rgb
    b encode_development
    nop
# END e_rgb
after_e_rgb:
    nop
    b encode_finish_pixel
    nop
encode_rgba:
# BEGIN e_rgba
    b encode_development
    nop
# END e_rgba
after_e_rgba:
    nop
    b encode_finish_pixel
    nop
encode_run:
# BEGIN e_run
    b encode_development
    nop
# END e_run
after_e_run:
    nop
encode_flush:
# BEGIN e_flush
    b encode_development
    nop
# END e_flush
after_e_flush:
    nop
encode_finish_pixel:
# BEGIN e_finish
    b encode_development
    nop
# END e_finish
after_e_finish:
    nop
# BEGIN e_marker
    b encode_development
    nop
# END e_marker
after_e_marker:
    nop
    b encode_restore
    nop
encode_invalid:
    li $v0, 1
    move $v1, $zero
    b encode_restore
    nop
encode_capacity:
    li $v0, 2
    move $v1, $zero
    b encode_restore
    nop
encode_development:
    li $v0, 4
    move $v1, $zero
    b encode_restore
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
    jr $ra
    nop
qoi_load:
# BEGIN l_bound
    b qoi_development
    nop
# END l_bound
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
# BEGIN l_open
    b load_development
    nop
# END l_open
after_l_open:
    nop
load_read:
# BEGIN l_read
    b load_development
    nop
# END l_read
after_l_read:
    nop
    b load_read
    nop
load_probe:
# BEGIN l_probe
    b load_development
    nop
# END l_probe
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
    b load_restore
    nop
load_eof:
# BEGIN l_finish
    b load_development
    nop
# END l_finish
after_l_finish:
    nop
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
qoi_save:
# BEGIN sv_bound
    b qoi_development
    nop
# END sv_bound
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
# BEGIN sv_validate
    b save_development
    nop
# END sv_validate
after_sv_validate:
    nop
    bnez $v0, save_restore
    nop
# BEGIN sv_open
    b save_development
    nop
# END sv_open
after_sv_open:
    nop
save_write:
# BEGIN sv_write
    b save_development
    nop
# END sv_write
after_sv_write:
    nop
    b save_write
    nop
save_write_error:
    move $a0, $s3
    li $v0, 16
    syscall
save_io_error:
    li $v0, 3
    move $v1, $zero
    b save_restore
    nop
save_done:
# BEGIN sv_finish
    b save_development
    nop
# END sv_finish
after_sv_finish:
    nop
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
