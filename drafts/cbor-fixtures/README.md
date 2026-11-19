# Fixed CBOR observation inputs

From the public book root, run `python3 drafts/cbor-fixtures/fixture.py trace.cbor`.
Use --fixture to choose a named observation and --record to write its flat 176-byte
little-endian record image instead of CBOR. Every output must be fresh; the helper
refuses to replace an existing file. It writes fixed teaching data, not a MIPS solution.

| Fixture | Input bytes | Required record |
| --- | ---: | --- |
| trace | 52 | Counter 0x0102030405060708, identifier 00ff41 and ten signed readings |
| canonical | 37 | Same required fields as trace; optional metadata omitted |
| empty | 7 | Counter zero, empty identifier and empty readings |
| maximum | 241 | Maximum required-field sizes and deterministic output length |
| budget | 1024 | Empty required record plus a bounded optional array |

The maximum fixture is the largest deterministic required-field encoding, not the
largest accepted input. The budget fixture reaches the separate input-byte limit and
projects to the same required record as empty. Unused identifier/reading tails in the
flat images contain a5 sentinels. The authoritative reading is composed directly from
includes/chapters/032-cbor-telemetry; its standalone entry is drafts/cbor-telemetry.tex.
MIPS implementations, cumulative authoring regions, actual IDE captures and executable
student grading exports remain subsequent work at this foundation stage.
