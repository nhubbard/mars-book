# File-loading construction, steps 37–45

Attach to a completed index working file, retaining its routines:

```sh
python3 drafts/video-io-construction/attach.py --source index-work.asm --output reader-work.asm
python3 drafts/video-io-construction/fixtures.py moving --output animation.mlv
python3 drafts/video-io-construction/prepare.py s37-1 --source reader-work.asm --output observe.asm
```

The native chapter defines `video_load`, staging-buffer ownership, the exact-fill
EOF probe, descriptor cleanup and result publication. Nine editable regions have
nine quizzes, eighteen hints and twenty-three recipes. Commands require fresh
outputs. Attachment renames the previous example entry to `index_example_main`;
student function instructions remain intact. Preparation changes only path and
capacity data. Path storage is 128 UTF-8 bytes including NUL.

Keep the generated file in the program's working directory or prepare a path to it.
Missing files deliberately have no fixture output. Empty and malformed fixtures
are actual host-file observations. Short-read and injected-error recipes use the
grading file backend; an ordinary host file does not force those transfer patterns.

Loading succeeds only after a nonempty file reaches observed EOF within capacity.
Its count context does not certify video validity. The caller then indexes the
complete bytes, and may reject malformed content. The staging buffer may contain a
prefix after failure; preserving a previous count does not restore previous file
bytes. Use separate candidate storage to keep an earlier animation usable during
replacement loading. Seeking, encoding and playback construction follow later.
