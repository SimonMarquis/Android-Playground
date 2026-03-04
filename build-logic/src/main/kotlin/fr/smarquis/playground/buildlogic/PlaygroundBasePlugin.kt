package fr.smarquis.playground.buildlogic

import fr.smarquis.playground.buildlogic.dsl.apply
import fr.smarquis.playground.buildlogic.utils.PlaygroundBadging
import fr.smarquis.playground.buildlogic.utils.PlaygroundDependencyAnalysis
import fr.smarquis.playground.buildlogic.utils.PlaygroundDependencyLocking
import fr.smarquis.playground.buildlogic.utils.PlaygroundGlobalCi
import fr.smarquis.playground.buildlogic.utils.PlaygroundGraph
import fr.smarquis.playground.buildlogic.utils.PlaygroundLint
import fr.smarquis.playground.buildlogic.utils.PlaygroundPlatforms
import fr.smarquis.playground.buildlogic.utils.PlaygroundTopology
import fr.smarquis.playground.buildlogic.utils.PlaygroundUnitTests
import org.gradle.StartParameter
import org.gradle.android.AndroidCacheFixPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.configuration.BuildFeatures
import javax.inject.Inject

internal abstract class PlaygroundBasePlugin : Plugin<Project> {

    @get:Inject
    abstract val buildFeatures: BuildFeatures

    @get:Inject
    abstract val startParameter: StartParameter

    override fun apply(target: Project) = with(target) {
        if (isJavaPlatform) return@with

        pluginManager.withPlugin("com.android.base") {
            apply<AndroidCacheFixPlugin>()
        }

        PlaygroundGlobalCi.configureSubproject(target)
        PlaygroundUnitTests.configureSubproject(target)
        PlaygroundLint.configureSubproject(target)
        PlaygroundBadging.configureProject(target)
        PlaygroundDependencyLocking.configureProject(target)
        PlaygroundPlatforms.configureProject(target)
        PlaygroundTopology.configureProject(target)
        PlaygroundGraph.configureProject(target, startParameter, buildFeatures)
        PlaygroundDependencyAnalysis.configureProject(target)
    }

}
