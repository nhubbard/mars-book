from collections import Counter
import itertools
import json
from pathlib import Path
import tempfile
import unittest

import partition_lesson_cases as partition


class PartitionLessonCasesTests(unittest.TestCase):
    def test_stability_permutation_and_idempotence_for_small_inputs_and_full_width_edges(self):
        for length in range(7):
            for values in itertools.product((-1,0,1),repeat=length):
                words=[123,*values,456]
                actual=partition.partition(words,1,length)
                expected=[value for _,value in sorted(enumerate(values),key=lambda item:(item[1]>=0,item[0]))]
                self.assertEqual([123,*expected,456],actual)
                self.assertEqual(Counter(words),Counter(actual))
                self.assertEqual(actual,partition.partition(actual,1,length))
        self.assertEqual([-2147483648,-1,2147483647,0],partition.partition([2147483647,-2147483648,0,-1],0,4))
        self.assertEqual('00000080ffffffffffffff7f',partition.word_bytes([-2147483648,-1,2147483647]))

    def test_all_subspans_and_repeated_calls_have_guard_access_and_caller_contracts(self):
        cases=partition.profile()['cases'];trials=partition.trials()
        self.assertEqual(4,partition.profile()['formatVersion'])
        self.assertEqual(56,len(cases))
        self.assertEqual({(offset,length) for offset in range(9) for length in range(9-offset)},
                         {(offset,length) for _,offset,length in trials[len(partition.EXAMPLES):]})
        for case,(words,offset,length) in zip(cases,trials):
            self.assertTrue(case['protectCallerStack'])
            self.assertEqual(32,case['callerStackBytes'])
            self.assertEqual(10,len(case['preservedRegisters']))
            self.assertEqual(6,len(case['noStores']))
            self.assertEqual({'label':'values','length':40,'offset':-4,'permittedOffset':4*offset,'permittedLength':4*length},case['accessBounds'][0])
            initial={r['label']:bytes.fromhex(r['bytes']) for r in case['initialMemory']}
            self.assertNotEqual(length,int.from_bytes(initial['count'],'little'))
            self.assertEqual(partition.word_bytes(words),initial['values'].hex())
            self.assertEqual(['partition_done','partition_exit'],[c['returnTo'] for c in case['calls']])
            for call in case['calls']:
                self.assertEqual({'a0':{'label':'values','offset':4*offset}},call['argumentAddresses'])
                self.assertEqual(length,int(call['arguments']['a1'],16))
                self.assertEqual(sum(v<0 for v in words[offset:offset+length]),int(call['expectedRegisters']['v0'],16))
                expected={r['label']:bytes.fromhex(r['bytes']) for r in call['memory']}
                self.assertEqual(initial.keys(),expected.keys())
                self.assertEqual(initial['values'][:4*offset],expected['values'][:4*offset])
                self.assertEqual(initial['values'][4*(offset+length):],expected['values'][4*(offset+length):])
                for name in initial.keys()-{'values'}:self.assertEqual(initial[name],expected[name])
            self.assertEqual(case['calls'][0]['memory'],case['calls'][1]['memory'])
        self.assertIn('callerStores.expectNone()',partition.script())
        self.assertEqual(112,partition.script().count('callFunction("partition_negative"'))

    def test_generated_exports_and_foundation_sources_remain_current_without_an_implementation(self):
        root=Path(__file__).resolve().parents[1];unit=root/'revision/lessons/009-sorting-costs'
        self.assertEqual((root/'revision/labs/files/sorting-costs.asm').read_bytes(),(unit/'sorting-costs.asm').read_bytes())
        starter=(root/'revision/labs/files/partition-negative.asm').read_text()
        self.assertEqual(starter.replace('values: .word','values_before: .word 0x76543210\nvalues: .word',1),
                         (unit/'independent/partition-negative.asm').read_text())
        self.assertIn('partition_negative:\n    break',(unit/'independent/partition-negative.asm').read_text())
        partition.build(unit/'independent',check=True)
        with tempfile.TemporaryDirectory() as temporary:
            directory=Path(temporary);partition.build(directory)
            self.assertEqual(partition.profile(),json.loads((directory/'partition-negative.cases.json').read_text()))
            (directory/'partition-negative.kts').write_text('stale')
            with self.assertRaises(ValueError):partition.build(directory,check=True)
