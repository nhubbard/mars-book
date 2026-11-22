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
