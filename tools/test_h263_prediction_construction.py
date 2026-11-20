"""Prediction fixtures retain student instructions and signed residual expectations."""
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

ROOT = Path(__file__).resolve().parents[1]
STAGE = ROOT / 'drafts/h263-prediction-construction'
SPEC = importlib.util.spec_from_file_location('prediction_preparation_tests', STAGE / 'prepare.py')
PREPARE = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(PREPARE)


class PredictionPreparationTests(unittest.TestCase):
    def test_real_chroma_windows_phases_and_unchanged_student_instructions(self):
        source = (STAGE / 'main.asm').read_text() + '\nstudent_helper:\n    daddiu $t0, $t0, 27\n'
        expected = {(0, 0): (128, 0, 0), (0, 1): (128, 0, 112),
                    (1, 0): (128, 0, 112), (1, 1): (120, 8, 232)}
        for mode in (0, 1):
            for px in (0, 1):
                for py in (0, 1):
                    prepared = PREPARE.prepare(source, mode, px, py)
                    self.assertEqual(source.split('\n.text\n')[1], prepared.split('\n.text\n')[1])
                    self.assertIn('.dword '+str(px)+', '+str(py)+', '+str(mode)+', 0', prepared)
                    arrays = []
                    for label, length in [('prediction_current', 656), ('prediction_reference', 32+(7+py)*88+8+px)]:
                        body = prepared.split(label+':\n', 1)[1].split(label+'_end:', 1)[0]
                        raw = bytes(int(v)&255 for line in body.splitlines()
                                    for v in line.strip().removeprefix('.byte ').split(','))
                        self.assertEqual(length, len(raw))
                        self.assertEqual(bytes([165]*16), raw[:16])
                        self.assertEqual(bytes([165]*16), raw[-16:])
                        arrays.append(raw)
                    current, reference = arrays
                    predictions = []; residuals = []
                    for y in range(8):
                        for x in range(8):
                            values = [reference[16+(y+v)*88+x+u] for v in range(py+1) for u in range(px+1)]
                            prediction = (sum(values)+len(values)//2)//len(values)
                            predictions.append(prediction)
                            residuals.append(current[16+y*88+x]-prediction)
                    self.assertEqual(expected[px,py], (predictions[0], residuals[0], sum(map(abs,residuals))))

    def test_invalid_controls_and_changed_declarations_are_rejected(self):
        source = (STAGE / 'main.asm').read_text()
        for bad in (-1, 2, True, '0'):
            for mode, px, py in [(bad,1,1), (0,bad,1), (0,1,bad)]:
                with self.assertRaises(ValueError):
                    PREPARE.prepare(source, mode, px, py)
        for broken in [source+'\n.text\n', source.replace('prediction_current_end:', 'other:'),
                       source.replace('prediction_reference:', 'prediction_current:'),
                       source.replace('.dword 1, 1, 1, 0', '.dword 1, 1, 1, 1'),
                       source.replace('.dword 0, 0, 88, 88', '.dword 0, 0, 8, 88')]:
            with self.assertRaises(ValueError):
                PREPARE.prepare(broken, 0)

    def test_cli_preserves_an_existing_working_file(self):
        with tempfile.TemporaryDirectory() as temporary:
            output = Path(temporary) / 'worked.asm'
            command = [sys.executable,str(STAGE/'prepare.py'),'--mode','1','--phase-x','0','--phase-y','1','--output',str(output)]
            first = subprocess.run(command,capture_output=True,text=True)
            self.assertEqual(0, first.returncode, first.stderr)
            before = output.read_bytes()
            self.assertNotEqual(0, subprocess.run(command,capture_output=True).returncode)
            self.assertEqual(before, output.read_bytes())


class PredictionFeedbackRunnerTests(unittest.TestCase):
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
