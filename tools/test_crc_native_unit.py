"""Protect the original executable contracts while composing the native CRC guide."""
import json
from pathlib import Path
import unittest
import crc_chunk_lesson_cases
import crc_file_lesson_cases

ROOT = Path(__file__).resolve().parents[1]
UNIT = ROOT / 'revision/lessons/024-crc32'


class CrcNativeUnitTest(unittest.TestCase):
    def test_every_original_profile_is_retained_without_reducing_budgets_or_permissions(self):
        chunk = crc_chunk_lesson_cases.build(UNIT/'independent', check=True)
        files = crc_file_lesson_cases.build(UNIT/'independent', check=True)
        lesson = json.loads((UNIT/'lesson.json').read_text())
        referenced = []
        for step in lesson['steps']:
            if step['kind'] == 'assessment':
                referenced.extend(step.get('caseProfiles', [step.get('caseProfile')]))
        expected = {'independent/'+name for name in chunk.keys() | files.keys()}
        self.assertEqual(set(referenced), expected)
        self.assertEqual(len(referenced), len(expected), 'Each original case profile must have one assessment owner')

    def test_independent_starter_and_optional_cli_checks_remain_the_original_contract(self):
        self.assertEqual((UNIT/'independent/crc32-chunk.asm').read_bytes(), (ROOT/'revision/labs/files/crc32-chunk.asm').read_bytes())
        for name in ['crc32-chunk.kts', 'crc32-files.kts']:
            self.assertEqual((UNIT/'independent'/name).read_bytes(), (ROOT/'revision/labs/checks'/name).read_bytes())
        self.assertEqual((UNIT/'worked/crc32-work.kts').read_bytes(), (ROOT/'revision/labs/checks/crc32-work.kts').read_bytes())
        self.assertIn('crc32_check_chunk:\n    break', (UNIT/'independent/crc32-chunk.asm').read_text())

    def test_complete_variants_change_only_the_stated_experiment(self):
        source = (ROOT/'revision/labs/files/crc32.asm').read_text()
        self.assertEqual((UNIT/'crc32.asm').read_text().split('\n', 1)[1], source)
        variants = {
            'empty-bit.asm': source.replace('input_count: .word 9', 'input_count: .word 0'),
            'file-bit.asm': source.replace('exercise: .word 0', 'exercise: .word 3'),
            'file-table.asm': source.replace('exercise: .word 0', 'exercise: .word 3').replace('method: .word 0', 'method: .word 1'),
            'stream-split.asm': (ROOT/'revision/labs/files/crc32-stream.asm').read_text(),
            'table-ready.asm': source.replace('exercise: .word 0', 'exercise: .word 2').replace(
                'choose_table:\n    dla $t9, crc32_table_update',
                'choose_table:\n    dla $a0, crc_table\n    balc crc32_table_init\n    nop\ntable_prepared:\n    dla $t9, crc32_table_update'),
        }
        for name, expected in variants.items():
            with self.subTest(name=name):
                self.assertEqual((UNIT/'worked'/name).read_text().split('\n', 1)[1], expected)

    def test_explicit_export_covers_each_student_source_and_supporting_file(self):
        data = json.loads((UNIT/'package.json').read_text())
        expected = {str(p.relative_to(UNIT)) for p in UNIT.rglob('*') if p.is_file()}
        actual = ['package.json', data['entry'], data['assignment'], data['nativeLesson'], *data['supportingFiles']]
        self.assertEqual(set(actual), expected)
        self.assertEqual(len(actual), len(expected))
        self.assertEqual((UNIT/'worked/fixture.bin').read_bytes(), b'123456789')
        lesson = json.loads((UNIT/'lesson.json').read_text())
        for step in lesson['steps']:
            if 'source' in step:
                self.assertIn(step['source'], expected)
            if 'fileRead' in step:
                fixture = step['fileRead']
                self.assertEqual(fixture['path'], 'fixture.bin')
                if not fixture.get('missing', False):
                    self.assertEqual(bytes.fromhex(fixture['bytes']), (UNIT/'worked/fixture.bin').read_bytes())
