# Verified native captures

Four reviewed native LaTeX figures combine eight actual IntelliJ Light screenshots:
initial floor and nearest publications, tick five after the first vertical reflection,
and paired native hexadecimal/signed-decimal overflow views. Original PNGs, raw
source/state receipts, reviewed previews and editable native annotations are retained.

The first two runs have identical raw state `(320,448,192,320)` and tick zero. Floor
selects cell `(1,1)`, ID 9; nearest selects `(1,2)`, ID 17. At tick five in floor mode,
raw state is `(1280,1536,192,-320)` and ID 53 selects `(5,6)`. Both complete nine-frame
histories, all framebuffer words and the exact 639-store sequence are checked independently.
The complete image shows one white logical cell; its thin `(0,0)` selection outline
is a native editor marker. No color distinction is needed to interpret the figure.

The overflow path uses only `DEMO = 1`, before animation initialization. Native hex
views show `ffffffff80000100` and `0000000080000100`; native signed-decimal views show
`-2147483392` and `2147483904`. The separate authoring plugin uses the existing MARS
display toggle and verifies its state and rendered values before/after capture.

Each capture uses the stated unchanged fixture or only the declared mode/DEMO patch.
The batch ran the actual `clean cleanSandbox runIde` tasks. The original pixels,
configuration and annotation bounds remain in receipts. Actual screenshots require
this native verification in addition to a correct model state. Interactive recognition
of every authoring step, independent accessibility review and later substantive editing
remain separate work.
