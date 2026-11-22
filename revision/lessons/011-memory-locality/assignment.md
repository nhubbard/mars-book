# Addresses, tiles and cache observations

Read the memory-locality chapter and lab. Derive the two address recurrences for
the three-by-five source. Follow the ordinary and tiled Debug routes using their
separate complete sources, then run both programs. Record output words, active
pointers, cache geometry and the exact counter boundary. Explain why the caller's
pending count store is outside a function-only measurement. Use fresh executions
for cold comparisons; backstep does not rewind cache history.

Implement the independent clockwise rotation and justify its coordinate mapping.
Use a rectangle to distinguish it from transpose. Complete both checks on the
current source. Then compare two correct traversal schedules using the lab's
measurement method, keeping correctness requirements and cache experiments
separate. Derive a duplicate-read counterexample even when output is unchanged.
