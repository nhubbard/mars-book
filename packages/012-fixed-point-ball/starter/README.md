# Fixed-point ball starter

The default path is DEMO=0, eight requested steps, floor rendering, and the illustrated
seed. The data layout, marker, fallback exit, demonstration caller, and return stubs
are provided. Append each fragment to its named region, preserving the supplied marker
instruction between frame_ready and frame_continue. The unmodified starter assembles
and exits but intentionally fails the completed-task checks.

The first function stages can be tested independently through the kernel grading API.
Main publishes useful data after step 18 and completes its motion loop at step 22.
The probe is complete after step 27. Before final checks restore any experimental
inputs, ADDU in the probe, and DEMO=0. Free-writing mode may change internal code and
register choices while preserving the published interfaces and memory/marker ABI.

The fallback after the advance region is reached by incomplete stages. A completed
positive-step loop branches back to publish before reaching it. It contributes no
executed instructions to the completed normal path. The marker is not a time delay.

From the extracted package root, using JDK 25 and the pinned or compatible MARS CLI,
run each public script against your source:

```sh
java -jar /path/to/mars-cli.jar grade checks/program.kts starter/main.asm
java -jar /path/to/mars-cli.jar grade checks/kernels.kts starter/main.asm
```

These commands execute the supplied checks; students do not need to author Kotlin.
Use a copy of the source for experiments and keep the completed submission in the
normal DEMO=0 configuration. A passing final image alone does not replace either script.
