# A leg has preparation, motion, and a stopped boundary

| Phase | Heading | Movement | Feedback and ink |
| --- | --- | --- | --- |
| Prepare east | Set 90 while stopped | Disabled | (0, 0), no displacement |
| Enable | 90 | Enabled | Countdown starts; no instantaneous side traversal |
| Poll | 90 | Enabled | x approaches L; y remains zero; ink extends east |
| Stop | 90 | Disabled by store | Endpoint (L, 0), first stroke complete |
| Prepare south | Set 180 while stopped | Disabled | Same corner, no new stroke yet |

The pen remains down through these phases. A turn while stopped changes future motion
without adding a diagonal connector.
