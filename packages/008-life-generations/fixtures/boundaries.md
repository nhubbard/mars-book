# Name the computation boundary before comparing state

| Boundary | Working current role | Frame and published number |
| --- | --- | --- |
| `seed_copied` | A contains generation 0 | Not rendered or published yet |
| `generation_computed`, first sweep | A still current; B complete next | Generation 0 still visible |
| `buffers_swapped`, first swap | B is current, working count 1 | Still generation 0 until rendering finishes |
| `generation_ready`, second hit | B current, A previous | Complete generation 1 |

A pointer assignment does not make the display follow it automatically. Rendering
and publication are separate program operations. These are logical observation
boundaries, not atomic multiword hardware transactions.
