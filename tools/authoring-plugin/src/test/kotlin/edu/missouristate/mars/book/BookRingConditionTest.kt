package edu.missouristate.mars.book

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class BookRingConditionTest {
  @Test
  fun `only public ring publications select a bounded native operation condition`() {
    assertNull(bookRingCondition(false, "operation_ready", emptyMap()))
    assertNull(bookRingCondition(true, "advance_cursor", emptyMap()))
    for (index in 0L..13L) assertEquals(
        "\$s3 == $index",
        bookRingCondition(true, "operation_ready", mapOf("\$s3" to index)),
    )
    assertThrows<IllegalStateException> { bookRingCondition(true, "operation_ready", emptyMap()) }
    for (index in listOf(-1L, 14L)) assertThrows<IllegalArgumentException> {
      bookRingCondition(true, "operation_ready", mapOf("\$s3" to index))
    }
  }
}
