package fr.smarquis.playground.buildlogic

import dev.zacsweers.metro.gradle.DiagnosticSeverity
import dev.zacsweers.metro.gradle.MetroGradleSubplugin
import dev.zacsweers.metro.gradle.MetroPluginExtension
import fr.smarquis.playground.buildlogic.dsl.apply
import fr.smarquis.playground.buildlogic.dsl.assign
import org.gradle.api.Project

internal fun Project.configurePlaygroundMetro() {
        apply<MetroGradleSubplugin>()

        extensions.configure<MetroPluginExtension>("metro") {
            contributesAsInject = true
            generateContributionProviders = true
            desugaredProviderSeverity = DiagnosticSeverity.WARN
            nonPublicContributionSeverity = DiagnosticSeverity.WARN
            publicScopedProviderSeverity = DiagnosticSeverity.WARN
            unusedGraphInputsSeverity = DiagnosticSeverity.WARN
        }

        dependencies.add("api", libs.`metro-runtime`)
    }
