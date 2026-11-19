.align 3
io_path: .asciiz "animation.mlv"
    .space 114
io_path_end:
.align 3
io_capacity: .dword 7344152
io_load_context: .dword -6510615555426900571, -6510615555426900571
io_context_guard: .byte -91:32
io_loaded_count: .dword -1
io_last_status: .dword -1
io_last_count: .dword -1
io_success_message: .asciiz "Video file indexed.\n"
io_missing_message: .asciiz "Video reader is still under construction.\n"
io_load_failure_message: .asciiz "Unable to load video.\n"
