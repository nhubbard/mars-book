from pathlib import Path
import json
import tempfile
import unittest
import zlib
import crc_chunk_lesson_cases as crc


class CrcChunkLessonCasesTests(unittest.TestCase):
    def test_reflected_entries_and_published_checksums_agree_with_independent_zlib(self):
        self.assertEqual([0,0x77073096,0xedb88320,0x2d02ef8d], [crc.TABLE[n] for n in [0,1,128,255]])
        self.assertEqual(0xcbf43926, zlib.crc32(b'123456789'))
        self.assertEqual(0xae426082, zlib.crc32(b'IEND'))
        for n in range(256):
            self.assertEqual(zlib.crc32(bytes([n])), crc.raw_update(bytes([n]),0xffffffff)^0xffffffff)
        raw = crc.chunk(b'IEND',b'')
        self.assertEqual(bytes.fromhex('0000000049454e44ae426082'),raw)
        self.assertEqual(raw[:-1]+bytes([raw[-1]^1]),crc.chunk(b'IEND',b'',True))

    def test_entire_original_grid_offsets_types_and_unsigned_rejections_are_retained(self):
        rows = crc.trials(); self.assertEqual(135,len(rows))
        self.assertEqual([80,32,23], [sum(r['group']==group for r in rows) for group in ['short','long','rejections']])
        for index, row in enumerate(rows[:112]):
            j = index//8; k = index//2%4; n = crc.LENGTHS[j]
            self.assertEqual((j*7+k)%32,row['offset']); self.assertEqual((j+k)%16*4,row['tableOffset'])
            self.assertEqual(crc.TYPES[k],row['raw'][4:8]);self.assertEqual(bytes((i*73+n)%256 for i in range(n)),row['raw'][8:-4])
            self.assertEqual(n+12,row['selected']);self.assertEqual(1000000,row['limit'])
            self.assertEqual(index%2,crc.outcome(row)['status'])
        self.assertEqual([*range(12),4097,-(1<<63),-1],[r['selected'] for r in rows[112:127]])
        self.assertEqual([0,1,2,7,4084,4085,0x80000000,0xffffffff],[int.from_bytes(r['raw'][:4],'big') for r in rows[-8:]])
        self.assertTrue(all(r['limit']==100000 and crc.outcome(r)['status']==2 for r in rows[112:]))

    def test_full_source_and_table_seeds_match_exactly_and_input_word_order_is_not_crc_order(self):
        for trial in crc.trials():
            row = crc.construction(trial); source = bytearray([165]*4160)
            source[trial['offset']:trial['offset']+len(trial['raw'])]=trial['raw'];self.assertEqual(bytes(source),row['source'])
            table = [int.from_bytes(row['table'][i:i+4],'little') for i in range(0,1088,4)]
            for n,value in enumerate(table):
                index=n-trial['tableOffset']//4
                self.assertEqual(crc.TABLE[index] if 0<=index<256 else 0x6a6b6c6d,value)
        self.assertEqual('826042ae',crc.little(zlib.crc32(b'IEND')).hex())

    def test_permissions_preserve_full_watched_span_and_validate_lengths_before_pointer_access(self):
        for trial in crc.trials():
            row=crc.outcome(trial);case=crc.definition(trial);call=case['calls'][0];policy=call['accessPolicies'][0]
            self.assertEqual({'label':'data'},policy['from']);self.assertEqual({'label':'file_status','offset':4},policy['until'])
            self.assertEqual(200000,policy['maxAccesses']);self.assertEqual([],policy['writable'])
            self.assertEqual([trial['selected'],1024] if row['valid'] else [4] if row['validCount'] else [],[w['length'] for w in policy['readable']])
            self.assertEqual(row['actual'],int(call['expectedRegisters']['v0'],16));self.assertEqual(row['status'],int(call['expectedRegisters']['v1'],16))
            self.assertEqual(trial['selected']&crc.MASK,int(call['arguments']['a1'],16))
            if not row['validCount']:self.assertEqual(1,int(call['arguments']['a0'],16))
            if not row['valid']:self.assertEqual(1,int(call['arguments']['a2'],16))
            self.assertEqual(0x7fff0000,int(case['initialRegisters']['sp'],16))
            for n,name in enumerate([*(f's{n}' for n in range(8)),'fp']):self.assertEqual(0x56789abc00000000+n*997,int(case['initialRegisters'][name],16))
            for n in range(10):self.assertEqual((-1234-n)&crc.MASK,int(case['initialRegisters'][f't{n}'],16))

    def test_same_session_retry_keeps_distinct_permissions_and_recomputes_every_result(self):
        case=crc.reuse_case();calls=case['calls']
        self.assertEqual([2,0,2,1,0],[int(c['expectedRegisters']['v1'],16) for c in calls])
        self.assertEqual([0,0xae426082,0],[int(c['expectedRegisters']['v0'],16) for c in calls[:3]])
        self.assertEqual(calls[3]['expectedRegisters']['v0'],calls[4]['expectedRegisters']['v0'])
        self.assertEqual([[],[12,1024],[4],[15,1024],[15,1024]],[[w['length'] for w in c['accessPolicies'][0]['readable']] for c in calls])
        self.assertEqual([67,131,195,259],[c['accessPolicies'][0]['readable'][0]['at']['offset'] for c in calls[1:]])
        self.assertTrue(all(c['accessPolicies'][0]['maxAccesses']==200000 for c in calls))

    def test_caller_observes_table_preparation_then_publication_without_executing_exit_early(self):
        case=crc.caller_case();self.assertEqual(100000,case['instructionLimit'])
        first,second=case['calls'];self.assertEqual(['chunk_done','results_ready'],[c['returnTo'] for c in case['calls']])
        expected={v['label']:v['bytes'] for v in first['memory']}
        self.assertEqual('ffffffff',expected['raw_result']);self.assertEqual('ffffffff',expected['file_status'])
        self.assertEqual(crc.TABLE,[int.from_bytes(bytes.fromhex(expected['crc_table'])[i:i+4],'little') for i in range(0,1024,4)])
        self.assertEqual(['raw_result','file_status'],[v['label'] for v in second['memory']])
        self.assertEqual([{'from':'results_ready','until':'function_return'}],case['noExecution'])
        self.assertEqual(['crc_table'],[w['at']['label'] for w in first['accessPolicies'][0]['writable']])

    def test_all_case_budgets_fit_bounded_profiles_and_data_author_is_reproducible(self):
        profiles=crc.profiles();self.assertEqual(16,len(profiles));cases=[c for p in profiles.values() for c in p['cases']]
        self.assertEqual(137,len(cases));self.assertEqual(142,sum(len(c['calls']) for c in cases));self.assertEqual(137,len({c['name'] for c in cases}))
        for profile in profiles.values():
            self.assertEqual(12,profile['formatVersion']);self.assertLessEqual(sum(c['instructionLimit'] for c in profile['cases']),10000000)
            self.assertLessEqual(len(json.dumps(profile,separators=(',',':')).encode()),1024*1024)
        with tempfile.TemporaryDirectory() as tmp:
            files=crc.build(tmp);self.assertEqual(17,len(files));crc.build(tmp,check=True)
            exit=json.loads((Path(tmp)/'caller-exit.cases.json').read_text());self.assertEqual(100000,exit['cases'][0]['instructionLimit'])
            (Path(tmp)/next(iter(files))).write_text('{}')
            with self.assertRaises(ValueError):crc.build(tmp,check=True)
