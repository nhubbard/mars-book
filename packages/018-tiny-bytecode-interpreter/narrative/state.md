# Describe the state in memory
The descriptor occupies 32 bytes. Its capacity and full stack address are inputs;
its other fields are outputs. Each call overwrites all five output fields even when
validation fails or the instruction budget is zero. Do not treat a previous output
PC as a request to resume. To inspect a later execution prefix in this exercise,
rerun from the beginning with a larger budget.


See assignment.md and the companion fixtures for complete numerical tables.


The fault field has a different purpose from PC. A branch-target validation failure
can identify an instruction at byte five while PC remains zero because execution has
not started. A runtime overflow at byte seven instead leaves both PC and fault at
seven. On budget exhaustion or halt, fault is minus one.

The return registers repeat two descriptor fields: `v0` is status and
`v1` is retired count. The caller pauses at `vm_ready` immediately
after the function returns. Its separate `last_status` and
`last_steps` words still contain 99 at that point; the following caller
instructions copy the registers into them. Descriptor outputs have already been
published by the function.

The guided implementation publishes descriptor outputs once, as it returns. While
inside its native code, the decode cursor and execution progress live in registers.
Use the returned descriptor at `vm_ready` for the public state comparison.
An equivalent implementation may update its output fields at other points during the
call as long as the specified returned state and ownership bounds hold.
