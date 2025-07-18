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
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.gradle.language.base.plugins.LifecycleBasePlugin

/**
 * Inspired by https://github.com/slackhq/foundry
 */
internal object PlaygroundLint {
    private const val GLOBAL_CI_LINT_TASK_NAME = "globalCiLint"
    private const val CI_LINT_TASK_NAME = "ciLint"
    private const val LOG = "PlaygroundLint:"

    fun configureRootProject(project: Project): TaskProvider<Task> =
        project.tasks.register(GLOBAL_CI_LINT_TASK_NAME) {
            group = LifecycleBasePlugin.VERIFICATION_GROUP
            description = "Global lifecycle task to run all ciLint tasks."
        }.also {
            PlaygroundGlobalCi.addToGlobalCi(project, it)
        }

    fun configureSubproject(project: Project) = with(project) {
        val globalTask = rootProject.tasks.named(GLOBAL_CI_LINT_TASK_NAME)
        pluginManager.withPlugin("com.android.base") {
            if (project.isAndroidTest) return@withPlugin // Android Test modules are special, SourceSet with name 'main' not found...
            configureDependencies()
            createAndroidCiLintTask(globalTask)
        }
        pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
            apply(plugin = "com.android.lint")
            configureDependencies()
            createJvmCiLintTask(globalTask)
        }
    }

    private fun Project.createJvmCiLintTask(
        globalTask: TaskProvider<Task>,
    ) = afterEvaluate {
        logger.debug("{} Creating CI lint tasks for project '{}'", LOG, this)

        val lint = tasks.named("lint")
        registerCiLintTask(name = "lintDebug", dependency = lint, disabled = true)
        registerCiLintTask(name = "lintRelease", dependency = lint, disabled = true)
        val ciLint = registerCiLintTask(name = CI_LINT_TASK_NAME, dependency = lint, disabled = true)
        globalTask.configure { dependsOn(ciLint) }
        configureLintTask(extensions.getByType())
    }

    private fun Project.createAndroidCiLintTask(
        globalTask: TaskProvider<Task>,
    ) = androidExtension.finalizeDsl { extension ->
        val variant = playground().ciLintVariant.get().capitalized()
        logger.debug("{} Creating CI lint tasks for project '{}' and variant '{}'", LOG, this, variant)
        val ciLint = registerCiLintTask(
            name = CI_LINT_TASK_NAME,
            dependency = "lint${variant}",
        )
        globalTask.configure { dependsOn(ciLint) }
        configureLintTask(extension.lint)
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
