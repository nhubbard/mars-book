"""Exercise source-role handling at the repository formatter entry point."""

import contextlib
import io
import json
from pathlib import Path
import subprocess
import tempfile
import unittest

import format_sources


class SourceFormattingTest(unittest.TestCase):
    def test_fragment_imports_and_frozen_evidence_survive_a_complete_audit(self):
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            subprocess.run(["git", "init", "--quiet", str(root)], check=True)
            source = "import java.nio.ByteOrder\nfun count()=1\n"
            fragment = root / "model.kts"
            complete = root / "check.kts"
            frozen = root / "verification-old/model.kts"
            frozen.parent.mkdir()
            for path in [fragment, complete, frozen]:
                path.write_text(source)
            report = root / "audit.json"
            arguments = [
                "--root",
                str(root),
                "--language",
                "kotlin",
                "--report",
                str(report),
            ]
            with contextlib.redirect_stdout(io.StringIO()):
                self.assertEqual(0, format_sources.main(arguments + ["--apply"]))
                self.assertEqual(0, format_sources.main(arguments))
            self.assertIn("import java.nio.ByteOrder", fragment.read_text())
            self.assertNotIn("import java.nio.ByteOrder", complete.read_text())
            self.assertEqual(source, frozen.read_text())
            receipt = json.loads(report.read_text())
            self.assertEqual(0, receipt["changed"])
            self.assertEqual(2, receipt["maintainedFiles"])
            self.assertEqual([], receipt["errors"])


if __name__ == "__main__":
    unittest.main()
