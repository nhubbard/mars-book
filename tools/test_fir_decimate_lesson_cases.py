from pathlib import Path
import json
import tempfile
import unittest

import fir_decimate_lesson_cases as fir


class FirDecimateLessonCasesTests(unittest.TestCase):
    def test_all_original_shapes_offsets_and_signed_rounding_boundaries(self):
        trials=fir.trials();self.assertEqual(141,len(trials))
        self.assertEqual({(n,f'family-{family}') for n in range(33) for family in range(4)}, {(t['count'],t['group']) for t in trials[:132]})
        for trial in trials:
            row=fir.construction(trial);n=trial['count'];index=trial['index']
            self.assertEqual(7 if n==0 else 2*n+6,len(trial['input']));self.assertEqual(8,len(trial['taps']))
            self.assertEqual((index%32,(index*7+1)%32,(index*13+3)%32),(row['sourceOffset'],row['coeffOffset'],row['outputOffset']))
            self.assertEqual({'source':192,'coeff':64,'output':192},{name:len(image) for name,image in row['arenas'].items()})
            self.assertNotEqual(row['source'],row['dest'])
            for offset,value in enumerate(row['expected']):self.assertNotEqual(value,row['arenas'][row['dest']][row['outputOffset']+offset])
        self.assertEqual([0,1,1,0,0,-1],[fir.quantized(t)[0] for t in trials[132:138]])
        self.assertEqual([32767,-32768,4],[fir.results(t)[0] for t in trials[-3:]])
        self.assertEqual([0,0,0],[fir.clips(t) for t in trials[-3:]])
        minimum=next(t for t in trials if t['count']==1 and t['group']=='family-2')
        self.assertEqual([8589934592],fir.numerators(minimum));self.assertEqual([262144],fir.quantized(minimum));self.assertEqual([32767],fir.results(minimum));self.assertEqual(1,fir.clips(minimum))
        impulse=next(t for t in trials if t['count']==32 and t['group']=='family-1')
        self.assertEqual([4000,1000,500,0,-500,0],fir.results(impulse)[:6])

    def test_all_original_cases_keep_their_budget_and_correct_empty_permissions(self):
        profiles=fir.profiles();self.assertEqual([43,36,34,33],[len(profile['cases']) for profile in profiles.values()])
        cases=[case for profile in profiles.values() for case in profile['cases']]
        self.assertEqual(146,len(cases));self.assertEqual(155,sum(len(case['calls']) for case in cases))
        for profile in profiles.values():
            self.assertEqual(9,profile['formatVersion']);self.assertLessEqual(sum(case['instructionLimit'] for case in profile['cases']),10000000)
        for case in cases:
            self.assertEqual(9,len(case['preservedRegisters']));self.assertEqual([{'from':'function_return','until':'done'}],case['noExecution'])
            self.assertEqual(300000 if len(case['calls'])==3 else 100000,case['instructionLimit'])
            self.assertNotIn('initializedOutput',case)
            policy=case['accessPolicies'][0];self.assertEqual({'label':'source_guard'},policy['from']);self.assertEqual({'label':'caller_end','offset':4},policy['until'])
            if case['name'].startswith('decimate count=0 '):self.assertTrue(all(window['length']==0 for window in policy['readable']+policy['writable']))

    def test_split_calls_keep_even_phase_whole_result_and_reinitialize_counts(self):
        cases=[case for profile in fir.profiles().values() for case in profile['cases']]
        split=[case for case in cases if len(case['calls'])==3];self.assertEqual(4,len(split))
        for case in split:
            whole,first,last=case['calls'];cut=int(first['arguments']['a3'],16)
            self.assertEqual([32,cut,32-cut],[int(call['arguments']['a3'],16) for call in case['calls']])
            self.assertEqual(13+4*cut,last['argumentAddresses']['a0']['offset']);self.assertEqual(99+2*cut,last['argumentAddresses']['a2']['offset'])
            for call in case['calls']:
                n=int(call['arguments']['a3'],16);policy=call['accessPolicies'][0]
                self.assertEqual([0 if n==0 else 4*n+12,0 if n==0 else 16,2*n],[window['length'] for window in policy['readable']])
                self.assertEqual(call['argumentAddresses']['a0'],policy['readable'][0]['at'])
                self.assertEqual(call['argumentAddresses']['a2'],policy['writable'][0]['at'])
                self.assertEqual([2*n],[window['length'] for window in policy['writable']])
            memory=bytes.fromhex(next(row['bytes'] for row in last['memory'] if row['label']=='output'))
            self.assertEqual(memory[3:67],memory[99:163])
        clipped=next(case for case in split if case['name'].endswith('family-2'))
        self.assertEqual([32,5,27],[int(call['expectedRegisters']['v1'],16) for call in clipped['calls']])
        caller=next(case for case in cases if case['calls'][0]['entry']=='main')
        self.assertEqual(['decimate_ready','results_ready'],[call['returnTo'] for call in caller['calls']])
        self.assertEqual(['output'],[window['at']['label'] for window in caller['calls'][0]['accessPolicies'][0]['writable']])
        self.assertEqual([],caller['calls'][1]['accessPolicies'][0]['readable'])
        self.assertEqual(['processed','clipped'],[window['at']['label'] for window in caller['calls'][1]['accessPolicies'][0]['writable']])
        for call,count,clip in zip(caller['calls'],[99,8],[99,0]):
            memory={row['label']:row['bytes'] for row in call['memory']}
            self.assertEqual(count,int.from_bytes(bytes.fromhex(memory['processed']),'little'));self.assertEqual(clip,int.from_bytes(bytes.fromhex(memory['clipped']),'little'))
            output=bytes.fromhex(memory['output']);self.assertEqual([2000]*4+[0]*4,[int.from_bytes(output[i:i+2],'little',signed=True) for i in range(0,16,2)])

    def test_checked_in_profiles_and_original_student_files_remain_current(self):
        root=Path(__file__).resolve().parents[1];unit=root/'revision/lessons/021-fixed-point-fir';fir.build(unit/'independent',check=True)
        for suffix,directory in [('asm','files'),('kts','checks')]:
            self.assertEqual((root/f'revision/labs/{directory}/fir-decimate.{suffix}').read_bytes(),(unit/f'independent/fir-decimate.{suffix}').read_bytes())
        self.assertEqual((root/'revision/labs/files/fir-filter.asm').read_bytes(),(unit/'fir-filter.asm').read_bytes())
        with tempfile.TemporaryDirectory() as temporary:
            directory=Path(temporary);fir.build(directory);fir.build(directory,check=True)
            for name,profile in fir.profiles().items():self.assertEqual(profile,json.loads((directory/f'fir-decimate-{name}.cases.json').read_text()))
            (directory/'caller-exit.cases.json').write_text('{}')
            with self.assertRaises(ValueError):fir.build(directory,check=True)

class FirNativePackageTests(unittest.TestCase):
    def test_all_observation_sources_preserve_the_worked_function_bodies(self):
        root=Path(__file__).resolve().parents[1];unit=root/'revision/lessons/021-fixed-point-fir';source=(unit/'fir-filter.asm').read_text()
        body=source.split('fir_q15:',1)[1]
        candidates=list((unit/'worked').glob('*.asm'));self.assertEqual(9,len(candidates))
        for candidate in candidates:self.assertEqual(body,candidate.read_text().split('fir_q15:',1)[1])
        scalar=(unit/'worked/scalar-impulse.asm').read_text()
        self.assertEqual(source.replace('    balc fir_q15','    balc fir_scalar',1),scalar)
        split=(unit/'worked/split-five.asm').read_text()
        self.assertIn('daddiu $a0, $a0, 10',split);self.assertIn('li $a3, 11',split)

    def test_archive_exports_complete_debug_run_and_independent_routes_without_private_material(self):
        from native_lesson import bundle
        import zipfile
        root=Path(__file__).resolve().parents[1];unit=root/'revision/lessons/021-fixed-point-fir'
        manifest=json.loads((unit/'package.json').read_text());lesson=json.loads((unit/'lesson.json').read_text())
        self.assertEqual(7,lesson['formatVersion']);self.assertEqual(9,sum(step['kind']=='prediction' for step in lesson['steps']))
        self.assertEqual(32,sum(len(step['stops']) for step in lesson['steps'] if step['kind']=='debug'))
        self.assertEqual(3,sum(step['kind']=='run' for step in lesson['steps']));self.assertEqual(5,sum(step['kind']=='assessment' for step in lesson['steps']))
        with tempfile.TemporaryDirectory() as temporary:
            archive=bundle(unit,Path(temporary)/'student.zip')
            with zipfile.ZipFile(archive) as opened:
                expected={manifest['id']+'/'+name for name in ['package.json',manifest['entry'],manifest['assignment'],manifest['nativeLesson'],*manifest['supportingFiles']]}
                self.assertEqual(expected,set(opened.namelist()))
                for step in lesson['steps']:
                    self.assertIn(manifest['id']+'/'+step.get('source',manifest['entry']),expected)
                    if step['kind']=='assessment':self.assertIn(manifest['id']+'/'+step['caseProfile'],expected)
                self.assertFalse(any('solution' in name or 'reference' in name or 'private' in name for name in expected))
