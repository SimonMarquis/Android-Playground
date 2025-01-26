package fr.smarquis.playground.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

internal class PlaygroundKspPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        apply(plugin = "com.google.devtools.ksp")
    }

}
