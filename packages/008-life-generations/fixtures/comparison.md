# A verified instruction-count comparison

| Default workload | Completed simulated instructions |
| --- | --- |
| Unrolled neighbor loads and Boolean rule | 7,007 |
| Offset-table loop and branching rule | 14,197 |

The workload initializes the horizontal three-cell seed, evolves twice, and renders
all three completed generations. Counts include setup and exit. Both implementations
are checked against the same phase and frame contract. These are not host timings,
cycle counts, or grading targets for equivalent submissions.
