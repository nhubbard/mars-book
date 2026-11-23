.data
source_guard: .word 0x13579bdf
source: .byte -5,-1,-1,-1
    .byte -91:188
source_end: .word 0x2468ace0
destination_guard: .word 0x31415926
destination: .byte -91:192
destination_end: .word 0x27182818
count: .dword 4
capacity: .dword 6
status: .word -1
.align 3
written: .dword -1
caller_end: .word 0x11223344
.align 4
workspace: .byte -91:256
alphabet: .ascii "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_"
.text
.globl main
main:
    dla $sp, workspace
    daddiu $sp, $sp, 256
    dla $a0, source
    dla $t0, count
    ld $a1, 0($t0)
    dla $a2, destination
    dla $t0, capacity
    ld $a3, 0($t0)
url_call:
    balc b64url_encode
    nop
url_ready:
    nop
    dla $t0, status
    sw $v0, 0($t0)
    dla $t0, written
    sd $v1, 0($t0)
results_ready:
    nop
    bc done
    nop
function_return:
    nop
done:
    li $v0, 10
    syscall

# Implement the URL-safe alphabet without padding for up to 64 input bytes.
# Full source/count/destination/capacity in a0..a3. v0=status, v1=length or zero.
b64url_encode:
    break
