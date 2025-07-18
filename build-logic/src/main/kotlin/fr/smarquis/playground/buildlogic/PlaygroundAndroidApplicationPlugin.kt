package fr.smarquis.playground.buildlogic

import app.cash.licensee.LicenseeExtension
import app.cash.licensee.LicenseePlugin
import app.cash.licensee.SpdxId
import fr.smarquis.playground.buildlogic.dsl.apply
import fr.smarquis.playground.buildlogic.dsl.assign
import fr.smarquis.playground.buildlogic.dsl.configure
import fr.smarquis.playground.buildlogic.utils.PlaygroundGlobalCi
import org.gradle.api.Plugin
import org.gradle.api.Project

internal class PlaygroundAndroidApplicationPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        apply(plugin = "com.android.application")
        apply<PlaygroundAndroidBasePlugin>()
        apply<LicenseePlugin>()

        androidApplication {
            defaultConfig {
                targetSdk = versions.targetSdk.toString().toInt()
                androidResources {
                    localeFilters += "en"
                }
            }
            buildTypes {
                debug {
                    applicationIdSuffix = ".debug"
                    isDebuggable = true
                    proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
                }
                release {
                    isMinifyEnabled = playground().isMinifyEnabled
                    isShrinkResources = isMinifyEnabled
                    signingConfig = signingConfigs.getByName("debug")
                    proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
                    // https://github.com/Kotlin/kotlinx.coroutines#avoiding-including-the-debug-infrastructure-in-the-resulting-apk
                    packaging.resources.excludes += "DebugProbesKt.bin"
                }
            }

            dependenciesInfo {
                includeInApk = false
                includeInBundle = false
            }
        }

        extensions.configure<LicenseeExtension> {
            bundleAndroidAsset = true
            allow(SpdxId.Apache_20)
            allow(SpdxId.BSD_3_Clause)
            allow(SpdxId.MIT)
        }
        PlaygroundGlobalCi.addToGlobalCi(project, "assembleRelease")
        PlaygroundGlobalCi.addToGlobalCi(project, "licensee")
    }
}
