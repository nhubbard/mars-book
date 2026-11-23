# Complete worked copy for the native CBOR lesson.
# Complete bounded CBOR telemetry example. Modes 0..7 are documented in the caller.
.data
.align 3
source:
    .byte -72,4,3,-94,1,-126,66,-128,-1,33,0,-128,2,-104,10,0,24,23,24,24,32,55,56,24,24,-1,25,1,0,25,127,-1,57,127,-1,1,88,3,0,-1,65,24,0,27,1,2,3,4,5,6,7,8
    .byte -91:1228
source_end:
record:
    .byte -91:256
record_end:
destination:
    .byte -91:320
destination_end:
status: .word -1
mode: .word 5
.align 3
count: .dword -1
observed_major: .dword -1
observed_argument: .dword -1
byte_count: .dword 52
byte_capacity: .dword 36
inherited_depth: .dword 0
size_argument: .dword 24
write_major: .dword 0
write_value: .dword 24
.align 4
workspace: .byte -91:8192
.text
main:
    dla $sp, workspace
    daddiu $sp, $sp, 8192
    dla $t0, mode
    lw $t0, 0($t0)
    li $t1, 0
    beqc $t0, $t1, caller_head
    nop
    li $t1, 1
    beqc $t0, $t1, caller_skip
    nop
    li $t1, 2
    beqc $t0, $t1, caller_decode
    nop
    li $t1, 3
    beqc $t0, $t1, caller_bound
    nop
    li $t1, 4
    beqc $t0, $t1, caller_encode
    nop
    li $t1, 5
    beqc $t0, $t1, caller_head
    nop
    li $t1, 6
    beqc $t0, $t1, caller_size
    nop
    li $t1, 7
    beqc $t0, $t1, caller_write
    nop
    li $v0, 1
    move $v1, $zero
    bc results_ready
    nop
caller_head:
    dla $a0, source
    dla $t0, byte_count
    ld $a1, 0($t0)
head_call:
    balc cbor_head
    nop
head_return:
    nop
    dla $t0, observed_major
    sd $a2, 0($t0)
    dla $t0, observed_argument
    sd $a3, 0($t0)
    bnezc $v0, results_ready
    nop
    dla $t0, mode
    lw $t0, 0($t0)
    li $t1, 5
    beqc $t0, $t1, caller_skip
    nop
    bc results_ready
    nop
caller_skip:
    dla $a0, source
    dla $t0, byte_count
    ld $a1, 0($t0)
    dla $t0, inherited_depth
    ld $a2, 0($t0)
skip_call:
    balc cbor_skip
    nop
skip_return:
    nop
    bnezc $v0, results_ready
    nop
    dla $t0, mode
    lw $t0, 0($t0)
    li $t1, 5
    beqc $t0, $t1, caller_decode
    nop
    bc results_ready
    nop
caller_decode:
    dla $a0, source
    dla $t0, byte_count
    ld $a1, 0($t0)
    dla $a2, record
decode_call:
    balc telemetry_decode
    nop
decode_return:
    nop
    bnezc $v0, results_ready
    nop
    dla $t0, mode
    lw $t0, 0($t0)
    li $t1, 5
    beqc $t0, $t1, caller_bound
    nop
    bc results_ready
    nop
caller_bound:
    dla $a0, record
bound_call:
    balc telemetry_bound
    nop
bound_return:
    nop
    bnezc $v0, results_ready
    nop
    dla $t0, mode
    lw $t0, 0($t0)
    li $t1, 5
    beqc $t0, $t1, caller_encode
    nop
    bc results_ready
    nop
caller_encode:
    dla $a0, record
    dla $a1, destination
    dla $t0, byte_capacity
    ld $a2, 0($t0)
encode_call:
    balc telemetry_encode
    nop
encode_return:
    nop
    bnezc $v0, results_ready
    nop
    bc results_ready
    nop
caller_size:
    dla $t0, size_argument
    ld $a0, 0($t0)
size_call:
    balc head_size
    nop
size_return:
    nop
    bnezc $v0, results_ready
    nop
    bc results_ready
    nop
caller_write:
    dla $t0, write_major
    ld $a0, 0($t0)
    dla $t0, write_value
    ld $a1, 0($t0)
    dla $a2, destination
write_call:
    balc write_head
    nop
write_return:
    nop
    bnezc $v0, results_ready
    nop
    bc results_ready
    nop
results_ready:
    nop
    dla $t0, status
    sw $v0, 0($t0)
    dla $t0, count
    sd $v1, 0($t0)
    li $v0, 10
    syscall
function_return:
    nop
cbor_head:
    move $v1, $zero
    move $a2, $zero
    move $a3, $zero
    li $v0, 1
    beqzc $a1, head_exit
    nop
    lbu $t0, 0($a0)
after_h_prefix:
    nop
    srl $t1, $t0, 5
    li $t2, 3
    beqc $t1, $t2, head_exit
    nop
    sltiu $t2, $t1, 6
    beqzc $t2, head_exit
    nop
after_h_type:
    nop
    andi $t0, $t0, 31
    sltiu $t2, $t0, 28
    beqzc $t2, head_exit
    nop
    sltiu $t2, $t0, 24
    bnezc $t2, head_inline
    nop
after_h_form:
    nop
    addiu $t2, $t0, -24
    li $t3, 1
    sllv $t3, $t3, $t2
    daddiu $t4, $t3, 1
    sltu $t2, $a1, $t4
    bnezc $t2, head_exit
    nop
after_h_width:
    nop
    move $t5, $a0
    move $t6, $zero
head_bytes:
    daddiu $t5, $t5, 1
    lbu $t0, 0($t5)
    dsll $t6, $t6, 8
    or $t6, $t6, $t0
    addiu $t3, $t3, -1
    bnezc $t3, head_bytes
    nop
    move $a3, $t6
    move $v1, $t4
after_h_bytes:
    nop
    bc head_success
    nop
head_inline:
    move $a3, $t0
    li $v1, 1
after_h_inline:
    nop
head_success:
    move $a2, $t1
    move $v0, $zero
after_h_success:
    nop
head_exit:
    jrc $ra
    nop
head_missing:
    li $v0, 4
    move $v1, $zero
    move $a2, $zero
    move $a3, $zero
    jrc $ra
    nop
cbor_skip:
    li $v0, 2
    move $v1, $zero
    sltiu $t0, $a1, 1025
    beqzc $t0, skip_early
    nop
    li $v0, 1
    sltiu $t0, $a2, 9
    beqzc $t0, skip_early
    nop
    beqzc $a1, skip_early
    nop
after_k_count:
    nop
    daddiu $sp, $sp, -80
    sd $ra, 0($sp)
    sd $s0, 8($sp)
    sd $s1, 16($sp)
    sd $s2, 24($sp)
    sd $s3, 32($sp)
    sd $s4, 40($sp)
    sd $s5, 48($sp)
    sd $s6, 56($sp)
    move $s0, $a0
    move $s1, $a1
    move $s2, $a2
    move $s3, $a0
    balc cbor_head
    nop
    li $t9, 4
    beqc $v0, $t9, skip_missing
    nop
    bnezc $v0, skip_bad
    nop
    daddu $s0, $s0, $v1
    dsubu $s1, $s1, $v1
after_k_head:
    nop
    sltiu $t0, $a2, 2
    bnezc $t0, skip_done
    nop
    li $t0, 2
    beqc $a2, $t0, skip_string
    nop
after_k_kind:
    nop
    sltiu $t0, $s2, 8
    beqzc $t0, skip_bad
    nop
    daddiu $s2, $s2, 1
    move $s6, $a2
    move $s4, $a3
    move $s5, $zero
    li $t0, 5
    beqc $s6, $t0, skip_map
    nop
after_k_depth:
    nop
skip_array:
    sltiu $t0, $s4, 65
    beqzc $t0, skip_bad
    nop
after_k_array:
    nop
    bc skip_next
    nop
skip_map:
    sltiu $t0, $s4, 9
    beqzc $t0, skip_bad
    nop
after_k_map:
    nop
skip_next:
    beqzc $s4, skip_done
    nop
    li $t0, 5
    bnec $s6, $t0, skip_child
    nop
    move $a0, $s0
    move $a1, $s1
    balc cbor_head
    nop
    li $t9, 4
    beqc $v0, $t9, skip_missing
    nop
    bnezc $v0, skip_bad
    nop
    bnezc $a2, skip_bad
    nop
    sltiu $t0, $a3, 8
    beqzc $t0, skip_bad
    nop
    li $t0, 1
    sllv $t0, $t0, $a3
    and $t1, $s5, $t0
    bnezc $t1, skip_bad
    nop
    or $s5, $s5, $t0
    daddu $s0, $s0, $v1
    dsubu $s1, $s1, $v1
after_k_key:
    nop
skip_child:
    move $a0, $s0
    move $a1, $s1
    move $a2, $s2
    balc cbor_skip
    nop
    li $t9, 4
    beqc $v0, $t9, skip_missing
    nop
    bnezc $v0, skip_bad
    nop
    daddu $s0, $s0, $v1
    dsubu $s1, $s1, $v1
    daddiu $s4, $s4, -1
after_k_child:
    nop
    bc skip_next
    nop
skip_string:
    sltiu $t0, $a3, 33
    beqzc $t0, skip_bad
    nop
    sltu $t0, $s1, $a3
    bnezc $t0, skip_bad
    nop
    daddu $s0, $s0, $a3
    dsubu $s1, $s1, $a3
after_k_string:
    nop
skip_done:
    move $v0, $zero
    dsubu $v1, $s0, $s3
after_k_finish:
    nop
    bc skip_restore
    nop
skip_bad:
    li $v0, 1
    move $v1, $zero
    bc skip_restore
    nop
skip_missing:
    li $v0, 4
    move $v1, $zero
skip_restore:
    ld $ra, 0($sp)
    ld $s0, 8($sp)
    ld $s1, 16($sp)
    ld $s2, 24($sp)
    ld $s3, 32($sp)
    ld $s4, 40($sp)
    ld $s5, 48($sp)
    ld $s6, 56($sp)
    daddiu $sp, $sp, 80
    jrc $ra
    nop
skip_early_missing:
    li $v0, 4
    move $v1, $zero
skip_early:
    jrc $ra
    nop
telemetry_decode:
    li $v0, 2
    move $v1, $zero
    sltiu $t0, $a1, 1025
    beqzc $t0, decode_early
    nop
    li $v0, 1
    sltiu $t0, $a1, 7
    bnezc $t0, decode_early
    nop
after_d_count:
    nop
    daddiu $sp, $sp, -288
    sd $ra, 0($sp)
    sd $s0, 8($sp)
    sd $s1, 16($sp)
    sd $s2, 24($sp)
    sd $s3, 32($sp)
    sd $s4, 40($sp)
    sd $s5, 48($sp)
    sd $s6, 56($sp)
    sd $s7, 64($sp)
    sd $a1, 80($sp)
    move $s0, $a0
    move $s1, $a1
    move $s2, $a2
    move $s4, $zero
    balc decode_head
    nop
    li $t9, 4
    beqc $v0, $t9, decode_missing
    nop
    bnezc $v0, decode_bad
    nop
    li $t0, 5
    bnec $a2, $t0, decode_bad
    nop
    sltiu $t0, $a3, 9
    beqzc $t0, decode_bad
    nop
    sltiu $t0, $a3, 3
    bnezc $t0, decode_bad
    nop
    move $s3, $a3
after_d_root:
    nop
decode_pair:
    beqzc $s3, decode_finish
    nop
    balc decode_head
    nop
    li $t9, 4
    beqc $v0, $t9, decode_missing
    nop
    bnezc $v0, decode_bad
    nop
    bnezc $a2, decode_bad
    nop
    sltiu $t0, $a3, 8
    beqzc $t0, decode_bad
    nop
    move $s5, $a3
    li $t0, 1
    sllv $t0, $t0, $s5
    and $t1, $s4, $t0
    bnezc $t1, decode_bad
    nop
    or $s4, $s4, $t0
after_d_key:
    nop
    beqzc $s5, decode_counter
    nop
    li $t0, 1
    beqc $s5, $t0, decode_identifier
    nop
    li $t0, 2
    beqc $s5, $t0, decode_readings
    nop
after_d_dispatch:
    nop
    move $a0, $s0
    move $a1, $s1
    li $a2, 1
    balc cbor_skip
    nop
    li $t9, 4
    beqc $v0, $t9, decode_missing
    nop
    bnezc $v0, decode_bad
    nop
    daddu $s0, $s0, $v1
    dsubu $s1, $s1, $v1
after_d_optional:
    nop
    bc decode_next
    nop
decode_counter:
    balc decode_head
    nop
    li $t9, 4
    beqc $v0, $t9, decode_missing
    nop
    bnezc $v0, decode_bad
    nop
    bnezc $a2, decode_bad
    nop
    sd $a3, 96($sp)
after_d_counter:
    nop
    bc decode_next
    nop
decode_identifier:
    balc decode_head
    nop
    li $t9, 4
    beqc $v0, $t9, decode_missing
    nop
    bnezc $v0, decode_bad
    nop
    li $t0, 2
    bnec $a2, $t0, decode_bad
    nop
    sltiu $t0, $a3, 33
    beqzc $t0, decode_bad
    nop
    sltu $t0, $s1, $a3
    bnezc $t0, decode_bad
    nop
    sw $a3, 104($sp)
after_d_identifier:
    nop
decode_id_copy:
    move $t0, $zero
    daddiu $t1, $sp, 112
decode_id_loop:
    beqc $t0, $a3, decode_id_done
    nop
    lbu $t2, 0($s0)
    sb $t2, 0($t1)
    daddiu $s0, $s0, 1
    daddiu $t1, $t1, 1
    daddiu $t0, $t0, 1
    bc decode_id_loop
    nop
 decode_id_done:
    dsubu $s1, $s1, $a3
after_d_id_copy:
    nop
    bc decode_next
    nop
decode_readings:
    balc decode_head
    nop
    li $t9, 4
    beqc $v0, $t9, decode_missing
    nop
    bnezc $v0, decode_bad
    nop
    li $t0, 4
    bnec $a2, $t0, decode_bad
    nop
    sltiu $t0, $a3, 65
    beqzc $t0, decode_bad
    nop
    sw $a3, 108($sp)
    move $s7, $a3
    move $s6, $zero
after_d_array:
    nop
decode_sample:
    beqc $s6, $s7, decode_next
    nop
    balc decode_head
    nop
    li $t9, 4
    beqc $v0, $t9, decode_missing
    nop
    bnezc $v0, decode_bad
    nop
    sltiu $t0, $a2, 2
    beqzc $t0, decode_bad
    nop
    li $t0, 32768
    sltu $t0, $a3, $t0
    beqzc $t0, decode_bad
    nop
    beqzc $a2, decode_positive
    nop
    nor $a3, $a3, $zero
 decode_positive:
    dsll $t0, $s6, 1
    daddu $t0, $t0, $sp
    sh $a3, 144($t0)
    daddiu $s6, $s6, 1
after_d_sample:
    nop
    bc decode_sample
    nop
decode_next:
    daddiu $s3, $s3, -1
    bc decode_pair
    nop
decode_finish:
    andi $t0, $s4, 7
    li $t1, 7
    bnec $t0, $t1, decode_bad
    nop
    bnezc $s1, decode_bad
    nop
after_d_finish:
    nop
    ld $t0, 96($sp)
    sd $t0, 0($s2)
    ld $t0, 104($sp)
    sd $t0, 8($s2)
after_d_prefix:
    nop
    lwu $t0, 104($sp)
    daddiu $t1, $sp, 112
    daddiu $t2, $s2, 16
 decode_publish_id:
    beqzc $t0, decode_publish_samples
    nop
    lbu $t3, 0($t1)
    sb $t3, 0($t2)
    daddiu $t1, $t1, 1
    daddiu $t2, $t2, 1
    daddiu $t0, $t0, -1
    bc decode_publish_id
    nop
 decode_publish_samples:
    lwu $t0, 108($sp)
    daddiu $t1, $sp, 144
    daddiu $t2, $s2, 48
 decode_publish_sample_loop:
    beqzc $t0, decode_success
    nop
    lhu $t3, 0($t1)
    sh $t3, 0($t2)
    daddiu $t1, $t1, 2
    daddiu $t2, $t2, 2
    daddiu $t0, $t0, -1
    bc decode_publish_sample_loop
    nop
 decode_success:
    move $v0, $zero
    ld $v1, 80($sp)
after_d_payload:
    nop
    bc decode_restore
    nop
decode_bad:
    li $v0, 1
    move $v1, $zero
    bc decode_restore
    nop
decode_missing:
    li $v0, 4
    move $v1, $zero
decode_restore:
    ld $ra, 0($sp)
    ld $s0, 8($sp)
    ld $s1, 16($sp)
    ld $s2, 24($sp)
    ld $s3, 32($sp)
    ld $s4, 40($sp)
    ld $s5, 48($sp)
    ld $s6, 56($sp)
    ld $s7, 64($sp)
    daddiu $sp, $sp, 288
    jrc $ra
    nop
decode_early_missing:
    li $v0, 4
    move $v1, $zero
decode_early:
    jrc $ra
    nop
decode_head:
    sd $ra, 88($sp)
    move $a0, $s0
    move $a1, $s1
    balc cbor_head
    nop
    bnezc $v0, decode_head_return
    nop
    daddu $s0, $s0, $v1
    dsubu $s1, $s1, $v1
after_d_head:
    nop
decode_head_return:
    ld $ra, 88($sp)
    jrc $ra
    nop
decode_head_missing:
    li $v0, 4
    move $v1, $zero
    bc decode_head_return
    nop
head_size:
    li $v1, 1
    sltiu $t0, $a0, 24
    beqzc $t0, size_extended
    nop
    move $v0, $zero
    bc size_exit
    nop
size_extended:
after_z_inline:
    nop
    li $v1, 2
    sltiu $t0, $a0, 256
    bnezc $t0, size_width_done
    nop
    li $v1, 3
    dsrl $t0, $a0, 16
    beqzc $t0, size_width_done
    nop
    li $v1, 5
    dsrl32 $t0, $a0, 0
    beqzc $t0, size_width_done
    nop
    li $v1, 9
size_width_done:
after_z_width:
    nop
    move $v0, $zero
after_z_success:
    nop
    move $v0, $zero
size_exit:
    jrc $ra
    nop
size_missing:
    li $v0, 4
    move $v1, $zero
    jrc $ra
    nop
telemetry_bound:
    daddiu $sp, $sp, -64
    sd $ra, 0($sp)
    sd $s0, 8($sp)
    sd $s1, 16($sp)
    sd $s2, 24($sp)
    sd $s3, 32($sp)
    move $s0, $a0
    lwu $s1, 8($s0)
    lwu $s2, 12($s0)
after_b_fields:
    nop
    sltiu $t0, $s1, 33
    beqzc $t0, bound_bad
    nop
    sltiu $t0, $s2, 65
    beqzc $t0, bound_bad
    nop
after_b_lengths:
    nop
    daddiu $s3, $s1, 4
    ld $a0, 0($s0)
    balc head_size
    nop
    bnezc $v0, bound_missing
    nop
    daddu $s3, $s3, $v1
    move $a0, $s1
    balc head_size
    nop
    bnezc $v0, bound_missing
    nop
    daddu $s3, $s3, $v1
    move $a0, $s2
    balc head_size
    nop
    bnezc $v0, bound_missing
    nop
    daddu $s3, $s3, $v1
    daddiu $s0, $s0, 48
after_b_heads:
    nop
bound_sample:
bound_item:
    beqzc $s2, bound_items_done
    nop
    lh $a0, 0($s0)
    bgezc $a0, bound_positive
    nop
    nor $a0, $a0, $zero
 bound_positive:
    balc head_size
    nop
    bnezc $v0, bound_missing
    nop
    daddu $s3, $s3, $v1
    daddiu $s0, $s0, 2
    daddiu $s2, $s2, -1
    bc bound_item
    nop
bound_items_done:
after_b_samples:
    nop
bound_done:
    move $v0, $zero
    move $v1, $s3
after_b_finish:
    nop
    bc bound_restore
    nop
bound_bad:
    li $v0, 1
    move $v1, $zero
    bc bound_restore
    nop
bound_missing:
    li $v0, 4
    move $v1, $zero
bound_restore:
    ld $ra, 0($sp)
    ld $s0, 8($sp)
    ld $s1, 16($sp)
    ld $s2, 24($sp)
    ld $s3, 32($sp)
    daddiu $sp, $sp, 64
    jrc $ra
    nop
write_head:
    daddiu $sp, $sp, -32
    sd $ra, 0($sp)
    sd $a0, 8($sp)
    move $a0, $a1
    balc head_size
    nop
    bnezc $v0, write_missing
    nop
after_w_fields:
    nop
    ld $t1, 8($sp)
    dsll $t1, $t1, 5
    li $t2, 1
    beqc $v1, $t2, write_inline
    nop
    daddiu $t3, $v1, -1
    li $t2, 24
    li $t4, 1
 write_info:
    beqc $t4, $t3, write_initial
    nop
    sll $t4, $t4, 1
    addiu $t2, $t2, 1
    bc write_info
    nop
 write_initial:
    or $t1, $t1, $t2
    sb $t1, 0($a2)
    daddiu $t4, $a2, 1
    daddiu $t5, $t3, -1
    dsll $t5, $t5, 3
after_w_info:
    nop
write_argument:
    dsrlv $t6, $a1, $t5
    sb $t6, 0($t4)
    daddiu $t4, $t4, 1
    daddiu $t5, $t5, -8
    daddiu $t3, $t3, -1
    bnezc $t3, write_argument
    nop
after_w_argument:
    nop
    bc write_done
    nop
write_inline:
    or $t1, $t1, $a1
    sb $t1, 0($a2)
after_w_inline:
    nop
write_done:
    move $v0, $zero
after_w_finish:
    nop
    bc write_restore
    nop
write_missing:
    li $v0, 4
    move $v1, $zero
write_restore:
    ld $ra, 0($sp)
    daddiu $sp, $sp, 32
    jrc $ra
    nop
telemetry_encode:
    daddiu $sp, $sp, -80
    sd $ra, 0($sp)
    sd $s0, 8($sp)
    sd $s1, 16($sp)
    sd $s2, 24($sp)
    sd $s3, 32($sp)
    sd $s4, 40($sp)
    sd $s5, 48($sp)
    move $s0, $a0
    move $s1, $a1
    move $s2, $a2
    balc telemetry_bound
    nop
    bnezc $v0, encode_restore
    nop
after_e_bound:
    nop
    sltu $t0, $s2, $v1
    beqzc $t0, encode_capacity_ok
    nop
    li $v0, 2
    move $v1, $zero
    bc encode_restore
    nop
encode_capacity_ok:
    move $s3, $v1
    move $s4, $s1
after_e_capacity:
    nop
    li $t0, 163
    sb $t0, 0($s1)
    sb $zero, 1($s1)
    daddiu $s1, $s1, 2
    move $a0, $zero
    ld $a1, 0($s0)
    move $a2, $s1
    balc write_head
    nop
    bnezc $v0, encode_restore
    nop
    daddu $s1, $s1, $v1
after_e_counter:
    nop
    li $t0, 1
    sb $t0, 0($s1)
    daddiu $s1, $s1, 1
    li $a0, 2
    lwu $a1, 8($s0)
    move $a2, $s1
    balc write_head
    nop
    bnezc $v0, encode_restore
    nop
    daddu $s1, $s1, $v1
    lwu $t0, 8($s0)
    daddiu $t1, $s0, 16
after_e_identifier_head:
    nop
encode_identifier:
encode_id_loop:
    beqzc $t0, encode_id_done
    nop
    lbu $t2, 0($t1)
    sb $t2, 0($s1)
    daddiu $s1, $s1, 1
    daddiu $t1, $t1, 1
    daddiu $t0, $t0, -1
    bc encode_id_loop
    nop
encode_id_done:
after_e_identifier_copy:
    nop
encode_readings:
    li $t0, 2
    sb $t0, 0($s1)
    daddiu $s1, $s1, 1
    li $a0, 4
    lwu $a1, 12($s0)
    move $a2, $s1
    balc write_head
    nop
    bnezc $v0, encode_restore
    nop
    daddu $s1, $s1, $v1
    lwu $s2, 12($s0)
    daddiu $s5, $s0, 48
after_e_array:
    nop
encode_sample:
encode_item:
    beqzc $s2, encode_items_done
    nop
    lh $a1, 0($s5)
    move $a0, $zero
    bgezc $a1, encode_positive
    nop
    li $a0, 1
    nor $a1, $a1, $zero
 encode_positive:
    move $a2, $s1
    balc write_head
    nop
    bnezc $v0, encode_restore
    nop
    daddu $s1, $s1, $v1
    daddiu $s5, $s5, 2
    daddiu $s2, $s2, -1
    bc encode_item
    nop
encode_items_done:
after_e_samples:
    nop
encode_done:
    move $v0, $zero
    dsubu $v1, $s1, $s4
after_e_finish:
    nop
    bc encode_restore
    nop
encode_missing:
    li $v0, 4
    move $v1, $zero
encode_restore:
    ld $ra, 0($sp)
    ld $s0, 8($sp)
    ld $s1, 16($sp)
    ld $s2, 24($sp)
    ld $s3, 32($sp)
    ld $s4, 40($sp)
    ld $s5, 48($sp)
    daddiu $sp, $sp, 80
    jrc $ra
    nop
