# Public function case grid

The fifth revised unit uses native lesson format 6 with checkKind set to function.
Its independent profile is version-1 function data: arguments, expected return
registers, initial and preserved state, helper intervals and bounded repeated
calls. The native MARS reader owns validation. The format contract is documented
in the MARS repository’s docs/native-lesson-format.md under “Format 6 function
assessments”; runtime semantics are in docs/prepared-function-checks.md.

The finite public grid is authored by tools/function_lesson_cases.py. Regenerate
or check it without changing publication prose:

```sh
python3 tools/function_lesson_cases.py --output-directory revision/lessons/003-row-functions/independent
python3 tools/function_lesson_cases.py --output-directory revision/lessons/003-row-functions/independent --check
```

The tool writes only reverse-and-invert.cases.json and its matching generated
reverse-and-invert.kts. It covers all 256 low bytes in each of three cases, using
positive, sign-bit-set and all-one upper patterns, full-width saved values,
different caller stack positions and alternating public continuations. Each case
owns one machine and a cumulative instruction budget. The script runs through
the existing CLI DSL; native lessons interpret the data without evaluating that
script. The adjacent student README explains the exact contract and its limits.

The book’s native LaTeX sources remain independently edited publication authority.
Private complete implementations and qualification faults are instructor materials.

## Continuation access policies (format 23)

Format 23 preserves format 22's fields and quotas, and adds optional
`exit.accessPolicies`. Earlier versions reject that additional exit field.
The policy shape and bounds are the same as a function/call `accessPolicies`
array: public `from`/`until` data addresses, typed permitted windows, bounded
notice histories and optional store-only observation.

Function observations still end at the supplied return continuation. Those
subscriptions close before caller code executes. An explicit exit policy then
observes the continuation through actual normal exit, independently of the
callee's policies. It is checked before final output and memory expectations.
Omitting it retains the earlier caller-publication behavior; an existing function
policy does not silently become a restriction on legitimate caller stores.

This lets the ring kernel profiles preserve their original complete footprint
checks after returning through `function_return` or `done`. A caller-side store
restored before exit still fails when outside the declared permitted windows.
The private observations release their histories after verification, while the
public trace API continues to support inspection after an ordinary close.
Nested exit policies are detached before acquiring a case session. Cancellation,
normal termination, bounded output and the cumulative instruction budget retain
their existing behavior.
