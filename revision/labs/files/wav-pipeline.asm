# Complete worked PCM WAV pipeline. Keep wav-functions.asm beside this file.
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
    beqc $t0, $t1, caller_encode
    nop
    bc results_ready
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
.include "wav-functions.asm"
