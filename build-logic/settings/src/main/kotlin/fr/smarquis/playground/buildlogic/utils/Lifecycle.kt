package fr.smarquis.playground.buildlogic.utils

import fr.smarquis.playground.buildlogic.PlaygroundExtension
import org.gradle.api.initialization.Settings

internal object Lifecycle {
    @Suppress("UnstableApiUsage")
    fun configure(settings: Settings) = with(settings) {
        gradle.lifecycle.beforeProject {
            logger.lifecycle("🏗️ Configuring $this")
            pluginManager.apply("base")
            pluginManager.apply("playground.project")
        }

        gradle.lifecycle.afterProject {
            if (isolated == isolated.rootProject) return@afterProject
            if (!buildFile.exists()) return@afterProject
            // Ensures each module selects its Playground configuration.
            require(extensions.getByType(PlaygroundExtension::class.java).configured) {
                "👮 $this is missing a module type in playground { }"
            }
        }
    }
}
