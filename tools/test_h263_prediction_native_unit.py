import hashlib
import json
from pathlib import Path
import runpy
import subprocess
import sys
import tempfile
import unittest
import zipfile
from native_lesson import bundle
from h263_reconstruct_lesson_cases import profiles

ROOT=Path(__file__).resolve().parents[1]
UNIT=ROOT/'revision/lessons/035-h263-prediction'


def chunk(text,start,end):return (start+text.split(start,1)[1]).split(end,1)[0]


class H263PredictionNativeUnitTest(unittest.TestCase):
    def test_every_original_reconstruction_case_is_staged_then_rechecked_on_the_same_starter(self):
        lesson=json.loads((UNIT/'lesson.json').read_text());stages=[s for s in lesson['steps'] if s['kind']=='assessment'];expected=profiles();paths=['independent/'+n for n in expected]
        self.assertEqual([s['id'] for s in stages],['check-samples','check-aliases','check-metadata','check-rejection','check-reuse','recheck-completed-source'])
        self.assertEqual([p for s in stages[:-1] for p in s['caseProfiles']],paths);self.assertEqual(stages[-1]['caseProfiles'],paths)
        self.assertEqual([sum(len(expected[p.split('/')[-1]]['cases']) for p in s['caseProfiles']) for s in stages],[60,24,36,16,2,138])
        self.assertTrue(all(s['source']=='independent/h263-reconstruct8.asm' for s in stages))
        for name,data in expected.items():self.assertEqual(json.loads((UNIT/'independent'/name).read_text()),data)
        starter=(UNIT/'independent/h263-reconstruct8.asm').read_bytes();self.assertEqual(starter,(ROOT/'revision/labs/files/h263-reconstruct8.asm').read_bytes())
        self.assertEqual(starter.split(b'\nh263_reconstruct8:',1)[1].count(b'    break'),1)
        for p in (UNIT/'worked').glob('*.asm'):self.assertNotIn('h263_reconstruct8:',p.read_text())

    def test_actual_numerical_helpers_and_transform_basis_are_preserved_without_clip_or_syntax_callers(self):
        original=(ROOT/'revision/labs/files/h263-encoder.asm').read_text()
        parts={'sad':('h263_sad16:\n','\nh263_predict_mb:'),'search':('h263_search16:\n','\nh263_sad16:'),'prediction':('h263_predict8:\n','\nh263_encode_block8:'),'coordinator':('h263_predict_mb:\n','\nh263_predict8:'),'block':('h263_encode_block8:\n','\nh263_transform8:'),'transform':('h263_transform8:\n','\nh263_quantize8:'),'quantizer':('h263_quantize8:\n','\nh263_emit_macroblock:')}
        expected={'sad-windows.asm':['sad'],'motion-ties.asm':['search','sad'],'diagonal-prediction.asm':['prediction'],'reconstruction-chain.asm':['block','transform','quantizer'],'negative-motion.asm':['coordinator','prediction']}
        for name,keys in expected.items():
            text=(UNIT/'worked'/name).read_text();self.assertEqual(text.count('\nmain:'),1);self.assertLess(len(text.encode()),30000)
            for key in keys:self.assertIn(chunk(original,*parts[key]),text)
            self.assertNotIn('h263_encode_clip:',text);self.assertNotIn('h263_emit_macroblock:',text)
        basis=chunk(original,'transform_B_h:','\n.data\n.align 3\nsyntax_mcbpc_codes:')
        self.assertIn(basis,(UNIT/'worked/reconstruction-chain.asm').read_text())
        self.assertEqual((UNIT/'tools/fixture.py').read_bytes(),(ROOT/'packages/035-h263/construction/h263-fixtures/fixture.py').read_bytes())

    def test_observations_distinguish_active_extents_rounding_representations_and_signed_boundaries(self):
        lesson=json.loads((UNIT/'lesson.json').read_text());self.assertEqual(len(lesson['steps']),25)
        questions=[s for s in lesson['steps'] if s['kind']=='prediction'];self.assertEqual(len(questions),12);self.assertEqual([s['correctChoice'] for s in questions],['abc'[i%3] for i in range(12)])
        debug={s['id']:s for s in lesson['steps'] if s['kind']=='debug'};self.assertEqual(len(debug),6);self.assertEqual(sum(len(s['stops']) for s in debug.values()),18)
        v=debug['observe-sad']['stops'][0]['vectorRegisters']['$w4'];self.assertEqual(v,'0x'+'1fe0'*8)
        self.assertEqual([int(s['registers']['$v1'],16) for s in debug['observe-motion']['stops']],[25,9,15])
        chain=debug['observe-chain']['stops'];self.assertEqual([int(chain[0]['registers'][k],16) for k in ['$s0','$s1']],[2,16])
        self.assertEqual([int(chain[1]['registers'][k],16) for k in ['$s0','$s1','$s2','$s3']],[1,23,3,131])
        self.assertEqual([int(chain[2]['registers'][k],16) for k in ['$s0','$s1','$s2','$s3']],[0,0,0,131])
        origin=debug['observe-coordinator']['stops'][0]['registers'];self.assertEqual(origin['$s1'],'0xffffffffffffffff');self.assertEqual(origin['$s2'],'0x0000000000000000')
        boundary=debug['observe-reconstruction']['stops'];self.assertEqual(int(boundary[-1]['registers']['$s0'],16),0x100000008)
        run=[s for s in lesson['steps'] if s['kind']=='run'];self.assertEqual(len(run),1);self.assertEqual(run[0]['source'],'worked/reconstruction-chain.asm');self.assertEqual(run[0]['stdout'],'')
        for src in (UNIT/'worked').glob('*.asm'):
            text=src.read_text();self.assertEqual(text.count('syscall'),1);self.assertNotIn('.include',text)

    def test_export_is_complete_deterministic_and_contains_only_the_unfinished_independent_body(self):
        manifest=json.loads((UNIT/'package.json').read_text());names={'package.json',manifest['entry'],manifest['assignment'],manifest['nativeLesson'],*manifest['supportingFiles']};actual={str(p.relative_to(UNIT)) for p in UNIT.rglob('*') if p.is_file()}
        self.assertEqual(names,actual);self.assertEqual(len(names),17)
        with tempfile.TemporaryDirectory() as directory:
            a=Path(directory)/'a.zip';b=Path(directory)/'b.zip';bundle(UNIT,a);bundle(UNIT,b);self.assertEqual(a.read_bytes(),b.read_bytes())
            with zipfile.ZipFile(a) as z:
                self.assertEqual(set(z.namelist()),{'035-h263-prediction/'+p for p in names})
                for name in names:self.assertEqual(z.read('035-h263-prediction/'+name),(UNIT/name).read_bytes())
                self.assertIn(b'# Replace this deliberate unfinished trap',z.read('035-h263-prediction/independent/h263-reconstruct8.asm'))

    def test_distributed_qcif_generator_records_exact_plane_bytes_hashes_and_course_limits(self):
        script=UNIT/'tools/fixture.py';module=runpy.run_path(str(script),run_name='h263_fixture_test')
        with tempfile.TemporaryDirectory() as directory:
            out=Path(directory)/'qcif';r=subprocess.run([sys.executable,str(script),'--output-dir',str(out)],capture_output=True,text=True);self.assertEqual(r.returncode,0,r.stderr)
            raw=(out/'clip.yuv').read_bytes();description=json.loads((out/'clip.json').read_text());self.assertEqual(len(raw),304128)
            self.assertEqual(raw,b''.join(module['frame'](i) for i in range(8)));self.assertEqual(description['sha256'],hashlib.sha256(raw).hexdigest())
            frame_bytes=module['FRAME_BYTES'];self.assertEqual(frame_bytes,38016);self.assertEqual(module['Y_BYTES'],25344);self.assertEqual(module['C_BYTES'],6336)
            self.assertEqual(raw[5*frame_bytes:6*frame_bytes],raw[6*frame_bytes:7*frame_bytes])
            rate=module['CLOCK']/module['DEFAULT_TICK_STEP'];self.assertLessEqual(rate,30);self.assertLessEqual(8/rate,10)
            before=hashlib.sha256(raw).hexdigest();r=subprocess.run([sys.executable,str(script),'--output-dir',str(out)],capture_output=True)
            self.assertNotEqual(r.returncode,0);self.assertEqual(hashlib.sha256((out/'clip.yuv').read_bytes()).hexdigest(),before)


if __name__=='__main__':unittest.main()
