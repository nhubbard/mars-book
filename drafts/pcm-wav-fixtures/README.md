# Fixed PCM WAV inputs

The native reading draft is `drafts/pcm-wav.tex`; its editable fragments live under
`includes/chapters/031-pcm-wav`. This helper provides input fixtures only, not a complete
assembly parser, writer or filter. Run `python3 fixture.py input.wav --fixture trace`
from this directory. The output must not already exist. The default fixture is trace.

The 82-byte trace has four stereo frames at 16000 frames per second. Its data starts
at 56 and occupies 16 bytes. A three-byte JUNK chunk and its nonzero pad precede fmt;
a one-byte note chunk and its nonzero pad follow the audio. Source pairs are
(12000,-12000), (0,16000), (-8000,0), (32767,-32768). These stored samples are independently
read by Python wave in the public tests. The native worked-filter table describes
results for eight Q15 coefficients 4096 and zero history per channel.

`--fixture listen` supplies 32000 stereo frames at 16000 frames per second, exactly two
seconds. The left integer triangle repeats every 80 frames, the right every 64. Their
alternating high components have opposite signs. The complete file is 128056 bytes,
with PCM again beginning at 56. No resampling or filtering occurs in the helper.

This is the reading-foundation stage. Full MIPS implementations, cumulative student
construction, public grading and separate chapter distributions remain to be authored.
The chapter is not yet registered as a packaged course entry or included in main.tex.
Actual automated light-theme IDE captures and native annotation coordinates remain
pending. A host model or playback test is not evidence of MARS execution.
