from pathlib import Path
import json
import tempfile
import unittest

from console_cases import parse, render
import clipped_lesson_cases as clipped


class ClippedLessonCasesTest(unittest.TestCase):
    def test_interval_intersection_agrees_with_selected_source_cells_including_extreme_coordinates(self):
        for frame_width in range(9):
            for source_width in range(9):
                for x in [*range(-10,11),-(1<<31),(1<<31)-1]:
                    frame=clipped.Shape(frame_width,1,16);source=clipped.Shape(source_width,1,16)
                    left,top,right,bottom=clipped.intersection(frame,source,x,0)
                    self.assertEqual([x+i for i in range(source_width) if 0<=x+i<frame_width],list(range(left,right)))
        self.assertEqual((0,1,3,5),clipped.intersection(clipped.Shape(8,6,16),clipped.Shape(5,4,7),-2,1))
        self.assertEqual((6,0,8,2),clipped.intersection(clipped.Shape(8,6,16),clipped.Shape(5,4,7),6,-2))

    def test_original_trial_grid_retains_offsets_word_patterns_results_and_complete_access_windows(self):
        rows=clipped.trials();self.assertEqual(581,len(rows));empty=0
        for index,trial in enumerate(rows):
            case=clipped.kernel_case(index,trial);call=case['calls'][0]
            initial={r['label']:r for r in case['initialMemory']}
            frame='frame_b' if index%3==0 else 'frame_a';source='sprite_b' if index%4==0 else 'sprite_a'
            def values(label):
                data=bytes.fromhex(initial[label]['bytes']);return [int.from_bytes(data[i:i+4],'little') for i in range(0,len(data),4)]
            source_words=values(source);result=values(frame);copied=visible=0;written=[];frame_offset=index%8;source_offset=index*3%8
            for sy in range(trial.sprite.height):
                for sx in range(trial.sprite.width):
                    dx,dy=trial.x+sx,trial.y+sy
                    if 0<=dx<trial.frame.width and 0<=dy<trial.frame.height:
                        visible+=1;value=source_words[source_offset+sy*trial.sprite.stride+sx]
                        if value!=trial.key&0xffffffff:
                            copied+=1;target=frame_offset+dy*trial.frame.stride+dx;result[target]=value;written.append(target)
            self.assertEqual({'v0':clipped.bits(copied),'v1':clipped.bits(visible)},call['expectedRegisters'])
            self.assertEqual(clipped.words(frame,result),next(r for r in call['memory'] if r['label']==frame))
            self.assertEqual(clipped.byte_windows(frame,written),call['accessPolicies'][0]['writable'])
            self.assertEqual(case['exit']['accessPolicies'],call['accessPolicies'])
            self.assertEqual(call['addressMemory'],call['setupAddressMemory'])
            self.assertEqual(frame_offset*4,call['setupAddressMemory'][0]['value']['offset'])
            self.assertEqual(source_offset*4,call['setupAddressMemory'][1]['value']['offset'])
            self.assertEqual([*[f's{i}' for i in range(8)],'fp'],case['preservedRegisters'])
            self.assertEqual(50000,case['instructionLimit']);self.assertLessEqual(len(case['name']),128)
            empty+=not visible
        self.assertEqual(474,empty)

    def test_equal_words_still_count_and_full_word_keys_distinguish_visible_pixels(self):
        shape=clipped.Shape(2,1,4);source=[0x00ff00ff,0x01ff00ff,0,0];before=source.copy()
        output,copied,visible,written=clipped.copy_cells(shape,shape,0,0,0x00ff00ff,source,before)
        self.assertEqual((before,1,2,[1]),(output,copied,visible,written))
        self.assertEqual(5,sum(t.identical for t in clipped.trials()))
        for i,t in enumerate(clipped.trials()):
            if t.identical:
                case=clipped.kernel_case(i,t);self.assertGreater(int(case['calls'][0]['expectedRegisters']['v0'],16),0)
                self.assertEqual(next(r['bytes'] for r in case['initialMemory'] if r['label'].startswith('frame_') and not r['label'].startswith('frame_desc')),next(r['bytes'] for r in case['calls'][0]['memory'] if r['label'].startswith('frame_') and not r['label'].startswith('frame_desc')))

    def test_caller_checks_initialization_returned_registers_and_publication_in_order(self):
        cases=parse(json.dumps(clipped.caller_profile()).encode());self.assertEqual(10,len(cases))
        for index,case in enumerate(cases):
            publication=case.publication;self.assertEqual('blit_call',publication.label);self.assertEqual(2,len(publication.snapshots));self.assertIsNone(publication.snapshots[0].label);self.assertEqual('blit_ready',publication.snapshots[1].label)
            expected=[(8,12),(13,20),(2,4)][index] if index<3 else None
            if expected:self.assertEqual(tuple((r,clipped.bits(v)) for r,v in zip(['v0','v1'],expected)),publication.snapshots[1].expected_registers)
            self.assertEqual(1,len(publication.final_state.access_policies[0].writable))
            self.assertEqual('copied',publication.final_state.access_policies[0].writable[0].at.label)
        self.assertIn('expectedRegisters = mapOf',render(cases));self.assertIn('label = "blit_ready"',render(cases))

    def test_bounded_exports_retain_every_trial_once_and_regenerate_exactly(self):
        root=Path(__file__).resolve().parents[1]/'revision/labs/checks/clipped-native';clipped.build(root,True)
        with tempfile.TemporaryDirectory() as temporary:
            clipped.build(temporary)
            for file in root.iterdir():self.assertEqual(file.read_bytes(),(Path(temporary)/file.name).read_bytes())
        rows=[]
        for kind,count in [('empty',474),('overlap',107)]:
            selected=[r for p in sorted(root.glob(kind+'-*.cases.json')) for r in json.loads(p.read_text())['cases']]
            self.assertEqual(count,len(selected));self.assertTrue(all((int(r['calls'][0]['expectedRegisters']['v1'],16)==0)==(kind=='empty') for r in selected));rows+=selected
        self.assertEqual({r['name'] for r in rows},{clipped.kernel_case(i,t)['name'] for i,t in enumerate(clipped.trials())})
        for p in root.glob('*.cases.json'):self.assertLessEqual(p.stat().st_size,1024*1024)
        with tempfile.TemporaryDirectory() as temporary:
            with self.assertRaises(FileNotFoundError):clipped.build(temporary,True)


if __name__=='__main__':unittest.main()
