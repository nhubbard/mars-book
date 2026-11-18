# Separate validation from later text processing

A well-formed encoding says how bytes represent scalars. It does not decide whether the resulting text belongs in a filename, identifier, terminal message, or application protocol. This scanner accepts all structurally valid scalar encodings, retains no normalization state, and returns no glyphs. Keep its purpose explicit when composing it with the packet parser or record queue from earlier chapters.

<div class="MARSExercise">

Change chunk boundaries without changing the stream Deliver the default bytes one at a time, then in two chunks at every possible split. Finalize each run at the same logical end. Compare total, scalar count, and the final status. Intermediate need and bounds can differ at different checkpoints, but the same byte prefix at a checkpoint must produce the same state regardless of how it was delivered. Include empty chunks in that comparison.

</div>

<div class="MARSExercise">

Report two positions precisely Place a valid multibyte lead at the end of a chunk and supply an impossible next byte in another. Report the sequence start, detecting byte, and current-call accepted count separately. Repeat with no next byte and an empty final chunk. Explain why truncation identifies an end position rather than a byte value.

</div>

<div class="MARSExercise">

Add scalar output deliberately Extend the interface to return decoded scalar values in a separate bounded array. Specify destination capacity, partial progress, output ownership, and error effects before adding bit accumulation. A four-byte encoding produces one scalar value; a UTF-16 output interface would instead sometimes need a pair of code units. Keep either extension separate from this submission’s counting contract.

</div>

A recovery decoder would also need a policy for replacement and resynchronization. The current scanner halts at the first error and does not consume its detecting byte. Do not skip an arbitrary fixed number of bytes and claim that recovery is implemented. Use the Unicode conformance rules for the intended conversion policy if you undertake that later design.
