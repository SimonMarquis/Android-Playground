package fr.smarquis.playground.buildlogic

import com.android.build.gradle.LibraryPlugin
import fr.smarquis.playground.buildlogic.dsl.apply
import org.gradle.api.Plugin
import org.gradle.api.Project

internal class PlaygroundAndroidLibraryPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        apply<LibraryPlugin>()
        apply<PlaygroundAndroidBasePlugin>()
        androidLibrary {
            defaultConfig {
                aarMetadata.minCompileSdk = versions.minSdk.toString().toInt()
            }
        }
    }

}
