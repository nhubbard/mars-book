# Bytecode-interpreter companion

Section 21.7 revises original project `018-tiny-bytecode-interpreter` after the expression
companion. It occupies lab pages 134–141 in the 141-page proof. The main proof remains
unchanged at 139 pages. The directly edited native companion has six conceptual
subsections, five native tables and four actual annotated Debug observations.

## Prerequisites and outcomes

The reader needs bounded byte access, full addresses, function preservation, nested-data
validation and the expression companion's checked-word arithmetic. Five observable outcomes
organize the work:

- Distinguish guest PC, operand stack and retired count from native PC, frames and MIPS work.
- Decode the entire current image, construct instruction starts, and enforce structural
  priority before validating every encoded branch target, including unreachable branches.
- Trace a countdown's stack effects and distinguish byte offsets from retirement counts.
- Preserve pre-failure live state, apply stack/range checks in order and retire only success.
- Implement and test the complete VM, publish every return field, and reset logical and
  private validation state when a same-address image is replaced between calls.

## Public materials and finite qualification

`labs/files/bytecode-vm.asm` is an independent skeleton. Only run_vm needs implementation;
its break is intentional. The shared caller aligns the native stack and supplies the
standard code, descriptor and stack arenas. The task does not require illustrated
private labels, registers, boundary representation, frame layout or dispatch organization.

`tools/revision_vm_programs.py` generates twelve deterministic fixtures, selects capacity
and guest budget, preserves native instruction text and refuses existing output/receipt
paths. Its small fixture assembler resolves absolute byte-offset labels and bounds signed
immediates. Deliberately malformed recipes are explicit modifications, not claimed valid
assembler output. Fourteen unit tests cover encoding, labels, limits, malformed fixtures,
caller-only preparation, preserved instruction text and fresh-output behavior. It supplies
no native interpreter implementation and performs no guest execution.

Bitmap, byte-map and above-32-bit-data implementations each pass 1247 public kernel cases,
23 caller cases, 12720 private semantic cases and ten same-session sequence cases: 42000
positive grading-case executions. Each sequence case makes ten calls; grading-case counts
are not unique programs or individual calls. The private domain includes 4369 images
through length three over a specified sixteen-byte alphabet, 1280 byte substitutions,
512 late-opcode images, sixty wide-immediate records, 4158 unreachable-branch combinations,
800 arithmetic pairs, 700 generated images, 585 countdown capacity/budget pairs and 256
target byte values. These are finite domains, not every 64-byte image or operand pair.
Wide data is executed; unsupported high text and high stack placements are not claimed.

Twenty-four assembled faults fail their matching feedback, including early HALT validation,
range-only targets, untaken-condition validation, stack preflight, atomic arithmetic,
retirement and fetch/budget order, signed immediate decoding, operand order, ownership,
saved-register width, stale private boundary maps and truncated image pointers. The stale
map fault fails all ten same-address sequence cases. The empty skeleton's 1247 failures
are separate. Complete native implementations and private cases remain private.

Twenty-six hand-specified scenarios independently check the written trace table and boundary
results against the host model, then execute under all three implementations/layouts:
78 successful grading cases. The fixture helper's twelve recipes are also executed under
all three variants against separate hand-specified outcomes, giving 36 further successful
checks. These runs bind actual prepared sources, receipts, checker inputs and outputs.

`labs/checks/bytecode-work.kts` measures complete callable execution at guest budgets zero
and 64 after checking outcomes. The bitmap implementation uses 734/2222 native instructions;
the byte-map alternative uses 599/2087. Their guest retirements are zero/33. The alternative
also groups opcode classes, so this is a composite design comparison, not an isolated map
optimization. The wide byte-map variant reproduces its counts. There is no elapsed-time,
hardware-cycle or exact-count free-writing requirement. The public helper reproduces the
measurement procedure on the student's completed implementation, not necessarily those counts.

## Native figures and publication evidence

Four actual IntelliJ Light Debug captures show halt, budget exhaustion, stack-capacity failure
and zero-budget invalid-target detection. Exact descriptor rows and stated live prefixes
are independently checked by CLI probes. The screenshots show descriptor fields; live stack
cells are not pictured and are not inferred from them. Captures use completed private VMs
with the same public caller/data interface; the unfinished student skeleton does not produce
those states. The separate authoring plugin ran after actual clean, cleanSandbox and runIde
tasks, and its IDE was closed after the completed batch.

Screenshot pixels are unchanged. Titles, crops, outlines, semantic descriptions and every
concept table are native LaTeX. The existing IntelliJ-derived minted manifest remains
unchanged at 825 records; this unit adds guest-encoding tables rather than a new literal
MIPS listing. Authoring tests pass 104 Jupiter cases. Public tooling passes 419 tests,
including the fourteen new fixture tests. The original 51-step package validates and
exports under its unchanged contract; that original ZIP is not represented as a newly
integrated guided version of this revision.

Final new pages were reviewed in color, and encoding, trace, measurement and all screenshot
types were inspected in grayscale. The exact final lab proof passes automated PDF/UA-2
validation. Author, learner and assistive-technology reviews remain open. The original
manuscript, main and advanced proofs are unchanged, and no MARS implementation change was
required. The native guided lesson remains unimplemented for this companion. The opening
orientation is still deferred. The next main chapter develops word operations through
ChaCha20; the complete curriculum and production instructional-system goal remains active.
