"""Preserve student edits and select exact, independently inspectable PNG inputs."""
import importlib.util,json,struct,subprocess,sys,tempfile,unittest,zlib
from pathlib import Path
import book

def load(name,path):
 s=importlib.util.spec_from_file_location(name,path);m=importlib.util.module_from_spec(s);s.loader.exec_module(m);return m

class CollectorConstructionTests(unittest.TestCase):
 @classmethod
 def setUpClass(cls):
  cls.root=book.ROOT/'drafts/png-collector-construction';cls.attach=load('test_collector_attach',cls.root/'attach.py');cls.prep=load('test_collector_prepare',cls.root/'prepare.py');cls.fixture=load('test_collector_fixture',cls.root/'fixtures.py');cls.source=(book.ROOT/'drafts/png-construction/main.asm').read_text();cls.recipes=json.loads((cls.root/'inputs.json').read_text())['recipes']
 def test_attachment_retains_loader_edits_and_extra_functions(self):
  original=self.source.replace('png_load:\n','png_load:\n    # my current implementation\n')+'\nextra_student_function:\n    jr $ra\n    nop\n';result=self.attach.attach(original)
  self.assertEqual(original.split('png_load:\n',1)[1].split('png_decode:\n',1)[0],result.split('png_load:\n',1)[1].split('# Temporary decoder:',1)[0]);self.assertTrue(result.endswith('extra_student_function:\n    jr $ra\n    nop\n'));self.assertIn('collector_capacity: .dword 46',result);self.assertEqual(17,result.count('# BEGIN collect_'))
 def test_attachment_rejects_changed_decoder_tail_and_existing_scaffold(self):
  for source in (self.source.replace('png_decode:\n','changed_decode:\n'),self.source+'\n.text\n',self.attach.attach(self.source),self.source+'\ncollector_capacity: .dword 1\n'):
   with self.assertRaises(ValueError):self.attach.attach(source)
 def test_all_recipe_controls_preserve_student_instructions(self):
  source=self.attach.attach(self.source).replace('png_collect:\n','png_collect:\n    # my collector edit\n')
  for recipe in self.recipes:
   result=self.prep.prepare(source,recipe);self.assertEqual(source.split('\n.text\n')[1],result.split('\n.text\n')[1]);self.assertIn('collector_capacity: .dword '+recipe['collectorCapacity'],result)
  for value in ('-1','01',str(2**64)):
   with self.assertRaises(ValueError):self.prep.prepare(source,dict(self.recipes[0],collectorCapacity=value))
  with self.assertRaises(ValueError):self.prep.prepare(source.replace('collector_capacity:','other_capacity:'),self.recipes[0])
 def test_fixture_envelopes_and_specific_faults(self):
  worked=self.fixture.material('worked');self.assertEqual(115,len(worked));self.assertEqual(worked[:-1],self.fixture.material('bad-crc')[:-1]);self.assertEqual(2**31,struct.unpack('>I',self.fixture.material('huge-length')[8:12])[0])
  for name in ('worked','palette','metadata','idat-gap','critical','reserved','bad-width'):
   raw=self.fixture.material(name);at=8;types=[]
   while at<len(raw):
    count=struct.unpack_from('>I',raw,at)[0];kind=raw[at+4:at+8];body=raw[at+8:at+8+count];crc=struct.unpack_from('>I',raw,at+8+count)[0];self.assertEqual(zlib.crc32(kind+body),crc);types.append(kind);at+=count+12
   self.assertEqual(len(raw),at)
   if name=='idat-gap':self.assertEqual([b'IHDR',b'IDAT',b'tEXt',b'IDAT',b'IEND'],types)
   if name=='bad-width':self.assertEqual(bytes(4),raw[16:20])
  with self.assertRaises(ValueError):self.fixture.material('unknown')
 def test_native_sequence_indexes_every_edit_once(self):
  steps=json.loads((self.root/'steps.json').read_text())['steps'];self.assertEqual(list(range(11,28)),[int(x['id'][1:])for x in steps]);self.assertEqual(17,len(set(x['region']for x in steps)))
  for prefix,step in enumerate(steps,1):
   self.assertTrue((book.ROOT/'includes/chapters/033-png'/('collect-'+step['id']+'.tex')).is_file());recipe=next(x for x in self.recipes if x['id']==step['recipes'][0]);self.assertEqual(prefix,recipe['prefix']);self.assertEqual(3,len(step['quiz']['choices']));self.assertEqual(2,len(step['hints']))
  positions={x['region']:n for n,x in enumerate(steps)};self.assertLess(positions['collect_next'],positions['collect_plte']);self.assertLess(positions['collect_be32'],positions['collect_signature']);self.assertLess(positions['collect_crc'],positions['collect_crc_check'])
 def test_helpers_refuse_to_replace_existing_outputs(self):
  with tempfile.TemporaryDirectory()as directory:
   folder=Path(directory);source=folder/'student.asm';source.write_text(self.source);out=folder/'next.asm';command=[sys.executable,str(self.root/'attach.py'),'--source',str(source),'--output',str(out)];self.assertEqual(0,subprocess.run(command,capture_output=True).returncode);before=out.read_bytes();self.assertNotEqual(0,subprocess.run(command,capture_output=True).returncode);self.assertEqual(before,out.read_bytes());png=folder/'worked.png';command=[sys.executable,str(self.root/'fixtures.py'),'worked',str(png)];self.assertEqual(0,subprocess.run(command,capture_output=True).returncode);before=png.read_bytes();self.assertNotEqual(0,subprocess.run(command,capture_output=True).returncode);self.assertEqual(before,png.read_bytes())
