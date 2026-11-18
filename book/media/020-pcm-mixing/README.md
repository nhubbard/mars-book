# PCM mixing listening fixtures

Each file contains 32000 signed 16-bit little-endian mono samples at 16000 frames per
second: two seconds. They use a 64-sample integer triangle, with a 250 Hz fundamental.
No normalization or gain change is applied by the renderer. The source streams are
input fixtures; the saturated and wrapping tracks contain samples actually stored by
the simulated routines and checked independently. The scalar and MSA implementations
produce identical files. receipt.json records exact WAV and raw-PCM hashes.

| Scene | Input amplitude | Second-input phase | Clipped samples |
|---|---:|---:|---:|
| quiet | 6000 | 0 | 0 |
| clipping | 24000 | 0 | 11000 |
| phase-offset | 24000 | 8 samples | 11000 |

For each scene, -a and -b are the two separate mono inputs, -saturated is their clamped
sum, and -wrapped is the comparison that retains only the low sixteen sum bits. Two
input streams do not make these files stereo. Quiet saturated/wrapped files are byte
identical; the louder scenes differ. Equal clipping totals in the last two scenes do
not imply identical waveforms. The native chapter gives the integer formula and a
waveform description for access without listening. No live playback-device result is
claimed.

- [Quiet saturated](quiet-saturated.wav) and [quiet wrapping](quiet-wrapped.wav)
- [Clipping saturated](clipping-saturated.wav) and [clipping wrapping](clipping-wrapped.wav)
- [Phase-offset saturated](phase-offset-saturated.wav) and [phase-offset wrapping](phase-offset-wrapped.wav)
