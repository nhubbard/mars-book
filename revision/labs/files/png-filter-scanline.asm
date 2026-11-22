# Independent PNG RGB8 forward-filter exercise for Chapter 24.
# Replace only the unfinished function; the demo encodes the Average trace row.
.data
.align 3
filter_settings: .dword 2, 3, 7
    .byte -91:8
current_rgb: .byte 25,35,45,55,65,75
    .byte -91:1546
previous_rgb: .byte 20,30,40,50,60,70
    .byte -91:1546
filtered_row: .byte -91:1556
.align 3
filter_status: .word -1
.align 3
filter_count: .dword 0
filter_workspace: .space 4096
.text
.globl main
main:
    dla $a0, current_rgb
    dla $a1, previous_rgb
    dla $a2, filtered_row
    dla $a3, filter_settings
    balc png_filter_scanline
    nop
filter_demo_return:
    dla $t0, filter_status
    sw $v0, 0($t0)
    dla $t0, filter_count
    sd $v1, 0($t0)
    li $v0, 10
    syscall
filter_function_return:
    nop
# a0=current RGB; a1=previous RGB or zero; a2=output; a3=settings.
# Settings: native u64 width, filter selector, unsigned output capacity.
# Success: v0=0, v1=1+3*width. Failure: v0=1, v1=0, no caller writes.
# Width 1..512; selector 0..4; output and settings are disjoint from inputs.
# Inputs may coincide. Previous RGB is unused for None/Sub or a null pointer.
# Preserve full saved registers, fp, sp and return continuation. No I/O.
png_filter_scanline:
    break
