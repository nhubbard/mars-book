# Complete worked restricted baseline H.263 encoder for Chapters 26 and 27.
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


.data
.align 4
transform_B_h:
    .half 23170,23170,23170,23170,23170,23170,23170,23170
    .half 32138,27246,18205,6393,-6393,-18205,-27246,-32138
    .half 30274,12540,-12540,-30274,-30274,-12540,12540,30274
    .half 27246,-6393,-32138,-18205,18205,32138,6393,-27246
    .half 23170,-23170,-23170,23170,23170,-23170,-23170,23170
    .half 18205,-32138,6393,27246,-27246,-6393,32138,-18205
    .half 12540,-30274,30274,-12540,-12540,30274,-30274,12540
    .half 6393,-18205,27246,-32138,32138,-27246,18205,-6393
.align 4
transform_B_w:
    .word 23170,23170,23170,23170,23170,23170,23170,23170
    .word 32138,27246,18205,6393,-6393,-18205,-27246,-32138
    .word 30274,12540,-12540,-30274,-30274,-12540,12540,30274
    .word 27246,-6393,-32138,-18205,18205,32138,6393,-27246
    .word 23170,-23170,-23170,23170,23170,-23170,-23170,23170
    .word 18205,-32138,6393,27246,-27246,-6393,32138,-18205
    .word 12540,-30274,30274,-12540,-12540,30274,-30274,12540
    .word 6393,-18205,27246,-32138,32138,-27246,18205,-6393
.align 4
transform_BT_h:
    .half 23170,32138,30274,27246,23170,18205,12540,6393
    .half 23170,27246,12540,-6393,-23170,-32138,-30274,-18205
    .half 23170,18205,-12540,-32138,-23170,6393,30274,27246
    .half 23170,6393,-30274,-18205,23170,27246,-12540,-32138
    .half 23170,-6393,-30274,18205,23170,-27246,-12540,32138
    .half 23170,-18205,-12540,32138,-23170,-6393,30274,-27246
    .half 23170,-27246,12540,6393,-23170,32138,-30274,18205
    .half 23170,-32138,30274,-27246,23170,-18205,12540,-6393
.align 4
transform_BT_w:
    .word 23170,32138,30274,27246,23170,18205,12540,6393
    .word 23170,27246,12540,-6393,-23170,-32138,-30274,-18205
    .word 23170,18205,-12540,-32138,-23170,6393,30274,27246
    .word 23170,6393,-30274,-18205,23170,27246,-12540,-32138
    .word 23170,-6393,-30274,18205,23170,-27246,-12540,32138
    .word 23170,-18205,-12540,32138,-23170,-6393,30274,-27246
    .word 23170,-27246,12540,6393,-23170,32138,-30274,18205
    .word 23170,-32138,30274,-27246,23170,-18205,12540,-6393

.data
.align 3
syntax_mcbpc_codes:
    .dword 1,1
    .dword 1,3
    .dword 2,3
    .dword 3,3
    .dword 1,1
    .dword 3,4
    .dword 2,4
    .dword 5,6
    .dword 3,5
    .dword 4,8
    .dword 3,8
    .dword 3,7
syntax_cbpy_codes:
    .dword 3,4
    .dword 5,5
    .dword 4,5
    .dword 9,4
    .dword 3,5
    .dword 7,4
    .dword 2,6
    .dword 11,4
    .dword 2,5
    .dword 3,6
    .dword 5,4
    .dword 10,4
    .dword 4,4
    .dword 8,4
    .dword 6,4
    .dword 3,2
syntax_mvd_codes:
    .dword 5,13
    .dword 7,13
    .dword 5,12
    .dword 7,12
    .dword 9,12
    .dword 11,12
    .dword 13,12
    .dword 15,12
    .dword 9,11
    .dword 11,11
    .dword 13,11
    .dword 15,11
    .dword 17,11
    .dword 19,11
    .dword 21,11
    .dword 23,11
    .dword 25,11
    .dword 27,11
    .dword 29,11
    .dword 31,11
    .dword 33,11
    .dword 35,11
    .dword 19,10
    .dword 21,10
    .dword 23,10
    .dword 7,8
    .dword 9,8
    .dword 11,8
    .dword 7,7
    .dword 3,5
    .dword 3,4
    .dword 3,3
    .dword 1,1
    .dword 2,3
    .dword 2,4
    .dword 2,5
    .dword 6,7
    .dword 10,8
    .dword 8,8
    .dword 6,8
    .dword 22,10
    .dword 20,10
    .dword 18,10
    .dword 34,11
    .dword 32,11
    .dword 30,11
    .dword 28,11
    .dword 26,11
    .dword 24,11
    .dword 22,11
    .dword 20,11
    .dword 18,11
    .dword 16,11
    .dword 14,11
    .dword 12,11
    .dword 10,11
    .dword 8,11
    .dword 14,12
    .dword 12,12
    .dword 10,12
    .dword 8,12
    .dword 6,12
    .dword 4,12
    .dword 6,13

.data
.align 3
coeff_scan:
    .dword 0,1,8,16,9,2,3,10,17,24,32,25,18,11,4,5,12,19,26,33,40,48,41,34,27,20,13,6,7,14,21,28,35,42,49,56,57,50,43,36,29,22,15,23,30,37,44,51,58,59,52,45,38,31,39,46,53,60,61,54,47,55,62,63
.align 3
coeff_codes:
    .dword 0,0,1,4,3
    .dword 0,0,2,30,5
    .dword 0,0,3,42,7
    .dword 0,0,4,46,8
    .dword 0,0,5,62,9
    .dword 0,0,6,74,10
    .dword 0,0,7,72,10
    .dword 0,0,8,66,11
    .dword 0,0,9,64,11
    .dword 0,0,10,14,12
    .dword 0,0,11,12,12
    .dword 0,0,12,64,12
    .dword 0,1,1,12,4
    .dword 0,1,2,40,7
    .dword 0,1,3,60,9
    .dword 0,1,4,30,11
    .dword 0,1,5,66,12
    .dword 0,1,6,160,13
    .dword 0,2,1,28,5
    .dword 0,2,2,58,9
    .dword 0,2,3,28,11
    .dword 0,2,4,162,13
    .dword 0,3,1,26,6
    .dword 0,3,2,70,10
    .dword 0,3,3,26,11
    .dword 0,4,1,24,6
    .dword 0,4,2,68,10
    .dword 0,4,3,164,13
    .dword 0,5,1,22,6
    .dword 0,5,2,24,11
    .dword 0,5,3,166,13
    .dword 0,6,1,38,7
    .dword 0,6,2,22,11
    .dword 0,6,3,168,13
    .dword 0,7,1,36,7
    .dword 0,7,2,20,11
    .dword 0,8,1,34,7
    .dword 0,8,2,18,11
    .dword 0,9,1,32,7
    .dword 0,9,2,16,11
    .dword 0,10,1,44,8
    .dword 0,10,2,170,13
    .dword 0,11,1,42,8
    .dword 0,12,1,40,8
    .dword 0,13,1,56,9
    .dword 0,14,1,54,9
    .dword 0,15,1,66,10
    .dword 0,16,1,64,10
    .dword 0,17,1,62,10
    .dword 0,18,1,60,10
    .dword 0,19,1,58,10
    .dword 0,20,1,56,10
    .dword 0,21,1,54,10
    .dword 0,22,1,52,10
    .dword 0,23,1,68,12
    .dword 0,24,1,70,12
    .dword 0,25,1,172,13
    .dword 0,26,1,174,13
    .dword 1,0,1,14,5
    .dword 1,0,2,50,10
    .dword 1,0,3,10,12
    .dword 1,1,1,30,7
    .dword 1,1,2,8,12
    .dword 1,2,1,28,7
    .dword 1,3,1,26,7
    .dword 1,4,1,24,7
    .dword 1,5,1,38,8
    .dword 1,6,1,36,8
    .dword 1,7,1,34,8
    .dword 1,8,1,32,8
    .dword 1,9,1,52,9
    .dword 1,10,1,50,9
    .dword 1,11,1,48,9
    .dword 1,12,1,46,9
    .dword 1,13,1,44,9
    .dword 1,14,1,42,9
    .dword 1,15,1,40,9
    .dword 1,16,1,38,9
    .dword 1,17,1,48,10
    .dword 1,18,1,46,10
    .dword 1,19,1,44,10
    .dword 1,20,1,42,10
    .dword 1,21,1,40,10
    .dword 1,22,1,38,10
    .dword 1,23,1,36,10
    .dword 1,24,1,34,10
    .dword 1,25,1,14,11
    .dword 1,26,1,12,11
    .dword 1,27,1,10,11
    .dword 1,28,1,8,11
    .dword 1,29,1,72,12
    .dword 1,30,1,74,12
    .dword 1,31,1,76,12
    .dword 1,32,1,78,12
    .dword 1,33,1,176,13
    .dword 1,34,1,178,13
    .dword 1,35,1,180,13
    .dword 1,36,1,182,13
    .dword 1,37,1,184,13
    .dword 1,38,1,186,13
    .dword 1,39,1,188,13
    .dword 1,40,1,190,13


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
    balc h263_encode_clip
    nop
clip_caller_return:
    dla $t0, clip_status
    sd $v0, 0($t0)
    dla $t0, clip_accepted
    sd $v1, 0($t0)
    bnezc $v0, clip_caller_failed
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
    li $v0, 1
    move $v1, $zero
    or $t0, $a0, $a1
    andi $t0, $t0, 7
    bnezc $t0, clip_early_return
    nop
    daddiu $t0, $a0, 104
    sltu $t0, $t0, $a0
    bnezc $t0, clip_early_return
    nop
    daddiu $t0, $a1, 24
    sltu $t0, $t0, $a1
    bnezc $t0, clip_early_return
    nop
    daddiu $sp, $sp, -352
    sd $s0, 0($sp)
    sd $s1, 8($sp)
    sd $s2, 16($sp)
    sd $s3, 24($sp)
    sd $s4, 32($sp)
    sd $s5, 40($sp)
    sd $s6, 48($sp)
    sd $s7, 56($sp)
    sd $fp, 64($sp)
    sd $ra, 72($sp)
    move $s0, $a0
    move $s1, $a1
    ld $fp, 24($s0)
    beqzc $fp, clip_invalid
    nop
    sltiu $t0, $fp, 300
    beqzc $t0, clip_invalid
    nop
    ld $t1, 32($s0)
    beqzc $t1, clip_invalid
    nop
    sltiu $t0, $t1, 256
    beqzc $t0, clip_invalid
    nop
    dmul $t0, $fp, $t1
    li $t2, 1001
    dmul $t0, $t0, $t2
    li $t2, 300001
    sltu $t0, $t0, $t2
    beqzc $t0, clip_invalid
    nop
    sd $t1, 88($sp)
    ld $t1, 40($s0)
    beqzc $t1, clip_invalid
    nop
    sltiu $t0, $t1, 32
    beqzc $t0, clip_invalid
    nop
    sd $t1, 96($sp)
    ld $t1, 48($s0)
    sltiu $t0, $t1, 2
    beqzc $t0, clip_invalid
    nop
    sd $t1, 104($sp)
    ld $t0, 96($s0)
    bnezc $t0, clip_invalid
    nop
    ld $t1, 0($s0)
    li $t0, 4096
    daddu $t0, $t1, $t0
    sltu $t0, $t0, $t1
    bnezc $t0, clip_invalid
    nop
    sd $t1, 160($sp)
    ld $t1, 8($s0)
    li $t0, 4096
    daddu $t0, $t1, $t0
    sltu $t0, $t0, $t1
    bnezc $t0, clip_invalid
    nop
    sd $t1, 168($sp)
    ld $t1, 16($s0)
    li $t0, 4096
    daddu $t0, $t1, $t0
    sltu $t0, $t0, $t1
    bnezc $t0, clip_invalid
    nop
    sd $t1, 176($sp)
    ld $t1, 56($s0)
    li $t0, 38016
    daddu $t0, $t1, $t0
    sltu $t0, $t0, $t1
    bnezc $t0, clip_invalid
    nop
    sd $t1, 112($sp)
    ld $t1, 64($s0)
    li $t0, 38016
    daddu $t0, $t1, $t0
    sltu $t0, $t0, $t1
    bnezc $t0, clip_invalid
    nop
    sd $t1, 120($sp)
    ld $t1, 72($s0)
    li $t0, 38016
    daddu $t0, $t1, $t0
    sltu $t0, $t0, $t1
    bnezc $t0, clip_invalid
    nop
    sd $t1, 128($sp)
    ld $t1, 80($s0)
    li $t0, 169088
    daddu $t0, $t1, $t0
    sltu $t0, $t0, $t1
    bnezc $t0, clip_invalid
    nop
    andi $t0, $t1, 15
    bnezc $t0, clip_invalid
    nop
    sd $t1, 136($sp)
    ld $t1, 88($s0)
    li $t0, 131072
    daddu $t0, $t1, $t0
    sltu $t0, $t0, $t1
    bnezc $t0, clip_invalid
    nop
    sd $t1, 144($sp)
    ld $a0, 160($sp)
    balc clip_path_length
    nop
    bnezc $v0, clip_invalid
    nop
    ld $a0, 168($sp)
    balc clip_path_length
    nop
    bnezc $v0, clip_invalid
    nop
    ld $a0, 176($sp)
    balc clip_path_length
    nop
    bnezc $v0, clip_invalid
    nop
    ld $a0, 160($sp)
    ld $a1, 168($sp)
    balc clip_paths_equal
    nop
    bnezc $v0, clip_invalid
    nop
    ld $a0, 160($sp)
    ld $a1, 176($sp)
    balc clip_paths_equal
    nop
    bnezc $v0, clip_invalid
    nop
    ld $a0, 168($sp)
    ld $a1, 176($sp)
    balc clip_paths_equal
    nop
    bnezc $v0, clip_invalid
    nop
    sd $zero, 0($s1)
    sd $zero, 8($s1)
    sd $zero, 16($s1)
    sd $zero, 152($sp)
    sd $zero, 320($sp)
    li $s2, -1
    li $s3, -1
    li $s4, -1
    move $s5, $zero
    move $s6, $zero
    ld $s7, 120($sp)
    ld $a0, 160($sp)
    move $a1, $zero
    move $a2, $zero
    li $v0, 13
    syscall
    bltzc $v0, clip_open_failed
    nop
    move $s2, $v0
    ld $a0, 168($sp)
    li $a1, 1
    move $a2, $zero
    li $v0, 13
    syscall
    bltzc $v0, clip_open_failed
    nop
    move $s3, $v0
    ld $a0, 176($sp)
    li $a1, 1
    move $a2, $zero
    li $v0, 13
    syscall
    bltzc $v0, clip_open_failed
    nop
    move $s4, $v0
clip_frame:
    move $a0, $s2
    ld $a1, 112($sp)
    li $a2, 38016
    balc clip_read_exact
    nop
    bnezc $v0, clip_read_failed
    nop
    ld $t0, 112($sp)
    sd $t0, 224($sp)
    sd $s6, 232($sp)
    sd $s7, 240($sp)
    daddiu $t0, $sp, 192
    sd $t0, 248($sp)
    ld $t0, 144($sp)
    sd $t0, 192($sp)
    li $t0, 131072
    sd $t0, 200($sp)
    sd $zero, 208($sp)
    ld $t0, 136($sp)
    sd $t0, 256($sp)
    li $t0, 169088
    sd $t0, 264($sp)
    sltu $t0, $zero, $s5
    sd $t0, 272($sp)
    sd $t0, 304($sp)
    ld $t0, 88($sp)
    dmul $t0, $s5, $t0
    andi $t0, $t0, 255
    sd $t0, 280($sp)
    ld $t0, 96($sp)
    sd $t0, 288($sp)
    ld $t0, 104($sp)
    sd $t0, 296($sp)
    sd $zero, 312($sp)
    daddiu $a0, $sp, 224
    balc h263_encode_picture
    nop
    bnezc $v0, clip_picture_failed
    nop
    beqzc $v1, clip_picture_failed
    nop
    li $t0, 105071
    sltu $t0, $v1, $t0
    beqzc $t0, clip_picture_failed
    nop
    sd $v1, 184($sp)
    move $a0, $s3
    ld $a1, 144($sp)
    move $a2, $v1
    balc clip_write_all
    nop
    bnezc $v0, clip_write_failed
    nop
    move $a0, $s4
    move $a1, $s7
    li $a2, 38016
    balc clip_write_all
    nop
    bnezc $v0, clip_write_failed
    nop
    move $s6, $s7
    ld $t0, 120($sp)
    bnec $s7, $t0, clip_next_is_a
    nop
    ld $s7, 128($sp)
    bc clip_next_ready
    nop
clip_next_is_a:
    move $s7, $t0
clip_next_ready:
    daddiu $s5, $s5, 1
    ld $t0, 152($sp)
    ld $t1, 184($sp)
    daddu $t0, $t0, $t1
    sd $t0, 152($sp)
    sd $s5, 0($s1)
    sd $t0, 8($s1)
    sd $s6, 16($s1)
    bnec $s5, $fp, clip_frame
    nop
    move $a0, $s2
    daddiu $a1, $sp, 328
    li $a2, 1
    li $v0, 14
    syscall
    bnezc $v0, clip_read_failed
    nop
    bc clip_close_owned
    nop
clip_open_failed:
    li $t0, 2
    bc clip_failure
    nop
clip_read_failed:
    li $t0, 3
    bc clip_failure
    nop
clip_picture_failed:
    li $t0, 4
    bc clip_failure
    nop
clip_write_failed:
    li $t0, 5
clip_failure:
    sd $t0, 320($sp)
clip_close_owned:
    bltzc $s4, clip_close_stream
    nop
    move $a0, $s4
    li $v0, 16
    syscall
clip_close_stream:
    bltzc $s3, clip_close_input
    nop
    move $a0, $s3
    li $v0, 16
    syscall
clip_close_input:
    bltzc $s2, clip_closed
    nop
    move $a0, $s2
    li $v0, 16
    syscall
clip_closed:
    ld $v0, 320($sp)
    move $v1, $s5
    bc clip_restore
    nop
clip_invalid:
    li $v0, 1
    move $v1, $zero
clip_restore:
    ld $s0, 0($sp)
    ld $s1, 8($sp)
    ld $s2, 16($sp)
    ld $s3, 24($sp)
    ld $s4, 32($sp)
    ld $s5, 40($sp)
    ld $s6, 48($sp)
    ld $s7, 56($sp)
    ld $fp, 64($sp)
    ld $ra, 72($sp)
    daddiu $sp, $sp, 352
clip_early_return:
    jrc $ra
    nop
clip_path_length:
    move $t0, $zero
clip_path_scan:
    daddu $t1, $a0, $t0
    lbu $t1, 0($t1)
    beqzc $t1, clip_path_end
    nop
    daddiu $t0, $t0, 1
    sltiu $t1, $t0, 4096
    bnezc $t1, clip_path_scan
    nop
    li $v0, 1
    jrc $ra
    nop
clip_path_end:
    sltiu $v0, $t0, 1
    move $v1, $t0
    jrc $ra
    nop
clip_paths_equal:
    lbu $t0, 0($a0)
    lbu $t1, 0($a1)
    bnec $t0, $t1, clip_path_different
    nop
    beqzc $t0, clip_path_same
    nop
    daddiu $a0, $a0, 1
    daddiu $a1, $a1, 1
    bc clip_paths_equal
    nop
clip_path_same:
    li $v0, 1
    jrc $ra
    nop
clip_path_different:
    move $v0, $zero
    jrc $ra
    nop
clip_read_exact:
    daddiu $sp, $sp, -64
    sd $ra, 0($sp)
    sd $s0, 8($sp)
    sd $s1, 16($sp)
    sd $s2, 24($sp)
    sd $s3, 32($sp)
    move $s0, $a0
    move $s1, $a1
    move $s2, $a2
    move $s3, $zero
clip_read_loop:
    beqzc $s2, clip_read_complete
    nop
    move $a0, $s0
    move $a1, $s1
    li $a2, 4096
    sltu $t0, $s2, $a2
    beqzc $t0, clip_read_bounded
    nop
    move $a2, $s2
clip_read_bounded:
    li $v0, 14
    syscall
    blezc $v0, clip_read_error
    nop
    sltu $t0, $a2, $v0
    bnezc $t0, clip_read_error
    nop
    daddu $s1, $s1, $v0
    dsubu $s2, $s2, $v0
    daddu $s3, $s3, $v0
    bc clip_read_loop
    nop
clip_read_complete:
    move $v0, $zero
    bc clip_read_restore
    nop
clip_read_error:
    li $v0, 1
clip_read_restore:
    move $v1, $s3
    ld $ra, 0($sp)
    ld $s0, 8($sp)
    ld $s1, 16($sp)
    ld $s2, 24($sp)
    ld $s3, 32($sp)
    daddiu $sp, $sp, 64
    jrc $ra
    nop
clip_write_all:
    daddiu $sp, $sp, -4160
    sd $ra, 0($sp)
    sd $s0, 8($sp)
    sd $s1, 16($sp)
    sd $s2, 24($sp)
    sd $s3, 32($sp)
    sd $s4, 40($sp)
    move $s0, $a0
    move $s1, $a1
    move $s2, $a2
    move $s3, $zero
clip_write_loop:
    beqzc $s2, clip_write_complete
    nop
    li $s4, 4096
    sltu $t0, $s2, $s4
    beqzc $t0, clip_write_bounded
    nop
    move $s4, $s2
clip_write_bounded:
    move $t0, $s1
    daddiu $t1, $sp, 48
    move $t2, $s4
clip_write_stage:
    lbu $t3, 0($t0)
    sb $t3, 0($t1)
    daddiu $t0, $t0, 1
    daddiu $t1, $t1, 1
    daddiu $t2, $t2, -1
    bnezc $t2, clip_write_stage
    nop
    sb $zero, 0($t1)
    move $a0, $s0
    daddiu $a1, $sp, 48
    move $a2, $s4
    li $v0, 15
    syscall
    blezc $v0, clip_write_error
    nop
    sltu $t0, $s4, $v0
    bnezc $t0, clip_write_error
    nop
    daddu $s1, $s1, $v0
    dsubu $s2, $s2, $v0
    daddu $s3, $s3, $v0
    bc clip_write_loop
    nop
clip_write_complete:
    move $v0, $zero
    bc clip_write_restore
    nop
clip_write_error:
    li $v0, 1
clip_write_restore:
    move $v1, $s3
    ld $ra, 0($sp)
    ld $s0, 8($sp)
    ld $s1, 16($sp)
    ld $s2, 24($sp)
    ld $s3, 32($sp)
    ld $s4, 40($sp)
    daddiu $sp, $sp, 4160
    jrc $ra
    nop


h263_encode_picture:
    li $v0, 1
    move $v1, $zero
    andi $t0, $a0, 7
    bnezc $t0, pic_early_return
    nop
    daddiu $t0, $a0, 96
    sltu $t0, $t0, $a0
    bnezc $t0, pic_early_return
    nop
    daddiu $sp, $sp, -4096
    sd $s0, 0($sp)
    sd $s1, 8($sp)
    sd $s2, 16($sp)
    sd $s3, 24($sp)
    sd $s4, 32($sp)
    sd $s5, 40($sp)
    sd $s6, 48($sp)
    sd $s7, 56($sp)
    sd $fp, 64($sp)
    sd $ra, 72($sp)
    ld $s0, 0($a0)
    ld $s1, 8($a0)
    ld $s2, 16($a0)
    ld $s3, 24($a0)
    ld $s4, 32($a0)
    ld $t1, 40($a0)
    ld $s5, 48($a0)
    ld $t2, 56($a0)
    ld $t3, 64($a0)
    ld $t4, 72($a0)
    ld $t5, 80($a0)
    ld $t6, 88($a0)
    bnezc $t6, pic_invalid
    nop
    sltiu $t0, $s5, 2
    beqzc $t0, pic_invalid
    nop
    sltiu $t0, $t2, 256
    beqzc $t0, pic_invalid
    nop
    beqzc $t3, pic_invalid
    nop
    sltiu $t0, $t3, 32
    beqzc $t0, pic_invalid
    nop
    sltiu $t0, $t4, 2
    beqzc $t0, pic_invalid
    nop
    sltiu $t0, $t5, 4
    beqzc $t0, pic_invalid
    nop
    li $t0, 169088
    sltu $t0, $t1, $t0
    bnezc $t0, pic_invalid
    nop
    andi $t0, $s3, 7
    bnezc $t0, pic_invalid
    nop
    andi $t0, $s4, 15
    bnezc $t0, pic_invalid
    nop
    sd $t2, 128($sp)
    sd $t3, 136($sp)
    sd $t4, 144($sp)
    sd $t5, 152($sp)
    li $t1, 38016
    daddu $t0, $s0, $t1
    sltu $t0, $t0, $s0
    bnezc $t0, pic_invalid
    nop
    daddu $t0, $s2, $t1
    sltu $t0, $t0, $s2
    bnezc $t0, pic_invalid
    nop
    bnezc $s5, pic_reference_extent
    nop
    bnezc $s1, pic_invalid
    nop
    bc pic_context_extent
    nop
pic_reference_extent:
    daddu $t0, $s1, $t1
    sltu $t0, $t0, $s1
    bnezc $t0, pic_invalid
    nop
pic_context_extent:
    daddiu $t0, $s3, 24
    sltu $t0, $t0, $s3
    bnezc $t0, pic_invalid
    nop
    li $t1, 169088
    daddu $t0, $s4, $t1
    sltu $t0, $t0, $s4
    bnezc $t0, pic_invalid
    nop
    ld $t1, 0($s3)
    ld $t2, 8($s3)
    ld $t3, 16($s3)
    bnezc $t3, pic_invalid
    nop
    li $t0, 1048577
    sltu $t0, $t2, $t0
    beqzc $t0, pic_invalid
    nop
    daddu $t0, $t1, $t2
    sltu $t0, $t0, $t1
    bnezc $t0, pic_invalid
    nop
    sd $s4, 208($sp)
    li $t0, 131072
    sd $t0, 216($sp)
    sd $zero, 224($sp)
    li $a1, 32
    li $a2, 22
    daddiu $a0, $sp, 208
    balc h263_write_bits
    nop
    bnezc $v0, pic_child_failed
    nop
    ld $a1, 128($sp)
    li $a2, 8
    daddiu $a0, $sp, 208
    balc h263_write_bits
    nop
    bnezc $v0, pic_child_failed
    nop
    sll $a1, $s5, 4
    ori $a1, $a1, 4160
    li $a2, 13
    daddiu $a0, $sp, 208
    balc h263_write_bits
    nop
    bnezc $v0, pic_child_failed
    nop
    ld $a1, 136($sp)
    li $a2, 5
    daddiu $a0, $sp, 208
    balc h263_write_bits
    nop
    bnezc $v0, pic_child_failed
    nop
    move $a1, $zero
    li $a2, 2
    daddiu $a0, $sp, 208
    balc h263_write_bits
    nop
    bnezc $v0, pic_child_failed
    nop
    move $s6, $zero
pic_row:
    sd $zero, 192($sp)
    sd $zero, 200($sp)
    beqzc $s6, pic_row_header_done
    nop
    ld $a2, 224($sp)
    dsubu $a2, $zero, $a2
    andi $a2, $a2, 7
    move $a1, $zero
    daddiu $a0, $sp, 208
    balc h263_write_bits
    nop
    bnezc $v0, pic_child_failed
    nop
    li $a1, 1
    li $a2, 17
    daddiu $a0, $sp, 208
    balc h263_write_bits
    nop
    bnezc $v0, pic_child_failed
    nop
    move $a1, $s6
    li $a2, 5
    daddiu $a0, $sp, 208
    balc h263_write_bits
    nop
    bnezc $v0, pic_child_failed
    nop
    ld $a1, 152($sp)
    li $a2, 2
    daddiu $a0, $sp, 208
    balc h263_write_bits
    nop
    bnezc $v0, pic_child_failed
    nop
    ld $a1, 136($sp)
    li $a2, 5
    daddiu $a0, $sp, 208
    balc h263_write_bits
    nop
    bnezc $v0, pic_child_failed
    nop
pic_row_header_done:
    move $s7, $zero
pic_macroblock:
    sd $zero, 176($sp)
    sd $zero, 184($sp)
    beqzc $s5, pic_motion_done
    nop
    li $t0, 2816
    dmul $t0, $s6, $t0
    dsll $t1, $s7, 4
    daddu $t0, $t0, $t1
    daddu $t0, $s0, $t0
    sd $t0, 240($sp)
    sd $s1, 248($sp)
    li $t0, 176
    sd $t0, 256($sp)
    sd $t0, 264($sp)
    dsll $t0, $s7, 4
    sd $t0, 272($sp)
    dsll $t0, $s6, 4
    sd $t0, 280($sp)
    li $t0, 2
    sd $t0, 288($sp)
    ld $t0, 144($sp)
    sd $t0, 296($sp)
    daddiu $a0, $sp, 240
    daddiu $a1, $sp, 304
    balc h263_search16
    nop
    bnezc $v0, pic_child_failed
    nop
    ld $t0, 304($sp)
    ld $t1, 312($sp)
    sd $t0, 176($sp)
    sd $t1, 184($sp)
    sd $s0, 336($sp)
    daddiu $t0, $s0, 25344
    sd $t0, 344($sp)
    daddiu $t0, $s0, 31680
    sd $t0, 352($sp)
    sd $s1, 360($sp)
    daddiu $t0, $s1, 25344
    sd $t0, 368($sp)
    daddiu $t0, $s1, 31680
    sd $t0, 376($sp)
    li $t0, 176
    sd $t0, 384($sp)
    sd $t0, 408($sp)
    li $t0, 88
    sd $t0, 392($sp)
    sd $t0, 400($sp)
    sd $t0, 416($sp)
    sd $t0, 424($sp)
    dsll $t0, $s7, 4
    sd $t0, 432($sp)
    dsll $t0, $s6, 4
    sd $t0, 440($sp)
    ld $t0, 176($sp)
    sd $t0, 448($sp)
    ld $t0, 184($sp)
    sd $t0, 456($sp)
    ld $t0, 144($sp)
    sd $t0, 464($sp)
    sd $zero, 472($sp)
    daddiu $a0, $sp, 336
    daddiu $a1, $sp, 480
    balc h263_predict_mb
    nop
    bnezc $v0, pic_child_failed
    nop
pic_motion_done:
    move $fp, $zero
pic_block:
    sltiu $t0, $fp, 4
    beqzc $t0, pic_chroma_origin
    nop
    li $t0, 2816
    dmul $t0, $s6, $t0
    dsll $t1, $s7, 4
    daddu $t0, $t0, $t1
    andi $t1, $fp, 1
    dsll $t1, $t1, 3
    daddu $t0, $t0, $t1
    dsrl $t1, $fp, 1
    li $t2, 1408
    dmul $t1, $t1, $t2
    daddu $t0, $t0, $t1
    li $t1, 176
    bc pic_block_origin_done
    nop
pic_chroma_origin:
    daddiu $t0, $fp, -4
    li $t1, 6336
    dmul $t0, $t0, $t1
    daddiu $t0, $t0, 25344
    li $t1, 704
    dmul $t1, $s6, $t1
    daddu $t0, $t0, $t1
    dsll $t1, $s7, 3
    daddu $t0, $t0, $t1
    li $t1, 88
pic_block_origin_done:
    daddu $t2, $s0, $t0
    sd $t2, 1632($sp)
    sd $t1, 1640($sp)
    li $t2, 131072
    daddu $t2, $s4, $t2
    daddu $t2, $t2, $t0
    sd $t2, 2992($sp)
    sd $t1, 3000($sp)
    sd $zero, 1648($sp)
    sd $zero, 1656($sp)
    li $t0, 1
    dsubu $t0, $t0, $s5
    sd $t0, 1672($sp)
    beqzc $s5, pic_block_request_done
    nop
    li $t0, 192
    dmul $t0, $fp, $t0
    daddiu $t1, $sp, 480
    daddu $t0, $t0, $t1
    sd $t0, 1648($sp)
    li $t0, 8
    sd $t0, 1656($sp)
pic_block_request_done:
    ld $t0, 136($sp)
    sd $t0, 1664($sp)
    ld $t0, 144($sp)
    sd $t0, 1680($sp)
    sd $zero, 1688($sp)
    daddiu $a0, $sp, 1632
    daddiu $a1, $sp, 1696
    balc h263_encode_block8
    nop
    bnezc $v0, pic_child_failed
    nop
    daddiu $t0, $sp, 1696
    dsll $t1, $fp, 7
    daddiu $t2, $sp, 2144
    daddu $t1, $t1, $t2
    li $t2, 16
pic_copy_levels:
    ld $t3, 0($t0)
    sd $t3, 0($t1)
    daddiu $t0, $t0, 8
    daddiu $t1, $t1, 8
    daddiu $t2, $t2, -1
    bnezc $t2, pic_copy_levels
    nop
    daddiu $t0, $sp, 2080
    ld $t1, 2992($sp)
    ld $t2, 3000($sp)
    li $t3, 8
pic_copy_pixels_row:
    li $t4, 8
    move $t5, $t1
pic_copy_pixels_sample:
    lbu $t6, 0($t0)
    sb $t6, 0($t5)
    daddiu $t0, $t0, 1
    daddiu $t5, $t5, 1
    daddiu $t4, $t4, -1
    bnezc $t4, pic_copy_pixels_sample
    nop
    daddu $t1, $t1, $t2
    daddiu $t3, $t3, -1
    bnezc $t3, pic_copy_pixels_row
    nop
    daddiu $fp, $fp, 1
    sltiu $t0, $fp, 6
    bnezc $t0, pic_block
    nop
    daddiu $t0, $sp, 2144
    sd $t0, 2912($sp)
    daddiu $t0, $sp, 208
    sd $t0, 2920($sp)
    sd $s5, 2928($sp)
    li $t0, 1
    dsubu $t0, $t0, $s5
    sd $t0, 2936($sp)
    ld $t0, 176($sp)
    dsll $t0, $t0, 1
    sd $t0, 2944($sp)
    ld $t0, 184($sp)
    dsll $t0, $t0, 1
    sd $t0, 2952($sp)
    ld $t0, 192($sp)
    sd $t0, 2960($sp)
    ld $t0, 200($sp)
    sd $t0, 2968($sp)
    li $t0, 1
    sd $t0, 2976($sp)
    sd $zero, 2984($sp)
    daddiu $a0, $sp, 2912
    balc h263_emit_macroblock
    nop
    bnezc $v0, pic_child_failed
    nop
    ld $t0, 2944($sp)
    sd $t0, 192($sp)
    ld $t0, 2952($sp)
    sd $t0, 200($sp)
    daddiu $s7, $s7, 1
    sltiu $t0, $s7, 11
    bnezc $t0, pic_macroblock
    nop
    daddiu $s6, $s6, 1
    sltiu $t0, $s6, 9
    bnezc $t0, pic_row
    nop
    ld $a2, 224($sp)
    dsubu $a2, $zero, $a2
    andi $a2, $a2, 7
    move $a1, $zero
    daddiu $a0, $sp, 208
    balc h263_write_bits
    nop
    bnezc $v0, pic_child_failed
    nop
    ld $t0, 224($sp)
    sd $t0, 3024($sp)
    dsrl $t1, $t0, 3
    sd $t1, 3032($sp)
    ld $t2, 8($s3)
    sltu $t2, $t2, $t1
    bnezc $t2, pic_capacity_failed
    nop
    ld $t2, 0($s3)
    move $t3, $s4
pic_publish_bytes:
    lbu $t4, 0($t3)
    sb $t4, 0($t2)
    daddiu $t3, $t3, 1
    daddiu $t2, $t2, 1
    daddiu $t1, $t1, -1
    bnezc $t1, pic_publish_bytes
    nop
    li $t1, 131072
    daddu $t1, $s4, $t1
    move $t2, $s2
    li $t3, 38016
pic_publish_reconstruction:
    lbu $t4, 0($t1)
    sb $t4, 0($t2)
    daddiu $t1, $t1, 1
    daddiu $t2, $t2, 1
    daddiu $t3, $t3, -1
    bnezc $t3, pic_publish_reconstruction
    nop
    ld $t0, 3024($sp)
    sd $t0, 16($s3)
    move $v0, $zero
    ld $v1, 3032($sp)
    bc pic_restore
    nop
pic_invalid:
    li $v0, 1
    bc pic_failed
    nop
pic_capacity_failed:
    li $v0, 2
    bc pic_failed
    nop
pic_child_failed:
    li $v0, 3
pic_failed:
    move $v1, $zero
pic_restore:
    ld $s0, 0($sp)
    ld $s1, 8($sp)
    ld $s2, 16($sp)
    ld $s3, 24($sp)
    ld $s4, 32($sp)
    ld $s5, 40($sp)
    ld $s6, 48($sp)
    ld $s7, 56($sp)
    ld $fp, 64($sp)
    ld $ra, 72($sp)
    daddiu $sp, $sp, 4096
pic_early_return:
    jrc $ra
    nop

h263_search16:
    li $v0, 1
    move $v1, $zero
    andi $t0, $a0, 7
    bnezc $t0, motion_early_return
    nop
    andi $t0, $a1, 7
    bnezc $t0, motion_early_return
    nop
    daddiu $t0, $a0, 64
    sltu $t0, $t0, $a0
    bnezc $t0, motion_early_return
    nop
    daddiu $t0, $a1, 24
    sltu $t0, $t0, $a1
    bnezc $t0, motion_early_return
    nop
    daddiu $sp, $sp, -192
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
    move $fp, $a1
    ld $s0, 0($a0)
    ld $s1, 8($a0)
    ld $s2, 16($a0)
    ld $s3, 24($a0)
    ld $s4, 32($a0)
    ld $s5, 40($a0)
    ld $s6, 48($a0)
    ld $s7, 56($a0)
    sltiu $t0, $s2, 16
    bnezc $t0, motion_return
    nop
    sltiu $t0, $s2, 4097
    beqzc $t0, motion_return
    nop
    sltiu $t0, $s3, 176
    bnezc $t0, motion_return
    nop
    sltiu $t0, $s3, 4097
    beqzc $t0, motion_return
    nop
    sltiu $t0, $s4, 161
    beqzc $t0, motion_return
    nop
    sltiu $t0, $s5, 129
    beqzc $t0, motion_return
    nop
    sltiu $t0, $s6, 3
    beqzc $t0, motion_return
    nop
    sltiu $t0, $s7, 2
    beqzc $t0, motion_return
    nop
    dsll $t0, $s2, 4
    dsubu $t0, $t0, $s2
    daddiu $t0, $t0, 16
    daddu $t0, $t0, $s0
    sltu $t0, $t0, $s0
    bnezc $t0, motion_return
    nop
    li $t0, 143
    dmul $t0, $t0, $s3
    daddiu $t0, $t0, 176
    daddu $t0, $t0, $s1
    sltu $t0, $t0, $s1
    bnezc $t0, motion_return
    nop
    dsubu $t0, $zero, $s6
    sltu $t1, $s4, $s6
    beqzc $t1, motion_lo_x_ready
    nop
    dsubu $t0, $zero, $s4
motion_lo_x_ready:
    sd $t0, 128($sp)
    move $t0, $s6
    li $t1, 160
    dsubu $t1, $t1, $s4
    sltu $t2, $t1, $s6
    beqzc $t2, motion_hi_x_ready
    nop
    move $t0, $t1
motion_hi_x_ready:
    sd $t0, 136($sp)
    dsubu $t0, $zero, $s6
    sltu $t1, $s5, $s6
    beqzc $t1, motion_lo_y_ready
    nop
    dsubu $t0, $zero, $s5
motion_lo_y_ready:
    sd $t0, 144($sp)
    move $t0, $s6
    li $t1, 128
    dsubu $t1, $t1, $s5
    sltu $t2, $t1, $s6
    beqzc $t2, motion_hi_y_ready
    nop
    move $t0, $t1
motion_hi_y_ready:
    sd $t0, 152($sp)
    sd $zero, 80($sp)
    sd $zero, 88($sp)
    sd $zero, 96($sp)
    sd $zero, 104($sp)
    li $t0, 65536
    sd $t0, 112($sp)
    sd $zero, 120($sp)
    sd $zero, 160($sp)
motion_candidate:
    move $a0, $s0
    ld $t0, 80($sp)
    daddu $t0, $t0, $s4
    ld $t1, 88($sp)
    daddu $t1, $t1, $s5
    dmul $t1, $t1, $s3
    daddu $a1, $s1, $t1
    daddu $a1, $a1, $t0
    move $a2, $s2
    move $a3, $s3
    beqzc $s7, motion_scalar
    nop
    balc h263_sad16_msa
    nop
    bc motion_scored
    nop
motion_scalar:
    balc h263_sad16
    nop
motion_scored:
    bnezc $v0, motion_failed
    nop
    ld $t0, 120($sp)
    daddiu $t0, $t0, 1
    sd $t0, 120($sp)
    ld $t0, 112($sp)
    sltu $t1, $v1, $t0
    beqzc $t1, motion_keep
    nop
motion_update:
    sd $v1, 112($sp)
    ld $t0, 80($sp)
    sd $t0, 96($sp)
    ld $t0, 88($sp)
    sd $t0, 104($sp)
motion_keep:
    ld $t0, 160($sp)
    bnezc $t0, motion_advance
    nop
    li $t0, 1
    sd $t0, 160($sp)
    ld $t0, 128($sp)
    sd $t0, 80($sp)
    ld $t0, 144($sp)
    sd $t0, 88($sp)
    bc motion_rect_candidate
    nop
motion_advance:
    ld $t0, 80($sp)
    ld $t1, 136($sp)
    beqc $t0, $t1, motion_next_row
    nop
    daddiu $t0, $t0, 1
    sd $t0, 80($sp)
    bc motion_rect_candidate
    nop
motion_next_row:
    ld $t0, 88($sp)
    ld $t1, 152($sp)
    beqc $t0, $t1, motion_complete
    nop
    daddiu $t0, $t0, 1
    sd $t0, 88($sp)
    ld $t0, 128($sp)
    sd $t0, 80($sp)
motion_rect_candidate:
    ld $t0, 80($sp)
    ld $t1, 88($sp)
    or $t0, $t0, $t1
    beqzc $t0, motion_advance
    nop
    bc motion_candidate
    nop
motion_complete:
    ld $t0, 96($sp)
    sd $t0, 0($fp)
    ld $t0, 104($sp)
    sd $t0, 8($fp)
    ld $t0, 112($sp)
    sd $t0, 16($fp)
    move $v0, $zero
    ld $v1, 120($sp)
    bc motion_return
    nop
motion_failed:
    li $v0, 1
    move $v1, $zero
motion_return:
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
    daddiu $sp, $sp, 192
motion_early_return:
    jrc $ra
    nop

h263_sad16:
    move $t9, $zero
    bc sad_entry
    nop
h263_sad16_msa:
    li $t9, 1
sad_entry:
    li $v0, 1
    move $v1, $zero
    sltiu $t0, $a2, 16
    bnezc $t0, sad_return
    nop
    sltiu $t0, $a2, 4097
    beqzc $t0, sad_return
    nop
    sltiu $t0, $a3, 16
    bnezc $t0, sad_return
    nop
    sltiu $t0, $a3, 4097
    beqzc $t0, sad_return
    nop
    dsll $t0, $a2, 4
    dsubu $t0, $t0, $a2
    daddiu $t0, $t0, 16
    daddu $t1, $a0, $t0
    sltu $t1, $t1, $a0
    bnezc $t1, sad_return
    nop
    dsll $t0, $a3, 4
    dsubu $t0, $t0, $a3
    daddiu $t0, $t0, 16
    daddu $t1, $a1, $t0
    sltu $t1, $t1, $a1
    bnezc $t1, sad_return
    nop
sad_admitted:
    nop
    move $t0, $a0
    move $t1, $a1
    li $t2, 16
    move $t8, $zero
    bnezc $t9, sad_vector
    nop
sad_scalar_row:
    move $t4, $t0
    move $t5, $t1
    li $t3, 16
sad_scalar_byte:
    lbu $t6, 0($t4)
    lbu $t7, 0($t5)
    subu $t6, $t6, $t7
    abs $t6, $t6
    daddu $t8, $t8, $t6
    daddiu $t4, $t4, 1
    daddiu $t5, $t5, 1
    daddiu $t3, $t3, -1
    bnezc $t3, sad_scalar_byte
    nop
    daddu $t0, $t0, $a2
    daddu $t1, $t1, $a3
    daddiu $t2, $t2, -1
    bnezc $t2, sad_scalar_row
    nop
    move $v1, $t8
    bc sad_success
    nop
sad_vector:
    ldi.b $w4, 0
sad_vector_row:
    ld.b $w0, 0($t0)
    ld.b $w1, 0($t1)
    asub_u.b $w2, $w0, $w1
    hadd_u.h $w3, $w2, $w2
    addv.h $w4, $w4, $w3
    daddu $t0, $t0, $a2
    daddu $t1, $t1, $a3
    daddiu $t2, $t2, -1
    bnezc $t2, sad_vector_row
    nop
sad_vector_accumulated:
    nop
    hadd_u.w $w5, $w4, $w4
    hadd_u.d $w6, $w5, $w5
    copy_s.d $t6, $w6[0]
    copy_s.d $t7, $w6[1]
    daddu $v1, $t6, $t7
sad_success:
    move $v0, $zero
sad_result_ready:
    nop
sad_return:
    jrc $ra
    nop

h263_predict_mb:
    li $v0, 1
    move $v1, $zero
    andi $t0, $a0, 7
    bnezc $t0, mb_early_return
    nop
    andi $t0, $a1, 15
    bnezc $t0, mb_early_return
    nop
    daddiu $t0, $a0, 144
    sltu $t0, $t0, $a0
    bnezc $t0, mb_early_return
    nop
    daddiu $t0, $a1, 1152
    sltu $t0, $t0, $a1
    bnezc $t0, mb_early_return
    nop
    daddiu $sp, $sp, -1440
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
    move $s1, $a1
    daddiu $s0, $sp, 80
    move $t0, $a0
    move $t1, $s0
    li $t2, 18
mb_copy_request:
    ld $t3, 0($t0)
    sd $t3, 0($t1)
    daddiu $t0, $t0, 8
    daddiu $t1, $t1, 8
    daddiu $t2, $t2, -1
    bnezc $t2, mb_copy_request
    nop
    ld $s4, 96($s0)
    ld $s5, 104($s0)
    ld $s6, 112($s0)
    ld $s7, 120($s0)
    ld $fp, 128($s0)
    ld $t0, 136($s0)
    bnezc $t0, mb_restore
    nop
    sltiu $t0, $s4, 161
    beqzc $t0, mb_restore
    nop
    sltiu $t0, $s5, 129
    beqzc $t0, mb_restore
    nop
    or $t0, $s4, $s5
    andi $t0, $t0, 15
    bnezc $t0, mb_restore
    nop
    daddiu $t0, $s6, 2
    sltiu $t0, $t0, 5
    beqzc $t0, mb_restore
    nop
    daddiu $t0, $s7, 2
    sltiu $t0, $t0, 5
    beqzc $t0, mb_restore
    nop
    daddu $t0, $s4, $s6
    sltiu $t0, $t0, 161
    beqzc $t0, mb_restore
    nop
    daddu $t0, $s5, $s7
    sltiu $t0, $t0, 129
    beqzc $t0, mb_restore
    nop
    sltiu $t0, $fp, 2
    beqzc $t0, mb_restore
    nop
    move $t0, $zero
mb_validate_plane:
    dsll $t1, $t0, 3
    daddu $t2, $s0, $t1
    ld $t3, 0($t2)
    ld $t4, 48($t2)
    li $t5, 88
    li $t6, 71
    beqzc $t0, mb_luma_plane
    nop
    li $t7, 3
    bnec $t0, $t7, mb_plane_size_ready
    nop
mb_luma_plane:
    li $t5, 176
    li $t6, 143
mb_plane_size_ready:
    sltu $t7, $t4, $t5
    bnezc $t7, mb_restore
    nop
    sltiu $t7, $t4, 4097
    beqzc $t7, mb_restore
    nop
    dmul $t6, $t6, $t4
    daddu $t6, $t6, $t5
    daddu $t6, $t6, $t3
    sltu $t7, $t6, $t3
    bnezc $t7, mb_restore
    nop
    daddiu $t0, $t0, 1
    sltiu $t1, $t0, 6
    bnezc $t1, mb_validate_plane
    nop
    move $s2, $zero
    move $s3, $zero
mb_block:
    sltiu $t0, $s2, 4
    beqzc $t0, mb_chroma_block
    nop
    move $t0, $zero
    andi $t6, $s2, 1
    dsll $t6, $t6, 3
    daddu $t6, $t6, $s4
    dsrl $t7, $s2, 1
    dsll $t7, $t7, 3
    daddu $t7, $t7, $s5
    daddu $t8, $t6, $s6
    daddu $t9, $t7, $s7
    sd $zero, 256($sp)
    sd $zero, 264($sp)
    bc mb_geometry_ready
    nop
mb_chroma_block:
    daddiu $t0, $s2, -3
    dsrl $t6, $s4, 1
    dsrl $t7, $s5, 1
    dsra $t2, $s6, 1
    dsra $t3, $s7, 1
    daddu $t8, $t6, $t2
    daddu $t9, $t7, $t3
    andi $t2, $s6, 1
    andi $t3, $s7, 1
    sd $t2, 256($sp)
    sd $t3, 264($sp)
mb_geometry_ready:
    dsll $t0, $t0, 3
    daddu $t1, $s0, $t0
    ld $t2, 0($t1)
    ld $t3, 24($t1)
    ld $t4, 48($t1)
    ld $t5, 72($t1)
    dmul $t7, $t7, $t4
    daddu $t2, $t2, $t7
    daddu $t2, $t2, $t6
    dmul $t9, $t9, $t5
    daddu $t3, $t3, $t9
    daddu $t3, $t3, $t8
    sd $t2, 224($sp)
    sd $t3, 232($sp)
    sd $t4, 240($sp)
    sd $t5, 248($sp)
    sd $fp, 272($sp)
    sd $zero, 280($sp)
    daddiu $a0, $sp, 224
    li $t0, 192
    dmul $t0, $t0, $s2
    daddiu $a1, $sp, 288
    daddu $a1, $a1, $t0
    balc h263_predict8
    nop
    bnezc $v0, mb_dependency_failure
    nop
    daddu $s3, $s3, $v1
    daddiu $s2, $s2, 1
    sltiu $t0, $s2, 6
    bnezc $t0, mb_block
    nop
mb_publish:
    daddiu $t0, $sp, 288
    move $t1, $s1
    li $t2, 144
mb_publish_word:
    ld $t3, 0($t0)
    sd $t3, 0($t1)
    daddiu $t0, $t0, 8
    daddiu $t1, $t1, 8
    daddiu $t2, $t2, -1
    bnezc $t2, mb_publish_word
    nop
    move $v0, $zero
    move $v1, $s3
    bc mb_restore
    nop
mb_dependency_failure:
    li $v0, 2
    move $v1, $zero
mb_restore:
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
    daddiu $sp, $sp, 1440
mb_early_return:
    jrc $ra
    nop

h263_predict8:
    li $v0, 1
    move $v1, $zero
    andi $t0, $a0, 7
    bnezc $t0, pred_early_return
    nop
    andi $t0, $a1, 15
    bnezc $t0, pred_early_return
    nop
    daddiu $t0, $a0, 64
    sltu $t0, $t0, $a0
    bnezc $t0, pred_early_return
    nop
    daddiu $t0, $a1, 192
    sltu $t0, $t0, $a1
    bnezc $t0, pred_early_return
    nop
    daddiu $sp, $sp, -96
    sd $s0, 0($sp)
    sd $s1, 8($sp)
    sd $s2, 16($sp)
    sd $s3, 24($sp)
    sd $s4, 32($sp)
    sd $s5, 40($sp)
    sd $s6, 48($sp)
    sd $s7, 56($sp)
    sd $fp, 64($sp)
    ld $s0, 0($a0)
    ld $s1, 8($a0)
    ld $s2, 16($a0)
    ld $s3, 24($a0)
    ld $s4, 32($a0)
    ld $s5, 40($a0)
    ld $s6, 48($a0)
    ld $t1, 56($a0)
    move $s7, $a1
    bnezc $t1, pred_return
    nop
    sltiu $t0, $s2, 8
    bnezc $t0, pred_return
    nop
    sltiu $t0, $s2, 4097
    beqzc $t0, pred_return
    nop
    sltiu $t0, $s4, 2
    beqzc $t0, pred_return
    nop
    sltiu $t0, $s5, 2
    beqzc $t0, pred_return
    nop
    sltiu $t0, $s6, 2
    beqzc $t0, pred_return
    nop
    daddiu $t1, $s4, 8
    sltu $t0, $s3, $t1
    bnezc $t0, pred_return
    nop
    sltiu $t0, $s3, 4097
    beqzc $t0, pred_return
    nop
    dsll $t0, $s2, 3
    dsubu $t0, $t0, $s2
    daddiu $t0, $t0, 8
    daddu $t0, $t0, $s0
    sltu $t0, $t0, $s0
    bnezc $t0, pred_return
    nop
    daddiu $t0, $s5, 7
    dmul $t0, $t0, $s3
    daddu $t0, $t0, $s4
    daddiu $t0, $t0, 8
    daddu $t0, $t0, $s1
    sltu $t0, $t0, $s1
    bnezc $t0, pred_return
    nop
    daddiu $fp, $s7, 64
    li $t8, 8
    bnezc $s6, pred_vector
    nop
pred_scalar_row:
    move $t0, $s0
    move $t1, $s1
    li $t7, 8
pred_scalar_pixel:
    lbu $t2, 0($t1)
    beqzc $s4, pred_scalar_horizontal_ready
    nop
    lbu $t3, 1($t1)
    addu $t2, $t2, $t3
pred_scalar_horizontal_ready:
    beqzc $s5, pred_scalar_vertical_ready
    nop
    daddu $t3, $t1, $s3
    lbu $t4, 0($t3)
    addu $t2, $t2, $t4
    beqzc $s4, pred_scalar_vertical_ready
    nop
    lbu $t4, 1($t3)
    addu $t2, $t2, $t4
pred_scalar_vertical_ready:
    addu $t3, $s4, $s5
    beqzc $t3, pred_scalar_rounded
    nop
    li $t4, 1
    daddiu $t5, $t3, -1
    sllv $t4, $t4, $t5
    addu $t2, $t2, $t4
    srlv $t2, $t2, $t3
pred_scalar_rounded:
    sb $t2, 0($s7)
    lbu $t4, 0($t0)
    subu $t4, $t4, $t2
    sh $t4, 0($fp)
    abs $t5, $t4
    daddu $v1, $v1, $t5
    daddiu $t0, $t0, 1
    daddiu $t1, $t1, 1
    daddiu $s7, $s7, 1
    daddiu $fp, $fp, 2
    daddiu $t7, $t7, -1
    bnezc $t7, pred_scalar_pixel
    nop
    daddu $s0, $s0, $s2
    daddu $s1, $s1, $s3
    daddiu $t8, $t8, -1
    bnezc $t8, pred_scalar_row
    nop
    bc pred_success
    nop
pred_vector:
    ldi.h $w8, 0
    ldi.h $w9, 0
pred_vector_row:
    lbu $t2, 0($s0)
    insert.h $w0[0], $t2
    lbu $t2, 1($s0)
    insert.h $w0[1], $t2
    lbu $t2, 2($s0)
    insert.h $w0[2], $t2
    lbu $t2, 3($s0)
    insert.h $w0[3], $t2
    lbu $t2, 4($s0)
    insert.h $w0[4], $t2
    lbu $t2, 5($s0)
    insert.h $w0[5], $t2
    lbu $t2, 6($s0)
    insert.h $w0[6], $t2
    lbu $t2, 7($s0)
    insert.h $w0[7], $t2
    lbu $t2, 0($s1)
    insert.h $w1[0], $t2
    lbu $t2, 1($s1)
    insert.h $w1[1], $t2
    lbu $t2, 2($s1)
    insert.h $w1[2], $t2
    lbu $t2, 3($s1)
    insert.h $w1[3], $t2
    lbu $t2, 4($s1)
    insert.h $w1[4], $t2
    lbu $t2, 5($s1)
    insert.h $w1[5], $t2
    lbu $t2, 6($s1)
    insert.h $w1[6], $t2
    lbu $t2, 7($s1)
    insert.h $w1[7], $t2
    beqzc $s4, pred_vector_horizontal_ready
    nop
    lbu $t2, 1($s1)
    insert.h $w2[0], $t2
    lbu $t2, 2($s1)
    insert.h $w2[1], $t2
    lbu $t2, 3($s1)
    insert.h $w2[2], $t2
    lbu $t2, 4($s1)
    insert.h $w2[3], $t2
    lbu $t2, 5($s1)
    insert.h $w2[4], $t2
    lbu $t2, 6($s1)
    insert.h $w2[5], $t2
    lbu $t2, 7($s1)
    insert.h $w2[6], $t2
    lbu $t2, 8($s1)
    insert.h $w2[7], $t2
pred_vector_horizontal_ready:
    beqzc $s5, pred_vector_vertical_ready
    nop
    daddu $t0, $s1, $s3
    lbu $t2, 0($t0)
    insert.h $w3[0], $t2
    lbu $t2, 1($t0)
    insert.h $w3[1], $t2
    lbu $t2, 2($t0)
    insert.h $w3[2], $t2
    lbu $t2, 3($t0)
    insert.h $w3[3], $t2
    lbu $t2, 4($t0)
    insert.h $w3[4], $t2
    lbu $t2, 5($t0)
    insert.h $w3[5], $t2
    lbu $t2, 6($t0)
    insert.h $w3[6], $t2
    lbu $t2, 7($t0)
    insert.h $w3[7], $t2
    beqzc $s4, pred_vector_vertical_ready
    nop
    lbu $t2, 1($t0)
    insert.h $w4[0], $t2
    lbu $t2, 2($t0)
    insert.h $w4[1], $t2
    lbu $t2, 3($t0)
    insert.h $w4[2], $t2
    lbu $t2, 4($t0)
    insert.h $w4[3], $t2
    lbu $t2, 5($t0)
    insert.h $w4[4], $t2
    lbu $t2, 6($t0)
    insert.h $w4[5], $t2
    lbu $t2, 7($t0)
    insert.h $w4[6], $t2
    lbu $t2, 8($t0)
    insert.h $w4[7], $t2
pred_vector_vertical_ready:
    beqzc $s4, pred_vector_no_horizontal
    nop
    beqzc $s5, pred_vector_horizontal
    nop
    addv.h $w5, $w1, $w2
    addv.h $w6, $w3, $w4
    addv.h $w5, $w5, $w6
    srlri.h $w5, $w5, 2
    bc pred_vector_predicted
    nop
pred_vector_horizontal:
    aver_u.h $w5, $w1, $w2
    bc pred_vector_predicted
    nop
pred_vector_no_horizontal:
    beqzc $s5, pred_vector_integer
    nop
    aver_u.h $w5, $w1, $w3
    bc pred_vector_predicted
    nop
pred_vector_integer:
    move.v $w5, $w1
pred_vector_predicted:
    subv.h $w6, $w0, $w5
    st.h $w6, 0($fp)
    asub_s.h $w7, $w6, $w9
    addv.h $w8, $w8, $w7
    copy_u.h $t2, $w5[0]
    sb $t2, 0($s7)
    copy_u.h $t2, $w5[1]
    sb $t2, 1($s7)
    copy_u.h $t2, $w5[2]
    sb $t2, 2($s7)
    copy_u.h $t2, $w5[3]
    sb $t2, 3($s7)
    copy_u.h $t2, $w5[4]
    sb $t2, 4($s7)
    copy_u.h $t2, $w5[5]
    sb $t2, 5($s7)
    copy_u.h $t2, $w5[6]
    sb $t2, 6($s7)
    copy_u.h $t2, $w5[7]
    sb $t2, 7($s7)
    daddiu $s7, $s7, 8
    daddiu $fp, $fp, 16
    daddu $s0, $s0, $s2
    daddu $s1, $s1, $s3
    daddiu $t8, $t8, -1
    bnezc $t8, pred_vector_row
    nop
    hadd_u.w $w10, $w8, $w8
    hadd_u.d $w11, $w10, $w10
    copy_s.d $t0, $w11[0]
    copy_s.d $t1, $w11[1]
    daddu $v1, $t0, $t1
pred_success:
    move $v0, $zero
pred_return:
    ld $s0, 0($sp)
    ld $s1, 8($sp)
    ld $s2, 16($sp)
    ld $s3, 24($sp)
    ld $s4, 32($sp)
    ld $s5, 40($sp)
    ld $s6, 48($sp)
    ld $s7, 56($sp)
    ld $fp, 64($sp)
    daddiu $sp, $sp, 96
pred_early_return:
    jrc $ra
    nop

h263_encode_block8:
    li $v0, 1
    move $v1, $zero
    andi $t0, $a0, 7
    bnezc $t0, block_early_return
    nop
    andi $t0, $a1, 15
    bnezc $t0, block_early_return
    nop
    daddiu $t0, $a0, 64
    sltu $t0, $t0, $a0
    bnezc $t0, block_early_return
    nop
    daddiu $t0, $a1, 448
    sltu $t0, $t0, $a1
    bnezc $t0, block_early_return
    nop
    daddiu $sp, $sp, -992
    sd $s0, 0($sp)
    sd $s1, 8($sp)
    sd $s2, 16($sp)
    sd $s3, 24($sp)
    sd $s4, 32($sp)
    sd $s5, 40($sp)
    sd $s6, 48($sp)
    sd $s7, 56($sp)
    sd $fp, 64($sp)
    sd $ra, 72($sp)
    ld $s0, 0($a0)
    ld $s1, 8($a0)
    ld $s2, 16($a0)
    ld $s3, 24($a0)
    ld $s4, 32($a0)
    ld $s5, 40($a0)
    ld $s6, 48($a0)
    ld $t0, 56($a0)
    bnezc $t0, block_restore
    nop
    move $s7, $a1
    sltiu $t0, $s5, 2
    beqzc $t0, block_restore
    nop
    sltiu $t0, $s6, 2
    beqzc $t0, block_restore
    nop
    daddiu $t0, $s4, -1
    sltiu $t0, $t0, 31
    beqzc $t0, block_restore
    nop
    daddiu $t0, $s1, -8
    sltiu $t0, $t0, 4089
    beqzc $t0, block_restore
    nop
    dsll $t0, $s1, 3
    dsubu $t0, $t0, $s1
    daddiu $t0, $t0, 8
    daddu $t0, $t0, $s0
    sltu $t0, $t0, $s0
    bnezc $t0, block_restore
    nop
    beqzc $s5, block_check_prediction
    nop
    or $t0, $s2, $s3
    bnezc $t0, block_restore
    nop
    bc block_gather_start
    nop
block_check_prediction:
    daddiu $t0, $s3, -8
    sltiu $t0, $t0, 4089
    beqzc $t0, block_restore
    nop
    dsll $t0, $s3, 3
    dsubu $t0, $t0, $s3
    daddiu $t0, $t0, 8
    daddu $t0, $t0, $s2
    sltu $t0, $t0, $s2
    bnezc $t0, block_restore
    nop
block_gather_start:
    move $fp, $zero
block_gather_loop:
    dsrl $t0, $fp, 3
    andi $t1, $fp, 7
    dmul $t2, $t0, $s1
    daddu $t2, $t2, $s0
    daddu $t2, $t2, $t1
    lbu $t2, 0($t2)
    move $t3, $zero
    bnezc $s5, block_prediction_ready
    nop
    dmul $t3, $t0, $s3
    daddu $t3, $t3, $s2
    daddu $t3, $t3, $t1
    lbu $t3, 0($t3)
block_prediction_ready:
    dsll $t0, $fp, 1
    daddu $t0, $t0, $sp
    sh $t3, 272($t0)
    dsubu $t2, $t2, $t3
    sh $t2, 144($t0)
    daddiu $fp, $fp, 1
    sltiu $t0, $fp, 64
    bnezc $t0, block_gather_loop
    nop
    daddiu $a0, $sp, 96
    daddiu $t0, $sp, 144
    sd $t0, 0($a0)
    li $t0, 16
    sd $t0, 8($a0)
    sd $zero, 16($a0)
    sd $s6, 24($a0)
    sd $zero, 32($a0)
    sd $zero, 40($a0)
    daddiu $a1, $sp, 400
    balc h263_transform8
    nop
    bnezc $v0, block_child_failure
    nop
    daddiu $a0, $sp, 96
    daddiu $t0, $sp, 400
    sd $t0, 0($a0)
    sd $s4, 8($a0)
    sd $s5, 16($a0)
    sd $s6, 24($a0)
    daddiu $a1, $sp, 528
    balc h263_quantize8
    nop
    bnezc $v0, block_child_failure
    nop
    sd $v1, 80($sp)
    daddiu $a0, $sp, 96
    daddiu $t0, $sp, 656
    sd $t0, 0($a0)
    li $t0, 16
    sd $t0, 8($a0)
    li $t0, 1
    sd $t0, 16($a0)
    sd $s6, 24($a0)
    sd $zero, 32($a0)
    sd $zero, 40($a0)
    daddiu $a1, $sp, 784
    balc h263_transform8
    nop
    bnezc $v0, block_child_failure
    nop
    move $fp, $zero
    bnezc $s6, block_vector_reconstruct
    nop
block_scalar_reconstruct:
    dsll $t0, $fp, 1
    daddu $t0, $t0, $sp
    lh $t1, 784($t0)
    lh $t2, 272($t0)
    daddu $t1, $t1, $t2
    bgezc $t1, block_pixel_min_ready
    nop
    move $t1, $zero
block_pixel_min_ready:
    slti $t2, $t1, 256
    bnezc $t2, block_pixel_ready
    nop
    li $t1, 255
block_pixel_ready:
    daddu $t0, $sp, $fp
    sb $t1, 912($t0)
    daddiu $fp, $fp, 1
    sltiu $t0, $fp, 64
    bnezc $t0, block_scalar_reconstruct
    nop
    bc block_publish_start
    nop
block_vector_reconstruct:
    ldi.h $w10, 0
    li $t0, 255
    fill.h $w11, $t0
block_vector_loop:
    dsll $t0, $fp, 1
    daddu $t0, $t0, $sp
    ld.h $w0, 784($t0)
    ld.h $w1, 272($t0)
    addv.h $w2, $w0, $w1
    max_s.h $w2, $w2, $w10
    min_s.h $w2, $w2, $w11
    st.h $w2, 144($sp)
    move $t1, $zero
block_store_vector_pixels:
    dsll $t0, $t1, 1
    daddu $t0, $t0, $sp
    lh $t2, 144($t0)
    daddu $t0, $fp, $t1
    daddu $t0, $t0, $sp
    sb $t2, 912($t0)
    daddiu $t1, $t1, 1
    sltiu $t0, $t1, 8
    bnezc $t0, block_store_vector_pixels
    nop
    daddiu $fp, $fp, 8
    sltiu $t0, $fp, 64
    bnezc $t0, block_vector_loop
    nop
block_publish_start:
    move $fp, $zero
block_publish_loop:
    daddu $t0, $sp, $fp
    ld $t1, 528($t0)
    daddu $t0, $s7, $fp
    sd $t1, 0($t0)
    daddiu $fp, $fp, 8
    sltiu $t0, $fp, 448
    bnezc $t0, block_publish_loop
    nop
    move $v0, $zero
    ld $v1, 80($sp)
    bc block_restore
    nop
block_child_failure:
    li $v0, 2
    move $v1, $zero
block_restore:
    ld $s0, 0($sp)
    ld $s1, 8($sp)
    ld $s2, 16($sp)
    ld $s3, 24($sp)
    ld $s4, 32($sp)
    ld $s5, 40($sp)
    ld $s6, 48($sp)
    ld $s7, 56($sp)
    ld $fp, 64($sp)
    ld $ra, 72($sp)
    daddiu $sp, $sp, 992
block_early_return:
    jrc $ra
    nop

h263_transform8:
    li $v0, 1
    move $v1, $zero
    andi $t0, $a0, 7
    bnezc $t0, transform_early_return
    nop
    andi $t0, $a1, 15
    bnezc $t0, transform_early_return
    nop
    daddiu $t0, $a0, 48
    sltu $t0, $t0, $a0
    bnezc $t0, transform_early_return
    nop
    daddiu $t0, $a1, 128
    sltu $t0, $t0, $a1
    bnezc $t0, transform_early_return
    nop
    daddiu $sp, $sp, -608
    sd $s0, 0($sp)
    sd $s1, 8($sp)
    sd $s2, 16($sp)
    sd $s3, 24($sp)
    sd $s4, 32($sp)
    sd $s5, 40($sp)
    sd $s6, 48($sp)
    sd $s7, 56($sp)
    sd $fp, 64($sp)
    sd $a1, 72($sp)
    ld $s7, 0($a0)
    ld $fp, 8($a0)
    ld $s3, 16($a0)
    ld $s2, 24($a0)
    ld $t0, 32($a0)
    ld $t1, 40($a0)
    or $t0, $t0, $t1
    bnezc $t0, transform_restore
    nop
    andi $t0, $s7, 1
    bnezc $t0, transform_restore
    nop
    andi $t0, $fp, 1
    bnezc $t0, transform_restore
    nop
    sltiu $t0, $fp, 16
    bnezc $t0, transform_restore
    nop
    sltiu $t0, $fp, 4097
    beqzc $t0, transform_restore
    nop
    sltiu $t0, $s3, 2
    beqzc $t0, transform_restore
    nop
    sltiu $t0, $s2, 2
    beqzc $t0, transform_restore
    nop
    dsll $t0, $fp, 3
    dsubu $t0, $t0, $fp
    daddiu $t0, $t0, 16
    daddu $t0, $t0, $s7
    sltu $t0, $t0, $s7
    bnezc $t0, transform_restore
    nop
    li $t4, 256
    li $t5, 512
    beqzc $s3, transform_domain_ready
    nop
    li $t4, 2048
    li $t5, 4096
transform_domain_ready:
    daddiu $a3, $sp, 80
    li $t6, 8
transform_admit_row:
    move $t0, $s7
    li $t7, 8
transform_admit_sample:
    lh $t1, 0($t0)
    daddu $t2, $t1, $t4
    sltu $t2, $t2, $t5
    beqzc $t2, transform_bad_sample
    nop
    sh $t1, 0($a3)
    daddiu $t0, $t0, 2
    daddiu $a3, $a3, 2
    daddiu $t7, $t7, -1
    bnezc $t7, transform_admit_sample
    nop
    daddu $s7, $s7, $fp
    daddiu $t6, $t6, -1
    bnezc $t6, transform_admit_row
    nop
    dla $s0, transform_B_h
    dla $s1, transform_B_w
    beqzc $s3, transform_basis_ready
    nop
    dla $s0, transform_BT_h
    dla $s1, transform_BT_w
transform_basis_ready:
    daddiu $s6, $sp, 208
    daddiu $s7, $sp, 80
    move $s4, $zero
transform_first_row:
    dsll $t0, $s4, 4
    daddu $fp, $s7, $t0
    move $s5, $zero
transform_first_column:
    dsll $t0, $s5, 4
    daddu $t0, $s0, $t0
    bnezc $s2, transform_first_vector
    nop
    move $t1, $fp
    move $t2, $zero
    li $t3, 8
transform_first_scalar:
    lh $t4, 0($t0)
    lh $t5, 0($t1)
    dmul $t4, $t4, $t5
    daddu $t2, $t2, $t4
    daddiu $t0, $t0, 2
    daddiu $t1, $t1, 2
    daddiu $t3, $t3, -1
    bnezc $t3, transform_first_scalar
    nop
    bc transform_first_store
    nop
transform_first_vector:
    ld.h $w0, 0($fp)
    ld.h $w1, 0($t0)
    dotp_s.w $w2, $w0, $w1
    hadd_s.d $w3, $w2, $w2
    copy_s.d $t4, $w3[0]
    copy_s.d $t5, $w3[1]
    daddu $t2, $t4, $t5
transform_first_store:
    dsll $t0, $s5, 3
    daddu $t0, $t0, $s4
    dsll $t0, $t0, 2
    daddu $t0, $t0, $s6
    sw $t2, 0($t0)
    daddiu $s5, $s5, 1
    sltiu $t0, $s5, 8
    bnezc $t0, transform_first_column
    nop
    daddiu $s4, $s4, 1
    sltiu $t0, $s4, 8
    bnezc $t0, transform_first_row
    nop
    daddiu $s7, $sp, 464
    move $s4, $zero
transform_second_row:
    dsll $t0, $s4, 5
    daddu $fp, $s1, $t0
    move $s5, $zero
transform_second_column:
    dsll $t0, $s5, 5
    daddu $t0, $s6, $t0
    bnezc $s2, transform_second_vector
    nop
    move $t1, $fp
    move $t2, $zero
    li $t3, 8
transform_second_scalar:
    lw $t4, 0($t0)
    lw $t5, 0($t1)
    dmul $t4, $t4, $t5
    daddu $t2, $t2, $t4
    daddiu $t0, $t0, 4
    daddiu $t1, $t1, 4
    daddiu $t3, $t3, -1
    bnezc $t3, transform_second_scalar
    nop
    bc transform_round
    nop
transform_second_vector:
    ld.w $w0, 0($t0)
    ld.w $w1, 0($fp)
    dotp_s.d $w2, $w0, $w1
    ld.w $w3, 16($t0)
    ld.w $w4, 16($fp)
    dotp_s.d $w5, $w3, $w4
    addv.d $w6, $w2, $w5
    copy_s.d $t4, $w6[0]
    copy_s.d $t5, $w6[1]
    daddu $t2, $t4, $t5
transform_round:
    dli $t0, 0x80000000
    daddu $t2, $t2, $t0
    dsra32 $t2, $t2, 0
    li $t3, -2048
    li $t4, 2047
    beqzc $s3, transform_clip
    nop
    li $t3, -256
    li $t4, 255
transform_clip:
    slt $t5, $t2, $t3
    beqzc $t5, transform_clip_high
    nop
    move $t2, $t3
transform_clip_high:
    slt $t5, $t4, $t2
    beqzc $t5, transform_output_sample
    nop
    move $t2, $t4
transform_output_sample:
    dsll $t0, $s4, 3
    daddu $t0, $t0, $s5
    dsll $t0, $t0, 1
    daddu $t0, $s7, $t0
    sh $t2, 0($t0)
    daddiu $s5, $s5, 1
    sltiu $t0, $s5, 8
    bnezc $t0, transform_second_column
    nop
    daddiu $s4, $s4, 1
    sltiu $t0, $s4, 8
    bnezc $t0, transform_second_row
    nop
    move $t0, $s7
    ld $t1, 72($sp)
    li $t2, 16
transform_publish:
    ld $t3, 0($t0)
    sd $t3, 0($t1)
    daddiu $t0, $t0, 8
    daddiu $t1, $t1, 8
    daddiu $t2, $t2, -1
    bnezc $t2, transform_publish
    nop
    move $v0, $zero
    li $v1, 64
    bc transform_restore
    nop
transform_bad_sample:
    li $v0, 2
    move $v1, $zero
transform_restore:
    ld $s0, 0($sp)
    ld $s1, 8($sp)
    ld $s2, 16($sp)
    ld $s3, 24($sp)
    ld $s4, 32($sp)
    ld $s5, 40($sp)
    ld $s6, 48($sp)
    ld $s7, 56($sp)
    ld $fp, 64($sp)
    daddiu $sp, $sp, 608
transform_early_return:
    jrc $ra
    nop

h263_quantize8:
    li $v0, 1
    move $v1, $zero
    andi $t0, $a0, 7
    bnezc $t0, quant_early_return
    nop
    andi $t0, $a1, 15
    bnezc $t0, quant_early_return
    nop
    daddiu $t0, $a0, 32
    sltu $t0, $t0, $a0
    bnezc $t0, quant_early_return
    nop
    daddiu $t0, $a1, 256
    sltu $t0, $t0, $a1
    bnezc $t0, quant_early_return
    nop
    ld $t0, 0($a0)
    andi $t1, $t0, 1
    bnezc $t1, quant_early_return
    nop
    daddiu $t1, $t0, 128
    sltu $t1, $t1, $t0
    bnezc $t1, quant_early_return
    nop
    ld $t1, 8($a0)
    daddiu $t2, $t1, -1
    sltiu $t2, $t2, 31
    beqzc $t2, quant_early_return
    nop
    ld $t2, 16($a0)
    sltiu $t3, $t2, 2
    beqzc $t3, quant_early_return
    nop
    ld $t3, 24($a0)
    sltiu $t4, $t3, 2
    beqzc $t4, quant_early_return
    nop
    daddiu $sp, $sp, -352
    sd $s0, 0($sp)
    sd $s1, 8($sp)
    sd $s2, 16($sp)
    sd $s3, 24($sp)
    sd $s4, 32($sp)
    sd $s5, 40($sp)
    sd $s6, 48($sp)
    sd $s7, 56($sp)
    move $s0, $t0
    move $s1, $t1
    move $s2, $t2
    move $s3, $t3
    move $s4, $a1
    move $s5, $zero
    move $s6, $zero
    dsll $s7, $s1, 1
quant_level_loop:
    dsll $t0, $s5, 1
    daddu $t1, $s0, $t0
    lh $t2, 0($t1)
    daddiu $t3, $t2, 2048
    sltiu $t3, $t3, 4096
    beqzc $t3, quant_bad_domain
    nop
    bnezc $s5, quant_ordinary_level
    nop
    bnezc $s2, quant_dc_level
    nop
quant_ordinary_level:
    move $t3, $t2
    bgezc $t3, quant_abs_ready
    nop
    dsubu $t3, $zero, $t3
quant_abs_ready:
    ddivu $t3, $t3, $s7
    sltiu $t4, $t3, 128
    bnezc $t4, quant_magnitude_ready
    nop
    li $t3, 127
quant_magnitude_ready:
    bgezc $t2, quant_sign_ready
    nop
    dsubu $t3, $zero, $t3
quant_sign_ready:
    beqzc $t3, quant_store_level
    nop
    daddiu $s6, $s6, 1
    bc quant_store_level
    nop
quant_dc_level:
    daddiu $t3, $t2, 4
    dsra $t3, $t3, 3
    bgtzc $t3, quant_dc_min_ready
    nop
    li $t3, 1
quant_dc_min_ready:
    slti $t4, $t3, 255
    bnezc $t4, quant_dc_max_ready
    nop
    li $t3, 254
quant_dc_max_ready:
    li $t4, 128
    bnec $t3, $t4, quant_store_level
    nop
    li $t3, 255
quant_store_level:
    daddiu $t1, $sp, 80
    daddu $t1, $t1, $t0
    sh $t3, 0($t1)
    daddiu $s5, $s5, 1
    sltiu $t0, $s5, 64
    bnezc $t0, quant_level_loop
    nop
    beqzc $s3, quant_scalar_start
    nop
    fill.h $w10, $s1
    andi $t0, $s1, 1
    xori $t0, $t0, 1
    fill.h $w11, $t0
    li $t0, -2048
    fill.h $w12, $t0
    ldi.h $w13, 0
    li $t0, 2047
    fill.h $w14, $t0
    move $s5, $zero
quant_vector_loop:
    daddu $t0, $sp, $s5
    ld.h $w0, 80($t0)
    srai.h $w1, $w0, 15
    xor.v $w2, $w0, $w1
    subv.h $w2, $w2, $w1
    slli.h $w2, $w2, 1
    addvi.h $w2, $w2, 1
    mulv.h $w2, $w2, $w10
    subv.h $w2, $w2, $w11
    xor.v $w2, $w2, $w1
    subv.h $w2, $w2, $w1
    ceq.h $w3, $w0, $w13
    nor.v $w3, $w3, $w3
    and.v $w2, $w2, $w3
    max_s.h $w2, $w2, $w12
    min_s.h $w2, $w2, $w14
    st.h $w2, 208($t0)
    daddiu $s5, $s5, 16
    sltiu $t0, $s5, 128
    bnezc $t0, quant_vector_loop
    nop
    bc quant_dc_reconstruct
    nop
quant_scalar_start:
    move $s5, $zero
quant_scalar_loop:
    daddu $t0, $sp, $s5
    lh $t1, 80($t0)
    move $t2, $zero
    beqzc $t1, quant_store_rec
    nop
    move $t2, $t1
    bgezc $t2, quant_rec_abs_ready
    nop
    dsubu $t2, $zero, $t2
quant_rec_abs_ready:
    dsll $t2, $t2, 1
    daddiu $t2, $t2, 1
    dmul $t2, $t2, $s1
    andi $t3, $s1, 1
    xori $t3, $t3, 1
    dsubu $t2, $t2, $t3
    bgezc $t1, quant_rec_sign_ready
    nop
    dsubu $t2, $zero, $t2
quant_rec_sign_ready:
    li $t3, -2048
    slt $t4, $t2, $t3
    beqzc $t4, quant_rec_min_ready
    nop
    move $t2, $t3
quant_rec_min_ready:
    li $t3, 2047
    slt $t4, $t3, $t2
    beqzc $t4, quant_store_rec
    nop
    move $t2, $t3
quant_store_rec:
    sh $t2, 208($t0)
    daddiu $s5, $s5, 2
    sltiu $t0, $s5, 128
    bnezc $t0, quant_scalar_loop
    nop
quant_dc_reconstruct:
    beqzc $s2, quant_publish
    nop
    lh $t0, 80($sp)
    li $t1, 255
    bnec $t0, $t1, quant_dc_decode_ready
    nop
    li $t0, 128
quant_dc_decode_ready:
    dsll $t0, $t0, 3
    sh $t0, 208($sp)
quant_publish:
    move $s5, $zero
quant_publish_loop:
    daddu $t0, $sp, $s5
    ld $t1, 80($t0)
    daddu $t0, $s4, $s5
    sd $t1, 0($t0)
    daddiu $s5, $s5, 8
    sltiu $t0, $s5, 256
    bnezc $t0, quant_publish_loop
    nop
    move $v0, $zero
    move $v1, $s6
    bc quant_restore
    nop
quant_bad_domain:
    li $v0, 2
    move $v1, $zero
quant_restore:
    ld $s0, 0($sp)
    ld $s1, 8($sp)
    ld $s2, 16($sp)
    ld $s3, 24($sp)
    ld $s4, 32($sp)
    ld $s5, 40($sp)
    ld $s6, 48($sp)
    ld $s7, 56($sp)
    daddiu $sp, $sp, 352
quant_early_return:
    jrc $ra
    nop

h263_emit_macroblock:
    li $v0, 1
    move $v1, $zero
    andi $t0, $a0, 7
    bnezc $t0, syntax_early_return
    nop
    daddiu $t0, $a0, 80
    sltu $t0, $t0, $a0
    bnezc $t0, syntax_early_return
    nop
    daddiu $sp, $sp, -1520
    sd $s0, 0($sp)
    sd $s1, 8($sp)
    sd $s2, 16($sp)
    sd $s3, 24($sp)
    sd $s4, 32($sp)
    sd $s5, 40($sp)
    sd $s6, 48($sp)
    sd $s7, 56($sp)
    sd $fp, 64($sp)
    sd $ra, 72($sp)
    ld $s0, 0($a0)
    ld $s1, 8($a0)
    ld $s2, 16($a0)
    ld $s3, 24($a0)
    ld $s4, 32($a0)
    ld $s5, 40($a0)
    ld $t0, 48($a0)
    sd $t0, 1440($sp)
    ld $t0, 56($a0)
    sd $t0, 1448($sp)
    ld $t0, 64($a0)
    sd $t0, 1456($sp)
    ld $t0, 72($a0)
    bnezc $t0, syntax_restore
    nop
    sltiu $t0, $s2, 2
    beqzc $t0, syntax_restore
    nop
    sltiu $t0, $s3, 2
    beqzc $t0, syntax_restore
    nop
    or $t0, $s2, $s3
    beqzc $t0, syntax_restore
    nop
    ld $t0, 1456($sp)
    sltiu $t0, $t0, 2
    beqzc $t0, syntax_restore
    nop
    andi $t0, $s0, 1
    bnezc $t0, syntax_restore
    nop
    daddiu $t0, $s0, 768
    sltu $t0, $t0, $s0
    bnezc $t0, syntax_restore
    nop
    andi $t0, $s1, 7
    bnezc $t0, syntax_restore
    nop
    daddiu $t0, $s1, 24
    sltu $t0, $t0, $s1
    bnezc $t0, syntax_restore
    nop
    beqzc $s3, syntax_validate_vectors
    nop
    or $t0, $s4, $s5
    ld $t1, 1440($sp)
    or $t0, $t0, $t1
    ld $t1, 1448($sp)
    or $t0, $t0, $t1
    bnezc $t0, syntax_restore
    nop
syntax_validate_vectors:
    daddiu $t0, $s4, 32
    sltiu $t0, $t0, 64
    beqzc $t0, syntax_restore
    nop
    daddiu $t0, $s5, 32
    sltiu $t0, $t0, 64
    beqzc $t0, syntax_restore
    nop
    ld $t0, 1440($sp)
    daddiu $t0, $t0, 32
    sltiu $t0, $t0, 64
    beqzc $t0, syntax_restore
    nop
    ld $t0, 1448($sp)
    daddiu $t0, $t0, 32
    sltiu $t0, $t0, 64
    beqzc $t0, syntax_restore
    nop
    ld $s7, 0($s1)
    ld $t0, 8($s1)
    sd $t0, 1432($sp)
    ld $t1, 16($s1)
    sd $t1, 1424($sp)
    dli $t2, 1048576
    sltu $t2, $t2, $t0
    bnezc $t2, syntax_restore
    nop
    daddu $t2, $s7, $t0
    sltu $t2, $t2, $s7
    bnezc $t2, syntax_restore
    nop
    dsll $t0, $t0, 3
    sltu $t0, $t0, $t1
    bnezc $t0, syntax_restore
    nop
    move $s6, $zero
    move $fp, $zero
syntax_validate_levels:
    dsll $t0, $fp, 1
    daddu $t0, $t0, $s0
    lh $t1, 0($t0)
    andi $t0, $fp, 63
    bnezc $t0, syntax_validate_ordinary
    nop
    beqzc $s3, syntax_validate_ordinary
    nop
    daddiu $t0, $t1, -1
    sltiu $t0, $t0, 255
    beqzc $t0, syntax_restore
    nop
    li $t0, 128
    beqc $t1, $t0, syntax_restore
    nop
    bc syntax_validate_next
    nop
syntax_validate_ordinary:
    daddiu $t0, $t1, 127
    sltiu $t0, $t0, 255
    beqzc $t0, syntax_restore
    nop
    beqzc $t1, syntax_validate_next
    nop
    dsrl $t0, $fp, 6
    li $t1, 5
    dsubu $t0, $t1, $t0
    li $t1, 1
    dsllv $t0, $t1, $t0
    or $s6, $s6, $t0
syntax_validate_next:
    daddiu $fp, $fp, 1
    sltiu $t0, $fp, 384
    bnezc $t0, syntax_validate_levels
    nop
    move $fp, $zero
    sd $zero, 1504($sp)
    beqzc $s2, syntax_non_skip
    nop
    bnezc $s3, syntax_non_skip
    nop
    or $t0, $s6, $s4
    or $t0, $t0, $s5
    bnezc $t0, syntax_non_skip
    nop
    ld $t0, 1456($sp)
    beqzc $t0, syntax_non_skip
    nop
    li $a1, 1
    li $a2, 1
    balc syntax_add_plan
    nop
    li $t0, 1
    sd $t0, 1504($sp)
    bc syntax_plan_ready
    nop
syntax_non_skip:
    beqzc $s2, syntax_mcbpc
    nop
    move $a1, $zero
    li $a2, 1
    balc syntax_add_plan
    nop
syntax_mcbpc:
    move $t0, $zero
    beqzc $s2, syntax_mcbpc_group_ready
    nop
    daddiu $t0, $s3, 1
syntax_mcbpc_group_ready:
    dsll $t0, $t0, 2
    andi $t1, $s6, 3
    daddu $t0, $t0, $t1
    dsll $t0, $t0, 4
    dla $t1, syntax_mcbpc_codes
    daddu $t0, $t0, $t1
    ld $a1, 0($t0)
    ld $a2, 8($t0)
    balc syntax_add_plan
    nop
    dsrl $t0, $s6, 2
    bnezc $s3, syntax_cbpy_ready
    nop
    xori $t0, $t0, 15
syntax_cbpy_ready:
    dsll $t0, $t0, 4
    dla $t1, syntax_cbpy_codes
    daddu $t0, $t0, $t1
    ld $a1, 0($t0)
    ld $a2, 8($t0)
    balc syntax_add_plan
    nop
    bnezc $s3, syntax_plan_ready
    nop
    ld $t0, 1440($sp)
    dsubu $t0, $s4, $t0
    daddiu $t0, $t0, 32
    andi $t0, $t0, 63
    dsll $t0, $t0, 4
    dla $t1, syntax_mvd_codes
    daddu $t0, $t0, $t1
    ld $a1, 0($t0)
    ld $a2, 8($t0)
    balc syntax_add_plan
    nop
    ld $t0, 1448($sp)
    dsubu $t0, $s5, $t0
    daddiu $t0, $t0, 32
    andi $t0, $t0, 63
    dsll $t0, $t0, 4
    dla $t1, syntax_mvd_codes
    daddu $t0, $t0, $t1
    ld $a1, 0($t0)
    ld $a2, 8($t0)
    balc syntax_add_plan
    nop
syntax_plan_ready:
    sd $fp, 1464($sp)
    move $fp, $zero
syntax_zero_staging:
    daddu $t0, $sp, $fp
    sd $zero, 272($t0)
    daddiu $fp, $fp, 8
    sltiu $t0, $fp, 1152
    bnezc $t0, syntax_zero_staging
    nop
    daddiu $t0, $sp, 272
    sd $t0, 80($sp)
    li $t0, 1152
    sd $t0, 88($sp)
    ld $t0, 1424($sp)
    andi $t0, $t0, 7
    sd $t0, 96($sp)
    move $fp, $zero
syntax_emit_header:
    dsll $t0, $fp, 4
    daddu $t0, $t0, $sp
    ld $a1, 144($t0)
    ld $a2, 152($t0)
    daddiu $a0, $sp, 80
    balc h263_write_bits
    nop
    bnezc $v0, syntax_child_failure
    nop
    daddiu $fp, $fp, 1
    ld $t0, 1464($sp)
    sltu $t0, $fp, $t0
    bnezc $t0, syntax_emit_header
    nop
    ld $t0, 1504($sp)
    bnezc $t0, syntax_payload_done
    nop
    move $fp, $zero
syntax_emit_blocks:
    bnezc $s3, syntax_emit_block
    nop
    li $t0, 5
    dsubu $t0, $t0, $fp
    dsrlv $t0, $s6, $t0
    andi $t0, $t0, 1
    beqzc $t0, syntax_next_block
    nop
syntax_emit_block:
    dsll $t0, $fp, 7
    daddu $t0, $t0, $s0
    sd $t0, 112($sp)
    daddiu $t0, $sp, 80
    sd $t0, 120($sp)
    sd $s3, 128($sp)
    sd $zero, 136($sp)
    daddiu $a0, $sp, 112
    balc h263_emit_coefficients
    nop
    bnezc $v0, syntax_child_failure
    nop
syntax_next_block:
    daddiu $fp, $fp, 1
    sltiu $t0, $fp, 6
    bnezc $t0, syntax_emit_blocks
    nop
syntax_payload_done:
    ld $t0, 96($sp)
    ld $t1, 1424($sp)
    andi $t2, $t1, 7
    dsubu $t3, $t0, $t2
    sd $t3, 1472($sp)
    ld $t4, 1432($sp)
    dsll $t4, $t4, 3
    dsubu $t4, $t4, $t1
    sltu $t4, $t4, $t3
    bnezc $t4, syntax_no_space
    nop
    daddiu $t0, $t0, 7
    dsrl $t0, $t0, 3
    sd $t0, 1480($sp)
    dsrl $t1, $t1, 3
    daddu $t1, $s7, $t1
    sd $t1, 1488($sp)
    li $t3, 8
    dsubu $t3, $t3, $t2
    li $t4, 255
    dsllv $t4, $t4, $t3
    andi $t4, $t4, 255
    lbu $t5, 0($t1)
    and $t5, $t5, $t4
    nor $t4, $t4, $zero
    lbu $t6, 272($sp)
    and $t6, $t6, $t4
    or $t6, $t6, $t5
    sb $t6, 272($sp)
    ld $t0, 1480($sp)
    daddiu $t0, $t0, -1
    ld $t2, 96($sp)
    andi $t2, $t2, 7
    beqzc $t2, syntax_publish_start
    nop
    li $t3, 8
    dsubu $t3, $t3, $t2
    li $t4, 1
    dsllv $t4, $t4, $t3
    daddiu $t4, $t4, -1
    ld $t1, 1488($sp)
    daddu $t1, $t1, $t0
    lbu $t5, 0($t1)
    and $t5, $t5, $t4
    nor $t4, $t4, $zero
    daddu $t0, $t0, $sp
    lbu $t6, 272($t0)
    and $t6, $t6, $t4
    or $t6, $t6, $t5
    sb $t6, 272($t0)
syntax_publish_start:
    move $fp, $zero
syntax_publish:
    daddu $t0, $sp, $fp
    lbu $t1, 272($t0)
    ld $t0, 1488($sp)
    daddu $t0, $t0, $fp
    sb $t1, 0($t0)
    daddiu $fp, $fp, 1
    ld $t0, 1480($sp)
    sltu $t0, $fp, $t0
    bnezc $t0, syntax_publish
    nop
    ld $v1, 1472($sp)
    ld $t0, 1424($sp)
    daddu $t0, $t0, $v1
    sd $t0, 16($s1)
    move $v0, $zero
    bc syntax_restore
    nop
syntax_no_space:
    li $v0, 2
    move $v1, $zero
    bc syntax_restore
    nop
syntax_child_failure:
    li $v0, 3
    move $v1, $zero
syntax_restore:
    ld $s0, 0($sp)
    ld $s1, 8($sp)
    ld $s2, 16($sp)
    ld $s3, 24($sp)
    ld $s4, 32($sp)
    ld $s5, 40($sp)
    ld $s6, 48($sp)
    ld $s7, 56($sp)
    ld $fp, 64($sp)
    ld $ra, 72($sp)
    daddiu $sp, $sp, 1520
syntax_early_return:
    jrc $ra
    nop
syntax_add_plan:
    dsll $t0, $fp, 4
    daddu $t0, $t0, $sp
    sd $a1, 144($t0)
    sd $a2, 152($t0)
    daddiu $fp, $fp, 1
    jrc $ra
    nop

h263_emit_coefficients:
    li $v0, 1
    move $v1, $zero
    andi $t0, $a0, 7
    bnezc $t0, coeff_early_return
    nop
    daddiu $t0, $a0, 32
    sltu $t0, $t0, $a0
    bnezc $t0, coeff_early_return
    nop
    daddiu $sp, $sp, -1440
    sd $s0, 0($sp)
    sd $s1, 8($sp)
    sd $s2, 16($sp)
    sd $s3, 24($sp)
    sd $s4, 32($sp)
    sd $s5, 40($sp)
    sd $s6, 48($sp)
    sd $s7, 56($sp)
    sd $fp, 64($sp)
    sd $ra, 72($sp)
    ld $s0, 0($a0)
    ld $s1, 8($a0)
    ld $s2, 16($a0)
    ld $t0, 24($a0)
    bnezc $t0, coeff_restore
    nop
    sltiu $t0, $s2, 2
    beqzc $t0, coeff_restore
    nop
    andi $t0, $s0, 1
    bnezc $t0, coeff_restore
    nop
    daddiu $t0, $s0, 128
    sltu $t0, $t0, $s0
    bnezc $t0, coeff_restore
    nop
    andi $t0, $s1, 7
    bnezc $t0, coeff_restore
    nop
    daddiu $t0, $s1, 24
    sltu $t0, $t0, $s1
    bnezc $t0, coeff_restore
    nop
    ld $s4, 0($s1)
    ld $s5, 8($s1)
    ld $s3, 16($s1)
    dli $t0, 1048576
    sltu $t0, $t0, $s5
    bnezc $t0, coeff_restore
    nop
    daddu $t0, $s4, $s5
    sltu $t0, $t0, $s4
    bnezc $t0, coeff_restore
    nop
    dsll $t0, $s5, 3
    sltu $t0, $t0, $s3
    bnezc $t0, coeff_restore
    nop
    move $s6, $zero
coeff_validate_loop:
    dsll $t0, $s6, 1
    daddu $t0, $t0, $s0
    lh $t1, 0($t0)
    bnezc $s6, coeff_validate_ordinary
    nop
    beqzc $s2, coeff_validate_ordinary
    nop
    daddiu $t0, $t1, -1
    sltiu $t0, $t0, 255
    beqzc $t0, coeff_restore
    nop
    li $t0, 128
    beqc $t1, $t0, coeff_restore
    nop
    bc coeff_validate_next
    nop
coeff_validate_ordinary:
    daddiu $t0, $t1, 127
    sltiu $t0, $t0, 255
    beqzc $t0, coeff_restore
    nop
coeff_validate_next:
    daddiu $s6, $s6, 1
    sltiu $t0, $s6, 64
    bnezc $t0, coeff_validate_loop
    nop
    li $s7, 63
coeff_find_last:
    slt $t0, $s7, $s2
    bnezc $t0, coeff_last_ready
    nop
    dsll $t0, $s7, 3
    dla $t1, coeff_scan
    daddu $t0, $t0, $t1
    ld $t0, 0($t0)
    dsll $t0, $t0, 1
    daddu $t0, $t0, $s0
    lh $t0, 0($t0)
    bnezc $t0, coeff_last_ready
    nop
    daddiu $s7, $s7, -1
    bc coeff_find_last
    nop
coeff_last_ready:
    move $fp, $zero
    sd $zero, 1336($sp)
    sd $zero, 1352($sp)
    beqzc $s2, coeff_plan_start
    nop
    lhu $t0, 0($s0)
    sd $t0, 112($sp)
    li $t0, 8
    sd $t0, 120($sp)
    sd $t0, 1336($sp)
    li $fp, 1
coeff_plan_start:
    move $s6, $s2
coeff_plan_loop:
    slt $t0, $s7, $s6
    bnezc $t0, coeff_plan_ready
    nop
    dsll $t0, $s6, 3
    dla $t1, coeff_scan
    daddu $t0, $t0, $t1
    ld $t0, 0($t0)
    dsll $t0, $t0, 1
    daddu $t0, $t0, $s0
    lh $t2, 0($t0)
    bnezc $t2, coeff_nonzero
    nop
    ld $t0, 1352($sp)
    daddiu $t0, $t0, 1
    sd $t0, 1352($sp)
    bc coeff_plan_next
    nop
coeff_nonzero:
    move $t5, $t2
    bgezc $t5, coeff_magnitude_ready
    nop
    dsubu $t5, $zero, $t5
coeff_magnitude_ready:
    xor $t6, $s6, $s7
    sltiu $t6, $t6, 1
    ld $t7, 1352($sp)
    slt $t8, $t2, $zero
    dli $t0, 98304
    dsll $t1, $t6, 14
    or $t0, $t0, $t1
    dsll $t1, $t7, 8
    or $t0, $t0, $t1
    andi $t1, $t2, 255
    or $t0, $t0, $t1
    li $t1, 22
    dla $t3, coeff_codes
    li $t4, 102
coeff_lookup_loop:
    ld $t9, 0($t3)
    bnec $t9, $t6, coeff_lookup_next
    nop
    ld $t9, 8($t3)
    bnec $t9, $t7, coeff_lookup_next
    nop
    ld $t9, 16($t3)
    bnec $t9, $t5, coeff_lookup_next
    nop
    ld $t0, 24($t3)
    or $t0, $t0, $t8
    ld $t1, 32($t3)
    bc coeff_plan_store
    nop
coeff_lookup_next:
    daddiu $t3, $t3, 40
    daddiu $t4, $t4, -1
    bnezc $t4, coeff_lookup_loop
    nop
coeff_plan_store:
    dsll $t2, $fp, 4
    daddu $t2, $t2, $sp
    sd $t0, 112($t2)
    sd $t1, 120($t2)
    ld $t0, 1336($sp)
    daddu $t0, $t0, $t1
    sd $t0, 1336($sp)
    daddiu $fp, $fp, 1
    sd $zero, 1352($sp)
coeff_plan_next:
    daddiu $s6, $s6, 1
    bc coeff_plan_loop
    nop
coeff_plan_ready:
    sd $fp, 1328($sp)
    ld $t0, 1336($sp)
    dsll $t1, $s5, 3
    dsubu $t1, $t1, $s3
    sltu $t1, $t1, $t0
    bnezc $t1, coeff_no_space
    nop
    beqzc $t0, coeff_empty
    nop
    dsrl $t1, $s3, 3
    daddu $t1, $t1, $s4
    sd $t1, 1360($sp)
    andi $t1, $s3, 7
    daddu $t0, $t0, $t1
    daddiu $t0, $t0, 7
    dsrl $t0, $t0, 3
    sd $t0, 1344($sp)
    move $s6, $zero
coeff_stage_bytes:
    ld $t0, 1360($sp)
    daddu $t0, $t0, $s6
    lbu $t1, 0($t0)
    daddu $t0, $sp, $s6
    sb $t1, 1136($t0)
    daddiu $s6, $s6, 1
    ld $t0, 1344($sp)
    sltu $t0, $s6, $t0
    bnezc $t0, coeff_stage_bytes
    nop
    daddiu $t0, $sp, 1136
    sd $t0, 80($sp)
    li $t0, 192
    sd $t0, 88($sp)
    andi $t0, $s3, 7
    sd $t0, 96($sp)
    move $s6, $zero
coeff_emit_plan:
    daddiu $a0, $sp, 80
    dsll $t0, $s6, 4
    daddu $t0, $t0, $sp
    ld $a1, 112($t0)
    ld $a2, 120($t0)
    balc h263_write_bits
    nop
    bnezc $v0, coeff_child_failure
    nop
    daddiu $s6, $s6, 1
    ld $t0, 1328($sp)
    sltu $t0, $s6, $t0
    bnezc $t0, coeff_emit_plan
    nop
    move $s6, $zero
coeff_publish:
    daddu $t0, $sp, $s6
    lbu $t1, 1136($t0)
    ld $t0, 1360($sp)
    daddu $t0, $t0, $s6
    sb $t1, 0($t0)
    daddiu $s6, $s6, 1
    ld $t0, 1344($sp)
    sltu $t0, $s6, $t0
    bnezc $t0, coeff_publish
    nop
    ld $v1, 1336($sp)
    daddu $t0, $s3, $v1
    sd $t0, 16($s1)
    move $v0, $zero
    bc coeff_restore
    nop
coeff_empty:
    move $v0, $zero
    move $v1, $zero
    bc coeff_restore
    nop
coeff_no_space:
    li $v0, 2
    move $v1, $zero
    bc coeff_restore
    nop
coeff_child_failure:
    li $v0, 3
    move $v1, $zero
coeff_restore:
    ld $s0, 0($sp)
    ld $s1, 8($sp)
    ld $s2, 16($sp)
    ld $s3, 24($sp)
    ld $s4, 32($sp)
    ld $s5, 40($sp)
    ld $s6, 48($sp)
    ld $s7, 56($sp)
    ld $fp, 64($sp)
    ld $ra, 72($sp)
    daddiu $sp, $sp, 1440
coeff_early_return:
    jrc $ra
    nop

h263_write_bits:
    li $v0, 1
    move $v1, $zero
    andi $t0, $a0, 7
    bnezc $t0, writer_return
    nop
    daddiu $t0, $a0, 24
    sltu $t0, $t0, $a0
    bnezc $t0, writer_return
    nop
    ld $t0, 0($a0)
    ld $t1, 8($a0)
    ld $t2, 16($a0)
    dli $t3, 1048576
    sltu $t3, $t3, $t1
    bnezc $t3, writer_return
    nop
    daddu $t3, $t0, $t1
    sltu $t3, $t3, $t0
    bnezc $t3, writer_return
    nop
    dsll $t1, $t1, 3
    sltu $t3, $t1, $t2
    bnezc $t3, writer_return
    nop
    sltiu $t3, $a2, 33
    beqzc $t3, writer_return
    nop
    dsrlv $t3, $a1, $a2
    bnezc $t3, writer_return
    nop
    dsubu $t1, $t1, $t2
    sltu $t1, $t1, $a2
    bnezc $t1, writer_no_space
    nop
    move $v0, $zero
    beqzc $a2, writer_return
    nop
    move $t3, $a2
writer_chunk_loop:
    dsrl $t1, $t2, 3
    daddu $t1, $t1, $t0
    andi $t5, $t2, 7
    li $t4, 8
    dsubu $t4, $t4, $t5
    sltu $t9, $t3, $t4
    beqzc $t9, writer_take_ready
    nop
    move $t4, $t3
writer_take_ready:
    dsubu $t7, $t3, $t4
    dsrlv $t8, $a1, $t7
    li $t6, 1
    dsllv $t6, $t6, $t4
    daddiu $t6, $t6, -1
    and $t8, $t8, $t6
    li $t7, 8
    dsubu $t7, $t7, $t5
    dsubu $t7, $t7, $t4
    dsllv $t8, $t8, $t7
    dsllv $t6, $t6, $t7
    nor $t6, $t6, $zero
    lbu $t9, 0($t1)
    and $t9, $t9, $t6
    or $t9, $t9, $t8
    sb $t9, 0($t1)
    daddu $t2, $t2, $t4
    dsubu $t3, $t3, $t4
    bnezc $t3, writer_chunk_loop
    nop
    sd $t2, 16($a0)
    move $v1, $a2
    jrc $ra
    nop
writer_no_space:
    li $v0, 2
writer_return:
    jrc $ra
    nop
