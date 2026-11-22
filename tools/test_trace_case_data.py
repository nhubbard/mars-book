import json
import unittest

import console_cases as checks


class TraceCaseDataTests(unittest.TestCase):
    def profile(self):
        return dict(formatVersion=3, cases=[dict(name='transactions', input='', stdout='', instructionLimit=1000,
            initialMemory=[dict(address='0x0000000010010001', bytes='aabbcc')],
            memory=[dict(address='0x0000000010010000', bytes='11aabbcc')],
            stores=[dict(kind='bytes', address='0x00000000ffff0000', length=65536, port='0x00000000ffff0010', bytes='aaaabb'),
                    dict(kind='none', address='0x0000000010010000', length=8)],
            reads=[dict(address='0x0000000010010000', length=8, allowedAddress='0x0000000010010001', allowedLength=2)])])

    def parse(self, profile=None):
        return checks.parse(json.dumps(self.profile() if profile is None else profile).encode())

    def reject(self, profile):
        with self.assertRaises(ValueError): self.parse(profile)

    def test_rendered_setup_and_trace_checks_match_the_declared_observational_contract(self):
        case = self.parse()[0]
        self.assertEqual((checks.MemoryCheck(0x10010001, 'aabbcc'),), case.initial_memory)
        self.assertEqual((checks.ReadCheck(0x10010000, 8, 0x10010001, 2),), case.reads)
        script = checks.render((case,))
        self.assertIn('setBytes(0x0000000010010001L, 0xaa, 0xbb, 0xcc)', script)
        self.assertLess(script.index('setBytes('), script.index('traceMemoryStores('))
        self.assertLess(script.index('traceMemoryAccesses('), script.index('runToExit()'))
        self.assertIn('stores0.expectByteStores(0x00000000ffff0010L, 0xaa, 0xaa, 0xbb)', script)
        self.assertIn('stores1.expectNone()', script)
        self.assertIn('reads0.expectReadsWithin(0x0000000010010001L, 2)', script)
        self.assertIsInstance(case.stores, tuple)
        for version in [1, 2]:
            for field in ['initialMemory', 'stores', 'reads']:
                data = self.profile(); data['formatVersion'] = version
                for other in ['memory', 'initialMemory', 'stores', 'reads']:
                    if other != field: data['cases'][0].pop(other)
                self.reject(data)

    def test_absent_empty_and_zero_read_contracts_are_explicit(self):
        data = self.profile(); row = data['cases'][0]
        for field in ['memory', 'initialMemory', 'stores', 'reads']: row.pop(field)
        self.assertEqual((), self.parse(data)[0].stores)
        data = self.profile(); row = data['cases'][0]
        row['stores'][0]['bytes'] = ''; row['reads'][0]['allowedLength'] = 0
        script = checks.render(self.parse(data))
        self.assertIn('stores0.expectByteStores(0x00000000ffff0010L)', script)
        self.assertIn('reads0.expectReadsWithin(0x0000000010010001L, 0)', script)
        for field in ['initialMemory', 'stores', 'reads']: row[field] = []
        self.assertEqual((), self.parse(data)[0].reads)

    def test_malformed_types_fields_addresses_and_read_bounds_do_not_coerce(self):
        for field in ['initialMemory', 'stores', 'reads']:
            for value in [None, {}, 0, True, [None], [{}]]:
                data = self.profile(); data['cases'][0][field] = value
                with self.subTest(field=field, value=value): self.reject(data)
        bad_stores = [dict(kind='unknown'), dict(kind='none', address='0x0000000010010000', length=8, bytes=''),
                      dict(kind='bytes', address='0x0000000010010000', length=8, port='0x0000000010010008', bytes='00')]
        original = self.profile()['cases'][0]['stores'][0]
        for field in ['address', 'length', 'port', 'bytes']:
            row = dict(original); row.pop(field); bad_stores.append(row)
        for field, values in [('length', [0, -1, True, '1', 1.5, 1048577]),
                              ('port', [None, 42, '0xffff0010', '0xffffffffffffffff']),
                              ('bytes', [None, 42, '', '0', 'gg', '00 ff', 'éé'])]:
            for value in values:
                if field == 'bytes' and value == '': continue  # Empty sequence is valid and checked above.
                bad_stores.append(dict(original, **{field: value}))
        for row in bad_stores:
            data = self.profile(); data['cases'][0]['stores'][0] = row; self.reject(data)
        original_read = self.profile()['cases'][0]['reads'][0]
        for changes in [dict(length=0), dict(length=1048577), dict(allowedLength=-1), dict(allowedLength=9),
                        dict(allowedLength=True), dict(allowedAddress='0x000000001000ffff'),
                        dict(allowedAddress='0x0000000010010009'), dict(extra='script')]:
            data = self.profile(); data['cases'][0]['reads'][0] = dict(original_read, **changes); self.reject(data)
        raw = json.dumps(self.profile()).replace('"allowedLength": 2', '"allowedLength": 2, "allowedLength": 0')
        with self.assertRaises(ValueError): checks.parse(raw.encode())

    def test_total_trace_and_expected_byte_budgets_reject_incomplete_or_unbounded_history(self):
        none = dict(kind='none', address='0x0000000010010000', length=8)
        data = self.profile(); row = data['cases'][0]; row['reads'] = []; row['stores'] = [none] * 64
        self.assertEqual(64, len(self.parse(data)[0].stores))
        row['stores'] = [none] * 65; self.reject(data)
        data = self.profile(); row = data['cases'][0]; row['stores'][0]['bytes'] = '00' * 65536
        self.assertEqual(65536, len(self.parse(data)[0].stores[0].bytes) // 2)
        self.assertIn('.joinToString("").chunked(2)', checks.render(self.parse(data)))
        row['stores'][0]['bytes'] += '00'; self.reject(data)
        row['stores'] = [dict(self.profile()['cases'][0]['stores'][0], bytes='00' * 32769)] * 2; self.reject(data)
        data = self.profile(); row = data['cases'][0]; row['stores'] = [none] * 64; self.reject(data)  # Plus its read check.
        data = self.profile(); data['cases'][0]['initialMemory'] *= 2; self.reject(data)
        data = self.profile(); data['cases'][0]['stores'][0].update(address='0x7fffffffffffffff', length=2); self.reject(data)


if __name__ == '__main__':
    unittest.main()
