# Independent frame-reversal exercise. Supplied WAV helpers are included below.
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
    beqzc $t0, caller_bound
    nop
    li $t1, 1
    beqc $t0, $t1, caller_parse
    nop
    li $t1, 2
    beqc $t0, $t1, caller_filter
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
    li $t1, 6
    beqc $t0, $t1, caller_load
    nop
    li $t1, 7
    beqc $t0, $t1, caller_kernel
    nop
    li $v0, 1
    move $v1, $zero
    bc results_ready
    nop
caller_bound:
    dla $a0, metadata
bound_call:
    balc wav_bound
    nop
bound_return:
    bc results_ready
    nop
caller_parse:
    dla $a0, source
    dla $t0, byte_count
    ld $a1, 0($t0)
    dla $a2, metadata
parse_call:
    balc wav_parse
    nop
parse_return:
    bc results_ready
    nop
caller_filter:
    dla $a0, source
    bc caller_filter_arguments
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
    balc wav_filter
    nop
filter_return:
    bnezc $v0, results_ready
    nop
    dla $t0, clips
    sd $v1, 0($t0)
    dla $t0, mode
    lw $t0, 0($t0)
    li $t1, 6
    beqc $t0, $t1, reverse_pipeline_transform
    nop
    bc results_ready
    nop
reverse_pipeline_transform:
    dla $a0, samples
    dla $a1, metadata
    move $a2, $a0
    balc wav_reverse_frames
    nop
    bnezc $v0, results_ready
    nop
    bc caller_encode
    nop
caller_encode:
    dla $a0, samples
    dla $a1, metadata
    dla $a2, destination
    dla $t0, byte_capacity
    ld $a3, 0($t0)
encode_call:
    balc wav_encode
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
    balc wav_load
    nop
load_return:
    bnezc $v0, results_ready
    nop
    dla $t0, mode
    lw $t0, 0($t0)
    li $t1, 6
    beqc $t0, $t1, pipeline_filter
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
    balc wav_save
    nop
save_return:
    bc results_ready
    nop
caller_kernel:
    dla $a0, kernel_window
    dla $a1, coeff
    dla $a2, kernel_output
    dla $t0, kernel_count
    ld $a3, 0($t0)
kernel_call:
    balc fir_q15
    nop
kernel_return:
    bltzc $v0, kernel_caller_development
    nop
    dla $t0, clips
    sd $v1, 0($t0)
    move $v1, $v0
    move $v0, $zero
    bc results_ready
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
# Complete shared WAV processing functions for Chapter 23.
wav_invalid:
    li $v0, 1
    move $v1, $zero
    jrc $ra
    nop
wav_capacity:
    li $v0, 2
    move $v1, $zero
    jrc $ra
    nop
wav_development:
    li $v0, 4
    move $v1, $zero
    jrc $ra
    nop
fir_q15:
    move $v0, $a3
    move $v1, $zero
    beqzc $a3, fir_return
    nop
fir_window:
    move $t0, $zero
    move $t1, $a0
    move $t2, $a1
    li $t3, 8
fir_tap:
    lbu $t4, 0($t1)
    lbu $t5, 1($t1)
    sll $t5, $t5, 8
    or $t4, $t4, $t5
    seh $t4, $t4
    lbu $t5, 0($t2)
    lbu $t6, 1($t2)
    sll $t6, $t6, 8
    or $t5, $t5, $t6
    seh $t5, $t5
    dmul $t7, $t4, $t5
    daddu $t0, $t0, $t7
    daddiu $t1, $t1, 2
    daddiu $t2, $t2, 2
    addiu $t3, $t3, -1
    bnezc $t3, fir_tap
    nop
fir_round:
    daddiu $t0, $t0, 16384
    dsra $t0, $t0, 15
    li $t8, 32767
    slt $t9, $t8, $t0
    bnezc $t9, fir_high
    nop
    li $t8, -32768
    slt $t9, $t0, $t8
    bnezc $t9, fir_low
    nop
    bc fir_store
    nop
fir_high:
    li $t0, 32767
    bc fir_clipped
    nop
fir_low:
    li $t0, -32768
fir_clipped:
    addiu $v1, $v1, 1
fir_store:
    sb $t0, 0($a2)
    dsrl $t1, $t0, 8
    sb $t1, 1($a2)
    daddiu $a0, $a0, 2
    daddiu $a2, $a2, 2
    addiu $a3, $a3, -1
    bnezc $a3, fir_window
    nop
fir_return:
    jrc $ra
    nop
after_kernel_import:
    nop
kernel_missing:
    li $v0, -1
    move $v1, $zero
    jrc $ra
    nop
wav_bound:
    lwu $t0, 0($a0)
    lwu $t1, 4($a0)
    ld $t2, 8($a0)
after_b_fields:
    nop
    sltiu $t3, $t0, 8000
    bnezc $t3, wav_invalid
    nop
    li $t3, 48000
    sltu $t3, $t3, $t0
    bnezc $t3, wav_invalid
    nop
    beqzc $t1, wav_invalid
    nop
    sltiu $t3, $t1, 3
    beqzc $t3, wav_invalid
    nop
    li $t3, 65536
    sltu $t3, $t3, $t2
    bnezc $t3, wav_invalid
    nop
after_b_domain:
    nop
    mul $v1, $t2, $t1
    dsll $v1, $v1, 1
    daddiu $v1, $v1, 44
    move $v0, $zero
after_b_size:
    nop
    jrc $ra
    nop
wav_parse:
    li $t0, 266240
    sltu $t0, $t0, $a1
    bnezc $t0, wav_capacity
    nop
    sltiu $t0, $a1, 44
    bnezc $t0, wav_invalid
    nop
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
    lbu $t0, 0($s0)
    lbu $t1, 1($s0)
    dsll $t1, $t1, 8
    or $t0, $t0, $t1
    lbu $t1, 2($s0)
    dsll $t1, $t1, 16
    or $t0, $t0, $t1
    lbu $t1, 3($s0)
    dsll $t1, $t1, 24
    or $t0, $t0, $t1
    li $t2, 0x46464952
    bnec $t0, $t2, parse_invalid
    nop
    lbu $t0, 8($s0)
    lbu $t1, 9($s0)
    dsll $t1, $t1, 8
    or $t0, $t0, $t1
    lbu $t1, 10($s0)
    dsll $t1, $t1, 16
    or $t0, $t0, $t1
    lbu $t1, 11($s0)
    dsll $t1, $t1, 24
    or $t0, $t0, $t1
    li $t2, 0x45564157
    bnec $t0, $t2, parse_invalid
    nop
after_p_magic:
    nop
    lbu $t0, 4($s0)
    lbu $t1, 5($s0)
    dsll $t1, $t1, 8
    or $t0, $t0, $t1
    lbu $t1, 6($s0)
    dsll $t1, $t1, 16
    or $t0, $t0, $t1
    lbu $t1, 7($s0)
    dsll $t1, $t1, 24
    or $t0, $t0, $t1
    daddiu $t0, $t0, 8
    bnec $t0, $s1, parse_invalid
    nop
    li $s3, 12
after_p_outer:
    nop
parse_chunk:
    beqc $s3, $s1, parse_finish
    nop
    dsubu $t0, $s1, $s3
    sltiu $t1, $t0, 8
    bnezc $t1, parse_invalid
    nop
    daddu $t9, $s0, $s3
    lbu $t8, 4($t9)
    lbu $t1, 5($t9)
    dsll $t1, $t1, 8
    or $t8, $t8, $t1
    lbu $t1, 6($t9)
    dsll $t1, $t1, 16
    or $t8, $t8, $t1
    lbu $t1, 7($t9)
    dsll $t1, $t1, 24
    or $t8, $t8, $t1
    daddiu $t9, $t9, 8
    andi $t7, $t8, 1
    daddu $t7, $t7, $t8
    daddiu $t4, $s3, 8
    daddu $t4, $t4, $t7
    sltu $t0, $s1, $t4
    bnezc $t0, parse_invalid
    nop
    move $t2, $t9
    move $t3, $t8
after_p_chunk:
    nop
    daddiu $t6, $t9, -8
    lbu $t0, 0($t6)
    lbu $t1, 1($t6)
    dsll $t1, $t1, 8
    or $t0, $t0, $t1
    lbu $t1, 2($t6)
    dsll $t1, $t1, 16
    or $t0, $t0, $t1
    lbu $t1, 3($t6)
    dsll $t1, $t1, 24
    or $t0, $t0, $t1
    li $t2, 0x20746d66
    beqc $t0, $t2, parse_format
    nop
    li $t2, 0x61746164
    beqc $t0, $t2, parse_data
    nop
    move $s3, $t4
    bc parse_chunk
    nop
after_p_dispatch:
    nop
parse_format:
    bnezc $s4, parse_invalid
    nop
    li $t0, 16
    beqc $t8, $t0, parse_format_size
    nop
    li $t0, 18
    bnec $t8, $t0, parse_invalid
    nop
after_p_fmt_size:
    nop
parse_format_size:
    lbu $t0, 0($t9)
    lbu $t1, 1($t9)
    dsll $t1, $t1, 8
    or $t0, $t0, $t1
    li $t2, 1
    bnec $t0, $t2, parse_invalid
    nop
after_p_codec:
    nop
    lbu $s6, 2($t9)
    lbu $t1, 3($t9)
    dsll $t1, $t1, 8
    or $s6, $s6, $t1
    beqzc $s6, parse_invalid
    nop
    sltiu $t0, $s6, 3
    beqzc $t0, parse_invalid
    nop
after_p_channels:
    nop
    lbu $s5, 4($t9)
    lbu $t1, 5($t9)
    dsll $t1, $t1, 8
    or $s5, $s5, $t1
    lbu $t1, 6($t9)
    dsll $t1, $t1, 16
    or $s5, $s5, $t1
    lbu $t1, 7($t9)
    dsll $t1, $t1, 24
    or $s5, $s5, $t1
    sltiu $t0, $s5, 8000
    bnezc $t0, parse_invalid
    nop
    li $t0, 48000
    sltu $t0, $t0, $s5
    bnezc $t0, parse_invalid
    nop
after_p_rate:
    nop
    lbu $t0, 8($t9)
    lbu $t1, 9($t9)
    dsll $t1, $t1, 8
    or $t0, $t0, $t1
    lbu $t1, 10($t9)
    dsll $t1, $t1, 16
    or $t0, $t0, $t1
    lbu $t1, 11($t9)
    dsll $t1, $t1, 24
    or $t0, $t0, $t1
    mul $t2, $s5, $s6
    dsll $t2, $t2, 1
    bnec $t0, $t2, parse_invalid
    nop
after_p_byte_rate:
    nop
    lbu $t0, 12($t9)
    lbu $t1, 13($t9)
    dsll $t1, $t1, 8
    or $t0, $t0, $t1
    dsll $t2, $s6, 1
    bnec $t0, $t2, parse_invalid
    nop
after_p_align:
    nop
    lbu $t0, 14($t9)
    lbu $t1, 15($t9)
    dsll $t1, $t1, 8
    or $t0, $t0, $t1
    li $t2, 16
    bnec $t0, $t2, parse_invalid
    nop
after_p_bits:
    nop
    li $t0, 18
    bnec $t8, $t0, parse_format_accepted
    nop
    lbu $t0, 16($t9)
    lbu $t1, 17($t9)
    dsll $t1, $t1, 8
    or $t0, $t0, $t1
    bnezc $t0, parse_invalid
    nop
after_p_extension:
    nop
parse_format_accepted:
    ori $s4, $s4, 1
    move $s3, $t4
after_p_fmt_accept:
    nop
    bc parse_chunk
    nop
parse_data:
    li $t0, 1
    bnec $s4, $t0, parse_invalid
    nop
    dsubu $s7, $t9, $s0
    move $fp, $t8
after_p_data:
    nop
parse_frame_count:
    dsll $t0, $s6, 1
    daddiu $t0, $t0, -1
    and $t0, $t0, $fp
    bnezc $t0, parse_invalid
    nop
    dsrl $t2, $fp, 1
    li $t0, 1
    beqc $s6, $t0, parse_divided
    nop
    dsrl $t2, $t2, 1
parse_divided:
    li $t0, 65536
    sltu $t0, $t0, $t2
    bnezc $t0, parse_invalid
    nop
    sd $t2, 80($sp)
after_p_frames:
    nop
    ori $s4, $s4, 2
    move $s3, $t4
after_p_record:
    nop
    bc parse_chunk
    nop
parse_finish:
    li $t0, 3
    bnec $s4, $t0, parse_invalid
    nop
    dsubu $t0, $s1, $fp
    li $t1, 4096
    sltu $t0, $t1, $t0
    bnezc $t0, parse_invalid
    nop
after_p_finish:
    nop
    sw $s5, 0($s2)
    sw $s6, 4($s2)
    ld $v1, 80($sp)
    sd $v1, 8($s2)
    sd $s7, 16($s2)
    sd $fp, 24($s2)
    move $v0, $zero
after_p_publish:
    nop
    bc parse_restore
    nop
parse_invalid:
    li $v0, 1
    move $v1, $zero
    bc parse_restore
    nop
parse_development:
    li $v0, 4
    move $v1, $zero
    bc parse_restore
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
    jrc $ra
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
    move $a0, $a1
    balc wav_bound
    nop
after_f_call:
    nop
    bnezc $v0, filter_restore
    nop
    ld $s1, 8($s5)
    lwu $s2, 4($s5)
    move $s5, $zero
    move $s7, $zero
after_f_state:
    nop
    beqzc $s1, filter_done
    nop
filter_channel:
    sd $zero, 112($sp)
    sd $zero, 120($sp)
    move $s6, $zero
after_f_channel:
    nop
filter_block:
    dsubu $fp, $s1, $s6
    sltiu $t0, $fp, 65
    bnezc $t0, filter_block_sized
    nop
    li $fp, 64
filter_block_sized:
after_f_block:
    nop
filter_gather_start:
    move $t0, $zero
filter_gather:
    daddu $t1, $s6, $t0
    mul $t1, $t1, $s2
    daddu $t1, $t1, $s5
    dsll $t1, $t1, 1
    daddu $t1, $s0, $t1
    lbu $t2, 0($t1)
    lbu $t3, 1($t1)
    dsll $t4, $t0, 1
    daddiu $t4, $t4, 126
    daddu $t4, $sp, $t4
    sb $t2, 0($t4)
    sb $t3, 1($t4)
    daddiu $t0, $t0, 1
    bnec $t0, $fp, filter_gather
    nop
after_f_gather:
    nop
filter_kernel_call:
    daddiu $a0, $sp, 112
    move $a1, $s4
    daddiu $a2, $sp, 256
    move $a3, $fp
    balc fir_q15
    nop
after_f_kernel:
    nop
    bnec $v0, $fp, filter_development
    nop
    daddu $s7, $s7, $v1
after_f_accumulate:
    nop
    move $t0, $zero
filter_scatter:
    dsll $t1, $t0, 1
    daddiu $t1, $t1, 256
    daddu $t1, $sp, $t1
    lbu $t2, 0($t1)
    lbu $t3, 1($t1)
    daddu $t4, $s6, $t0
    mul $t4, $t4, $s2
    daddu $t4, $t4, $s5
    dsll $t4, $t4, 1
    daddu $t4, $s3, $t4
    sb $t2, 0($t4)
    sb $t3, 1($t4)
    daddiu $t0, $t0, 1
    bnec $t0, $fp, filter_scatter
    nop
after_f_scatter:
    nop
    dsll $t1, $fp, 1
    daddiu $t1, $t1, 112
    daddu $t1, $sp, $t1
    daddiu $t2, $sp, 112
    li $t0, 14
after_f_history_base:
    nop
filter_history:
    lbu $t3, 0($t1)
    sb $t3, 0($t2)
    daddiu $t1, $t1, 1
    daddiu $t2, $t2, 1
    daddiu $t0, $t0, -1
    bnezc $t0, filter_history
    nop
after_f_history:
    nop
    daddu $s6, $s6, $fp
    bnec $s6, $s1, filter_block
    nop
after_f_advance_block:
    nop
    daddiu $s5, $s5, 1
    bnec $s5, $s2, filter_channel
    nop
after_f_advance_channel:
    nop
filter_done:
    move $v0, $zero
    move $v1, $s7
after_f_return:
    nop
    bc filter_restore
    nop
filter_development:
    li $v0, 4
    move $v1, $zero
    bc filter_restore
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
    jrc $ra
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
    move $a0, $a1
    balc wav_bound
    nop
    move $s4, $v1
after_e_call:
    nop
    bnezc $v0, encode_restore
    nop
    sltu $t0, $s3, $s4
    bnezc $t0, encode_capacity
    nop
    lwu $s5, 0($s1)
    lwu $s6, 4($s1)
    daddiu $fp, $s4, -44
after_e_capacity:
    nop
    li $t0, 0x46464952
    sb $t0, 0($s2)
    dsrl $t8, $t0, 8
    sb $t8, 1($s2)
    dsrl $t8, $t0, 16
    sb $t8, 2($s2)
    dsrl $t8, $t0, 24
    sb $t8, 3($s2)
    daddiu $t0, $s4, -8
    sb $t0, 4($s2)
    dsrl $t8, $t0, 8
    sb $t8, 5($s2)
    dsrl $t8, $t0, 16
    sb $t8, 6($s2)
    dsrl $t8, $t0, 24
    sb $t8, 7($s2)
    li $t0, 0x45564157
    sb $t0, 8($s2)
    dsrl $t8, $t0, 8
    sb $t8, 9($s2)
    dsrl $t8, $t0, 16
    sb $t8, 10($s2)
    dsrl $t8, $t0, 24
    sb $t8, 11($s2)
after_e_riff:
    nop
    li $t0, 0x20746d66
    sb $t0, 12($s2)
    dsrl $t8, $t0, 8
    sb $t8, 13($s2)
    dsrl $t8, $t0, 16
    sb $t8, 14($s2)
    dsrl $t8, $t0, 24
    sb $t8, 15($s2)
    li $t0, 16
    sb $t0, 16($s2)
    dsrl $t8, $t0, 8
    sb $t8, 17($s2)
    dsrl $t8, $t0, 16
    sb $t8, 18($s2)
    dsrl $t8, $t0, 24
    sb $t8, 19($s2)
    li $t0, 1
    sb $t0, 20($s2)
    dsrl $t8, $t0, 8
    sb $t8, 21($s2)
    move $t0, $s6
    sb $t0, 22($s2)
    dsrl $t8, $t0, 8
    sb $t8, 23($s2)
after_e_fmt:
    nop
    move $t0, $s5
    sb $t0, 24($s2)
    dsrl $t8, $t0, 8
    sb $t8, 25($s2)
    dsrl $t8, $t0, 16
    sb $t8, 26($s2)
    dsrl $t8, $t0, 24
    sb $t8, 27($s2)
    mul $t0, $s5, $s6
    dsll $t0, $t0, 1
    sb $t0, 28($s2)
    dsrl $t8, $t0, 8
    sb $t8, 29($s2)
    dsrl $t8, $t0, 16
    sb $t8, 30($s2)
    dsrl $t8, $t0, 24
    sb $t8, 31($s2)
after_e_rate:
    nop
    dsll $t0, $s6, 1
    sb $t0, 32($s2)
    dsrl $t8, $t0, 8
    sb $t8, 33($s2)
    li $t0, 16
    sb $t0, 34($s2)
    dsrl $t8, $t0, 8
    sb $t8, 35($s2)
after_e_frame_format:
    nop
    li $t0, 0x61746164
    sb $t0, 36($s2)
    dsrl $t8, $t0, 8
    sb $t8, 37($s2)
    dsrl $t8, $t0, 16
    sb $t8, 38($s2)
    dsrl $t8, $t0, 24
    sb $t8, 39($s2)
    move $t0, $fp
    sb $t0, 40($s2)
    dsrl $t8, $t0, 8
    sb $t8, 41($s2)
    dsrl $t8, $t0, 16
    sb $t8, 42($s2)
    dsrl $t8, $t0, 24
    sb $t8, 43($s2)
after_e_data:
    nop
    move $t0, $zero
    daddiu $t1, $s2, 44
encode_copy:
    beqc $t0, $fp, encode_copy_done
    nop
    daddu $t2, $s0, $t0
    daddu $t3, $t1, $t0
    lbu $t4, 0($t2)
    sb $t4, 0($t3)
    daddiu $t0, $t0, 1
    bc encode_copy
    nop
encode_copy_done:
after_e_copy:
    nop
    move $v0, $zero
    move $v1, $s4
after_e_return:
    nop
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
    daddiu $sp, $sp, 128
    jrc $ra
    nop
wav_load:
    beqzc $a2, wav_capacity
    nop
    li $t0, 266240
    sltu $t0, $t0, $a2
    bnezc $t0, wav_capacity
    nop
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
    li $t0, 4096
    sltu $t1, $t0, $a2
    beqzc $t1, load_request_ready
    nop
    move $a2, $t0
load_request_ready:
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
    daddiu $a1, $sp, 112
    li $a2, 1
    li $v0, 14
    syscall
    bltzc $v0, load_read_error
    nop
    beqzc $v0, load_probe_ok
    nop
    move $a0, $s3
    li $v0, 16
    syscall
    li $s3, -1
    li $v0, 2
    bc load_failed
    nop
load_probe_ok:
after_l_probe:
    nop
load_eof:
    move $a0, $s3
    li $v0, 16
    syscall
    li $s3, -1
    move $a0, $s0
    move $a1, $s4
    move $a2, $s2
    balc wav_parse
    nop
    bnezc $v0, load_restore
    nop
    move $v1, $s4
after_l_finish:
    nop
    bc load_restore
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
    bc load_restore
    nop
load_development:
    bltzc $s3, load_development_status
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
    jrc $ra
    nop
wav_save:
    li $t0, 266240
    sltu $t0, $t0, $a2
    bnezc $t0, wav_capacity
    nop
    sltiu $t0, $a2, 44
    bnezc $t0, wav_invalid
    nop
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
    move $a0, $a1
    move $a1, $a2
    daddiu $a2, $sp, 80
    balc wav_parse
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
    li $t0, 4096
    sltu $t1, $t0, $a2
    beqzc $t1, save_request_ready
    nop
    move $a2, $t0
save_request_ready:
    li $v0, 15
    syscall
    blezc $v0, save_write_error
    nop
    daddu $s4, $s4, $v0
after_sv_write:
    nop
    bc save_write
    nop
save_done:
    move $a0, $s3
    li $v0, 16
    syscall
    li $s3, -1
    move $v0, $zero
    move $v1, $s2
after_sv_finish:
    nop
    bc save_restore
    nop
save_write_error:
    move $a0, $s3
    li $v0, 16
    syscall
    li $s3, -1
save_io_error:
    li $v0, 3
    move $v1, $zero
    bc save_restore
    nop
save_development:
    bltzc $s3, save_development_status
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
    jrc $ra
    nop

wav_reverse_frames:
    break
