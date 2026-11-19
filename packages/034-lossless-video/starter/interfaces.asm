.data
video_interface_message: .asciiz "Video interfaces are still under construction.\n"
.text
.globl main
main:
    dla $a0, video_interface_message
    li $v0, 4
    syscall
    li $v0, 10
    syscall
video_xor_words:
    b video_interface_unfinished
    nop
video_xor_words_msa:
    b video_interface_unfinished
    nop
video_apply_record:
    b video_interface_unfinished
    nop
video_index:
    b video_interface_unfinished
    nop
video_load:
    b video_interface_unfinished
    nop
video_seek:
    b video_interface_unfinished
    nop
video_encode_full:
    b video_interface_unfinished
    nop
video_encode_record:
    b video_interface_unfinished
    nop
video_encode_file:
    b video_interface_unfinished
    nop
video_store:
    b video_interface_unfinished
    nop
video_now:
    b video_interface_unfinished
    nop
video_play:
    b video_interface_unfinished
    nop
video_interface_unfinished:
    li $v0, 4
    move $v1, $zero
    jr $ra
    nop
