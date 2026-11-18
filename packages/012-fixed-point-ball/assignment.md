# Project contract

Use signed 32-bit raw integers with eight fractional bits: represented value = raw/256.
Normal simulation inputs are x and y in 0..1792 inclusive, vx and vy in -1024..1024
inclusive, steps in 0..32 inclusive, and round_mode in 0..1. Initial outward velocity
at a wall is allowed. Horizontal coordinates increase rightward and vertical downward.
The two raw walls are 0 and 1792 (pixel-center positions zero and seven).

Implement three independent function interfaces:

| Function | Arguments | Results |
| --- | --- | --- |
| advance_axis | a0=raw position, a1=raw velocity | v0=new position, v1=new velocity |
| render_ball | a0=full destination pointer, a1=x raw, a2=y raw, a3=mode | v0=pixel ID |
| add_fixed_probe | a0 and a1=correctly sign-extended signed word inputs | v0=sign-extended wrapped word sum, v1=exact signed 64-bit raw sum |

For advance_axis, set trial=position+velocity. If trial<=0, return (-trial,-velocity).
If trial>=1792, return (3584-trial,-velocity). Otherwise return (trial,velocity).
Exact contacts leave nonzero velocity inward. Zero stays zero. The published velocity
bound makes one reflection sufficient and prevents signed word overflow in this routine.

The renderer receives two nonnegative raw positions in the same 0..1792 domain. Mode
zero chooses floor(raw/256); mode one chooses floor((raw+128)/256), with half ties upward.
Return ID=8*row+column. Write a complete 64-word frame with 0x00ffffff at that ID and
zero elsewhere. Use the supplied pointer, including spare_frame or a relocated buffer.
The rendering store order and exact instruction count are not specified. The sum probe
has the broader full signed-word operand domain and deliberately exposes sums that do
not fit in a word; it is separate from the bounded motion update.

Each function preserves all 64 bits of s0..s7 and restores sp, returns to its supplied
ra, and produces no console output. Axis/probe preserve all provided static data.
Renderer may write only its destination frame among the provided objects. Stack use
and other scratch-register conventions are permitted under those preservation rules.

Preserve the provided word-aligned data ABI: frame[64], frame_guard, spare_frame[64],
spare_guard, the contiguous four seeds seed_x/seed_y/seed_vx/seed_vy, seed_guard, contiguous
steps/round_mode, options_guard, contiguous x_raw/y_raw/vx_raw/vy_raw/tick/pixel_id, and
state_guard. Keep frame at default static-data base 0x10010000 and frame_guard at
0x10010100 for Bitmap Display. Free-writing code may use different internal labels and
registers while retaining these public objects, function interfaces, and markers.

Normal main uses DEMO=0, reads inputs, publishes initial tick zero and every updated
state through the requested tick, then exits normally. At frame_ready, all four raw
state words, tick, pixel_id, and all 64 frame words must agree. Keep the supplied single
nop marker immediately before frame_continue; the scoped marker count must be steps+1
after exit. Input words, every guard, and spare_frame are read-only to normal main,
including writes later restored. Final state and frame remain correct after exit.
Never replace model coordinates with rounded display cells.

DEMO=1 selects the separate supplied overflow demonstration. Stop at overflow_ready to
inspect both results before the exit service overwrites v0. The intentional ADD fault
is a temporary experiment; restore ADDU and DEMO=0 before submission. Public checks call
all kernels independently and exercise normal main. They do not require a reference
whole-program instruction total, scratch register, or particular rendering loop.

The book and text guides support guided and free-writing modes. Instructor progression
and hint controls are future trainer policies. CLI checks and author-verified cumulative
programs are available now. Actual light-theme IDE screenshots remain planned.
