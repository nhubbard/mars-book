import json
from pathlib import Path
import tempfile
import unittest
import ppm_negative_lesson_cases as ppm


class PpmNegativeLessonCasesTests(unittest.TestCase):
    def test_exact_original_grid_budgets_offsets_and_unsigned_capacities(self):
        trials = ppm.trials(); self.assertEqual(166, len(trials))
        self.assertEqual([80, 80, 6], [sum(t['group'] == g for t in trials) for g in ['single-digit-width', 'two-digit-width', 'invalid']])
        for trial in trials:
            row = ppm.construction(trial); case = ppm.definition(trial)
            self.assertEqual(trial['index'] % 8 * 4, row['sourceOffset'])
            self.assertEqual((trial['index'] * 7 + 1) % 32, row['destinationOffset'])
            self.assertEqual(trial['limit'], case['instructionLimit'])
            self.assertEqual(3000000 if trial['group'] != 'invalid' else 100000, trial['limit'])
            self.assertEqual(trial['capacity'] & ppm.MASK, int(case['calls'][0]['arguments']['a3'], 16))
            expected = 1 if trial['group'] == 'invalid' else 0 if trial['capacity'] < 0 or trial['capacity'] >= 11 + 3 * trial['width'] * trial['height'] + (trial['width'] >= 10) + (trial['height'] >= 10) else 2
            self.assertEqual(expected, row['status'])
        self.assertEqual({0, 65, 0xffffffff, 0x80000000, 2}, {t[k] for t in trials[-6:] for k in ['width', 'height']})

    def test_rgb_channel_order_ignores_alpha_and_complements_each_channel(self):
        row = ppm.construction(dict(width=1, height=1, index=0, capacity=14))
        self.assertEqual(bytes.fromhex('193c7fa5'), row['raw'])
        self.assertEqual(b'P6\n1 1\n255\n'+bytes.fromhex('80c3e6'), row['result'])
        self.assertEqual(0, row['status'])
        large = ppm.construction(dict(width=64, height=64, index=137, capacity=12544))
        self.assertEqual(12301, len(large['result'])); self.assertEqual(16384, len(large['raw']))
        self.assertTrue(large['result'].startswith(b'P6\n64 64\n255\n'))

    def test_failure_uses_invalid_pointers_and_permits_no_caller_access(self):
        for trial in ppm.trials():
            row = ppm.construction(trial); case = ppm.definition(trial); call = case['calls'][0]
            if row['status']:
                self.assertEqual({}, call['argumentAddresses']); self.assertEqual([], call['memory'])
                self.assertEqual(1, int(call['arguments']['a0'], 16)); self.assertEqual(1, int(call['arguments']['a2'], 16))
                self.assertTrue(all(not p['readable'] and not p['writable'] for p in case['accessPolicies']))
            else:
                self.assertEqual(len(row['result']), int(call['expectedRegisters']['v1'], 16))
                self.assertEqual(row['result'].hex(), call['memory'][0]['bytes'])
                self.assertEqual(row['destinationOffset'], call['memory'][0]['offset'])

    def test_full_original_arenas_workspace_and_preserved_upper_bits_are_initialized(self):
        for trial in ppm.trials():
            case = ppm.definition(trial); row = ppm.construction(trial)
            initial = {m['label']: bytes.fromhex(m['bytes']) for m in case['initialMemory']}
            self.assertEqual(bytes([0xa5])*4096, initial['workspace'])
            for label, size in ppm.ARENAS.items():
                self.assertEqual(size, len(initial[label]))
                expected = bytearray([0xa5]*size)
                if label == 'framebuffer': expected[row['sourceOffset']:row['sourceOffset']+len(row['raw'])] = row['raw']
                self.assertEqual(bytes(expected), initial[label])
            self.assertEqual({'sp': {'label': 'workspace', 'offset': 4096}}, case['initialRegisterAddresses'])
            self.assertEqual(9, len(case['preservedRegisters']))
            for i, name in enumerate([*(f's{i}' for i in range(8)), 'fp']):
                self.assertEqual(0x2345678900000000+271*i, int(case['initialRegisters'][name], 16))

    def test_watched_regions_exclude_private_stack_and_preserve_complete_access_widths(self):
        case = ppm.definition(next(t for t in ppm.trials() if t['width'] == 64 and t['height'] == 64 and t['capacity'] < 0))
        policies = case['accessPolicies']; self.assertEqual(13, len(policies))
        self.assertEqual(set(ppm.ARENAS), {p['from']['label'] for p in policies})
        self.assertNotIn('workspace', {p['from']['label'] for p in policies})
        for policy in policies:
            self.assertEqual(ppm.ARENAS[policy['from']['label']], policy['until']['offset'])
            self.assertEqual([16384, 12301], [w['length'] for w in policy['readable']])
            self.assertEqual([12301], [w['length'] for w in policy['writable']])
        self.assertLessEqual(sum(w['length'] for p in policies for key in ['readable', 'writable'] for w in p[key]), 1024*1024)

    def test_caller_checks_return_before_publication_then_the_result_objects(self):
        case = ppm.caller_case(); self.assertEqual(3000000, case['instructionLimit'])
        self.assertEqual(['negative_return', 'function_return'], [c['returnTo'] for c in case['calls']])
        self.assertEqual(0, case['callerStackBytes'])
        first, second = case['calls']
        self.assertEqual(b'P6\n2 2\n255\n'+bytes([0,255,255,255,0,255,255,255,0,0,0,0]), bytes.fromhex(first['memory'][0]['bytes']))
        self.assertEqual(['status', 'count'], [row['label'] for row in second['memory']])
        self.assertEqual(second['accessPolicies'][0]['writable'], second['accessPolicies'][0]['readable'])
        self.assertEqual({'status', 'count'}, {w['at']['label'] for w in second['accessPolicies'][0]['writable']})

    def test_profiles_preserve_every_case_once_and_bound_bytes_and_work(self):
        profiles = ppm.profiles(); self.assertEqual(57, len(profiles))
        cases = [c for p in profiles.values() for c in p['cases']]
        self.assertEqual(167, len(cases)); self.assertEqual(168, sum(len(c['calls']) for c in cases))
        self.assertEqual(167, len({c['name'] for c in cases}))
        for profile in profiles.values():
            self.assertEqual(11, profile['formatVersion'])
            self.assertLessEqual(sum(c['instructionLimit'] for c in profile['cases']), 10000000)
            self.assertLessEqual(len(json.dumps(profile, separators=(',', ':')).encode()), 1024*1024)
        with tempfile.TemporaryDirectory() as tmp:
            files = ppm.build(tmp); self.assertEqual(57, len(files)); ppm.build(tmp, check=True)
            first = Path(tmp)/next(iter(files)); first.write_text('{}')
            with self.assertRaises(ValueError): ppm.build(tmp, check=True)
