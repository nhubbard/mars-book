"""Bind CBOR's guide to its complete worked code and original sequence contracts."""
import json
from pathlib import Path
import re
import unittest
import cbor_sequence_lesson_cases as c
ROOT=Path(__file__).resolve().parents[1]
UNIT=ROOT/'revision/lessons/032-cbor-telemetry'


def source_bytes(text):
    block=text.split('source:\n',1)[1].split('source_end:',1)[0];out=bytearray()
    for line in block.splitlines():
        if '.byte ' not in line:continue
        value=line.split('.byte ',1)[1]
        if ':' in value:
            byte,n=value.split(':');out.extend(bytes([int(byte)&255])*int(n))
        else:out.extend(int(v)&255 for v in value.split(','))
    return bytes(out)


class CborNativeUnitTest(unittest.TestCase):
    def test_every_original_and_supplemental_profile_has_one_assessment_owner(self):
        generated=c.build(UNIT/'independent',check=True);lesson=json.loads((UNIT/'lesson.json').read_text())
        self.assertEqual(lesson['formatVersion'],11)
        groups=[s for s in lesson['steps'] if s['kind']=='assessment'];refs=[path for s in groups for path in s['caseProfiles']]
        self.assertEqual(set(refs),{'independent/'+n for n in generated});self.assertEqual(len(refs),len(set(refs)))
        self.assertEqual([sum(len(json.loads((UNIT/path).read_text())['cases']) for path in s['caseProfiles']) for s in groups],[55,12,8,112,16,1,1])
        self.assertTrue(all(s['source']=='independent/cbor-sequence.asm' for s in groups))
        for name,folder in [('cbor-sequence.asm','files'),('cbor-sequence.kts','checks')]:
            self.assertEqual((UNIT/'independent'/name).read_bytes(),(ROOT/'revision/labs'/folder/name).read_bytes())
        self.assertIn('cbor_sequence_count:\n    break',(UNIT/'independent/cbor-sequence.asm').read_text())

    def test_complete_variants_keep_all_executable_code_and_only_select_the_documented_experiment(self):
        base=(ROOT/'revision/labs/files/cbor-telemetry.asm').read_text()
        self.assertEqual((UNIT/'cbor-telemetry.asm').read_text().split('\n',1)[1],base)
        variants={
          'head-truncated.asm':(0,c.TRACE,1,0,37),'counter.asm':(0,bytes.fromhex('1b0102030405060708'),9,0,37),
          'nested-item.asm':(1,bytes.fromhex('a201824280ff21008000'),10,0,37),
          'depth-seven.asm':(1,b'\x80',1,7,37),'depth-eight.asm':(1,b'\x80',1,8,37),'primitive-eight.asm':(1,b'\0',1,8,37),
          'duplicate-key.asm':(1,bytes.fromhex('a20000180000'),6,0,37),'nested-maps.asm':(1,bytes.fromhex('a100a10000'),5,0,37),
          'decode.asm':(2,c.TRACE,52,0,37),'late-counter.asm':(2,c.TRACE,51,0,37),
          'pipeline.asm':(5,c.TRACE,52,0,37),'pipeline-short.asm':(5,c.TRACE,52,0,36)}
        self.assertEqual({p.name for p in (UNIT/'worked').iterdir()},set(variants))
        for name,(mode,raw,count,depth,capacity) in variants.items():
            source=(UNIT/'worked'/name).read_text().split('\n',1)[1]
            with self.subTest(name=name):
                self.assertEqual(source.split('.text\n')[1],base.split('.text\n')[1])
                self.assertEqual(source_bytes(source),raw+bytes([165])*(1280-len(raw)))
                for field,expected in [('mode',mode),('byte_count',count),('inherited_depth',depth),('byte_capacity',capacity)]:
                    self.assertEqual(int(re.search(r'^'+field+r': \.\w+ (\d+)$',source,re.M).group(1)),expected)
                restored=source
                a=restored.index('source:\n');b=restored.index('source_end:',a);x=base.index('source:\n');y=base.index('source_end:',x)
                restored=restored[:a]+base[x:y]+restored[b:]
                expected=base.replace('mode: .word 0','mode: .word '+str(mode)).replace('byte_count: .dword 52','byte_count: .dword '+str(count)).replace('inherited_depth: .dword 0','inherited_depth: .dword '+str(depth)).replace('byte_capacity: .dword 37','byte_capacity: .dword '+str(capacity))
                self.assertEqual(restored,expected)

    def test_every_exported_file_and_activity_source_is_explicit_and_quizzes_vary_positions(self):
        m=json.loads((UNIT/'package.json').read_text());paths=['package.json',m['entry'],m['assignment'],m['nativeLesson'],*m['supportingFiles']]
        self.assertEqual(set(paths),{str(f.relative_to(UNIT)) for f in UNIT.rglob('*') if f.is_file()});self.assertEqual(len(paths),len(set(paths)))
        lesson=json.loads((UNIT/'lesson.json').read_text())
        self.assertTrue(all(s['source'] in paths for s in lesson['steps']))
        predictions=[s for s in lesson['steps'] if s['kind']=='prediction'];self.assertEqual(len(predictions),12)
        self.assertEqual({s['correctChoice'] for s in predictions},{'a','b','c'})
        self.assertTrue(all(len({v['id'] for v in s['choices']})==3 for s in predictions))

    def test_recursive_visits_preserve_parent_depth_and_per_map_identity(self):
        lesson=json.loads((UNIT/'lesson.json').read_text());steps={s['id']:s for s in lesson['steps']}
        returns=[s for s in steps['follow-parent-and-child']['stops'] if s['anchor']['label']=='after_k_child']
        self.assertEqual([int(s['registers']['$s6'],16) for s in returns],[4,4,5])
        self.assertEqual([int(s['registers']['$s4'],16) for s in returns],[1,0,1])
        self.assertEqual([int(s['registers']['$s2'],16) for s in returns],[2,2,1])
        self.assertEqual(int(returns[1]['registers']['$a2'],16),1)
        masks=steps['separate-map-masks']['stops'][:2]
        self.assertEqual([int(s['registers']['$s5'],16) for s in masks],[1,1])
        self.assertEqual([int(s['registers']['$s2'],16) for s in masks],[1,2])
        self.assertEqual([int(s['registers']['$v1'],16) for s in steps['capacity-before-output']['stops']],[52,37,0])
