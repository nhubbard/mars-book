# Work on h263_encode_picture; retain your previously completed dependencies.
.data
.align 4
picture_current: .byte -128:38016
picture_reference: .byte -128:38016
picture_reconstruction: .byte -91:38016
picture_workspace: .byte -91:169088
picture_encoded: .byte -91:131072
.align 3
picture_context: .dword 0,131072,0
picture_request: .dword 0,0,0,0,0,169088,0,0,8,1,0,0
picture_status: .dword -1
picture_bytes: .dword -1
picture_message: .asciiz "Picture construction is unfinished or failed.\n"
.text
main:
    andi $t0, $sp, 15
    dsubu $sp, $sp, $t0
    dla $a0, picture_request
    dla $t0, picture_current
    sd $t0, 0($a0)
    ld $t0, 48($a0)
    beqz $t0, picture_reference_ready
    nop
    dla $t0, picture_reference
picture_reference_ready:
    sd $t0, 8($a0)
    dla $t0, picture_reconstruction
    sd $t0, 16($a0)
    dla $t0, picture_context
    sd $t0, 24($a0)
    dla $t1, picture_encoded
    sd $t1, 0($t0)
    dla $t0, picture_workspace
    sd $t0, 32($a0)
    jal h263_encode_picture
    nop
picture_caller_return:
    dla $t0, picture_status
    sd $v0, 0($t0)
    dla $t0, picture_bytes
    sd $v1, 0($t0)
    bnez $v0, picture_failed
    nop
    move $a0, $v1
    li $v0, 1
    syscall
    li $a0, 10
    li $v0, 11
    syscall
    li $v0, 10
    syscall
picture_failed:
    dla $a0, picture_message
    li $v0, 4
    syscall
    li $a0, 1
    li $v0, 17
    syscall
h263_encode_picture:
    li $v0, 4
    move $v1, $zero
    jr $ra
    nop
# Replace these four dependency groups with your own completed entries and data.
h263_search16:
h263_predict_mb:
h263_encode_block8:
h263_emit_macroblock:
    li $v0, 4
    move $v1, $zero
    jr $ra
    nop
