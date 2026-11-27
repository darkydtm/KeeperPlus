package com.tonapps.perps.trade

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PerpsRunningOperationsTest {

    @Test
    fun `operations are tracked until finished`() {
        val operations = PerpsRunningOperations()
        assertFalse("operation" in operations)
        operations.start("operation")
        assertTrue("operation" in operations)
        operations.finish("operation")
        assertFalse("operation" in operations)
    }
}
