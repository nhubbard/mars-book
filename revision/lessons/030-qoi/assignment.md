# Trace the decoder's remembered state

Read Chapter 20 and its written lab before opening this unit in Learn. The
complete qoi-codec.asm is selected in decode mode. The worked copies change one
input or caller choice at a time. They are complete programs; the independent
qoi_decode_mirrored entry is the part you will write yourself.

A decoder has two boundaries to protect. Its source cursor may consume only
selected chunk bytes, ending before the eight-byte marker. Its output may contain
only the required pixels and may be published only after the whole input and
capacity decision succeed. A valid-looking prefix does not establish either
condition. Keep byte count, pixel count, reserved capacity and packed dimensions
separate in your notes.

Answer predictions using the inline choices. The source pointer locates the
operation under discussion. For a Debug activity, launch its selected source,
set one requested breakpoint at a time and inspect the selected paused frame.
The highlighted instruction has not executed yet. Labels beginning after_ mark
checkpoints after the preceding operations. When a checkpoint occurs more than
once, the activity explains which visit to inspect. Resume between observations
and finish the process before moving to another source.

Begin by deriving the 62-byte reservation for an eight-pixel encoding. The
selected input is only 38 bytes. Reconstruct its big-endian width and height,
then follow the initial predictor and cleared cache. The predictor starts as
opaque black; cache words start as transparent black. A register's meaning can
change across calls: s3 holds a predictor in the walker but saved capacity in
the outer decoder. Explain a value at a specific routine boundary.

Trace every chunk in source order, recording consumed bytes, produced pixels,
previous color and cache changes. The two INDEX operations restore complete
colors, including alpha. The DIFF and LUMA fields contain biased differences;
components wrap modulo 256. The channels and colorspace flags describe the
image without discarding alpha or transforming components.

Then compare validation with emission. Validation maintains the same predictor
and cache semantics but writes no caller pixels. Emission restarts with fresh
predictor, cache and INDEX history. The initial-INDEX and cache-reset examples
make that distinction observable. For malformed framing, even a correct-looking
pixel sequence must leave the caller destination untouched. A valid image that
exceeds output capacity is a different failure.

During short-read-loader, Learn supplies a virtual input.qoi containing the same
38 bytes as worked/input.qoi. Transfers contain at most seven bytes; the first
owned descriptor is 19. This activity does not read the host file. Each fresh
launch starts with independent file state. Outside the activity, ordinary
execution can use the real file in worked/ with the source's normal working
directory. A requested byte capacity is not an initialized prefix; advance by
the returned positive count and validate only the completed file.

Finally implement qoi_decode_mirrored in independent/qoi-mirrored.asm under the
contract in independent/README.md. Begin with a one-row image, then reason about
multiple rows, odd widths, runs crossing rows and error paths. You may decode
then reverse rows, or emit to reflected addresses while keeping decoder state
in forward source order. The native checks accept both approaches and do not
require the worked source's labels or temporary registers.

The seven independent groups retain the original 205-case grid and budgets.
Use a failure to choose a concrete observation rather than changing several
conditions at once. Retain an eight-pixel trace, a publication argument and the
row-placement formula alongside your source. Passing finite checks supports
those arguments; it does not establish all possible streams or hardware speed.
The optional qoi-mirrored.kts checker permits the same task to be run through
the CLI. The original complete-codec checks and real-file pipeline remain in
the book repository for further investigation.
