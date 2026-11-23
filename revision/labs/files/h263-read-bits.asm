# Independent bounded MSB-first bit reader for Chapter 27.
# h263_read_bits(a0=context,a1=width) returns status/value in v0/v1.
# Context: four native unsigned doublewords: input byte pointer, capacity bytes
# (0..1048576), valid bit count (0..8*capacity), cursor (0..valid bits).
# Context is eight-byte aligned; its 32-byte extent and the input capacity
# extent must not wrap. Positive-capacity input is mapped and disjoint from
# context, with no pointer alignment requirement. Zero capacity never reads input.
# Width is full-width 0..32. Metadata error returns 1/0 before any input read;
# insufficient remaining valid bits returns 2/0, retaining all caller state.
# Success returns 0/right-aligned unsigned field, MSB first, and advances cursor.
# Width zero requires valid metadata, returns 0/0 and makes no input/context write.
# Nonempty reads touch only bytes intersecting the selected field. Ignore unused
# bits after valid count; retain all input bytes and all context fields except
# cursor on nonempty success. Preserve s0..s7, fp,gp,sp and return continuation.
# Stack: sixteen-byte aligned, 8192 private bytes. No console/file I/O.
.data
.align 4
reader_bytes: .byte -70,53
    .space 1048542
reader_bytes_tail: .space 64
.align 3
reader_context: .dword 0,2,12,3,-6510615555426900571,-6510615555426900571
reader_status: .dword -1
reader_value: .dword -1
.align 4
reader_stack: .space 8192
.text
.globl main
main:
    dla $sp, reader_stack
    daddiu $sp, $sp, 8192
    dla $a0, reader_context
    dla $t0, reader_bytes
    sd $t0, 0($a0)
    li $a1, 9
    balc h263_read_bits
    nop
reader_demo_return:
    dla $t0, reader_status
    sd $v0, 0($t0)
    dla $t0, reader_value
    sd $v1, 0($t0)
    li $v0, 10
    syscall
reader_function_return:
    break
h263_read_bits:
    # Replace this deliberate unfinished trap with your implementation.
    break
    jrc $ra
    nop
