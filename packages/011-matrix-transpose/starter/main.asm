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
    dla $a0, input
    dla $a1, output
    li $a2, ROWS
    li $a3, COLS
    dla $t9, transpose_naive
    li $t0, METHOD
    beq $t0, $zero, transpose_begin
    nop
    dla $t9, transpose_tiled
transpose_begin:
    jalr $t9
    nop
transpose_done:
    dla $t0, copied
    sw $v0, 0($t0)
    li $v0, 10
    syscall

transpose_naive:
# BEGIN naive
# END naive
naive_return:
    jr $ra
    nop

transpose_tiled:
# BEGIN tiled
# END tiled
tiled_return:
    jr $ra
    nop
