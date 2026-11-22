package fr.smarquis.playground.buildlogic.utils

import fr.smarquis.playground.buildlogic.PlaygroundSettingsPlugin.Companion.logger
import org.gradle.api.initialization.Settings

internal object VersionCatalog {

    /**
     * Override Gradle Catalog versions:
     * ```bash
     * ./gradlew --system-prop "libs_version_minCompileSdk=35"
     * ./gradlew -Dlibs_version_minCompileSdk=35
     * ```
     */
    fun configure(settings: Settings) = with(settings) {
        dependencyResolutionManagement.versionCatalogs.maybeCreate("libs").apply {
            providers.systemPropertiesPrefixedBy("libs_version_").get()
                .mapKeys { (key, _) -> key.removePrefix("libs_version_") }
                .also { if (it.isNotEmpty()) logger.lifecycle("ℹ️ Overriding versions $it") }
                .forEach { (key, value) -> version(key, value) }
        }
    }
}
