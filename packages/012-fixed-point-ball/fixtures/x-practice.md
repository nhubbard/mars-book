# Extend one numeric policy at a time

<div class="MARSExercise">

Change the fractional precision Choose a different power-of-two scale. Derive the new raw position bound, velocity bound, nearest-rounding adjustment, and representable signed range. Convert the seed values rather than reusing their old raw integers. State which checks and worked traces need new expected values.

</div>

<div class="MARSExercise">

Render a trail as a separate experiment Define how long a previous point remains visible and how it is erased. Explain why that is a different frame contract from this chapter’s current-state image. Keep the same raw trajectory and publication numbering so the visual change can be studied independently.

</div>

<div class="MARSExercise">

Allow a larger step Give a velocity that crosses more than one wall in a step. Show why one reflection can fail, then derive a periodic-fold result with a specified outgoing direction at an exact contact. Bound all intermediate arithmetic before choosing word or doubleword instructions.

</div>
