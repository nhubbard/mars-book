# Independent reconstruction boundary for Chapter 26.
# h263_reconstruct8(a0=prediction bytes,a1=signed inverse halfwords,
#                   a2=output bytes,a3=native stride settings).
# Eight rows of eight samples; settings are three native unsigned doublewords:
# prediction byte stride 8..4096, inverse byte stride even 16..4096,
# output byte stride 8..4096. Settings are eight-byte aligned; inverse is
# two-byte aligned. Byte pointers need no further alignment. All selected
# extents, including the 24-byte settings object, must not wrap.
# Output is disjoint from inverse/settings. It may exactly alias prediction
# with identical strides, otherwise it is disjoint from prediction.
# Inputs/settings disjoint from each other except the stated output alias.
# Success 0/clipped-count in v0/v1; publish clip(prediction+inverse,0,255).
# Inverse samples must all lie in [-256,255]. Invalid metadata returns 1/0
# before input reads; invalid sample returns 2/0 before output publication.
# Read no row padding; retain all inputs/settings except the exact output alias.
# Preserve s0..s7, fp,gp,sp and the supplied return continuation. Stack is
# sixteen-byte aligned with 8192 private bytes. No console or file I/O.
.data
.align 4
recon_prediction: .byte -91:28720
recon_inverse: .byte -91:28752
recon_output: .byte -91:28720
.align 3
recon_settings: .dword 8,16,8,-6510615555426900571,-6510615555426900571
recon_status: .dword -1
recon_count: .dword -1
.align 4
recon_stack: .space 8192
.text
.globl main
main:
    dla $sp, recon_stack
    daddiu $sp, $sp, 8192
    dla $a0, recon_prediction
    dla $a1, recon_inverse
    dla $a2, recon_output
    dla $a3, recon_settings
    li $t0, 64
    move $t1, $a0
    move $t2, $a1
recon_demo_setup:
    li $t3, 128
    sb $t3, 0($t1)
    andi $t3, $t0, 7
    dsll $t3, $t3, 1
    dla $t4, recon_demo_residuals
    daddu $t4, $t4, $t3
    lh $t3, 0($t4)
    sh $t3, 0($t2)
    daddiu $t1, $t1, 1
    daddiu $t2, $t2, 2
    daddiu $t0, $t0, -1
    bnezc $t0, recon_demo_setup
    nop
    balc h263_reconstruct8
    nop
recon_demo_return:
    dla $t0, recon_status
    sd $v0, 0($t0)
    dla $t0, recon_count
    sd $v1, 0($t0)
    li $v0, 10
    syscall
recon_function_return:
    break
.data
.align 1
recon_demo_residuals: .half -129,255,128,127,126,0,-127,-128
.text
h263_reconstruct8:
    # Replace this deliberate unfinished trap with your implementation.
    break
    jrc $ra
    nop
