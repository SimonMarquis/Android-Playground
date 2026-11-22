package fr.smarquis.playground.buildlogic

import com.android.build.gradle.LibraryPlugin
import fr.smarquis.playground.buildlogic.dsl.apply
import org.gradle.api.Project

internal fun Project.configurePlaygroundAndroidLibrary() {
        apply<LibraryPlugin>()
        configurePlaygroundAndroidBase()
        androidLibrary {
            defaultConfig {
                aarMetadata.minCompileSdk = versions.minSdk.toString().toInt()
            }
        }
    }
