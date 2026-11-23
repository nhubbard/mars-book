# Read-only delta scanner

Implement scan_delta in delta-scan.asm, retaining the caller and public data
labels. Input a0 is a full byte pointer, a1 is byte length zero through 160,
and a2 is a reading limit zero through 32. Return committed bytes in v0,
committed readings in v1, last committed signed reading in a2, and status in a3.
With no committed reading, the last reading is zero. Initialize predecessor
and both counters for every invocation, including calls after failure or success.

The wire contains unsigned base-128 values in least-significant-group-first
order. A byte contributes its low seven bits; bit seven requests another group.
Each candidate occupies one through five bytes. Its fifth byte must terminate
and have payload below 32. A multi-byte spelling ending in payload zero is
redundant and rejected. Truncation, excessive width, a fifth continuation and
redundant spelling have status two. No sixth byte belongs to a candidate.

Reverse the signed mapping with difference u/2 for even u and −(u+1)/2 for odd u.
Add that exact difference to the last committed predecessor, starting at zero.
If the reconstructed reading is outside −2147483648 through 2147483647, return
range status three. Preserve the full mapped value and reconstruction until the
range check. Valid source readings do not imply that their difference fits a word.

Use separate candidate and committed state. Commit the input cursor, count and
last reading only after all syntax and range checks pass. A rejected candidate
leaves the accepted prefix intact. Check input completion before the reading
limit: exhausted input succeeds with status zero, even if the limit is reached.
If input remains and the limit is reached, return capacity status one before
reading the next candidate. Thus 02 80 00 at limit 32 returns 1/1/1/status 2;
at limit one it returns 1/1/1/status 1 without inspecting either tail byte.

Read only the selected input slice. Empty input or limit zero permits no caller
buffer reads. Write no caller bytes, including same-value or temporary writes
later restored. Preserve all guards, metadata, padding, full s0 through s7,
fp, sp and caller stack bytes. Submission-owned constants and a restored private
frame below incoming sp are allowed. Return through the supplied continuation
and produce no console I/O. Backing arena capacity does not enlarge the selected
input. Each repeated-call fixture has separate invocation-scoped access windows.

The ordinary caller's eight bytes d0 0f 04 01 00 ee a8 02 describe five readings
1000,1002,1001,1001,20000. At scan_ready the live results are 8/5/20000/status 0,
while all published result words are −1. At results_ready those words contain
the returned values. Normal exit subsequently uses v0 for its service selector.

The unchanged delta-scan.kts retains 297 public cases: 296 direct function trials
and the default caller. Native profiles independently preserve every original
byte, seed-derived offset, full 192-byte arena, expected prefix and one-million-
instruction budget. Native assessments group 264 sequence/limit cases, 32 rejected-
tail/limit cases and two state cases. The state group includes the original
caller observed before and after publication, plus five repeated invocations
covering error, success, nonempty zero-limit work, another independent block and
empty input. There are 298 function cases and 303 calls. A separate assessment
checks complete silent exit.

The function data are split into 32 bounded files, with at most ten original-
budget cases per file. Native lesson format 8 combines them into three student
checks, followed by exit. The existing per-profile execution limits remain;
all checks in an activity belong to one source revision and one assessment
attempt. A later failed profile prevents completion, and cancellation does not
consume an assessment. This requires the updated MARS reader for native lesson
format 8 and function-profile version 9.

Explain candidate versus committed state, precedence, independent-call origin
and read-only bounds beside your implementation. The finite sequences, tail
families, offsets and reuse case do not exhaust every byte string, reading
sequence or independently chosen offset. The broader written exercise and
standalone script remain usable without the interactive lesson.
