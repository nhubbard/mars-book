.data 0x100000000
.align 4
h263Quant_input: .byte -91:160
.align 3
h263Quant_request: .byte -91:32
.align 4
h263Quant_output: .byte -91:288
h263Quant_stack: .byte -91:16416
.text
h263Quant_return:
    nop
