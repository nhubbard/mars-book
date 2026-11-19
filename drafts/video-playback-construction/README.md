# Validate and play a reloaded animation

Steps 102–122 add twenty-one unfinished regions, twenty-one quizzes and forty-two
hints. Attach the earlier file codec with `attach.py --source file.asm --output
playback.asm`. Prepare data-only observations with `prepare.py s102-1 --source
playback.asm --output observe.asm`. Existing output files are refused. The old caller
and all earlier instructions remain available; no completed player is supplied.

The native chapter defines `video_now` and `video_play`. Validate the current loaded
file before clock or display effects. Seek once to the start, apply later records
sequentially and preserve all display intervals, including unchanged/final frames.
Use the original anchor for deadlines, request only positive remaining time, resample
after sleep, reject backward observed time and unsigned deadline overflow. Return
the actual accepted count even on a later failure; final context outputs change only
on complete success. Playback is streaming and cannot undo already presented pixels.

Deterministic checks use the opt-in MARS grader timing fixture and public syscall
boundaries. They do not establish actual host wakeup or IDE repaint timing. The new
caller performs the earlier file round trip, then validates the reloaded bytes again
inside playback. Final chapter feedback/distributions, manuscript integration, actual
light-theme IDE captures and independent accessibility review remain pending. H.263
is still planning only until chapters 33 and 34 are complete. Substantive editing
remains a separate future goal.
