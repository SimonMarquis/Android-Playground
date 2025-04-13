package fr.smarquis.playground.buildlogic.utils

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.doesNotContain
import assertk.assertions.each
import fr.smarquis.playground.buildlogic.PlaygroundProperties
import fr.smarquis.playground.buildlogic.playground
import org.gradle.StartParameter
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.problems.ProblemGroup
import org.gradle.api.problems.ProblemId
import org.gradle.api.problems.Problems
import org.gradle.api.problems.Severity
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.register
import org.gradle.language.base.plugins.LifecycleBasePlugin.VERIFICATION_GROUP
import javax.inject.Inject

/**
 * Configures Gradle [Dependency Locking](https://docs.gradle.org/current/userguide/dependency_locking.html) for Android Application modules.
 * This relies on the `dependencies` tasks, and **always** requires the `--write-locks` flag for all operations.
 * It generates the `gradle.lockfile` in [ProjectLayout.getBuildDirectory] and only copies the result in the [ProjectLayout.getProjectDirectory] when requested.
 */
internal object PlaygroundDependencyLocking {
    private const val LOG = "PlaygroundDependencyLocking:"

    private object Tasks {
        const val GLOBAL_CI = "globalCiDependencyLockState"
        const val CI = "ciDependencyLockState"
        const val BASE = "dependencyLockState"
        const val CHECK = "checkDependencyLockState"
        const val GENERATE = "generateDependencyLockState"
    }

    fun configureRootProject(project: Project): TaskProvider<Task> =
        project.tasks.register(Tasks.GLOBAL_CI) {
            group = VERIFICATION_GROUP
            description = "Global lifecycle task to run all $Tasks.CI tasks."
        }

    fun configureProject(project: Project) = with(project) {
        val globalTask = rootProject.tasks.named(Tasks.GLOBAL_CI)
        pluginManager.withPlugin("com.android.application") {
            createAndroidDependencyLockingTasks(globalTask)
        }
    }

    private fun Project.createAndroidDependencyLockingTasks(
        globalTask: TaskProvider<Task>,
        properties: PlaygroundProperties = playground(),
    ) {
        val isWriteDependencyLocks = gradle.startParameter.isWriteDependencyLocks
        dependencyLocking {
            lockMode = properties.dependencyLockingMode
            if (isWriteDependencyLocks) lockFile = layout.buildDirectory.file("gradle.lockfile")
        }
        val lockedConfigurations = properties.dependencyLockingConfigurations.get()
        configurations.configureEach {
            if (name in lockedConfigurations) resolutionStrategy.activateDependencyLocking()
        }

        val dependencyLockState = tasks.register(Tasks.BASE) {
            notCompatibleWithConfigurationCache("This task relies on --write-locks which is not compatible with configuration cache.")
            doFirst {
                require(isWriteDependencyLocks) { "Dependency locking must be run with the `--write-locks` flag!" }
            }
            dependsOn("dependencies")
        }

        tasks.register<Copy>(Tasks.GENERATE) {
            dependsOn(dependencyLockState)
            from(dependencyLocking.lockFile)
            into(layout.projectDirectory)
        }

        val checkDependencyLockingTask = tasks.register<CheckDependencyLockingTask>(Tasks.CHECK) {
            dependsOn(dependencyLockState)
            fixTaskName = Tasks.GENERATE
            golden = layout.projectDirectory.file("gradle.lockfile")
            generated = dependencyLocking.lockFile
        }

        logger.debug("{} Creating CI Dependency Locking tasks for project '{}'", LOG, this)
        val ciDependencyLocking = tasks.register(Tasks.CI) {
            group = VERIFICATION_GROUP
            dependsOn(checkDependencyLockingTask)
        }
        globalTask.configure { dependsOn(ciDependencyLocking) }
    }

}

@Suppress("UnstableApiUsage")
internal abstract class CheckDependencyLockingTask : DefaultTask() {

    override fun getGroup() = VERIFICATION_GROUP
    override fun getDescription() = "Validates the generated dependency lock file against the golden lock file"

    @get:Inject
    abstract val startParameter: StartParameter

    @get:Inject
    abstract val layout: ProjectLayout

    @get:PathSensitive(PathSensitivity.NONE)
    @get:InputFile
    abstract val golden: RegularFileProperty

    @get:PathSensitive(PathSensitivity.NONE)
    @get:InputFile
    abstract val generated: RegularFileProperty

    @get:Input
    abstract val fixTaskName: Property<String>

    @get:Inject
    abstract val problems: Problems

    @TaskAction
    fun taskAction() {
        checkDifferences()
        checkDependencies()
    }

    private fun checkDifferences() = runCatching {
        assertThat(generated.get().asFile.readLines())
            .containsExactly(*golden.get().asFile.readLines().toTypedArray())
    }.onFailure {
        val problemGroup = ProblemGroup.create(/* name = */ "playground-group", /* displayName = */ "Playground")
        val problemId = ProblemId.create(
            /* name = */ "playground-dependency-locking-diff",
            /* displayName = */"Dependency lock file changed!",
            /* group = */ problemGroup,
        )
        val exception = GradleException("Generated dependency lock file differs from the golden lock file!").initCause(it)
        problems.reporter.throwing(exception, problemId) {
            contextualLabel(exception.message.orEmpty())
            fileLocation(generated.get().asFile.absolutePath)
            details(it.message.orEmpty())
            solution("If this change is intended, run: ${fixTaskName.get()} --write-locks")
            severity(Severity.ERROR)
            withException(exception)
        }
    }

    private fun checkDependencies() = runCatching {
        generated.get().asFile.readLines()
            .filterNot { it.startsWith("#") || it.isBlank() }
            .map { it.split("=").let { (dependency, configuration) -> dependency to configuration } }
            .filter { (_, configurations) -> "releaseRuntimeClasspath" in configurations.split(",") }
            .map { (dependency, _) -> dependency }
            .let(::assertThat)
            .each { it.doesNotContain("androidx.compose.ui:ui-tooling:", "junit") }
    }.onFailure {
        val problemGroup = ProblemGroup.create(/* name = */ "playground-group", /* displayName = */ "Playground")
        val problemId = ProblemId.create(
            /* name = */ "playground-dependency-locking-blocked",
            /* displayName = */"Blocked dependencies in dependency lock file!",
            /* group = */ problemGroup,
        )
        val exception = GradleException("Generated dependency lock file contains blocked dependencies!").initCause(it)
        problems.reporter.throwing(exception, problemId) {
            contextualLabel(exception.message.orEmpty())
            fileLocation(generated.get().asFile.absolutePath)
            details(it.message.orEmpty())
            severity(Severity.ERROR)
            withException(exception)
        }
    }

}
