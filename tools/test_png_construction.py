"""Full-width recipe controls and preservation of student instruction text."""
import importlib.util,json
from pathlib import Path
import subprocess,sys,tempfile,unittest
import book


def module(name,path):
    spec=importlib.util.spec_from_file_location(name,path);m=importlib.util.module_from_spec(spec);spec.loader.exec_module(m);return m

class PngConstructionTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.root=book.ROOT/'drafts/png-construction';cls.prepare=module('prepare_png_tests',cls.root/'prepare.py')
        cls.fixtures=module('png_loader_fixture_tests',cls.root/'fixtures.py')
        cls.source=(cls.root/'main.asm').read_text();cls.inputs=json.loads((cls.root/'inputs.json').read_text())['recipes']

    def test_each_recipe_preserves_arbitrary_student_instructions_and_has_exact_controls(self):
        source=self.source.replace('png_load:\n','png_load:\n    # retain this student edit\n')
        for recipe in self.inputs:
            result=self.prepare.prepare(source,recipe)
            self.assertEqual(source.split('\n.text\n')[1],result.split('\n.text\n')[1])
            cap=int(recipe['capacity']);self.assertIn('file_capacity: .dword '+str(cap if cap<2**63 else cap-2**64),result)
            self.assertEqual(1,result.count('image_path_end:'))
        recipe=dict(self.inputs[0],capacity=str(2**64-1),mode=str(2**32-1))
        result=self.prepare.prepare(source,recipe)
        self.assertIn('file_capacity: .dword -1',result);self.assertIn('vector_mode: .word -1',result)

    def test_invalid_controls_and_broken_arena_boundaries_fail_without_partial_preparation(self):
        recipe=self.inputs[0]
        for value in ('-1','01','1.0',str(2**64),1):
            with self.assertRaises(ValueError):self.prepare.prepare(self.source,dict(recipe,capacity=value))
        for path in ('','bad\x00path','x'*128,'line\nname','é.png'):
            with self.assertRaises(ValueError):self.prepare.prepare(self.source,dict(recipe,path=path))
        for source in (self.source.replace('image_path_end:','other:'),self.source+'\n.text\n',self.source.replace('file_capacity: .dword','file_capacity: .word')):
            with self.assertRaises(ValueError):self.prepare.prepare(source,recipe)

    def test_fixture_boundaries_are_the_published_worked_file(self):
        worked=self.fixtures.material('worked');partial=self.fixtures.material('partial')
        self.assertEqual(115,len(worked));self.assertEqual(worked[:17],partial)
        self.assertEqual(b'',self.fixtures.material('empty'));self.assertEqual(worked+b'X',self.fixtures.material('long'))
        with self.assertRaises(ValueError):self.fixtures.material('unknown')

    def test_command_helpers_refuse_to_replace_existing_outputs(self):
        with tempfile.TemporaryDirectory() as directory:
            path=Path(directory)/'observe.asm';command=[sys.executable,str(self.root/'prepare.py'),'s01-wide','--output',str(path)]
            result=subprocess.run(command,capture_output=True,text=True);self.assertEqual(0,result.returncode,result.stderr)
            initial=path.read_bytes();self.assertIn(b'4294967296',initial)
            self.assertNotEqual(0,subprocess.run(command,capture_output=True).returncode);self.assertEqual(initial,path.read_bytes())
            path=Path(directory)/'image.png';command=[sys.executable,str(self.root/'fixtures.py'),'worked',str(path)]
            result=subprocess.run(command,capture_output=True,text=True);self.assertEqual(0,result.returncode,result.stderr)
            initial=path.read_bytes();self.assertNotEqual(0,subprocess.run(command,capture_output=True).returncode);self.assertEqual(initial,path.read_bytes())

    def test_step_region_and_recipe_links_are_complete_without_becoming_book_composition(self):
        steps=json.loads((self.root/'steps.json').read_text())['steps'];self.assertEqual(10,len(steps));self.assertEqual(14,len(self.inputs))
        regions=[q['region'] for q in steps if q['region']];self.assertEqual(9,len(regions))
        for region in regions:
            self.assertEqual(1,self.source.count('# BEGIN '+region+'\n'));self.assertEqual(1,self.source.count('# END '+region+'\n'))
        recipes={q['id']:q for q in self.inputs}
        for step in steps:
            self.assertEqual(3,len(step['quiz']['choices']));self.assertEqual(2,len(step['hints']))
            for rid in step['recipes']:self.assertEqual(step['id'],recipes[rid]['step'])
            self.assertTrue((book.ROOT/'includes/chapters/033-png'/('load-'+step['id']+'.tex')).is_file())
        self.assertNotIn('033-png',(book.ROOT/'main.tex').read_text())
