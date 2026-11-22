.data
.align 2
input_guard: .word 0x13579bdf
input: .byte 0x41,0,0x80,0x0a,0x42
    .space 251
input_end_guard: .word 0x2468ace0
line_a: .word -1,-1
line_a_guard: .word 0x31415926
line_b: .word -1,-1
line_b_guard: .word 0x27182818
available: .word 5
status: .word -1
.align 3
payload_address: .dword 0
state_end: .word 0x76543210
.text
.globl main
main:
    dli $t0, -16
    and $sp, $sp, $t0
    la $a0, input
    lw $a1, available
    la $a2, line_a
    balc read_line
line_ready:
    nop
    sw $v0, status
    sd $v1, payload_address
    bc line_done
line_function_return:
    nop
line_done:
    li $v0, 10
    syscall

# a0 = input window; a1 = available bytes; a2 = selected 8-byte output.
# Read one LF-delimited binary record with at most 32 payload bytes.
# Success: v0 = payload length+1, v1 = a0; output = [length, sum mod 256].
# Failure: v0 = -1 need more or -2 too long, v1 = 0, no output writes.
read_line:
    break
