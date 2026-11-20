.data 0x100000000
.align 4
h263Picture_current: .byte -91:38048
h263Picture_reference: .byte -91:38048
h263Picture_reconstruction: .byte -91:38048
h263Picture_workspace: .byte -91:169120
h263Picture_buffer: .byte -91:131104
.align 3
h263Picture_request: .byte -91:128
h263Picture_context: .byte -91:56
.align 4
h263Picture_stack: .byte -91:65568
.text
h263Picture_return:
    nop
