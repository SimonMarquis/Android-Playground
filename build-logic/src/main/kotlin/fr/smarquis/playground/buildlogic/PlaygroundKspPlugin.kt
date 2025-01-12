package fr.smarquis.playground.buildlogic

import com.google.devtools.ksp.gradle.KspExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

internal class PlaygroundKspPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        apply(plugin = "com.google.devtools.ksp")

        extensions.configure<KspExtension> {
            arg("dagger.hilt.disableModulesHaveInstallInCheck", "true")
        }
    }

}
