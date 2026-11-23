from copy import deepcopy
from dataclasses import FrozenInstanceError
import json
from pathlib import Path
import unittest

import console_cases as checks


class ConsoleMazeCaseTests(unittest.TestCase):
    def search(self):
        return dict(kind='search', walls='00' * 64, start=0, goal=63, fixtureIndex=65535)

    def parse(self, maze, version=6):
        return checks.parse(json.dumps(dict(formatVersion=version, cases=[dict(
            name='maze', input='', stdout='', instructionLimit=250000, maze=maze,
        )])).encode())[0]

    def test_native_data_and_generated_dsl_share_the_checker_without_a_reference_route(self):
        case = self.parse(self.search())
        self.assertEqual(checks.MazeSearchCheck('00' * 64, 0, 63, 65535), case.maze)
        with self.assertRaises(FrozenInstanceError): case.maze.start = 1
        script = checks.render((case,))
        self.assertIn('import edu.missouristate.mars.cli.grading.ConsoleMazeCheck', script)
        self.assertIn('ConsoleMazeCheck.Search("' + '00' * 64 + '", 0, 63, 65535).verify(this)', script)
        self.assertNotIn('runToExit()', script)
        self.assertIn('public ABI', script)
        for fixture in range(5):
            case = self.parse(dict(kind='renderer', fixture=fixture))
            self.assertEqual(checks.MazeRendererCheck(fixture), case.maze)
            self.assertIn(f'ConsoleMazeCheck.Renderer({fixture}).verify(this)', checks.render((case,)))
        for version in range(1, 6):
            with self.assertRaises(ValueError): self.parse(self.search(), version)

    def test_shipped_original_search_and_renderer_profiles_match_their_generated_checks(self):
        root = Path(__file__).resolve().parents[1] / 'revision/labs/checks'
        for kind, count, limit in [('search', 8, 250000), ('render', 5, 10000)]:
            cases = checks.load(root / f'maze-{kind}.cases.json')
            self.assertEqual(count, len(cases))
            self.assertEqual((root / f'maze-{kind}.kts').read_text(), checks.render(cases))
            self.assertTrue(all(case.input == case.stdout == '' and case.instruction_limit == limit for case in cases))
            if kind == 'search':
                self.assertEqual([(0, 9), (63, 63), (0, 63), (63, 0), (7, 8), (0, 9), (0, 56), (27, 63)],
                                 [(case.maze.start, case.maze.goal) for case in cases])
                self.assertEqual(list(range(8)), [case.maze.fixture_index for case in cases])
                self.assertEqual({7, 8}, {cell for cell in range(64) if cases[4].maze.walls[2 * cell:2 * cell + 2] == '00'})
                self.assertEqual({0, 9}, {cell for cell in range(64) if cases[5].maze.walls[2 * cell:2 * cell + 2] == '00'})
            else:
                self.assertEqual(list(range(5)), [case.maze.fixture for case in cases])

    def test_untrusted_maze_data_is_strictly_typed_and_bounded(self):
        for good in [self.search(), dict(kind='renderer', fixture=0)]:
            invalid = [None, [], 1, True, 'search', dict(good, predicate='run code')]
            invalid += [dict(good, kind=value) for value in [None, [], True, 'unknown']]
            for field in good:
                missing = deepcopy(good); del missing[field]; invalid.append(missing)
            for bad in invalid:
                with self.subTest(bad=bad), self.assertRaises(ValueError): self.parse(bad)
        for field, maximum in [('start', 63), ('goal', 63), ('fixtureIndex', 65535)]:
            for bad in [None, [], True, '1', 1.0, -1, maximum + 1]:
                with self.subTest(field=field, bad=bad), self.assertRaises(ValueError): self.parse(dict(self.search(), **{field: bad}))
            for value in [0, maximum]: self.parse(dict(self.search(), **{field: value}))
        for bad in [None, [], True, '1', 1.0, -1, 5]:
            with self.assertRaises(ValueError): self.parse(dict(kind='renderer', fixture=bad))
        for walls in [None, 5, '', '00' * 63, '00' * 63 + '02', '01' + '00' * 63, '00' * 63 + '01']:
            with self.assertRaises(ValueError): self.parse(dict(self.search(), walls=walls))


if __name__ == '__main__':
    unittest.main()
