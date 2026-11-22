package fr.smarquis.playground.buildlogic

import org.gradle.api.JavaVersion
import org.gradle.api.initialization.Settings
import org.gradle.api.logging.Logging
import org.gradle.util.GradleVersion

internal fun Settings.logBuildVersions() {
    Logging.getLogger(PlaygroundSettingsPlugin::class.java).lifecycle(
        """
        📦 Gradle: ${GradleVersion.current().version}
        📦 Java:   ${JavaVersion.current()}
        📦 Kotlin: ${KotlinVersion.CURRENT}
        """.trimIndent(),
    )
}
