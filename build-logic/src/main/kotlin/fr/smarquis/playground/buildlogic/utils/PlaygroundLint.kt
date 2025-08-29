package fr.smarquis.playground.buildlogic.utils

import com.android.build.api.dsl.Lint
import fr.smarquis.playground.buildlogic.PlaygroundProperties
import fr.smarquis.playground.buildlogic.androidExtension
import fr.smarquis.playground.buildlogic.capitalized
import fr.smarquis.playground.buildlogic.dsl.apply
import fr.smarquis.playground.buildlogic.dsl.getByType
import fr.smarquis.playground.buildlogic.isAndroidTest
import fr.smarquis.playground.buildlogic.libs
import fr.smarquis.playground.buildlogic.playground
import fr.smarquis.playground.buildlogic.versions
import org.gradle.api.Project
import org.gradle.language.base.plugins.LifecycleBasePlugin

/**
 * Inspired by https://github.com/slackhq/foundry
 */
internal object PlaygroundLint {
    private const val CI_LINT_TASK_NAME = "ciLint"
    private const val LOG = "PlaygroundLint:"

    fun configureSubproject(project: Project) = with(project) {
        pluginManager.withPlugin("com.android.base") {
            if (project.isAndroidTest) return@withPlugin // Android Test modules are special, SourceSet with name 'main' not found...
            configureDependencies()
            createAndroidCiLintTask()
        }
        pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
            apply(plugin = "com.android.lint")
            configureDependencies()
            createJvmCiLintTask()
        }
    }

    private fun Project.createJvmCiLintTask() = afterEvaluate {
        logger.debug("{} Creating CI lint tasks for project '{}'", LOG, this)

        val lint = tasks.named("lint")
        registerCiLintTask(name = "lintDebug", dependency = lint, disabled = true)
        registerCiLintTask(name = "lintRelease", dependency = lint, disabled = true)
        configureLintTask(extensions.getByType())
        val ciLint = registerCiLintTask(name = CI_LINT_TASK_NAME, dependency = lint, disabled = true)
        PlaygroundGlobalCi.addToGlobalCi(project, ciLint)
    }

    private fun Project.createAndroidCiLintTask() = androidExtension.finalizeDsl { extension ->
        val variant = playground().ciLintVariant.get().capitalized()
        logger.debug("{} Creating CI lint tasks for project '{}' and variant '{}'", LOG, this, variant)
        configureLintTask(extension.lint)
        val ciLint = registerCiLintTask(name = CI_LINT_TASK_NAME, dependency = "lint${variant}")
        PlaygroundGlobalCi.addToGlobalCi(project, ciLint)
    }

    private fun Project.registerCiLintTask(
        name: String,
        dependency: Any, // String, Provider<Task>, etc.
        disabled: Boolean = true,
    ) = tasks.register(name) {
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        dependsOn(dependency)
        if (disabled) enabled = false
    }

    private fun Project.configureDependencies() {
        dependencies.add("lintChecks", libs.`android-security-lint`)
        dependencies.add("lintChecks", project(":lint"))
    }

    private fun Project.configureLintTask(
        lint: Lint,
        properties: PlaygroundProperties = playground(),
    ) = lint.apply {
        targetSdk = versions.targetSdk.toString().toInt()

        abortOnError = true
        warningsAsErrors = properties.lintWarningsAsErrors

        explainIssues = true
        textReport = true
        xmlReport = true
        htmlReport = true
        sarifReport = true

        checkDependencies = true
        checkReleaseBuilds = false
        absolutePaths = true

        enable += "StopShip"
        disable += "AndroidGradlePluginVersion"
        disable += "GradleDependency"
        disable += "NewerVersionAvailable"
        disable += "ObsoleteLintCustomCheck"
        warning += "OldTargetApi"

        lintConfig = isolated.rootProject.projectDirectory.dir(".config").file("lint.xml").asFile
        baseline = isolated.rootProject.projectDirectory.dir(".config").file("lint-baseline.xml").asFile
    }

}
