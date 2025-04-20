package fr.smarquis.playground.feature.licenses

import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import fr.smarquis.playground.core.ui.Theme
import fr.smarquis.playground.core.ui.invoke
import fr.smarquis.playground.core.ui.screenshotRule
import org.junit.Rule
import org.junit.runner.RunWith
import kotlin.test.Test

@RunWith(TestParameterInjector::class)
class LicensesScreenshotTest(@TestParameter val theme: Theme) {

    @get:Rule
    val screenshot = screenshotRule()

    @Test
    fun loading() = screenshot(theme) { LicensesScreenContentLoadingPreview() }

    @Test
    fun error() = screenshot(theme) { LicensesScreenContentFailurePreview() }

    @Test
    fun content() = screenshot(theme) { LicensesScreenContentPreview() }

}
