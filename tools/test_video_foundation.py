"""Public video inputs, controls and native lessons remain reproducible student material."""
import importlib.util
import json
from pathlib import Path
import re
import struct
import subprocess
import sys
import tempfile
import unittest
import zlib
ROOT=Path(__file__).resolve().parents[1];D=ROOT/'drafts/video-construction'
def load(name,path):
 s=importlib.util.spec_from_file_location(name,path);m=importlib.util.module_from_spec(s);s.loader.exec_module(m);return m
FIX=load('video_fixture_tests',ROOT/'drafts/video-fixtures/fixture.py');PREP=load('video_controls_tests',D/'prepare.py')
class VideoFoundationTests(unittest.TestCase):
 def test_known_file_counts_and_record_extents(self):
  counts={'moving':24795,'still':12388,'all-change':24636,'extra-full':37066}
  for name in FIX.NAMES:
   m=FIX.material(name);data=m['file'];self.assertEqual(counts[name],len(data));self.assertEqual((b'MLV1',64,64,len(m['frames']),4,0,40,0),struct.unpack_from('>4sHHIHHII',data));at=24
   for n,item in enumerate(m['directory']):
    self.assertEqual(at,item['offset']);kind,sequence,length,crc=struct.unpack_from('>B3xIII',data,at);self.assertEqual(n,sequence);self.assertEqual(item['kind'],kind);self.assertEqual(item['payload_bytes'],length);payload=data[at+16:at+16+length];self.assertEqual(zlib.crc32(data[at:at+12]+payload),crc)
    if n%4==0:self.assertEqual(0,kind)
    if kind==0:self.assertEqual(12288,length)
    at+=16+length
   self.assertEqual(len(data),at)
 def test_worked_commands_and_same_animation_alternative(self):
  moving=FIX.material('moving');extra=FIX.material('extra-full');self.assertEqual(moving['frames'],extra['frames']);self.assertNotEqual(moving['file'],extra['file']);self.assertEqual(0,extra['directory'][5]['kind'])
  at=moving['directory'][1]['offset'];self.assertEqual('000000010100000fff0000',moving['file'][at+16:at+27].hex());at=moving['directory'][2]['offset'];self.assertEqual(24,moving['directory'][2]['payload_bytes']);self.assertEqual('01450002102040102040003e00021020401020400e790000',moving['file'][at+16:at+40].hex())
 def test_all_control_recipes_retain_exact_instructions(self):
  src=(D/'kernel.asm').read_text();code=src.split('\n.text\n')[1];recipes=json.loads((D/'inputs.json').read_text())['recipes'];self.assertEqual(26,len(recipes))
  for r in recipes:
   text=PREP.prepare(src,r);self.assertEqual(code,text.split('\n.text\n')[1]);self.assertIn(r['checkpoint']+':',text);self.assertIn(f"kernel_count: .dword {r['count']}",text)
  changed=src+'\nstudent_extra:\n    daddiu $t0, $t0, 99\n';self.assertIn('student_extra:',PREP.prepare(changed,recipes[0]))
 def test_bad_controls_and_altered_arenas_are_refused(self):
  src=(D/'kernel.asm').read_text();base=dict(count=5,mode=0,alias=0,pattern='worked')
  for updates in ({'count':-1},{'count':4097},{'count':True},{'mode':True},{'mode':2},{'alias':3},{'pattern':'unknown'},{'count':4}):
   with self.subTest(updates=updates),self.assertRaises(ValueError):PREP.prepare(src,base|updates)
  with self.assertRaises(ValueError):PREP.prepare(src.replace('left_words:','changed:'),base)
 def test_native_steps_match_quiz_metadata_without_duplicate_blocks(self):
  steps=json.loads((D/'steps.json').read_text())['steps'];self.assertEqual(6,len(steps));regions=re.findall(r'^# BEGIN (\w+)$',(D/'kernel.asm').read_text(),re.M);self.assertEqual(set(regions),{s['region']for s in steps})
  for s in steps:
   text=(ROOT/f"includes/chapters/034-lossless-video/kernel-{s['id']}.tex").read_text();self.assertEqual(1,text.count(r'\paragraph{Observe}'));self.assertEqual(1,text.count(r'\paragraph{Check}'));self.assertIn(s['recipe'],text);self.assertIn(s['quiz']['question'],text);self.assertEqual(2,len(s['hints']));self.assertNotIn('answer',s['quiz'])
   for choice in s['quiz']['choices']:self.assertIn(choice['text'],text)
 def test_fixture_cli_writes_complete_fresh_bundle(self):
  with tempfile.TemporaryDirectory()as directory:
   folder=Path(directory)/'fixture';r=subprocess.run([sys.executable,str(ROOT/'drafts/video-fixtures/fixture.py'),'moving','--output',str(folder)],capture_output=True,text=True);self.assertEqual(0,r.returncode,r.stderr);m=FIX.material('moving');self.assertEqual(m['file'],(folder/'animation.mlv').read_bytes());self.assertEqual(8,len(list(folder.glob('*.ppm'))));self.assertEqual(b'P6\n64 64\n255\n'+m['frames'][0],(folder/'frame-000.ppm').read_bytes())
   marker=folder/'keep';marker.write_text('student work');r=subprocess.run([sys.executable,str(ROOT/'drafts/video-fixtures/fixture.py'),'still','--output',str(folder)],capture_output=True,text=True);self.assertNotEqual(0,r.returncode);self.assertEqual('student work',marker.read_text())
 def test_prepare_refuses_existing_output(self):
  with tempfile.TemporaryDirectory()as directory:
   out=Path(directory)/'existing';out.write_text('keep');r=subprocess.run([sys.executable,str(D/'prepare.py'),'s01-1','--output',str(out)],capture_output=True,text=True);self.assertNotEqual(0,r.returncode);self.assertEqual('keep',out.read_text())
 def test_native_draft_has_no_unexported_highlighted_listing(self):
  book=load('video_book_tests',ROOT/'tools/book.py');self.assertEqual([],book.validate('drafts/lossless-video.tex'))
if __name__=='__main__':unittest.main()
