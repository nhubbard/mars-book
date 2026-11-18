# PCM construction draft

This companion supports the directly editable native chapter at drafts/pcm-mixing.tex.
The complete student package and interactive lesson runner are still in development.
The main book still counts nineteen complete text packages. No IDE screenshots have
been captured for this chapter.

Start with main.asm. Replace one marked region at a time using the native text. Keep
the markers and labels. The provided entry initializes both result registers and the
return uses ra. The initial vector placeholder goes to the scalar tail; other unfinished
regions return provisionally. mix_wrap16 is supplied unchanged as a different arithmetic
rule for comparison. The starter intentionally fails final behavior.

steps.json is a companion transcript with quizzes and two available hints per step;
it does not generate or constrain the native publication. Instructor progression and
hint controls are a future runner capability. Correct-choice keys remain private.

## Repeat a construction observation

inputs.json contains forty-eight exact input recipes, named by step and observation.
Use the cumulative program after that step's edit. From the public repository root:

```sh
python3 drafts/pcm-construction/prepare.py s05-1 \
  --source /absolute/path/to/your-cumulative-main.asm \
  --output /tmp/pcm-s05-1.asm
```

The destination must not exist; the command preserves your source. Open the result in
MARS and start a fresh Debug execution with a breakpoint on mix_ready. This is before
the first caller nop and before processed/clipped are stored. Each recipe gives its
complete signed left/right arrays; their length is count. The preparation command
replaces each source arena with those samples and zero padding to 192 bytes, fills all
192 output-C bytes with a5, and leaves guards, output D and metadata unchanged. Byte
initializers use signed notation for bytes above 127. Result bytes are read unsigned.

For manual preparation, a source array uses `.half` with the listed signed samples and
`.space 192-2*count` bytes of trailing storage. For count zero, use only `.space 192`.
Fill pcm_c with 192 bytes of -91. Do not add these bytes ahead of existing arena data;
replace the old contents between that arena label and its `_end` label. Set count to
the array length. Keep all other source lines. These edits are observation inputs;
restore the default starter inputs for the complete default comparison.

Each recipe's registers, vector lanes, outputPrefix and optional advance describe
construction state at mix_ready. Vector lane values are raw unsigned bit patterns at
the stated width; negative scalar expectations are signed full-register values. A
missing expected register is unconstrained. An empty outputPrefix means no output was
written, and every later byte remains a5. advance applies equally to a0, a1 and a2.
The private checks repeat these observations with source offsets three/five and output
seven, separately from the aligned caller. Illustration temporaries are not requirements
for an equivalent free-writing solution.
