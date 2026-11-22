# mars:tool cache blocks=8 wordsPerBlock=4 placement=full ways=8 replacement=lru logging=true
.eqv ROWS 3
.eqv COLS 5
.eqv METHOD 0
.data
.align 6
input:
    .word 0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15
    .word 16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31
    .word 32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47
    .word 48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63
    .word 64,65,66,67,68,69,70,71,72,73,74,75,76,77,78,79
    .word 80,81,82,83,84,85,86,87,88,89,90,91,92,93,94,95
    .word 96,97,98,99,100,101,102,103,104,105,106,107,108,109,110,111
    .word 112,113,114,115,116,117,118,119,120,121,122,123,124,125,126,127
    .word 128,129,130,131,132,133,134,135,136,137,138,139,140,141,142,143
    .word 144,145,146,147,148,149,150,151,152,153,154,155,156,157,158,159
    .word 160,161,162,163,164,165,166,167,168,169,170,171,172,173,174,175
    .word 176,177,178,179,180,181,182,183,184,185,186,187,188,189,190,191
    .word 192,193,194,195,196,197,198,199,200,201,202,203,204,205,206,207
    .word 208,209,210,211,212,213,214,215,216,217,218,219,220,221,222,223
    .word 224,225,226,227,228,229,230,231,232,233,234,235,236,237,238,239
    .word 240,241,242,243,244,245,246,247,248,249,250,251,252,253,254,255
input_guard: .word 0x13579bdf
.align 6
output: .space 1024
output_guard: .word 0x2468ace0
copied: .word -1
metadata_guard: .word 0x76543210
.text
.globl main
main:
    dli $t0, -16
    and $sp, $sp, $t0
    la $a0, input
    la $a1, output
    li $a2, ROWS
    li $a3, COLS
    la $t9, transpose_naive
    li $t0, METHOD
    beqzc $t0, transpose_begin
    nop
    la $t9, transpose_tiled
transpose_begin:
    jalrc $t9
transpose_done:
    sw $v0, copied
    li $v0, 10
    syscall

# a0/a1 = separate full pointers; a2/a3 = rows/columns, each 0..16.
transpose_naive:
    mul $v0, $a2, $a3
    beqzc $a2, naive_return
    nop
    beqzc $a3, naive_return
    nop
    sll $v1, $a2, 2
    move $t0, $zero
    move $t2, $a0
    move $t3, $a1
naive_row:
    move $t1, $zero
    move $t4, $t3
naive_cell:
    lw $t5, 0($t2)
    sw $t5, 0($t4)
    daddiu $t2, $t2, 4
    daddu $t4, $t4, $v1
    addiu $t1, $t1, 1
    bltc $t1, $a3, naive_cell
    nop
    daddiu $t3, $t3, 4
    addiu $t0, $t0, 1
    bltc $t0, $a2, naive_row
    nop
naive_return:
    jrc $ra

transpose_tiled:
    mul $v0, $a2, $a3
    beqzc $a2, tiled_return
    nop
    beqzc $a3, tiled_return
    nop
    sll $v1, $a2, 2
    move $t0, $zero
next_tile_row:
    move $t1, $zero
tile_begin:
    addiu $t2, $t0, 4
    blec $t2, $a2, tile_rows_ready
    nop
    move $t2, $a2
tile_rows_ready:
    addiu $t3, $t1, 4
    blec $t3, $a3, tile_columns_ready
    nop
    move $t3, $a3
tile_columns_ready:
    move $t4, $t0
tile_row:
    mul $t6, $t4, $a3
    addu $t6, $t6, $t1
    sll $t6, $t6, 2
    daddu $t6, $a0, $t6
    mul $t7, $t1, $a2
    addu $t7, $t7, $t4
    sll $t7, $t7, 2
    daddu $t7, $a1, $t7
    move $t5, $t1
tile_cell:
    lw $t8, 0($t6)
    sw $t8, 0($t7)
    daddiu $t6, $t6, 4
    daddu $t7, $t7, $v1
    addiu $t5, $t5, 1
    bltc $t5, $t3, tile_cell
    nop
    addiu $t4, $t4, 1
    bltc $t4, $t2, tile_row
    nop
    addiu $t1, $t1, 4
    bltc $t1, $a3, tile_begin
    nop
    addiu $t0, $t0, 4
    bltc $t0, $a2, next_tile_row
    nop
tiled_return:
    jrc $ra
