# Revised ring-buffer written companion

The native supplement is `labs/companions/ring-buffer.tex`, included as section 8.6
of the state-and-buffers lab chapter. The current main proof remains 51 pages; the
lab proof is 35 pages. This companion begins on lab page 30 and occupies pages 30–35.
The lab chapter is now titled “Snapshots and reusable storage” to include both
Life and the queue assignment.

The text explains descriptor fields, logical versus physical record order,
prefix accounting, full/empty states with equal indices, arbitrary-capacity
wraparound, failure preflight, bounded copying and completed-call observation.
It includes a native seven-byte diagram, an exact five-command worked trace and
a native LaTeX figure comparing actual full/empty Memory observations. These
explanations support independent implementation of both queue functions; full
queue solutions remain private.

| Outcome | Written activity | Technical evidence |
| --- | --- | --- |
| Interpret indices and occupied bytes | Derive head=tail for both full and empty | Five-command seven-byte trace and actual native Memory observations |
| Handle arbitrary-capacity wrapping | Compare one-byte advance with record jumps and masking | All 528 valid index/capacity pairs plus complete cursor caller |
| Preserve a queued record on failure | Reject a short destination, then retry | No descriptor/ring/output stores during the failed command |
| Account for every byte | Include prefixes and copy exact binary payloads | Existing kernel footprint checks and rejected prefix/terminator/overread faults |
| Implement equivalent queue functions | Write push_record and pop_record against the interface | Two private implementations pass both original and compact caller checks |

The revised public work area is `labs/files/ring-records.asm`, with the same data,
command sequence, function labels and operation_ready observation contract as
original project 014. Its caller aligns the stack and uses compact transfers;
the unfinished function bodies fail explicitly with break. The archived package
starter and versioned contract remain unchanged. Both private queue implementations
pass 520 kernel and 12 complete-caller cases with the revised caller, as they do
with the archived one. Their queue function bodies are byte-identical between
those caller variants. Both intentionally unfinished starters are rejected.

The separate complete experiment is `labs/files/ring-cursor.asm`. Run its public
`labs/checks/ring-cursor.kts` check with the CLI and JDK 25. The printed helper is
an exact suffix of that source. It and a remainder-based alternative pass all
528 valid index/capacity pairs and the complete caller; the mask-based variant
is rejected. This does not exhaust all valid queue histories or descriptor states.

The seven-byte example changes only the supplied descriptor initialization and
command inputs relative to a filled version of the revised caller. Five operations
produce the exact printed table. The failed pop executes no descriptor, ring or
output stores. Four actual light-theme Debug captures observe full, failed-pop,
retry and empty states, using native breakpoint conditions and supported views.
The two published PNGs show only the shared public caller and memory; private
function bodies are outside the viewport. Crops and highlight boxes are native
LaTeX, with unchanged originals and coordinate receipts.

An initial successful archived-caller capture batch is retained privately; final
publication binds the successful compact-caller batch. All eleven installed JARs
match the previously verified plugin archives. No authoring code or grader API was
added to MARS. Existing store/access observations already express the required
failure and footprint contracts. The separate authoring plugin passes 104 Jupiter
tests. Its fresh cursor highlight preserves all 792 earlier records (793 total).

Five additional meaningful queue faults are rejected: lost prefix size, an unchanged
but forbidden descriptor store before pop failure, an appended output terminator,
cleared ring bytes during pop, and an extra source load for an empty payload. The
original package validates and builds; public tooling passes 391 tests. Final native
composition, automated PDF/UA-2 and visual/grayscale review qualify the proof.

The companion is written and technically qualified, not an implemented native
interactive ring lesson or human learner/assistive-technology validation. Remaining
companion revisions, main chapters and instructional-system work remain open.
Original manuscript and unrelated MARS edits are preserved. The proposed opening
MARS-versus-hardware explanation remains deferred.
