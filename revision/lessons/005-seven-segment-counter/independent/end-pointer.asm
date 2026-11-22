# mars:tool digitalLab
.eqv RIGHT_DISPLAY 0x00000000ffff0010
.data
.align 2
glyphs:       .byte 0x3f, 0x06, 0x5b, 0x4f, 0x66, 0x6d, 0x7d, 0x07
              .byte 0x7f, 0x6f, 0x77, 0x7c, 0x39, 0x5e, 0x79, 0x71
glyph_guard:  .word 0x13579bdf
first_digit:  .word 0
digit_count:  .word 16
inputs_guard: .word 0x2468ace0

.text
.globl main
main:
    # Retain a current pointer and an exclusive end pointer.
    # Read the supplied input block; preserve it and issue the stated byte stores.
    # This starter only exits, so it passes empty cases but not nonempty ranges.
    li $v0, 10
    syscall
