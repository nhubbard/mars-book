from pathlib import Path
import json
import tempfile
import unittest

import bytecode_lesson_cases as vm
from console_cases import parse


class BytecodeLessonCasesTest(unittest.TestCase):
    def test_manually_derived_outcomes_cover_validation_priority_and_retirement(self):
        for trial,expected in vm.KNOWN:self.assertEqual(expected,vm.simulate(trial.image,trial.capacity,trial.fuel))
        self.assertEqual(vm.Outcome(-2,0,0,2,()),vm.simulate(bytes([48,1,2]),8,0))
        self.assertEqual(vm.Outcome(-5,0,0,0,()),vm.simulate(bytes([32,0]),0,64))
        self.assertEqual(vm.Outcome(-7,10,2,10,(-(1<<31),-1)),vm.simulate(vm.push(-(1<<31))+vm.push(-1)+bytes([19,0]),8,64))
        self.assertEqual(vm.Outcome(0,2,1,-1,(7,)),vm.simulate(bytes([1,7]),8,1))
        self.assertEqual(vm.Outcome(-4,2,1,2,(7,)),vm.simulate(bytes([1,7]),8,2))

    def test_all_original_trials_preserve_selected_views_sentinels_and_full_registers(self):
        trials=vm.trials();self.assertEqual(1247,len(trials));names=set()
        for index,trial in enumerate(trials):
            case=vm.kernel_case(index,trial);call=case['calls'][0];o=vm.simulate(trial.image,trial.capacity,trial.fuel);code='code_b' if index%3==0 else 'code_a';stack='stack_b' if index%4==0 else 'stack_a';state='state_b' if index%2==0 else 'state_a'
            self.assertEqual(vm.address(code,index%33),call['argumentAddresses']['a0']);self.assertEqual(vm.address(state),call['argumentAddresses']['a2']);self.assertEqual(vm.address(stack,index%9*4),call['setupAddressMemory'][0]['value']);self.assertEqual(call['setupAddressMemory'],call['addressMemory'])
            data=bytes.fromhex(call['setupMemory'][0]['bytes']);self.assertEqual(96,len(data));self.assertEqual(trial.image,data[index%33:index%33+len(trial.image)]);self.assertEqual(vm.bits(o.status),call['expectedRegisters']['v0']);self.assertEqual(vm.bits(o.retired),call['expectedRegisters']['v1'])
            policies=call['accessPolicies'];self.assertEqual([vm.window(state,0,16),vm.window(state,20,4),vm.window(stack,index%9*4,trial.capacity*4)],policies[0]['writable']);self.assertTrue(policies[0]['checkReads']);self.assertEqual(policies,case['exit']['accessPolicies']);self.assertEqual(9,len(case['preservedRegisters']));self.assertEqual(100000,case['instructionLimit']);names.add(case['name'])
        self.assertEqual(1247,len(names))

    def test_new_public_replacements_reuse_same_address_and_reset_each_trial(self):
        rows=vm.repeated_cases();self.assertEqual(4,len(rows))
        for case in rows:
            self.assertEqual(3,len(case['calls']));self.assertEqual(1,len({json.dumps(c['argumentAddresses'],sort_keys=True) for c in case['calls']}));self.assertEqual(3,len({c['setupMemory'][0]['bytes'] for c in case['calls']}))
        first=rows[0]['calls'];self.assertEqual([vm.bits(v) for v in (1,-3,1)],[c['expectedRegisters']['v0'] for c in first]);self.assertEqual(18,len(vm.REPLACEMENTS[0][0].image));self.assertEqual(18,len(vm.REPLACEMENTS[0][1].image))

    def test_caller_has_initial_return_and_stored_observations(self):
        rows=parse(json.dumps(vm.caller_profile()).encode());self.assertEqual(23,len(rows))
        for row in rows:
            self.assertEqual('vm_call',row.publication.label);self.assertEqual([None,'vm_ready','vm_stored'],[s.label for s in row.publication.snapshots]);self.assertEqual((),row.publication.final_state.access_policies[0].writable)
            self.assertEqual(2,len(row.publication.snapshots[1].expected_registers));self.assertEqual(row.publication.snapshots[1].expected_registers,row.publication.snapshots[2].expected_registers)

    def test_generated_profiles_are_reproducible_and_fit_native_limits(self):
        root=Path(__file__).resolve().parents[1]/'revision/labs/checks/bytecode-native';counts=vm.build(root,True);self.assertEqual(dict(structure=500,targets=18,completed=421,runtime=308),counts)
        with tempfile.TemporaryDirectory() as temporary:
            self.assertEqual(counts,vm.build(temporary))
            for file in root.iterdir():self.assertEqual(file.read_bytes(),(Path(temporary)/file.name).read_bytes())
        for kind,count in counts.items():
            files=sorted(root.glob(kind+'-*.cases.json'));self.assertEqual(count,sum(len(json.loads(p.read_text())['cases']) for p in files));self.assertTrue(all(p.stat().st_size<=1024*1024 and len(json.loads(p.read_text())['cases'])<=50 for p in files))


if __name__=='__main__':unittest.main()
