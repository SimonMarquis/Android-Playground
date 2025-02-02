package fr.smarquis.playground.buildlogic

import fr.smarquis.playground.buildlogic.utils.PlaygroundBadging
import fr.smarquis.playground.buildlogic.utils.PlaygroundLint
import fr.smarquis.playground.buildlogic.utils.PlaygroundUnitTests
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Plugin to be applied on the root project only.
 */
internal class PlaygroundRootPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        require(target == target.rootProject) { "$this must be applied on the root project, but was applied on $target" }
        PlaygroundUnitTests.configureRootProject(target)
        PlaygroundLint.configureRootProject(target)
        PlaygroundBadging.configureRootProject(target)
    }

}
