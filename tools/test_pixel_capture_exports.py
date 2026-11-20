"""Reviewed multi-view pixel figures remain usable after student relocation."""
import json
from pathlib import Path
import shutil
import tempfile
import unittest
from unittest.mock import patch
import console_capture_exports as exporter
import course
ROOT=Path(__file__).resolve().parents[1]
ID='002-single-pixel'

class PixelCaptureExportTests(unittest.TestCase):
    def setUp(self):
        self.temp=tempfile.TemporaryDirectory();self.addCleanup(self.temp.cleanup);self.root=Path(self.temp.name)
        for relative in ['packages/'+ID,'book/captures/'+ID,'includes/chapters/'+ID]:
            shutil.copytree(ROOT/relative,self.root/relative)
        self.package=self.root/'packages'/ID

    def test_three_reviewed_multi_view_figures_survive_relocation(self):
        exporter.export(self.root,ID)
        with patch.object(course,'ROOT',self.root):course.validate(ID)
        plan=json.loads((self.package/'captures/plan.json').read_text())
        self.assertEqual(['base_ready','final_ready','after_final_load'],[f['checkpoint']for f in plan['figures']])
        for f in plan['figures']:
            self.assertEqual('verified',f['captureStatus'])
            self.assertEqual(dict(width=1280,height=900,scale=1),f['viewport'])
            text=(self.package/f['nativeAnnotations']).read_text()
            self.assertNotIn('book/captures/',text)
            self.assertIn('packages/'+ID+'/captures/',text)
            receipt=json.loads((self.package/f['receipt']).read_text())
            self.assertEqual(2,len(receipt['parts']))
            self.assertEqual(3,len(receipt['panels']))
            self.assertEqual(exporter.sha(self.package/f['nativeAnnotations']),receipt['nativeExportSha256'])
            for part in receipt['parts']:
                self.assertEqual(exporter.sha((self.package/f['receipt']).parent/part['receipt']),part['receiptSha256'])

    def test_edited_student_native_export_is_retained(self):
        path=self.package/'native/f01-base-color.tex';path.write_text(path.read_text()+'\n% Human revision\n')
        with self.assertRaisesRegex(ValueError,'Edited native export'):exporter.export(self.root,ID)
        self.assertIn('Human revision',path.read_text())

    def test_modified_raw_receipt_fails_student_validation(self):
        path=self.package/'captures/f02-byte-update/memory.json';raw=json.loads(path.read_text());raw['checkpoint']='base_ready';path.write_text(json.dumps(raw))
        with patch.object(course,'ROOT',self.root):
            with self.assertRaisesRegex(ValueError,'receipt changed after review'):course.validate(ID)

    def test_invalid_package_identity_is_rejected_before_file_access(self):
        for value in ['../002-single-pixel','/tmp/002-single-pixel','002-single-pixel/other','002-pixel_unsafe',None]:
            with self.subTest(value=value):
                with self.assertRaisesRegex(ValueError,'Unsafe package identity'):exporter.export(self.root,value)

if __name__=='__main__':unittest.main()
