.data
.align 6
data: .byte 97,98,99,-128,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,24
      .byte -91:4096
data_end: .word 0x13579bdf
.align 6
state: .word 0x6a6b6c6d:24
state_end:
schedule: .word 0x6a6b6c6d:80
schedule_end:
context: .word 0x6a09e667, 0xbb67ae85, 0x3c6ef372, 0xa54ff53a, 0x510e527f, 0x9b05688c, 0x1f83d9ab, 0x5be0cd19
    .dword 0
    .word 0,0
    .byte -91:784
context_end:
digest: .byte -91:96
digest_end:
path: .asciiz "fixture.bin"
      .space 116
path_end:
scratch: .byte -91:320
scratch_end:
length: .word 3
mode: .word 2
buffer_capacity: .word 7
status: .word -1
.align 3
sha_iv: .word 0x6a09e667, 0xbb67ae85, 0x3c6ef372, 0xa54ff53a, 0x510e527f, 0x9b05688c, 0x1f83d9ab, 0x5be0cd19
sha_k: .word 0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5, 0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5, 0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3, 0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174, 0xe49b69c1, 0xefbe4786, 0x0fc19dc6, 0x240ca1cc, 0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da, 0x983e5152, 0xa831c66d, 0xb00327c8, 0xbf597fc7, 0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967, 0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13, 0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85, 0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3, 0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070, 0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5, 0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3, 0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208, 0x90befffa, 0xa4506ceb, 0xbef9a3f7, 0xc67178f2
.text
.globl main
main:
    li $sp, 0x7fff0000
    dla $t0, mode
    lw $t0, 0($t0)
    beq $t0, $zero, main_memory
    nop
    li $t1, 1
    beq $t0, $t1, main_file
    nop
    b main_exercise
    nop
main_memory:
    dla $a0, context
    jal sha256_init
    nop
    dla $a0, context
    dla $a1, data
    dla $t0, length
    lwu $a2, 0($t0)
memory_call:
    jal sha256_update
    nop
    dla $a0, context
    dla $a1, digest
    jal sha256_final
    nop
    b main_done
    nop
main_file:
    dla $a0, path
    dla $a1, scratch
    dla $t0, buffer_capacity
    lwu $a2, 0($t0)
    dla $a3, digest
file_call:
    jal sha256_file
    nop
    b main_done
    nop
main_exercise:
    li $t1, 2
    beq $t0, $t1, main_schedule
    nop
    li $t1, 3
    beq $t0, $t1, main_compress
    nop
    li $t1, 4
    beq $t0, $t1, main_init
    nop
    li $t1, 5
    beq $t0, $t1, main_update
    nop
    dla $a0, context
    dla $a1, digest
    dla $t9, sha256_final
    b kernel_call
    nop
main_schedule:
    dla $a0, data
    dla $a1, schedule
    dla $t9, sha256_schedule
    b kernel_call
    nop
main_compress:
    dla $a0, state
    dla $a1, data
    dla $a2, schedule
    dla $t9, sha256_compress
    b kernel_call
    nop
main_init:
    dla $a0, context
    dla $t9, sha256_init
    b kernel_call
    nop
main_update:
    dla $a0, context
    dla $a1, data
    dla $t0, length
    lwu $a2, 0($t0)
    dla $t9, sha256_update
kernel_call:
    jalr $t9
    nop
    b main_done
    nop
main_done:
    dla $t0, status
    sw $v0, 0($t0)
results_ready:
    li $v0, 10
    syscall
function_return:
    b results_ready
    nop


sha256_schedule:
sha_sch_setup:
# BEGIN sch_setup
    b schedule_development
    nop
# END sch_setup
schedule_decode:
sha_sch_high:
# BEGIN sch_high
    b schedule_development
    nop
# END sch_high
sha_sch_low:
# BEGIN sch_low
    b schedule_development
    nop
# END sch_low
sha_sch_walk:
# BEGIN sch_walk
    b schedule_development
    nop
# END sch_walk
schedule_expand:
sha_sch_sigma0:
# BEGIN sch_sigma0
    b schedule_development
    nop
# END sch_sigma0
sha_sch_sigma1:
# BEGIN sch_sigma1
    b schedule_development
    nop
# END sch_sigma1
sha_sch_sum:
# BEGIN sch_sum
    b schedule_development
    nop
# END sch_sum
schedule_done:
    move $v0, $zero
    jr $ra
    nop
schedule_development:
    li $v0, 3
    jr $ra
    nop


sha256_compress:
    daddiu $sp, $sp, -96
    sd $s0, 0($sp)
    sd $s1, 8($sp)
    sd $s2, 16($sp)
    sd $s3, 24($sp)
    sd $s4, 32($sp)
    sd $s5, 40($sp)
    sd $s6, 48($sp)
    sd $s7, 56($sp)
    sd $ra, 64($sp)
    sd $a0, 72($sp)
    sd $a2, 80($sp)
sha_comp_prepare:
# BEGIN comp_prepare
    b compress_development
    nop
# END comp_prepare
sha_comp_load:
# BEGIN comp_load
    b compress_development
    nop
# END comp_load
compress_round:
sha_comp_sigma1:
# BEGIN comp_sigma1
    b compress_development
    nop
# END comp_sigma1
sha_comp_t1:
# BEGIN comp_t1
    b compress_development
    nop
# END comp_t1
sha_comp_sigma0:
# BEGIN comp_sigma0
    b compress_development
    nop
# END comp_sigma0
sha_comp_t2:
# BEGIN comp_t2
    b compress_development
    nop
# END comp_t2
sha_comp_rotate:
# BEGIN comp_rotate
    b compress_development
    nop
# END comp_rotate
compress_round_done:
sha_comp_walk:
# BEGIN comp_walk
    b compress_development
    nop
# END comp_walk
sha_comp_feed:
# BEGIN comp_feed
    b compress_development
    nop
# END comp_feed
compress_done:
    move $v0, $zero
    b compress_restore
    nop
compress_development:
    li $v0, 3
compress_restore:
    ld $s0, 0($sp)
    ld $s1, 8($sp)
    ld $s2, 16($sp)
    ld $s3, 24($sp)
    ld $s4, 32($sp)
    ld $s5, 40($sp)
    ld $s6, 48($sp)
    ld $s7, 56($sp)
    ld $ra, 64($sp)
    daddiu $sp, $sp, 96
    jr $ra
    nop


sha256_init:
sha_init_copy:
# BEGIN init_copy
    b init_development
    nop
# END init_copy
sha_init_counts:
# BEGIN init_counts
    b init_development
    nop
# END init_counts
init_done:
    move $v0, $zero
    jr $ra
    nop
init_development:
    li $v0, 3
    jr $ra
    nop


sha256_update:
    daddiu $sp, $sp, -48
    sd $s0, 0($sp)
    sd $s1, 8($sp)
    sd $s2, 16($sp)
    sd $s3, 24($sp)
    sd $ra, 32($sp)
    move $s0, $a0
    move $s1, $a1
    move $s2, $a2
    lwu $s3, 40($s0)
sha_upd_count:
# BEGIN upd_count
    b update_development
    nop
# END upd_count
update_byte:
sha_upd_copy:
# BEGIN upd_copy
    b update_development
    nop
# END upd_copy
sha_upd_walk:
# BEGIN upd_walk
    b update_development
    nop
# END upd_walk
sha_upd_compress:
# BEGIN upd_compress
    b update_development
    nop
# END upd_compress
sha_upd_resume:
# BEGIN upd_resume
    b update_development
    nop
# END upd_resume
update_done:
update_success:
    move $v0, $zero
    b update_restore
    nop
update_development:
    li $v0, 3
update_restore:
    sw $s3, 40($s0)
    ld $s0, 0($sp)
    ld $s1, 8($sp)
    ld $s2, 16($sp)
    ld $s3, 24($sp)
    ld $ra, 32($sp)
    daddiu $sp, $sp, 48
    jr $ra
    nop


sha256_final:
    daddiu $sp, $sp, -48
    sd $s0, 0($sp)
    sd $s1, 8($sp)
    sd $s2, 16($sp)
    sd $s3, 24($sp)
    sd $ra, 32($sp)
    move $s0, $a0
    move $s1, $a1
    lwu $s2, 40($s0)
sha_fin_marker:
# BEGIN fin_marker
    b final_development
    nop
# END fin_marker
sha_fin_decide:
# BEGIN fin_decide
    b final_development
    nop
# END fin_decide
final_zero_first:
sha_fin_zero_first:
# BEGIN fin_zero_first
    b final_development
    nop
# END fin_zero_first
final_first_compress:
sha_fin_first:
# BEGIN fin_first
    b final_development
    nop
# END fin_first
final_zero_to_length:
sha_fin_zero_last:
# BEGIN fin_zero_last
    b final_development
    nop
# END fin_zero_last
final_write_length:
sha_fin_length:
# BEGIN fin_length
    b final_development
    nop
# END fin_length
sha_fin_compress:
# BEGIN fin_compress
    b final_development
    nop
# END fin_compress
sha_fin_output:
# BEGIN fin_output
    b final_development
    nop
# END fin_output
final_done:
    move $v0, $zero
    b final_restore
    nop
final_development:
    li $v0, 3
final_restore:
    ld $s0, 0($sp)
    ld $s1, 8($sp)
    ld $s2, 16($sp)
    ld $s3, 24($sp)
    ld $ra, 32($sp)
    daddiu $sp, $sp, 48
    jr $ra
    nop


sha256_file:
    daddiu $sp, $sp, -432
    sd $s0, 368($sp)
    sd $s1, 376($sp)
    sd $s2, 384($sp)
    sd $s3, 392($sp)
    sd $s4, 400($sp)
    sd $s5, 408($sp)
    sd $ra, 416($sp)
    move $s0, $sp
    move $s1, $a1
    move $s2, $a2
    move $s3, $a3
    li $s4, -1
    li $s5, 3
sha_file_open:
# BEGIN file_open
    b file_development
    nop
# END file_open
sha_file_init:
# BEGIN file_init
    b file_development
    nop
# END file_init
file_read:
sha_file_read:
# BEGIN file_read
    b file_development
    nop
# END file_read
sha_file_result:
# BEGIN file_result
    b file_development
    nop
# END file_result
sha_file_update:
# BEGIN file_update
    b file_development
    nop
# END file_update
sha_file_repeat:
# BEGIN file_repeat
    b file_development
    nop
# END file_repeat
file_finish:
sha_file_final:
# BEGIN file_final
    b file_development
    nop
# END file_final
file_read_error:
    li $s5, 2
file_close:
    move $a0, $s4
    li $v0, 16
    syscall
    b file_return
    nop
file_open_error:
    li $s5, 1
file_return:
    move $v0, $s5
    ld $s0, 368($sp)
    ld $s1, 376($sp)
    ld $s2, 384($sp)
    ld $s3, 392($sp)
    ld $s4, 400($sp)
    ld $s5, 408($sp)
    ld $ra, 416($sp)
    daddiu $sp, $sp, 432
    jr $ra
    nop

file_development:
    bltz $s4, file_return
    nop
    b file_close
    nop

