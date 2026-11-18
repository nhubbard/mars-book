# Predict a working set before measuring

A complete cubic tile selects 4*T*T bytes from each input and 8*T*T output bytes. T=4 totals 256 bytes; placement conflicts and row fragments still matter. Keep cache capacity fixed while comparing associativity and record allocation bases.

The full directly edited composition is in `native/capacity.tex`.
