.data
input_path: .asciiz "input.wav"
    .space 118
output_path: .asciiz "output.wav"
    .space 117
.align 3
source:
    .byte 82,73,70,70,74,0,0,0,87,65,86,69,74,85,78,75,3,0,0,0,1,2,3,-91,102,109,116,32,16,0,0,0,1,0,2,0,-128,62,0,0,0,-6,0,0,4,0,16,0,100,97,116,97,16,0,0,0,-32,46,32,-47,0,0,-128,62,-64,-32,0,0,-1,127,0,-128,110,111,116,101,1,0,0,0,66,127
    .byte -91:266190
source_end:
metadata: .word 16000,2
    .dword 4
    .byte -91:48
samples: .byte -91:262176
samples_end:
destination: .byte -91:266272
coeff: .half 4096,4096,4096,4096,4096,4096,4096,4096
    .byte -91:48
kernel_window:
    .half 0,0,0,0,0,0,0,12000,0,-8000,32767
    .byte -91:138
kernel_window_end:
kernel_output: .byte -91:160
status: .word -1
.align 3
count: .dword -1
clips: .dword -1
mode: .word 0
.align 3
byte_count: .dword 82
byte_capacity: .dword 266240
kernel_count: .dword 4
workspace: .space 8192
.text
main:
    dla $sp, workspace
    daddiu $sp, $sp, 8192
    dla $t0, clips
    sd $zero, 0($t0)
    dla $t0, mode
    lw $t0, 0($t0)
    beqz $t0, caller_bound
    nop
    li $t1, 1
    beq $t0, $t1, caller_parse
    nop
    li $t1, 2
    beq $t0, $t1, caller_filter
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
    li $t1, 6
    beq $t0, $t1, caller_load
    nop
    li $t1, 7
    beq $t0, $t1, caller_kernel
    nop
    li $v0, 1
    move $v1, $zero
    b results_ready
    nop
caller_bound:
    dla $a0, metadata
bound_call:
    jal wav_bound
    nop
bound_return:
    b results_ready
    nop
caller_parse:
    dla $a0, source
    dla $t0, byte_count
    ld $a1, 0($t0)
    dla $a2, metadata
parse_call:
    jal wav_parse
    nop
parse_return:
    b results_ready
    nop
caller_filter:
    dla $a0, source
    b caller_filter_arguments
    nop
pipeline_filter:
    dla $a0, source
    dla $t0, metadata
    ld $t0, 16($t0)
    daddu $a0, $a0, $t0
caller_filter_arguments:
    dla $a1, metadata
    dla $a2, samples
    dla $a3, coeff
filter_call:
    jal wav_filter
    nop
filter_return:
    bnez $v0, results_ready
    nop
    dla $t0, clips
    sd $v1, 0($t0)
    dla $t0, mode
    lw $t0, 0($t0)
    li $t1, 6
    beq $t0, $t1, caller_encode
    nop
    b results_ready
    nop
caller_encode:
    dla $a0, samples
    dla $a1, metadata
    dla $a2, destination
    dla $t0, byte_capacity
    ld $a3, 0($t0)
encode_call:
    jal wav_encode
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
    jal wav_load
    nop
load_return:
    bnez $v0, results_ready
    nop
    dla $t0, mode
    lw $t0, 0($t0)
    li $t1, 6
    beq $t0, $t1, pipeline_filter
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
    jal wav_save
    nop
save_return:
    b results_ready
    nop
caller_kernel:
    dla $a0, kernel_window
    dla $a1, coeff
    dla $a2, kernel_output
    dla $t0, kernel_count
    ld $a3, 0($t0)
kernel_call:
    jal fir_q15
    nop
kernel_return:
    bltz $v0, kernel_caller_development
    nop
    dla $t0, clips
    sd $v1, 0($t0)
    move $v1, $v0
    move $v0, $zero
    b results_ready
    nop
kernel_caller_development:
    li $v0, 4
    move $v1, $zero
results_ready:
    dla $t0, status
    sw $v0, 0($t0)
    dla $t0, count
    sd $v1, 0($t0)
    li $v0, 10
    syscall
function_return:
    nop
wav_invalid:
    li $v0, 1
    move $v1, $zero
    jr $ra
    nop
wav_capacity:
    li $v0, 2
    move $v1, $zero
    jr $ra
    nop
wav_development:
    li $v0, 4
    move $v1, $zero
    jr $ra
    nop
fir_q15:
# BEGIN kernel_import
    b kernel_missing
    nop
# END kernel_import
after_kernel_import:
    nop
kernel_missing:
    li $v0, -1
    move $v1, $zero
    jr $ra
    nop
wav_bound:
# BEGIN b_fields
    b wav_development
    nop
# END b_fields
after_b_fields:
    nop
# BEGIN b_domain
    b wav_development
    nop
# END b_domain
after_b_domain:
    nop
# BEGIN b_size
    b wav_development
    nop
# END b_size
after_b_size:
    nop
    jr $ra
    nop
wav_parse:
# BEGIN p_count
    b wav_development
    nop
# END p_count
after_p_count:
    nop
    daddiu $sp, $sp, -160
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
    move $s4, $zero
# BEGIN p_magic
    b parse_development
    nop
# END p_magic
after_p_magic:
    nop
# BEGIN p_outer
    b parse_development
    nop
# END p_outer
after_p_outer:
    nop
parse_chunk:
# BEGIN p_chunk
    b parse_development
    nop
# END p_chunk
after_p_chunk:
    nop
# BEGIN p_dispatch
    b parse_development
    nop
# END p_dispatch
after_p_dispatch:
    nop
parse_format:
# BEGIN p_fmt_size
    b parse_development
    nop
# END p_fmt_size
after_p_fmt_size:
    nop
parse_format_size:
# BEGIN p_codec
    b parse_development
    nop
# END p_codec
after_p_codec:
    nop
# BEGIN p_channels
    b parse_development
    nop
# END p_channels
after_p_channels:
    nop
# BEGIN p_rate
    b parse_development
    nop
# END p_rate
after_p_rate:
    nop
# BEGIN p_byte_rate
    b parse_development
    nop
# END p_byte_rate
after_p_byte_rate:
    nop
# BEGIN p_align
    b parse_development
    nop
# END p_align
after_p_align:
    nop
# BEGIN p_bits
    b parse_development
    nop
# END p_bits
after_p_bits:
    nop
# BEGIN p_extension
    b parse_development
    nop
# END p_extension
after_p_extension:
    nop
parse_format_accepted:
# BEGIN p_fmt_accept
    b parse_development
    nop
# END p_fmt_accept
after_p_fmt_accept:
    nop
    b parse_chunk
    nop
parse_data:
# BEGIN p_data
    b parse_development
    nop
# END p_data
after_p_data:
    nop
parse_frame_count:
# BEGIN p_frames
    b parse_development
    nop
# END p_frames
after_p_frames:
    nop
# BEGIN p_record
    b parse_development
    nop
# END p_record
after_p_record:
    nop
    b parse_chunk
    nop
parse_finish:
# BEGIN p_finish
    b parse_development
    nop
# END p_finish
after_p_finish:
    nop
# BEGIN p_publish
    b parse_development
    nop
# END p_publish
after_p_publish:
    nop
    b parse_restore
    nop
parse_invalid:
    li $v0, 1
    move $v1, $zero
    b parse_restore
    nop
parse_development:
    li $v0, 4
    move $v1, $zero
    b parse_restore
    nop
parse_restore:
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
    daddiu $sp, $sp, 160
    jr $ra
    nop
wav_filter:
    daddiu $sp, $sp, -512
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
    move $s3, $a2
    move $s4, $a3
    move $s5, $a1
# BEGIN f_call
    b filter_development
    nop
# END f_call
after_f_call:
    nop
    bnez $v0, filter_restore
    nop
# BEGIN f_state
    b filter_development
    nop
# END f_state
after_f_state:
    nop
    beqz $s1, filter_done
    nop
filter_channel:
# BEGIN f_channel
    b filter_development
    nop
# END f_channel
after_f_channel:
    nop
filter_block:
# BEGIN f_block
    b filter_development
    nop
# END f_block
after_f_block:
    nop
filter_gather_start:
# BEGIN f_gather
    b filter_development
    nop
# END f_gather
after_f_gather:
    nop
filter_kernel_call:
# BEGIN f_kernel
    b filter_development
    nop
# END f_kernel
after_f_kernel:
    nop
    bne $v0, $fp, filter_development
    nop
# BEGIN f_accumulate
    b filter_development
    nop
# END f_accumulate
after_f_accumulate:
    nop
# BEGIN f_scatter
    b filter_development
    nop
# END f_scatter
after_f_scatter:
    nop
# BEGIN f_history_base
    b filter_development
    nop
# END f_history_base
after_f_history_base:
    nop
# BEGIN f_history
    b filter_development
    nop
# END f_history
after_f_history:
    nop
# BEGIN f_advance_block
    b filter_development
    nop
# END f_advance_block
after_f_advance_block:
    nop
# BEGIN f_advance_channel
    b filter_development
    nop
# END f_advance_channel
after_f_advance_channel:
    nop
filter_done:
# BEGIN f_return
    b filter_development
    nop
# END f_return
after_f_return:
    nop
    b filter_restore
    nop
filter_development:
    li $v0, 4
    move $v1, $zero
    b filter_restore
    nop
filter_restore:
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
    daddiu $sp, $sp, 512
    jr $ra
    nop
wav_encode:
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
# BEGIN e_call
    b encode_development
    nop
# END e_call
after_e_call:
    nop
    bnez $v0, encode_restore
    nop
# BEGIN e_capacity
    b encode_development
    nop
# END e_capacity
after_e_capacity:
    nop
# BEGIN e_riff
    b encode_development
    nop
# END e_riff
after_e_riff:
    nop
# BEGIN e_fmt
    b encode_development
    nop
# END e_fmt
after_e_fmt:
    nop
# BEGIN e_rate
    b encode_development
    nop
# END e_rate
after_e_rate:
    nop
# BEGIN e_frame_format
    b encode_development
    nop
# END e_frame_format
after_e_frame_format:
    nop
# BEGIN e_data
    b encode_development
    nop
# END e_data
after_e_data:
    nop
# BEGIN e_copy
    b encode_development
    nop
# END e_copy
after_e_copy:
    nop
# BEGIN e_return
    b encode_development
    nop
# END e_return
after_e_return:
    nop
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
    daddiu $sp, $sp, 128
    jr $ra
    nop
wav_load:
# BEGIN l_bound
    b wav_development
    nop
# END l_bound
after_l_bound:
    nop
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
    move $s0, $a1
    move $s1, $a2
    move $s2, $a3
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
load_eof:
# BEGIN l_finish
    b load_development
    nop
# END l_finish
after_l_finish:
    nop
    b load_restore
    nop
load_read_error:
    move $a0, $s3
    li $v0, 16
    syscall
    li $s3, -1
load_io_error:
    li $v0, 3
load_failed:
    move $v1, $zero
    b load_restore
    nop
load_development:
    bltz $s3, load_development_status
    nop
    move $a0, $s3
    li $v0, 16
    syscall
    li $s3, -1
load_development_status:
    li $v0, 4
    move $v1, $zero
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
wav_save:
# BEGIN sv_bound
    b wav_development
    nop
# END sv_bound
after_sv_bound:
    nop
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
save_done:
# BEGIN sv_finish
    b save_development
    nop
# END sv_finish
after_sv_finish:
    nop
    b save_restore
    nop
save_write_error:
    move $a0, $s3
    li $v0, 16
    syscall
    li $s3, -1
save_io_error:
    li $v0, 3
    move $v1, $zero
    b save_restore
    nop
save_development:
    bltz $s3, save_development_status
    nop
    move $a0, $s3
    li $v0, 16
    syscall
    li $s3, -1
save_development_status:
    li $v0, 4
    move $v1, $zero
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
