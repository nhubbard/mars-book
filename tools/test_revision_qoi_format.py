"""Contract boundaries that a successful image preview cannot establish."""
import struct
import unittest

from revision_qoi_format import decode, END


def stream(payload, width=1, height=1, channels=4, colorspace=0):
    return b'qoif' + struct.pack('>IIBB', width, height, channels, colorspace) + payload + END


class RevisionQoiFormatTests(unittest.TestCase):
    def test_predictor_and_initial_cache_have_different_alpha(self):
        self.assertEqual(decode(stream(b'\xc0')).rgba, bytes((0, 0, 0, 255)))
        self.assertEqual(decode(stream(b'\x00')).rgba, bytes(4))

    def test_run_inserts_predictor_into_cache(self):
        self.assertEqual(decode(stream(b'\xc0\x35', width=2)).rgba, bytes((0, 0, 0, 255))*2)

    def test_alpha_is_retained_with_descriptive_three_channel_flag(self):
        for channels in (3, 4):
            for space in (0, 1):
                image = decode(stream(b'\xff\x07\x06\x05\x80', channels=channels, colorspace=space))
                self.assertEqual((image.channels, image.colorspace, image.rgba), (channels, space, bytes((7, 6, 5, 128))))

    def test_rgb_keeps_alpha_from_previous_rgba(self):
        image = decode(stream(bytes.fromhex('ff01020380 fe040506'), width=2))
        self.assertEqual(image.rgba, bytes.fromhex('01020380 04050680'))

    def test_full_byte_tags_precede_run_classification(self):
        self.assertEqual(decode(stream(bytes.fromhex('feff0000'))).rgba, bytes.fromhex('ff0000ff'))
        self.assertEqual(decode(stream(bytes.fromhex('ff01020304'))).rgba, bytes.fromhex('01020304'))

    def test_difference_bias_and_modular_wrap(self):
        self.assertEqual(decode(stream(bytes.fromhex('feff0000 7f'), width=2)).rgba,
                         bytes.fromhex('ff0000ff 000101ff'))
        self.assertEqual(decode(stream(bytes.fromhex('fe010101 40'), width=2)).rgba,
                         bytes.fromhex('010101ff ffffffff'))

    def test_luma_residual_is_relative_to_green(self):
        self.assertEqual(decode(stream(bytes.fromhex('7f a597'), width=2)).rgba,
                         bytes.fromhex('010101ff 070605ff'))

    def test_hash_collision_replaces_the_old_color(self):
        self.assertEqual(decode(stream(bytes.fromhex('c0 fe400000 35'), width=3)).rgba,
                         bytes.fromhex('000000ff 400000ff 400000ff'))

    def test_index_history_is_not_cache_contents(self):
        with self.assertRaises(ValueError):
            decode(stream(bytes.fromhex('00 00'), width=2))
        self.assertEqual(decode(stream(bytes.fromhex('00 c0 00'), width=3)).rgba, bytes(12))

    def test_run_limit_and_remaining_extent(self):
        self.assertEqual(len(decode(stream(b'\xfd', width=62)).rgba), 62*4)
        self.assertEqual(len(decode(stream(b'\xfd\xc0', width=63)).rgba), 63*4)
        with self.assertRaises(ValueError):
            decode(stream(b'\xc1'))

    def test_marker_cannot_supply_missing_operands(self):
        for payload in (bytes.fromhex('fe0102'), bytes.fromhex('ff010203'), bytes.fromhex('a0')):
            with self.subTest(payload=payload), self.assertRaises(ValueError):
                decode(stream(payload))

    def test_complete_pixel_count_marker_and_exact_file_extent(self):
        good = stream(b'\xc0')
        for data in (good[:-1], good[:-1]+b'\0', good+b'\0', stream(b'\xc0\xc0'), stream(b'\xc0', width=2)):
            with self.subTest(data=data), self.assertRaises(ValueError):
                decode(data)

    def test_header_domain_and_unsigned_dimensions(self):
        for width, height, channels, space in ((0,1,4,0),(65,1,4,0),(0xffffffff,1,4,0),
                                               (1,0,4,0),(1,65,4,0),(1,1,2,0),(1,1,5,0),(1,1,4,2)):
            with self.subTest(values=(width,height,channels,space)), self.assertRaises(ValueError):
                decode(stream(b'\xc0',width,height,channels,space))
        with self.assertRaises(ValueError):
            decode(b'bad!' + stream(b'\xc0')[4:])

    def test_file_count_bounds_are_rejected(self):
        for data in (b'', bytes(22), stream(b'\xc0')+bytes(20503)):
            with self.subTest(size=len(data)), self.assertRaises(ValueError):
                decode(data)
