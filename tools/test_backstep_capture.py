"""A reverse illustration requires the actual native action and source transition."""
import copy
import unittest
import course

class BackstepCaptureTest(unittest.TestCase):
    def setUp(self):
        self.figure={'backstepFrom':'east_stopped'}
        self.raw={'backstepFrom':'east_stopped','sourceLine':23,'observedBackstep':{'fromCheckpoint':'east_stopped','sourceLine':25,'toSourceLine':23,'actionText':'Backstep Instruction'}}
    def test_actual_reverse_transition_and_older_forward_captures_pass(self):
        course.validate_capture_mode(self.figure,self.raw)
        course.validate_capture_mode({}, {})
    def test_missing_action_source_or_checkpoint_evidence_fails(self):
        for change in ['missing','wrong-start','wrong-action','wrong-from','wrong-to','same-line','bool-line','wrong-arrival']:
            with self.subTest(change=change):
                raw=copy.deepcopy(self.raw)
                if change=='missing':raw.pop('observedBackstep')
                if change=='wrong-start':raw['backstepFrom']='other'
                if change=='wrong-action':raw['observedBackstep']['actionText']='Step Over'
                if change=='wrong-from':raw['observedBackstep']['fromCheckpoint']='other'
                if change=='wrong-to':raw['observedBackstep']['toSourceLine']=24
                if change=='same-line':raw['observedBackstep']['sourceLine']=23
                if change=='bool-line':raw['observedBackstep']['sourceLine']=True
                if change=='wrong-arrival':raw['sourceLine']=24
                with self.assertRaises(ValueError):course.validate_capture_mode(self.figure,raw)
    def test_forward_and_presentation_captures_cannot_claim_reverse_history(self):
        with self.assertRaises(ValueError):course.validate_capture_mode({},self.raw)
        with self.assertRaises(ValueError):course.validate_capture_mode({}, {'observedBackstep':self.raw['observedBackstep']})
        figure=dict(self.figure,captureMode='editor');raw=dict(self.raw,mode='editor')
        with self.assertRaises(ValueError):course.validate_capture_mode(figure,raw)

if __name__=='__main__':unittest.main()
