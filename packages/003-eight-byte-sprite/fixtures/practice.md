# Change one representation rule at a time

1. Reverse the byte 0x81 by hand and explain why its value is unchanged.
2. Predict the result of stopping reversal when the source becomes zero for 0x01.
   Compare it with the required fixed-eight-bit result.
3. Change the foreground to an RGB color with three distinct components in a separate
   copy. State the new output contract before changing its checks.
4. Derive the frame stride if each display row held three eight-column images.
   Explain which pointers would keep a one-byte stride and which would change.
5. Write another correct implementation and check substituted source rows. Explain
   which observations are about final state and which require execution history.

Do not edit the submitted baseline's pixel format or layout unless the assignment
contract is intentionally changed.
