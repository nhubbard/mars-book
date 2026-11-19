"""A student figure cannot silently substitute images, state, or native annotations."""
import copy
import json
from pathlib import Path
import shutil
import tempfile
import unittest

import course


class CompositeCaptureTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        package = course.ROOT / 'packages/034-lossless-video'
        shutil.copytree(package / 'captures', self.root / 'captures')
        (self.root / 'native').mkdir()
        shutil.copyfile(package / 'native/fig-vector-count.tex', self.root / 'native/fig-vector-count.tex')
        self.figure = next(f for f in course.read_json(package/'captures/plan.json')['figures'] if f['id']=='video-vector-count')
        self.receipt = course.read_json(self.root / self.figure['receipt'])

    def validate(self, receipt=None):
        course.validate_composite_capture(self.root, self.figure, self.receipt if receipt is None else receipt)

    def test_two_actual_views_and_three_asserted_panels_pass(self):
        self.validate()
        self.assertEqual(2,len(self.receipt['parts']))
        self.assertEqual(3,len(self.receipt['panels']))

    def test_unknown_or_downgraded_receipt_format_fails(self):
        for key,value in [('formatVersion',1),('kind','unrecognized')]:
            with self.subTest(field=key):
                receipt=copy.deepcopy(self.receipt);receipt[key]=value
                with self.assertRaises(ValueError):self.validate(receipt)

    def test_changed_raw_preview_or_native_bytes_fail(self):
        for relative in ('captures/video-vector-count/counter.png', self.figure['artifact'], self.figure['nativeAnnotations']):
            with self.subTest(relative=relative):
                path=self.root/relative; original=path.read_bytes()
                try:
                    path.write_bytes(original+b'changed')
                    with self.assertRaises(ValueError):self.validate()
                finally:path.write_bytes(original)

    def test_different_source_runtime_or_readiness_fails(self):
        path=self.root/'captures/video-vector-count/lanes.json'; original=path.read_text()
        for key,value in [('sourceSha256','0'*64),('sourceLine',151),('captureDriverSha256','1'*64),('notificationBalloonsVisible',1),('visibleAnalysisIndicators',['Analyzing'])]:
            with self.subTest(field=key):
                raw=json.loads(original);raw[key]=value;path.write_text(json.dumps(raw))
                with self.assertRaises(ValueError):self.validate()
                path.write_text(original)

    def test_crop_cannot_clip_its_asserted_row_or_leave_the_image(self):
        for change in ('clip','outside','noninteger','missing-callout'):
            with self.subTest(change=change):
                receipt=copy.deepcopy(self.receipt);panel=receipt['panels'][1]
                if change=='clip':panel['crop'][2]=10
                if change=='outside':panel['crop'][0]=1280
                if change=='noninteger':panel['crop'][1]=0.5
                if change=='missing-callout':panel['callout']=-1
                with self.assertRaises(ValueError):self.validate(receipt)

    def test_additional_assertions_cannot_be_clipped_or_duplicated(self):
        for indices in ([0],[1]):
            with self.subTest(indices=indices):
                receipt=copy.deepcopy(self.receipt)
                receipt['panels'][1]['additionalCallouts']=indices
                with self.assertRaises(ValueError):self.validate(receipt)

    def test_raw_parts_cannot_escape_duplicate_or_disappear(self):
        for change in ('escape','duplicate','missing-part','unused-part'):
            with self.subTest(change=change):
                receipt=copy.deepcopy(self.receipt)
                if change=='escape':receipt['parts'][0]['image']='../../../../outside.png'
                if change=='duplicate':receipt['parts'][1]['id']=receipt['parts'][0]['id']
                if change=='missing-part':receipt['panels'][0]['part']='absent'
                if change=='unused-part':receipt['panels']=[p for p in receipt['panels'] if p['part']=='counter']
                with self.assertRaises(ValueError):self.validate(receipt)


if __name__=='__main__':unittest.main()
