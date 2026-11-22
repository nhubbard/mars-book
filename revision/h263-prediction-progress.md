# H.263 prediction unit: implementation checkpoint

Chapter 26 and its written lab are being composed directly in
`main/predicting-pictures.tex` and `labs/predicting-pictures.tex`. They are not yet
included in the revised publication entries. The current released proofs still
end with Chapter 25. The earlier reconstruction editorial pilot remains unchanged.

The complete worked `labs/files/h263-encoder.asm` uses compact branch/call spellings
while retaining observation nops and the original callable interfaces. Numerical
qualification has been renewed for reference, alternative and high-code placement:
42 public/private domain runs, 11760 passing cases across SAD, search, prediction,
six-block coordination, transforms, quantization and local block reconstruction.
The full inverse Annex A gate passes all twelve variant/mode/byte-order combinations:
720000 actual inverse blocks and 72 exact-zero calls. These checks are not whole
stream interoperability or complete encoder performance measurements. The syntax,
picture and file pipeline will receive separate renewed verification in Chapter 27.

The independent `h263-reconstruct8.asm` has a complete public contract and unfinished
body. It combines prediction bytes and signed inverse halfwords, clips only after
addition, counts strictly clipped samples and supports an exact prediction alias.
Full-width strides, active-row access and late invalid-sample retention are explicit.
Validation/replay and private staging implementations, plus high-code/data placement,
pass 1950 grading cases. Six ordinary-demo cases verify every output and count in
both byte orders. The starter fails all 138 public cases as expected. Seven assembled
faults are rejected: unsigned inverse values, byte wrapping, endpoint miscounting,
early publication, narrowed strides, padding reads and an incorrect alias shortcut.
Instructor bodies and expanded checks remain private.

One actual IntelliJ highlighter export extends the preserved 833-record catalog to
834, with all 105 authoring Jupiter tests passing. No new graphical H.263 capture or
revised Chapter 26 proof is claimed at this checkpoint. Native diagrams/captures,
proof layout, automated accessibility, public-tooling/package checks and archival
publication remain in progress. Advanced native lesson delivery and human reviews
remain open; the full curriculum goal is active.
