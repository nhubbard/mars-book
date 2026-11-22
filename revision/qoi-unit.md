# Decoding with a state machine

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

This unit uses ordinary Run and Debug. Its native guided lesson is not implemented.
The full curriculum goal remains active; the next main unit develops CBOR after
these bounded parser and state-machine foundations. Original projects 017 and 018
remain assigned to expression/bytecode companion labs in the restructuring plan.
