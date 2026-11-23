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

    def test_memory_lesson_scripts_match_their_exported_data_profiles(self):
        root = Path(__file__).resolve().parents[1] / 'revision/lessons/002-single-pixel/independent'
        for variant in ['red-component', 'independent-copy']:
            cases = checks.load(root / (variant + '.cases.json'))
            self.assertEqual((root / (variant + '.kts')).read_text(), checks.render(cases))
            self.assertEqual(6, len(cases))
            self.assertTrue(all(case.memory for case in cases))

    def test_json_shapes_duplicate_fields_constants_and_names_are_rejected(self):
        invalid = [b'', b'[]', b'null', b'{"formatVersion":1,"formatVersion":1,"cases":[]}',
                   b'{"formatVersion":NaN,"cases":[]}', b'{"formatVersion":1,"cases":[' + b'[' * 1100,
                   self.encode([]), self.encode([self.row(), self.row()])]
        base = json.loads(self.encode())
        for field, value in [('formatVersion', True), ('formatVersion', 1.0), ('formatVersion', 7), ('cases', {}), ('script', 'run this')]:
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

    def memory_profile(self, memory):
        return json.dumps(dict(formatVersion=2, cases=[self.row(memory=memory)])).encode()

    def test_memory_ranges_preserve_byte_order_and_generate_address_checks(self):
        memory = [{'address': '0x0000000010010000', 'bytes': '9966AA00df9b5713'}]
        cases = checks.parse(self.memory_profile(memory))
        self.assertEqual((checks.MemoryCheck(0x10010000, '9966AA00df9b5713'),), cases[0].memory)
        self.assertIn('expectBytes(0x0000000010010000L, 0x99, 0x66, 0xAA, 0x00, 0xdf, 0x9b, 0x57, 0x13)', checks.render(cases))
        self.assertEqual((), checks.parse(self.memory_profile([]))[0].memory)
        raw = self.memory_profile(memory).replace(b'"formatVersion": 2', b'"formatVersion": 1')
        with self.assertRaises(ValueError): checks.parse(raw)
        ranges = [{'address': f'0x{i:016x}', 'bytes': '00'} for i in range(65)]
        self.assertEqual(64, len(checks.parse(self.memory_profile(ranges[:64]))[0].memory))
        self.assertEqual(0x7fffffffffffffff, checks.parse(self.memory_profile([{'address': '0x7fffffffffffffff', 'bytes': 'ff'}]))[0].memory[0].address)
        invalid = [None, {}, 0, ranges, [None], [{'address': '0x10010000', 'bytes': 'ff'}], memory * 2]
        for field, values in [('address', [None, 42, [], '0xffffffffffffffff', '0X0000000010010000']),
                              ('bytes', [None, 42, [], '', '0', 'gg', '00 ff', '+1'])]:
            for value in values:
                invalid.append([dict(memory[0], **{field: value})])
        invalid += [[dict(memory[0], extra='script')], [{'address': '0x7fffffffffffffff', 'bytes': '0000'}]]
        for value in invalid:
            with self.subTest(value=str(value)[:100]), self.assertRaises(ValueError):
                checks.parse(self.memory_profile(value))
        with self.assertRaises(ValueError): checks.parse_memory([{'address': '0x0000000000000000', 'bytes': '00' * (checks.MAX_BYTES + 1)}])

    def test_large_literals_and_memory_use_runtime_chunks_without_changing_small_v1_scripts(self):
        for value in ['a' * 65536, '🙂' * 10000]:
            rendered = checks.kotlin_string(value)
            self.assertTrue(rendered.startswith('listOf('))
            self.assertTrue(rendered.endswith('.joinToString("")'))
        cases = checks.parse(self.memory_profile([{'address': '0x0000000010010000', 'bytes': '00' * 4096}]))
        rendered = checks.render(cases)
        self.assertIn('expectBytes(0x0000000010010000L, *(listOf(', rendered)
        self.assertIn('.chunked(2).map { it.toInt(16) }.toIntArray()', rendered)

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
