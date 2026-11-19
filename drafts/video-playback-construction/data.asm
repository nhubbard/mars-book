.align 4
playback_frame: .byte -91:16448
.align 3
playback_context: .dword 16384, 0, 2048, 0, 16384, 0
    .dword -6510615555426900571, -6510615555426900571, -6510615555426900571, -6510615555426900571
    .dword -6510615555426900571, -6510615555426900571
playback_context_guard: .byte -91:32
playback_success_message: .asciiz "Video file loaded and played.\n"
playback_missing_message: .asciiz "Video playback is still under construction.\n"
playback_failure_message: .asciiz "Unable to play video file.\n"
