package fr.smarquis.playground.buildlogic

import fr.smarquis.playground.buildlogic.dsl.apply
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper

internal fun Project.configurePlaygroundKotlinJvm() = with(this) {
        apply<KotlinPluginWrapper>()
        apply<PlaygroundBasePlugin>()
        configureKotlin<KotlinJvmProjectExtension>()
    }

