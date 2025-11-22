package fr.smarquis.playground.buildlogic

import fr.smarquis.playground.buildlogic.dsl.apply
import fr.smarquis.playground.buildlogic.utils.PlaygroundGlobalCi
import org.gradle.api.Plugin
import org.gradle.api.Project

internal class PlaygroundAndroidLibraryPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        apply(plugin = "org.jetbrains.kotlinx.binary-compatibility-validator")
        apply(plugin = "com.android.library")
        apply<PlaygroundAndroidBasePlugin>()
        androidLibrary {
            defaultConfig {
                aarMetadata.minCompileSdk = versions.minSdk.toString().toInt()
            }
        }
        // PlaygroundGlobalCi.addToGlobalCi(project, "apiCheck")
    }

}
