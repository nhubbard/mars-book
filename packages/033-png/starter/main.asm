# Open with MARS Run or Debug after creating image.png beside this project.
# mars:tool bitmap width=1024 height=1024 unitWidth=2 unitHeight=2 base=heap
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
.align 3
collector_capacity: .dword 46
# Read-only DEFLATE format constants.
alphabet_order: .byte 16, 17, 18, 0, 8, 7, 9, 6, 10, 5, 11, 4, 12, 3, 13, 2, 14, 1, 15
length_extra: .byte 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 0
distance_extra: .byte 0, 0, 0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 8, 8, 9, 9, 10, 10, 11, 11, 12, 12, 13, 13
.align 1
length_base: .half 3, 4, 5, 6, 7, 8, 9, 10, 11, 13, 15, 17, 19, 23, 27, 31, 35, 43, 51, 59, 67, 83, 99, 115, 131, 163, 195, 227, 258
distance_base: .half 1, 2, 3, 4, 5, 7, 9, 13, 17, 25, 33, 49, 65, 97, 129, 193, 257, 385, 513, 769, 1025, 1537, 2049, 3073, 4097, 6145, 8193, 12289, 16385, 24577
.align 3

# Student-authored immutable RGB shuffle selectors. Keep exactly 64 bytes.
.align 4
# BEGIN pack_masks
rgb_shuffle: .byte 0:64
# END pack_masks
.align 3

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
    li $t1, 512
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
# Final caller wiring. Result publication remains a student-authored region.
png_decode:
    move $t9, $zero
    b publication_entry
    nop
png_decode_msa:
    li $t9, 1
publication_entry:
    li $v0, 1
    move $v1, $zero
    sltiu $t0, $a1, 8
    bnez $t0, publication_early
    nop
    dli $t0, 1048577
    sltu $t0, $a1, $t0
    beqz $t0, publication_early
    nop
    daddiu $sp, $sp, -112
    sd $ra, 0($sp)
    sd $a3, 48($sp)
    sd $t9, 56($sp)
    sd $a2, 64($sp)
    ld $a2, 16($a3)
    ld $t0, 24($a3)
    sd $t0, 8($sp)
    dli $t0, 0xa5a5a5a5a5a5a5a5
    sd $t0, 16($sp)
    sd $t0, 24($sp)
    sd $t0, 32($sp)
    sd $t0, 40($sp)
    daddiu $a3, $sp, 8
collector_call:
    jal png_collect
    nop
collector_return:
    bnez $v0, publication_fail
    nop
    ld $t0, 48($sp)
    ld $a0, 16($t0)
    ld $a1, 32($sp)
    ld $a2, 32($t0)
    ld $a3, 40($sp)
    ld $t1, 40($t0)
    sltu $t1, $t1, $a3
    beqz $t1, inflate_call
    nop
    li $v0, 1
    b publication_fail
    nop
inflate_call:
    jal png_inflate
    nop
inflate_return:
    bnez $v0, publication_fail
    nop
    ld $t0, 48($sp)
    ld $a0, 32($t0)
    ld $a1, 40($sp)
    ld $a2, 16($sp)
    ld $a3, 24($sp)
    ld $t0, 56($sp)
    beqz $t0, row_scalar_call
    nop
row_vector_call:
    jal png_unfilter_msa
    nop
    b publication_rows_return
    nop
row_scalar_call:
    jal png_unfilter
    nop
publication_rows_return:
    bnez $v0, publication_fail
    nop
    ld $t0, 16($sp)
    sd $t0, 80($sp)
    ld $t0, 24($sp)
    sd $t0, 88($sp)
    ld $t1, 48($sp)
    ld $t0, 0($t1)
    sd $t0, 96($sp)
    ld $t0, 8($t1)
    sd $t0, 104($sp)
    ld $a0, 32($t1)
    ld $a1, 40($sp)
    ld $a2, 64($sp)
    daddiu $a3, $sp, 80
    ld $t0, 56($sp)
    beqz $t0, packing_scalar_call
    nop
packing_vector_call:
    jal png_blit_msa
    nop
    b publication_blit_return
    nop
packing_scalar_call:
    jal png_blit
    nop
publication_blit_return:
    bnez $v0, publication_fail
    nop
# BEGIN publish_results
    li $v0, 4
    move $v1, $zero
    b publication_return
    nop
# END publish_results
publication_results_ready:
    nop
    b publication_return
    nop
publication_fail:
    move $v1, $zero
publication_return:
    ld $ra, 0($sp)
    daddiu $sp, $sp, 112
publication_early:
    jr $ra
    nop

# Append in the text section through attach.py. Every unfinished region exits safely.
png_collect:
# BEGIN collect_bounds
    li $v0, 4
    move $v1, $zero
    b collect_early
    nop
# END collect_bounds
collect_bounds_ready:
    nop
    daddiu $sp, $sp, -128
    sd $ra, 0($sp)
    sd $s0, 8($sp)
    sd $s1, 16($sp)
    sd $s2, 24($sp)
    sd $s3, 32($sp)
    sd $s4, 40($sp)
    sd $s5, 48($sp)
    sd $s6, 56($sp)
    sd $s7, 64($sp)
    sd $fp, 72($sp)
# BEGIN collect_state
    b collect_missing
    nop
# END collect_state
collect_state_ready:
    nop
# BEGIN collect_signature
    b collect_missing
    nop
# END collect_signature
collect_signature_ready:
    nop
collect_chunk:
# BEGIN collect_envelope
    b collect_missing
    nop
# END collect_envelope
collect_envelope_ready:
    nop
# BEGIN collect_type
    b collect_missing
    nop
# END collect_type
collect_type_ready:
    nop
# BEGIN collect_crc_check
    b collect_missing
    nop
# END collect_crc_check
collect_crc_ready:
    nop
# BEGIN collect_dispatch
    b collect_missing
    nop
# END collect_dispatch
collect_dispatch_ready:
    nop
collect_ancillary:
# BEGIN collect_ancillary
    b collect_missing
    nop
# END collect_ancillary
collect_ancillary_ready:
    nop
    b collect_next
    nop
collect_ihdr:
# BEGIN collect_ihdr
    b collect_missing
    nop
# END collect_ihdr
collect_ihdr_ready:
    nop
    b collect_next
    nop
collect_plte:
# BEGIN collect_plte
    b collect_missing
    nop
# END collect_plte
collect_plte_ready:
    nop
    b collect_next
    nop
collect_idat:
# BEGIN collect_idat
    b collect_missing
    nop
# END collect_idat
collect_idat_ready:
    nop
collect_copy:
# BEGIN collect_copy
    b collect_missing
    nop
# END collect_copy
collect_copy_ready:
    nop
collect_next:
# BEGIN collect_next
    b collect_missing
    nop
# END collect_next
collect_next_ready:
    nop
    b collect_chunk
    nop
collect_iend:
# BEGIN collect_iend
    b collect_missing
    nop
# END collect_iend
collect_iend_ready:
    nop
# BEGIN collect_publish
    b collect_missing
    nop
# END collect_publish
collect_publish_ready:
    nop
    b collect_restore
    nop
collect_missing:
    li $v0, 4
    move $v1, $zero
    b collect_restore
    nop
collect_bad:
    li $v0, 1
    move $v1, $zero
collect_restore:
    ld $ra, 0($sp)
    ld $s0, 8($sp)
    ld $s1, 16($sp)
    ld $s2, 24($sp)
    ld $s3, 32($sp)
    ld $s4, 40($sp)
    ld $s5, 48($sp)
    ld $s6, 56($sp)
    ld $s7, 64($sp)
    ld $fp, 72($sp)
    daddiu $sp, $sp, 128
collect_early:
    jr $ra
    nop

# Internal helpers: incomplete bodies leave through the collector-owned frame.
png_be32:
# BEGIN collect_be32
    b collect_missing
    nop
# END collect_be32
collect_be32_ready:
    nop
    jr $ra
    nop
png_crc32:
# BEGIN collect_crc
    b collect_missing
    nop
# END collect_crc
collect_crc32_ready:
    nop
    jr $ra
    nop

# Stored-block construction; fixed and dynamic paths remain explicit development exits.
png_inflate:
# BEGIN inflate_bounds
    li $v0, 4
    move $v1, $zero
    b inflate_early
    nop
# END inflate_bounds
inflate_bounds_ready:
    nop
    daddiu $sp, $sp, -2048
    sd $ra, 64($sp)
    sd $s0, 72($sp)
    sd $s1, 80($sp)
    sd $s2, 88($sp)
    sd $s3, 96($sp)
    sd $s4, 104($sp)
    sd $s5, 112($sp)
    sd $s6, 120($sp)
    sd $s7, 128($sp)
    sd $fp, 136($sp)
    move $fp, $sp
# BEGIN inflate_header
    b inflate_missing
    nop
# END inflate_header
inflate_header_ready:
    nop
# BEGIN inflate_state
    b inflate_missing
    nop
# END inflate_state
inflate_state_ready:
    nop
deflate_block:
    li $a0, 3
    jal read_bits
    nop
# BEGIN inflate_dispatch
    b inflate_missing
    nop
# END inflate_dispatch
inflate_dispatch_ready:
    nop
stored_block:
# BEGIN inflate_align
    b inflate_missing
    nop
# END inflate_align
inflate_align_ready:
    nop
# BEGIN inflate_lengths
    b inflate_missing
    nop
# END inflate_lengths
inflate_lengths_ready:
    nop
# BEGIN inflate_stored_bounds
    b inflate_missing
    nop
# END inflate_stored_bounds
inflate_stored_bounds_ready:
    nop
stored_copy:
# BEGIN inflate_copy
    b inflate_missing
    nop
# END inflate_copy
inflate_copy_ready:
    nop
block_done:
# BEGIN inflate_block_done
    b inflate_missing
    nop
# END inflate_block_done
inflate_final_ready:
    nop
# BEGIN inflate_exact
    b inflate_missing
    nop
# END inflate_exact
inflate_exact_ready:
    nop
    move $a0, $s7
    move $a1, $s5
    jal adler32
    nop
inflate_adler_ready:
    nop
    sd $v0, 0($sp)
    move $a0, $s1
    jal inflate_be32
    nop
inflate_trailer_ready:
    nop
# BEGIN inflate_checksum
    b inflate_missing
    nop
# END inflate_checksum
inflate_checksum_ready:
    nop
# BEGIN inflate_publish
    b inflate_missing
    nop
# END inflate_publish
inflate_publish_ready:
    nop
    b inflate_restore
    nop
fixed_block:
# BEGIN huff_fixed
    b inflate_missing
    nop
# END huff_fixed
huff_fixed_ready:
    nop
    b build_data_trees
    nop
dynamic_block:
# BEGIN huff_dynamic_counts
    b inflate_missing
    nop
# END huff_dynamic_counts
huff_dynamic_counts_ready:
    nop
# BEGIN huff_clear_alphabet
    b inflate_missing
    nop
# END huff_clear_alphabet
huff_alphabet_cleared:
    nop
read_alphabet:
# BEGIN huff_read_alphabet
    b inflate_missing
    nop
# END huff_read_alphabet
huff_alphabet_ready:
    nop
# BEGIN huff_code_tree
    b inflate_missing
    nop
# END huff_code_tree
huff_code_tree_ready:
    nop
read_length:
    daddiu $a0, $fp, 928
    jal decode_symbol
    nop
# BEGIN huff_direct_length
    b inflate_missing
    nop
# END huff_direct_length
huff_direct_length_ready:
    nop
    b store_lengths
    nop
length_repeat:
# BEGIN huff_repeat16
    b inflate_missing
    nop
# END huff_repeat16
huff_repeat16_ready:
    nop
    b store_lengths
    nop
length_repeat_zero:
# BEGIN huff_repeat17
    b inflate_missing
    nop
# END huff_repeat17
huff_repeat17_ready:
    nop
    b store_lengths
    nop
length_repeat_long:
# BEGIN huff_repeat18
    b inflate_missing
    nop
# END huff_repeat18
huff_repeat18_ready:
    nop
store_lengths:
# BEGIN huff_store_lengths
    b inflate_missing
    nop
# END huff_store_lengths
huff_lengths_stored:
    nop
    bne $t0, $t3, read_length
    nop
# BEGIN huff_end_code
    b inflate_missing
    nop
# END huff_end_code
huff_end_code_ready:
    nop
    b build_data_trees
    nop

# Supplied calls establish the two table interfaces; their algorithms remain student work.
build_data_trees:
    ld $a1, 0($fp)
    daddiu $a2, $fp, 160
    daddiu $a0, $fp, 1096
    li $a3, 1
    jal build_tree
    nop
    ld $t0, 0($fp)
    daddiu $a0, $fp, 1096
    daddu $a0, $a0, $t0
    ld $a1, 8($fp)
    li $a3, 2
    daddiu $a2, $fp, 800
    jal build_tree
    nop
huff_data_trees_ready:
    nop
inflate_symbol:
    daddiu $a0, $fp, 160
    jal decode_symbol
    nop
# BEGIN huff_classify
    b inflate_missing
    nop
# END huff_classify
huff_classified:
    nop
# BEGIN huff_length
    b inflate_missing
    nop
# END huff_length
huff_length_ready:
    nop
# BEGIN huff_distance_symbol
    b inflate_missing
    nop
# END huff_distance_symbol
huff_distance_symbol_ready:
    nop
# BEGIN huff_distance_extra
    b inflate_missing
    nop
# END huff_distance_extra
huff_distance_ready:
    nop
# BEGIN huff_match_bounds
    b inflate_missing
    nop
# END huff_match_bounds
huff_match_bounds_ready:
    nop
copy_match:
# BEGIN huff_match_copy
    b inflate_missing
    nop
# END huff_match_copy
huff_match_copied:
    nop
    b inflate_symbol
    nop
emit_literal:
# BEGIN huff_literal
    b inflate_missing
    nop
# END huff_literal
huff_literal_ready:
    nop
    b inflate_symbol
    nop
inflate_missing:
    li $v0, 4
    move $v1, $zero
    b inflate_restore
    nop
inflate_bad:
    li $v0, 1
    move $v1, $zero
inflate_restore:
    ld $ra, 64($sp)
    ld $s0, 72($sp)
    ld $s1, 80($sp)
    ld $s2, 88($sp)
    ld $s3, 96($sp)
    ld $s4, 104($sp)
    ld $s5, 112($sp)
    ld $s6, 120($sp)
    ld $s7, 128($sp)
    ld $fp, 136($sp)
    daddiu $sp, $sp, 2048
inflate_early:
    jr $ra
    nop

# Until complete, internal helpers unwind the owning inflater frame.
read_bits:
# BEGIN inflate_refill
    b inflate_missing
    nop
# END inflate_refill
inflate_refill_unreached:
    nop
bits_ready:
# BEGIN inflate_extract
    b inflate_missing
    nop
# END inflate_extract
inflate_bits_ready:
    nop
    jr $ra
    nop
adler32:
# BEGIN inflate_adler
    b inflate_missing
    nop
# END inflate_adler
inflate_adler_return_ready:
    nop
    jr $ra
    nop
inflate_be32:
# BEGIN inflate_trailer
    b inflate_missing
    nop
# END inflate_trailer
inflate_be32_ready:
    nop
    jr $ra
    nop

# Internal helpers use the owning inflater frame.
build_tree:
# BEGIN huff_tree_clear
    b inflate_missing
    nop
# END huff_tree_clear
huff_tree_cleared:
    nop
tree_count:
# BEGIN huff_tree_count
    b inflate_missing
    nop
# END huff_tree_count
huff_tree_counted:
    nop
# BEGIN huff_tree_prefix
    b inflate_missing
    nop
# END huff_tree_prefix
huff_tree_prefix_ready:
    nop
# BEGIN huff_tree_shape
    b inflate_missing
    nop
# END huff_tree_shape
tree_shape_ok:
huff_tree_shape_ready:
    nop
# BEGIN huff_tree_symbols
    b inflate_missing
    nop
# END huff_tree_symbols
huff_tree_symbols_ready:
    nop
    jr $ra
    nop
decode_symbol:
# BEGIN huff_symbol_state
    b inflate_missing
    nop
# END huff_symbol_state
huff_symbol_state_ready:
    nop
decode_bit:
# BEGIN huff_symbol_refill
    b inflate_missing
    nop
# END huff_symbol_refill
huff_symbol_refill_fallthrough:
    nop
decode_has_byte:
# BEGIN huff_symbol_step
    b inflate_missing
    nop
# END huff_symbol_step
huff_symbol_step_unreached:
    nop
decode_found:
# BEGIN huff_symbol_found
    b inflate_missing
    nop
# END huff_symbol_found
huff_symbol_ready:
    nop
    jr $ra
    nop

# Unfinished row reconstruction. The two entries share a per-call working frame.
png_unfilter:
    move $t9, $zero
    b unfilter_entry
    nop
png_unfilter_msa:
    li $t9, 1
unfilter_entry:
    li $v0, 1
    move $v1, $zero
# BEGIN row_shape
    b row_missing_early
    nop
# END row_shape
row_shape_ready:
    nop
# BEGIN row_selectors
    b row_missing_early
    nop
# END row_selectors
row_selectors_ready:
    nop
    daddiu $sp, $sp, -1552
    sd $a1, 1536($sp)
# BEGIN row_zero
    b row_missing_frame
    nop
# END row_zero
row_zero_ready:
    nop
filter_row:
# BEGIN row_cursor
    b row_missing_frame
    nop
# END row_cursor
row_cursor_ready:
    nop
    beqz $t0, filter_row_done
    nop
    beqz $v1, filter_byte
    nop
    li $t4, 2
    beq $t0, $t4, filter_up_vector
    nop
filter_byte:
# BEGIN row_neighbors
    b row_missing_frame
    nop
# END row_neighbors
row_neighbors_ready:
    nop
filter_predict:
    li $t7, 1
    beq $t0, $t7, filter_left
    nop
    li $t7, 2
    beq $t0, $t7, filter_store
    nop
    li $t7, 3
    beq $t0, $t7, filter_average
    nop
# BEGIN row_paeth
    b row_missing_frame
    nop
# END row_paeth
row_paeth_ready:
    nop
    b filter_store
    nop
filter_left:
# BEGIN row_left
    b row_missing_frame
    nop
# END row_left
row_left_ready:
    nop
    b filter_store
    nop
filter_average:
# BEGIN row_average
    b row_missing_frame
    nop
# END row_average
row_average_ready:
    nop
filter_store:
# BEGIN row_store
    b row_missing_frame
    nop
# END row_store
row_store_ready:
    nop
    bne $t1, $t3, filter_byte
    nop
    b filter_row_done
    nop
filter_up_vector:
# BEGIN row_vector_bound
    b row_missing_frame
    nop
# END row_vector_bound
row_vector_ready:
    nop
    bnez $t4, filter_up_tail
    nop
# BEGIN row_vector_body
    b row_missing_frame
    nop
# END row_vector_body
row_vector_written:
    nop
    b filter_up_vector
    nop
filter_up_tail:
# BEGIN row_up_tail
    b row_missing_frame
    nop
# END row_up_tail
row_up_tail_ready:
    nop
filter_row_done:
# BEGIN row_finish
    b row_missing_frame
    nop
# END row_finish
row_advance_ready:
    nop
    bnez $a1, filter_row
    nop
    ld $v1, 1536($sp)
    daddiu $sp, $sp, 1552
    move $v0, $zero
row_result_ready:
    nop
    jr $ra
    nop
row_missing_frame:
    daddiu $sp, $sp, 1552
row_missing_early:
    li $v0, 4
    move $v1, $zero
row_early:
    jr $ra
    nop

# Unfinished RGB8 packing; descriptor fields and result contract are in the chapter.
png_blit:
    move $t9, $zero
    b blit_entry
    nop
png_blit_msa:
    li $t9, 1
blit_entry:
    li $v0, 1
    move $v1, $zero
# BEGIN pack_admit
    b pack_missing
    nop
# END pack_admit
pack_admitted:
    nop
# BEGIN pack_windows
    b pack_missing
    nop
# END pack_windows
pack_windows_ready:
    nop
# BEGIN pack_state
    b pack_missing
    nop
# END pack_state
pack_state_ready:
    nop
    beqz $v1, blit_row
    nop
pack_masks_ready:
    nop
# BEGIN pack_load_masks
    b pack_missing
    nop
# END pack_load_masks
pack_masks_loaded:
    nop
blit_row:
# BEGIN pack_cursor
    b pack_missing
    nop
# END pack_cursor
pack_cursor_ready:
    nop
    beqz $v1, blit_pixel
    nop
blit_vector:
# BEGIN pack_vector_bound
    b pack_missing
    nop
# END pack_vector_bound
pack_vector_ready:
    nop
    bnez $t2, blit_tail
    nop
# BEGIN pack_vector_load
    b pack_missing
    nop
# END pack_vector_load
pack_inputs_loaded:
    nop
# BEGIN pack_shuffle_first
    b pack_missing
    nop
# END pack_shuffle_first
pack_first_written:
    nop
# BEGIN pack_shuffle_middle
    b pack_missing
    nop
# END pack_shuffle_middle
pack_middle_written:
    nop
# BEGIN pack_shuffle_last
    b pack_missing
    nop
# END pack_shuffle_last
pack_last_written:
    nop
# BEGIN pack_vector_advance
    b pack_missing
    nop
# END pack_vector_advance
pack_vector_advanced:
    nop
    b blit_vector
    nop
blit_tail:
    beqz $t1, blit_row_done
    nop
blit_pixel:
# BEGIN pack_pixel
    b pack_missing
    nop
# END pack_pixel
pack_pixel_written:
    nop
    bnez $t1, blit_pixel
    nop
blit_row_done:
# BEGIN pack_row
    b pack_missing
    nop
# END pack_row
pack_row_advanced:
    nop
    bnez $a3, blit_row
    nop
    move $v1, $t8
    move $v0, $zero
pack_success:
    jr $ra
    nop
pack_missing:
    li $v0, 4
    move $v1, $zero
pack_early:
    jr $ra
    nop
