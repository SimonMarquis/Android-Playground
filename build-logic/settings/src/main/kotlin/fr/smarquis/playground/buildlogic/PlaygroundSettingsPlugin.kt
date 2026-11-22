package fr.smarquis.playground.buildlogic

import fr.smarquis.playground.buildlogic.PlaygroundSettingsPlugin.Companion.logger
import fr.smarquis.playground.buildlogic.utils.BuildCache
import fr.smarquis.playground.buildlogic.utils.Develocity
import fr.smarquis.playground.buildlogic.utils.Lifecycle
import fr.smarquis.playground.buildlogic.utils.VersionCatalog
import org.gradle.api.JavaVersion
import org.gradle.api.JavaVersion.VERSION_17
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.api.logging.Logging
import org.gradle.util.GradleVersion

internal class PlaygroundSettingsPlugin : Plugin<Settings> {
    override fun apply(settings: Settings) = with(settings) {
        // https://docs.gradle.org/current/userguide/configuration_cache.html#config_cache:stable
        enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
        // https://docs.gradle.org/9.6.1/release-notes.html#opt-into-gradle-10-behavior-by-disabling-implicit-lookup-in-parent-projects
        enableFeaturePreview("NO_IMPLICIT_LOOKUP_IN_PARENT_PROJECTS")

        Develocity.configure(this)
        BuildCache.configure(this)
        VersionCatalog.configure(this)
        Lifecycle.configure(this)

        checkJavaVersion()
        logBuildVersions()
    }

    companion object {
        val logger = Logging.getLogger(PlaygroundSettingsPlugin::class.java)
    }
}

private fun checkJavaVersion() = with(VERSION_17) {
    check(JavaVersion.current().isCompatibleWith(this)) {
        """
    This build requires JDK $this+ but it is currently using JDK ${JavaVersion.current()}.
    Java Home: [${System.getProperty("java.home")}]
    """.trimIndent()
    }
}

private fun logBuildVersions() {
    logger.lifecycle(
        """
        📦 Gradle: ${GradleVersion.current().version}
        📦 Java:   ${JavaVersion.current()}
        📦 Kotlin: ${KotlinVersion.CURRENT}
        """.trimIndent(),
    )
}
