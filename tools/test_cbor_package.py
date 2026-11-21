"""CBOR student exports retain native authority, exact public checks and safe packaging."""
import json
import hashlib
from pathlib import Path
import re
import shutil
import tempfile
import unittest
from unittest.mock import patch
import zipfile

import cbor_package
import course


class CborPackageTests(unittest.TestCase):
    def test_native_order_and_metadata_do_not_duplicate_book_prose(self):
        root, data = course.validate('032-cbor-telemetry')
        native = course.ROOT/'includes/chapters/032-cbor-telemetry'
        expected = re.findall(r'\\input\{includes/chapters/032-cbor-telemetry/([^}]+)\}', (native/'chapter.tex').read_text())
        segments = course.reading_segments(root, data)
        actual = [s['step'] if s['kind']=='step' else s['figure'].removeprefix('cbor-') if s['kind']=='figure' else Path(s['content']).stem for s in segments]
        self.assertEqual(expected, actual)
        self.assertEqual(56, len(data['steps']))
        self.assertEqual(data['steps'],json.loads((root/'steps.json').read_text())['steps'])
        self.assertTrue(all('prose' not in step and 'expectedObservation' not in step for step in data['steps']))
        for path in native.glob('*.tex'):
            expected = path.read_text().replace('includes/chapters/032-cbor-telemetry','packages/032-cbor-telemetry/native').replace('drafts/cbor-construction/main.asm','starter/main.asm').replace('drafts/cbor-construction','the package').replace('drafts/cbor-fixtures/fixture.py','fixtures/fixture.py').replace('book/captures/032-cbor-telemetry/','packages/032-cbor-telemetry/captures/')
            self.assertEqual(expected,(root/'native'/path.name).read_text())

    def test_checks_and_starter_match_the_tested_public_draft(self):
        root,data=course.validate('032-cbor-telemetry');draft=course.ROOT/'drafts/cbor-construction'
        for path in (draft/'checks').rglob('*.kts'):
            self.assertEqual(path.read_bytes(),(root/'checks'/path.relative_to(draft/'checks')).read_bytes())
        self.assertEqual(7,len(data['publicChecks']))
        self.assertEqual(56,len(list((root/'checks/construction').glob('*.kts'))))
        self.assertEqual((draft/'main.asm').read_bytes(),(root/'starter/main.asm').read_bytes())
        self.assertEqual((draft/'inputs.json').read_bytes(),(root/'inputs.json').read_bytes())
        self.assertEqual((draft/'prepare.py').read_text().replace("default=ROOT/'main.asm'","default=ROOT/'starter/main.asm'"),(root/'prepare.py').read_text())

    def test_archive_is_deterministic_exact_and_student_only(self):
        root,data=course.validate('032-cbor-telemetry')
        with tempfile.TemporaryDirectory() as directory:
            first=course.bundle(data['id'],Path(directory)/'first.zip');second=course.bundle(data['id'],Path(directory)/'second.zip')
            self.assertEqual(first.read_bytes(),second.read_bytes())
            with zipfile.ZipFile(first) as archive:
                expected={str(Path(data['id'])/p.relative_to(root)):p.read_bytes()for p in course.student_files(root,data)}
                self.assertEqual(set(expected),set(archive.namelist()))
                for name,raw in expected.items():
                    self.assertEqual(raw,archive.read(name))
                    self.assertFalse(any(part.lower() in ['instructor','private','reference','solutions','__pycache__']for part in Path(name).parts))
                self.assertFalse(any('quiz-keys' in name for name in archive.namelist()))

    def test_capture_plans_bind_reviewed_native_figures(self):
        root,data=course.validate('032-cbor-telemetry');plans=json.loads((root/data['figures']).read_text())['figures'];self.assertEqual(4,len(plans))
        for plan in plans:
            self.assertEqual('verified',plan['captureStatus']);self.assertEqual('IntelliJ Light',plan['theme'])
            receipt=json.loads((root/plan['receipt']).read_text())
            self.assertEqual('native-composite',receipt['kind']);self.assertEqual(plan['id'],receipt['figure'])
            self.assertTrue(receipt['assertionsPassed']);self.assertEqual(plan['checkpoint'],receipt['sharedState']['checkpoint'])
            self.assertEqual(receipt['previewSha256'],hashlib.sha256((root/plan['artifact']).read_bytes()).hexdigest())
            self.assertEqual(receipt['nativeExportSha256'],hashlib.sha256((root/plan['nativeAnnotations']).read_bytes()).hexdigest())


    def test_reexport_preserves_reviewed_figures_and_refuses_edited_export(self):
        package_id='032-cbor-telemetry'
        with tempfile.TemporaryDirectory() as directory:
            root=Path(directory)
            for relative in ['packages/'+package_id,'includes/chapters/'+package_id,
                             'drafts/cbor-construction','drafts/cbor-fixtures',
                             'book/captures/'+package_id]:
                target=root/relative;target.parent.mkdir(parents=True,exist_ok=True)
                shutil.copytree(course.ROOT/relative,target,ignore=shutil.ignore_patterns('__pycache__'))
            package=root/'packages'/package_id
            inventory=lambda:{str(p.relative_to(package)):p.read_bytes()for p in package.rglob('*')if p.is_file()}
            before=inventory()
            with patch.multiple(cbor_package,ROOT=root,PACKAGE=package,
                                NATIVE=root/'includes/chapters'/package_id,
                                DRAFT=root/'drafts/cbor-construction'):
                cbor_package.build()
                self.assertEqual(before,inventory())
                edited=package/'native/head-fields.tex'
                edited.write_text(edited.read_text()+'% Retain this manual export edit.\n')
                authored=edited.read_bytes()
                before_failure=inventory()
                with self.assertRaisesRegex(ValueError,'Edited native export retained'):
                    cbor_package.build()
                self.assertEqual(authored,edited.read_bytes())
                self.assertEqual(before_failure,inventory())
                edited.write_bytes(before['native/head-fields.tex'])
                (package/'captures/head-fields/receipt.json').unlink()
                untracked=inventory()
                with self.assertRaisesRegex(ValueError,'Untracked native export retained'):
                    cbor_package.build()
                self.assertEqual(untracked,inventory())



if __name__=='__main__':unittest.main()
