import contextlib
from dataclasses import FrozenInstanceError
import io
import json
from pathlib import Path
import tempfile
import unittest

import console_cases as checks


class ConsoleCaseTests(unittest.TestCase):
    def row(self, **changes):
        return dict(name='input 0', input='0\n', stdout='1\n', instructionLimit=20000, **changes)

    def encode(self, rows=None, **changes):
        return json.dumps(dict(formatVersion=1, cases=[self.row()] if rows is None else rows, **changes)).encode()

    def test_checked_in_contracts_and_generated_scripts_agree(self):
        root = Path(__file__).resolve().parents[1] / 'revision/lessons/004-byte-inspector/independent'
        for variant in ['plus-one', 'wrapped-byte']:
            cases = checks.load(root / (variant + '.cases.json'))
            self.assertEqual(['0\n', '1\n', '127\n', '128\n', '150\n', '254\n', '255\n'], [c.input for c in cases])
            self.assertEqual((root / (variant + '.kts')).read_text(), checks.render(cases))
            self.assertTrue(cases[-1].stdout.endswith('256\n' if variant == 'plus-one' else '0\n'))
            self.assertIsInstance(cases, tuple)
            with self.assertRaises(FrozenInstanceError):
                cases[0].name = 'changed'

    def test_json_shapes_duplicate_fields_constants_and_names_are_rejected(self):
        invalid = [b'', b'[]', b'null', b'{"formatVersion":1,"formatVersion":1,"cases":[]}',
                   b'{"formatVersion":NaN,"cases":[]}', b'{"formatVersion":1,"cases":[' + b'[' * 1100,
                   self.encode([]), self.encode([self.row(), self.row()])]
        base = json.loads(self.encode())
        for field, value in [('formatVersion', True), ('formatVersion', 1.0), ('formatVersion', 2), ('cases', {}), ('script', 'run this')]:
            item = dict(base); item[field] = value; invalid.append(json.dumps(item).encode())
        for name in ['', ' ', 'line\nname', '\x1b[31mred', 'x' * 129, None, 4]:
            row = self.row(); row['name'] = name; invalid.append(self.encode([row]))
        for changes in [{'script': 'run this'}, {'input': None}, {'stdout': []}, {'instructionLimit': True},
                        {'instructionLimit': 0}, {'instructionLimit': 0.5}, {'instructionLimit': 1_000_001}]:
            invalid.append(self.encode([dict(self.row(), **changes)]))
        row = self.row(); del row['stdout']; invalid.append(self.encode([row]))
        invalid.append(self.encode([None]))
        for raw in invalid:
            with self.subTest(raw=raw[:120]), self.assertRaises(ValueError):
                checks.parse(raw)

    def test_utf8_case_count_text_and_total_work_have_explicit_bounds(self):
        rows = [dict(self.row(), name=f'case {i}') for i in range(256)]
        self.assertEqual(256, len(checks.parse(self.encode(rows))))
        with self.assertRaises(ValueError): checks.parse(self.encode(rows + [dict(self.row(), name='extra')]))
        ten = [dict(self.row(), name=f'case {i}', instructionLimit=1_000_000) for i in range(10)]
        self.assertEqual(10, len(checks.parse(self.encode(ten))))
        with self.assertRaises(ValueError): checks.parse(self.encode(ten + [dict(self.row(), name='extra', instructionLimit=1)]))
        for field in ['input', 'stdout']:
            self.assertEqual(65536, len(getattr(checks.parse(self.encode([dict(self.row(), **{field:'a' * 65536})]))[0], field)))
            for value in ['a' * 65537, '€' * 21846, '\ud800']:
                with self.assertRaises(ValueError): checks.parse(self.encode([dict(self.row(), **{field:value})]))
        self.assertEqual('', checks.parse(self.encode([dict(self.row(), input='', stdout='')]))[0].stdout)
        with self.assertRaises(ValueError): checks.parse(b'\xff')
        raw = self.encode().ljust(checks.MAX_BYTES, b' ')
        self.assertEqual(1, len(checks.parse(raw)))
        with self.assertRaises(ValueError): checks.parse(raw + b' ')

    def test_kotlin_literals_keep_interpolation_and_control_text_as_data(self):
        rendered = checks.kotlin_string('"\\$input\n\r\t\b\f\x00é\u2028🙂')
        self.assertEqual('"\\"\\\\\\$input\\n\\r\\t\\b\\u000c\\u0000\\u00e9\\u2028\\ud83d\\ude42"', rendered)
        rendered = checks.render((checks.ConsoleCase('case', '"}; error("bad") // $x', '', 1000),))
        self.assertIn('\\"}; error(\\"bad\\") // \\$x', rendered)
        self.assertIn('expectOutput("")', rendered)

    def test_generation_checks_staleness_and_preserves_handwritten_files(self):
        with tempfile.TemporaryDirectory() as folder, contextlib.redirect_stdout(io.StringIO()), contextlib.redirect_stderr(io.StringIO()):
            root = Path(folder); data = root / 'cases.json'; data.write_bytes(self.encode()); output = root / 'checks.kts'
            self.assertEqual(0, checks.main([str(data), '--output', str(output)]))
            self.assertEqual(0, checks.main([str(data), '--output', str(output), '--check']))
            output.write_text('// handwritten\n')
            for tail in [[], ['--check']]:
                with self.assertRaises(SystemExit): checks.main([str(data), '--output', str(output), *tail])
            self.assertEqual('// handwritten\n', output.read_text())
            output.unlink(); output.symlink_to(data)
            with self.assertRaises(SystemExit): checks.main([str(data), '--output', str(output)])
            self.assertEqual(self.encode(), data.read_bytes())
            with self.assertRaises(SystemExit): checks.main([str(data), '--output', str(data)])
            with self.assertRaises(SystemExit): checks.main([str(root / 'missing'), '--output', str(root / 'new.kts')])
            huge = root / 'huge.json'; huge.write_bytes(b' ' * (checks.MAX_BYTES + 1))
            with self.assertRaises(ValueError): checks.load(huge)


if __name__ == '__main__':
    unittest.main()
