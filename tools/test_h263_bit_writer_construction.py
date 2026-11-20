"""MSB-first field order, unchanged bit windows and safe observation preparation."""
import contextlib
import importlib.util
import io
import json
from pathlib import Path
import runpy
import subprocess
import sys
import tempfile
import unittest
from unittest.mock import patch
ROOT=Path(__file__).resolve().parents[1];STAGE=ROOT/'drafts/h263-bit-writer-construction'
MODULES={}
for name in('model','prepare'):
    spec=importlib.util.spec_from_file_location('writer_'+name,STAGE/(name+'.py'));m=importlib.util.module_from_spec(spec);spec.loader.exec_module(m);MODULES[name]=m
MODEL=MODULES['model'];PREP=MODULES['prepare']

class WriterModelTests(unittest.TestCase):
    def test_every_width_offset_and_field_pattern(self):
        for width in range(33):
            for offset in range(8):
                for value in (0,(1<<width)-1,0x89abcdef&((1<<width)-1)):
                    before=bytes([0xa5,0x3c,0xf0,0x0f,0x96,0x69])
                    out,used=MODEL.append(before,offset,value,width)
                    original=''.join(f'{b:08b}'for b in before)
                    expected=original[:offset]+(f'{value:0{width}b}'if width else '')+original[offset+width:]
                    self.assertEqual(expected,''.join(f'{b:08b}'for b in out));self.assertEqual(offset+width,used)
                    self.assertEqual(bytes([0xa5,0x3c,0xf0,0x0f,0x96,0x69]),before)

    def test_default_crossing_and_explicit_zero_alignment(self):
        a,p=MODEL.append(bytes([165]*8),0,5,3);b,p=MODEL.append(a,p,19,5)
        self.assertEqual(bytes([0xb3]+[165]*7),b);self.assertEqual(8,p)
        a,p=MODEL.append(bytes([165]*8),0,5,3);b,p=MODEL.append(a,p,419,9)
        self.assertEqual(bytes([0xba,0x35]+[165]*6),b);self.assertEqual(12,p)
        c,p=MODEL.append(b,p,0,4);self.assertEqual(bytes([0xba,0x30]+[165]*6),c);self.assertEqual(16,p)

    def test_empty_exact_fit_failure_and_full_width_validation(self):
        self.assertEqual((b'',0),MODEL.append(b'',0,0,0))
        self.assertEqual((b'\xa5',3),MODEL.append(b'\xa5',3,0,0))
        self.assertEqual((b'\xa4',8),MODEL.append(b'\xa5',7,0,1))
        with self.assertRaises(BufferError):MODEL.append(b'\xa5',7,3,2)
        for used,value,width in[(-1,0,1),(9,0,0),(0,1,0),(0,256,8),(0,-1,32),(0,1<<32,32),(0,0,33),(0,0,True),(False,0,0)]:
            with self.assertRaises(ValueError):MODEL.append(b'\xa5',used,value,width)

    def test_preparation_keeps_all_student_instructions(self):
        source=(STAGE/'main.asm').read_text()+'\nstudent_helper:\n    nop\n'
        for first,fw,second,sw,capacity in[(5,3,419,9,8),(0,0,0,0,0),(0xffffffff,32,1,1,4)]:
            result=PREP.prepare(source,first,fw,second,sw,capacity)
            self.assertEqual(source.split('\n.text\n')[1],result.split('\n.text\n')[1])
            self.assertIn(f'writer_first: .dword {first}, {fw}',result)
            self.assertIn(f'writer_second: .dword {second}, {sw}',result)
            self.assertIn(f'writer_context: .dword 0, {capacity}, 0',result)
        for args in[(1,0,0,0,8),(0,33,0,0,8),(0,0,-1,1,8),(0,0,0,0,9),(True,1,0,0,8)]:
            with self.assertRaises(ValueError):PREP.prepare(source,*args)
        for broken in[source+'\n.text\n',source.replace('writer_first:','another:'),source.replace('writer_context:','another:')]:
            with self.assertRaises(ValueError):PREP.prepare(broken)

    def test_preparation_refuses_to_overwrite_work(self):
        with tempfile.TemporaryDirectory()as tmp:
            out=Path(tmp)/'writer.asm';args=[sys.executable,str(STAGE/'prepare.py'),'--second','419','--second-width','9','--output',str(out)]
            r=subprocess.run(args,capture_output=True,text=True);self.assertEqual(0,r.returncode,r.stderr)
            before=out.read_bytes();self.assertNotEqual(0,subprocess.run(args,capture_output=True).returncode);self.assertEqual(before,out.read_bytes())


class WriterFeedbackRunnerTests(unittest.TestCase):
    def execute(self, source, cli, report, result):
        args = ['grade.py', str(source), '--cli', str(cli), '--java', 'test-java', '--report', str(report)]
        with patch.object(sys, 'argv', args), patch('subprocess.run', return_value=result) as run, \
                contextlib.redirect_stdout(io.StringIO()), contextlib.redirect_stderr(io.StringIO()), \
                self.assertRaises(SystemExit) as stop:
            runpy.run_path(str(STAGE / 'grade.py'), run_name='__main__')
        return stop.exception.code, run

    def test_complete_output_and_exclusive_report(self):
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            source = root / 'student.asm'; source.write_text('.text\n')
            cli = root / 'cli.jar'; cli.touch()
            report = root / 'report.json'
            result = subprocess.CompletedProcess([], 0, 'PASS case\n' * 160, '')
            code, run = self.execute(source, cli, report, result)
            self.assertEqual(0, code)
            self.assertTrue(json.loads(report.read_text())['passed'])
            self.assertEqual(['test-java', '-jar', str(cli), 'grade', str(STAGE / 'check.kts'),
                              str(source), str(STAGE / 'harness.asm')], run.call_args.args[0])
            before = report.read_bytes()
            code, run = self.execute(source, cli, report, result)
            self.assertEqual(2, code); run.assert_not_called()
            self.assertEqual(before, report.read_bytes())

    def test_partial_output_failure_or_failed_process_never_passes(self):
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            source = root / 'student.asm'; source.touch()
            cli = root / 'cli.jar'; cli.touch()
            for index, result in enumerate([subprocess.CompletedProcess([], 0, 'PASS case\n' * 159, ''),
                    subprocess.CompletedProcess([], 0, 'PASS case\n' * 160 + 'FAIL extra\n', ''),
                    subprocess.CompletedProcess([], 1, 'PASS case\n' * 160, 'error')]):
                report = root / (str(index) + '.json')
                code, _ = self.execute(source, cli, report, result)
                self.assertEqual(1, code)
                self.assertFalse(json.loads(report.read_text())['passed'])

    def test_missing_inputs_stop_before_process_creation(self):
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            code, run = self.execute(root / 'missing.asm', root / 'missing.jar', root / 'report.json', None)
            self.assertEqual(2, code); run.assert_not_called()
            self.assertFalse((root / 'report.json').exists())


if __name__ == '__main__':
    unittest.main()
