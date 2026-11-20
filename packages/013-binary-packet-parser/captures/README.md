# Verified native captures

Three reviewed native LaTeX figures combine twelve actual IntelliJ Light panels:
successful default parsing, an eleven-byte prefix of the unchanged mapped packet,
and a corrupt checksum. Each combines the stopped caller marker, native hexadecimal
registers, native signed-decimal return values, mapped input bytes and output-record
bytes. Original screenshots, raw receipts, previews and editable annotations are retained.

The default returns 12, input plus eight as the payload pointer, and record words
`[1,4660,3,254]`. With only eleven available bytes, the checksum trailer remains mapped
and visible but lies outside the provided range; status is -1 and the payload pointer
is zero. Changing only trailer `1a` to `1b` instead gives checksum status -6. Both failures
leave the four initial -1 record words unchanged and make no record stores.

Independent checks cover all 324 data bytes before execution, at parser return and
after exit; every execution read's logical bound; and exact ordered stores. Tests
also reject a mapped overread and a failed store followed by restoration, even when
final values match. A correct memory image alone does not establish those histories.

The separate authoring plugin depends on MARS. Its exact unchanged source/build/binary
identity binds reuse of 67 passing Jupiter tests and compatible pinned API evidence.
The accepted graphical batch independently ran the actual `clean cleanSandbox runIde`
tasks and checked both native display bases and all rendered values. The rejected
preparation and print-scaling attempts remain private and supply no accepted captures.

Each figure stops at `parse_ready` after the parser returns and before the caller
stores status and payload address. Interactive recognition of every authoring step,
independent accessibility review and later substantive editing remain separate work.
