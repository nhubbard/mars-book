import importlib.util
import json
import tempfile
import unittest
from pathlib import Path
from types import SimpleNamespace
from unittest.mock import patch
ROOT=Path(__file__).resolve().parents[1]
STAGE=ROOT/'drafts/h263-clip-construction'
def load(name):
    s=importlib.util.spec_from_file_location('clip_test_'+name,STAGE/(name+'.py'));m=importlib.util.module_from_spec(s);s.loader.exec_module(m);return m
PREP=load('prepare');COMPOSE=load('compose');RUN=load('run')
class ClipToolsTest(unittest.TestCase):
    def setUp(self):self.source=(STAGE/'main.asm').read_text()
    def test_preparation_preserves_instructions_and_sets_all_controls(self):
        text=PREP.prepare(self.source,Path('/tmp/clip run'),2,3,12,0)
        self.assertEqual(text.split('\n.text\n')[1],self.source.split('\n.text\n')[1]);self.assertIn('0,0,0,2,3,12,0,0,0,0,0,0,0',text)
        self.assertIn('/tmp/clip run/encoded.263',text)
    def test_each_path_owns_exactly_4096_bytes(self):
        folder=Path('/tmp/clip-run')
        text=PREP.prepare(self.source,folder)
        for name,file in [('input','clip.yuv'),('stream','encoded.263'),('reconstruction','reconstructed.yuv')]:
            part=text.split(f'clip_{name}_path:\n',1)[1].split(f'clip_{name}_path_end:',1)[0];pad=int(part.split('.space ')[1]);self.assertEqual(len(str(folder.resolve()/file))+1+pad,4096)
    def test_path_quoting_and_rejected_byte_encodings(self):
        self.assertEqual(PREP.quote('a"b\\c'),'a\\"b\\\\c')
        for path in ['nul\0path','line\nbreak','non-ascii-λ']:
            with self.assertRaises(ValueError):PREP.quote(path)
    def test_invalid_timing_quantizer_and_mode_are_rejected(self):
        for kwargs in [dict(frames=0),dict(frames=300),dict(tick_step=0),dict(tick_step=256),dict(frames=2,tick_step=255),dict(quant=0),dict(mode=2),dict(mode=True)]:
            with self.assertRaises(ValueError):PREP.prepare(self.source,Path('/tmp/new'),**kwargs)
    def test_changed_source_boundaries_are_rejected(self):
        for text in [self.source.replace('clip_request:','missing:'),self.source.replace('clip_input_path_end:','missing:'),self.source+'\n.text\n']:
            with self.assertRaises(ValueError):PREP.prepare(text,Path('/tmp/new'))
    def test_new_run_has_exact_generated_input_and_retains_source(self):
        with tempfile.TemporaryDirectory()as temp:
            folder=Path(temp)/'run';metadata=PREP.write(self.source,folder,2,6)
            self.assertEqual((folder/'clip.yuv').stat().st_size,76032);self.assertEqual(metadata['frames'],2);self.assertEqual(json.loads((folder/'clip.json').read_text())['tickStep'],6)
            self.assertEqual((folder/'main.asm').read_text().split('\n.text\n')[1],self.source.split('\n.text\n')[1])
    def test_existing_run_is_never_replaced(self):
        with tempfile.TemporaryDirectory()as temp:
            folder=Path(temp)/'run';folder.mkdir();sentinel=folder/'main.asm';sentinel.write_text('student edits')
            with self.assertRaises(FileExistsError):PREP.write(self.source,folder)
            self.assertEqual(sentinel.read_text(),'student edits');self.assertFalse((folder/'clip.yuv').exists())
    def test_invalid_source_creates_no_partial_run(self):
        with tempfile.TemporaryDirectory()as temp:
            folder=Path(temp)/'run'
            with self.assertRaises(ValueError):PREP.write('bad source',folder)
            self.assertFalse(folder.exists())
    def test_composition_carries_student_picture_data_and_helpers(self):
        picture='.data\nowned_table: .word 17\n.text\nmain:\n nop\nh263_encode_picture:\n li $v0, 0\n jr $ra\n nop\nowned_helper:\n nop\n'
        joined=COMPOSE.compose(self.source,picture);self.assertEqual(joined.count('\n.text\n'),1);self.assertEqual(joined.count('h263_encode_picture:'),1);self.assertIn('owned_table:',joined);self.assertIn('owned_helper:',joined);self.assertIn(self.source.split('\n.text\n')[1].split(COMPOSE.MARKER)[0],joined)
    def test_composition_rejects_missing_or_ambiguous_entries(self):
        for dependency in ['.text\nmain:\n nop\n','.text\nh263_encode_picture:\n nop\n.text\n']:
            with self.assertRaises(ValueError):COMPOSE.compose(self.source,dependency)
    def test_run_records_actual_program_and_forwards_to_stream_checker(self):
        with tempfile.TemporaryDirectory()as temp:
            d=Path(temp);source=d/'student.asm';source.write_text(self.source);cli=d/'cli.jar';cli.write_bytes(b'archive');folder=d/'run';calls=[]
            def check(*args):calls.append(args);return dict(frames=2,encodedBytes=900)
            original=RUN.load
            with patch.object(RUN,'load',side_effect=lambda name,path:original(name,path)if name=='clip_run_prepare'else SimpleNamespace(check=check)),patch.object(RUN.subprocess,'run',return_value=SimpleNamespace(returncode=0,stdout='2\n\n123456\n',stderr=''))as process:
                result=RUN.run(source,cli,'java',folder,2,6)
                self.assertEqual(result['guestInstructions'],123456);self.assertEqual(calls[0][-2:],(2,6));self.assertEqual(process.call_args.args[0][-1],str((folder/'main.asm').resolve()));self.assertEqual(json.loads((folder/'verification.json').read_text())['javaExitCode'],0)
    def test_run_failure_and_invalid_count_do_not_claim_verification(self):
        with tempfile.TemporaryDirectory()as temp:
            d=Path(temp);source=d/'student.asm';source.write_text(self.source);cli=d/'cli.jar';cli.write_bytes(b'archive')
            for n,result in enumerate([SimpleNamespace(returncode=1,stdout='failed',stderr='error'),SimpleNamespace(returncode=0,stdout='1\n123\n',stderr='')]):
                folder=d/f'run-{n}'
                with patch.object(RUN.subprocess,'run',return_value=result):
                    with self.assertRaises(ValueError):RUN.run(source,cli,'java',folder,2,6)
                self.assertTrue((folder/'run.txt').exists());self.assertFalse((folder/'verification.json').exists())
