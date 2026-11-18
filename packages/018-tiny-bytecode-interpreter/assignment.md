# Run a tiny bytecode interpreter

Implement `run_vm(a0=full code address, a1=length, a2=full state address, a3=guest budget)` in MIPS64 Release 6. The code slice has zero through 64 bytes inside a 96-byte arena and may start at an unaligned byte address. Budget is zero through 64. The eight-byte-aligned descriptor names a word-aligned stack subview with capacity zero through eight words in a sixteen-word allocation. Code, descriptor and stack allocations are disjoint. Do not infer usable capacity from the backing allocation.

Every call is fresh: begin at PC zero, depth zero and retired count zero, ignoring old output fields. The caller may replace the bytes at the same address and length between calls. Validate current contents each time. This interface does not resume a prior output PC.

## Representation and results

Descriptor fields, in bytes: PC at 0, depth at 4, retired count at 8, status at 12, input capacity at 16, fault offset at 20, full eight-byte input stack pointer at 24. All other fields are four-byte words. Write all five output fields on every return, including validation failure and zero budget. Preserve capacity and stack pointer. Live values occupy the first depth stack words; inactive cells inside capacity are unspecified workspace.

Return status in `v0` and retired guest count in `v1`. Status codes: 0 budget exhausted; 1 halt; -1 invalid opcode; -2 truncated record; -3 invalid branch target; -4 fetch at image end; -5 stack underflow; -6 stack overflow; -7 arithmetic overflow; -8 division by zero. Fault offset is -1 on budget exhaustion or halt. Validation failures set PC, depth and retired count to zero and fault to the offending instruction start. Runtime failures retain the failing instruction PC, its pre-instruction live prefix and depth, and the count before that instruction; fault equals PC. A failing guest instruction does not retire.

## Encoding

All opcode numbers below are hexadecimal. Offsets and lengths are in bytes. PUSH32 encodes a signed 32-bit value in four little-endian bytes; every bit pattern is valid. PUSH8 sign extends one signed byte. Branch operands are unsigned absolute byte offsets from the code base.

| Opcode | Operation | Size | Effect |
|---|---|---:|---|
| 00 | HALT | 1 | Stop successfully, advance PC one, retire once |
| 01 | PUSH8 | 2 | Append signed-byte value, require room |
| 02 | PUSH32 | 5 | Append signed-word value, require room |
| 10 | ADD | 1 | Pop right and left, append checked left+right |
| 11 | SUB | 1 | Pop right and left, append checked left-right |
| 12 | MUL | 1 | Pop right and left, append checked left*right |
| 13 | DIV | 1 | Pop right and left, append checked left/right |
| 20 | DUP | 1 | Require one and room, copy top |
| 21 | DROP | 1 | Require one, remove top |
| 22 | SWAP | 1 | Require two, exchange top pair |
| 23 | OVER | 1 | Require two and room, copy second from top |
| 30 | JMP | 2 | Select target without changing stack |
| 31 | JZ | 2 | Require one, pop on both paths; select target for zero, else PC+2 |

Arithmetic requires two values, left older and right on top. Results must fit -2147483648 through 2147483647. Division truncates toward zero and checks zero divisor before quotient range; minimum signed word divided by -1 is overflow. Underflow takes priority over room requirements. Check all requirements before changing the semantic live prefix or depth. Workspace within capacity has no promised inactive value.

## Validation, execution and priority

First decode the entire image left to right, including bytes after HALT. A record must have a legal opcode and its complete payload. Report the first structural failure. Only after that succeeds, check all encoded branch targets left to right, including unreachable and untaken JZ targets. Each target must be below length and at a decoded instruction beginning, never inside an operand. Any structural failure takes priority over every target failure. No guest instruction retires during either validation phase.

Then execute from PC zero and an empty logical stack. Check remaining budget before fetch. Empty input with zero budget yields; empty input with positive budget fails at the end. Each successful instruction, including HALT and a self-branch, retires once and consumes one budget unit. Nonbranch instructions advance by encoded size. A successful final non-HALT instruction may leave PC equal to length; if its budget is exhausted, yield there, otherwise the next attempted fetch fails. A HALT return takes priority once that instruction has successfully retired.

Only the selected code slice, all 32 bytes of the selected descriptor and the selected stack capacity may be read from caller data. Only five output fields and selected stack capacity may be written. Preserve program bytes, other arenas, guards, descriptor inputs and bytes outside capacity, including temporary writes later restored. Submission-owned constants and private native stack storage are allowed. Preserve all bits of s0 through s7, fp and sp and return through the supplied ra. Produce no console output. Grading checks every byte in a memory access, not merely its starting address.

## Work in either mode

Keep the supplied caller/data harness. In guided mode replace each named region's temporary branch to vm_commit with its listed code. Do not append after the branch. Partial observations inspect registers at vm_commit before its stores and restore; those are provisional construction states, not the completed public contract. The provided exits balance the frame. After the last edit the default image halts with status 1, retired 33, PC 18, depth 1, live value 6 and fault -1.

Free-writing implementations may use another decoder, private stack layout, boundary representation or dispatch strategy. Final grading imposes no private labels, scratch registers, exact instruction count or fixed memory-access order. Current-content validation, ownership, diagnostics and retirement behavior still apply. Instructor progression and hints are design metadata; interactive recognition and the separate capture runner remain pending.

## Run the command-line checks

Run `python3 tools/course.py check --package 018-tiny-bytecode-interpreter --cli /absolute/path/to/cli.jar --source /absolute/path/to/main.asm --java /absolute/path/to/java` from the book repository. In an extracted student package, run `java -jar /path/to/cli.jar grade checks/kernel.kts starter/main.asm` and likewise `checks/program.kts`. The untouched starter assembles and exits but intentionally fails final grading.
