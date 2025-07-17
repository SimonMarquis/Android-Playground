package fr.smarquis.playground.buildlogic

import fr.smarquis.playground.buildlogic.dsl.apply
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

internal class PlaygroundKotlinJvmPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        apply(plugin = "org.jetbrains.kotlin.jvm")
        apply<PlaygroundBasePlugin>()
        configureKotlin<KotlinJvmProjectExtension>()
    }

}
