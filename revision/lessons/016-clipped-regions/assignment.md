# Clip a sprite without crossing its storage

Read the clipped-sprite companion in Chapter 6. You need row-major addressing, signed comparisons and callable-function reasoning. The new task is a bounded rectangular image copy with an exact transparency key.

Start with worked/interval.asm and worked/extreme.asm. These are complete experiments, not construction templates. Predict the surviving half-open interval at x=-2, then inspect the full-width endpoint when x=2147483647. Small image dimensions do not make intermediate placement arithmetic fit a signed word. worked/interval.kts checks 2,268 argument combinations plus the complete first caller.

Implement blit_sprite in independent/blit.asm by replacing its break body. Preserve the caller, allocations, descriptors and public observation labels. a0 selects the frame descriptor, a1 selects the sprite descriptor, and signed a2/a3 place the sprite. The companion specifies dimensions, full pointer fields, interior allocation offsets, word strides, preserved registers and disjoint objects. Choose your own private labels, traversal and equivalent implementation. You may inline clipping or call a correctly preserved helper.

Begin with empty intersections. Check those 474 cases while the overlapping copy path is still unfinished. Then complete and check the 107 overlap cases. Their union is all 581 original kernel trials. Translate destination coordinates back into source coordinates, and use the two physical strides independently. Return opaque count in v0 and visible count in v1. A transparent source word permits no destination store; a same-colored opaque word still counts. Compare all 32 key bits.

The ten caller cases then observe initialization, actual returned registers and final stored counts in one execution. In Debug, stop at blit_call, blit_ready and done without restarting between stops. The default placement returns (8,12). Inspect copied and visible before the exit service replaces v0. The display is a view of storage; the descriptor defines the logical frame.

Finish with ordinary Run and retain three examples: an empty intersection, a partial overlap and an all-transparent visible rectangle. Explain source origin, first addresses, row transitions and permitted stores for each. A final matching image does not establish the store-history contract. Hints and progression follow the instructor's policy.
