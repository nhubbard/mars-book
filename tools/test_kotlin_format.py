import unittest

from kotlin_format import format_kotlin


class KotlinFormatTest(unittest.TestCase):
    def test_script_statements_expand_without_changing_literal_code(self):
        source = 'case("semi;colon"){val text="x;y";expectOutput(text);expectRegister("v0",1)}\n'
        actual = format_kotlin(source)
        self.assertIn('"semi;colon"', actual)
        self.assertIn('"x;y"', actual)
        self.assertIn('  val text = "x;y"\n  expectOutput(text)\n', actual)
        self.assertEqual(actual, format_kotlin(actual))

    def test_regular_source_preserves_raw_literal_and_separates_functions(self):
        source = 'fun first()="""a;b\n c;d"""\nfun second(x:Int):Int{return x+1}\n'
        actual = format_kotlin(source, script=False)
        self.assertIn('"""a;b\n c;d"""', actual)
        self.assertIn("\n\nfun second(x: Int): Int {\n", actual)
        self.assertEqual(actual, format_kotlin(actual, script=False))

    def test_composition_fragments_keep_imports_used_by_later_cases(self):
        fragment = "import java.nio.ByteOrder\nfun example()=1\n"
        preserved = format_kotlin(fragment, preserve_imports=True)
        self.assertIn("import java.nio.ByteOrder", preserved)
        self.assertNotIn("import java.nio.ByteOrder", format_kotlin(fragment))
        composed = format_kotlin(preserved + "val order=ByteOrder.BIG_ENDIAN\n")
        self.assertIn("import java.nio.ByteOrder", composed)
        self.assertEqual(composed, format_kotlin(composed))

    def test_rejected_source_does_not_corrupt_the_next_request(self):
        with self.assertRaisesRegex(ValueError, "KTFMT rejected"):
            format_kotlin("fun bad( {")
        self.assertEqual("val recovered = 7\n", format_kotlin("val recovered=7"))


if __name__ == "__main__":
    unittest.main()
