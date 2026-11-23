# mars:tool bitmap width=64 height=64 unitWidth=8 unitHeight=8 base=static_data
.data
.align 2
frame: .space 256
frame_guard: .word 0x13579bdf
walls: .byte 0, 0, 1, 1, 1, 1, 1, 1
    .byte 0, 0, 1, 1, 1, 1, 1, 1
    .byte 1, 1, 1, 1, 1, 1, 1, 1
    .byte 1, 1, 1, 1, 1, 1, 1, 1
    .byte 1, 1, 1, 1, 1, 1, 1, 1
    .byte 1, 1, 1, 1, 1, 1, 1, 1
    .byte 1, 1, 1, 1, 1, 1, 1, 1
    .byte 1, 1, 1, 1, 1, 1, 1, 1
walls_guard: .word 0x2468ace0
start: .word 0
start_guard: .word 0x10203040
goal: .word 9
goal_guard: .word 0x50607080
states: .space 64
states_guard: .word 0x31415926
parents: .space 64
parents_guard: .word 0x27182818
distance: .space 64
distance_guard: .word 0x12345678
queue: .space 64
queue_guard: .word 0x76543210
path: .space 64
path_guard: .word 0x11223344
head: .word -1
tail: .word -1
current: .word -1
phase: .word -1
result: .word -1
length: .word -1
metadata_guard: .word 0x55667788
dx: .byte 0, 1, 0, -1
dy: .byte -1, 0, 1, 0
.text
.globl main
main:
    # Implement initialization, FIFO search and path reconstruction here.
    # Publish phases 0, 1 and 2 through render; keep inputs and guards intact.
    # Neighbor order and private function/label organization are your choice.
    break

done:
    li $v0, 10
    syscall

render:
    # Paint all 64 words from walls, states and path; preserve saved state.
    # Reach maze_ready once with the completed frame, then return.
    break
maze_ready:
    jrc $ra
