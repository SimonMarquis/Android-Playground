package fr.smarquis.playground.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings

internal class PlaygroundSettingsPlugin : Plugin<Settings> {
    override fun apply(settings: Settings) {
        settings.configureFeaturePreviews()
        settings.checkJavaVersion()
        settings.configureVersionOverrides()
        settings.configureBuildScan()
        settings.configureProjectLifecycle()
        settings.logBuildVersions()
    }
}
