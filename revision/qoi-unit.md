# Decoding with a state machine

The current native draft is complete through automated first-use/resume verification:
ten inline predictions, ten Debug activities / 29 boundaries, one Run and seven
assessment groups retaining 205 original cases. The directly composed lab companion
has 212 pages, with this chapter on 141–149. Current evidence and scope are recorded
in the later native-guide sections below. Earlier page/test counts describe the
historical QOI proof, not the current full companion. Human learner review remains
separate.

## Earlier composition and verification

Chapter 20 revises original project `030-qoi`, following bounded file I/O and
checksums. The main proof has 132 pages; this chapter occupies 126–132. Its written
lab occupies 116–122 of the 122-page companion. Both are native LaTeX compositions.
The original manuscript remains unchanged.

## Prerequisites and outcomes

Readers need bounded parsing, byte order, framebuffer words, full-width calling
conventions and the educational file services established in the preceding units.

- Distinguish serialized header/chunks/marker from native metadata and RGBA words.
  Explain why an RGB preview cannot establish alpha preservation.
- Derive every transition in the eight-pixel example, including biased DIFF/LUMA,
  modulo-256 reconstruction, complete-tag priority and RUN cache insertion.
- Maintain independent source and pixel bounds, validate before publication, and
  reset predictor, cache and history for both passes and repeated calls.
- Explain conservative reservation versus actual encoded size, legal alternative
  encodings, file ownership and the difference between round-trip consistency
  and independent interoperability evidence.
- Reflect output within rows while retaining forward decoder state, exact RGBA,
  error precedence and the whole-output failure guarantee.

## Student materials and qualification

`labs/files/qoi-codec.asm` is a complete six-function worked codec and file caller.
Its eight-pixel input uses every chunk type. The host pipeline in
`tools/run_revision_qoi.py` generates trace, sprite or bounded pattern input,
executes actual host-file load/decode/encode/save, verifies strict format and RGBA
with `revision_qoi_format.py`, and retains raw RGBA, RGB preview and a receipt.
A longer old output exercises successful truncation. The trace shrinks from 38 to
35 bytes through a legal wrapping DIFF; the 8-by-8 sprite occupies 61 bytes.
These are byte-size observations, not execution-time measurements.

Reference, alternative and above-32-bit-address implementations each pass 191
original public and 1394 private cases. Nine written cases check the actual
exposition, including all eight words, RUN cache slot 53, late rejection, alpha
under descriptive channel flags, the maximum 20502-byte file, sprite and reflection.

`labs/files/qoi-mirrored.asm` deliberately leaves the independent entry unfinished.
Its 205 public checks accept both post-decode row swaps and direct reflected
emission. Both designs and a wide-address variant also pass 786 expanded cases
each: 49 dimension pairs, 160 correlated offset/shape combinations, all 256 alpha
values under both channel flags, 64 deterministic images and a workspace-preserving
same-session retry. These are explicit finite domains, not all possible streams or
all independent pointer combinations. The unmodified starter fails all 205 cases.
An initial INDEX-zero input and a two-pass cache-poisoning counterexample check
state initialization independently of the familiar trace.

Twenty-five deliberate semantic faults assemble and fail behavioral checks.
A separately bound zero-progress-write fault is also rejected: three valid codecs
pass four thresholds each while the faulty writer reaches the instruction limit.
The latter uses the already tested optional zero-write grading facility; it does
not require a new upstream change in this unit. Complete independent solutions,
expanded cases and deliberate faults remain in the private repository.

Seven actual host runs cover trace, sprite, pattern, maximum input, descriptive
flags with alpha, and both reflection strategies. A pinned official QOI decoder
independently reconstructs all four components and flags for their outputs. This
is positive interoperability evidence; it does not claim that the official decoder
applies the local validator's strict rejection policy to malformed files.

## Publication evidence and limits

Five actual light-theme native Debug captures show RGBA words, RUN cache insertion,
untouched rejected output, reflected three-pixel rows and the decoded sprite.
The first capture phase completed four Memory observations; the Bitmap capture
required a second fresh IDE launch after adding its source tool settings. Each
launch ran the actual clean, cleanSandbox and runIde tasks. The sprite receipt
checks all 64 displayed RGB cell interiors exactly. Separate executable checks
establish full RGBA and whole-buffer guarantees. Screenshot pixels are unchanged;
all titles, crops, outlines and semantic descriptions are native LaTeX.

Two actual IntelliJ highlighter exports extend the existing 820 records to 822;
the previous records remain unchanged. The authoring plugin passes 104 Jupiter
tests, and public tooling passes 405 tests, including fourteen host-format cases.
Final pages were reviewed in color and representative code/figure pages in gray.
Both exact final PDFs pass PDF/UA-2 automated validation. That does not establish
human readability, assistive-technology usability or an independent learner walkthrough.

At that earlier proof stage, this unit used ordinary Run and Debug and its native
guided lesson was not implemented.
The full curriculum goal remains active; the next main unit develops CBOR after
these bounded parser and state-machine foundations. Original projects 017 and 018
remain assigned to expression/bytecode companion labs in the restructuring plan.

## Native grading foundation, 23 November 2026

The original 205 reflection cases now have an authored native data equivalent,
with original six-million/100,000 instruction allowances, memory permissions,
complete enclosing access-history capacity, preserved state and actual caller
termination. A field-by-field export comparison covers every original case.
See [the native QOI grading contract](../docs/native-qoi-checks.md) for versioned
formats, limits and verification scope. This foundation does not complete the
QOI guided lesson, its desktop walkthrough or refreshed lab composition.
The earlier page and tool-test counts above describe the historical QOI proof.

## Connected native guide draft, 23 November 2026

`revision/lessons/030-qoi` now delivers ten inline predictions, ten Debug
activities with 29 ordered boundaries, one ordinary Run and seven independent
assessment groups retaining all 205 original cases. Complete worked copies
select decode, bound, validation, descriptive flags, late marker failure,
capacity failure, initial INDEX, cache reset and a short-read file loader.
Every exported source is public worked code or the unchanged independent starter;
no private reflection implementation is included. The original CLI checker is
retained as an optional route.

Eleven executable probes verify every Debug register boundary and final caller
result, exact output and whole padding, complete untouched destinations on errors,
validation before publication and descriptor closure. These are executable core
observations, not desktop walkthroughs. The actual archive is also checked through
the production importer and source pointers through native PSI fixtures.
Predictions select their own worked source using native lesson format 11.
The separate authoring plugin is being prepared to follow those same source
choices. The actual first/resume IDE exercise is now verified, as recorded below. The
connected written-lab revision retains the earlier genuine worked-example figures.

## Connected written companion proof

The lab now connects the guided observations to length accounting, caller
publication, predictor/cache transitions, changing register meanings, two-pass
initialization, late rejection, positive short reads and independent row placement.
A native table accounts for the sixteen chunk bytes and eight output pixels.
The local source remains directly edited LaTeX. The existing five genuine
light-theme worked-example captures and their native annotations are retained.
They do not claim a new guided IDE walkthrough.

The compiled companion has 212 pages, with QOI on 141–149. Pages 140–150 were
reviewed in color and table/figure pages 142–145 in grayscale. The build reports
no missing glyph, font substitution or overflow. Minion Pro, Noto Sans Math and
JetBrains Mono remain embedded. The exact proof passes PDF/UA-2 validation:
1,728 rules and 1,000,842 checks, with zero failures. This is assistant layout and
automated accessibility verification; human learner and assistive-technology
review remain separate. The main learning proof is unchanged.

## Verified native desktop delivery

Both dedicated IDE launches run actual `clean cleanSandbox runIde` tasks. The first
walkthrough records 47 observations: ten predictions, 29 Debug boundaries, one Run
and seven assessments. It records one deliberately wrong quiz answer before the
correct answer, rejects the unfinished geometry starter at 0/150, then accepts the
private test implementation across all seven groups. Every worked Run/Debug launch
terminates normally. Nine source edits then invalidate all 18 execution checks.

The fresh-process resume preserves all eleven quiz attempts and all nine edited
source digests, renews every execution check, and increments each assessment group
exactly once. It records 37 observations. All eleven installed plugin JARs match
the pinned archives and accepted IDE logs contain no ERROR/SEVERE entries. The 52
first-use and 38 resumed photographs pass light-theme and byte-digest checks.
These include private-answer captures, so the desktop corpus stays private. The
publication retains the five previously verified public worked-example figures
and their native LaTeX annotations.

Each native project is saved, closed and disposed before stopping only its owned
IDE child with SIGTERM. Child exit 143 and outer Gradle exit 1 are intentional
cleanup outcomes, not natural `runIde` success. No production simulator semantics
were changed for this unit. All 41 unrelated manual edits remain unchanged.
The full regression passes 255 CLI and 1,277 plugin tests with the coverage and
generated-parser gates, followed by a four-test check of the final archive and
prediction-source contract. All 547 book-tool tests and 134 separate-authoring
tests pass. Paired API verification reports compatibility with 91 MARS / 20
authoring experimental usages and no internal API dependency. An initial passing
test run missed coverage on an unused default constructor; that diagnostic is
retained, and removing the unused default restored the full gate.

The next native instructional unit is Chapter 21 CBOR. The QOI draft still needs
the author's and an independent learner's reading where available; the recorded
walkthroughs establish automated behavior, not human usability.
