# Clipped sprite companion

Section 6.6 occupies lab pages 23–29. Directly edited publication sources are
`labs/companions/clipped-sprite.tex` and `figures/clipped`; the combined lab proof
has 71 pages. The main book is unchanged at 78 pages. The reading table and page
references in README.md have been reconciled with the actual combined PDF,
including older stale early-unit references.

The companion connects descriptors to addresses before introducing clipping. It
explains logical dimensions versus stored row stride, half-open intersections,
full-width signed endpoint calculations, translation back to source coordinates,
independent row-pointer updates, full-word key comparison and distinct visible
and opaque counts. A native rectangle diagram and two tables support the worked
five-by-four sprite at (-2,1). The complete interval experiment supplies a short
assembly connection; the blitter remains an independent implementation task.

The revised work area is `labs/files/clipped-sprite.asm`; its supplied caller and
data layout retain original project 016's contracts, with compact transfers and
a correctly aligned stack. Its source Bitmap Display annotation uses the supported
custom-base/address syntax for frame_a in the Default layout, exposing a 16x8
storage view with an 8x6 logical frame. The starter's break deliberately fails
until its function is implemented. Original versioned package files are preserved.

The complete `labs/files/clip-interval.asm` experiment has public checks in
`labs/checks/clip-interval.kts`. It passes 2,269 cases: all source/frame widths
zero through eight over the stated ordinary/extreme positions, plus the default
caller. One printed fragment exactly matches that tested source and has a fresh
actual IntelliJ highlight export. All 803 earlier records remain unchanged
(804 total).

Clipped-rectangle, per-pixel, reversed per-pixel and wide-data blitters each pass
581 public kernel, ten public caller and 7,220 private geometry cases. The private
domain includes every 0..8 dimension tuple, a dense placement square, every legal
stride in each descriptor role and equal-destination cases, with varied colors,
keys and interior offsets. It does not exhaust all arbitrary arrays or pointers.
Ten faults are rejected, including RGB-only transparency, temporary transparent
stores, wrong strides and lost pointer bits. Three exact written examples verify
the default geometry/origins/eight-store set, opaque zero/full-word distinctions
and the opposite-corner fragment. Full solutions and expanded checks remain private.

Three actual IntelliJ Light observations check all 128 native display cells exactly.
Two unchanged PNGs appear in the native publication comparison; the third corner
observation remains available as an additional captured asset. Native annotations
outline the logical frame and identify opaque zero and a high-byte key distinction.
The first screen photograph was rejected: ColorSync introduced a three-level red
component in three green/cyan cells despite exact native pixels. That evidence is
retained separately. The repeated batch allows three levels per screen channel;
native rendered pixels and tooltip RGB remain exact. No screenshot was recolored. A final third batch repeats all three observations
after the caller-alignment correction; earlier accepted captures remain separately
identified as prealignment evidence.

Every launch used actual clean cleanSandbox runIde tasks. All eleven installed
JARs match the previously API-verified archives. The accepted batch's log has no
ERROR/SEVERE entries; the rejected batch's diagnostics remain separate. The public
tooling suite passes 391 tests and the authoring plugin 104 Jupiter tests. Original
project 016 validates and builds. No MARS or authoring-plugin implementation change,
and no grader API addition, was needed.

The final lab PDF passes automated PDF/UA-2 with zero failures. Pages 23–30 and
representative grayscale diagram/capture pages were visually reviewed. Established
fonts and native LaTeX publication authority remain. Original main.tex and forty
unrelated MARS files are preserved. Human learner/assistive-technology reviews and
native interactive delivery remain open. The MarsBot companion is now section 4.7; see marsbot-unit.md. Advanced chapters
and remaining instructional-system work are unfinished. The proposed MARS-versus-
hardware opening remains deferred; this milestone does not complete the goal.
