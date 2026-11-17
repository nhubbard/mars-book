# Capture status

These four light-theme captures are planned, not captured. The separate authoring
plugin must launch the reference, verify the selected execution and paused source
boundary, await the MarsBot view showing the expected position/controls/path, and save
an actual PNG and receipt. Callout anchors remain unset until that image exists.

The fourth recipe reaches `east_stopped` and backsteps exactly the movement-stop
instruction. At `east_stop` the position and trail are unchanged, but movement is enabled
again. No instructions complete during the ensuing pause, so the robot does not drift.
The model's backstep tests support this expectation; real IDE rendering still needs
verification. Keep final labels before exit to avoid assuming view retention after disposal.
