"""Full-frame preparation preserves student code and keeps all six plane observations."""
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

ROOT=Path(__file__).resolve().parents[1]
STAGE=ROOT/'drafts/h263-macroblock-construction'
SPEC=importlib.util.spec_from_file_location('macroblock_preparation_tests',STAGE/'prepare.py')
PREPARE=importlib.util.module_from_spec(SPEC);SPEC.loader.exec_module(PREPARE)


class MacroblockPreparationTests(unittest.TestCase):
    def test_full_frames_all_six_block_observations_and_unchanged_instructions(self):
        source=(STAGE/'main.asm').read_text()+'\nstudent_helper:\n    daddiu $t0, $t0, 27\n'
        expected={-2:(2976,0,0,2976),-1:(5208,232,348,5788),0:(7872,480,720,9072),1:(7872,480,720,9072),2:(7872,480,720,9072)}
        for mode in(0,1):
            for delta in range(-2,3):
                result=PREPARE.prepare(source,mode,delta,delta)
                self.assertEqual(source.split('\n.text\n')[1],result.split('\n.text\n')[1])
                self.assertIn('.dword 16, 32, '+str(delta)+', '+str(delta)+', '+str(mode)+', 0',result)
                arrays=[]
                for label in('macro_current','macro_reference'):
                    body=result.split(label+':\n',1)[1].split(label+'_end:',1)[0]
                    raw=bytes(int(v)&255 for line in body.splitlines()for v in line.strip().removeprefix('.byte ').split(','))
                    self.assertEqual(38048,len(raw));self.assertEqual(bytes([165]*16),raw[:16]);self.assertEqual(bytes([165]*16),raw[-16:])
                    arrays.append(raw[16:-16])
                a,b=arrays;scores=[]
                for block in range(6):
                    if block<4:
                        plane=0;stride=176;cx=16+8*(block%2);cy=32+8*(block//2);rx=cx+delta;ry=cy+delta;phase=0
                    else:
                        plane=25344+(block-4)*6336;stride=88;cx=8;cy=16;rx=cx+delta//2;ry=cy+delta//2;phase=delta%2
                    score=0
                    for y in range(8):
                        for x in range(8):
                            values=[b[plane+(ry+y+v)*stride+rx+x+u]for v in range(phase+1)for u in range(phase+1)]
                            prediction=(sum(values)+len(values)//2)//len(values)
                            score+=abs(a[plane+(cy+y)*stride+cx+x]-prediction)
                    scores.append(score)
                self.assertEqual(expected[delta],(sum(scores[:4]),scores[4],scores[5],sum(scores)))

    def test_invalid_controls_and_changed_request_are_rejected(self):
        source=(STAGE/'main.asm').read_text()
        for bad in(-3,3,True,'0'):
            for mode,dx,dy in[(bad,0,0),(0,bad,0),(0,0,bad)]:
                with self.assertRaises(ValueError):PREPARE.prepare(source,mode,dx,dy)
        for broken in[source+'\n.text\n',source.replace('macro_current_end:','other:'),source.replace('macro_reference:','macro_current:'),source.replace('.dword 176, 88, 88, 176, 88, 88','.dword 176, 88, 88, 176, 88, 0'),source.replace('.dword 16, 32, -2, -2, 1, 0','.dword 16, 32, -2, -2, 1, 1')]:
            with self.assertRaises(ValueError):PREPARE.prepare(broken,0)

    def test_cli_protects_existing_student_file(self):
        with tempfile.TemporaryDirectory()as temporary:
            output=Path(temporary)/'worked.asm'
            command=[sys.executable,str(STAGE/'prepare.py'),'--mode','1','--dx','-1','--dy','1','--output',str(output)]
            first=subprocess.run(command,capture_output=True,text=True);self.assertEqual(0,first.returncode,first.stderr)
            before=output.read_bytes();self.assertNotEqual(0,subprocess.run(command,capture_output=True).returncode);self.assertEqual(before,output.read_bytes())


class MacroblockFeedbackRunnerTests(unittest.TestCase):
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
            result = subprocess.CompletedProcess([], 0, 'PASS case\n' * 64, '')
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
            for index, result in enumerate([subprocess.CompletedProcess([], 0, 'PASS case\n' * 63, ''),
                    subprocess.CompletedProcess([], 0, 'PASS case\n' * 64 + 'FAIL extra\n', ''),
                    subprocess.CompletedProcess([], 1, 'PASS case\n' * 64, 'error')]):
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
