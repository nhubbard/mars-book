"""Transform inputs/tables remain portable and feedback never accepts partial output."""
import contextlib
import importlib.util
import io
import json
import math
from pathlib import Path
import re
import runpy
import subprocess
import sys
import tempfile
import unittest
from unittest.mock import patch
ROOT=Path(__file__).resolve().parents[1];STAGE=ROOT/'drafts/h263-transform-construction'
MODULES={}
for name in('prepare','matrix'):
    spec=importlib.util.spec_from_file_location('transform_'+name,STAGE/(name+'.py'))
    module=importlib.util.module_from_spec(spec);spec.loader.exec_module(module);MODULES[name]=module
PREPARE=MODULES['prepare'];MATRIX=MODULES['matrix']


class TransformDataTests(unittest.TestCase):
    def test_published_tables_have_signed_width_and_exact_transposes(self):
        source=(STAGE/'main.asm').read_text();scaled=MATRIX.SCALED;transposed=tuple(zip(*scaled))
        self.assertLessEqual(max(abs(v)for row in scaled for v in row),32767)
        for label,values,kind in [('transform_B_h',scaled,'half'),('transform_B_w',scaled,'word'),('transform_BT_h',transposed,'half'),('transform_BT_w',transposed,'word')]:
            data=source.split(label+':\n',1)[1].split('\n.',1)[0]
            actual=[int(v)for line in data.splitlines()for v in line.strip().removeprefix('.'+kind+' ').split(',')]
            self.assertEqual([v for row in values for v in row],actual)

    def test_floating_reference_normalization_and_orthogonality(self):
        for a in range(8):
            for b in range(8):
                dot=sum(MATRIX.BASIS[a][x]*MATRIX.BASIS[b][x]for x in range(8))
                self.assertAlmostEqual(1 if a==b else 0,dot,places=12)
        self.assertEqual([1024]+[0]*63,MATRIX.reference([128]*64))
        self.assertEqual([128]*64,MATRIX.reference([1024]+[0]*63,True))
        self.assertEqual([0]*64,MATRIX.reference([0]*64,True))
        with self.assertRaises(ValueError):MATRIX.reference([0]*63)

    def test_preparation_preserves_all_instructions_and_tables(self):
        source=(STAGE/'main.asm').read_text()+'\nstudent_helper:\n    daddiu $t0, $t0, 27\n'
        for mode in(0,1):
            for direction in(0,1):
                for pattern in('flat','worked'):
                    result=PREPARE.prepare(source,mode,direction,pattern)
                    self.assertEqual(source.split('\n.text\n')[1],result.split('\n.text\n')[1])
                    before=source.split('transform_B_h:',1)[1].split('\n.text\n')[0]
                    after=result.split('transform_B_h:',1)[1].split('\n.text\n')[0]
                    self.assertEqual(before,after)
                    self.assertIn('transform_request: .dword 0, 16, '+str(direction)+', '+str(mode)+', 0, 0',result)
                    body=result.split('transform_input:\n',1)[1].split('transform_input_end:',1)[0]
                    values=[int(v)for line in body.splitlines()for v in line.strip().removeprefix('.half ').split(',')]
                    self.assertEqual(64,len(values));self.assertEqual(PREPARE.samples(direction,pattern),values)
                    expected={(0,'flat'):1024,(0,'worked'):-192,(1,'flat'):128,(1,'worked'):137}
                    self.assertEqual(expected[direction,pattern],MATRIX.reference(values,bool(direction))[0])

    def test_changed_controls_or_boundaries_are_rejected(self):
        source=(STAGE/'main.asm').read_text()
        for bad in(-1,2,True,'0'):
            with self.assertRaises(ValueError):PREPARE.prepare(source,bad)
            with self.assertRaises(ValueError):PREPARE.prepare(source,0,bad)
        with self.assertRaises(ValueError):PREPARE.prepare(source,0,0,'unrecognized')
        for broken in[source+'\n.text\n',source.replace('transform_input_end:','other:'),source.replace('transform_input:','other:'),source.replace('.dword 0, 16, 0, 1, 0, 0','.dword 0, 18, 0, 1, 0, 0'),source.replace('.dword 0, 16, 0, 1, 0, 0','.dword 0, 16, 0, 1, 0, 1')]:
            with self.assertRaises(ValueError):PREPARE.prepare(broken,0)

    def test_cli_never_overwrites_a_working_file(self):
        with tempfile.TemporaryDirectory()as temporary:
            output=Path(temporary)/'worked.asm';args=[sys.executable,str(STAGE/'prepare.py'),'--mode','1','--direction','1','--pattern','worked','--output',str(output)]
            result=subprocess.run(args,capture_output=True,text=True);self.assertEqual(0,result.returncode,result.stderr)
            before=output.read_bytes();self.assertNotEqual(0,subprocess.run(args,capture_output=True).returncode);self.assertEqual(before,output.read_bytes())


class TransformFeedbackRunnerTests(unittest.TestCase):
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
            result = subprocess.CompletedProcess([], 0, 'PASS case\n' * 96, '')
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
            for index, result in enumerate([subprocess.CompletedProcess([], 0, 'PASS case\n' * 95, ''),
                    subprocess.CompletedProcess([], 0, 'PASS case\n' * 96 + 'FAIL extra\n', ''),
                    subprocess.CompletedProcess([], 1, 'PASS case\n' * 96, 'error')]):
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
