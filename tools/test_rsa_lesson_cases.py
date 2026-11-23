import hashlib
import json
from pathlib import Path
import tempfile
import unittest
from rsa_lesson_cases import MAX32, MAX64, MAX128, OBJECTS, arithmetic, build, expected, groups, profiles


class RsaLessonCasesTest(unittest.TestCase):
    def test_unsigned_results_include_high_product_carry_and_full_exponent(self):
        self.assertEqual(expected('gcd64', 1 << 63, MAX64), 1)
        self.assertEqual(expected('gcd64', 0, 0), 0)
        self.assertEqual(expected('mul128', MAX128, MAX128), (1 << 256) - (1 << 129) + 1)
        self.assertEqual(expected('addmod128', MAX128 - 1, MAX128 - 1, MAX128), MAX128 - 2)
        self.assertEqual(expected('pow32', 4294907291, 3, 4294967291), 2494998972)
        self.assertEqual(expected('pow128', 65, 17, 3233), 2790)
        self.assertEqual(expected('pow128', 2790, 2753, 3233), 65)
        self.assertNotEqual(expected('pow128', 65, 1 << 127, MAX128 - 158), 1)
        self.assertEqual(expected('pow128', MAX128, 0, 1), 0)
        self.assertIsNone(expected('pow128', 0, 0, 0))
        with self.assertRaises(ValueError):
            expected('unknown', 1)

    def test_original_grid_and_cumulative_budgets_are_retained(self):
        grid = groups()
        self.assertEqual({k: len(v) for k, v in grid.items()}, dict(gcd=8, word=22, product=13, add=22, reduction=17, modproduct=14, power=11, caller=12, reuse=4))
        cases = [c for rows in grid.values() for c in rows]
        self.assertEqual(len(cases), 123)
        self.assertEqual(len({c['name'] for c in cases}), 123)
        self.assertEqual(sum(len(c['calls']) for c in cases), 142)
        self.assertEqual([len(c['calls']) for c in grid['reuse']], [3, 3, 2, 3])
        self.assertEqual([c['instructionLimit'] for c in grid['reuse']], [20000000, 40000000, 20000000, 20000000])
        for c in cases:
            self.assertEqual(c['stackAlignment'], 8)
            self.assertEqual(c['callerStackBytes'], 0)
            self.assertEqual(len(c['preservedRegisters']), 9)
        for profile in profiles().values():
            self.assertEqual(profile['formatVersion'], 18)
            self.assertLessEqual(sum(c['instructionLimit'] for c in profile['cases']), 40000000)
            self.assertLessEqual(len(json.dumps(profile, separators=(',', ':')).encode()) + 1, 1048576)

    def test_aliases_share_the_declared_input_and_output_without_constraining_private_gaps(self):
        for alias, label in enumerate(['output', 'left', 'right', 'left']):
            call = arithmetic('addmod128', 16, 16, 17, index=7, alias=alias)[0]
            ownership = call['callerAccess']
            self.assertEqual(ownership['maxAccesses'], 2000000)
            self.assertEqual({x['at']['label']: x['length'] for x in ownership['objects']}, OBJECTS)
            self.assertNotIn('workspace', OBJECTS)
            self.assertNotIn('accessPolicies', call)
            self.assertEqual(call['memory'][0]['label'], label)
            self.assertEqual(int.from_bytes(bytes.fromhex(call['memory'][0]['bytes']), 'little'), 15)
            if alias == 3:
                self.assertEqual(call['argumentAddresses']['a0'], call['argumentAddresses']['a1'])
        for group in ['word', 'add', 'reduction', 'modproduct', 'power']:
            call = groups()[group][-1 if group != 'word' else -2]['calls'][0]
            self.assertEqual(call['callerAccess']['writable'], [])
            self.assertEqual(call['expectedRegisters']['v0'], '0x0000000000000001')

    def test_callers_run_the_supplied_dispatch_and_publish_status_after_return(self):
        for c in groups()['caller']:
            dispatch, observed = c['calls']
            self.assertEqual(dispatch['entry'], 'main')
            self.assertEqual(dispatch['expectedRegisters'], {})
            self.assertEqual(dispatch['expectedAddresses']['sp'], dict(label='workspace', offset=1024))
            self.assertTrue(dispatch['returnTo'].endswith('_call'))
            self.assertEqual(observed['entry'], dispatch['returnTo'])
            self.assertTrue(observed['returnTo'].endswith('_return'))
            self.assertEqual(c['exit']['memory'][0]['label'], 'status')
        # Reused power carries the first output into the inverse call, without resetting arenas.
        calls = groups()['reuse'][1]['calls']
        self.assertEqual(int.from_bytes(bytes.fromhex(calls[1]['setupMemory'][0]['bytes']), 'little'), 2790)
        self.assertEqual(len(calls[1]['setupMemory']), 3)
        self.assertEqual(int.from_bytes(bytes.fromhex(calls[1]['memory'][0]['bytes']), 'little'), 65)

    def test_source_bindings_and_repeatable_generation(self):
        root = Path(__file__).resolve().parents[1]
        manifest = json.loads(Path(__file__).with_name('rsa_original_checks.json').read_text())
        for row in manifest['sources']:
            self.assertEqual(hashlib.sha256((root / row['source']).read_bytes()).hexdigest(), row['sha256'])
        self.assertEqual(len(manifest['sources']), 9)
        with tempfile.TemporaryDirectory() as directory:
            result = build(directory)
            self.assertEqual(result, build(directory, True))
            (Path(directory) / next(iter(result))).write_text('{}')
            with self.assertRaisesRegex(ValueError, 'Stale RSA profile'):
                build(directory, True)


if __name__ == '__main__':
    unittest.main()
