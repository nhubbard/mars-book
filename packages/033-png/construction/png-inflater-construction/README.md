# PNG inflater construction: stored blocks

Steps 28–43 extend the completed collector stage. Attach with `attach.py --source
/path/to/current.asm --output /fresh/inflater.asm`. Your loader and collector edits
remain intact. The helper replaces only the exact supplied collector-stage wrapper
and appends sixteen unfinished inflater regions; it refuses an existing output or
an unrecognized wrapper. Free-written wrapper changes need manual reconciliation.

Prepare with `prepare.py s35-1 --source /path/to/inflater.asm --output /fresh/observe.asm`.
Create the named PNG beside the launched project with `fixtures.py worked /fresh/worked.png`.
Use `--kind zlib` to write the raw stream for a direct-entry observation. The loader
expects a complete PNG; the inflater expects only its joined zlib stream. Preparation
changes path/capacity/mode controls, retaining every instruction edit.

The native passages are the editable prose authority. Metadata indexes sixteen edit
steps, sixteen quizzes, thirty-two hints and twenty-three recipes. Prefixes count the
inflater edits after your already completed loader and collector. Observe direct
`png_inflate` calls and the actual supplied program at the same internal checkpoints.
The supplied wrapper passes the collector's exact joined and predicted row counts,
checks actual row capacity, and exposes `inflate_call` and `inflate_return`.

The intermediate stage supports stored blocks through exact stream/output completion
and Adler verification. Fixed and dynamic handlers still return development status
four. Reserved type three is a failure. A stored success is not credit for the completed
inflater contract. Even after stored success, the whole program remains under
construction because reconstruction and packing are missing. Neither final context
outputs nor pixels are published by this temporary wrapper.

The bad-header, bad-length, bad-Adler and extra-body fixtures retain valid PNG chunk
CRCs around a failing zlib stream. The two-block fixture is valid stored data with a
nonfinal and final block. Fixed and dynamic fixtures are development controls. The
latter requires the host encoder actually to emit a dynamic first block and refuses
otherwise; the checker records exact observed bytes rather than equating every auto
compression choice with a dynamic block.

Internal incomplete helpers unwind the owning inflater frame. Enter through
`png_inflate` while constructing them; standalone helper entry requires a completed
body and its declared state. Scratch rows may contain a produced prefix on failure or
development return, but outside-window bytes remain caller-owned. Instructor solutions
and checking cases remain private. Huffman and row construction follow this stage.
The complete `033-png` package supplies final feedback, distributions and reviewed IDE
figures. Remote progression and hint enforcement remain unimplemented.
