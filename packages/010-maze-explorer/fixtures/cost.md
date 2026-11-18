# Count search work and observation work separately

Let $`V_r`$ be the reachable cells and $`E_r`$ the examined neighbor relations. Marking on discovery bounds queue insertions by $`V_r`$, so traversal work is $`O(V_r+E_r)`$. In a four-neighbor grid, each processed non-goal cell examines at most four candidates.

For a generalized grid of $`C`$ cells, a full redraw after each of $`P`$ processing steps adds $`O(CP)`$ pixel work. The present task fixes $`C=64`$; it is useful for stepping but should not be mistaken for an efficient rendering policy at every scale. Both the tiny four-cell room and the fully open grid still render 64 pixel words per publication. Compare identical observation policies before attributing instruction-count differences to the graph algorithm.

| Fixture   | Implementation |   Whole | Search range |
|:----------|:---------------|--------:|-------------:|
| Tiny room | Reference      |   7,286 |          800 |
| Tiny room | Alternative    |   7,318 |          832 |
| Full room | Reference      | 125,384 |       14,875 |
| Full room | Alternative    | 126,314 |       15,805 |

These are measured completed-instruction counts on the pinned core, not cycle estimates. Whole counts include initialization, all rendering, and exit. The half-open PC range from the reference’s `search` label to `done` excludes initialization, the renderer body placed after `done`, and the exit sequence. It includes caller publication stores, calls, and any in-range delay-slot instructions. The alternative changes both neighbor order and bounds instructions; this table compares those complete implementations and does not isolate one cause. Exact totals are reference evidence, not free-writing grading requirements.
