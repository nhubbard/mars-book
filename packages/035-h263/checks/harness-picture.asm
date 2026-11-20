.data 0x100000000
.align 4
h263Picture_current: .dword -6510615555426900571:4756
h263Picture_reference: .dword -6510615555426900571:4756
h263Picture_reconstruction: .dword -6510615555426900571:4756
h263Picture_workspace: .dword -6510615555426900571:21140
h263Picture_buffer: .dword -6510615555426900571:16388
.align 3
h263Picture_request: .dword -6510615555426900571:16
h263Picture_context: .dword -6510615555426900571:7
.align 4
h263Picture_stack: .dword -6510615555426900571:8196

.text
h263Picture_return:
    nop
