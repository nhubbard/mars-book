# File boundaries and binary images

Chapter 18 and its written lab revise original project `029-binary-ppm`.
The main proof has 118 pages, with this chapter on 112–118. The lab proof has 110 pages,
with this unit on 106–110. Both are directly edited
native LaTeX. The original manuscript remains preserved.

The main chapter connects a precise local P6 contract to bounded token scanning,
header/raster interpretation, deferred metadata publication, RGB words versus
little-endian bytes, educational file services, descriptors, partial transfers,
EOF probes, validation before truncation and complete host artifacts. Two native
concept tables explain the byte layout and pixel representations. The written
lab asks for predictions, observes return boundaries, diagnoses selected-extent
mistakes and finishes with a complementary-color serializer.

The supplied `labs/files/ppm-codec.asm` completes parse, decode, encode, load and
save. Its caller selects the individual operations or the complete pipeline.
Three ordinary/alternative/wide-address implementations each pass 104 original
public cases and 866 existing private completed-interface cases. These checks
include exact publication intervals, saved state, short transfers, failure
precedence and descriptor ownership. They do not establish every possible file.

The independent `labs/files/ppm-negative.asm` intentionally stops at an unfinished
`ppm_encode_negative`. The public `labs/checks/ppm-negative.kts` accepts equivalent
implementations through 167 behavioral cases. Direct serialization and reuse of
the supplied encoder both pass those cases and 4257 expanded cases each; a
wide-address direct implementation also passes the expanded set. That domain
covers all 4096 permitted dimension pairs, 160 selected dimension/offset cases
and a retry sequence. Its offsets are correlated rather than every independent
source/destination combination. Pixel data are deterministic varied values,
not exhaustive RGB combinations. The untouched starter fails all 167 cases.

`tools/run_revision_ppm.py` creates a new directory, generates raw input bytes,
executes the real MARS CLI and checks exact canonical output, including truncation
of a longer previous file. Six recorded successful runs include ordinary,
commented, maximum-size and both completed independent designs. FFmpeg/FFprobe
independently decode the canonical output and check its pixels and dimensions.
The comment-adjacent-magic input is accepted by the local contract but not by
that FFmpeg decoder; this limitation is explicit in the lab. Four actual failing
loads preserve an existing output file. These host checks and the native virtual
fixture checks have different, explicitly stated scopes.

Three actual light-theme Debug captures show the punctuation-fixture descriptor,
the twelve-byte 9-by-10 header and RGB samples, and the four decoded words after
the complete pipeline. The pipeline uses one deterministic virtual path with
seven-byte transfer limits and descriptor 19, verifying all thirteen operations,
exact output and closed descriptors. Actual host files are exercised separately
by the public runner. Native LaTeX supplies crops, boxes, titles and semantic
figure descriptions. Screenshot automation remains in the separate plugin.

Seven exact written probes pass. Fourteen deliberate semantic faults are rejected
by existing behavior suites. A fifteenth, ignoring a zero-progress write, is
rejected by four newly enabled checks. Those four cases pass for the complete
reference, alternative and wide-address implementations.

The missing grader facility was added upstream: `provideFiles` accepts an optional
`zeroWriteAfterBytes`, with per-descriptor accepted-byte accounting and reset on
reopen. Invalid requests, configured failures and file-size exhaustion retain
priority. The isolated implementation passed all 133 CLI Jupiter tests and was
committed independently before integrating only the tested changes into the
working checkout. Manual edits were preserved. The original CLI remains intact;
new zero-progress evidence identifies the separate newly built grader digest.

Two fresh actual IntelliJ highlight exports extend the prior 816 records without
altering them. The authoring suite passes 104 Jupiter tests; public tooling passes
391 tests. The original PPM package validates and exports. PDF font, rendering,
grayscale and final PDF/UA-2 results are retained in the private archive. No human
learner, instructor or assistive-technology validation is claimed. The PPM native
guided lesson and the broader curriculum/instructional-system goal remain open.
