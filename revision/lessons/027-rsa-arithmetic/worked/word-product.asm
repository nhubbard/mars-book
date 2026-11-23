# Compare interpretations of one word and retain its entire unsigned product.
.data
.align 2
limb: .word 0xffffffff
.text
.globl main
main:
    dla $t9, limb
    lwu $t0, 0($t9)
    lw $t1, 0($t9)
loaded_limb:
    dmulu $t2, $t0, $t0
full_product:
    dmodu $t3, $t2, $t0
    dmod $t5, $t2, $t0
    mulu $t4, $t0, $t0
product_interpretations:
    li $v0, 10
    syscall
