# PNG collector construction draft

Steps 11–27 extend a student's completed loader. Copy your current program to a new
stage using `attach.py --source /path/to/current.asm --output /fresh/next.asm`. The
helper replaces only the exact two supplied decoder stubs. It retains loader edits,
adds the independent compressed-capacity control, and appends the temporary wrapper
and seventeen unfinished collector regions. Existing outputs are refused.

Prepare controls with `prepare.py s22-1 --source /path/to/next.asm --output /fresh/observe.asm`.
Create `palette.png` beside the launched project with `fixtures.py palette /fresh/palette.png`.
Other fixture names are listed by the helper. The working directory must contain the
selected named file. The loader keeps its 1 MiB window; `collector_capacity` defaults
to 46 for the worked stream. This is a selected window inside the actual 1 MiB compressed
allocation, not a new allocation. The wrapper rejects a selection exceeding that allocation.

Follow the directly authored native passages in `includes/chapters/033-png/collect-*.tex`.
The source and checkpoint metadata do not regenerate prose. First develop the byte
reader before the signature calls it; develop CRC before comparing checksums; develop
chunk advancement after IHDR before attempting later chunk handlers. Recipe prefixes
count the seventeen collector edits, in addition to your already completed loader.

Helper recipes s13-1 and s17-1 use direct completed-helper observations: four signature
bytes for `png_be32` and the seventeen IHDR type/payload bytes for `png_crc32`.
Other primary recipes follow `png_collect` through either direct entry or the supplied
caller. Stop at `collector_call` to inspect arguments and `collector_return` to inspect
its still-live descriptor. Do not call an unfinished helper directly: its development
exit unwinds the owning collector frame. All incomplete whole-collector paths return
status four with count zero, saved state restored and no successful metadata publication.

The tight, bad-CRC and IDAT-gap recipes use all seventeen completed regions. A complete
collector returns one/zero for these inputs while retaining descriptor sentinels. Its
compressed scratch may contain a prefix. The temporary decoder returns development
status after collector success and publishes no pixels or final context outputs.

Inflater, reconstruction and packing construction follow this stage. The complete
`033-png` package supplies final feedback, distributions and reviewed IDE figures.
Instructor solutions,
quiz keys, alternatives and private cases stay in the private repository. No remote
progression or hint enforcement is implemented by these scripts.
