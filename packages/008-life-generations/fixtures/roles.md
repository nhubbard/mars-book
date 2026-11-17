# Physical storage and logical roles

| Published generation | Current | Other allocation |
| --- | --- | --- |
| 0 | A contains seed | B interior is scratch |
| 1 | B contains generation 1 | A retains generation 0 |
| 2 | A contains generation 2 | B retains generation 1 |
| 3 | B contains generation 3 | A retains generation 2 |
| 4 | A contains generation 4 | B retains generation 3 |

Only roles change at the pointer swap. Source protection follows the current role
from one ready boundary to the next. The physical A and B names do not change.
Both halos are permanently protected.
