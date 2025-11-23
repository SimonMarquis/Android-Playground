package fr.smarquis.playground.buildlogic

import fr.smarquis.playground.buildlogic.dsl.apply
import fr.smarquis.playground.buildlogic.dsl.assign
import fr.smarquis.playground.buildlogic.dsl.configure
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradleSubplugin
import org.jetbrains.kotlin.gradle.plugin.KotlinAndroidPluginWrapper

internal class PlaygroundAndroidComposePlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        apply<KotlinAndroidPluginWrapper>()
        apply<ComposeCompilerGradleSubplugin>()
        apply<PlaygroundAndroidBasePlugin>()

        android {
            buildFeatures.compose = true
        }

        configureComposeCompilerMetrics()

        dependencies.add("implementation", target.libs.`androidx-compose-foundations`)
        dependencies.add("implementation", target.libs.`androidx-compose-runtime`)
        dependencies.add("implementation", target.libs.`androidx-compose-ui-tooling-preview`)
        dependencies.add("debugImplementation", target.libs.`androidx-compose-ui-test-manifest`)
        dependencies.add("debugImplementation", target.libs.`androidx-compose-ui-tooling`)
        dependencies.add("lintChecks", target.libs.`slack-compose-lint`)
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
