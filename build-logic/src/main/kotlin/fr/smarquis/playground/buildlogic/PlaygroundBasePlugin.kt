package fr.smarquis.playground.buildlogic

import fr.smarquis.playground.buildlogic.utils.PlaygroundBadging
import fr.smarquis.playground.buildlogic.utils.PlaygroundDependencyLocking
import fr.smarquis.playground.buildlogic.utils.PlaygroundLint
import fr.smarquis.playground.buildlogic.utils.PlaygroundPlatforms
import fr.smarquis.playground.buildlogic.utils.PlaygroundUnitTests
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.withType

internal class PlaygroundBasePlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        if (isJavaPlatform) return@with

        pluginManager.withPlugin("com.android.base") {
            apply(plugin = "org.gradle.android.cache-fix")
        }

        PlaygroundUnitTests.configureSubproject(target)
        PlaygroundLint.configureSubproject(target)
        PlaygroundBadging.configureProject(target)
        PlaygroundDependencyLocking.configureProject(target)
        PlaygroundPlatforms.configureProject(target)

        configureReproducibleBuilds()
    }

    /**
     * https://docs.gradle.org/current/userguide/working_with_files.html#sec:reproducible_archives
     */
    private fun Project.configureReproducibleBuilds() {
        tasks.withType<AbstractArchiveTask>().configureEach {
            isPreserveFileTimestamps = false
            isReproducibleFileOrder = true
        }
    }

}
