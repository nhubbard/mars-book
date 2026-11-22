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
