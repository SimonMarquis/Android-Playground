package fr.smarquis.playground.buildlogic

import org.gradle.api.initialization.Settings
import org.gradle.api.logging.Logging

internal fun Settings.configureVersionOverrides() {
    // Override Gradle Catalog versions:
    // ./gradlew --system-prop "libs_version_minCompileSdk=35"
    // ./gradlew -Dlibs_version_minCompileSdk=35
    val versions = providers.systemPropertiesPrefixedBy("libs_version_").get()
        .mapKeys { (key, _) -> key.removePrefix("libs_version_") }
    if (versions.isNotEmpty()) Logging.getLogger(PlaygroundSettingsPlugin::class.java).lifecycle("ℹ️ Overriding versions $versions")
    dependencyResolutionManagement.versionCatalogs.maybeCreate("libs").apply {
        versions.forEach { (key, value) -> version(key, value) }
    }
}
