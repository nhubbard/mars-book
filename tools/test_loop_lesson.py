"""Publication consistency and student-export boundaries for the loop unit."""
import json
from pathlib import Path
import tempfile
import unittest
import zipfile

from native_lesson import bundle


class LoopLessonTests(unittest.TestCase):
    def test_worked_variants_retain_the_published_program_with_only_the_stated_input_changes(self):
        root = Path(__file__).resolve().parents[1]
        unit = root / 'revision/lessons/005-seven-segment-counter'
        worked = (root / 'revision/labs/files/range-counter.asm').read_text()
        self.assertEqual(worked, (unit / 'range-counter.asm').read_text())
        for name, first, count in [('empty', 16, 0), ('tail', 14, 2)]:
            expected = worked.replace('first_digit:  .word 0', f'first_digit:  .word {first}').replace(
                'digit_count:  .word 16', f'digit_count:  .word {count}')
            self.assertEqual(expected, (unit / f'ranges/{name}.asm').read_text())

    def test_student_archive_exports_every_activity_dependency_and_no_private_solution(self):
        root = Path(__file__).resolve().parents[1] / 'revision/lessons/005-seven-segment-counter'
        manifest = json.loads((root / 'package.json').read_text())
        plan = json.loads((root / 'lesson.json').read_text())
        self.assertEqual(5, plan['formatVersion'])
        self.assertEqual({'run', 'debug', 'prediction', 'assessment'}, {s['kind'] for s in plan['steps']})
        sources = [s.get('source', manifest['entry']) for s in plan['steps'] if s['kind'] == 'debug']
        self.assertEqual(['range-counter.asm', 'ranges/empty.asm', 'ranges/tail.asm'], sources)
        with tempfile.TemporaryDirectory() as temporary:
            archive = bundle(root, Path(temporary) / 'student.zip')
            with zipfile.ZipFile(archive) as zipped:
                names = set(zipped.namelist())
                for activity in plan['steps']:
                    self.assertIn(manifest['id'] + '/' + activity.get('source', manifest['entry']), names)
                    if activity['kind'] == 'assessment':
                        self.assertIn(manifest['id'] + '/' + activity['caseProfile'], names)
                self.assertEqual({manifest['id'] + '/' + n for n in
                    ['package.json', manifest['entry'], manifest['assignment'], manifest['nativeLesson'], *manifest['supportingFiles']]}, names)
                self.assertNotIn('reference.asm', ' '.join(names))
                self.assertNotIn('alternative.asm', ' '.join(names))
