import hashlib
import json
from pathlib import Path
import tempfile
import unittest
from h263_reconstruct_lesson_cases import OBJECTS,STRIDES,build,cases,expanded,invocation,profiles


class H263ReconstructLessonCasesTest(unittest.TestCase):
    def test_original_case_grid_orders_and_reused_machine_are_complete(self):
        rows=cases()
        self.assertEqual(len(rows),138);self.assertEqual(len({r['name'] for r in rows}),138)
        self.assertEqual(sum(len(r['calls']) for r in rows),146)
        self.assertEqual([sum(r['byteOrder']==o for r in rows) for o in ['big','little']],[69,69])
        self.assertEqual(len(STRIDES),5)
        for row in rows:
            self.assertEqual(row['instructionLimit'],100000);self.assertEqual(row['stackAlignment'],16)
            self.assertEqual(row['callerStackBytes'],0);self.assertEqual(row['preservedRegisters'],[f's{i}' for i in range(8)]+['fp','gp'])
            for call in row['calls']:
                self.assertEqual({r['at']['label']:r['length'] for r in call['callerAccess']['objects']},OBJECTS)
                self.assertEqual(call['callerAccess']['maxAccesses'],10000)
        repeat=[r for r in rows if 'repeated results' in r['name']]
        self.assertEqual(len(repeat),2)
        for row in repeat:
            self.assertEqual(len(row['calls']),5)
            self.assertEqual([int(c['expectedRegisters']['v1'],16) for c in row['calls'][:3]],[64,64,24])

    def test_exact_signed_addition_final_saturation_and_counts_from_actual_selected_bytes(self):
        passed=invalid_sample=0
        for row in cases():
            for call in row['calls']:
                state={label:bytearray([165])*size for label,size in OBJECTS.items()};occupied={k:set() for k in OBJECTS}
                for region in call['setupMemory']:
                    raw=expanded(region);label=region['label'];offset=region['offset'];indices=set(range(offset,offset+len(raw)))
                    self.assertFalse(indices&occupied[label]);occupied[label]|=indices;state[label][offset:offset+len(raw)]=raw
                for label in ['recon_prediction','recon_inverse','recon_settings']:self.assertEqual(occupied[label],set(range(OBJECTS[label])))
                status=int(call['expectedRegisters']['v0'],16)
                if status==1:continue
                order=row['byteOrder'];settings=state['recon_settings'];ps,rs,os=[int.from_bytes(settings[i:i+8],order) for i in [0,8,16]]
                p,r=(call['argumentAddresses'][k] for k in ['a0','a1']);samples=[];pred=[]
                for y in range(8):
                    for x in range(8):
                        pred.append(state[p['label']][p['offset']+y*ps+x]);at=r['offset']+y*rs+2*x;samples.append(int.from_bytes(state[r['label']][at:at+2],order,signed=True))
                if status==2:
                    invalid_sample+=1;self.assertTrue(any(x<-256 or x>255 for x in samples));self.assertEqual(call['memory'],[]);self.assertEqual(call['callerAccess']['writable'],[]);continue
                passed+=1;self.assertTrue(all(-256<=x<=255 for x in samples));sums=[a+b for a,b in zip(pred,samples)];result=[max(0,min(255,v)) for v in sums]
                self.assertEqual(list(b''.join(expanded(m) for m in call['memory'])),result,row['name'])
                self.assertEqual(int(call['expectedRegisters']['v1'],16),sum(v<0 or v>255 for v in sums),row['name'])
                self.assertEqual(len(call['callerAccess']['readable']),17);self.assertEqual(len(call['callerAccess']['writable']),8)
                self.assertEqual(sum(w['length'] for w in call['callerAccess']['writable']),64)
                for i,w in enumerate(call['callerAccess']['writable']):self.assertEqual(w['at']['offset']-call['argumentAddresses']['a2']['offset'],i*os)
        self.assertEqual(passed,94);self.assertEqual(invalid_sample,16)
        row=invocation((8,16,8),True)
        self.assertEqual(list(expanded(row['memory'][0])),[0,0,1,128,254,255,255,255])
        self.assertEqual(int(row['expectedRegisters']['v1'],16),24)

    def test_full_width_alignment_wrap_and_alias_publication_policies(self):
        for little in [False,True]:
            for strides in [(0x100000008,16,8),(8,0x100000010,8),(8,16,0x100000008),(8,17,8)]:
                c=invocation(strides,little,3);self.assertEqual(int(c['expectedRegisters']['v0'],16),1);self.assertEqual(len(c['callerAccess']['readable']),1);self.assertEqual(c['callerAccess']['writable'],[])
            for bad,key,value in [('prediction-wrap','a0',-4),('inverse-wrap','a1',-2),('output-wrap','a2',-4),('settings-wrap','a3',-16)]:
                c=invocation((9,18,11),little,3,bad=bad);self.assertEqual(int(c['arguments'][key],16),value%(1<<64));self.assertNotIn(key,c['argumentAddresses']);self.assertEqual(c['memory'],[])
            c=invocation((9,18,11),little,3,bad='settings-align');self.assertEqual(c['argumentAddresses']['a3']['offset'],4);self.assertEqual(c['callerAccess']['readable'],[])
            c=invocation((9,18,9),little,3,alias=True,bad='sample-low-last');self.assertEqual(c['argumentAddresses']['a0'],c['argumentAddresses']['a2']);self.assertEqual(int(c['expectedRegisters']['v0'],16),2);self.assertEqual(c['memory'],[]);self.assertEqual(c['callerAccess']['writable'],[])
            c=invocation((9,18,9),little,3,alias=True);self.assertTrue(all(w['at']['label']=='recon_prediction' for w in c['callerAccess']['writable']))

    def test_profiles_fit_current_bounds_and_bind_the_unchanged_original_check(self):
        result=profiles();self.assertEqual([len(p['cases']) for p in result.values()],[60,24,36,16,2])
        self.assertEqual({c['name'] for p in result.values() for c in p['cases']},{c['name'] for c in cases()})
        for p in result.values():
            self.assertEqual(p['formatVersion'],21);self.assertLessEqual(len(json.dumps(p,separators=(',',':')).encode())+1,1048576);self.assertLessEqual(sum(c['instructionLimit'] for c in p['cases']),40000000)
            for c in p['cases']:
                for call in c['calls']:
                    self.assertLessEqual(len(call['setupMemory']),64);self.assertLessEqual(sum(len(expanded(m)) for m in call['setupMemory']),1048576)
        root=Path(__file__).resolve().parents[1];binding=json.loads(Path(__file__).with_name('h263_reconstruct_original_check.json').read_text());self.assertEqual(hashlib.sha256((root/binding['source']).read_bytes()).hexdigest(),binding['sha256'])
        with tempfile.TemporaryDirectory() as directory:
            self.assertEqual(build(directory),build(directory,True));(Path(directory)/'samples.json').write_text('{}')
            with self.assertRaisesRegex(ValueError,'Stale reconstruction profile'):build(directory,True)


if __name__=='__main__':unittest.main()
