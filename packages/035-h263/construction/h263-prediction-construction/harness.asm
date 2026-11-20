.data 0x100000000
.align 4
h263Prediction_current: .byte -91:28720
h263Prediction_reference: .byte -91:32832
.align 3
h263Prediction_request: .byte -91:64
.align 4
h263Prediction_output: .byte -91:224
h263Prediction_stack: .byte -91:16416
.text
h263Prediction_return:
    nop
