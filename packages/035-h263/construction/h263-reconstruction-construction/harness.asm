.data 0x100000000
.align 4
h263Block_current: .dword -6510615555426900571:3590
h263Block_prediction: .dword -6510615555426900571:3590
.align 3
h263Block_request: .byte -91:64
.align 4
h263Block_output: .byte -91:480
h263Block_stack: .byte -91:32800
.text
h263Block_return:
    nop
