package edu.missouristate.mars.book

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class BookUtf8ConditionTest {
    @Test
    fun `only public UTF-8 publications select a bounded native operation condition`() {
        assertNull(bookUtf8Condition(false,"chunk_ready",emptyMap()))
        assertNull(bookUtf8Condition(true,"within_range",emptyMap()))
        for (index in 0L..3L) assertEquals("\$s3 == $index", bookUtf8Condition(true,"chunk_ready",mapOf("\$s3" to index)))
        assertThrows<IllegalStateException> { bookUtf8Condition(true,"chunk_ready",emptyMap()) }
        for (index in listOf(-1L,4L)) assertThrows<IllegalArgumentException> {
            bookUtf8Condition(true,"chunk_ready",mapOf("\$s3" to index))
        }
    }
}
