package fr.smarquis.playground.buildlogic

import com.android.build.gradle.LintPlugin
import fr.smarquis.playground.buildlogic.dsl.apply
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper

internal fun Project.configurePlaygroundKotlinJvm() {
        apply<KotlinPluginWrapper>()
        apply<LintPlugin>()
        plugins.getPlugin(PlaygroundProjectPlugin::class.java).configureModule(this)
        configureKotlin<KotlinJvmProjectExtension>()
    }
