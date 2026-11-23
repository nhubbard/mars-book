# Follow H.263 prediction and local reconstruction

Read Chapter 26 and its written lab. These experiments use the supplied numerical
routines before assigning one independent reconstruction function. The following
stream unit handles actual H.263 syntax, file publication and independent decoding.
A successful numerical experiment does not establish stream interoperability.

Begin with `worked/sad-windows.asm`. Answer predictions inline in MARS Learning,
then use ordinary Debug at the named boundaries. Inspect state before executing
the anchored instruction. Finish each process before moving to another activity.
If you alter state or backstep while exploring, start a fresh forward Debug
session to record the required sequence. The yellow lesson pointer names a source
location; the selected debugger frame identifies where execution is paused.

Five sources contain unchanged supplied helpers and short new callers. The
sixth is explicitly an arithmetic experiment, not an implementation of your
independent function. No required experiment reads a host file, requests a clock
or needs external media. Static arrays make each observation repeatable. You
need not memorize the helper bodies; follow the caller and the named boundaries.

## Start with admitted samples, not an allocation size

The SAD experiment gives each sixteen-byte active row a stride of 176. The
smallest containing extent is `15*176+16 = 2656` bytes, while only 256 bytes are
active samples. A padding byte inside that extent is still not an admitted input
sample. Both pointers are deliberately displaced by three bytes; byte-image
pointers do not need extra vector alignment.

Every current sample is 255 and every reference sample zero. The scalar result
is 65280. At `sad_vector_accumulated`, inspect the full w4 vector as eight
halfword lanes. Each contains 8160: two byte magnitudes per row times sixteen
rows times 255. The pairwise operation widened the byte magnitudes before their
accumulation. A wide final scalar result cannot restore carries already lost
in a narrower accumulator.

At `lesson_sad_compared`, compare the saved scalar and MSA results. Then inspect
`lesson_sad_same_window`: the same pointer and same stride select the same
samples, giving zero. A shared pointer with a different stride would not
necessarily identify the same window.

## Derive the search interval and its tie policy

`worked/motion-ties.asm` supplies flat zero-valued data so all admitted scores
tie. The interior request at (16,32) and radius two has five horizontal and five
vertical displacements: 25 candidates. The centre wins a minimum tie. This
result belongs to the required policy, not an incidental traversal order.

The next request moves to (0,0), leaving three displacements on each axis and
nine candidates. At the left edge away from the corner, three times five gives
fifteen. Inspect the returned candidate count at all three labels and the
published displacement/score after the first call. The reference pointer names
the complete luma plane; the current pointer already names its selected block.
Derive bounds before forming candidate addresses.

## Keep prediction and signed residuals distinct

`worked/diagonal-prediction.asm` sets both half phases. The first four reference
neighbours are 128,128,128,96. Their single rounded sum predicts 120, and current
128 minus prediction gives signed residual eight. Four output positions touch
the changed reference sample, so the total magnitude is 32. At
`lesson_prediction_values`, distinguish the predicted byte, signed halfword
residual and aggregate score.

The second input has zero neighbours except one value of one. One final diagonal
rounding gives zero; two independently rounded pair averages can give a different
answer. At `lesson_single_rounding`, inspect the zero result. The current window
is eight by eight, but diagonal reference prediction needs a nine-by-nine halo.
The third call sets an invalid horizontal phase of two. At
`lesson_prediction_rejected`, status one and count zero accompany retention of
the previous caller output. A visible sample is not proof about every byte;
complete memory checks provide that stronger evidence.

## Follow each representation through the block loop

`worked/reconstruction-chain.asm` first calls the actual forward transform on
a constant signed residual of two. At `lesson_forward_dc`, compare residual two
with DC sixteen. The return count is 64 coefficients, whose storage occupies
128 bytes. This is a count of coefficients, not a byte length or a nonzero count.

The complete block call then uses raw current 130, prediction 128 and quantizer
eight. At `lesson_first_reconstruction`, inspect selected level one,
reconstructed coefficient 23, signed inverse residual three and final sample
131. Output regions begin at byte offsets zero, 128, 256 and 384 respectively.
The first three contain native signed halfwords; the last contains bytes.
Equal storage widths do not make level one interchangeable with coefficient 23.

The next current block is 132. Its prediction comes from the first reconstructed
block, 131, rather than the earlier raw 130. Residual one quantizes to zero, and
`lesson_second_reconstruction` still shows sample 131. The decoder can reproduce
that reference state. Explain the quantization loss separately from disagreement
caused by feeding the encoder a reference the decoder does not have.

These constant blocks make the representation boundaries easy to inspect. They
do not replace asymmetric transform tests or the full independent inverse-accuracy
gate. The original numerical checks and accuracy receipts remain separate.

## Decompose negative motion before coordinating planes

`worked/negative-motion.asm` starts with full-luma displacement minus one. An
arithmetic shift gives floor minus one after division by two; signed division
truncates that value to zero. The low phase bit is one. Chroma origin (8,16)
therefore selects integer reference origin (7,15) with both half phases set.
Inspect those quantities at `lesson_negative_origin`.

The actual coordinator then uses constant current/reference QCIF planes. All six
blocks predict 128 with zero residual, giving total magnitude zero. Inspect the
first Y and last chroma records at `lesson_six_blocks`. Each record has 64
predicted bytes followed by 128 residual bytes, making 1152 bytes for six.
A later request changes the last reference chroma stride to 87, below its
required 88. At `lesson_coordinator_rejected`, the preceding caller records
remain. This is an upfront metadata rejection; it is not evidence that this run
failed after successful child calls. The original dependency-failure checks cover
that separate publication boundary.

## Prepare the independent final addition

`worked/signed-reconstruction.asm` isolates halfword interpretation and clipping.
Bits `ff7f` mean minus129 under a signed halfword load and 65407 under an unsigned
load. Adding prediction 128 must first produce minus one, not a wrapped byte.
At `lesson_signed_sum`, inspect the wide sum and lower-clipping flag.

At `lesson_clipped_endpoints`, sum 255 contributes no clipped event, whereas 256
contributes one and must publish 255. The last stop compares full native stride
hexadecimal 0000000100000008 with its low word eight. The complete stride is
outside the interface range. Validate it before row access or narrowing.

Implement only `h263_reconstruct8` in `independent/h263-reconstruct8.asm`. Keep
that same source through sample, alias, metadata, invalid-sample and reuse stages,
then run the final 138-case assessment. The README states the complete interface.
The feedback accepts equivalent validate/replay and private-staging approaches;
it checks exact output, counts, permissions and saved state rather than a private
label or exact instruction sequence.

A metadata error permits no sample reads. An inverse value outside [-256,255]
permits no output stores. This includes a last invalid correction when output
exactly aliases prediction. Validate every correction before publication. Read
only eight prediction bytes and sixteen inverse bytes per row, leaving all gaps
and surrounding bytes intact. Handle ordinary repeated calls without retained
counts or partial prior results.

When edits make earlier execution evidence stale, follow MARS's renewal prompts.
Predictions and hint history remain saved. Guided/free-writing availability and
hints follow the instructor's policy. The normal Run activity is the silent
local block chain; it creates no encoded stream.

For the optional whole-picture work, `tools/fixture.py --output-dir work/qcif`
generates the ordinary raw QCIF input and description in a new directory. The
default is eight pictures at 5000/1001 pictures/s, within the restricted course
limits. The next unit connects raw input to a real H.263 stream and independent
decoding. No instructor solution is included here.
