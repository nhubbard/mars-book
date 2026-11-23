import copy
from dataclasses import FrozenInstanceError
import json
import unittest

import console_cases as checks


class ConsoleMarsBotTests(unittest.TestCase):
    def profile(self):
        return {'formatVersion': 5, 'cases': [{'name': 'east 16', 'input': '', 'stdout': '', 'instructionLimit': 100000,
            'marsBot': {'instructionsPerTick': 64, 'x': 16, 'y': 0, 'heading': 90, 'moving': False, 'penDown': False,
                        'trail': [{'x': 0, 'y': 0}, {'x': 16, 'y': 0}]}}]}

    def parse(self, profile):
        return checks.parse(json.dumps(profile).encode())

    def test_device_data_generates_actual_device_checks_and_preserves_order(self):
        profile = self.profile()
        case = self.parse(profile)[0]
        self.assertEqual(case.mars_bot, checks.MarsBotCheck(64, 16, 0, 90, False, False, ((0, 0), (16, 0))))
        with self.assertRaises(FrozenInstanceError):
            case.mars_bot.x = 1
        profile['cases'][0]['marsBot']['trail'][1]['x'] = 17
        self.assertEqual(case.mars_bot.trail[-1], (16, 0))
        script = checks.render((case,))
        self.assertLess(script.index('connectMarsBot(64)'), script.index('runToExit()'))
        self.assertIn('bot.expectState(16.0, 0.0, 90, moving = false, penDown = false)', script)
        self.assertIn('bot.expectTrail(0.0 to 0.0, 16.0 to 0.0)', script)
        profile = self.profile();profile['cases'][0]['marsBot']['trail'] = []
        self.assertIn('bot.expectTrail()', checks.render(self.parse(profile)))
        profile['cases'][0]['marsBot'].update(moving=True, penDown=True)
        self.assertIn('moving = true, penDown = true', checks.render(self.parse(profile)))
        del profile['cases'][0]['marsBot']
        self.assertIsNone(self.parse(profile)[0].mars_bot)
        self.assertNotIn('connectMarsBot', checks.render(self.parse(profile)))

    def test_legacy_versions_cannot_acquire_device_behavior_or_new_instruction_limits(self):
        for version in range(1, 5):
            profile = self.profile();profile['formatVersion'] = version
            with self.assertRaises(ValueError):self.parse(profile)
        for version in range(1, 6):
            profile = self.profile();profile['formatVersion'] = version;del profile['cases'][0]['marsBot']
            maximum = 3_000_000 if version >= 4 else 1_000_000
            profile['cases'][0]['instructionLimit'] = maximum
            self.assertEqual(maximum, self.parse(profile)[0].instruction_limit)
            profile['cases'][0]['instructionLimit'] = maximum + 1
            with self.assertRaises(ValueError):self.parse(profile)
        profile = self.profile();row = profile['cases'][0]
        profile['cases'] = [dict(copy.deepcopy(row), name=f'east {i}', instructionLimit=3_000_000) for i in range(4)]
        with self.assertRaises(ValueError):self.parse(profile)

    def test_shapes_unknown_fields_boolean_and_integral_domains_are_strict(self):
        original = self.profile()['cases'][0]['marsBot']
        invalid = [None, [], 0, True, 'bot']
        invalid += [{key: val for key, val in original.items() if key != missing} for missing in original]
        invalid += [dict(original, script='run this')]
        for field in ['instructionsPerTick', 'x', 'y', 'heading']:
            invalid += [dict(original, **{field: val}) for val in [None, [], True, '16', 16.0, -1, 10000000]]
        for field, val in [('instructionsPerTick', 0), ('instructionsPerTick', 1000001), ('x', 512), ('y', 512), ('heading', 360)]:
            invalid.append(dict(original, **{field: val}))
        for field in ['moving', 'penDown']:
            invalid += [dict(original, **{field: val}) for val in [None, [], 0, 1, 'false', {}]]
        for trail in [None, {}, 'trail', [None], [0], [{'x': 0}], [{'x': 0, 'y': 0, 'script': 'run this'}], [{'x': 0, 'y': 0}], [{'x': 0, 'y': 0}]*257]:
            invalid.append(dict(original, trail=trail))
        for field in ['x', 'y']:
            for val in [None, [], True, '0', -1, 512, 0.5]:
                point = dict(x=0, y=0);point[field] = val
                invalid.append(dict(original, trail=[point, {'x': 16, 'y': 0}]))
        for bad in invalid:
            with self.subTest(bad=repr(bad)[:150]), self.assertRaises(ValueError):
                checks.parse_mars_bot(bad)
        for rate in [1, 1000000]:
            valid = dict(original, instructionsPerTick=rate, x=511, y=511, heading=359, trail=[{'x': 511, 'y': 511}]*256)
            self.assertEqual(256, len(checks.parse_mars_bot(valid).trail))


if __name__ == '__main__':
    unittest.main()
