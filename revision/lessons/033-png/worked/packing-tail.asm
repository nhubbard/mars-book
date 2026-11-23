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
.align 3
collector_capacity: .dword 46
alphabet_order: .byte 16, 17, 18, 0, 8, 7, 9, 6, 10, 5, 11, 4, 12, 3, 13, 2, 14, 1, 15
length_extra: .byte 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 0
distance_extra: .byte 0, 0, 0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 8, 8, 9, 9, 10, 10, 11, 11, 12, 12, 13, 13
.align 1
length_base: .half 3, 4, 5, 6, 7, 8, 9, 10, 11, 13, 15, 17, 19, 23, 27, 31, 35, 43, 51, 59, 67, 83, 99, 115, 131, 163, 195, 227, 258
distance_base: .half 1, 2, 3, 4, 5, 7, 9, 13, 17, 25, 33, 49, 65, 97, 129, 193, 257, 385, 513, 769, 1025, 1537, 2049, 3073, 4097, 6145, 8193, 12289, 16385, 24577
.align 3

.align 4
rgb_shuffle:
    .byte 2, 1, 0, -128, 5, 4, 3, -128, 8, 7, 6, -128, 11, 10, 9, -128
    .byte 14, 13, 12, -128, 17, 16, 15, -128, 20, 19, 18, -128, 23, 22, 21, -128
    .byte 10, 9, 8, -128, 13, 12, 11, -128, 16, 15, 14, -128, 19, 18, 17, -128
    .byte 6, 5, 4, -128, 9, 8, 7, -128, 12, 11, 10, -128, 15, 14, 13, -128
.align 3

# One row crosses the sixteen-pixel vector boundary; samples are byte values 0..50.
lesson_row: .byte 0,0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50
.align 3
lesson_descriptor: .dword 17,1,17,68
lesson_output: .byte -91:80
.text
.globl main
main:
    dla $sp,workspace
    daddiu $sp,$sp,16384
    dla $a0,lesson_row
    li $a1,52
    dla $a2,lesson_output
    dla $a3,lesson_descriptor
    balc png_blit_msa
    nop
    dla $t0,lesson_output
    lwu $s0,20($t0)
    lwu $s1,60($t0)
    lwu $s2,64($t0)
    lbu $s3,68($t0)
packed_tail:
    li $v0,10
    syscall
png_load:
    li $v0, 1
    move $v1, $zero
    beqzc $a2, load_early
    nop
    dli $t0, 1048577
    sltu $t0, $a2, $t0
    beqzc $t0, load_early
    nop
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
    move $s0, $a1
    move $s1, $a2
    move $s2, $zero
    li $s4, 1
load_state_ready:
    nop
    move $a1, $zero
    move $a2, $zero
    li $v0, 13
    syscall
    bltzc $v0, load_open_failed
    nop
    move $s3, $v0
load_open_ready:
    nop
load_read:
    beqc $s2, $s1, load_probe
    nop
    move $a0, $s3
    daddu $a1, $s0, $s2
    dsubu $a2, $s1, $s2

    li $v0, 14
    syscall
load_request_ready:
    nop
    bltzc $v0, load_close
    nop
    beqzc $v0, load_eof
    nop
    daddu $s2, $s2, $v0
load_progress_ready:
    nop
    bc load_read
    nop
load_probe:
    move $a0, $s3
    daddiu $a1, $sp, 64
    li $a2, 1
    li $v0, 14
    syscall
    bnezc $v0, load_close
    nop
load_probe_ready:
    nop
load_eof:
    beqzc $s2, load_close
    nop
    move $s4, $zero
load_eof_ready:
    nop
load_close:
    move $a0, $s3
    li $v0, 16
    syscall
load_close_ready:
    nop
    move $v0, $s4
    move $v1, $zero
    bnezc $s4, load_restore
    nop
    move $v1, $s2
load_publish_ready:
    nop
    bc load_restore
    nop
load_missing_owned:
    # Later unfinished regions release the descriptor acquired by load_open.
    move $a0, $s3
    li $v0, 16
    syscall
load_missing:
    li $v0, 4
    move $v1, $zero
    bc load_restore
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
    jrc $ra
    nop
png_decode:
    move $t9, $zero
    bc publication_entry
    nop
png_decode_msa:
    li $t9, 1
publication_entry:
    li $v0, 1
    move $v1, $zero
    sltiu $t0, $a1, 8
    bnezc $t0, publication_early
    nop
    dli $t0, 1048577
    sltu $t0, $a1, $t0
    beqzc $t0, publication_early
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
    balc png_collect
    nop
collector_return:
    bnezc $v0, publication_fail
    nop
    ld $t0, 48($sp)
    ld $a0, 16($t0)
    ld $a1, 32($sp)
    ld $a2, 32($t0)
    ld $a3, 40($sp)
    ld $t1, 40($t0)
    sltu $t1, $t1, $a3
    beqzc $t1, inflate_call
    nop
    li $v0, 1
    bc publication_fail
    nop
inflate_call:
    balc png_inflate
    nop
inflate_return:
    bnezc $v0, publication_fail
    nop
    ld $t0, 48($sp)
    ld $a0, 32($t0)
    ld $a1, 40($sp)
    ld $a2, 16($sp)
    ld $a3, 24($sp)
    ld $t0, 56($sp)
    beqzc $t0, row_scalar_call
    nop
row_vector_call:
    balc png_unfilter_msa
    nop
    bc publication_rows_return
    nop
row_scalar_call:
    balc png_unfilter
    nop
publication_rows_return:
    bnezc $v0, publication_fail
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
    beqzc $t0, packing_scalar_call
    nop
packing_vector_call:
    balc png_blit_msa
    nop
    bc publication_blit_return
    nop
packing_scalar_call:
    balc png_blit
    nop
publication_blit_return:
    bnezc $v0, publication_fail
    nop
    ld $t1, 48($sp)
    ld $t0, 16($sp)
    sd $t0, 48($t1)
    ld $t0, 24($sp)
    sd $t0, 56($t1)
    sd $v1, 64($t1)
publication_results_ready:
    nop
    bc publication_return
    nop
publication_fail:
    move $v1, $zero
publication_return:
    ld $ra, 0($sp)
    daddiu $sp, $sp, 112
publication_early:
    jrc $ra
    nop

png_collect:
    li $v0, 1
    move $v1, $zero
    dli $t0, 1048577
    sltu $t0, $a1, $t0
    beqzc $t0, collect_early
    nop
    sltiu $t0, $a1, 8
    bnezc $t0, collect_early
    nop
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
    move $s0, $a0
    move $s1, $a1
    move $s2, $a2
    move $s4, $a3
    ld $s3, 0($s4)
    move $s5, $zero
    move $fp, $zero
collect_state_ready:
    nop
    balc png_be32
    nop
    dli $t0, 0x89504e47
    bnec $v0, $t0, collect_bad
    nop
    daddiu $a0, $s0, 4
    balc png_be32
    nop
    dli $t0, 0x0d0a1a0a
    bnec $v0, $t0, collect_bad
    nop
    daddiu $s0, $s0, 8
    daddiu $s1, $s1, -8
collect_signature_ready:
    nop
collect_chunk:
    sltiu $t0, $s1, 12
    bnezc $t0, collect_bad
    nop
    move $a0, $s0
    balc png_be32
    nop
    sd $v0, 80($sp)
    dsrl $t0, $v0, 31
    bnezc $t0, collect_bad
    nop
    daddiu $t0, $s1, -12
    sltu $t0, $t0, $v0
    bnezc $t0, collect_bad
    nop
collect_envelope_ready:
    nop
    daddiu $t0, $s0, 4
    li $t1, 4
collect_type_letter:
    lbu $t2, 0($t0)
    ori $t3, $t2, 32
    daddiu $t3, $t3, -97
    sltiu $t3, $t3, 26
    beqzc $t3, collect_bad
    nop
    daddiu $t0, $t0, 1
    addiu $t1, $t1, -1
    bnezc $t1, collect_type_letter
    nop
    lbu $t0, 6($s0)
    andi $t0, $t0, 32
    bnezc $t0, collect_bad
    nop
collect_type_ready:
    nop
    daddiu $a0, $s0, 4
    ld $a1, 80($sp)
    daddiu $a1, $a1, 4
    balc png_crc32
    nop
    sd $v0, 104($sp)
    ld $t0, 80($sp)
    daddu $a0, $s0, $t0
    daddiu $a0, $a0, 8
    balc png_be32
    nop
    ld $t0, 104($sp)
    bnec $v0, $t0, collect_bad
    nop
collect_crc_ready:
    nop
    daddiu $a0, $s0, 4
    balc png_be32
    nop
    sd $v0, 88($sp)
    dli $t0, 0x49484452
    beqc $v0, $t0, collect_ihdr
    nop
    beqzc $s5, collect_bad
    nop
    dli $t0, 0x49444154
    beqc $v0, $t0, collect_idat
    nop
    dli $t0, 0x49454e44
    beqc $v0, $t0, collect_iend
    nop
    dli $t0, 0x504c5445
    beqc $v0, $t0, collect_plte
    nop
collect_dispatch_ready:
    nop
collect_ancillary:
    dli $t0, 0x74524e53
    beqc $v0, $t0, collect_bad
    nop
    dli $t0, 0x6163544c
    beqc $v0, $t0, collect_bad
    nop
    dli $t0, 0x6663544c
    beqc $v0, $t0, collect_bad
    nop
    dli $t0, 0x66644154
    beqc $v0, $t0, collect_bad
    nop
    lbu $t0, 4($s0)
    andi $t0, $t0, 32
    beqzc $t0, collect_bad
    nop
    andi $t0, $s5, 4
    beqzc $t0, collect_ancillary_ready
    nop
    ori $s5, $s5, 8
collect_ancillary_ready:
    nop
    bc collect_next
    nop
collect_ihdr:
    bnezc $s5, collect_bad
    nop
    ld $t0, 80($sp)
    li $t1, 13
    bnec $t0, $t1, collect_bad
    nop
    daddiu $a0, $s0, 8
    balc png_be32
    nop
    beqzc $v0, collect_bad
    nop
    sltiu $t0, $v0, 513
    beqzc $t0, collect_bad
    nop
    move $s6, $v0
    daddiu $a0, $s0, 12
    balc png_be32
    nop
    beqzc $v0, collect_bad
    nop
    sltiu $t0, $v0, 513
    beqzc $t0, collect_bad
    nop
    move $s7, $v0
    daddiu $a0, $s0, 16
    balc png_be32
    nop
    dli $t0, 0x08020000
    bnec $v0, $t0, collect_bad
    nop
    lbu $t0, 20($s0)
    bnezc $t0, collect_bad
    nop
    li $s5, 1
collect_ihdr_ready:
    nop
    bc collect_next
    nop
collect_plte:
    andi $t0, $s5, 6
    bnezc $t0, collect_bad
    nop
    ld $t0, 80($sp)
    beqzc $t0, collect_bad
    nop
    sltiu $t1, $t0, 769
    beqzc $t1, collect_bad
    nop
    li $t1, 3
    dmodu $t2, $t0, $t1
    bnezc $t2, collect_bad
    nop
    ori $s5, $s5, 2
collect_plte_ready:
    nop
    bc collect_next
    nop
collect_idat:
    andi $t0, $s5, 8
    bnezc $t0, collect_bad
    nop
    ld $t0, 80($sp)
    sltu $t1, $s3, $t0
    bnezc $t1, collect_bad
    nop
    dsubu $s3, $s3, $t0
    daddu $fp, $fp, $t0
    ori $s5, $s5, 4
    daddiu $t1, $s0, 8
    beqzc $t0, collect_copy_ready
    nop
collect_idat_ready:
    nop
collect_copy:
    lbu $t2, 0($t1)
    sb $t2, 0($s2)
    daddiu $t1, $t1, 1
    daddiu $s2, $s2, 1
    daddiu $t0, $t0, -1
    bnezc $t0, collect_copy
    nop
collect_copy_ready:
    nop
collect_next:
    ld $t0, 80($sp)
    daddiu $t0, $t0, 12
    daddu $s0, $s0, $t0
    dsubu $s1, $s1, $t0
collect_next_ready:
    nop
    bc collect_chunk
    nop
collect_iend:
    ld $t0, 80($sp)
    bnezc $t0, collect_bad
    nop
    li $t0, 12
    bnec $s1, $t0, collect_bad
    nop
    andi $t0, $s5, 4
    beqzc $t0, collect_bad
    nop
    sltiu $t0, $fp, 6
    bnezc $t0, collect_bad
    nop

collect_iend_ready:
    nop
    sd $s6, 8($s4)
    sd $s7, 16($s4)
    sd $fp, 24($s4)
    dsll $t0, $s6, 1
    daddu $t0, $t0, $s6
    daddiu $t0, $t0, 1
    dmul $t0, $t0, $s7
    sd $t0, 32($s4)
    move $v1, $fp
    move $v0, $zero
collect_publish_ready:
    nop
    bc collect_restore
    nop
collect_missing:
    li $v0, 4
    move $v1, $zero
    bc collect_restore
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
    jrc $ra
    nop

# Internal helpers: incomplete bodies leave through the collector-owned frame.
png_be32:
    move $v0, $zero
    li $t1, 4
be32_loop:
    lbu $t0, 0($a0)
    dsll $v0, $v0, 8
    or $v0, $v0, $t0
    daddiu $a0, $a0, 1
    addiu $t1, $t1, -1
    bnezc $t1, be32_loop
    nop
collect_be32_ready:
    nop
    jrc $ra
    nop
png_crc32:
    dli $v0, 0xffffffff
    dli $t4, 0xedb88320
    dli $t5, 0xffffffff
crc_byte:
    beqzc $a1, crc_done
    nop
    lbu $t0, 0($a0)
    xor $v0, $v0, $t0
    li $t1, 8
crc_bit:
    andi $t2, $v0, 1
    srl $v0, $v0, 1
    beqzc $t2, crc_skip
    nop
    xor $v0, $v0, $t4
crc_skip:
    addiu $t1, $t1, -1
    bnezc $t1, crc_bit
    nop
    daddiu $a0, $a0, 1
    daddiu $a1, $a1, -1
    bc crc_byte
    nop
crc_done:
    xor $v0, $v0, $t5
collect_crc32_ready:
    nop
    jrc $ra
    nop

png_inflate:
    li $v0, 1
    move $v1, $zero
    sltiu $t0, $a1, 6
    bnezc $t0, inflate_early
    nop
    dli $t0, 1048577
    sltu $t0, $a1, $t0
    beqzc $t0, inflate_early
    nop
    dli $t0, 786945
    sltu $t0, $a3, $t0
    beqzc $t0, inflate_early
    nop
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
    lbu $t0, 0($a0)
    lbu $t1, 1($a0)
    andi $t2, $t0, 15
    li $t3, 8
    bnec $t2, $t3, inflate_bad
    nop
    sltiu $t2, $t0, 128
    beqzc $t2, inflate_bad
    nop
    srl $t2, $t0, 4
    daddiu $t2, $t2, 8
    li $t3, 1
    dsllv $t3, $t3, $t2
    sd $t3, 56($sp)
    andi $t2, $t1, 32
    bnezc $t2, inflate_bad         # PNG zlib streams do not use preset dictionaries
    nop
    sll $t0, $t0, 8
    or $t0, $t0, $t1
    li $t1, 31
    modu $t0, $t0, $t1
    bnezc $t0, inflate_bad
    nop
inflate_header_ready:
    nop
    daddiu $s0, $a0, 2
    daddu $s1, $a0, $a1
    daddiu $s1, $s1, -4
    move $s2, $zero
    move $s3, $zero
    move $s4, $a2
    daddu $s5, $a2, $a3
    move $s7, $a2
inflate_state_ready:
    nop
deflate_block:
    li $a0, 3
    balc read_bits
    nop
    andi $s6, $v0, 1
    srl $t0, $v0, 1
    beqzc $t0, stored_block
    nop
    li $t1, 1
    beqc $t0, $t1, fixed_block
    nop
    li $t1, 2
    beqc $t0, $t1, dynamic_block
    nop
    bc inflate_bad
    nop

inflate_dispatch_ready:
    nop
stored_block:
    move $s2, $zero
    move $s3, $zero
inflate_align_ready:
    nop
    li $a0, 16
    balc read_bits
    nop
    sd $v0, 0($sp)
    li $a0, 16
    balc read_bits
    nop
    ld $t0, 0($sp)
    xor $t1, $t0, $v0
    li $t2, 65535
    bnec $t1, $t2, inflate_bad
    nop
inflate_lengths_ready:
    nop
    dsubu $t1, $s1, $s0
    bgtu $t0, $t1, inflate_bad
    nop
    dsubu $t1, $s5, $s4
    bgtu $t0, $t1, inflate_bad
    nop
    daddu $t1, $s0, $t0
inflate_stored_bounds_ready:
    nop
stored_copy:
    beqc $s0, $t1, inflate_copy_ready
    nop
    lbu $t2, 0($s0)
    sb $t2, 0($s4)
    daddiu $s0, $s0, 1
    daddiu $s4, $s4, 1
    bc stored_copy
    nop

inflate_copy_ready:
    nop
block_done:
    beqzc $s6, deflate_block
    nop
inflate_final_ready:
    nop
    bnec $s4, $s5, inflate_bad
    nop
    bnec $s0, $s1, inflate_bad
    nop
inflate_exact_ready:
    nop
    move $a0, $s7
    move $a1, $s5
    balc adler32
    nop
inflate_adler_ready:
    nop
    sd $v0, 0($sp)
    move $a0, $s1
    balc inflate_be32
    nop
inflate_trailer_ready:
    nop
    ld $t0, 0($sp)
    bnec $v0, $t0, inflate_bad
    nop
inflate_checksum_ready:
    nop
    dsubu $v1, $s5, $s7
    move $v0, $zero
inflate_publish_ready:
    nop
    bc inflate_restore
    nop
fixed_block:
    daddiu $t0, $fp, 1096
    move $t1, $zero
    li $t2, 288
fixed_literal_length:
    li $t3, 8
    sltiu $t4, $t1, 144
    bnezc $t4, fixed_store_length
    nop
    li $t3, 9
    sltiu $t4, $t1, 256
    bnezc $t4, fixed_store_length
    nop
    li $t3, 7
    sltiu $t4, $t1, 280
    bnezc $t4, fixed_store_length
    nop
    li $t3, 8
fixed_store_length:
    sb $t3, 0($t0)
    daddiu $t0, $t0, 1
    daddiu $t1, $t1, 1
    bnec $t1, $t2, fixed_literal_length
    nop
    li $t1, 32
    li $t3, 5
fixed_distance_length:
    sb $t3, 0($t0)
    daddiu $t0, $t0, 1
    daddiu $t1, $t1, -1
    bnezc $t1, fixed_distance_length
    nop
    li $t0, 288
    sd $t0, 0($sp)
    li $t0, 32
    sd $t0, 8($sp)
huff_fixed_ready:
    nop
    bc build_data_trees
    nop
dynamic_block:
    li $a0, 5
    balc read_bits
    nop
    daddiu $v0, $v0, 257
    sltiu $t0, $v0, 287
    beqzc $t0, inflate_bad
    nop
    sd $v0, 0($sp)               # literal code count
    li $a0, 5
    balc read_bits
    nop
    daddiu $v0, $v0, 1
    sd $v0, 8($sp)               # distance code count
    li $a0, 4
    balc read_bits
    nop
    daddiu $v0, $v0, 4
    sd $v0, 16($sp)               # code length alphabet count
huff_dynamic_counts_ready:
    nop
    daddiu $t0, $fp, 1416
    li $t1, 19
clear_alphabet:
    sb $zero, 0($t0)
    daddiu $t0, $t0, 1
    daddiu $t1, $t1, -1
    bnezc $t1, clear_alphabet
    nop
    sd $zero, 24($sp)
huff_alphabet_cleared:
    nop
read_alphabet:
    li $a0, 3
    balc read_bits
    nop
    ld $t0, 24($sp)
    la $t1, alphabet_order
    daddu $t1, $t1, $t0
    lbu $t1, 0($t1)
    daddiu $t2, $fp, 1416
    daddu $t2, $t2, $t1
    sb $v0, 0($t2)
    daddiu $t0, $t0, 1
    sd $t0, 24($sp)
    ld $t1, 16($sp)
    bnec $t0, $t1, read_alphabet
    nop
huff_alphabet_ready:
    nop
    daddiu $a0, $fp, 1416
    li $a1, 19
    daddiu $a2, $fp, 928
    move $a3, $zero
    balc build_tree
    nop
    ld $t0, 0($sp)
    ld $t1, 8($sp)
    daddu $t0, $t0, $t1
    daddiu $t1, $fp, 1096
    daddu $t0, $t0, $t1
    sd $t0, 16($sp)               # end of combined code lengths
    sd $t1, 24($sp)              # code length cursor
huff_code_tree_ready:
    nop
read_length:
    daddiu $a0, $fp, 928
    balc decode_symbol
    nop
    sltiu $t0, $v0, 16
    beqzc $t0, length_repeat
    nop
    move $t1, $v0
    li $t2, 1
huff_direct_length_ready:
    nop
    bc store_lengths
    nop
length_repeat:
    li $t0, 16
    bnec $v0, $t0, length_repeat_zero
    nop
    ld $t0, 24($sp)
    daddiu $t1, $fp, 1096
    beqc $t0, $t1, inflate_bad
    nop
    lbu $t1, -1($t0)
    sd $t1, 32($sp)
    li $a0, 2
    balc read_bits
    nop
    daddiu $t2, $v0, 3
    ld $t1, 32($sp)
huff_repeat16_ready:
    nop
    bc store_lengths
    nop
length_repeat_zero:
    li $t0, 17
    bnec $v0, $t0, length_repeat_long
    nop
    li $a0, 3
    balc read_bits
    nop
    daddiu $t2, $v0, 3
    move $t1, $zero
huff_repeat17_ready:
    nop
    bc store_lengths
    nop
length_repeat_long:
    li $a0, 7
    balc read_bits
    nop
    daddiu $t2, $v0, 11
    move $t1, $zero
huff_repeat18_ready:
    nop
store_lengths:
    ld $t0, 24($sp)
    ld $t3, 16($sp)
    dsubu $t4, $t3, $t0
    bgtu $t2, $t4, inflate_bad
    nop
length_store_byte:
    sb $t1, 0($t0)
    daddiu $t0, $t0, 1
    daddiu $t2, $t2, -1
    bnezc $t2, length_store_byte
    nop
    sd $t0, 24($sp)
huff_lengths_stored:
    nop
    bnec $t0, $t3, read_length
    nop
    daddiu $t0, $fp, 1096
    lbu $t1, 256($t0)
    beqzc $t1, inflate_bad         # each compressed block needs an end-of-block code
    nop
huff_end_code_ready:
    nop
    bc build_data_trees
    nop

build_data_trees:
    ld $a1, 0($fp)
    daddiu $a2, $fp, 160
    daddiu $a0, $fp, 1096
    li $a3, 1
    balc build_tree
    nop
    ld $t0, 0($fp)
    daddiu $a0, $fp, 1096
    daddu $a0, $a0, $t0
    ld $a1, 8($fp)
    li $a3, 2
    daddiu $a2, $fp, 800
    balc build_tree
    nop
huff_data_trees_ready:
    nop
inflate_symbol:
    daddiu $a0, $fp, 160
    balc decode_symbol
    nop
    sltiu $t0, $v0, 256
    bnezc $t0, emit_literal
    nop
    li $t0, 256
    beqc $v0, $t0, block_done
    nop
    daddiu $t0, $v0, -257
    sltiu $t1, $t0, 29
    beqzc $t1, inflate_bad
    nop
huff_classified:
    nop
    la $t1, length_extra
    daddu $t1, $t1, $t0
    lbu $a0, 0($t1)
    la $t1, length_base
    sll $t0, $t0, 1
    daddu $t1, $t1, $t0
    lhu $t1, 0($t1)
    sd $t1, 32($sp)
    balc read_bits
    nop
    ld $t1, 32($sp)
    daddu $t1, $t1, $v0
    sd $t1, 32($sp)
huff_length_ready:
    nop
    daddiu $a0, $fp, 800
    balc decode_symbol
    nop
    sltiu $t0, $v0, 30
    beqzc $t0, inflate_bad
    nop
huff_distance_symbol_ready:
    nop
    la $t0, distance_extra
    daddu $t0, $t0, $v0
    lbu $a0, 0($t0)
    la $t0, distance_base
    sll $t1, $v0, 1
    daddu $t0, $t0, $t1
    lhu $t0, 0($t0)
    sd $t0, 40($sp)
    balc read_bits
    nop
    ld $t0, 40($sp)
    daddu $t0, $t0, $v0
huff_distance_ready:
    nop
    ld $t1, 56($sp)
    bgtu $t0, $t1, inflate_bad
    nop
    dsubu $t1, $s4, $s7
    bgtu $t0, $t1, inflate_bad
    nop
    ld $t1, 32($sp)
    dsubu $t2, $s5, $s4
    bgtu $t1, $t2, inflate_bad
    nop
    dsubu $t0, $s4, $t0
    daddu $t1, $s4, $t1
huff_match_bounds_ready:
    nop
copy_match:
    lbu $t2, 0($t0)              # forward copy also handles overlapping matches
    sb $t2, 0($s4)
    daddiu $t0, $t0, 1
    daddiu $s4, $s4, 1
    bnec $s4, $t1, copy_match
    nop
huff_match_copied:
    nop
    bc inflate_symbol
    nop
emit_literal:
    beqc $s4, $s5, inflate_bad
    nop
    sb $v0, 0($s4)
    daddiu $s4, $s4, 1
huff_literal_ready:
    nop
    bc inflate_symbol
    nop
inflate_missing:
    li $v0, 4
    move $v1, $zero
    bc inflate_restore
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
    jrc $ra
    nop

# Until complete, internal helpers unwind the owning inflater frame.
read_bits:
    bgeu $s3, $a0, bits_ready
    nop
    beqc $s0, $s1, inflate_bad
    nop
    lbu $t0, 0($s0)
    daddiu $s0, $s0, 1
    sllv $t0, $t0, $s3
    or $s2, $s2, $t0
    daddiu $s3, $s3, 8
    bc read_bits
    nop
inflate_refill_unreached:
    nop
bits_ready:
    li $t0, 1
    sllv $t0, $t0, $a0
    daddiu $t0, $t0, -1
    and $v0, $s2, $t0
    srlv $s2, $s2, $a0
    dsubu $s3, $s3, $a0
inflate_bits_ready:
    nop
    jrc $ra
    nop
adler32:
    li $t0, 1
    move $t1, $zero
    li $t2, 65521
adler_byte:
    beqc $a0, $a1, adler_done
    nop
    lbu $t3, 0($a0)
    daddu $t0, $t0, $t3
    sltu $t3, $t0, $t2
    bnezc $t3, adler_second
    nop
    dsubu $t0, $t0, $t2
adler_second:
    daddu $t1, $t1, $t0
    sltu $t3, $t1, $t2
    bnezc $t3, adler_next
    nop
    dsubu $t1, $t1, $t2
adler_next:
    daddiu $a0, $a0, 1
    bc adler_byte
    nop
adler_done:
    dsll $v0, $t1, 16
    or $v0, $v0, $t0
inflate_adler_return_ready:
    nop
    jrc $ra
    nop
inflate_be32:
    move $v0, $zero
    li $t1, 4
inflate_be32_loop:
    lbu $t0, 0($a0)
    dsll $v0, $v0, 8
    or $v0, $v0, $t0
    daddiu $a0, $a0, 1
    daddiu $t1, $t1, -1
    bnezc $t1, inflate_be32_loop
    nop
inflate_be32_ready:
    nop
    jrc $ra
    nop

# Internal helpers use the owning inflater frame.
build_tree:
    move $t0, $a2
    daddiu $t1, $a2, 64
tree_clear:
    sw $zero, 0($t0)
    daddiu $t0, $t0, 4
    bnec $t0, $t1, tree_clear
    nop
    move $t0, $a0
    daddu $t1, $a0, $a1
    move $t8, $zero
    move $t9, $zero
huff_tree_cleared:
    nop
tree_count:
    lbu $t2, 0($t0)
    sltiu $t3, $t2, 16
    beqzc $t3, inflate_bad
    nop
    beqzc $t2, tree_zero_length
    nop
    daddiu $t8, $t8, 1
    sltu $t3, $t9, $t2
    beqzc $t3, tree_zero_length
    nop
    move $t9, $t2
tree_zero_length:
    sll $t2, $t2, 2
    daddu $t2, $t2, $a2
    lw $t3, 0($t2)
    daddiu $t3, $t3, 1
    sw $t3, 0($t2)
    daddiu $t0, $t0, 1
    bnec $t0, $t1, tree_count
    nop
huff_tree_counted:
    nop
    daddiu $t0, $a2, 4
    daddiu $t1, $a2, 64
    daddiu $t2, $fp, 1032
    daddiu $t2, $t2, 4
    move $t3, $zero
    li $t4, 1
tree_prefix:
    sw $t3, 0($t2)
    lw $t5, 0($t0)
    daddu $t3, $t3, $t5
    sll $t4, $t4, 1
    dsubu $t4, $t4, $t5
    bltzc $t4, inflate_bad         # too many codes for this bit length
    nop
    daddiu $t0, $t0, 4
    daddiu $t2, $t2, 4
    bnec $t0, $t1, tree_prefix
    nop
huff_tree_prefix_ready:
    nop
    beqzc $t4, tree_shape_ok
    nop
    beqzc $a3, inflate_bad
    nop
    li $t0, 2
    bnec $a3, $t0, tree_single
    nop
    beqzc $t8, tree_shape_ok
    nop
tree_single:
    li $t0, 1
    bnec $t8, $t0, inflate_bad
    nop
    bnec $t9, $t0, inflate_bad
    nop
tree_shape_ok:
huff_tree_shape_ready:
    nop
    daddiu $t0, $fp, 1032
    daddiu $t1, $a2, 64
    move $t2, $zero
tree_symbol:
    lbu $t3, 0($a0)
    beqzc $t3, tree_next
    nop
    sll $t3, $t3, 2
    daddu $t3, $t3, $t0
    lw $t4, 0($t3)
    daddiu $t5, $t4, 1
    sw $t5, 0($t3)
    sll $t4, $t4, 1
    daddu $t4, $t4, $t1
    sh $t2, 0($t4)
tree_next:
    daddiu $a0, $a0, 1
    daddiu $t2, $t2, 1
    bnec $t2, $a1, tree_symbol
    nop
huff_tree_symbols_ready:
    nop
    jrc $ra
    nop
decode_symbol:
    move $t0, $zero               # current code
    move $t1, $zero               # first code of current length
    move $t2, $zero               # index of that code in the symbol table
    daddiu $t3, $a0, 4
    daddiu $t4, $a0, 64
huff_symbol_state_ready:
    nop
decode_bit:
    bnezc $s3, decode_has_byte
    nop
    beqc $s0, $s1, inflate_bad
    nop
    lbu $s2, 0($s0)
    daddiu $s0, $s0, 1
    li $s3, 8
huff_symbol_refill_fallthrough:
    nop
decode_has_byte:
    andi $t5, $s2, 1
    srl $s2, $s2, 1
    daddiu $s3, $s3, -1
    or $t0, $t0, $t5
    lw $t5, 0($t3)
    dsubu $t6, $t0, $t1
    bltu $t6, $t5, decode_found
    nop
    daddu $t1, $t1, $t5
    sll $t1, $t1, 1
    sll $t0, $t0, 1
    daddu $t2, $t2, $t5
    daddiu $t3, $t3, 4
    bnec $t3, $t4, decode_bit
    nop
    bc inflate_bad
    nop
huff_symbol_step_unreached:
    nop
decode_found:
    daddu $t6, $t6, $t2
    sll $t6, $t6, 1
    daddu $t6, $t6, $t4
    lhu $v0, 0($t6)
huff_symbol_ready:
    nop
    jrc $ra
    nop

png_unfilter:
    move $t9, $zero
    bc unfilter_entry
    nop
png_unfilter_msa:
    li $t9, 1
unfilter_entry:
    li $v0, 1
    move $v1, $zero
    beqzc $a2, row_early
    nop
    beqzc $a3, row_early
    nop
    sltiu $t0, $a2, 513
    beqzc $t0, row_early
    nop
    sltiu $t0, $a3, 513
    beqzc $t0, row_early
    nop
    dsll $t0, $a2, 1
    daddu $t0, $t0, $a2
    daddiu $t1, $t0, 1
    dmul $t2, $t1, $a3
    bnec $a1, $t2, row_early
    nop
row_shape_ready:
    nop
    move $t3, $a0
    move $t4, $a3
unfilter_validate:
    lbu $t5, 0($t3)
    sltiu $t5, $t5, 5
    beqzc $t5, row_early
    nop
    daddu $t3, $t3, $t1
    daddiu $t4, $t4, -1
    bnezc $t4, unfilter_validate
    nop
row_selectors_ready:
    nop
    daddiu $sp, $sp, -1552
    sd $a1, 1536($sp)
    move $t3, $sp
    daddu $t4, $sp, $t0
unfilter_zero:
    sb $zero, 0($t3)
    daddiu $t3, $t3, 1
    bnec $t3, $t4, unfilter_zero
    nop
    move $v1, $t9
    move $a1, $a3
    move $a2, $t0
    move $a3, $sp
row_zero_ready:
    nop
filter_row:
    lbu $t0, 0($a0)
    daddiu $a0, $a0, 1
    move $t1, $a0                 # destination byte
    move $t2, $a3                 # corresponding byte in the previous row
    daddu $t3, $a0, $a2            # end of current row
row_cursor_ready:
    nop
    beqzc $t0, filter_row_done
    nop
    beqzc $v1, filter_byte
    nop
    li $t4, 2
    beqc $t0, $t4, filter_up_vector
    nop
filter_byte:
    lbu $t4, 0($t2)               # up
    move $t5, $zero               # left
    move $t6, $zero               # upper left
    dsubu $t7, $t1, $a0
    sltiu $t7, $t7, 3
    bnezc $t7, row_neighbors_ready
    nop
    lbu $t5, -3($t1)
    lbu $t6, -3($t2)
row_neighbors_ready:
    nop
filter_predict:
    li $t7, 1
    beqc $t0, $t7, filter_left
    nop
    li $t7, 2
    beqc $t0, $t7, filter_store
    nop
    li $t7, 3
    beqc $t0, $t7, filter_average
    nop
    dsubu $t7, $t4, $t6
    dsubu $t8, $t5, $t6
    daddu $t9, $t7, $t8
    abs $t7, $t7
    abs $t8, $t8
    abs $t9, $t9
    bgt $t7, $t8, filter_check_up
    nop
    ble $t7, $t9, row_paeth_left
    nop
filter_check_up:
    ble $t8, $t9, row_paeth_ready
    nop
    move $t4, $t6
    bc row_paeth_ready
    nop
row_paeth_left:
    move $t4, $t5
row_paeth_ready:
    nop
    bc filter_store
    nop
filter_left:
    move $t4, $t5
row_left_ready:
    nop
    bc filter_store
    nop
filter_average:
    daddu $t4, $t4, $t5
    srl $t4, $t4, 1
row_average_ready:
    nop
filter_store:
    lbu $t7, 0($t1)
    daddu $t7, $t7, $t4
    sb $t7, 0($t1)
    daddiu $t1, $t1, 1
    daddiu $t2, $t2, 1
row_store_ready:
    nop
    bnec $t1, $t3, filter_byte
    nop
    bc filter_row_done
    nop
filter_up_vector:
    dsubu $t4, $t3, $t1
    sltiu $t4, $t4, 16
row_vector_ready:
    nop
    bnezc $t4, filter_up_tail
    nop
    ld.b $w0, 0($t1)
    ld.b $w1, 0($t2)
    addv.b $w0, $w0, $w1
    st.b $w0, 0($t1)
    daddiu $t1, $t1, 16
    daddiu $t2, $t2, 16
row_vector_written:
    nop
    bc filter_up_vector
    nop
filter_up_tail:
    beqc $t1, $t3, row_up_tail_ready
    nop
    lbu $t4, 0($t1)
    lbu $t5, 0($t2)
    daddu $t4, $t4, $t5
    sb $t4, 0($t1)
    daddiu $t1, $t1, 1
    daddiu $t2, $t2, 1
    bc filter_up_tail
    nop
row_up_tail_ready:
    nop
filter_row_done:
    move $a3, $a0
    move $a0, $t3
    daddiu $a1, $a1, -1
row_advance_ready:
    nop
    bnezc $a1, filter_row
    nop
    ld $v1, 1536($sp)
    daddiu $sp, $sp, 1552
    move $v0, $zero
row_result_ready:
    nop
    jrc $ra
    nop
row_missing_frame:
    daddiu $sp, $sp, 1552
row_missing_early:
    li $v0, 4
    move $v1, $zero
row_early:
    jrc $ra
    nop

png_blit:
    move $t9, $zero
    bc blit_entry
    nop
png_blit_msa:
    li $t9, 1
blit_entry:
    li $v0, 1
    move $v1, $zero
    ld $t5, 0($a3)
    ld $t6, 8($a3)
    ld $t7, 16($a3)
    ld $t8, 24($a3)
    beqzc $t5, pack_early
    nop
    beqzc $t6, pack_early
    nop
    sltiu $t0, $t5, 513
    beqzc $t0, pack_early
    nop
    sltiu $t0, $t6, 513
    beqzc $t0, pack_early
    nop
    sltu $t0, $t7, $t5
    bnezc $t0, pack_early
    nop
    sltiu $t0, $t7, 513
    beqzc $t0, pack_early
    nop
    andi $t0, $a2, 3
    bnezc $t0, pack_early
    nop
pack_admitted:
    nop
    dsll $t0, $t5, 1
    daddu $t0, $t0, $t5
    daddiu $t0, $t0, 1
    dmul $t0, $t0, $t6
    bnec $a1, $t0, pack_early
    nop
    daddiu $t0, $t6, -1
    dmul $t0, $t0, $t7
    daddu $t0, $t0, $t5
    dsll $t0, $t0, 2
    sltu $t0, $t8, $t0
    bnezc $t0, pack_early
    nop
pack_windows_ready:
    nop
    move $v1, $t9
    dsll $t9, $t7, 2
    dmul $t8, $t5, $t6
    dsll $t8, $t8, 2
    move $a1, $a2
    move $a2, $t5
    move $a3, $t6
pack_state_ready:
    nop
    beqzc $v1, blit_row
    nop
pack_masks_ready:
    nop
    la $t0, rgb_shuffle
    ld.b $w4, 0($t0)
    ld.b $w5, 16($t0)
    ld.b $w6, 32($t0)
    ld.b $w7, 48($t0)
pack_masks_loaded:
    nop
blit_row:
    daddiu $a0, $a0, 1
    move $t0, $a1
    move $t1, $a2
pack_cursor_ready:
    nop
    beqzc $v1, blit_pixel
    nop
blit_vector:
    sltiu $t2, $t1, 16
pack_vector_ready:
    nop
    bnezc $t2, blit_tail
    nop
    ld.b $w0, 0($a0)
    ld.b $w1, 16($a0)
    ld.b $w2, 32($a0)
pack_inputs_loaded:
    nop
    move.v $w3, $w4
    vshf.b $w3, $w0, $w0
    st.w $w3, 0($t0)
pack_first_written:
    nop
    move.v $w3, $w5
    vshf.b $w3, $w1, $w0
    st.w $w3, 16($t0)
    move.v $w3, $w6
    vshf.b $w3, $w2, $w1
    st.w $w3, 32($t0)
pack_middle_written:
    nop
    move.v $w3, $w7
    vshf.b $w3, $w2, $w2
    st.w $w3, 48($t0)
pack_last_written:
    nop
    daddiu $a0, $a0, 48
    daddiu $t0, $t0, 64
    daddiu $t1, $t1, -16
pack_vector_advanced:
    nop
    bc blit_vector
    nop
blit_tail:
    beqzc $t1, blit_row_done
    nop
blit_pixel:
    lbu $t2, 0($a0)
    lbu $t3, 1($a0)
    lbu $t4, 2($a0)
    sll $t2, $t2, 16
    sll $t3, $t3, 8
    or $t2, $t2, $t3
    or $t2, $t2, $t4
    sw $t2, 0($t0)
    daddiu $a0, $a0, 3
    daddiu $t0, $t0, 4
    daddiu $t1, $t1, -1
pack_pixel_written:
    nop
    bnezc $t1, blit_pixel
    nop
blit_row_done:
    daddu $a1, $a1, $t9
    daddiu $a3, $a3, -1
pack_row_advanced:
    nop
    bnezc $a3, blit_row
    nop
    move $v1, $t8
    move $v0, $zero
pack_success:
    jrc $ra
    nop
pack_missing:
    li $v0, 4
    move $v1, $zero
pack_early:
    jrc $ra
    nop
