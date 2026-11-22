# mars:tool bitmap width=64 height=64 unitWidth=32 unitHeight=32 base=static_data
# MIPS64 Release 6, Default layout, little-endian. Read independent/README.md.
.data
.align 2
pixel: .word 0
neighbor: .word 0x13579bdf
copied_pixel: .word 0
second_guard: .word 0x2468ace0
prompt: .asciiz "Enter an RGB integer: "

.text
.globl main
main:
    # Write the input, storage and byte-change instructions here.
    # This starter only exits; it does not meet the contract.
    li $v0, 10
    syscall
