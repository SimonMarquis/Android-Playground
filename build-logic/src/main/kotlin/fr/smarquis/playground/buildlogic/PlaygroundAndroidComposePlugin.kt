package fr.smarquis.playground.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.provideDelegate
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

internal class PlaygroundAndroidComposePlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        apply(plugin = "org.jetbrains.kotlin.android")
        apply(plugin = "org.jetbrains.kotlin.plugin.compose")
        apply<PlaygroundAndroidBasePlugin>()

        android {
            buildFeatures.compose = true
            testOptions.unitTests.isIncludeAndroidResources = true
        }

        configureComposeCompilerMetrics()

        val implementation by configurations
        val debugImplementation by configurations
        val lintChecks by configurations
        dependencies {
            implementation(libs.`androidx-compose-foundations`)
            implementation(libs.`androidx-compose-runtime`)
            implementation(libs.`androidx-compose-ui-tooling-preview`)
            debugImplementation(libs.`androidx-compose-ui-test-manifest`)
            debugImplementation(libs.`androidx-compose-ui-tooling`)
            lintChecks(libs.`slack-compose-lint`)
        }
    }

    /**
     * https://github.com/JetBrains/kotlin/blob/master/plugins/compose/design/compiler-metrics.md
     */
    private fun Project.configureComposeCompilerMetrics(
        properties: PlaygroundProperties = playground(),
    ) {
        extensions.configure<ComposeCompilerGradlePluginExtension> {
            fun String.relativeToRootProject() = isolated.rootProject.projectDirectory
                .dir("build")
                .dir(projectDir.toRelativeString(rootDir))
                .dir(this)

            if (properties.composeCompilerMetrics) {
                metricsDestination = "compose-metrics".relativeToRootProject()
            }
            if (properties.composeCompilerReports) {
                reportsDestination = "compose-reports".relativeToRootProject()
            }

            stabilityConfigurationFiles
                .add(isolated.rootProject.projectDirectory.dir(".config").file("compose_compiler_config.conf"))
        }
    }

}
