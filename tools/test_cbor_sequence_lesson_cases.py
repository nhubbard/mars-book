import hashlib
import json
from pathlib import Path
import unittest
from cbor_sequence_lesson_cases import (ARENAS, CALLER, INVALID, call, definition,
    head, outcome, profiles, sequence_count, setup, supplemental, trials)


class CborSequenceLessonCasesTest(unittest.TestCase):
    def test_original_grid_and_budgets_are_preserved_and_all_profiles_are_bounded(self):
        rows=trials();self.assertEqual(len(rows),203);self.assertEqual(len({r['name'] for r in rows}),203)
        self.assertTrue(all(r['instructionLimit']==4000000 for r in rows))
        sample=Path(__file__).with_name('cbor_sequence_samples.json');data=json.loads(sample.read_text())
        root=sample.parents[1];self.assertEqual(data['originalSha256'],hashlib.sha256((root/data['originalSource']).read_bytes()).hexdigest())
        files=profiles();self.assertEqual(len(files),104)
        cases=[c for p in files.values() for c in p['cases']]
        self.assertEqual(len(cases),205);self.assertEqual(sum(len(c['calls']) for c in cases),208)
        self.assertTrue({r['name'] for r in rows}<={c['name'] for c in cases})
        for p in files.values():
            self.assertEqual(p['formatVersion'],14)
            self.assertLessEqual(sum(c['instructionLimit'] for c in p['cases']),10000000)
            self.assertLessEqual(len(json.dumps(p,separators=(',',':')).encode())+1,1024*1024)

    def test_local_generic_syntax_tracks_independent_map_identity_and_inherited_depth(self):
        self.assertEqual(sequence_count(bytes.fromhex('a10000a1180000'),0),2)
        with self.assertRaises(ValueError):sequence_count(bytes.fromhex('a20000180001'),0)
        self.assertEqual(sequence_count(bytes.fromhex('4180'),8),1,'A byte string payload is not a nested value')
        self.assertEqual(sequence_count(bytes.fromhex('80'),7),1)
        for raw in ['80','a0']:
            with self.assertRaises(ValueError):sequence_count(bytes.fromhex(raw),8)
        with self.assertRaises(ValueError):sequence_count(bytes.fromhex('808180'),7)
        self.assertEqual(sequence_count(bytes.fromhex('0018181bffffffffffffffff'),8),3)
        for raw in ['18','1900','1b00000000000000','f4','60','c0','9fff','5821'+'00'*33,'9841'+'00'*65]:
            with self.assertRaises(ValueError):sequence_count(bytes.fromhex(raw),0)
        self.assertEqual(head(bytes.fromhex('1800'),0),(0,0,2),'Nonpreferred heads remain admitted by this profile')

    def test_setup_exact_snapshots_history_and_early_pointer_permission_match_the_original_contract(self):
        for row in trials():
            case=definition(row);c=case['calls'][0];seed=setup(row)
            self.assertEqual({v['label']:bytes.fromhex(v['bytes']) for v in case['initialMemory']},seed)
            self.assertEqual({v['label']:bytes.fromhex(v['bytes']) for v in c['memory']},{k:seed[k] for k in CALLER})
            self.assertEqual(case['callerStackBytes'],0);self.assertEqual(len(case['preservedRegisters']),9)
            self.assertEqual(case['initialRegisterAddresses']['sp'],{'label':'workspace','offset':8192})
            self.assertEqual(seed['workspace'],bytes([165])*8192)
            self.assertEqual(len(c['accessPolicies']),3)
            self.assertEqual(c['accessHistory']['maxAccesses'],500000)
            self.assertEqual([(w['at']['label'],w['length']) for w in c['accessHistory']['regions']],[('source',1280),('workspace',8192)])
            early=row['count']>1024 or row['depth']>8 or row['count']==0
            for policy in c['accessPolicies']:
                self.assertEqual(policy['maxAccesses'],500000);self.assertEqual(policy['writable'],[])
                self.assertEqual(policy['readable'],[] if early else [{'at':{'label':'source','offset':row['offset']},'length':row['count']}])
            if early:self.assertEqual(c['arguments']['a0'],f'0x{INVALID:016x}')
            else:self.assertEqual(c['argumentAddresses']['a0'],{'label':'source','offset':row['offset']})
        prefix=next(r for r in trials() if r['name']=='selected prefix excludes unsupported suffix')
        self.assertEqual(outcome(prefix),(0,2))
        both=next(r for r in trials() if r['name']=='early count 1025 depth 9')
        self.assertEqual(outcome(both),(2,0))
        self.assertEqual(outcome(next(r for r in trials() if r['name']=='selected zero excludes real input')),(0,0))

    def test_supplemental_cases_require_real_caller_exit_and_clear_results_between_invocations(self):
        extra=supplemental();caller=extra['caller'][0]
        self.assertNotIn('initialMemory',caller)
        self.assertEqual(caller['calls'][0]['entry'],'main');self.assertEqual(caller['calls'][0]['returnTo'],'sequence_return')
        self.assertEqual(caller['exit'],{'code':0,'memory':[{'label':'status','bytes':'00000000'},{'label':'count','bytes':'0100000000000000'}]})
        retry=extra['retry'][0];self.assertEqual(len(retry['calls']),4)
        self.assertEqual([int(c['expectedRegisters']['v0'],16) for c in retry['calls']],[1,0,0,1])
        self.assertEqual([int(c['expectedRegisters']['v1'],16) for c in retry['calls']],[0,1,0,0])
        self.assertTrue(all(c['accessHistory']['maxAccesses']==500000 for c in retry['calls']))
