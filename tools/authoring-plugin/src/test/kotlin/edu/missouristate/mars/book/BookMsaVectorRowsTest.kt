package edu.missouristate.mars.book

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class BookMsaVectorRowsTest {
  @Test
  fun `complete vector rendering retains both halves and rejects scalar or partial rows`() {
    val bits = "0x8000000000000001fffffffffffffffe"
    assertEquals(
        bits,
        lessonVectorValue(
            listOf("\$w4 (written) = ${bits.uppercase().replace("0X", "0x")}"),
            "\$w4",
        ),
    )
    assertEquals(bits, lessonVectorValue(listOf("\$w4 = $bits"), "\$w4"))
    for (rows in
        listOf(
            emptyList(),
            listOf("\$f4 = $bits"),
            listOf("\$w4 = 0xfffffffffffffffe"),
            listOf("\$w4 = 0x8000000000000001fffffffffffffffg"),
            listOf("\$w4 = -2"),
        )) {
      assertNull(lessonVectorValue(rows, "\$w4"))
    }
    assertThrows<IllegalStateException> {
      lessonVectorValue(listOf("\$w4 = $bits", "\$w4 (written) = $bits"), "\$w4")
    }
  }
}
