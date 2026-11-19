.align 4
seek_frame: .byte -91:16448
.align 3
seek_context: .dword 0, 0, 0, 0, 16384, 0, 16384
    .dword -6510615555426900571, -6510615555426900571, -6510615555426900571
    .dword -6510615555426900571, -6510615555426900571
seek_context_guard: .byte -91:32
seek_target: .dword 6
seek_last_status: .dword -1
seek_last_count: .dword -1
seek_success_message: .asciiz "Requested video frame ready.\n"
seek_missing_message: .asciiz "Video seek is still under construction.\n"
seek_failure_message: .asciiz "Unable to seek video frame.\n"
