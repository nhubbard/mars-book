# Unfinished native-word XOR/change-count kernel. Six regions are cumulative.
.data
.align 4
left_words: .word -1515870811, 1122867, 4478310, 66051, 11259375, 0
    .byte -91:16424
right_words: .word -1515870811, 1122867, 4478446, 66051, 11193839, 16711680
    .byte -91:16424
out_words: .byte -91:16448
workspace: .byte -91:8192
.align 3
kernel_count: .dword 5
kernel_mode: .word 0
kernel_alias: .word 0
last_status: .dword -1
last_changed: .dword -1
message_development: .asciiz "Kernel is still under construction.\n"
message_invalid: .asciiz "Invalid kernel arguments.\n"
newline: .asciiz "\n"
.text
main:
    dla $a0, left_words
    daddiu $a0, $a0, 4
    dla $a1, right_words
    daddiu $a1, $a1, 4
    dla $a2, out_words
    daddiu $a2, $a2, 4
    dla $t0, kernel_alias
    lwu $t0, 0($t0)
    beqz $t0, main_alias_ready
    nop
    li $t1, 1
    beq $t0, $t1, main_alias_left
    nop
    li $t1, 2
    bne $t0, $t1, main_invalid
    nop
    move $a2, $a1
    b main_alias_ready
    nop
main_alias_left:
    move $a2, $a0
main_alias_ready:
    dla $a3, kernel_count
    ld $a3, 0($a3)
    dla $t0, kernel_mode
    lwu $t0, 0($t0)
    sltiu $t1, $t0, 2
    beqz $t1, main_invalid
    nop
kernel_call:
    beqz $t0, main_scalar
    nop
    jal video_xor_words_msa
    nop
    b kernel_return
    nop
main_scalar:
    jal video_xor_words
    nop
kernel_return:
    dla $t0, last_status
    sd $v0, 0($t0)
    sd $v1, 8($t0)
    li $t0, 4
    beq $v0, $t0, main_development
    nop
    bnez $v0, main_invalid
    nop
    move $a0, $v1
    li $v0, 1
    syscall
    dla $a0, newline
    li $v0, 4
    syscall
    b main_success
    nop
main_development:
    dla $a0, message_development
    li $v0, 4
    syscall
main_success:
    move $a0, $zero
    li $v0, 17
    syscall
main_invalid:
    dla $a0, message_invalid
    li $v0, 4
    syscall
    li $a0, 1
    li $v0, 17
    syscall
function_return:
    nop
video_xor_words:
    move $t9, $zero
    b video_entry
    nop
video_xor_words_msa:
    li $t9, 1
video_entry:
    li $v0, 1
    move $v1, $zero
# BEGIN video_admit
    b video_missing
    nop
# END video_admit
video_admitted:
    nop
    beqz $t9, video_scalar
    nop
video_vector:
# BEGIN video_group_bound
    b video_missing
    nop
# END video_group_bound
video_group_ready:
    nop
    bnez $t0, video_tail
    nop
# BEGIN video_group_xor
    b video_missing
    nop
# END video_group_xor
video_group_stored:
    nop
# BEGIN video_group_count
    b video_missing
    nop
# END video_group_count
video_group_counted:
    nop
# BEGIN video_group_advance
    b video_missing
    nop
# END video_group_advance
video_group_advanced:
    nop
    b video_vector
    nop
video_tail:
    beqz $a3, video_success
    nop
video_scalar:
# BEGIN video_scalar_word
    b video_missing
    nop
# END video_scalar_word
video_scalar_stored:
    nop
    bnez $a3, video_scalar
    nop
video_success:
    move $v0, $zero
video_return:
    jr $ra
    nop
video_missing:
    li $v0, 4
    move $v1, $zero
    jr $ra
    nop
