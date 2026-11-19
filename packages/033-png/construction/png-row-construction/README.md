# PNG row reconstruction construction

Steps 71–83 continue the native chapter at `drafts/png.tex`. Basic programming
knowledge and completed loader, collector and inflater stages are assumed. Attach
this unfinished stage to the student's own Huffman-stage program:

```sh
python3 drafts/png-row-construction/attach.py --source my-inflater.asm --output my-rows.asm
python3 drafts/png-row-construction/fixtures.py worked --output worked.png
python3 drafts/png-row-construction/prepare.py s71-1 --source my-rows.asm --output row-observation.asm
```

Edit the thirteen named regions in `my-rows.asm` cumulatively. Preparation changes
observation controls only and retains all instruction edits and extra functions.
Outputs are created exclusively; existing files are refused. For a checkpoint,
stop before its label. Numeric states describe the supplied construction layout;
completed free-writing solutions are graded through the callable interface and
observable memory effects, not those scratch registers or private frame offsets.

The wrapper now calls reconstruction after successful inflation, choosing its scalar
or MSA entry from the existing mode control. It still returns development status 4
on valid input because RGB packing and final publication are unfinished. A bad
selector returns failure 1 before changing any sample. The framebuffer and final
context outputs remain untouched. No successful display is claimed for this stage.

The worked fixture contains all five filters. The Up fixtures have two rows: first
row residuals 250 and second row residuals 10, producing sample values 250 then 4.
They expose wrapping and full-vector/tail boundaries without requiring students
to hand-author compressed data. `bad-selector` has a valid container, zlib checksum
and inflated length but an invalid final row selector.

Nineteen recipes include thirteen construction observations and six completed-stage
boundaries. Quiz choices and two progressive hints per step are public; answers
and private grading cases remain in the instructor repository. Row success is
separate from whole-program success. Packing, final feedback, distributions and
actual annotated IDE captures remain pending.
