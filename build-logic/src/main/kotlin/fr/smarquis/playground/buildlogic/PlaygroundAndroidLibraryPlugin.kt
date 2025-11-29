package fr.smarquis.playground.buildlogic

import com.android.build.gradle.LibraryPlugin
import fr.smarquis.playground.buildlogic.dsl.apply
import fr.smarquis.playground.buildlogic.utils.PlaygroundGlobalCi
import kotlinx.validation.BinaryCompatibilityValidatorPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

internal class PlaygroundAndroidLibraryPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        apply<BinaryCompatibilityValidatorPlugin>()
        apply<LibraryPlugin>()
        apply<PlaygroundAndroidBasePlugin>()
        androidLibrary {
            defaultConfig {
                aarMetadata.minCompileSdk = versions.minSdk.toString().toInt()
            }
        }
        // PlaygroundGlobalCi.addToGlobalCi(project, "apiCheck")
    }

}
