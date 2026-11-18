# Binary PPM reading fixture

Run `python3 fixture.py /path/to/new-example.ppm` to create the two-by-two image
explained in the native chapter. The helper refuses to overwrite an existing file.
It writes an eleven-byte canonical header and twelve RGB sample bytes: red and green
on the first row, blue and white on the second. This is fixture preparation, not the
student assembly parser or writer. The actual image routines remain to be implemented.
