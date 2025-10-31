package fr.smarquis.playground.buildlogic

import fr.smarquis.playground.buildlogic.dsl.apply
import io.github.cdsap.agp.artifacts.AndroidArtifactsInfoPlugin
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

internal class PlaygroundAndroidBasePlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        apply<PlaygroundBasePlugin>()
        apply<AndroidArtifactsInfoPlugin>()
        configureKotlin<KotlinAndroidProjectExtension>()

        android {
            namespace = "fr.smarquis.playground." +
                path.split("""\W""".toRegex()).drop(1).distinct().joinToString(separator = ".").lowercase()
            resourcePrefix = "playground_" +
                path.split("""\W""".toRegex()).drop(1).distinct().joinToString(separator = "_").lowercase() + "_"

            compileSdk {
                version = release(versions.compileSdk.toString().toInt())
            }

            defaultConfig.minSdk = versions.minSdk.toString().toInt()
            defaultConfig.testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

            testOptions {
                animationsDisabled = true
            }

            packaging.resources {
                excludes += "/META-INF/{AL2.0,LGPL2.1}"
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

        dependencies.add("coreLibraryDesugaring", libs.`android-desugarJdkLibs`.get())
    }

}
