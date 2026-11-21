# Revised algorithms and cost unit

The ninth main chapter is `main/algorithms-and-cost.tex`; its written supplement is
`labs/algorithms-and-cost.tex`. They begin on pages 52 and 36 of the current 58-page
main proof and 39-page lab proof. Both are directly edited native LaTeX.

The complete `labs/files/sorting-costs.asm` program implements adjacent-exchange
insertion sort and first-minimum selection sort, using original project 009's
published visualization interface. The versioned original package is preserved.
The chapter explains ordering and value preservation, sorted-prefix invariants,
comparison bounds, signed keys, stability, complete exchanges and caller state
across observation calls. Native tables show four-word exchange histories and
six measured eight-word workloads. Two printed fragments match the full source
exactly and use actual IntelliJ highlighter exports.

The lab develops predictions, a half-exchange debugger trace, coherent display
publication, boundary and comparison faults, and scope-aware measurement. Its
independent `partition-negative.asm` task accepts full signed 32-bit words and
requires stable negative/nonnegative grouping, bounded accesses and preserved
caller state. Public checks are in `labs/checks/partition-negative.kts`; complete
instructor implementations and additional checks remain private. Equivalent
algorithms, temporary registers and private labels are accepted.

| Outcome | Written activity | Technical evidence |
| --- | --- | --- |
| Distinguish order from preservation | Trace complete pairs of stores | 872 visual runs per worked implementation with exact exchange states |
| Explain invariants and boundaries | Inspect both loop guards and signed keys | 90 direct sort calls and 450 independent renderer calls per implementation |
| Preserve state across display calls | Trace nested return links and saved cursors | Alternative column renderer deliberately clobbers allowed argument/temporary registers |
| Compare costs with a stated scope | Reproduce 14 workload measurements | Core completed-instruction intervals, actual key comparisons and framebuffer stores |
| Implement a different ordering requirement | Stable sign partition | Public 56 cases; three private implementations pass 1,202 expanded cases each |

Run the measurement and qualification tools from the public repository root:

```sh
python3 tools/measure_revision_sorting.py --java /path/to/jdk-25/bin/java --cli /path/to/cli.jar --output /new/report/directory
python3 tools/check_revision_sorting.py --java /path/to/jdk-25/bin/java --cli /path/to/cli.jar --output /another/new/report/directory
```

Both retain stdout, stderr and source/CLI bindings. The qualification tool checks
the literal worked source by default; `--alternative` adds equivalent renderers.
Its source-specific observation labels apply to this worked publication, not the
independent partition grading contract.

On the default mixed eight-word input, insertion uses 344 instructions in its
routine interval and 19,633 across the visualized run; selection uses 399 and
7,510. Their 17 and five exchanges cause 19 and seven complete renders. Counts
include completed simulator instructions, not elapsed host time or hardware cycles.
The routine interval includes setup, control, call preparation, calls and return,
while excluding helper and renderer bodies. All comparisons start from fresh
sessions and input copies. This is not a startup benchmark or simulator optimization.

Two actual light-theme IDE Debug observations verify all 64 initial and final
bitmap cells exactly. Their raw images and coordinates remain in the private
technical evidence; the publication uses native explanatory tables. The separate
authoring plugin remains outside MARS. The IDE batch used the required actual
clean cleanSandbox runIde tasks and byte-matched the previously API-verified plugin
archives. It recorded no ERROR or SEVERE log entries.

The public tooling suite passes 391 tests, the separate authoring plugin passes
104 Jupiter tests, and original project 009 validates and builds. Seven worked
program faults and six independent-task faults are rejected. Every new proof page
and representative grayscale pages were visually reviewed; both final PDFs pass
automated PDF/UA-2 validation with the established fonts. The original manuscript
and unrelated MARS edits are preserved.

Human reading, representative learner and assistive-technology reviews remain
open. Native interactive support for this unit, the maze companion, further
main chapters and the rest of the instructional system remain unfinished. The
proposed opening explanation of MARS versus hardware is still deferred.
