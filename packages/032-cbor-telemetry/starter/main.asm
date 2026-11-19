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
mode: .word 0
.align 3
count: .dword -1
observed_major: .dword -1
observed_argument: .dword -1
byte_count: .dword 52
byte_capacity: .dword 37
inherited_depth: .dword 0
size_argument: .dword 24
write_major: .dword 0
write_value: .dword 24
workspace: .byte -91:8192
.text
main:
    dla $sp, workspace
    daddiu $sp, $sp, 8192
    dla $t0, mode
    lw $t0, 0($t0)
    li $t1, 0
    beq $t0, $t1, caller_head
    nop
    li $t1, 1
    beq $t0, $t1, caller_skip
    nop
    li $t1, 2
    beq $t0, $t1, caller_decode
    nop
    li $t1, 3
    beq $t0, $t1, caller_bound
    nop
    li $t1, 4
    beq $t0, $t1, caller_encode
    nop
    li $t1, 5
    beq $t0, $t1, caller_head
    nop
    li $t1, 6
    beq $t0, $t1, caller_size
    nop
    li $t1, 7
    beq $t0, $t1, caller_write
    nop
    li $v0, 1
    move $v1, $zero
    b results_ready
    nop
caller_head:
    dla $a0, source
    dla $t0, byte_count
    ld $a1, 0($t0)
head_call:
    jal cbor_head
    nop
head_return:
    nop
    dla $t0, observed_major
    sd $a2, 0($t0)
    dla $t0, observed_argument
    sd $a3, 0($t0)
    bnez $v0, results_ready
    nop
    dla $t0, mode
    lw $t0, 0($t0)
    li $t1, 5
    beq $t0, $t1, caller_skip
    nop
    b results_ready
    nop
caller_skip:
    dla $a0, source
    dla $t0, byte_count
    ld $a1, 0($t0)
    dla $t0, inherited_depth
    ld $a2, 0($t0)
skip_call:
    jal cbor_skip
    nop
skip_return:
    nop
    bnez $v0, results_ready
    nop
    dla $t0, mode
    lw $t0, 0($t0)
    li $t1, 5
    beq $t0, $t1, caller_decode
    nop
    b results_ready
    nop
caller_decode:
    dla $a0, source
    dla $t0, byte_count
    ld $a1, 0($t0)
    dla $a2, record
decode_call:
    jal telemetry_decode
    nop
decode_return:
    nop
    bnez $v0, results_ready
    nop
    dla $t0, mode
    lw $t0, 0($t0)
    li $t1, 5
    beq $t0, $t1, caller_bound
    nop
    b results_ready
    nop
caller_bound:
    dla $a0, record
bound_call:
    jal telemetry_bound
    nop
bound_return:
    nop
    bnez $v0, results_ready
    nop
    dla $t0, mode
    lw $t0, 0($t0)
    li $t1, 5
    beq $t0, $t1, caller_encode
    nop
    b results_ready
    nop
caller_encode:
    dla $a0, record
    dla $a1, destination
    dla $t0, byte_capacity
    ld $a2, 0($t0)
encode_call:
    jal telemetry_encode
    nop
encode_return:
    nop
    bnez $v0, results_ready
    nop
    b results_ready
    nop
caller_size:
    dla $t0, size_argument
    ld $a0, 0($t0)
size_call:
    jal head_size
    nop
size_return:
    nop
    bnez $v0, results_ready
    nop
    b results_ready
    nop
caller_write:
    dla $t0, write_major
    ld $a0, 0($t0)
    dla $t0, write_value
    ld $a1, 0($t0)
    dla $a2, destination
write_call:
    jal write_head
    nop
write_return:
    nop
    bnez $v0, results_ready
    nop
    b results_ready
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
# BEGIN h_prefix
    b head_missing
    nop
# END h_prefix
after_h_prefix:
    nop
# BEGIN h_type
    b head_missing
    nop
# END h_type
after_h_type:
    nop
# BEGIN h_form
    b head_missing
    nop
# END h_form
after_h_form:
    nop
# BEGIN h_width
    b head_missing
    nop
# END h_width
after_h_width:
    nop
# BEGIN h_bytes
    b head_missing
    nop
# END h_bytes
after_h_bytes:
    nop
    b head_success
    nop
head_inline:
# BEGIN h_inline
    b head_missing
    nop
# END h_inline
after_h_inline:
    nop
head_success:
# BEGIN h_success
    b head_missing
    nop
# END h_success
after_h_success:
    nop
head_exit:
    jr $ra
    nop
head_missing:
    li $v0, 4
    move $v1, $zero
    move $a2, $zero
    move $a3, $zero
    jr $ra
    nop
cbor_skip:
# BEGIN k_count
    b skip_early_missing
    nop
# END k_count
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
# BEGIN k_head
    b skip_missing
    nop
# END k_head
after_k_head:
    nop
# BEGIN k_kind
    b skip_missing
    nop
# END k_kind
after_k_kind:
    nop
# BEGIN k_depth
    b skip_missing
    nop
# END k_depth
after_k_depth:
    nop
skip_array:
# BEGIN k_array
    b skip_missing
    nop
# END k_array
after_k_array:
    nop
    b skip_next
    nop
skip_map:
# BEGIN k_map
    b skip_missing
    nop
# END k_map
after_k_map:
    nop
skip_next:
# BEGIN k_key
    b skip_missing
    nop
# END k_key
after_k_key:
    nop
skip_child:
# BEGIN k_child
    b skip_missing
    nop
# END k_child
after_k_child:
    nop
    b skip_next
    nop
skip_string:
# BEGIN k_string
    b skip_missing
    nop
# END k_string
after_k_string:
    nop
skip_done:
# BEGIN k_finish
    b skip_missing
    nop
# END k_finish
after_k_finish:
    nop
    b skip_restore
    nop
skip_bad:
    li $v0, 1
    move $v1, $zero
    b skip_restore
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
    jr $ra
    nop
skip_early_missing:
    li $v0, 4
    move $v1, $zero
skip_early:
    jr $ra
    nop
telemetry_decode:
# BEGIN d_count
    b decode_early_missing
    nop
# END d_count
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
# BEGIN d_root
    b decode_missing
    nop
# END d_root
after_d_root:
    nop
decode_pair:
# BEGIN d_key
    b decode_missing
    nop
# END d_key
after_d_key:
    nop
# BEGIN d_dispatch
    b decode_missing
    nop
# END d_dispatch
after_d_dispatch:
    nop
# BEGIN d_optional
    b decode_missing
    nop
# END d_optional
after_d_optional:
    nop
    b decode_next
    nop
decode_counter:
# BEGIN d_counter
    b decode_missing
    nop
# END d_counter
after_d_counter:
    nop
    b decode_next
    nop
decode_identifier:
# BEGIN d_identifier
    b decode_missing
    nop
# END d_identifier
after_d_identifier:
    nop
decode_id_copy:
# BEGIN d_id_copy
    b decode_missing
    nop
# END d_id_copy
after_d_id_copy:
    nop
    b decode_next
    nop
decode_readings:
# BEGIN d_array
    b decode_missing
    nop
# END d_array
after_d_array:
    nop
decode_sample:
# BEGIN d_sample
    b decode_missing
    nop
# END d_sample
after_d_sample:
    nop
    b decode_sample
    nop
decode_next:
    daddiu $s3, $s3, -1
    b decode_pair
    nop
decode_finish:
# BEGIN d_finish
    b decode_missing
    nop
# END d_finish
after_d_finish:
    nop
# BEGIN d_prefix
    b decode_missing
    nop
# END d_prefix
after_d_prefix:
    nop
# BEGIN d_payload
    b decode_missing
    nop
# END d_payload
after_d_payload:
    nop
    b decode_restore
    nop
decode_bad:
    li $v0, 1
    move $v1, $zero
    b decode_restore
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
    jr $ra
    nop
decode_early_missing:
    li $v0, 4
    move $v1, $zero
decode_early:
    jr $ra
    nop
decode_head:
    sd $ra, 88($sp)
# BEGIN d_head
    b decode_head_missing
    nop
# END d_head
after_d_head:
    nop
decode_head_return:
    ld $ra, 88($sp)
    jr $ra
    nop
decode_head_missing:
    li $v0, 4
    move $v1, $zero
    b decode_head_return
    nop
head_size:
# BEGIN z_inline
    b size_missing
    nop
# END z_inline
after_z_inline:
    nop
# BEGIN z_width
    b size_missing
    nop
# END z_width
after_z_width:
    nop
# BEGIN z_success
    b size_missing
    nop
# END z_success
after_z_success:
    nop
    move $v0, $zero
size_exit:
    jr $ra
    nop
size_missing:
    li $v0, 4
    move $v1, $zero
    jr $ra
    nop
telemetry_bound:
    daddiu $sp, $sp, -64
    sd $ra, 0($sp)
    sd $s0, 8($sp)
    sd $s1, 16($sp)
    sd $s2, 24($sp)
    sd $s3, 32($sp)
# BEGIN b_fields
    b bound_missing
    nop
# END b_fields
after_b_fields:
    nop
# BEGIN b_lengths
    b bound_missing
    nop
# END b_lengths
after_b_lengths:
    nop
# BEGIN b_heads
    b bound_missing
    nop
# END b_heads
after_b_heads:
    nop
bound_sample:
# BEGIN b_samples
    b bound_missing
    nop
# END b_samples
after_b_samples:
    nop
bound_done:
# BEGIN b_finish
    b bound_missing
    nop
# END b_finish
after_b_finish:
    nop
    b bound_restore
    nop
bound_bad:
    li $v0, 1
    move $v1, $zero
    b bound_restore
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
    jr $ra
    nop
write_head:
    daddiu $sp, $sp, -32
    sd $ra, 0($sp)
# BEGIN w_fields
    b write_missing
    nop
# END w_fields
after_w_fields:
    nop
# BEGIN w_info
    b write_missing
    nop
# END w_info
after_w_info:
    nop
write_argument:
# BEGIN w_argument
    b write_missing
    nop
# END w_argument
after_w_argument:
    nop
    b write_done
    nop
write_inline:
# BEGIN w_inline
    b write_missing
    nop
# END w_inline
after_w_inline:
    nop
write_done:
# BEGIN w_finish
    b write_missing
    nop
# END w_finish
after_w_finish:
    nop
    b write_restore
    nop
write_missing:
    li $v0, 4
    move $v1, $zero
write_restore:
    ld $ra, 0($sp)
    daddiu $sp, $sp, 32
    jr $ra
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
# BEGIN e_bound
    b encode_missing
    nop
# END e_bound
after_e_bound:
    nop
# BEGIN e_capacity
    b encode_missing
    nop
# END e_capacity
after_e_capacity:
    nop
# BEGIN e_counter
    b encode_missing
    nop
# END e_counter
after_e_counter:
    nop
# BEGIN e_identifier_head
    b encode_missing
    nop
# END e_identifier_head
after_e_identifier_head:
    nop
encode_identifier:
# BEGIN e_identifier_copy
    b encode_missing
    nop
# END e_identifier_copy
after_e_identifier_copy:
    nop
encode_readings:
# BEGIN e_array
    b encode_missing
    nop
# END e_array
after_e_array:
    nop
encode_sample:
# BEGIN e_samples
    b encode_missing
    nop
# END e_samples
after_e_samples:
    nop
encode_done:
# BEGIN e_finish
    b encode_missing
    nop
# END e_finish
after_e_finish:
    nop
    b encode_restore
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
    jr $ra
    nop
