import hashlib
import json
from pathlib import Path
import tempfile
import unittest
from video_bounds_lesson_cases import OBJECTS,DIMENSIONS,build,cases,changed_indices,expanded,invocation,profiles,rectangle


class VideoBoundsLessonCasesTest(unittest.TestCase):
    def test_complete_original_grid_orders_and_reused_machine_calls(self):
        rows=cases()
        self.assertEqual(len(rows),182)
        self.assertEqual(len({r['name'] for r in rows}),182)
        self.assertEqual(sum(len(r['calls']) for r in rows),192)
        self.assertEqual([sum(r['byteOrder']==order for r in rows) for order in ['big','little']],[91,91])
        self.assertEqual(len(DIMENSIONS),11)
        for row in rows:
            self.assertEqual(row['instructionLimit'],1000000)
            self.assertEqual(row['stackAlignment'],16)
            self.assertEqual(row['callerStackBytes'],0)
            self.assertEqual(len(row['preservedRegisters']),9)
            for call in row['calls']:
                self.assertEqual({r['at']['label']:r['length'] for r in call['callerAccess']['objects']},OBJECTS)
                self.assertEqual(call['callerAccess']['maxAccesses'],100000)
        reused=[r for r in rows if r['name'].startswith('repeated calls ')]
        self.assertEqual(len(reused),2)
        for row in reused:
            self.assertEqual(len(row['calls']),6)
            self.assertEqual([int(c['expectedRegisters']['v1'],16) for c in row['calls']][:4],[255,0,1,1])
            self.assertEqual(expanded(row['calls'][1]['memory'][0]),bytes(32))

    def test_selected_words_produce_exact_minimal_exclusive_rectangle(self):
        accepted=0
        for row in cases():
            for call in row['calls']:
                setup={label:bytearray([165])*size for label,size in OBJECTS.items()}
                occupied={label:set() for label in OBJECTS}
                for region in call['setupMemory']:
                    raw=expanded(region);label=region['label'];start=region['offset'];span=set(range(start,start+len(raw)))
                    self.assertFalse(occupied[label]&span)
                    occupied[label]|=span;setup[label][start:start+len(raw)]=raw
                for label in ['bounds_previous','bounds_current','bounds_settings']:
                    self.assertEqual(occupied[label],set(range(OBJECTS[label])))
                if not call['memory']:continue
                accepted+=1
                order=row['byteOrder'];settings=setup['bounds_settings'];width=int.from_bytes(settings[:8],order);height=int.from_bytes(settings[8:16],order)
                a,b=(call['argumentAddresses'][k] for k in ['a0','a1'])
                left=setup[a['label']];right=setup[b['label']];points=[]
                for y in range(height):
                    for x in range(width):
                        at=4*(y*width+x)
                        if left[a['offset']+at:a['offset']+at+4]!=right[b['offset']+at:b['offset']+at+4]:points.append((x,y))
                expected=[0,0,0,0] if not points else [min(x for x,y in points),min(y for x,y in points),max(x for x,y in points)+1,max(y for x,y in points)+1]
                result=expanded(call['memory'][0]);observed=[int.from_bytes(result[i:i+8],order) for i in range(0,32,8)]
                self.assertEqual(observed,expected,row['name'])
                self.assertEqual(int(call['expectedRegisters']['v1'],16),len(points),row['name'])
        self.assertEqual(accepted,168)
        self.assertEqual(rectangle(changed_indices(64,64,6),64),[5,5,7,7])
        self.assertEqual(rectangle({1,4},2),[0,0,2,3])
        self.assertEqual(rectangle({16,17},17),[0,0,17,2])

    def test_alignment_full_width_and_exact_access_contracts(self):
        for order in [False,True]:
            bad=invocation(0x100000001,1,order)
            self.assertEqual(bad['memory'],[])
            self.assertEqual(bad['callerAccess']['writable'],[])
            self.assertEqual([r['at']['label'] for r in bad['callerAccess']['readable']],['bounds_settings'])
            misaligned=invocation(3,5,order,4,bad='settings')
            self.assertEqual(misaligned['argumentAddresses']['a3']['offset'],4)
            self.assertEqual(misaligned['callerAccess']['readable'],[])
            self.assertEqual(misaligned['callerAccess']['writable'],[])
            for field,register,offset in [('left','a0',9),('right','a1',9),('output','a2',12)]:
                call=invocation(3,5,order,4,bad=field)
                self.assertEqual(call['argumentAddresses'][register]['offset'],offset)
                self.assertEqual(int(call['expectedRegisters']['v0'],16),1)
                self.assertEqual(len(call['callerAccess']['readable']),1)
            alias=invocation(17,15,order,4,alias=True)
            self.assertEqual(alias['argumentAddresses']['a0'],alias['argumentAddresses']['a1'])
            self.assertEqual(int(alias['expectedRegisters']['v1'],16),0)
            call=invocation(1,1,order,4)
            self.assertEqual(int(call['expectedRegisters']['v1'],16),1) # Difference includes the high bit.
            self.assertEqual(call['callerAccess']['writable'][0]['length'],32)

    def test_profiles_are_bounded_complete_and_deterministic_and_bind_original(self):
        result=profiles()
        self.assertEqual([len(p['cases']) for p in result.values()],[22]*7+[28])
        self.assertEqual({c['name'] for p in result.values() for c in p['cases']},{c['name'] for c in cases()})
        for profile in result.values():
            self.assertEqual(profile['formatVersion'],21)
            self.assertLessEqual(len(json.dumps(profile,separators=(',',':')).encode())+1,1048576)
            self.assertLessEqual(sum(c['instructionLimit'] for c in profile['cases']),40000000)
        root=Path(__file__).resolve().parents[1];binding=json.loads(Path(__file__).with_name('video_bounds_original_check.json').read_text())
        self.assertEqual(hashlib.sha256((root/binding['source']).read_bytes()).hexdigest(),binding['sha256'])
        with tempfile.TemporaryDirectory() as directory:
            self.assertEqual(build(directory),build(directory,True))
            p=Path(directory)/'aliases.json';p.write_text('{}')
            with self.assertRaisesRegex(ValueError,'Stale video bounds profile'):build(directory,True)


if __name__=='__main__':unittest.main()
