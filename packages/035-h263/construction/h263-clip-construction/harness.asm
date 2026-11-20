.data 0x100000000
.align 4
.align 4
h263Clip_raw: .byte -91:38048
.align 4
h263Clip_a: .byte -91:38048
.align 4
h263Clip_b: .byte -91:38048
.align 4
h263Clip_workspace: .byte -91:169120
.align 4
h263Clip_encoded: .byte -91:131104
.align 4
h263Clip_input_path: .byte -91:4128
.align 4
h263Clip_stream_path: .byte -91:4128
.align 4
h263Clip_reconstruction_path: .byte -91:4128
.align 4
h263Clip_request: .byte -91:136
.align 4
h263Clip_progress: .byte -91:56
.align 4
h263Clip_stack: .byte -91:65568
.text
h263Clip_return:
    nop
