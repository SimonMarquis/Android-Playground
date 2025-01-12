package fr.smarquis.playground.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

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
