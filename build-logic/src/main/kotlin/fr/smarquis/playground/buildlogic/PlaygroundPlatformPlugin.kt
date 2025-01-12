package fr.smarquis.playground.buildlogic

import fr.smarquis.playground.buildlogic.utils.PlaygroundPlatforms
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlatformPlugin
import org.gradle.kotlin.dsl.apply

internal class PlaygroundPlatformPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        apply<JavaPlatformPlugin>()
        apply<PlaygroundBasePlugin>()

        PlaygroundPlatforms.configurePlatformProject(this)
        PlaygroundPlatforms.logBillOfMaterials(this)
    }

}
