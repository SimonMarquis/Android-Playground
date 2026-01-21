package fr.smarquis.playground.buildlogic

import fr.smarquis.playground.buildlogic.dsl.apply
import io.github.cdsap.agp.artifacts.AndroidArtifactsInfoPlugin
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinAndroidPluginWrapper

internal class PlaygroundAndroidBasePlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        apply<PlaygroundBasePlugin>()
        apply<AndroidArtifactsInfoPlugin>()
        configureKotlin<KotlinAndroidProjectExtension>()

        android {
            val parts = path.split("""\W""".toRegex()).drop(1).distinct().dropLastWhile { it == "impl" }
            namespace = "fr.smarquis.playground." + parts.joinToString(separator = ".").lowercase()
            resourcePrefix = "playground_" + parts.joinToString(separator = "_").lowercase() + "_"

            compileSdk {
                version = release(versions.compileSdk.toString().toInt())
            }

            defaultConfig.apply {
                minSdk = versions.minSdk.toString().toInt()
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }

            testOptions.animationsDisabled = true

            packaging.resources {
                excludes += "/META-INF/{AL2.0,LGPL2.1}"
            }

            compileOptions.apply {
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
