package fr.smarquis.playground.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.LockMode.STRICT
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.get

internal class PlaygroundAndroidApplicationPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        apply(plugin = "com.android.application")
        apply<PlaygroundAndroidBasePlugin>()

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
                    signingConfig = signingConfigs["debug"]
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

        // Configure Gradle Dependency Locking https://docs.gradle.org/current/userguide/dependency_locking.html
        val isWriteDependencyLocks = gradle.startParameter.isWriteDependencyLocks
        tasks.register("dependencyLockState") {
            dependsOn("dependencies")
            doFirst {
                require(isWriteDependencyLocks) { "$path must be run from the command line with the `--write-locks` flag" }
            }
        }
        dependencyLocking {
            lockMode = STRICT
        }
        configurations.configureEach {
            if (name == "releaseRuntimeClasspath") resolutionStrategy.activateDependencyLocking()
        }
    }

}
