package fr.smarquis.playground.buildlogic

import app.cash.licensee.LicenseeExtension
import app.cash.licensee.LicenseePlugin
import app.cash.licensee.SpdxId
import com.android.build.api.dsl.ApkSigningConfig
import com.android.build.gradle.AppPlugin
import fr.smarquis.playground.buildlogic.dsl.apply
import fr.smarquis.playground.buildlogic.dsl.assign
import fr.smarquis.playground.buildlogic.dsl.configure
import fr.smarquis.playground.buildlogic.utils.PlaygroundGlobalCi
import fr.smarquis.playground.buildlogic.utils.computeVersionCode
import fr.smarquis.playground.buildlogic.utils.computeVersionName
import fr.smarquis.playground.buildlogic.utils.versionProperties
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project

internal class PlaygroundAndroidApplicationPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        apply<AppPlugin>()
        apply<PlaygroundAndroidBasePlugin>()
        apply<LicenseePlugin>()

        androidApplication {
            defaultConfig {
                targetSdk = versions.targetSdk.toString().toInt()
                androidResources {
                    localeFilters += "en"
                }
                val versionProperties = versionProperties().asFile.loadAsProperties()
                versionCode = versionProperties.computeVersionCode()
                versionName = versionProperties.computeVersionName()
            }
            signingConfigs {
                named("debug") {
                    configure(target, this@signingConfigs)
                }
                register("release") {
                    configure(target, this@signingConfigs)
                }
            }
            buildTypes {
                debug {
                    applicationIdSuffix = ".debug"
                    signingConfig = signingConfigs.getByName("debug")
                    isDebuggable = true
                    proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
                }
                release {
                    signingConfig = signingConfigs.getByName("release")
                    isMinifyEnabled = playground().isMinifyEnabled
                    isShrinkResources = isMinifyEnabled
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

private fun ApkSigningConfig.configure(
    project: Project,
    container: NamedDomainObjectContainer<out ApkSigningConfig>,
) {
    val root = project.isolated.rootProject.projectDirectory
    val properties = root.file("signing/$name.properties").asFile.takeIf { it.exists() }?.loadAsProperties()
        ?: return initWith(container.getByName("debug"))
    keyAlias = properties.getProperty("keyAlias")
    keyPassword = properties.getProperty("keyPassword")
    storeFile = root.file("signing/$name.keystore").asFile
    storePassword = properties.getProperty("storePassword")
}
