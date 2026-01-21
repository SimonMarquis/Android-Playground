package fr.smarquis.playground.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.DeviceConfig.Companion.PIXEL_6_PRO
import app.cash.paparazzi.Paparazzi
import fr.smarquis.playground.core.ui.Theme.Dark
import fr.smarquis.playground.core.ui.Theme.Light

public fun screenshotRule(
    device: DeviceConfig = PIXEL_6_PRO,
): Paparazzi = Paparazzi(
    deviceConfig = device,
    useDeviceResolution = true,
    maxPercentDifference = 0.001,
)

public operator fun Paparazzi.invoke(
    theme: Theme = Light,
    composable: @Composable () -> Unit,
): Unit = snapshot {
    CompositionLocalProvider(LocalInspectionMode provides true) {
        AndroidPlaygroundTheme(
            darkTheme = theme == Dark,
            dynamicColor = false,
            content = composable
        )
    }
}

@Suppress("unused")
public enum class Theme { Light, Dark }
