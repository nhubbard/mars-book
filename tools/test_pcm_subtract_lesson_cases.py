from pathlib import Path
import json
import tempfile
import unittest

import pcm_subtract_lesson_cases as pcm


class PcmSubtractLessonCasesTests(unittest.TestCase):
    def test_complete_count_alias_grid_and_strict_boundary_oracle(self):
        trials=pcm.trials();self.assertEqual(331,len(trials))
        self.assertEqual({(n,mode) for n in range(65) for mode in range(5)}, {(len(t['left']),t['mode']) for t in trials[:325]})
        rows=[pcm.construction(trial) for trial in trials]
        for row,trial in zip(rows,trials):
            self.assertEqual(4,len(row['arenas']))
            self.assertTrue(all(len(image)==192 for image in row['arenas'].values()))
            right=trial['left'] if trial['mode']>=3 else trial['right']
            differences=[left-right for left,right in zip(trial['left'],right)]
            self.assertEqual(sum(value < -32768 or value > 32767 for value in differences),row['clips'])
            decoded=[int.from_bytes(bytes(row['expected'][n:n+2]),'little',signed=True) for n in range(0,len(row['expected']),2)]
            self.assertEqual([max(-32768,min(32767,value)) for value in differences],decoded)
            if trial['mode']>=3:self.assertTrue(all(value==0 for value in decoded));self.assertEqual(0,row['clips'])
            if trial['mode'] in (0,3):
                for n,value in enumerate(row['expected']):self.assertNotEqual(value,row['arenas'][row['dest']][row['destOffset']+n])
        self.assertEqual([1,1,0,0,1,1],[row['clips'] for row in rows[-6:]])
        self.assertTrue(any(row['destOffset']%2 for row in rows))
        self.assertEqual([0xd0,0x8a],pcm.encoded([-30000]))

    def test_profiles_keep_each_original_budget_all_aliases_and_caller_publication(self):
        profiles=pcm.profiles();self.assertEqual([73,65,65,65,65],[len(profile['cases']) for profile in profiles.values()])
        cases=[case for profile in profiles.values() for case in profile['cases']]
        self.assertEqual(333,len(cases));self.assertEqual(337,sum(len(case['calls']) for case in cases))
        for profile in profiles.values():
            self.assertEqual(8,profile['formatVersion']);self.assertLessEqual(sum(case['instructionLimit'] for case in profile['cases']),10000000)
        for case in cases:
            self.assertEqual(400000 if len(case['calls'])==4 else 100000,case['instructionLimit']);self.assertEqual(9,len(case['preservedRegisters']))
            self.assertNotIn('initializedOutput',case)
            policy=case['accessPolicies'][0];self.assertEqual({'label':'pcm_a_guard'},policy['from']);self.assertEqual({'label':'caller_end','offset':4},policy['until'])
        reuse=profiles['alias-0']['cases'][-2]
        self.assertEqual([8,1,8,0],[int(call['arguments']['a3'],16) for call in reuse['calls']])
        self.assertGreater(int(reuse['calls'][0]['expectedRegisters']['v1'],16),0)
        self.assertEqual(['function_return','done','function_return','done'],[call['returnTo'] for call in reuse['calls']])
        self.assertEqual([0,0],[int(reuse['calls'][n]['expectedRegisters']['v1'],16) for n in [1,3]])
        caller=profiles['alias-0']['cases'][-1]
        self.assertEqual(['subtract_ready','results_ready'],[call['returnTo'] for call in caller['calls']])
        for call,processed,clipped in zip(caller['calls'],[99,9],[99,5]):
            memory={row['label']:row['bytes'] for row in call['memory']}
            self.assertEqual(processed,int.from_bytes(bytes.fromhex(memory['processed']),'little'))
            self.assertEqual(clipped,int.from_bytes(bytes.fromhex(memory['clipped']),'little'))
            output=bytes.fromhex(memory['pcm_c']);self.assertEqual([-32768,32767,32767,-32768,1,32767,-32768,-4444,32767],[int.from_bytes(output[n:n+2],'little',signed=True) for n in range(0,18,2)])

    def test_checked_in_profiles_and_original_student_files_remain_current(self):
        root=Path(__file__).resolve().parents[1];unit=root/'revision/lessons/020-pcm-mixing'
        pcm.build(unit/'independent',check=True)
        for suffix,directory in [('asm','files'),('kts','checks')]:
            self.assertEqual((root/f'revision/labs/{directory}/pcm-subtract.{suffix}').read_bytes(),(unit/f'independent/pcm-subtract.{suffix}').read_bytes())
        self.assertEqual((root/'revision/labs/files/pcm-mix.asm').read_bytes(),(unit/'pcm-mix.asm').read_bytes())
        with tempfile.TemporaryDirectory() as temporary:
            directory=Path(temporary);pcm.build(directory);pcm.build(directory,check=True)
            for name,profile in pcm.profiles().items():self.assertEqual(profile,json.loads((directory/f'pcm-subtract-{name}.cases.json').read_text()))
            (directory/'caller-exit.cases.json').write_text('{}')
            with self.assertRaises(ValueError):pcm.build(directory,check=True)

class PcmNativePackageTests(unittest.TestCase):
    def test_complete_sources_and_extra_independent_count_extensions_are_preserved(self):
        root=Path(__file__).resolve().parents[1];unit=root/'revision/lessons/020-pcm-mixing';source=(unit/'pcm-mix.asm').read_text()
        for count in [0,7,8,63,64]:
            self.assertEqual(source.replace('count: .word 16',f'count: .word {count}'),(unit/f'worked/count-{count}.asm').read_text())
        mask=(unit/'worked/mask-nine.asm').read_text();inplace=(unit/'worked/inplace-nine.asm').read_text()
        self.assertEqual(mask.replace('    dla $a2, pcm_c','    dla $a2, pcm_b',1),inplace)
        signed=(unit/'worked/signed-one.asm').read_text()
        expected=source.replace('pcm_a: .half 0,1000','pcm_a: .half -30000,1000').replace('pcm_b: .half 0,2000','pcm_b: .half -10000,2000').replace('count: .word 16','count: .word 1').replace('    addu $t0, $t0, $t1','mix_signed:\n    addu $t0, $t0, $t1',1)
        self.assertEqual(expected,signed)

    def test_student_archive_exports_seven_guided_routes_and_all_six_assessments(self):
        from native_lesson import bundle
        import zipfile
        root=Path(__file__).resolve().parents[1];unit=root/'revision/lessons/020-pcm-mixing'
        manifest=json.loads((unit/'package.json').read_text());lesson=json.loads((unit/'lesson.json').read_text())
        self.assertEqual(7,lesson['formatVersion']);self.assertEqual(8,sum(step['kind']=='prediction' for step in lesson['steps']))
        self.assertEqual(22,sum(len(step['stops']) for step in lesson['steps'] if step['kind']=='debug'))
        self.assertEqual(7,sum(step['kind']=='run' for step in lesson['steps']));self.assertEqual(6,sum(step['kind']=='assessment' for step in lesson['steps']))
        with tempfile.TemporaryDirectory() as temporary:
            archive=bundle(unit,Path(temporary)/'student.zip')
            with zipfile.ZipFile(archive) as opened:
                expected={manifest['id']+'/'+name for name in ['package.json',manifest['entry'],manifest['assignment'],manifest['nativeLesson'],*manifest['supportingFiles']]}
                self.assertEqual(expected,set(opened.namelist()))
                for step in lesson['steps']:
                    self.assertIn(manifest['id']+'/'+step.get('source',manifest['entry']),expected)
                    if step['kind']=='assessment':self.assertIn(manifest['id']+'/'+step['caseProfile'],expected)
                self.assertFalse(any('solution' in name or 'reference' in name or 'private' in name for name in expected))
