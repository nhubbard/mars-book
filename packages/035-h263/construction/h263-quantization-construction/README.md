# Quantization and baseline coefficient reconstruction

This is a construction stage of chapter 35, not a complete encoder distribution.
The authoritative exposition is in `includes/chapters/035-h263/quantization-*.tex`.
Complete `h263_quantize8` in a copy of `main.asm`; use ordinary MARS Run and Debug.
The unfinished caller reports its development status and exits one.

The 32-byte, eight-byte-aligned request contains four native-endian doublewords:
input pointer, quantizer 1..31, INTRA selector 0/1 and arithmetic mode 0/1.
Input is 64 tight signed halfwords in natural coefficient order, aligned to two
bytes, each in [-2048,2047]. Output is a disjoint sixteen-byte-aligned 256-byte
record: first 64 halfword levels, then 64 reconstructed coefficient halfwords.
All extents are mapped/non-wrapping and input, request and output are disjoint.
Supply an aligned stack and valid private space; preserve saved GPRs, SP, input
and request. Vector registers are caller-saved.

Invalid metadata returns status one/count zero, with no sample reads or output
writes. Invalid sample domain after valid metadata returns status two/count zero,
with validation reads allowed and output unchanged. Success returns zero and the
number of nonzero ordinary levels, excluding separate INTRA DC.

The exercise's forward policy is signed floor(abs(C)/(2Q)), capped in magnitude
at 127. Ordinary level zero reconstructs zero. Nonzero levels reconstruct with
magnitude Q*(2*abs(L)+1), subtracting one for even Q, restoring the sign, then
clipping to [-2048,2047]. INTRA DC instead selects floor((C+4)/8), clamps to
1..254 and stores code 255 for selection 128. Its reconstructed coefficient is
eight times the selection. Code zero/128 is never emitted for INTRA DC. INTER DC
uses the ordinary policy. Mode one keeps level selection scalar and uses MSA
for ordinary reconstruction; matching behavior alone does not prove MSA use.

The deterministic forward policy is an exercise contract. Full encoder assessment
must accept other legal choices meeting syntax, quality and reconstruction
requirements; this stage's exact levels must not become a private reference-bit
requirement for that later assessment.

Prepare a new observation without replacing instructions or input data:

```sh
python3 drafts/h263-quantization-construction/prepare.py \
  --source my-quantizer.asm --quant 8 --intra 1 --mode 1 --output q8.asm
python3 drafts/h263-quantization-construction/grade.py \
  q8.asm --java /path/to/java --cli /path/to/mars-cli.jar --report q8-results.json
```

At `quant_caller_return`, before its first instruction, result and return registers
are complete; named status/count fields have not yet been copied. The default
prints first code 255, returns count seven and has reconstructed DC 1024.
The 204 public cases cover Q=1/8/31, both block types/modes/byte orders, data and
admission boundaries, high pointers, private stack guards and late domain failure.
Reports and prepared files require new paths. `model.py` independently exposes
the published arithmetic for host experiments; it does not generate target code.
Private checks add every quantizer value and alternate schedules. Syntax/quality,
complete-loop accuracy, bit output, encoder throughput and actual IDE figures
remain later work. Screenshot automation belongs to the separate authoring plugin
depending on MARS, outside the simulator plugin.

Normative baseline reconstruction and INTRA DC mapping are described in ITU-T
H.263 (02/1998), clauses 5.4.1, 5.4.2, 6.2.1 and 6.2.2. The chapter links the
[official recommendation](https://www.itu.int/rec/dologin_pub.asp?id=T-REC-H.263-199802-S!!PDF-E&lang=e&type=items).
