.data 0x100000000
.align 4
h263Motion_current: .byte -91:61488
h263Motion_reference: .byte -91:585952
.align 3
h263Motion_request: .byte -91:64
h263Motion_output: .byte -91:56
.align 4
h263Motion_stack: .byte -91:16416
.text
h263Motion_return:
    nop
