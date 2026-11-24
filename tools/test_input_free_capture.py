"""Input-free Run evidence cannot claim an input event, output, or exit it did not observe."""

import unittest
import course


class InputFreeRunCaptureTest(unittest.TestCase):
    def setUp(self):
        self.figure = dict(captureMode="run", expectedStdout="signed: -1\n")
        self.raw = dict(mode="run", stdout="signed: -1\n", stderr="", exitCode=0)

    def test_exact_automatic_run_is_accepted_without_input_fields(self):
        course.validate_capture_mode(self.figure, self.raw)

    def test_changed_output_exit_or_fabricated_input_is_rejected(self):
        for changes in [
            dict(stdout="signed: 1\n"),
            dict(stderr="error"),
            dict(exitCode=1),
            dict(standardInput="1\n"),
            dict(inputMethod="Native console typing and Enter handlers"),
        ]:
            with self.subTest(changes=changes):
                with self.assertRaises(ValueError):
                    course.validate_capture_mode(self.figure, dict(self.raw, **changes))

    def test_interactive_run_still_needs_nonempty_native_keyboard_evidence(self):
        figure = dict(self.figure, standardInput="20\n")
        raw = dict(self.raw, standardInput="20\n")
        with self.assertRaises(ValueError):
            course.validate_capture_mode(figure, raw)
        course.validate_capture_mode(
            figure, dict(raw, inputMethod="Native console typing and Enter handlers")
        )
        with self.assertRaises(ValueError):
            course.validate_capture_mode(
                dict(self.figure, standardInput=""), dict(self.raw, standardInput="")
            )


if __name__ == "__main__":
    unittest.main()
