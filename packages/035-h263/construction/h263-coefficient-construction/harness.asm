.data 0x100000000
.align 4
h263Coeff_levels: .byte -91:160
.align 3
h263Coeff_request: .byte -91:64
h263Coeff_context: .byte -91:56
h263Coeff_buffer: .dword -6510615555426900571:131076
.align 4
h263Coeff_stack: .byte -91:32800
.text
h263Coeff_return:
    nop
