# Trace widths before choosing an instruction

1. Interpret 0x80000001 as a signed word and as an unsigned word. Predict both
   full-register load results and both plus-one results.
2. Explain why service 1 still prints -1 when passed 0x00000000ffffffff.
3. Predict service 36 output for a full register containing 0x0000000100000000.
   State which bits the service reads.
4. Replace one sd with sw in a separate copy. Explain why a zero-filled field
   might hide the mistake and how poisoned high bytes reveal it.
5. Explain why addiu and daddiu agree for -1 yet disagree for 2147483647.
6. Derive an alternative zero-extension sequence and test it on substituted words.

For each answer, state the observation width explicitly. A hexadecimal numeral
with eight digits describes a word; a full 64-bit pattern needs sixteen.
