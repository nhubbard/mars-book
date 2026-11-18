# Reproducible PCM observations

## Repeat a construction observation

inputs.json contains forty-eight exact input recipes, named by step and observation.
Use the cumulative program after that step's edit. From the extracted package directory:

```sh
python3 prepare.py s05-1 \
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
