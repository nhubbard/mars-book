.data
stream_guard: .word 0x13579bdf
stream: .byte -48,15,4,1,0,-18,-88,2
    .byte -91:184
stream_end: .word 0x2468ace0
byte_count: .word 8
reading_limit: .word 32
input_used: .word -1
reading_count: .word -1
last_reading: .word -1
status: .word -1
caller_end: .word 0x31415926
.text
.globl main
main:
    dli $t0, -16
    and $sp, $sp, $t0
    dla $a0, stream
    dla $t0, byte_count
    lw $a1, 0($t0)
    dla $t0, reading_limit
    lw $a2, 0($t0)
    balc scan_delta
    nop
scan_ready:
    nop
    dla $t0, input_used
    sw $v0, 0($t0)
    sw $v1, 4($t0)
    sw $a2, 8($t0)
    sw $a3, 12($t0)
results_ready:
    nop
    bc done
    nop
function_return:
    nop
done:
    li $v0, 10
    syscall

# Validate bytes without writing decoded readings.
# a0=byte pointer; a1=byte length; a2=reading limit.
# Return v0=committed bytes; v1=readings; a2=last valid reading (or zero); a3=status.
scan_delta:
    break
