# Independent bounded VM

Replace run_vm in vm.asm. Keep the supplied caller/data layout and observation labels. Read the written bytecode companion for the guest encoding, complete descriptor and status contracts. There is no requirement to copy private labels, scratch registers, frame sizes, start-set representation or exact native instruction counts.

The native lesson checks all 1,247 original interface trials, four newly authored public replacement sequences and twenty-three complete callers. Each direct-call trial retains arena/descriptor selection, unaligned code offset, aligned stack subview, capacity, budget, initial sentinels and full saved state. Inactive workspace inside capacity is unspecified; the live prefix on failure must remain intact. Reads and even temporary stores outside the stated views remain forbidden.

A call initializes logical PC/depth/retired zero and validates its current image. A preceding budget return does not request resume. The caller may replace bytes at the same pointer and length; stale private instruction-start markers must not survive.

From this directory with JDK 25:

    java -jar /path/to/cli.jar grade kernel.kts vm.asm
    java -jar /path/to/cli.jar grade caller.kts vm.asm
    java -jar /path/to/cli.jar grade work.kts vm.asm

The additional public replacements run in the native assessment. caller.kts uses console format 8. work.kts reports complete native call work at zero/sixty-four guest budget after checking outcomes; it does not prescribe counts or establish elapsed time. The instructor distribution contains additional private cases and equivalent completed implementations separately.
