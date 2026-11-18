# Purpose

PCM represents samples as signed numerical codes. This kernel mixes two already decoded mono streams at matching times. It does not parse files, resample, or change channels. The rate belongs to the listening-file metadata; clipping is sample arithmetic independent of playback timing.
