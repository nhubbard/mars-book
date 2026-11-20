.data 0x100000000
.align 4
h263Prediction_current: .dword -6510615555426900571:3590
h263Prediction_reference: .dword -6510615555426900571:4104
.align 3
h263Prediction_request: .dword -6510615555426900571:8
.align 4
h263Prediction_output: .dword -6510615555426900571:28
h263Prediction_stack: .dword -6510615555426900571:2052

.text
h263Prediction_return:
    nop
