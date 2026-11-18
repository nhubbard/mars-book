# Derive the physical strides

Use a+4*(i*K+k), b+4*(k*N+j), and c+8*(i*N+j). The right column advances by 4*N bytes. A tile does not repack either input or change these physical row widths. Add bounded offsets to full-width bases.

The full directly edited composition is in `native/addresses.tex`.
