# Native MarsBot assessment foundation

Status: assessment support and native first-use/resume delivery are verified.
See `marsbot-native-unit.md` for the completed live and written integration.
This note supplements the
historical written-unit evidence in `marsbot-unit.md`; it does not relabel those
older captures or counts.

The original public square assignment remains `006-marsbot-square`. Its revised
written companion uses `labs/files/marsbot-square.asm`, with the complete one-leg
experiment `labs/files/marsbot-east.asm`. The current prerequisite placement is
after decisions and repetition. Students can solve the square using compact
branches and coordinate feedback without functions, arrays or a command table.

`tools/marsbot_lesson_cases.py` exports data for two assessments. The square
profile preserves the original three public cases: side/rate 16/64, 1/64 and
63/17, each with the original 100,000-instruction allowance. Each case sets the
side and varying guard, forbids writes to those eight input bytes and both
coordinate-feedback words, and requires unchanged input, empty console, the
complete actual ordered square trail, and the final stopped/north/pen-up state.
It checks execution-origin writes even if a program later restores their bytes.
Equivalent loop structure, register allocation and implementation organization
are accepted. Preserve the supplied data interface when replacing the starter.

The one-leg profile retains sixteen public length/rate pairs and checks final
state and trail. It does not claim the earlier stopped/pen-down observation in
the original hand-authored `labs/checks/marsbot-east.kts`. That checker remains
available, including its intermediate pause and complete final checks. Guided
Debug work should explain the reached/moving, stopped/pen-down and final pen-up
boundaries separately.

Console-case version 5 in MARS connects the actual instruction-clocked device in
each isolated check. It accepts bounded integral board coordinates, movement
rate, final heading/flags and a complete continuous trail. Existing byte fixtures
and access traces remain active. Device state and subscriptions close with the
case, and expected trails are detached before session creation. Source comments
configure ordinary Run/Debug; the explicit case rate controls isolated assessment.
The new checker neither uses nor resets the student's active Run/Debug device.

The public generator `tools/console_cases.py` accepts this data and writes checks
using the same `connectMarsBot`, actual state/trail and existing trace assertions.
Legacy console profiles retain their behavior. Version 4's larger instruction
allowances are also supported by the generator; formats 1–3 keep their original
limit. No downloaded host predicate or executable grading script is evaluated
by the native lesson runtime.

Private qualification exercises the production plugin data reader and prepared
console evaluator directly. Explicit-leg, command-table and threshold-based
implementations each pass every documented side 1–63 / integer rate 8–128 pair:
7,623 each, 22,869 in total. Each bounded profile stays within the existing
case, byte and combined-instruction quotas. The public east experiment also
passes its sixteen final-state cases. All twelve negative processes are rejected
by distinguishing cases, including an unfinished starter, forbidden feedback/
input writes, missing ink, incorrect state and both naive and calibrated delays.
Some faulty implementations pass individual cases; none passes its whole profile.

Generated public square checks also execute successfully through the real CLI
for all three equivalent implementations. The unchanged original east checker
passes its sixteen cases, including the intermediate stopped/pen-down state.
Public tooling passes 633 tests. Unit/parser validation, complete regression and
coverage evidence are retained privately with exact runtime/source/profile hashes.
This foundation adds no new IDE capture, publication figure, PDF proof or completed
native lesson. Human walkthrough and accessibility reviews remain separate.
