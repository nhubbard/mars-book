"""Exercise separate build processes sharing one generated auxiliary tree."""
import errno
from pathlib import Path
import select
import subprocess
import sys
import tempfile
import types
import unittest
from unittest.mock import Mock, patch

import book


class BuildLockTests(unittest.TestCase):
    @unittest.skipIf(sys.platform == 'win32', 'Pipe readiness uses the POSIX test host')
    def test_two_processes_cannot_enter_shared_build_state_at_once(self):
        child = '''import sys
from pathlib import Path
import book
book.ROOT = Path(sys.argv[1])
def compile(entry):
    print("entered", flush=True)
    sys.stdin.readline()
    return entry
book._build_locked = compile
print("attempting", flush=True)
book.build("chapter")
'''
        with tempfile.TemporaryDirectory() as folder:
            args = [sys.executable, '-c', child, folder]
            processes = []
            try:
                first = subprocess.Popen(args, cwd=book.ROOT / 'tools', stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE, bufsize=0)
                processes.append(first)
                self.assertEqual(b'attempting', first.stdout.readline().strip())
                self.assertEqual(b'entered', first.stdout.readline().strip())
                second = subprocess.Popen(args, cwd=book.ROOT / 'tools', stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE, bufsize=0)
                processes.append(second)
                self.assertEqual(b'attempting', second.stdout.readline().strip())
                self.assertEqual([], select.select([second.stdout], [], [], 0.2)[0])
                first.stdin.write(b'\n'); first.stdin.flush()
                self.assertEqual(0, first.wait(timeout=5))
                self.assertTrue(select.select([second.stdout], [], [], 5)[0])
                self.assertEqual(b'entered', second.stdout.readline().strip())
                second.stdin.write(b'\n'); second.stdin.flush()
                self.assertEqual(0, second.wait(timeout=5))
            finally:
                for process in processes:
                    if process.poll() is None:
                        process.terminate()
                    process.communicate(timeout=5)

    def test_failed_build_releases_lock_for_the_next_entry(self):
        with tempfile.TemporaryDirectory() as folder:
            with patch.object(book, 'ROOT', Path(folder)), patch.object(book, '_build_locked', side_effect=[ValueError('compile failed'), 'next.pdf']) as compile:
                with self.assertRaisesRegex(ValueError, 'compile failed'):
                    book.build('first')
                self.assertEqual('next.pdf', book.build('second'))
                self.assertEqual(['first', 'second'], [call.args[0] for call in compile.call_args_list])

    def test_windows_contention_retries_but_other_errors_propagate(self):
        module = types.SimpleNamespace(LK_NBLCK=1, locking=Mock(side_effect=[OSError(errno.EACCES, 'busy'), None]))
        with tempfile.TemporaryDirectory() as folder:
            with patch.dict(sys.modules, msvcrt=module), patch.object(book.sys, 'platform', 'win32'), patch('time.sleep') as pause:
                with book.build_lock(Path(folder)):
                    self.assertEqual(2, module.locking.call_count)
                pause.assert_called_once_with(0.1)
                module.locking.side_effect = OSError(errno.EBADF, 'invalid descriptor')
                with self.assertRaises(OSError):
                    with book.build_lock(Path(folder)):
                        self.fail('Acquired an invalid descriptor')
