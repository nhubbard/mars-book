# Deterministic file input during a native Debug lesson

Native lesson format 9 adds an optional `fileRead` object to a Debug activity.
Formats 1–8 retain their existing behavior and reject this field. Run activities,
predictions and independent assessments do not acquire the field. Independent
function assessments use their separate version-13 case-profile contract.

For example, this member supplies the nine ASCII bytes of `123456789` under a
virtual filename. Reads transfer at most three bytes at a time and fail after
five bytes have been accepted. A correct reader therefore sees positive results
of three and two, followed by minus one. The first acquired descriptor is 19.

```json
"fileRead": {
  "path": "fixture.bin",
  "bytes": "313233343536373839",
  "maxRequest": 7,
  "maxReadSize": 3,
  "failAfterBytes": 5,
  "firstDescriptor": 19
}
```

The required fields are `path`, hexadecimal `bytes` and `maxRequest`.
`maxReadSize` defaults to 1,048,576, `firstDescriptor` to 3, and `missing` to false.
Omit `failAfterBytes` for ordinary EOF. A missing fixture requires empty bytes
and no failure position. Input is limited to one MiB, descriptors to 3–31, and
request/transfer bounds to 1–1,048,576. Paths must be nonblank UTF-8 without NUL,
through 4,096 encoded bytes. Unknown members and malformed values are errors.
The lesson's overall one-MiB JSON limit also applies.

`maxRequest` documents the read contract used by independent grading; it does
not clamp a Debug program's requests. `maxReadSize` limits each actual transfer.
The distinction lets a student observe a wrong request rather than hiding it.
An injected failure at the input length is an error at that boundary, not EOF.

While this Debug activity is active, ordinary MARS Run and Debug launches of
its selected source receive independent copies of the input definition. The
path match uses the normalized absolute entry path. Other sources retain the
normal frontend environment. Leaving the activity removes its binding for new
launches. A launch that already started keeps its own file contents, descriptors
and read position until simulation stops, including when Learn advances while
the program is paused at its final required observation.

All file operations in a bound launch use in-memory fixture files; unknown
paths and writes fail. No operation falls back to a host file. Console, input,
heap, time, dialog and other non-file behavior stay with the existing frontend.
The fixture does not create a file in the student's project. Instructional text
must identify the supplied input and explain that ordinary execution outside
the activity needs the corresponding real file.

The main MARS plugin implements this student behavior through its public
syscall-environment extension point. Each launch owns cleanup through IntelliJ's
public disposal facilities. The shared file engine is also used by native grading,
but interactive launches do not retain the grader's per-operation history.
Screenshot automation remains in the separate authoring plugin.

This contract supports the pending Chapter 19 CRC lesson. The existing screenshots
and published proof predate this delivery; this API description does not claim a
completed guided unit, new screen capture or learner review.
