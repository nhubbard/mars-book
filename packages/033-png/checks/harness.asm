# Public grading storage only: no decoder implementation or quiz keys.
# The separate wide-data window does not overlap the main caller's heap.
.data 0x100000000
.align 4
pngCheck_source: .space 65568
pngCheck_compressed: .space 65568
pngCheck_rows: .space 65568
pngCheck_frame: .space 65568
pngCheck_context: .space 128
.align 4
pngCheck_stack: .space 32768
pngCheck_path: .asciiz "image.png"
.text
pngCheck_return:
    nop
