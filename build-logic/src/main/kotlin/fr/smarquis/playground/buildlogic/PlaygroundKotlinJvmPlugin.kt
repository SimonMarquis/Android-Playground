package fr.smarquis.playground.buildlogic

import fr.smarquis.playground.buildlogic.dsl.apply
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper

internal class PlaygroundKotlinJvmPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        apply<KotlinPluginWrapper>()
        apply<PlaygroundBasePlugin>()
        configureKotlin<KotlinJvmProjectExtension>()
    }

}
