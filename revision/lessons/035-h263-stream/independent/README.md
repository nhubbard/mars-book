# Continuing bounded reader

Keep one `h263-read-bits.asm` implementation through every stage. The caller and its public labels supply test storage. `reader_bytes_tail` names the final unchanged 64-byte allocation so maximum-capacity access windows stay within the grader's offset limits. Preserve those public fixture labels when editing the function; they prescribe no private helper labels, registers or frame layout.

The eight profiles retain all 592 original cases and 598 calls. The two final assessments each select 328 overlapping placements; together they cover every case without changing the original 100000-instruction case allowances. They accept equivalent correct implementations and compare returned bits, complete context retention, exact input permissions and cursor publication. Shared calls retain one machine and cumulative allowance. The starter intentionally fails until implemented.

Native function-case format 22 requires the relocated-pointer fixture support introduced by MARS commit `6d50e406`. Case data initializes context pointers from the current assembled labels. It does not require a fixed data address or an initialization function in your assembly.
