package fr.smarquis.playground.buildlogic

import fr.smarquis.playground.buildlogic.dsl.apply
import org.gradle.api.Plugin
import org.gradle.api.Project

internal class PlaygroundAndroidLibraryPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        apply(plugin = "org.jetbrains.kotlinx.binary-compatibility-validator")
        apply(plugin = "com.android.library")
        apply<PlaygroundAndroidBasePlugin>()
        androidLibrary {
            defaultConfig {
                consumerProguardFile("consumer-rules.pro")
                aarMetadata.minCompileSdk = versions.minSdk.toString().toInt()
            }
        }
    }

}
