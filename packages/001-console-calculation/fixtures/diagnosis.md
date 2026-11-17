# Use the failure to choose the next observation

| Observation | Inspect next |
| --- | --- |
| The file does not assemble | Reported source location, mnemonic, operands, and label spelling |
| The prompt prints but no input is read | Input selector and input syscall |
| The answer prefix prints without a number | Integer argument, selector, and request |
| The integer is wrong | Returned input and each arithmetic intermediate |
| The number is right but grading fails | Exact spaces, newline, and termination |

Inspect the state before changing the instruction. A symptom identifies a
question to investigate; it does not by itself prove the cause.
