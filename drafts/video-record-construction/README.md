# Scalar record construction, steps 07–23

Read the native chapter at `drafts/lossless-video.tex`. Copy `main.asm` to your own
working file and fill its seventeen named regions. `record.asm` is the same
unfinished routine without the data/caller; do not concatenate both definitions.

Prepare a new observation file while preserving every instruction in your work:

```sh
python3 drafts/video-record-construction/prepare.py s07-1 --source work.asm --output observe.asm
```

There are 37 recipes: seventeen checkpoints, eleven completed valid records and
nine failure controls. `fixtures.py` emits exact record bytes and includes a
bad-CRC input plus a CRC-valid late skip overflow. These are input generators,
not decoders. The native text defines the interface, native context fields,
byte order, caller storage promises, validation-before-write and return statuses.
Development status four is limited to unfinished work. A completed routine returns
zero/success or one/failure; failure must retain the whole frame and context.

Guided checkpoint examples describe the illustrated registers. Free writing may
use equivalent correct implementations and a different private stack frame while
retaining the public interface and behavior. Instructor hints/progression policies
and private checks remain separate. The file reader, seek, encoder and playback
construction are later stages; this module applies one in-memory record.
