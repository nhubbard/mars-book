# Saturation

A sum smaller than -32768 becomes -32768, and one larger than 32767 becomes 32767. Equality is representable. Sum 30000+2767 and sum 32767+1 store the same positive endpoint but contribute clipping counts zero and one. Storing only the low sixteen bits instead wraps 40000 to -25536.
