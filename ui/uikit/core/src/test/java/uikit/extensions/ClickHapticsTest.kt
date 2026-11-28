package uikit.extensions

import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClickHapticsTest {
    @Test
    fun disabledPreferenceBlocksFeedback() {
        assertFalse(ClickHaptics.clickHapticAllowed(false, true, true))
    }

    @Test
    fun viewOrSystemSettingBlocksFeedback() {
        assertFalse(ClickHaptics.clickHapticAllowed(true, false, true))
        assertFalse(ClickHaptics.clickHapticAllowed(true, true, false))
    }

    @Test
    fun enabledPreferenceAllowsFeedback() {
        assertTrue(ClickHaptics.clickHapticAllowed(true, true, true))
    }

	@Test
	fun strengthMapsToDistinctEffectsAndClamps() {
		assertEquals(40, ClickHaptics.clickHapticAmplitude(0))
		assertEquals(40, ClickHaptics.clickHapticAmplitude(1))
		assertEquals(130, ClickHaptics.clickHapticAmplitude(2))
		assertEquals(255, ClickHaptics.clickHapticAmplitude(3))
		assertEquals(255, ClickHaptics.clickHapticAmplitude(99))
		assertEquals(20L, ClickHaptics.CLICK_HAPTIC_DURATION_MS)
	}
}
