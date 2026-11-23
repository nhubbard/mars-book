# Independent search and renderer contract

Write a breadth-first search over a fixed 8 by 8 maze and publish its progress through Bitmap Display. The input `walls` contains 64 bytes: zero is open and one is a wall. Word inputs `start` and `goal` are cell IDs from zero through 63 and name open cells. A legal edge joins open orthogonal neighbors and has cost one. There are no diagonal or weighted moves in this contract.

Keep the public layout from the starter. `frame` is the first static-data object at hexadecimal 10010000 and contains 64 RGB words. `frame_guard` follows at hexadecimal 10010100. All byte-array bases and metadata words are four-byte aligned. Buffer labels may move after the frame guard. Preserve the named guard words and never execute stores to `walls`, `start`, or `goal`, even if a later store would restore them.

Initialize all 64 bytes of `states`, `parents`, `distance`, `queue`, and `path` on every launch. State values are zero unseen, one frontier, and two processed. Unknown parent and distance bytes are 255. The start has distance zero and parent 255. Initialize the entire queue and path arrays to zero. Discover a cell once: record its waiting state, first predecessor, and distance before appending its ID to the monotone first-in, first-out (FIFO) queue. Retain queue history and a zero unused suffix. Never append beyond its 64-byte capacity.

At the end of each renderer execution, restore the saved-register and stack-pointer state, then reach the public `maze_ready` label exactly once before returning. The frame and preserved registers are complete at this observation boundary and remain valid through the return. Phase zero is the initialized start. Phase one follows each dequeue and all unseen open neighbor discoveries; if that dequeued cell is the goal, skip its neighbors. Neighbor order is your choice. `head`, `tail`, and `current` describe that boundary, with current minus one initially. Result and length are minus one in phases zero and one. The path mask stays zero until reconstruction.

After processing the goal, publish phase two with result one, the shortest edge count in `length`, and a path mask containing exactly one predecessor chain including both endpoints. If the queue empties first, publish phase two with result zero, length minus one, and an all-zero path mask. A final redraw precedes normal exit. The start-equals-goal route has zero edges and one marked cell.

The public function `render` reads `walls`, `states`, and `path` and paints all 64 frame words. Walls have priority, followed by path membership, then states. The RGB values are dark wall 202020, blue path 0072B2, white unseen FFFFFF, orange frontier E69F00, and green processed 009E73, all hexadecimal. The renderer must preserve full saved-register values and the stack pointer, and change neither its input arrays nor metadata. It returns through `maze_ready` using the caller’s return address. The supplied `done` label is an independent-call continuation.

The grader allows 250,000 executed instructions per complete case. It checks every published transition, queue uniqueness and FIFO history, valid predecessor relations, shortest distances, frame contents, immutable inputs, guards, final exit, and independent renderer behavior. It accepts different neighbor orders and valid shortest paths. It does not prescribe scratch registers, private labels, or exact instruction counts. The tiny fixture opens only zero, one, eight, and nine; the full fixture opens all 64 cells with endpoints zero and 63.


## Public assessment entry points

The native renderer assessment uses five independent fixtures, with 10,000
instructions per fixture. It starts at `render`, expects one completed
`maze_ready` publication with restored saved state, returns to `done`, and exits
normally. It checks all frame words and preserves the arrays, metadata and guards.
This permits testing the renderer while the search body is unfinished.

The native complete-search assessment uses eight fixtures, with a cumulative
250,000 instructions per fixture. The cases include equal shortest routes,
start equal to goal, a full queue, reverse direction, a potential row wrap,
a diagonal, a corridor and an isolated start. Each fresh case poisons outputs,
installs its inputs and varying guard words, checks every publication, and
checks the final state again after normal exit. Input stores are forbidden even
when restored. Initialization, every complete dequeue/expansion and the terminal
result must each be published once. Queue history determines the expected first
parents; another valid neighbor order remains accepted.

`maze-render.cases.json` and `maze-search.cases.json` are the data-only native
profiles. Their generated `.kts` files call the same `ConsoleMazeCheck` from the
command-line grader. No instructor solution or private fixture is included.

A public pass is evidence for these cases, not a proof over every 8×8 wall
layout. Explain the invariants and investigate the written lab's deliberate
faults. Course grading can add private cases using the same documented interface.
