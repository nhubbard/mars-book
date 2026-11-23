import hashlib
import json
from pathlib import Path
import tempfile
import unittest
from sha_lesson_cases import (ARENAS, OWNED, IV, build, compressed, context_result,
    expanded, final_from_context, groups, pattern, profiles)


class ShaLessonCasesTest(unittest.TestCase):
    def test_arithmetic_and_padding_match_independent_digest_engine_at_block_boundaries(self):
        block = b'abc\x80' + bytes(52) + bytes.fromhex('0000000000000018')
        self.assertEqual(expanded(block)[16:20], [0x61626380, 0x000f0000, 0x7da86405, 0x600003c6])
        result = b''.join(w.to_bytes(4, 'big') for w in compressed(IV, block))
        self.assertEqual(result, hashlib.sha256(b'abc').digest())
        for n in [0, 1, 3, 55, 56, 63, 64, 65, 119, 120, 127, 128, 129, 4096]:
            raw, state = pattern(n), list(IV)
            for i in range(0, n // 64 * 64, 64):
                state = compressed(state, raw[i:i + 64])
            self.assertEqual(final_from_context(state, n, raw[n // 64 * 64:]), hashlib.sha256(raw).digest())
        self.assertNotEqual(final_from_context(IV, 3, b'abc'), final_from_context(IV, 0x100000003, b'abc'))

    def test_original_case_counts_budgets_and_source_bindings_remain_complete(self):
        root = Path(__file__).resolve().parents[1]
        manifest = json.loads(Path(__file__).with_name('sha_original_checks.json').read_text())
        grid = groups()
        self.assertEqual({k: len(v) for k, v in grid.items()}, dict(kernel=54, file=40, program=12, reuse=22))
        for row in manifest['sources']:
            self.assertEqual(hashlib.sha256((root / row['source']).read_bytes()).hexdigest(), row['sha256'])
            self.assertEqual(len(grid[row['group']]), row['cases'])
        all_cases = [c for rows in grid.values() for c in rows]
        self.assertEqual(len({c['name'] for c in all_cases}), 128)
        self.assertEqual(sum(len(c['calls']) for c in all_cases), 298)
        for c in all_cases:
            self.assertIn(c['instructionLimit'], [100000, 3000000])
            self.assertEqual(c['callerStackBytes'], 0)
            self.assertEqual(c['stackAlignment'], 8)
            self.assertEqual(len(c['preservedRegisters']), 9)
            self.assertEqual(c['exit']['code'], 0)
        self.assertTrue(all(c['instructionLimit'] == 100000 for c in grid['kernel'][:31]))
        self.assertTrue(all(c['instructionLimit'] == 3000000 for c in grid['kernel'][31:]))
        for p in profiles().values():
            self.assertEqual(p['formatVersion'], 16)
            self.assertLessEqual(sum(c['instructionLimit'] for c in p['cases']), 10000000)
            self.assertLessEqual(len(json.dumps(p, separators=(',', ':')).encode()) + 1, 1048576)

    def test_whole_span_store_history_covers_gaps_while_context_results_leave_private_bytes_free(self):
        self.assertEqual([(m['offset'], len(bytes.fromhex(m['bytes']))) for m in context_result(8, IV, 3, b'abc')], [(8, 44), (56, 3)])
        for rows in groups().values():
            for c in rows:
                for call in c['calls']:
                    if call['entry'] == 'main':
                        self.assertNotIn('accessPolicies', call)
                        continue
                    policies = call['accessPolicies']
                    self.assertEqual(len(policies), len(OWNED) + 1)
                    whole = policies[-1]
                    self.assertEqual(whole['from'], {'label':'data','offset':0})
                    self.assertEqual(whole['until'], {'label':'status','offset':4})
                    self.assertFalse(whole['checkReads'])
                    self.assertEqual(whole['readable'], [])
                    self.assertEqual(call['accessHistory']['maxAccesses'], 200000)
                    self.assertTrue(all(p['maxAccesses'] == 200000 for p in policies))
                    for p in policies[:-1]:
                        self.assertEqual(p['readable'], policies[0]['readable'])
                        self.assertEqual(p['writable'], whole['writable'])
                    if call['entry'] == 'sha256_final':
                        self.assertTrue(all(m['label'] == 'digest' for m in call['memory']))
                    if call['entry'] == 'sha256_compress':
                        self.assertEqual([(m['label'],len(bytes.fromhex(m['bytes']))) for m in call['memory']], [('state',32)])

    def test_reused_file_failures_retain_successful_output_without_host_reset(self):
        for c in groups()['reuse'][-4:]:
            calls = c['calls']
            self.assertEqual(len(calls), 4)
            self.assertEqual([x['fileReadInvocation']['maxRequest'] for x in calls], [7,64,3,1])
            self.assertEqual([x['fileReadInvocation']['path'] for x in calls], ['repeat-one.bin','repeat-two.bin','repeat-one.bin','missing.bin'])
            self.assertIn(c['fileRead']['firstDescriptor'], [7,31])
            self.assertEqual(c['fileRead']['additionalFiles'].keys(), {'repeat-two.bin'})
            self.assertTrue(all([m['label'] for m in x['setupMemory']] == ['path'] for x in calls))
            if c['fileRead'].get('failAfterBytes') == 3:
                self.assertEqual([int(x['expectedRegisters']['v0'],16) for x in calls], [0,2,0,1])
                self.assertEqual(calls[0]['memory'][0]['bytes'], calls[1]['memory'][0]['bytes'])
                self.assertEqual(calls[2]['memory'][0]['bytes'], calls[3]['memory'][0]['bytes'])
        for c in groups()['program']:
            self.assertEqual(c['calls'][0]['entry'], 'main')
            self.assertEqual(c['calls'][1]['returnTo'], 'main_done')
            self.assertEqual(c['calls'][1]['memory'][-1]['bytes'], 'ffffffff')
            self.assertEqual(c['exit']['memory'][0]['bytes'], '00000000')

    def test_generation_is_deterministic_and_check_refuses_stale_profiles(self):
        with tempfile.TemporaryDirectory() as directory:
            first = build(directory)
            self.assertEqual(first, build(directory, True))
            path = Path(directory) / next(iter(first))
            path.write_text('{}')
            with self.assertRaisesRegex(ValueError, 'Stale SHA-256 profile'):
                build(directory, True)
