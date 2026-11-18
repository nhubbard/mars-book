# Check the predecessor relation

For every discovered non-start cell $`v`$, its recorded predecessor $`p(v)`$ is open, shares a side with $`v`$, and satisfies
``` math
d(v)=d(p(v))+1.
```
In the small room, $`p(1)=0`$, $`p(8)=0`$, and the reference records $`p(9)=1`$. Both $`d(1)`$ and $`d(8)`$ equal one, so recording $`p(9)=8`$ would also be valid under a different discovery order.

A path mask alone cannot establish this relation. Three marked cells could be separated by walls. A distance alone cannot establish it either. Check the links, their geometric adjacency, and their decreasing distances before accepting the chain.
