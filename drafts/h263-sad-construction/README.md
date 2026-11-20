# First H.263 kernel: exact 16-by-16 SAD

The editable native chapter begins in `includes/chapters/035-h263`. Its ten first
steps explain the scalar comparison, unsigned vector differences, widened
accumulation and completed-work measurement. `main.asm` supplies two unfinished
callable entries and an ordinary Run/Debug caller; development status four is
expected until the functions are implemented.

Prepare a new file from **your current working source**:

```sh
python3 drafts/h263-sad-construction/prepare.py --source my-sad.asm --mode 0 --output worked-scalar.asm
python3 drafts/h263-sad-construction/prepare.py --source my-sad.asm --mode 1 --output worked-msa.asm
```

Preparation selects real 16-by-16 Y windows at (16,32) from pictures one and zero
of the public Python fixture. Row stride remains 176 bytes. Each 2688-byte arena
contains sixteen leading guard bytes, a 2656-byte minimal window extent and
sixteen trailing guards. Only sixteen samples per row participate in SAD; the
intervening image bytes must not be read as extra lanes. Signed `.byte` literals
preserve the raw bit patterns without assembler warnings. Preparation preserves
every byte of instruction text and refuses existing output files or changed
provided arena/control declarations.

`h263_sad16` and `h263_sad16_msa` take two byte pointers in a0/a1 and independent
unsigned byte strides in a2/a3. Strides are 16..4096. Validate both non-wrapping
minimal windows before sample access. The caller supplies mapped active samples;
read-only aliases and unaligned byte pointers are valid. Return v0=0/v1=exact SAD,
or v0=1/v1=0 for invalid stride/address arithmetic. Preserve saved general
registers, the stack pointer and all input bytes. Both byte orders must agree.

The completed worked caller prints `7872` and exits zero. Its Debug checkpoint is
`sad_caller_return`, whose first instruction saves the returned status. It has not
saved the status/sum into their named data fields when stopped before that line.
Equal windows return successful zero; an all-zero/all-255 pair returns 65280.

Run completed-entry feedback with the real MARS CLI archive:

```sh
python3 drafts/h263-sad-construction/grade.py my-sad.asm --cli /path/to/mars-cli.jar --report sad-results.json
```

The 32 public cases use separate high-address arenas and 16384 bytes of valid
private stack. The source's ordinary main is not the grading harness. Reports
require a new path. Development status four fails completed checks. Saved registers,
source guards, active-row access boundaries and exact sums are checked in both
byte orders. Valid private frames and equivalent instruction sequences are allowed.

This is the first construction stage, not the completed chapter package. Full
motion search, transform/quantization, reconstruction and bitstream emission,
cumulative checkpoint grading and automated chapter-figure capture remain to be
integrated. Equivalent completed entries need not use the book's scratch registers
or a prescribed instruction count. The separate authoring plugin handles future
IDE capture; MARS contains no chapter-specific trainer code.
