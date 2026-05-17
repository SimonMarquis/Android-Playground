package fr.smarquis.playground.buildlogic

import dev.zacsweers.metro.gradle.DiagnosticSeverity
import dev.zacsweers.metro.gradle.ExperimentalMetroGradleApi
import dev.zacsweers.metro.gradle.MetroGradleSubplugin
import dev.zacsweers.metro.gradle.MetroPluginExtension
import fr.smarquis.playground.buildlogic.dsl.apply
import fr.smarquis.playground.buildlogic.dsl.assign
import org.gradle.api.Plugin
import org.gradle.api.Project

internal class PlaygroundMetroPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        apply<MetroGradleSubplugin>()

        @OptIn(ExperimentalMetroGradleApi::class)
        extensions.configure<MetroPluginExtension>("metro") {
            contributesAsInject = true
            generateContributionProviders = true
            nonPublicContributionSeverity = DiagnosticSeverity.WARN
        }

        dependencies.add("implementation", libs.`metro-runtime`)
    }

}
