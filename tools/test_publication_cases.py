from copy import deepcopy
from dataclasses import FrozenInstanceError
import json
import unittest

import console_cases
import publication_cases as checks


class PublicationCasesTest(unittest.TestCase):
    def state(self):
        return dict(memory=[dict(label='used', bytes='01000000')],
                    addressMemory=[dict(at=dict(label='pointer'), value=dict(label='ring', offset=3))],
                    accessPolicies=[dict(**{'from': dict(label='used')}, until=dict(label='end', offset=4),
                                         writable=[dict(at=dict(label='used'), length=4)], checkReads=False)])

    def publication(self):
        return dict(label='operation_ready', initialMemory=[dict(label='command_count', bytes='01000000')],
                    snapshots=[self.state()], finalState=self.state())

    def encode(self, publication=None, version=7):
        return json.dumps(dict(formatVersion=version, cases=[dict(name='caller', input='', stdout='',
            instructionLimit=20000, publication=self.publication() if publication is None else publication)])).encode()

    def test_native_models_are_detached_and_generated_checks_use_the_same_public_checker(self):
        original = self.publication()
        check = checks.parse(original)
        original['snapshots'][0]['memory'].clear()
        self.assertEqual(1, len(check.snapshots[0].memory))
        self.assertEqual(3, check.final_state.address_memory[0].value.offset)
        with self.assertRaises(FrozenInstanceError): check.label = 'other'
        case = console_cases.parse(self.encode())[0]
        script = console_cases.render((case,))
        for name in checks.IMPORTS:
            self.assertIn('import edu.missouristate.mars.cli.grading.' + name, script)
        self.assertIn('ConsolePublicationCheck("operation_ready"', script)
        self.assertIn('checkReads = false', script)
        self.assertIn('FunctionDataAddress("ring", 3)', script)
        self.assertIn('.verify(this)', script)
        self.assertNotIn('runToExit()', script)
        for version in range(1, 7):
            with self.assertRaises(ValueError): console_cases.parse(self.encode(version=version))
        mixed = json.loads(self.encode());mixed['cases'][0]['maze'] = dict(kind='renderer', fixture=0)
        with self.assertRaises(ValueError): console_cases.parse(json.dumps(mixed).encode())

    def test_labels_rows_fields_policies_and_native_work_bounds_are_strict(self):
        for bad in [None, [], True, 7, 'anything']:
            with self.assertRaises(ValueError): checks.parse(bad)
        for key in self.publication():
            missing = self.publication();del missing[key]
            with self.assertRaises(ValueError): checks.parse(missing)
        with self.assertRaises(ValueError): checks.parse(dict(self.publication(), callback='run this'))
        for bad in ['', 'two labels', 'x' * 129, None, 1]:
            with self.assertRaises(ValueError): checks.parse(dict(self.publication(), label=bad))
        self.assertEqual(0, len(checks.parse(dict(self.publication(), snapshots=[])).snapshots))
        self.assertEqual(256, len(checks.parse(dict(self.publication(), snapshots=[self.state()] * 256)).snapshots))
        with self.assertRaises(ValueError): checks.parse(dict(self.publication(), snapshots=[self.state()] * 257))
        for field in ['memory', 'addressMemory', 'accessPolicies']:
            for bad in [None, {}, True, 4]:
                with self.assertRaises(ValueError): checks.state(dict(self.state(), **{field: bad}))
            missing = self.state();del missing[field]
            with self.assertRaises(ValueError): checks.state(missing)
        for field, value in [('maxAccesses', 0), ('maxAccesses', True), ('maxAccesses', 1000001),
                             ('checkReads', 'false'), ('predicate', 'anything')]:
            policy = deepcopy(self.state()['accessPolicies'][0]);policy[field] = value
            with self.assertRaises(ValueError): checks.policies([policy])
        policy = deepcopy(self.state()['accessPolicies'][0]);policy['readable'] = [dict(at=dict(label='used'), length=1)]
        with self.assertRaises(ValueError): checks.policies([policy])
        with self.assertRaises(ValueError): checks.policies([dict(**{'from':dict(label='used')}, until=dict(label='end'), maxAccesses=1000000)] * 5)
        with self.assertRaises(ValueError): checks.parse(dict(self.publication(), initialMemory=[dict(label='used', bytes='00', repeat=1048576)]))
        for bad in [True, '1', 1.0, -1, 1048577]:
            with self.assertRaises(ValueError): checks.memory([dict(label='used', bytes='00', repeat=bad)])
        for bad in ['', 'z0', '001', None]:
            with self.assertRaises(ValueError): checks.memory([dict(label='used', bytes=bad)])
        for bad in [True, 1.0, '1', -1048577, 1048577]:
            with self.assertRaises(ValueError): checks.address(dict(label='used', offset=bad))
        with self.assertRaises(ValueError): checks.state(dict(self.state(), memory=[dict(label='used', bytes='00')] * 64))
        with self.assertRaises(ValueError): checks.windows([dict(at=dict(label='used'), length=-1)])
        with self.assertRaises(ValueError): checks.windows([dict(at=dict(label='used'), length=0)] * 65)


if __name__ == '__main__':
    unittest.main()
