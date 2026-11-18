# State what the cache model measures

The Data Cache Simulator observes the running program’s data accesses. A cache line holds a consecutive block of bytes. On an access, the model checks whether the containing line is already resident. A hit finds the line; a miss brings it into the modeled cache. A store miss also allocates a line. The counters describe this model’s response to the address sequence.

Use eight lines of four words each for the first comparison. Each line holds sixteen bytes, and the total capacity is 128 bytes. A sixteen-by-sixteen word matrix occupies 1024 bytes, so both matrices cannot remain resident together. Nevertheless, a four-by-four source tile and its transposed destination each occupy only four lines when their rows and tile boundaries have the required alignment. Eight live lines can fit together if the placement policy permits it.

## Placement determines which lines can coexist

For a sixteen-byte line, let the line number of byte address $`a`$ be $`\lfloor a/16\rfloor`$. A direct-mapped eight-line cache uses
``` math
\operatorname{set}(a)=\lfloor a/16\rfloor\bmod8.
```
Each set has only one position. Two different memory lines with that same set number displace each other, even if the other seven positions are empty.

A fully associative eight-line cache has one set with eight positions, or ways. Any eight different memory lines can coexist. A two-way cache with the same total capacity has four sets, with two positions in each. It lies between those two placement policies: a line can use either position in its set, but cannot borrow a position from a different set. Keep the total number of lines and the words per line constant when comparing associativity.

When a set is full, the least-recently-used replacement policy evicts the line whose most recent modeled access occurred earliest. It uses both reads and writes to update recency. Select this policy explicitly in the experiment so that the comparison is repeatable. A different replacement policy is another experiment, not a change to the transpose’s required output.

## Follow one sixteen-by-sixteen row

A source row occupies four lines. Consecutive loads use all four words of each line. The corresponding output stores are sixty-four bytes apart because the output row width is sixteen words. They visit sixteen output lines while the ordinary version handles its first source row. The output lines needed for the next source row may already have been evicted.

Within a four-by-four tile, those destination stores visit only four output lines. The next three source rows of that tile revisit the same four output lines, filling the remaining words in each. The fully associative eight-line cache has room for the four source lines and those four destination lines. For the aligned sixteen-by-sixteen example, each matrix has sixty-four lines. One initial miss for each input and output line gives a lower bound of 128 misses under this store-allocation policy. The measured tiled reference reaches that bound in the fully associative cache.

The same capacity argument does not prove a direct-mapped result. A sixteen-word row advances the line number by four, so successive row starts alternate between only two of the eight set numbers. Four source rows therefore contain conflicts before the destination is considered. The selected output allocation introduces its own set conflicts. A tile can be small enough in bytes and still map poorly.

## Keep the model’s limits beside its results

The model counts each native data-access notice once, at its starting address. It does not split one notice into separate cache accesses when its width crosses a line boundary. This chapter uses aligned word accesses, so a four-byte matrix access fits within one sixteen-byte line. The grader checks that observable width as part of the exercise contract.

Instruction fetches, debugger inspection, and the grader’s memory reads do not contribute to these data-cache counters. Stack and auxiliary execution data accesses do contribute. A solution that saves registers on the stack can therefore produce different whole-function counters while still meeting the matrix contract. Exact cache counts in the worked example describe the measured reference program, not a universal correctness requirement for all submissions.

There is no cycle estimate, memory-latency penalty, prefetcher, or write-back traffic calculation in this model. A reduction in misses is not itself an elapsed-time speedup. The interpreter’s host execution time also includes its implementation overhead. Keep completed simulated instructions, cache accesses, cache misses, and host time as separate quantities.
