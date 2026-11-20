"""QCIF input geometry, exact clock limits and independently calculated pixel positions."""
import hashlib
import importlib.util
import json
import subprocess
import sys
import tempfile
import unittest
from fractions import Fraction
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
SPEC=importlib.util.spec_from_file_location('h263_fixture_tests',ROOT/'drafts/h263-fixtures/fixture.py')
FIX=importlib.util.module_from_spec(SPEC);SPEC.loader.exec_module(FIX)
HEADER_SPEC=importlib.util.spec_from_file_location('h263_header_tests',ROOT/'drafts/h263-fixtures/headers.py')
HEADERS=importlib.util.module_from_spec(HEADER_SPEC);HEADER_SPEC.loader.exec_module(HEADERS)


class H263FixtureTests(unittest.TestCase):
    def test_plane_offsets_and_default_timing_are_exact(self):
        data=FIX.metadata();self.assertEqual(38016,data['frameBytes']);self.assertEqual(304128,data['bytes'])
        self.assertEqual([0,25344,31680],[p['offset']for p in data['planes']])
        self.assertEqual([176,88,88],[p['stride']for p in data['planes']])
        self.assertEqual(dict(numerator=5000,denominator=1001),data['frameRate'])
        self.assertEqual(Fraction(1001,625),Fraction(data['duration']['numerator'],data['duration']['denominator']))
        self.assertEqual([0,6,12,18,24,30,36,42],data['temporalReferences'])
    def test_actual_planes_and_square_motion(self):
        a,b=FIX.frame(0),FIX.frame(1)
        self.assertEqual(38016,len(a));self.assertEqual(38016,len(b))
        for pixels,left,top in[(a,16,32),(b,18,34)]:
            self.assertEqual(32,pixels[0]);self.assertEqual(200,pixels[top*176+left])
            self.assertNotEqual(200,pixels[top*176+left-1]);self.assertNotEqual(200,pixels[(top-1)*176+left])
            self.assertEqual(96,pixels[25344+(top//2)*88+left//2])
            self.assertEqual(176,pixels[31680+(top//2)*88+left//2])
            self.assertEqual(128,pixels[25344]);self.assertEqual(128,pixels[31680])
            self.assertTrue(all(16<=v<=235 for v in pixels[:25344]));self.assertTrue(all(16<=v<=240 for v in pixels[25344:]))
        self.assertNotEqual(a,b);self.assertEqual(FIX.frame(5),FIX.frame(6));self.assertNotEqual(FIX.frame(6),FIX.frame(7))
    def test_exact_duration_ceiling_and_wrapping_reference(self):
        FIX.validate(299,1);FIX.validate(49,6);FIX.validate(1,255)
        self.assertEqual(2,FIX.metadata(130,2)['temporalReferences'][-1])
        for frames,step in[(300,1),(50,6),(2,255),(0,6),(8,0),(1,256),(True,6),(8,6.0)]:
            with self.subTest(frames=frames,step=step),self.assertRaises(ValueError):FIX.validate(frames,step)
        for index in[-1,True,1.5]:
            with self.assertRaises(ValueError):FIX.frame(index)
    def test_write_is_deterministic_and_refuses_existing_paths(self):
        with tempfile.TemporaryDirectory()as temporary:
            root=Path(temporary)
            first=FIX.write(root/'a');second=FIX.write(root/'b');self.assertEqual(first,second)
            pixels=(root/'a/clip.yuv').read_bytes();self.assertEqual(304128,len(pixels));self.assertEqual(hashlib.sha256(pixels).hexdigest(),first['sha256'])
            for index,digest in enumerate(first['frameSha256']):self.assertEqual(digest,hashlib.sha256(pixels[index*38016:(index+1)*38016]).hexdigest())
            before=(root/'a/clip.json').read_bytes()
            with self.assertRaises(FileExistsError):FIX.write(root/'a')
            self.assertEqual(before,(root/'a/clip.json').read_bytes())
            with self.assertRaises(ValueError):FIX.write(root/'invalid',50,6)
            self.assertFalse((root/'invalid').exists())
    def test_cli_controls_and_existing_output_protection(self):
        with tempfile.TemporaryDirectory()as temporary:
            target=Path(temporary)/'clip';command=[sys.executable,str(ROOT/'drafts/h263-fixtures/fixture.py'),'--output-dir',str(target)]
            result=subprocess.run(command,capture_output=True,text=True);self.assertEqual(0,result.returncode,result.stderr)
            self.assertEqual(8,json.loads((target/'clip.json').read_text())['frames'])
            self.assertNotEqual(0,subprocess.run(command,capture_output=True).returncode)
            result=subprocess.run(command+['--frames','300'],capture_output=True,text=True);self.assertNotEqual(0,result.returncode)


class H263HeaderTests(unittest.TestCase):
    @staticmethod
    def header(tr=0,ptype=0x1040,quant=6,cpm=0,pei=0):
        # Six independent fixed-width fields, then zero byte-alignment padding.
        bits=f'{32:022b}{tr:08b}{ptype:013b}{quant:05b}{cpm:01b}{pei:01b}'
        bits+='0'*(-len(bits)%8)
        return int(bits,2).to_bytes(len(bits)//8,'big')
    def test_baseline_headers_and_nonzero_picture_clock_steps(self):
        stream=self.header()+b'\xff'+self.header(6,0x1050)
        result=HEADERS.inspect(stream)
        self.assertEqual([0,6],[h['temporalReference']for h in result]);self.assertEqual(['I','P'],[h['pictureType']for h in result]);self.assertEqual([6,6],[h['quantizer']for h in result])
    def test_unsupported_or_truncated_headers_are_rejected(self):
        bad=[b'',b'\xff'+self.header(),self.header()[:6],self.header(ptype=0x1050),self.header(ptype=0x0040),self.header(ptype=0x10e0),self.header(ptype=0x1041),self.header(ptype=0x1140),self.header(quant=0),self.header(cpm=1),self.header(pei=1)]
        for stream in bad:
            with self.subTest(stream=stream.hex()),self.assertRaises(ValueError):HEADERS.inspect(stream)
    def test_header_inspection_does_not_claim_macroblock_validation(self):
        self.assertEqual(1,len(HEADERS.inspect(self.header())))
        with self.assertRaises(ValueError):HEADERS.bits(b'\x00',1,8)


if __name__=='__main__':unittest.main()
