package edu.missouristate.mars.book

import java.awt.image.BufferedImage

/** Model visibility alone cannot attest a real photograph when macOS denies capture access. */
internal fun requireBookLightPhotograph(image: BufferedImage): Map<String, Int> {
    var samples = 0
    var light = 0
    var dark = 0
    for (y in 0 until image.height step maxOf(1, image.height / 128)) {
        for (x in 0 until image.width step maxOf(1, image.width / 128)) {
            val rgb = image.getRGB(x, y)
            val red = rgb ushr 16 and 255
            val green = rgb ushr 8 and 255
            val blue = rgb and 255
            samples++
            if (minOf(red, green, blue) >= 200) light++
            if (maxOf(red, green, blue) <= 120) dark++
        }
    }
    check(light * 10 >= samples && dark * 1000 >= samples) {
        "The captured screen does not contain a legible light IDE view: $light light and $dark dark samples of $samples. Check screen-recording access and window visibility."
    }
    return mapOf("samples" to samples, "lightSamples" to light, "darkSamples" to dark)
}
