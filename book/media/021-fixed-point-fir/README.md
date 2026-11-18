# Eight-tap FIR listening fixtures

Six mono WAV files contain 32000 signed sixteen-bit little-endian samples at 16000
frames per second: two seconds each. Each scene has an input and a filtered track.
All eight coefficients are 4096 (one eighth). There is one final nearest rounding
with ties toward positive infinity; no normalization or gain change is applied.
The filtered tracks contain actual simulator-produced bytes checked independently.
Both complete implementations produce byte-identical WAV files.

- low: a period-sixty-four integer triangle with amplitude 6000, fundamental 250 Hz.
- high: the repeating sample pattern 0,12000,0,-12000, a 4 kHz component at this rate.
- mixed: the sample-by-sample sum of those two inputs.

Every call receives seven preceding samples from the same periodic scene. This is
warm input history, not a hidden simulator/kernel state or a zero-start transient.
Every eight-sample window sums the high component to zero, so high-filtered.wav is
all-zero PCM. mixed-filtered.wav and low-filtered.wav are byte-identical. The low
filtered waveform differs from the original low input because averaging also changes
its shape and delay. These properties are available numerically without listening.
The native plot connects stored high samples and shows the zero filtered result;
it is not a reconstruction of a continuous analog waveform.

receipt.json records WAV/raw-PCM hashes, source-check fingerprints, exact coefficient
and rounding rules, and frame metadata. No live playback-device result is claimed.
The full incremental chapter, student package and actual IDE captures remain in progress.
