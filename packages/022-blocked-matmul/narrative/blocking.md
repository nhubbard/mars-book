# Retain contributions between reduction tiles

Clip exclusive ends independently to M,N,K. Initialize C, then reload each cell partial sum before adding a later reduction segment. For the worked example with T=2, the first segment gives 25,28,73,82; the final one adds 33,36,66,72.

The full directly edited composition is in `native/blocking.tex`.
