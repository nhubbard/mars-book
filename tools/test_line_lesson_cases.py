import itertools
import json
from pathlib import Path
import tempfile
import unittest
import zipfile

from native_lesson import bundle

import line_lesson_cases as line


class LineLessonCasesTests(unittest.TestCase):
    def test_oracle_matches_a_bounded_byte_scanner_and_delimiter_precedes_capacity_rejection(self):
        def scan(data,n):
            length=0;total=0
            while length<n and length<=32:
                if data[length]==10:return length+1,length,total&255
                if length==32:return -2,None,None
                total+=data[length];length+=1
            return -1,None,None
        for length in range(7):
            for data in itertools.product((0,10,255),repeat=length):
                for n in range(length+1):self.assertEqual(scan(data,n),line.oracle(data,n))
        for byte in range(256):
            for length in (31,32,33,64):
                data=[byte]*length+[10,99,255]
                self.assertEqual(scan(data,len(data)),line.oracle(data,len(data)))
        self.assertEqual((33,32,32),line.oracle([1]*32+[10],33))
        self.assertEqual((-1,None,None),line.oracle([1]*32,32))
        self.assertEqual((-2,None,None),line.oracle([1]*33+[10],34))
        self.assertEqual((4,3,193),line.oracle([65,0,128,10,66],5))

    def test_full_original_corpus_is_bounded_without_losing_pointer_store_or_continuation_contracts(self):
        trials=line.trials();self.assertEqual(393,len(trials));self.assertEqual(393,len({t['name'] for t in trials}))
        self.assertEqual(set(range(256)),{t['data'][0] for t in trials if t['group']=='bytes'})
        profiles=line.profiles();self.assertEqual([139,256],[len(p['cases']) for p in profiles.values()])
        all_cases=[c for p in profiles.values() for c in p['cases']]
        self.assertEqual(789,sum(len(c['calls']) for c in all_cases))
        for number,trial in enumerate(trials):
            case=line.function_case(number,trial);result,length,total=line.oracle(trial['data'],trial['count'])
            self.assertEqual(['line_function_return','line_done'],[c['returnTo'] for c in case['calls']])
            self.assertEqual([{'from':'line_function_return','until':'line_done'}],case['noExecution'])
            self.assertTrue(case['protectCallerStack']);self.assertEqual(10,len(case['preservedRegisters']))
            bounds=case['accessBounds'][0];self.assertEqual(4+trial['offset'],bounds['permittedOffset'])
            self.assertEqual(result if result>0 else min(trial['count'],33),bounds['permittedLength'])
            self.assertEqual(264,len(bytes.fromhex(case['initialMemory'][0]['bytes'])))
            for call in case['calls']:
                if result>0:self.assertEqual({'label':'input','offset':trial['offset']},call['expectedAddresses']['v1'])
                else:self.assertEqual('0x0000000000000000',call['expectedRegisters']['v1'])
            store=case['storeBounds'][0];self.assertEqual({'label':'state_end','offset':4},store['until'])
            if result>0:self.assertEqual(8,store['permittedLength'])
            else:self.assertNotIn('permittedFrom',store)
        caller=line.caller_case()['calls'][0]
        self.assertEqual('main',caller['entry']);self.assertEqual('line_done',caller['returnTo'])
        self.assertEqual([{'at':{'label':'payload_address'},'value':{'label':'input'}}],caller['addressMemory'])

    def test_generated_profiles_and_original_sources_remain_current_without_a_solution(self):
        root=Path(__file__).resolve().parents[1];unit=root/'revision/lessons/013-bounded-interfaces'
        for name in ['bounded-packet.asm','bounded-line.asm']:
            target=unit/name if name=='bounded-packet.asm' else unit/'independent'/name
            self.assertEqual((root/'revision/labs/files'/name).read_bytes(),target.read_bytes())
        self.assertIn('read_line:\n    break',(unit/'independent/bounded-line.asm').read_text())
        self.assertEqual((root/'revision/labs/checks/bounded-line.kts').read_bytes(),(unit/'independent/bounded-line.kts').read_bytes())
        line.build(unit/'independent',check=True)
        with tempfile.TemporaryDirectory() as temporary:
            folder=Path(temporary);line.build(folder)
            for name,profile in line.profiles().items():
                path=folder/f'bounded-line-{name}.cases.json';self.assertLess(path.stat().st_size,1048576)
                self.assertEqual(profile,json.loads(path.read_text()))
            (folder/'caller-exit.cases.json').write_text('stale')
            with self.assertRaises(ValueError):line.build(folder,check=True)

    def test_archive_keeps_three_worked_sources_and_all_independent_profiles(self):
        root=Path(__file__).resolve().parents[1];unit=root/'revision/lessons/013-bounded-interfaces'
        lesson=json.loads((unit/'lesson.json').read_text());quizzes=[s for s in lesson['steps'] if s['kind']=='prediction']
        self.assertEqual(7,len(quizzes));self.assertEqual({'a','b','c'},{q['correctChoice'] for q in quizzes})
        self.assertEqual([8,2,2],[len(s['stops']) for s in lesson['steps'] if s['kind']=='debug'])
        worked=(unit/'bounded-packet.asm').read_text()
        self.assertEqual(worked.replace('available: .word 12','available: .word 7'),(unit/'worked/packet-short.asm').read_text())
        self.assertEqual(worked.replace('127,-128,-1,26','127,-128,-1,27'),(unit/'worked/packet-checksum.asm').read_text())
        assessments=[s for s in lesson['steps'] if s['kind']=='assessment']
        self.assertEqual(['function','function','console'],[s['checkKind'] for s in assessments])
        self.assertEqual(1,len({s['source'] for s in assessments}))
        with tempfile.TemporaryDirectory() as temporary:
            with zipfile.ZipFile(bundle(unit,Path(temporary)/'student.zip')) as archive:
                for s in lesson['steps']:
                    self.assertIn('013-bounded-interfaces/'+s.get('source','bounded-packet.asm'),archive.namelist())
                    if s['kind']=='assessment':self.assertIn('013-bounded-interfaces/'+s['caseProfile'],archive.namelist())
                self.assertFalse(any('solution' in n or 'reference' in n or 'private' in n for n in archive.namelist()))
