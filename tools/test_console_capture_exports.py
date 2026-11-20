import json
from pathlib import Path
import shutil
import tempfile
import unittest
from unittest.mock import patch
import console_capture_exports as exporter
import course
ROOT=Path(__file__).resolve().parents[1];ID=exporter.ID

class ConsoleCaptureExportTests(unittest.TestCase):
    def setUp(self):
        self.temp=tempfile.TemporaryDirectory();self.addCleanup(self.temp.cleanup);self.root=Path(self.temp.name)
        for relative in ['packages/'+ID,'book/captures/'+ID,'includes/chapters/'+ID]:shutil.copytree(ROOT/relative,self.root/relative)
        self.package=self.root/'packages'/ID
    def test_all_five_reviewed_modes_and_native_paths_survive_relocation(self):
        exporter.export(self.root)
        with patch.object(course,'ROOT',self.root):course.validate(ID)
        plan=json.loads((self.package/'captures/plan.json').read_text())
        self.assertEqual(['editor','run','debug','debug','debug'],[f['captureMode']for f in plan['figures']])
        self.assertTrue(all(f['captureStatus']=='verified'for f in plan['figures']))
        self.assertTrue(all(f['viewport']==dict(width=1280,height=900,scale=1)for f in plan['figures']))
        for f in plan['figures']:
            text=(self.package/f['nativeAnnotations']).read_text();self.assertNotIn('book/captures/',text);self.assertIn('packages/'+ID+'/captures/',text)
            receipt=json.loads((self.package/f['receipt']).read_text());self.assertEqual(exporter.sha(self.package/f['nativeAnnotations']),receipt['nativeExportSha256'])
    def test_edited_export_is_retained(self):
        path=self.package/'native/f01-project.tex';path.write_text(path.read_text()+'\n% Human revision\n')
        with self.assertRaisesRegex(ValueError,'Edited native export'):exporter.export(self.root)
        self.assertIn('Human revision',path.read_text())
    def test_changed_native_composition_requires_new_review(self):
        path=self.root/'includes/chapters'/ID/'f01-project.tex';path.write_text(path.read_text()+'\n% New caption\n')
        with self.assertRaisesRegex(ValueError,'composition changed after review'):exporter.export(self.root)
    def test_symlink_asset_or_escaping_registry_is_rejected(self):
        asset=self.root/'book/captures'/ID/'f01-project/raw.png';data=asset.read_bytes();asset.unlink();outside=self.root/'unrelated.png';outside.write_bytes(data);asset.symlink_to(outside)
        with self.assertRaisesRegex(ValueError,'Symlink'):exporter.export(self.root)
        asset.unlink();asset.write_bytes(data)
        path=self.root/'book/captures'/ID/'figures.json';registry=json.loads(path.read_text());registry['figures'][0]['artifact']='../unrelated.png';path.write_text(json.dumps(registry))
        with self.assertRaisesRegex(ValueError,'Unsafe'):exporter.export(self.root)

if __name__=='__main__':unittest.main()
