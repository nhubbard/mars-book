"""Two views stopped under different breakpoint conditions cannot form one figure."""
import hashlib
import json
from pathlib import Path
import shutil
import tempfile
import unittest
import course

class ConditionedCompositeTest(unittest.TestCase):
    def test_views_with_different_native_conditions_are_rejected(self):
        with tempfile.TemporaryDirectory() as temporary:
            root=Path(temporary)/'package'
            shutil.copytree(course.ROOT/'packages/003-eight-byte-sprite',root)
            figure=json.loads((root/'captures/plan.json').read_text())['figures'][2]
            receipt=json.loads((root/figure['receipt']).read_text())
            course.validate_composite_capture(root,figure,receipt)
            part=receipt['parts'][0];path=(root/figure['receipt']).parent/part['receipt']
            raw=json.loads(path.read_text());raw['breakpointCondition']='$s2 == 10';path.write_text(json.dumps(raw))
            part['receiptSha256']=hashlib.sha256(path.read_bytes()).hexdigest()
            with self.assertRaisesRegex(ValueError,'different source or runtime provenance'):
                course.validate_composite_capture(root,figure,receipt)

if __name__=='__main__':unittest.main()
