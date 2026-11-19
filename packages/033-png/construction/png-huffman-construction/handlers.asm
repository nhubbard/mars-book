fixed_block:
# BEGIN huff_fixed
    b inflate_missing
    nop
# END huff_fixed
huff_fixed_ready:
    nop
    b build_data_trees
    nop
dynamic_block:
# BEGIN huff_dynamic_counts
    b inflate_missing
    nop
# END huff_dynamic_counts
huff_dynamic_counts_ready:
    nop
# BEGIN huff_clear_alphabet
    b inflate_missing
    nop
# END huff_clear_alphabet
huff_alphabet_cleared:
    nop
read_alphabet:
# BEGIN huff_read_alphabet
    b inflate_missing
    nop
# END huff_read_alphabet
huff_alphabet_ready:
    nop
# BEGIN huff_code_tree
    b inflate_missing
    nop
# END huff_code_tree
huff_code_tree_ready:
    nop
read_length:
    daddiu $a0, $fp, 928
    jal decode_symbol
    nop
# BEGIN huff_direct_length
    b inflate_missing
    nop
# END huff_direct_length
huff_direct_length_ready:
    nop
    b store_lengths
    nop
length_repeat:
# BEGIN huff_repeat16
    b inflate_missing
    nop
# END huff_repeat16
huff_repeat16_ready:
    nop
    b store_lengths
    nop
length_repeat_zero:
# BEGIN huff_repeat17
    b inflate_missing
    nop
# END huff_repeat17
huff_repeat17_ready:
    nop
    b store_lengths
    nop
length_repeat_long:
# BEGIN huff_repeat18
    b inflate_missing
    nop
# END huff_repeat18
huff_repeat18_ready:
    nop
store_lengths:
# BEGIN huff_store_lengths
    b inflate_missing
    nop
# END huff_store_lengths
huff_lengths_stored:
    nop
    bne $t0, $t3, read_length
    nop
# BEGIN huff_end_code
    b inflate_missing
    nop
# END huff_end_code
huff_end_code_ready:
    nop
    b build_data_trees
    nop

# Supplied calls establish the two table interfaces; their algorithms remain student work.
build_data_trees:
    ld $a1, 0($fp)
    daddiu $a2, $fp, 160
    daddiu $a0, $fp, 1096
    li $a3, 1
    jal build_tree
    nop
    ld $t0, 0($fp)
    daddiu $a0, $fp, 1096
    daddu $a0, $a0, $t0
    ld $a1, 8($fp)
    li $a3, 2
    daddiu $a2, $fp, 800
    jal build_tree
    nop
huff_data_trees_ready:
    nop
inflate_symbol:
    daddiu $a0, $fp, 160
    jal decode_symbol
    nop
# BEGIN huff_classify
    b inflate_missing
    nop
# END huff_classify
huff_classified:
    nop
# BEGIN huff_length
    b inflate_missing
    nop
# END huff_length
huff_length_ready:
    nop
# BEGIN huff_distance_symbol
    b inflate_missing
    nop
# END huff_distance_symbol
huff_distance_symbol_ready:
    nop
# BEGIN huff_distance_extra
    b inflate_missing
    nop
# END huff_distance_extra
huff_distance_ready:
    nop
# BEGIN huff_match_bounds
    b inflate_missing
    nop
# END huff_match_bounds
huff_match_bounds_ready:
    nop
copy_match:
# BEGIN huff_match_copy
    b inflate_missing
    nop
# END huff_match_copy
huff_match_copied:
    nop
    b inflate_symbol
    nop
emit_literal:
# BEGIN huff_literal
    b inflate_missing
    nop
# END huff_literal
huff_literal_ready:
    nop
    b inflate_symbol
    nop
