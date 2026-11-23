import json
import unittest

from console_cases import parse, render
from marsbot_lesson_cases import profile


class MarsBotLessonCasesTest(unittest.TestCase):
    def test_square_preserves_original_three_cases_and_complete_actual_device_contract(self):
        cases = parse(json.dumps(profile()).encode())
        self.assertEqual([case.name for case in cases], ['side 16 clock 64', 'side 1 clock 64', 'side 63 clock 17'])
        for case, (side, rate) in zip(cases, [(16, 64), (1, 64), (63, 17)]):
            self.assertEqual((case.input, case.stdout, case.instruction_limit), ('', '', 100000))
            self.assertEqual(case.initial_memory, case.memory)
            self.assertEqual(bytes.fromhex(case.memory[0].bytes), side.to_bytes(4, 'little')+(0x13579bdf ^ (side*97+rate)).to_bytes(4, 'little'))
            self.assertEqual([(s.address, s.length, s.port) for s in case.stores], [(0x10010000, 8, None), (0xffff8030, 4, None), (0xffff8040, 4, None)])
            bot = case.mars_bot
            self.assertEqual((bot.instructions_per_tick, bot.x, bot.y, bot.heading, bot.moving, bot.pen_down), (rate, 0, 0, 0, False, False))
            self.assertEqual(bot.trail, ((0, 0), (side, 0), (side, side), (0, side), (0, 0)))
        script = render(cases)
        self.assertEqual(script.count('connectMarsBot('), 3)
        self.assertEqual(script.count('.expectNone()'), 9)
        self.assertNotIn('expectRegister', script)

    def test_east_profiles_retain_final_boundaries_without_claiming_the_old_midrun_observation(self):
        cases = parse(json.dumps(profile(False)).encode())
        self.assertEqual(len(cases), 16)
        self.assertEqual({(c.mars_bot.x, c.mars_bot.instructions_per_tick) for c in cases}, {(side, rate) for side in [1, 2, 16, 63] for rate in [8, 17, 64, 128]})
        for case in cases:
            bot = case.mars_bot
            self.assertEqual((bot.y, bot.heading, bot.moving, bot.pen_down), (0, 90, False, False))
            self.assertEqual(bot.trail, ((0, 0), (bot.x, 0)))
            self.assertEqual(bytes.fromhex(case.memory[0].bytes)[4:], bytes.fromhex('df9b5713'))
        # The existing hand-authored east checker retains its earlier stopped/pen-down assertion.
        self.assertNotIn('runUntil', render(cases))


if __name__ == '__main__':
    unittest.main()
