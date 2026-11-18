# Counting

Each clipped halfword contributes sixteen one bits. pcnt.d counts bits in two groups of four halfwords. The first default group gives low population sixteen and high population thirty-two. Sum forty-eight and divide by sixteen to get three clipped samples. Accumulate groups and scalar tails into the count initialized at entry.
