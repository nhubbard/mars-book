# Trace the room with two shortest routes

The reference direction order is north, east, south, west. Brackets show the retained queue history; the waiting work begins at `head`.

| Boundary  | Head | Tail | Queue history  | New discoveries          |
|:----------|:-----|:-----|:---------------|:-------------------------|
| Initial   | 0    | 1    | \[0\]          | start 0                  |
| Process 0 | 1    | 3    | \[0, 1, 8\]    | 1 and 8                  |
| Process 1 | 2    | 4    | \[0, 1, 8, 9\] | 9, parent 1              |
| Process 8 | 3    | 4    | \[0, 1, 8, 9\] | none; 9 is waiting       |
| Process 9 | 4    | 4    | \[0, 1, 8, 9\] | none; goal test succeeds |
| Final     | 4    | 4    | \[0, 1, 8, 9\] | route \[0, 1, 9\]        |

Distances at removal are 0, 1, 1, 2. A south-first order may instead retain \[0, 8, 1, 9\] and choose parent 8 for the goal. Both routes have two edges.
