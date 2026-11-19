"""PNG reading sizes, byte domains and safe fixture reproduction."""
import importlib.util
from pathlib import Path
import struct
import subprocess
import sys
import tempfile
import unittest
import zlib

import book


class PngFoundationTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.path = book.ROOT/'drafts/png-fixtures/fixture.py'
        spec = importlib.util.spec_from_file_location('png_reading_fixture',cls.path)
        cls.f = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(cls.f)

    def test_exact_worked_rows_and_display_words(self):
        data = self.f.material()
        self.assertEqual((115,46,35,30,40),tuple(len(data[n]) for n in ('png','zlib','filtered','rgb','words')))
        self.assertEqual(self.f.TRACE_FILTERED,data['filtered'])
        self.assertEqual(bytes.fromhex('7801012300dcff'),data['zlib'][:7])
        self.assertEqual(data['filtered'],zlib.decompress(data['zlib']))
        self.assertEqual(bytes.fromhex('1e140a00'),data['words'][:4])
        self.assertEqual((0x000a141e,0x0028323c,0x000f1923,0x002d3741,
                          0x00141e28,0x00323c46,0x0019232d,0x0037414b,
                          0x001e2832,0x003c4650),struct.unpack('<10I',data['words']))
        self.assertEqual(zlib.adler32(data['filtered']),int.from_bytes(data['zlib'][-4:],'big'))

    def test_png_chunk_offsets_crc_domains_and_header_split(self):
        data = self.f.material(); png = data['png']; at = 8; chunks = []
        self.assertEqual(self.f.SIGNATURE,png[:8])
        while at < len(png):
            length = int.from_bytes(png[at:at+4],'big')
            kind = png[at+4:at+8]; payload = png[at+8:at+8+length]
            crc = int.from_bytes(png[at+8+length:at+12+length],'big')
            self.assertEqual(zlib.crc32(kind+payload),crc)
            chunks.append((at,kind,payload)); at += 12+length
        self.assertEqual(115,at)
        self.assertEqual([8,33,46,103],[c[0] for c in chunks])
        self.assertEqual([b'IHDR',b'IDAT',b'IDAT',b'IEND'],[c[1] for c in chunks])
        self.assertEqual((2,5,8,2,0,0,0),struct.unpack('>IIBBBBB',chunks[0][2]))
        self.assertEqual(b'\x78',chunks[1][2])
        self.assertEqual(data['zlib'],chunks[1][2]+chunks[2][2])

    def test_vector_boundaries_compressions_and_empty_payload_splits(self):
        for width in (1,5,15,16,17,31,32,33,511,512):
            for mode in ('stored','fixed','auto'):
                data = self.f.material(width,5,mode)
                self.assertEqual(5*(1+3*width),len(data['filtered']))
                self.assertEqual(15*width,len(data['rgb']))
                self.assertEqual(20*width,len(data['words']))
                self.assertEqual(data['filtered'],zlib.decompress(data['zlib']))
                for split in (0,2,len(data['zlib'])):
                    changed = self.f.material(width,5,mode,split)
                    self.assertEqual(data['zlib'],changed['zlib'])
                    self.assertEqual(data['rgb'],changed['rgb'])
        for dimensions in ((0,5),(513,5),(2,0),(2,513)):
            with self.assertRaises(ValueError): self.f.material(*dimensions)
        with self.assertRaises(ValueError): self.f.material(split=47)
        with self.assertRaises(ValueError): self.f.filtered(2,5,b'')

    def test_fixture_cli_preserves_existing_destination(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory)/'first.png'
            command = [sys.executable,str(self.path),str(path)]
            result = subprocess.run(command,capture_output=True,text=True)
            self.assertEqual(0,result.returncode,result.stderr)
            expected = path.read_bytes()
            self.assertEqual(self.f.material()['png'],expected)
            result = subprocess.run(command+['--width','17'],capture_output=True,text=True)
            self.assertNotEqual(0,result.returncode)
            self.assertEqual(expected,path.read_bytes())
            bad = Path(directory)/'bad.png'
            result = subprocess.run(command[:-1]+[str(bad),'--height','0'],capture_output=True,text=True)
            self.assertNotEqual(0,result.returncode); self.assertFalse(bad.exists())

    def test_native_composition_is_direct_and_not_integrated_prematurely(self):
        paths = [p for p,_ in book.sources('drafts/png.tex') if p.parent.name == '033-png']
        self.assertEqual(20,len(paths)); self.assertEqual(20,len(set(paths)))
        self.assertEqual('purpose',paths[1].stem); self.assertEqual('sources',paths[-1].stem)
        self.assertEqual(0,len(book.validate('drafts/png.tex')))
        self.assertNotIn('033-png',(book.ROOT/'main.tex').read_text())
