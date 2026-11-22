# Revised chapter prerequisites and assessment map

This is an editorial map of the current native reading sequence, not a source for
composing the books or generating lessons. The LaTeX includes remain authoritative.
Each numbered entry names three assessable outcomes and an existing written
activity that can demonstrate each one. An activity's presence is not evidence
that a student has achieved the outcome. Author walkthrough and representative
learner review remain open.

Chapter numbers refer to the revised main book and its matching written lab.
Prerequisites identify concepts needed on entry. Later chapters may reuse earlier
concepts without repeating every dependency here. Elective companion assignments
are not prerequisites for the main sequence. Assembly vocabulary and instruction
semantics introduced by an example must be explained in that chapter before the
reader is expected to use them independently.

## 1. Your first assembly program

Prerequisites: basic variables, expressions and integer arithmetic; no assembly.
[Written lab](../revision/labs/first-program.tex).

1. Trace an input through two instructions: predict the intermediate and final
   register values in “Observe two different additions.”
2. Distinguish a computed value from an output argument and service selector:
   locate the first wrong state in “Investigate a plausible wrong result.”
3. Author a related calculation: implement and explain `3x+1`, then change its
   temporary register in “Work without the construction hints.”

## 2. Registers and integer representation

Prerequisites: chapter 1's registers, output arguments and execution boundaries.
[Written lab](../revision/labs/integer-representation.tex).

1. Interpret one bit pattern at a stated width: derive both meanings of 150 in
   “Make two predictions from one pattern.”
2. Distinguish extension from formatting: inspect complete registers and expose
   the substituted-copy fault in “Choose a test that exposes a fault.”
3. Implement deliberate narrowing: produce 256 and then zero for the two input-255
   variants in “Work without the conversion recipe.”

## 3. Addresses and memory

Prerequisites: chapter 2's byte widths, hexadecimal and extension; chapter 1's tracing.
[Written lab](../revision/labs/addresses-and-memory.tex).

1. Distinguish an address from the value stored there: explain the registers in
   “Separate the address from the color.”
2. Derive the bytes changed by a store: distinguish wrong-address and wrong-width
   faults in “Distinguish two faulty stores.”
3. Modify one component and retain an independent copy: predict and verify source,
   destination and guards in “Work from a new component.”

## 4. Decisions and repetition

Prerequisites: chapter 3's loads, stores and byte-addressed objects; chapter 2's masks.
[Written lab](../revision/labs/decisions-and-repetition.tex).

1. Explain a conditional transfer and its execution boundary: follow taken and
   not-taken cases in “Exercise the taken path.” Compact branches and forbidden
   slots are introduced here, not assumed from another course.
2. State a loop invariant and justify its reads: account for the empty range and
   final index in “Follow one complete iteration.”
3. Preserve the device protocol with a different loop representation: implement
   “Implement an end-pointer version,” including zero stores for an empty range.

The MarsBot companion adds its own device protocol; its ports and movement rules
are not inherited from the counter.

## 5. Functions and the stack

Prerequisites: masks from chapter 2, memory from chapter 3 and compact branches
from chapter 4. Logical shifts, OR and XOR receive explanations in this chapter.
[Written lab](../revision/labs/functions-and-stack.tex).

1. Separate a function result from its return continuation: trace the caller and
   nested helper in “Separate a result from a continuation.”
2. Explain why values survive a call: account for every full-width saved slot and
   restored pointer in “Account for every saved byte.”
3. Implement a related non-leaf operation: reverse and invert a byte while honoring
   preservation in “Write a related function independently.”

The reading-order audit corrected the chapter's former claim that all of these
bit operations were prerequisites. Its existing reversal trace and exhaustive
independent exercise now follow an explicit explanation of the operations.

## 6. Arrays and images

Prerequisites: chapter 3's memory layout, chapter 4's loops and chapter 5's calls
and register preservation. [Written lab](../revision/labs/arrays-and-images.tex).

1. Translate a source bit into a destination address: use the asymmetric pattern
   in “Predict one asymmetric row.”
2. Distinguish row stride from element size: identify the first illegal address
   in “Investigate an address error.”
3. Change row order while preserving within-row orientation and caller state:
   implement “Draw a vertical variation independently.”

Clipping adds signed rectangle intersection and physical stride in its companion;
it is not required to understand the next chapter's bounded frame.

## 7. Integer algorithms

Prerequisites: signed comparisons, chapter 5's function contract and chapter 6's
pixel addressing. [Written lab](../revision/labs/integer-algorithms.tex).

1. Predict the canonical raster, including endpoint and tie cases: complete
   “Predict cells and addresses.”
2. Explain dependent updates from a saved decision: compare the snapshot and
   modified accumulator in “Separate the snapshot from the accumulator.”
3. Adapt the operation without changing its geometry: implement “Add a dash
   pattern independently,” including vertical and coincident endpoints.

## 8. State and buffers

Prerequisites: chapter 6's row-major loops and chapter 5's helper-call preservation.
[Written lab](../revision/labs/state-and-buffers.tex).

1. Derive a next state from one retained snapshot: predict the oscillator and
   changed cells in “Predict cells, counts and allocation roles.”
2. Distinguish allocation identity from buffer role: inspect both sides of
   publication in “Stop before and after the role exchange.”
3. Compute a different observable without changing the transition: implement
   “Return a changed-cell count independently,” distinguishing changes from live cells.

The ring-buffer companion adds record wrap and occupied-byte state; it is optional.

## 9. Algorithms and their cost

Prerequisites: array traversal, nested loops, signed comparisons, function
preservation and chapter 8's complete-state observations.
[Written lab](../revision/labs/algorithms-and-cost.tex).

1. Distinguish ordered output from preservation of all input values: explain the
   transient half-exchange in “Inspect a half-exchange and a publication.”
2. Compare work within a stated scope: separate sorting from rendering in
   “Measure the work that answers your question.”
3. Apply stability as an independent requirement: implement “Write a stable sign
   partition independently,” using equal keys and signed extremes as counterexamples.

The maze companion introduces FIFO search and shortest-path reasoning separately.

## 10. Fixed-point arithmetic

Prerequisites: signed widths and shifts, bounded image coordinates and function
contracts. [Written lab](../revision/labs/fixed-point.tex).

1. Retain fractional state while deriving a display position: distinguish model
   state and quantized pixels in “Diagnose quantization and reflection faults.”
2. Bound intermediate arithmetic before reducing precision: perform “Compare a
   wrapped sum with an exact wider sum.”
3. Implement a stated rounding rule across signed boundaries: complete “Implement
   signed interpolation independently,” including negative half ties and endpoint weights.

## 11. Memory access and locality

Prerequisites: chapter 6's rectangular layouts, bounded loops and preserved function
state. The cache model is introduced here.
[Written lab](../revision/labs/memory-locality.tex).

1. Derive rectangular source and output addresses: complete “Predict addresses in
   a rectangular matrix.”
2. Explain a cache observation under a specified model and initial state: compare
   placements in “Compare equal-capacity placements.”
3. Separate the data mapping from traversal choice: implement “Implement clockwise
   rotation independently,” using a nonsquare example to distinguish it from transpose.

## 12. Bounded interfaces and independent implementation

Prerequisites: byte loads, unsigned extension, array bounds and function contracts.
[Written lab](../revision/labs/bounded-interfaces.tex).

1. Distinguish declared, available and consumed lengths: complete “Predict bytes,
   fields and three lengths.”
2. Justify error priority and delayed publication: inspect failure and acceptance
   in “Inspect acceptance before and after publication.”
3. Implement a different bounded input rule: complete “Implement a bounded delimiter
   parser independently,” including the 32-byte payload/next-byte boundary.

The UTF-8 companion adds an encoding-specific state machine; later formats do not
require completing that elective.

## 13. Measuring a complete kernel

Prerequisites: chapter 11's matrix layout and cache model, wide signed intermediates,
initialized storage and function preservation.
[Written lab](../revision/labs/measuring-programs.tex).

1. Explain an exact reduction across blocks: inspect retained partial sums in
   “Inspect the handoff between reduction blocks.”
2. Define a reproducible measurement interval: perform “Reproduce cold, warm and
   reset observations,” identifying preparation and rendering separately.
3. Transfer the reduction to a new shape: implement “Implement a matrix-vector
   reduction independently,” including zero rows and zero reduction width.

## 14. Working with MSA lanes

Prerequisites: masks and shifts, byte layouts, bounded input/output spans and the
scalar-before-measurement discipline. MSA registers and lane operations are new here.
[Written lab](../revision/labs/working-with-msa.tex).

1. Derive a lane arrangement from actual loaded bytes: complete “Predict the first
   gathered vector.”
2. Prove group and remainder bounds together: perform “Prove the gate and the
   remainder together,” including incomplete vector groups.
3. Derive a new packed representation: implement “Implement RGB555 without a
   construction sequence,” using a green value that distinguishes RGB555 from RGB565.

## 15. Mixing samples without losing their meaning

Prerequisites: signed halfwords, MSA lanes, scalar tails and bounded accesses.
[Written lab](../revision/labs/pcm-mixing.tex).

1. Distinguish clipping from wrapping and exact endpoint results: complete “Predict
   samples and the two return values.”
2. Explain what a comparison mask counts: perform “Read the mask as evidence, then
   change its unit.”
3. Implement saturated subtraction with permitted exact aliases: complete “Write
   and test saturated subtraction independently,” including tails and identical inputs.

## 16. Filtering a sequence of samples

Prerequisites: chapter 10's scale and rounding, chapter 15's signed PCM and clipping,
and MSA widening. [Written lab](../revision/labs/filtering-signals.tex).

1. Derive a window's exact sum before one conversion: use the impulse in “Explain
   an impulse before stepping the loop” and the widths in “Observe widening before multiplication.”
2. Retain the right raw history between calls: complete “Keep history when dividing
   work into calls.”
3. Change output spacing while preserving the filter definition: implement “Implement
   a decimating filter independently,” deriving the last input sample and split-call advance.

## 17. Compact integer encodings

Prerequisites: full-width differences, bit operations, bounded parsing and committed
state. [Written lab](../revision/labs/compact-encodings.tex).

1. Derive canonical bytes from signed readings: complete “Recover the wire from
   the readings.”
2. Distinguish a candidate from the accepted prefix: explain a rejected value in
   “Distinguish scanning from committing.”
3. Implement a read-only scanner with the same error rules: complete “Build a
   read-only scanner independently,” including a successful call after an error.

Base64's regrouping exercise is a separate companion, not a requirement for files.

## 18. File boundaries and binary images

Prerequisites: chapter 12's validation/publication discipline, RGB layouts and exact
byte counts. File handles and partial transfers are introduced here.
[Written lab](../revision/labs/files-and-formats.tex).

1. Distinguish text-header syntax from arbitrary raster bytes: perform “Put
   punctuation in the raster.”
2. Explain ownership and progress through a complete file operation: complete
   “Account for partial transfers and ownership.”
3. Publish a different image through the same format: implement “Write a
   complementary-color serializer,” preserving the header and failed outputs.

## 19. Checksums and streaming state

Prerequisites: byte/word operations, loop invariants and chapter 18's file progress.
[Written lab](../revision/labs/checksums-and-streams.tex).

1. Distinguish raw continuation state from a finalized checksum: perform “Continue
   the returned state through another method.”
2. Include reusable preparation in a fair comparison: complete “Prepare a table,
   then account for its cost.”
3. Apply checksum and bounds reasoning to a new interface: complete “Write the
   bounded chunk checker,” including rejected capacity and checksum cases.

## 20. Decoding with a state machine

Prerequisites: bounded validation, image coordinates, bit fields, file handling and
explicit state. [Written lab](../revision/labs/decoding-state-machines.tex).

1. Explain state changes for each operation, including remembered alpha: complete
   “Follow all six operations, including alpha.”
2. Distinguish a plausible prefix from an accepted image: perform “Reject a late
   error without publishing a convincing prefix.”
3. Change placement without reversing decoding state: implement “Place reflected
   pixels while keeping forward decoder state,” including odd widths and runs across rows.

## 21. Reading nested data

Prerequisites: bounded byte parsing, nested calls, explicit saved state and delayed
publication. [Written lab](../revision/labs/nested-data.tex).

1. Distinguish a head's description from available payload: complete “Predict a
   head without assuming its payload exists.”
2. Retain unfinished container work during traversal: complete “Follow a child
   while retaining its parent's work.”
3. Count complete top-level items under the specified failure contract: implement
   “Count top-level items in your own scanner,” including an incomplete final item.

Expression and bytecode companions introduce their own language semantics. Neither
is a prerequisite for the cryptographic or codec chapters.

## 22. Transforming words in parallel

Prerequisites: modular word arithmetic, MSA shuffles, bounded byte output and
nested-call preservation. [Written lab](../revision/labs/word-oriented-transformations.tex).

1. Trace arithmetic dependencies independently of position selection: perform
   “Separate selection errors from arithmetic errors.”
2. Derive a lane shuffle from selected word positions: complete “Explain a shuffle
   before executing it.”
3. Implement the published primitive, block and restart interfaces: complete
   “Finish with an implementation you can explain,” then specify continuation
   separately on paper rather than changing the restart contract silently.

SHA-256 and RSA are optional companions with additional algorithm-specific
prerequisites. They do not gate audio or video work.

## 23. An audio file through the whole program

Prerequisites: chunk bounds and file ownership, channel/sample layouts and chapter
16's filtering history. [Written lab](../revision/labs/audio-files.tex).

1. Distinguish file bytes, samples and frames: perform “Predict the parser's next address.”
2. Explain channel-specific continuation: complete “Separate the two channel histories.”
3. Reverse frame order while preserving bytes within each frame: implement “Write
   a frame reversal that can share its buffer,” including exact in-place operation.

## 24. Decode a PNG through several representations

Prerequisites: checksum state, bounded bit/byte interfaces, decoder state, image rows
and MSA width reasoning. Deflate and PNG prediction are developed in this chapter.
[Written lab](../revision/labs/png-pipeline.tex).

1. Track a bound through several representations: complete “Make a boundary ledger
   before debugging.”
2. Explain history and reconstructed-neighbor dependencies: perform “Trace history
   and row dependencies.”
3. Implement the forward counterpart of row reconstruction: complete “Implement
   the forward row filter,” then verify exact bytes and inversion rather than
   relying on a convincing displayed image.

## 25. Retain state between lossless video frames

Prerequisites: validated image publication, state snapshots, bounded records and
whole-file validation. [Written lab](../revision/labs/lossless-video.tex).

1. Apply one record to the intended prior frame: complete “Predict one update before
   applying a record.”
2. Explain the relationship between validation, an index and seeking: perform
   “Keep a directory tied to its validated file.”
3. Distinguish changed-pixel count from rectangle area: implement “Implement minimal
   exclusive change bounds,” including identical frames and high-bit differences.

## 26. Predict pictures from reconstructed samples

Prerequisites: frame/reference ownership, signed fixed-point rounding, exact vector
accumulation and bounded plane addressing. Motion, transforms and the restricted
H.263 reconstruction rules are new material.
[Written lab](../revision/labs/predicting-pictures.tex).

1. Explain fractional prediction at a bounded origin: complete “Test fractional
   prediction before the transform.”
2. Distinguish source residuals, selected levels, reconstructed coefficients and
   reconstructed samples: complete “Keep four representations visible.”
3. Implement clipped prediction-plus-correction with explicit strides: complete
   “Write the independent reconstruction function,” including exact permitted aliases.

## 27. Produce a stream another decoder can read

Prerequisites: chapter 26's reconstruction agreement, bit fields, bounded contexts,
file progress and independent output inspection. Restricted H.263 syntax is
explained here. [Written lab](../revision/labs/producing-video-streams.tex).

1. Relate a field sequence to the bytes actually emitted: complete “Trace bits
   before reading them as bytes” and recover levels from the coded block.
2. Distinguish completed local reconstruction from accepted file progress: perform
   “Diagnose failure from the accepted prefix,” retaining both output artifacts.
3. Implement a bounded reader and use it on real syntax: complete “Implement the
   independent bounded reader,” then “Use the reader to explain an actual header.”

## Review and delivery implications

This map identifies assessment opportunities; it does not claim a completed
student trial or replace each unit's technical verification note. During the
author walkthrough, record the first unexplained prerequisite before supplying
help. Update the native explanation and this map together when that diagnosis
changes a chapter's responsibilities.

The existing first-unit native lesson can enforce its supported questions and
execution checks. Later activities here currently use ordinary Run and Debug.
Their presence in this map does not register native lessons, make old package
steps match the revised chapters, or enable remote delivery. Those changes must
bind actual lesson content, editor files and behavioral checks explicitly.
