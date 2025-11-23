package fr.smarquis.playground.buildlogic

import com.android.build.gradle.LibraryPlugin
import fr.smarquis.playground.buildlogic.dsl.apply
import fr.smarquis.playground.buildlogic.utils.PlaygroundGlobalCi
import org.gradle.api.Plugin
import org.gradle.api.Project

internal class PlaygroundAndroidLibraryPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        apply<LibraryPlugin>()
        apply<PlaygroundAndroidBasePlugin>()
        androidLibrary {
            defaultConfig {
                consumerProguardFile("consumer-rules.pro")
                aarMetadata.minCompileSdk = versions.minSdk.toString().toInt()
            }
        }
        PlaygroundGlobalCi.addToGlobalCi(project, "checkLegacyAbi")
    }

}
