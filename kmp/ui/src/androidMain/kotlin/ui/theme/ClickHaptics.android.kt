package ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import uikit.extensions.ClickHaptics

@Composable
internal actual fun rememberClickHaptic(): () -> Unit {
    val view = LocalView.current
    return remember(view) { { ClickHaptics.perform(view) } }
}
