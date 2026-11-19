# Reproducible MLV1 teaching inputs

Generate a fresh folder containing an animation file, exact RGB reference frames as
binary PPM images and an observation receipt:

```sh
python3 drafts/video-fixtures/fixture.py moving --output /tmp/moving-video
```

Existing folders are refused. `moving` has eight 64 × 64 frames, required full
frames at zero and four, and forty milliseconds per logical display interval.
`still` contains one full frame and three unchanged deltas. `all-change` changes
every pixel and illustrates a delta payload larger than a full payload.
`extra-full` reconstructs the same frames as `moving` but adds an optional full
frame at five so a later seek can begin closer to its target.

The fixture writer encodes explicitly planned updates. It supplies no student
video decoder. PPM previews are reference images, not IDE screenshots. The native
chapter defines the complete byte contract; structured observations do not control
or regenerate its prose.
