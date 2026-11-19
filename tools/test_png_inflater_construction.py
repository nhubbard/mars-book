"""Student edit preservation and reproducible stored-stage bytes."""
import importlib.util,json,struct,subprocess,sys,tempfile,unittest,zlib
from pathlib import Path
import book

def load(name,path):
 s=importlib.util.spec_from_file_location(name,path);m=importlib.util.module_from_spec(s);s.loader.exec_module(m);return m

class StoredConstructionTests(unittest.TestCase):
 @classmethod
 def setUpClass(cls):
  cls.root=book.ROOT/'drafts/png-inflater-construction';cls.attach=load('stored_attach',cls.root/'attach.py');cls.prep=load('stored_controls',cls.root/'prepare.py');cls.fixtures=load('stored_fixtures',cls.root/'fixtures.py');previous=load('previous_attach',book.ROOT/'drafts/png-collector-construction/attach.py');cls.previous=previous.attach((book.ROOT/'drafts/png-construction/main.asm').read_text());cls.recipes=json.loads((cls.root/'inputs.json').read_text())['recipes']
 def test_attach_keeps_loader_collector_and_extra_instruction_edits(self):
  source=self.previous.replace('png_load:\n','png_load:\n    # own loader\n').replace('png_collect:\n','png_collect:\n    # own collector\n')+'\nextra_student_function:\n    jr $ra\n    nop\n';result=self.attach.attach(source);old=(book.ROOT/'drafts/png-collector-construction/wrapper.asm').read_text();before,after=source.split(old);replacement=(self.root/'wrapper.asm').read_text();self.assertTrue(result.startswith(before+replacement+after));self.assertEqual(1,result.count('# own loader'));self.assertEqual(1,result.count('# own collector'));self.assertIn('extra_student_function:',result);self.assertEqual(16,result.count('# BEGIN inflate_'))
 def test_attach_refuses_changed_wrappers_and_reapplication(self):
  for source in (self.previous.replace('collector_partial_exit:','changed_exit:'),self.previous+'\n.text\n',self.attach.attach(self.previous)):
   with self.assertRaises(ValueError):self.attach.attach(source)
 def test_all_recipes_keep_current_instruction_text(self):
  source=self.attach.attach(self.previous).replace('png_inflate:\n','png_inflate:\n    # preserve current work\n')
  self.assertEqual(23,len(self.recipes))
  for recipe in self.recipes:
   prepared=self.prep.prepare(source,recipe);self.assertEqual(source.split('\n.text\n')[1],prepared.split('\n.text\n')[1]);self.assertIn('collector_capacity: .dword 1048576',prepared)
  with self.assertRaises(ValueError):self.prep.prepare(self.previous,self.recipes[0])
 def test_valid_streams_and_exact_block_observations(self):
  for name in ('worked','two-blocks','fixed','dynamic'):
   m=self.fixtures.material(name);obj=zlib.decompressobj();decoded=obj.decompress(m['zlib'])+obj.flush();self.assertTrue(obj.eof);self.assertFalse(obj.unused_data);self.assertEqual(m['plain'],decoded)
  m=self.fixtures.material('worked');self.assertEqual(46,len(m['zlib']));self.assertEqual(35,len(m['plain']));self.assertEqual(bytes.fromhex('7801012300dcff'),m['zlib'][:7]);self.assertEqual(bytes.fromhex('2e0c023f'),m['zlib'][-4:]);two=self.fixtures.material('two-blocks')['zlib'];self.assertEqual(0,two[2]&1);self.assertEqual(1,two[14]&1)
  self.assertEqual(1,(self.fixtures.material('fixed')['zlib'][2]>>1)&3);self.assertEqual(2,(self.fixtures.material('dynamic')['zlib'][2]>>1)&3)
 def test_corrupted_zlib_is_still_inside_valid_png_chunks(self):
  for name in self.fixtures.NAMES:
   material=self.fixtures.material(name);raw=material['png'];at=8;joined=bytearray()
   while at<len(raw):
    n=struct.unpack_from('>I',raw,at)[0];kind=raw[at+4:at+8];data=raw[at+8:at+8+n];crc=struct.unpack_from('>I',raw,at+8+n)[0];self.assertEqual(zlib.crc32(kind+data),crc)
    if kind==b'IDAT':joined.extend(data)
    at+=n+12
   self.assertEqual(len(raw),at);self.assertEqual(material['zlib'],bytes(joined))
  for name in ('bad-header','bad-length','bad-adler'):
   with self.assertRaises(zlib.error):zlib.decompress(self.fixtures.material(name)['zlib'])
  m=self.fixtures.material('extra-body');self.assertEqual(47,len(m['zlib']))
  with self.assertRaises(ValueError):self.fixtures.material('unknown')
 def test_native_steps_and_controls_cover_each_edit(self):
  steps=json.loads((self.root/'steps.json').read_text())['steps'];self.assertEqual(list(range(28,44)),[int(s['id'][1:])for s in steps]);self.assertEqual(16,len(set(s['region']for s in steps)));scaffold=(self.root/'inflate.asm').read_text()
  for prefix,s in enumerate(steps,1):
   self.assertEqual(1,scaffold.count('# BEGIN '+s['region']+'\n'));r=next(r for r in self.recipes if r['id']==s['recipes'][0]);self.assertEqual(prefix,r['prefix']);self.assertEqual(3,len(s['quiz']['choices']));self.assertEqual(2,len(s['hints']));native=book.ROOT/'includes/chapters/033-png'/('inflate-'+s['id']+'.tex');self.assertTrue(native.is_file())
 def test_command_helpers_refuse_existing_outputs(self):
  with tempfile.TemporaryDirectory()as folder:
   d=Path(folder);source=d/'previous.asm';source.write_text(self.previous);target=d/'stage.asm';command=[sys.executable,str(self.root/'attach.py'),'--source',str(source),'--output',str(target)];self.assertEqual(0,subprocess.run(command,capture_output=True).returncode);before=target.read_bytes();self.assertNotEqual(0,subprocess.run(command,capture_output=True).returncode);self.assertEqual(before,target.read_bytes());target=d/'worked.zlib';command=[sys.executable,str(self.root/'fixtures.py'),'worked',str(target),'--kind','zlib'];self.assertEqual(0,subprocess.run(command,capture_output=True).returncode);before=target.read_bytes();self.assertNotEqual(0,subprocess.run(command,capture_output=True).returncode);self.assertEqual(before,target.read_bytes())
