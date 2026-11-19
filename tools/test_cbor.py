"""Fixed CBOR teaching values, record storage and native source authority."""
import importlib.util
from pathlib import Path
import re
import struct
import subprocess
import sys
import tempfile
import unittest

import book


class CborFoundationTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.path = book.ROOT/'drafts/cbor-fixtures/fixture.py'
        spec = importlib.util.spec_from_file_location('fixed_cbor_tests', cls.path)
        cls.f = importlib.util.module_from_spec(spec); spec.loader.exec_module(cls.f)

    def test_exact_worked_offsets_and_record_fields(self):
        raw, record = self.f.material('trace')
        self.assertEqual(52,len(raw)); self.assertEqual(bytes.fromhex('b804'),raw[:2])
        self.assertEqual(bytes.fromhex('a201824280ff210080'),raw[3:12])
        self.assertEqual(bytes.fromhex('980a'),raw[13:15])
        self.assertEqual(bytes.fromhex('01580300ff41'),raw[35:41])
        self.assertEqual(bytes.fromhex('18001b0102030405060708'),raw[41:52])
        self.assertEqual(176,len(record))
        self.assertEqual((0x0102030405060708,3,10),struct.unpack('<QII',record[:16]))
        self.assertEqual(bytes.fromhex('00ff41'),record[16:19])
        self.assertEqual(bytes([0xa5])*29,record[19:48])
        self.assertEqual(self.f.READINGS,struct.unpack('<10h',record[48:68]))
        self.assertEqual(bytes([0xa5])*108,record[68:])
        canonical, other = self.f.material('canonical')
        self.assertEqual(record,other); self.assertEqual(37,len(canonical))
        self.assertEqual(bytes.fromhex('a3001b0102030405060708014300ff41028a'),canonical[:18])
        self.assertEqual(bytes.fromhex('001718182037381818ff190100197fff397fff'),canonical[18:])

    def test_empty_maximum_and_input_budget_are_different_observations(self):
        raw,record = self.f.material('empty')
        self.assertEqual(bytes.fromhex('a3000001400280'),raw)
        self.assertEqual(bytes(16)+bytes([0xa5])*160,record)
        maximum,record = self.f.material('maximum')
        self.assertEqual(241,len(maximum))
        self.assertEqual((2**64-1,32,64),struct.unpack('<QII',record[:16]))
        self.assertEqual(bytes(range(32)),record[16:48])
        self.assertEqual((32767,-32768)*32,struct.unpack('<64h',record[48:]))
        budget,flat = self.f.material('budget')
        self.assertEqual(1024,len(budget)); self.assertEqual(self.f.material('empty')[1],flat)
        self.assertEqual(bytes.fromhex('a4000001400280039840'),budget[:10])
        self.assertEqual(b'\x40'*35,budget[-35:])
        with self.assertRaises(ValueError):self.f.material('unknown')

    def test_fixture_cli_refuses_existing_output(self):
        with tempfile.TemporaryDirectory() as directory:
            path=Path(directory)/'trace.cbor'
            command=[sys.executable,str(self.path),str(path)]
            result=subprocess.run(command,capture_output=True,text=True)
            self.assertEqual(0,result.returncode,result.stderr)
            self.assertEqual(self.f.TRACE,path.read_bytes())
            result=subprocess.run(command+['--fixture','maximum'],capture_output=True,text=True)
            self.assertNotEqual(0,result.returncode);self.assertEqual(self.f.TRACE,path.read_bytes())
            record=Path(directory)/'record.bin'
            result=subprocess.run(command[:-1]+[str(record),'--record'],capture_output=True,text=True)
            self.assertEqual(0,result.returncode,result.stderr);self.assertEqual(self.f.material('trace')[1],record.read_bytes())

    def test_native_reading_includes_every_passage_once(self):
        source=[p for p,_ in book.sources('drafts/cbor-telemetry.tex')if p.parent.name=='032-cbor-telemetry']
        self.assertEqual(27,len(source));self.assertEqual(len(source),len(set(source)))
        self.assertEqual('chapter',source[0].stem);self.assertEqual('purpose',source[1].stem);self.assertEqual('sources',source[-1].stem)
        self.assertEqual(0,len(book.validate('drafts/cbor-telemetry.tex')))
        chapter=(book.ROOT/'includes/chapters/032-cbor-telemetry/chapter.tex').read_text()
        self.assertEqual(26,len(re.findall(r'\\input\{',chapter)))
