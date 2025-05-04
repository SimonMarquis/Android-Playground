package fr.smarquis.playground.buildlogic

import io.github.cdsap.agp.artifacts.AndroidArtifactsInfoPlugin
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.provideDelegate
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

internal class PlaygroundAndroidBasePlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        apply(plugin = "org.jetbrains.kotlin.android")
        apply<PlaygroundBasePlugin>()
        apply<AndroidArtifactsInfoPlugin>()
        configureKotlin<KotlinAndroidProjectExtension>()

        androidBase {
            namespace = "fr.smarquis.playground." +
                path.split("""\W""".toRegex()).drop(1).distinct().joinToString(separator = ".").lowercase()
            resourcePrefix = "playground_" +
                path.split("""\W""".toRegex()).drop(1).distinct().joinToString(separator = "_").lowercase() + "_"

            compileSdkVersion(versions.compileSdk.toString().toInt())

            defaultConfig {
                minSdk = versions.minSdk.toString().toInt()
                targetSdk = versions.targetSdk.toString().toInt()
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }

            testOptions {
                animationsDisabled = true
            }

            packagingOptions {
                resources {
                    excludes += "/META-INF/{AL2.0,LGPL2.1}"
                }
            }

            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_11
                targetCompatibility = JavaVersion.VERSION_11
                isCoreLibraryDesugaringEnabled = true
            }
            buildTypes.configureEach {
                vcsInfo.include = false
            }
        }

        val coreLibraryDesugaring by configurations
        dependencies {
            coreLibraryDesugaring(libs.`android-desugarJdkLibs`.get())
        }
    }

}
