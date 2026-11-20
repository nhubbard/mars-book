# CRC figure status

All four figures have reviewed native LaTeX compositions, nineteen original light-mode screenshots and twenty-two numbered explanations. They cover changed-bit raw state, all 256 table entries, real three-byte file buffering, and the distinction between failed open and a later read error.

The fourth figure uses explicit read-only in-memory file fixtures supplied by the separate authoring plugin through the public MARS I/O hook. It does not claim that an ordinary host file produced the injected error. Native operation receipts check a failed open without acquiring a descriptor, or positive reads of three bytes and one byte, a failed read, then a guest close. Separate post-execution receipts prove lifetime disposal without counting it as guest cleanup. Function and caller instruction bodies remain unchanged.

The two separate authoring batches verified twenty-four raw views. Nineteen originals are used in the four print figures; the remaining views stay in private verification evidence. Independent accessibility review and later substantive editing remain open.
