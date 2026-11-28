package uikit.extensions

import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.view.HapticFeedbackConstants
import android.view.View

/** Optional feedback for ordinary clicks; semantic confirmation/rejection feedback is separate. */
object ClickHaptics {
    private const val PREFS = "click_haptics"
    private const val ENABLED = "enabled"
    private const val STRENGTH = "strength"

    fun getEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(ENABLED, false)

    fun setEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(ENABLED, enabled).apply()
    }

    fun getStrength(context: Context): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(STRENGTH, 2).coerceIn(1, 3)

    fun setStrength(context: Context, strength: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putInt(STRENGTH, strength.coerceIn(1, 3)).apply()
    }

    internal fun clickHapticAllowed(enabled: Boolean, viewEnabled: Boolean, systemEnabled: Boolean): Boolean =
        enabled && viewEnabled && systemEnabled

    internal fun clickHapticAmplitude(strength: Int): Int = when (strength.coerceIn(1, 3)) {
        1 -> 50
        3 -> 180
        else -> 100
    }

    fun perform(view: View) {
        val context = view.context
        if (!clickHapticAllowed(
                getEnabled(context),
                view.isHapticFeedbackEnabled,
                Settings.System.getInt(context.contentResolver, Settings.System.HAPTIC_FEEDBACK_ENABLED, 0) != 0,
            )
        ) return

        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && vibrator != null &&
            vibrator.hasVibrator() && vibrator.hasAmplitudeControl()
        ) {
            val amplitude = clickHapticAmplitude(getStrength(context))
            val effect = VibrationEffect.createOneShot(12, amplitude)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                vibrator.vibrate(effect, VibrationAttributes.Builder().setUsage(VibrationAttributes.USAGE_TOUCH).build())
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(effect, AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION).build())
            }
        } else {
            // The platform respects system haptic settings and device-specific feedback.
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        }
    }
}
