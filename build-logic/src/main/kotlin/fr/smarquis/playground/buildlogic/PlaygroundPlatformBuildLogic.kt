package fr.smarquis.playground.buildlogic

import fr.smarquis.playground.buildlogic.dsl.apply
import fr.smarquis.playground.buildlogic.utils.PlaygroundPlatforms
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlatformPlugin

internal fun Project.configurePlaygroundPlatform() = with(this) {
        apply<JavaPlatformPlugin>()

        PlaygroundPlatforms.configurePlatformProject(this)
        PlaygroundPlatforms.logBillOfMaterials(this)
    }

