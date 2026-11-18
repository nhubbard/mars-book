# Compare equal work at the same boundary

Begin with fresh architectural state and a cold cache for each measurement. Set the dimensions and buffer addresses before entering the function. Stop immediately after it returns, before the caller stores the returned count in memory. The reference functions use no stack or auxiliary memory, so their measured data traffic consists of exactly one read and one write per matrix cell.

The following observations come from the standalone MARS grader using the same cache implementation as the IDE. Every cache has eight lines of four words and uses least-recently-used replacement. Both buffers begin at 64-byte-aligned addresses in the reference layout. Method O is the ordinary source-row traversal; method T finishes four-by-four tiles. Each function starts from a cold cache.

| Shape | Method | Instructions | Accesses | Full misses | Direct misses | 2-way misses |
|:---|---:|---:|---:|---:|---:|---:|
| $`3\times5`$ | O | 159 | 30 | 8 | 8 | 8 |
| $`3\times5`$ | T | 241 | 30 | 8 | 8 | 8 |
| $`8\times8`$ | O | 596 | 128 | 80 | 86 | 80 |
| $`8\times8`$ | T | 791 | 128 | 32 | 48 | 54 |
| $`16\times16`$ | O | 2204 | 512 | 320 | 344 | 320 |
| $`16\times16`$ | T | 3117 | 512 | 128 | 344 | 320 |
| $`15\times13`$ | O | 1707 | 390 | 244 | 230 | 237 |
| $`15\times13`$ | T | 2585 | 390 | 174 | 227 | 235 |

For the sixteen-by-sixteen case, the fully associative tiled version has $`320-128=192`$ fewer misses, a reduction of $`192/320=60\%`$. It also completes 913 more instructions, about 41 percent more than the ordinary reference. The loop bounds, tile setup, and repeated address calculations account for extra work. Whether that tradeoff helps a particular hardware implementation requires a performance model or measurements of that hardware.

The three-by-five input and output occupy four lines each. All eight fit in the fully associative cache, and the measured direct and two-way placements also retain the needed reuse in this layout. Tiling cannot reduce the eight initial misses, but it still adds loop overhead. An optimization can benefit one size and add work at another size.

The fifteen-by-thirteen example requires clipped tiles. Its rows are not a multiple of a sixteen-byte line, so the simple four-lines-per-tile explanation for the aligned square no longer applies. It still benefits from tiling in the fully associative measurement, but its tiled miss count exceeds the initial-access lower bound. Use its actual address sequence to explain that result rather than carrying over the square matrix’s count.

# Repeat the comparison in the IDE

The IDE capture sequence will stop at the function-entry boundary and at the return boundary. The first view must show zero cache accesses; the second must show the completed output, the selected cache geometry, and the counters. Change one variable at a time: function, dimensions, placement policy, then buffer alignment. Restart execution for each cold measurement. Continuing through the caller’s result store adds a data access outside the measured function; do not compare that later total with the table’s function-only count.

Backstepping restores architectural state but retains this tool’s observations. Replaying an instruction can therefore hit a line brought in by the previous forward execution. Use a new run with a cold cache for a repeatable comparison. A warm-cache experiment is valid too, provided the preceding access history is specified and identical for both functions.

<div class="MARSExercise">

Separate a mapping effect from a correctness error Explain why equal direct-mapped miss counts for the sixteen-by-sixteen example do not show that the tiled function is wrong. Identify the independent checks needed to establish the output, the tile traversal, and the cache measurement boundary. Then predict which evidence should remain unchanged when both buffers are moved above a 32-bit address boundary.

</div>

<div class="MARSExercise">

Change one assumption Choose one experiment: a different tile side, a different output alignment, or a larger cache. Write down the expected effect before executing the program. Record the dimensions, line size, placement, replacement policy, cold or warm starting state, completed instructions, data accesses, and misses. Check the output again after the change. A lower counter is useful evidence only when the program still performs the required transpose.

</div>
