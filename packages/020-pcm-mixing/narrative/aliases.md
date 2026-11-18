# Aliases

Load both samples before replacing output. The same dependency holds for a full vector group: both input windows must be retained before any output store. Exact aliases do not overlap later groups because all pointers advance by the same sample width. Shifted overlaps are excluded.
