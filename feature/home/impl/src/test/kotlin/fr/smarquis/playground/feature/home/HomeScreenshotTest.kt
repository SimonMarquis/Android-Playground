package fr.smarquis.playground.feature.home

import app.cash.paparazzi.DeviceConfig.Companion.PIXEL_6_PRO
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import fr.smarquis.playground.core.ui.Theme
import fr.smarquis.playground.core.ui.invoke
import fr.smarquis.playground.core.ui.screenshotRule
import org.junit.Rule
import org.junit.runner.RunWith
import kotlin.test.Test

@RunWith(TestParameterInjector::class)
class HomeScreenshotTest(@param:TestParameter val theme: Theme) {

    @get:Rule
    val screenshot = screenshotRule(
        // Artificially increase screenshot size to match the expected output
        PIXEL_6_PRO.run { copy(screenHeight = (screenHeight * 1.26).toInt()) },
    )

    @Test
    fun content() = screenshot(theme) { HomeScreenContentPreview() }

}
