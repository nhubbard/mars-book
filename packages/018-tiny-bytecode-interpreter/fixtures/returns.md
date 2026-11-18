# Returned state comparisons

| Scene | Status | PC | Depth | Retired | Fault | Live values |
|---|---:|---:|---:|---:|---:|---|
| Default full | 1 | 18 | 1 | 33 | -1 | 6 |
| Default fuel four | 0 | 7 | 2 | 4 | -1 | 0,3 |
| Default capacity two, fuel two | 0 | 4 | 2 | 2 | -1 | 0,3 |
| Default capacity two, fuel three | -6 | 4 | 2 | 2 | 4 | 0,3 |
| Invalid target six, fuel zero | -3 | 0 | 0 | 0 | 5 | Empty |

Each call begins fresh. The descriptor is published before return; last_status and last_steps still contain 99 at vm_ready and change only after caller stores.
