# Caller

At mix_ready, output samples exist but the caller has not stored the result words. At wrap_ready, processed and clipped hold the saved saturation results while the live result registers belong to the wrapping comparison. At comparison_ready, all caller words have been stored. Observe state at its defined boundary.
