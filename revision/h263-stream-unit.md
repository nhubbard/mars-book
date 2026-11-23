# H.263 stream and file-publication unit

Chapter 27 completes the two-chapter narrative revision of original project
`035-h263`. Directly edited native authority is `main/producing-video-streams.tex`
and `labs/producing-video-streams.tex`. The main proof is 189 pages, with this
unit on 181–189; the written lab is 185 pages, with this unit on 181–185.
The initial manuscript and twelve-page advanced editorial pilot remain preserved.
The deferred general opening about MARS versus hardware has not been added.

## Teaching sequence

The chapter follows a concrete cross-byte append before distinguishing scan order,
RUN/LAST events, compact/escape equivalence, ordinary presence and special INTRA DC.
It derives wrapped motion differences, explains why explicit GOB boundaries affect
neighbour prediction, and connects staged picture publication to accepted file
progress. The fifty-bit restricted header has a concrete field map. The independent
checking section distinguishes syntax, source quality, local/decoder drift and
inverse accuracy; PSNR is defined rather than left as an unexplained threshold.
Clock duration, temporal-reference wrap and guest work have separate meanings.

The complete worked encoder is the exact source qualified in Chapter 26. No
numerical body or simulator implementation changed. The original full-encoder
construction assignment remains available. This unit renews every remaining
stream/file domain and actual whole-clip interoperability, rather than inheriting
old bitstreams or requiring identical bytes from equivalent encoders.

The new independent `h263-read-bits.asm` has an unfinished bounded reader. Its
context separates capacity, valid bits and cursor. It admits full-width metadata,
reads only bytes intersecting a selected field, zero-extends a 32-bit result, and
commits only a successful nonempty cursor advance. The lab connects it to actual
picture headers while explaining why a header inspector is not a complete decoder.
Bit-by-bit and byte-fragment solutions and private frame choices are accepted.

## Finite and real-file evidence

Reference, equivalent and high-code sources pass all 17 remaining domains:
35148 positive cases across 51 runs. Together with Chapter 26's exact-source
11760 numerical cases, all 31 manifest domains pass on all three layouts:
46908 positive finite executions. The same exact sources/CLI retain the previous
fresh 720000-block/72-zero full inverse gate; that is inherited evidence, not a
second execution of those blocks in this unit.

Eight assembled stream faults are rejected against matching public domains:
old field ones retained, natural-order coefficient emission, LAST tied to array
position, direct INTER CBPY, five-bit motion wrapping, destructive capacity
failure, requested-count write advancement and early reference promotion.
The matching unique public domains contain 504 cases.

Five actual host workflows encode/decode reference MSA, reference scalar,
alternative MSA, high-code MSA and a 44-picture temporal-wrap sequence. The four
defaults have eight pictures each. Reference streams are 2212 bytes; equivalent
streams 5101. The 44-picture stream is 11249 bytes. Strict full-profile parsing
and independent FFmpeg decoding check all 76 pictures and every plane; decoded
pixels, raw input, local reconstructions, source/tool hashes and reports remain
retained. Default maximum local/decoder difference is two; the longer run's is
three. Minimum measured plane PSNR is approximately 45.865 dB in defaults and
40.073 dB in the longer sequence, above the documented 35 dB gate. These observations
do not establish quality for every possible input or replace inverse accuracy.

The printed run helper and separate checker are executed on another actual eight-
picture run. Both protect existing results on a rejected repeat, retaining every
byte. Individual fresh-JVM wall times include assembly, I/O and shutdown under
uncontrolled host load and are not throughput benchmarks. Complete guest instruction
counts are bound to the prepared caller/path layout rather than grading identities.

The reader's three layouts pass 3576 primary cases: 592 public and 600 expanded
each. Additional checks cover a legal full 8192-byte private frame (592 cases),
six complete ordinary-demo cases, 144 actual target-writer/reader round trips,
and six cases reading all 76 real picture headers in both byte orders. Those
additional cases are counted separately. The unfinished starter fails all 592
public cases as expected.

Eight assembled reader faults are rejected: LSB-first interpretation, signed
full-field result, early cursor publication, narrowed metadata, adjacent-byte
reads, input rewriting, empty cursor stores and stale result reuse. The earlier
590 isolated-call cases accepted the stale accumulator; two final retained-cursor
cases reject the same fault. That before acceptance is excluded from positive
qualification. Initial wrapping-address setup and guard-clamp diagnostics remain
historical evidence, not simulator accuracy failures. No new grading API was needed.

## Native publication evidence and remaining scope

Four actual IntelliJ Light Debug captures show appended field bytes, complete
coefficient payload, accepted clip progress and the reader value/cursor. The actual
clean cleanSandbox runIde tasks ran before launch. All four core probes pass before
capture. The native IDE itself encodes eight complete pictures; its actual files
independently decode and match the verified CLI reference byte for byte. Its owned
IDE is closed. Capture automation remains entirely in the separate authoring plugin.
Raw PNGs/receipts and editable native crops, outlines, diagrams, captions and semantic
alternatives are retained. The driver matches the previously paired-verified binary;
all ten installed MARS JARs match the pinned immutable archive.

All 105 authoring Jupiter and 419 public tooling tests pass. Two actual IntelliJ
exports extend the preserved 834-record highlighting catalog to 836. Student-package
validation/build and the printed workflows pass. All new main/lab pages and
representative grayscale code/figures are reviewed. Automated PDF/UA-2 checks bind
the exact final proofs with zero failures. Fonts are embedded; native authority
and original draft evidence remain preserved. All 41 manual MARS edits are unchanged.

The narrative now reaches its final planned project, but this does not complete the
full curriculum goal. A current coverage/prerequisite and cross-reference audit,
advanced native instructional delivery and remaining instructional-system work
are still open. Author, learner and assistive-technology reviews remain pending;
these automated checks are not claims of student usability validation.


### Native bounded-reader assessment foundation, 23 November 2026

The native reader model retains all 592 original public cases and 598 calls,
including every field width zero through 32, every starting phase, both native
byte orders, full-width invalid metadata, maximum capacity and retained-cursor
success/failure/empty sequences. Eight format-22 profiles retain the original
100000-instruction case allowances. Two final checks each select 328 placements;
their union covers all 592 original cases, with the shared boundary/metadata/reuse
cases repeated. This fits existing file, assessment and instruction quotas.

A small upstream grading extension initializes native pointers from relocated
public data labels before a call. It does not require an assembly initialization
helper or fix a student's data address. The public starter adds only
`reader_bytes_tail`, a symbol within the unchanged final 64-byte allocation;
its unfinished function remains unchanged. The full first MiB has an interval
access policy, while an always-present caller object guards the final tail.
Together they reject input writes and reads outside the selected field over the
entire allocation, without increasing grader quotas. Context/caller retention,
zero-width no-store behavior and cumulative reused-call state remain checked.

The original checker and native model agree on every original before/after input
window, 48-byte context, status/value object, argument and result for 598 calls in
each of three implementations. Four native valid variants, including wide data/
text and a full private stack frame, pass 2368 placements. The starter and all
eight known faulty readers are rejected across 5328 further placements; the
stale-result fault is rejected by the two reused-cursor cases. Actual plugin
parsing retains all eight profiles and combines both bounded final checks.

Upstream implementation commit `6d50e406` passes the full 293 CLI / 1329 plugin
regression, 100% plugin package line/branch coverage and generated-parser ledger
gates. Two additional actual-profile parser tests pass in test-only commit
`34e7db91`. Public tooling passes 622 tests. All 41 unrelated MARS edits are
preserved. Exact pinned CLI, failed private fixture attempts, original comparisons
and qualification receipts are retained in private
`native-h263-reader-foundation-01`.

This is the stream unit's independent grading foundation. The native stream
experiments, student archive, actual IDE first/resume delivery and written-lab
integration remain next. No new whole-clip interoperability, inverse-accuracy,
PDF/accessibility or learner-review result is claimed by this step. Existing
stream and publication evidence above remains historical and separate.


### Native stream student-unit draft, 23 November 2026

`revision/lessons/035-h263-stream` now contains a complete native draft with
28 activities: twelve inline predictions, eighteen Debug stops across six worked
programs, one ordinary Run and nine staged/final reader assessments. Small actual
writer, coefficient, macroblock and header experiments precede the complete clip
caller. A bounded arithmetic experiment prepares the independent reader without
publishing its implementation. The supplied helpers and coding tables are exact
copies of the existing worked encoder; no codec instruction body changed.

The actual clip caller runs eight generated QCIF pictures, then separately exposes
truncated and trailing-byte requests. Return status/count pairs are `0/8`, `3/1`
and `3/2`. The final pair distinguishes accepted count from whole-request success.
Relative paths work from the assembly directory in ordinary MARS Run/Debug. The
bundled Python-generated raw data has rate 5000/1001 fps and duration 1.6016 seconds.
The public inspection tool verifies the actual full output and accepted prefixes
using strict profile parsing and FFmpeg, refuses an existing report, and explicitly
retains the failed status of the latter requests.

Twelve core probes verify all eighteen boundaries in both byte orders, exact
payloads and guards, forbidden buffer access on rejection, accepted reference
pointers, closed descriptors and complete output bytes. An actual fresh CLI run
uses real files and matches the virtual-file observations byte for byte. Independent
decoding checks eleven pictures / 33 plane observations: maximum local/decoder
difference two and minimum finite PSNR about 45.865 dB. This limited default-clip
evidence does not renew the separate full inverse-accuracy gate.

The exact exported reader starter passes all 592 cases with three private bodies,
including relocated wide data/text; its unfinished form fails all 592. The public
archive contains no completed reader. Six public artifact tests bring the full
tooling suite to 628 passing tests. Two actual importer/native-PSI Jupiter tests
preserve all activities and thirty source anchors. The explicit 31-file archive
is approximately 78 KiB. Test-only MARS commit `7b28cb6d` adds its archive and tests;
production simulator/plugin code and all 41 unrelated edits remain unchanged.

This completes the student-unit draft and executable qualification. First/resume
IDE walkthroughs, new native publication capture and direct written-lab integration
remain next, followed by course-wide acceptance. No new graphical IDE launch,
publication PDF, accessibility validation or human learner review is claimed by
this draft. Exact sources, tools, process records and output reports are retained
in private `native-h263-stream-unit-01`.


### Native IDE and written stream integration, 23 November 2026

Two actual fresh IntelliJ processes now verify first use and saved resume of the
complete native stream unit. First use records forty observations and forty-five
photographs; resume records twenty-eight and twenty-nine. Both sequences exercise
ordinary Debug and Run on the real generated files, then pass 1248 overlapping
reader placements. Thirteen quiz attempts and seven edited sources are retained;
all sixteen execution checks are renewed. Unsaved checking, source invalidation,
native save/close/disposal and preserved assessment attempt history are verified.
No private body is reinjected on resume. Each process's eleven installed JARs
match the two immutable pinned archives.

The actual IDE output after each completed sequence passes the imported public
inspection tool: eleven accepted pictures per process, including the one-/two-
picture prefixes of failed requests. That external decode is scoped to retained
files after each sequence; Run replaces its preceding Debug output. The original
whole-request status remains failed for truncated/trailing input. Neither a host
encoder nor the authoring callback fills the output stream.

Directly edited native LaTeX now connects all six experiments, the three Run
status/count pairs, actual independent file inspection and one continuing reader
through both final checks. A new unchanged light-theme photograph has native
annotations for the actual trailing-input frame, status three and accepted count
two. The preceding generator exercise is clarified as optional because the native
unit bundles the same generated pixels. Human prose and the initial draft remain
preserved.

The separate authoring plugin passes 144 Jupiter tests, public tooling 628, and
six publication guards. Both pinned plugins pass compatibility verification on
IntelliJ 2026.3 without internal API use reported. The accepted lab proof has
228 pages, with the stream lab on 222–228 and its figure on 225. Pages 221–228
were visually reviewed, including grayscale figure review. After the transition
clarification, page 221 was reviewed again; every stream page and its grayscale
figure remained pixel-identical. Final exact PDF/UA-2 validation passes 1728 rules
and 1,076,661 checks with zero failures. All 41 unrelated MARS edits remain intact.
Exact binaries, native trial records, retained host outputs, independent decode
reports, proof pins and previous transition proof are retained in private
`native-h263-stream-ide-01`.

Native stream delivery and written integration are complete. The full curriculum
goal remains active pending the course-wide requirements, prerequisites,
cross-references, distributions and evidence acceptance audit. Human author,
learner and assistive-technology reviews remain open; no renewed full inverse-
accuracy gate is claimed by this integration.
