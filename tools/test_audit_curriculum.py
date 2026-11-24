"""Structural regressions that a successful TeX compile alone cannot detect."""

import copy
import json
from pathlib import Path
import tempfile
import unittest

import audit_curriculum as curriculum


class CurriculumAuditTests(unittest.TestCase):
    def setUp(self):
        self.folder = tempfile.TemporaryDirectory()
        self.addCleanup(self.folder.cleanup)
        self.root = Path(self.folder.name)
        self.write(
            "revision/learning.tex",
            r"\input{revision/main/one}\input{revision/main/two}",
        )
        self.write(
            "revision/lab-book.tex",
            r"\input{revision/labs/one}\input{revision/labs/two}",
        )
        self.write("revision/main/one.tex", r"\chapter{One}\label{one}\ref{two}")
        self.write("revision/main/two.tex", r"\chapter{Two}\label{two}")
        self.write(
            "revision/labs/one.tex",
            r"\chapter{One}\input{revision/labs/companions/extra}",
        )
        self.write("revision/labs/two.tex", r"\chapter{Two}")
        self.write(
            "revision/labs/companions/extra.tex", r"\section{Extra}\label{extra}"
        )
        self.write("revision/one-unit.md", "Verification scope and limits.")
        self.write("packages/001-one/package.json", json.dumps(dict(id="001-one")))
        self.write("packages/002-extra/package.json", json.dumps(dict(id="002-extra")))
        self.index = dict(
            formatVersion=1,
            placements=[
                dict(
                    project="001-one",
                    chapter="one",
                    lab="revision/labs/one.tex",
                    verificationNote="revision/one-unit.md",
                ),
                dict(
                    project="002-extra",
                    chapter="one",
                    lab="revision/labs/companions/extra.tex",
                    verificationNote="revision/one-unit.md",
                ),
            ],
        )

    def write(self, name, text):
        path = self.root / name
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(text)

    def audit(self):
        return curriculum.audit(self.root, self.index)

    def test_current_manuscripts_retain_every_original_project(self):
        result = curriculum.audit()
        self.assertEqual(
            (27, 35, 36, 10),
            tuple(
                result[key]
                for key in ("chapters", "originalProjects", "placements", "companions")
            ),
        )

    def test_independent_documents_may_use_the_same_label(self):
        self.write("revision/labs/two.tex", r"\chapter{Two}\label{two}\ref{extra}")
        self.assertEqual(2, self.audit()["lab"]["labels"])

    def test_h263_style_two_chapter_placement_is_allowed(self):
        entry = copy.deepcopy(self.index["placements"][0])
        entry.update(chapter="two", lab="revision/labs/two.tex")
        self.index["placements"].append(entry)
        self.assertEqual(3, self.audit()["placements"])

    def test_missing_project_and_unknown_project_are_rejected(self):
        self.index["placements"].pop()
        with self.assertRaisesRegex(ValueError, "without revised locations"):
            self.audit()
        self.index["placements"][0]["project"] = "999-unknown"
        with self.assertRaisesRegex(ValueError, "Unknown original project"):
            self.audit()

    def test_reordered_lab_is_rejected_even_when_all_files_exist(self):
        self.write(
            "revision/lab-book.tex",
            r"\input{revision/labs/two}\input{revision/labs/one}",
        )
        with self.assertRaisesRegex(ValueError, "sequence differs"):
            self.audit()

    def test_detached_or_moved_companion_is_rejected(self):
        self.write("revision/labs/one.tex", r"\chapter{One}")
        with self.assertRaisesRegex(
            ValueError, "absent or included under another chapter"
        ):
            self.audit()
        self.write(
            "revision/labs/two.tex",
            r"\chapter{Two}\input{revision/labs/companions/extra}",
        )
        with self.assertRaisesRegex(
            ValueError, "absent or included under another chapter"
        ):
            self.audit()

    def test_unindexed_companion_is_rejected(self):
        self.write("revision/labs/companions/unmapped.tex", "Additional assignment")
        self.write(
            "revision/labs/two.tex",
            r"\chapter{Two}\input{revision/labs/companions/unmapped}",
        )
        with self.assertRaisesRegex(ValueError, "companion includes"):
            self.audit()

    def test_duplicate_placement_is_rejected(self):
        self.index["placements"].append(copy.deepcopy(self.index["placements"][0]))
        with self.assertRaisesRegex(ValueError, "Duplicate project placement"):
            self.audit()

    def test_dangling_reference_and_duplicate_label_are_rejected(self):
        self.write("revision/main/two.tex", r"\chapter{Two}\label{typo}")
        with self.assertRaisesRegex(ValueError, "unresolved references"):
            self.audit()
        self.write("revision/main/two.tex", r"\chapter{Two}\label{one}")
        with self.assertRaisesRegex(ValueError, "duplicate labels"):
            self.audit()

    def test_comments_and_literal_programs_do_not_create_labels_or_references(self):
        self.write(
            "revision/main/two.tex",
            "\\chapter{Two}\\label{two}\n"
            "% \\label{one} \\ref{absent}\n"
            "\\begin{minted}{mars-idea}\n# \\ref{absent} % \\label{one}\n\\end{minted}\n",
        )
        self.assertEqual(2, self.audit()["main"]["labels"])

    def test_include_cycle_and_escape_are_rejected(self):
        for source in [r"\input{revision/learning}", r"\input{../outside}"]:
            with self.subTest(source=source):
                self.write("revision/main/one.tex", source)
                with self.assertRaisesRegex(ValueError, "Escaping or cyclic"):
                    self.audit()

    def test_verification_note_must_exist_inside_repository(self):
        for name in [
            "revision/missing.md",
            "../private.md",
            str(self.root / "revision/one-unit.md"),
        ]:
            with self.subTest(name=name):
                self.index["placements"][0]["verificationNote"] = name
                with self.assertRaisesRegex(ValueError, "repository"):
                    self.audit()

    def test_unknown_format_and_wrong_package_identity_are_rejected(self):
        self.index["formatVersion"] = 2
        with self.assertRaisesRegex(ValueError, "Unsupported"):
            self.audit()
        self.index["formatVersion"] = 1
        self.write("packages/001-one/package.json", json.dumps(dict(id="002-extra")))
        with self.assertRaisesRegex(ValueError, "package identity"):
            self.audit()


if __name__ == "__main__":
    unittest.main()
