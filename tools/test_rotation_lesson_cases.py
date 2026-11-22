import json
from pathlib import Path
import tempfile
import unittest
import zipfile

from native_lesson import bundle

import rotation_lesson_cases as rotation


class RotationLessonCasesTests(unittest.TestCase):
    def test_coordinate_mapping_is_bijective_and_four_turns_restore_every_small_rectangle(self):
        for rows in range(17):
            for columns in range(17):
                mapping=rotation.mapping(rows,columns)
                self.assertEqual(list(range(rows*columns)),sorted(mapping))
                source=list(range(rows*columns));actual=source[:];r,c=rows,columns
                for _ in range(4):
                    target=[None]*len(actual)
                    for n,destination in enumerate(rotation.mapping(r,c)):target[destination]=actual[n]
                    actual=target;r,c=c,r
                self.assertEqual(source,actual)
        self.assertEqual([1,3,5,0,2,4],rotation.mapping(2,3))
        self.assertEqual([3,0,4,1,5,2],rotation.rotate(list(range(256)),2,3,0,0)[:6])

    def test_profiles_preserve_full_allocations_and_require_each_repeated_mapped_transfer(self):
        profile=rotation.profile();self.assertEqual(5,profile['formatVersion']);self.assertEqual(22,len(profile['cases']))
        for case,(rows,columns) in zip(profile['cases'],rotation.SHAPES):
            count=rows*columns;self.assertEqual(2,len(case['calls']))
            self.assertEqual(['rotate_done','rotate_exit'],[c['returnTo'] for c in case['calls']])
            self.assertTrue(case['protectCallerStack']);self.assertEqual(10,len(case['preservedRegisters']))
            mapping=case['transfers'][0];self.assertEqual(rotation.mapping(rows,columns),mapping['destinationIndices'])
            before={r['label']:bytes.fromhex(r['bytes']) for r in case['initialMemory']}
            after={r['label']:bytes.fromhex(r['bytes']) for r in case['calls'][0]['memory']}
            self.assertEqual(1024,len(after['output']));self.assertEqual(before['input'],after['input'])
            for label in rotation.GUARDS+('copied',):self.assertEqual(before[label],after[label])
            source_start=mapping['source']['offset'];dest_start=mapping['destination']['offset']
            for cell,to in enumerate(mapping['destinationIndices']):
                self.assertEqual(before['input'][source_start+4*cell:source_start+4*cell+4],after['output'][dest_start+4*to:dest_start+4*to+4])
            self.assertEqual(before['output'][:dest_start],after['output'][:dest_start])
            self.assertEqual(before['output'][dest_start+4*count:],after['output'][dest_start+4*count:])
            for bounds in case['accessBounds']:self.assertEqual(4*count,bounds['permittedLength'])
        caller=profile['cases'][-1]
        self.assertEqual('main',caller['calls'][0]['entry']);self.assertEqual('rotate_exit',caller['calls'][0]['returnTo'])
        self.assertEqual('0f000000',next(r['bytes'] for r in caller['calls'][0]['memory'] if r['label']=='copied'))
        self.assertNotIn('copied',[r['label'] for r in caller['noStores']])

    def test_generated_profiles_and_sources_remain_current_without_exporting_an_implementation(self):
        root=Path(__file__).resolve().parents[1];unit=root/'revision/lessons/011-memory-locality'
        rotation.build(unit/'independent',check=True)
        for name in ['matrix-locality.asm','matrix-rotate.asm']:
            target=unit/name if name=='matrix-locality.asm' else unit/'independent'/name
            self.assertEqual((root/'revision/labs/files'/name).read_bytes(),target.read_bytes())
        self.assertIn('rotate_clockwise:\n    break',(unit/'independent/matrix-rotate.asm').read_text())
        self.assertEqual((root/'revision/labs/checks/matrix-rotate.kts').read_bytes(),(unit/'independent/matrix-rotate.kts').read_bytes())
        with tempfile.TemporaryDirectory() as temporary:
            folder=Path(temporary);rotation.build(folder)
            self.assertEqual(rotation.profile(),json.loads((folder/'matrix-rotate.cases.json').read_text()))
            self.assertLess((folder/'matrix-rotate.cases.json').stat().st_size,1048576)
            (folder/'caller-exit.cases.json').write_text('stale')
            with self.assertRaises(ValueError):rotation.build(folder,check=True)

    def test_archive_retains_both_worked_sources_and_all_independent_dependencies(self):
        root=Path(__file__).resolve().parents[1];unit=root/'revision/lessons/011-memory-locality'
        lesson=json.loads((unit/'lesson.json').read_text());quizzes=[s for s in lesson['steps'] if s['kind']=='prediction']
        self.assertEqual(6,len(quizzes));self.assertEqual({'a','b','c'},{q['correctChoice'] for q in quizzes})
        debug=[s for s in lesson['steps'] if s['kind']=='debug']
        self.assertEqual([4,4],[len(s['stops']) for s in debug])
        self.assertEqual('worked/matrix-tiled.asm',debug[1]['source'])
        ordinary=(unit/'matrix-locality.asm').read_text();tiled=(unit/'worked/matrix-tiled.asm').read_text()
        self.assertEqual(ordinary.replace('.eqv METHOD 0','.eqv METHOD 1'),tiled)
        with tempfile.TemporaryDirectory() as temporary:
            with zipfile.ZipFile(bundle(unit,Path(temporary)/'student.zip')) as archive:
                names=set(archive.namelist())
                for step in lesson['steps']:
                    self.assertIn('011-memory-locality/'+step.get('source','matrix-locality.asm'),names)
                    if step['kind']=='assessment':self.assertIn('011-memory-locality/'+step['caseProfile'],names)
                self.assertFalse(any('solution' in n or 'reference' in n or 'private' in n for n in names))
