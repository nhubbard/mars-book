.data
clip_input_path:
    .asciiz "clip.yuv"
    .space 4087
clip_input_path_end:
clip_stream_path:
    .asciiz "encoded.263"
    .space 4084
clip_stream_path_end:
clip_reconstruction_path:
    .asciiz "reconstructed.yuv"
    .space 4078
clip_reconstruction_path_end:
.align 4
clip_raw: .byte -91:38016
clip_reference_a: .byte -91:38016
clip_reference_b: .byte -91:38016
clip_workspace: .byte -91:169088
clip_encoded: .byte -91:131072
.align 3
clip_request: .dword 0,0,0,8,6,8,1,0,0,0,0,0,0
clip_progress: .dword -1,-1,-1
clip_status: .dword -1
clip_accepted: .dword -1
clip_message: .asciiz "Clip failed with status "
.text
main:
    andi $t0, $sp, 15
    dsubu $sp, $sp, $t0
    dla $a0, clip_request
    dla $t0, clip_input_path
    sd $t0, 0($a0)
    dla $t0, clip_stream_path
    sd $t0, 8($a0)
    dla $t0, clip_reconstruction_path
    sd $t0, 16($a0)
    dla $t0, clip_raw
    sd $t0, 56($a0)
    dla $t0, clip_reference_a
    sd $t0, 64($a0)
    dla $t0, clip_reference_b
    sd $t0, 72($a0)
    dla $t0, clip_workspace
    sd $t0, 80($a0)
    dla $t0, clip_encoded
    sd $t0, 88($a0)
    dla $a1, clip_progress
    jal h263_encode_clip
    nop
clip_caller_return:
    dla $t0, clip_status
    sd $v0, 0($t0)
    dla $t0, clip_accepted
    sd $v1, 0($t0)
    bnez $v0, clip_caller_failed
    nop
    move $a0, $v1
    li $v0, 1
    syscall
    li $a0, 10
    li $v0, 11
    syscall
    li $v0, 10
    syscall
clip_caller_failed:
    dla $a0, clip_message
    li $v0, 4
    syscall
    dla $t0, clip_status
    ld $a0, 0($t0)
    li $v0, 1
    syscall
    li $a0, 10
    li $v0, 11
    syscall
    li $a0, 1
    li $v0, 17
    syscall
h263_encode_clip:
    li $v0, 7
    move $v1, $zero
    jr $ra
    nop
# Replace the picture dependency group with your own completed entry and data.
h263_encode_picture:
    li $v0, 4
    move $v1, $zero
    jr $ra
    nop
