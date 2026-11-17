# mars:tool bitmap width=64 height=64 unitWidth=8 unitHeight=8 base=static_data
.data
.align 2
frame: .space 256
frame_guard: .word 0x13579bdf
values: .word 2, -2, 3, -4, 0, 1, -3, -1
values_guard: .word 0x2468ace0
count: .word 8
count_guard: .word 0x10203040
algorithm: .word 0
algorithm_guard: .word 0x50607080
phase: .word -1
phase_guard: .word 0x12345678
swap_count: .word -1
swaps_guard: .word 0x31415926
.text
.globl main
main:
# BEGIN setup
# END setup
sort_begin:
# BEGIN dispatch
# END dispatch
insertion_sort:
# BEGIN insertion_init
# END insertion_init
insertion_inner:
# BEGIN insertion_inner
# END insertion_inner
insertion_next:
# BEGIN insertion_next
# END insertion_next
selection_sort:
# BEGIN selection_init
# END selection_init
selection_scan:
# BEGIN selection_scan
# END selection_scan
selection_exchange:
# BEGIN selection_exchange
# END selection_exchange
selection_next:
# BEGIN selection_next
# END selection_next
sort_end:
# BEGIN final
# END final
done:
    li $v0, 10
    syscall
render:
# BEGIN render
# END render
sort_ready:
    jr $ra
    nop
