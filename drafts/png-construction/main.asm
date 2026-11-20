# Open with MARS Run or Debug after creating image.png beside this project.
# mars:tool bitmap width=1024 height=1024 unitWidth=2 unitHeight=2 base=heap
.eqv FRAME_STRIDE 512
.eqv FRAME_BYTES 1048576
.eqv FILE_BYTES 1048576
.eqv COMPRESSED_BYTES 1048576
.eqv ROW_BYTES 786944
.eqv TOTAL_BYTES 3932672
.data
image_path: .asciiz "image.png"
    .space 118
image_path_end:
vector_mode: .word 1
.align 3
png_context: .byte -91:80
framebuffer_address: .dword 0
source_address: .dword 0
compressed_address: .dword 0
row_address: .dword 0
loaded_count: .dword 0
file_capacity: .dword 1048576
last_status: .word -1
message_done: .asciiz "PNG displayed.\n"
message_read: .asciiz "Cannot read a nonempty PNG within 1 MiB.\n"
message_png: .asciiz "Invalid or unsupported PNG.\n"
message_mode: .asciiz "Select scalar mode 0 or MSA mode 1.\n"
message_development: .asciiz "Program is still under construction.\n"
.align 3
workspace: .space 16384
.text
.globl main
main:
    dla $t0, vector_mode
    lwu $s6, 0($t0)
    sltiu $t1, $s6, 2
    beqz $t1, caller_mode_error
    nop
    li $a0, TOTAL_BYTES
    li $v0, 9
    syscall
    move $s0, $v0
    dli $t0, FRAME_BYTES
    daddu $s1, $s0, $t0
    dli $t0, FILE_BYTES
    daddu $s2, $s1, $t0
    dli $t0, COMPRESSED_BYTES
    daddu $s3, $s2, $t0
    dla $t0, framebuffer_address
    sd $s0, 0($t0)
    sd $s1, 8($t0)
    sd $s2, 16($t0)
    sd $s3, 24($t0)
    dla $t0, png_context
    li $t1, FRAME_STRIDE
    sd $t1, 0($t0)
    dli $t1, FRAME_BYTES
    sd $t1, 8($t0)
    sd $s2, 16($t0)
    dli $t1, COMPRESSED_BYTES
    sd $t1, 24($t0)
    sd $s3, 32($t0)
    dli $t1, ROW_BYTES
    sd $t1, 40($t0)
    dla $a0, image_path
    move $a1, $s1
    dla $t0, file_capacity
    ld $a2, 0($t0)
loader_call:
    jal png_load
    nop
loader_return:
    dla $t0, last_status
    sw $v0, 0($t0)
    li $t1, 4
    beq $v0, $t1, caller_development
    nop
    bnez $v0, caller_read_error
    nop
    dla $t0, loaded_count
    sd $v1, 0($t0)
    move $a0, $s1
    move $a1, $v1
    move $a2, $s0
    dla $a3, png_context
decode_call:
    beqz $s6, caller_scalar
    nop
    jal png_decode_msa
    nop
    b decode_return
    nop
caller_scalar:
    jal png_decode
    nop
decode_return:
    dla $t0, last_status
    sw $v0, 0($t0)
    li $t1, 4
    beq $v0, $t1, caller_development
    nop
    bnez $v0, caller_png_error
    nop
    dla $a0, message_done
    b caller_success
    nop
caller_development:
    dla $a0, message_development
caller_success:
    li $v0, 4
    syscall
    move $a0, $zero
    li $v0, 17
    syscall
caller_read_error:
    dla $a0, message_read
    b caller_failure
    nop
caller_png_error:
    dla $a0, message_png
    b caller_failure
    nop
caller_mode_error:
    dla $a0, message_mode
caller_failure:
    li $v0, 4
    syscall
    li $a0, 1
    li $v0, 17
    syscall
function_return:
    nop
# Student entries begin. Status 4 means the selected entry is unfinished.
# Region markers are stable editing boundaries, not required final solution structure.
png_load:
# BEGIN load_capacity
    li $v0, 4
    move $v1, $zero
    jr $ra
    nop
# END load_capacity
load_capacity_ready:
    nop
    # Supplied full-width call frame; no file is owned yet.
    daddiu $sp, $sp, -80
    sd $ra, 0($sp)
    sd $s0, 8($sp)
    sd $s1, 16($sp)
    sd $s2, 24($sp)
    sd $s3, 32($sp)
    sd $s4, 40($sp)
# BEGIN load_state
    b load_missing
    nop
# END load_state
load_state_ready:
    nop
# BEGIN load_open
    b load_missing
    nop
# END load_open
load_open_ready:
    nop
load_read:
# BEGIN load_request
    b load_missing_owned
    nop
# END load_request
load_request_ready:
    nop
# BEGIN load_result
    b load_missing_owned
    nop
# END load_result
load_progress_ready:
    nop
    b load_read
    nop
load_probe:
# BEGIN load_probe
    b load_missing_owned
    nop
# END load_probe
load_probe_ready:
    nop
load_eof:
# BEGIN load_eof
    b load_missing_owned
    nop
# END load_eof
load_eof_ready:
    nop
load_close:
# BEGIN load_close
    b load_missing_owned
    nop
# END load_close
load_close_ready:
    nop
# BEGIN load_publish
    b load_missing
    nop
# END load_publish
load_publish_ready:
    nop
    b load_restore
    nop
load_missing_owned:
    # Later unfinished regions release the descriptor acquired by load_open.
    move $a0, $s3
    li $v0, 16
    syscall
load_missing:
    li $v0, 4
    move $v1, $zero
    b load_restore
    nop
load_open_failed:
    li $v0, 1
    move $v1, $zero
load_restore:
    ld $ra, 0($sp)
    ld $s0, 8($sp)
    ld $s1, 16($sp)
    ld $s2, 24($sp)
    ld $s3, 32($sp)
    ld $s4, 40($sp)
    daddiu $sp, $sp, 80
load_early:
    jr $ra
    nop
png_decode:
    li $v0, 4
    move $v1, $zero
    jr $ra
    nop
png_decode_msa:
    li $v0, 4
    move $v1, $zero
    jr $ra
    nop
