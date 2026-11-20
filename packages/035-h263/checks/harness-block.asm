.data 0x100000000
.align 4
h263Block_current: .dword -6510615555426900571:3590
h263Block_prediction: .dword -6510615555426900571:3590
.align 3
h263Block_request: .dword -6510615555426900571:8
.align 4
h263Block_output: .dword -6510615555426900571:60
h263Block_stack: .dword -6510615555426900571:4100

.text
h263Block_return:
    nop
