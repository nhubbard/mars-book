# PNG packing and final publication construction

Steps 84–100 continue the native chapter at `drafts/png.tex`. Fifteen editable
regions implement scalar packing, immutable shuffle masks, MSA packing and final
result publication. Two diagnostic steps add no code: they inspect reconstruction
failure and packing success in the supplied caller.

```sh
python3 drafts/png-packing-construction/attach.py --source my-rows.asm --output my-png.asm
python3 drafts/png-packing-construction/fixtures.py worked --output worked.png
python3 drafts/png-packing-construction/prepare.py s84-1 --source my-png.asm --output packing-observation.asm
```

Attachment preserves the student's loader, collector, inflater, reconstruction and
extra functions. It replaces only the previous supplied wrapper, inserts the blank
64-byte mask table and appends packing shells. Altered wrappers and conflicting
labels are refused rather than overwritten. Preparation retains the instruction
section exactly; all helpers refuse existing output files.

Work cumulatively through the named regions. The scalar path can complete before
MSA masks and shuffles. The wrapper calls the selected packing entry after all
prior stages succeed. Once pixel stores are under construction, development status
4 may accompany partially or completely updated pixels. It is not a completed
format/capacity failure. Completed failure status 1 must retain the framebuffer and
all context bytes; final success publishes width, height and active byte count and
reports `PNG displayed.` The final wrapper uses the context's actual compressed
capacity, not an observation-wide assumed capacity.

The worked input uses all five filters. Width15, width16 and width17 exercise the
full-group boundary and scalar remainder. The invalid final selector is carried
inside valid container CRCs and a valid zlib stream. Its diagnostic recipe is a
caller observation: packing alone intentionally ignores retained selector values.

The file caller has stride 512 words and a 1 MiB framebuffer. Direct examples also
use stride seventeen (worked input) or width plus three, with exact minimal writable
extents. Private grading covers other strides, short capacities, source counts,
alignment, byte orders and addresses above 4 GiB. Checkpoints illustrate particular
scratch registers; free-writing acceptance uses the public callable contracts and
observable accesses, permitting equivalent solutions.

Twenty-seven public recipes accompany seventeen quizzes and thirty-four progressive
hints. Answers, completed algorithms and private checks remain in the instructor
repository. Final chapter feedback, student/instructor distributions and actual
annotated light-theme IDE captures remain pending even after functional completion.
