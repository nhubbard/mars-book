# Internal helpers use the owning inflater frame.
build_tree:
# BEGIN huff_tree_clear
    b inflate_missing
    nop
# END huff_tree_clear
huff_tree_cleared:
    nop
tree_count:
# BEGIN huff_tree_count
    b inflate_missing
    nop
# END huff_tree_count
huff_tree_counted:
    nop
# BEGIN huff_tree_prefix
    b inflate_missing
    nop
# END huff_tree_prefix
huff_tree_prefix_ready:
    nop
# BEGIN huff_tree_shape
    b inflate_missing
    nop
# END huff_tree_shape
tree_shape_ok:
huff_tree_shape_ready:
    nop
# BEGIN huff_tree_symbols
    b inflate_missing
    nop
# END huff_tree_symbols
huff_tree_symbols_ready:
    nop
    jr $ra
    nop
decode_symbol:
# BEGIN huff_symbol_state
    b inflate_missing
    nop
# END huff_symbol_state
huff_symbol_state_ready:
    nop
decode_bit:
# BEGIN huff_symbol_refill
    b inflate_missing
    nop
# END huff_symbol_refill
huff_symbol_refill_fallthrough:
    nop
decode_has_byte:
# BEGIN huff_symbol_step
    b inflate_missing
    nop
# END huff_symbol_step
huff_symbol_step_unreached:
    nop
decode_found:
# BEGIN huff_symbol_found
    b inflate_missing
    nop
# END huff_symbol_found
huff_symbol_ready:
    nop
    jr $ra
    nop
