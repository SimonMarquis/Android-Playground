package fr.smarquis.playground.buildlogic.utils

import fr.smarquis.playground.buildlogic.PlaygroundProperties
import fr.smarquis.playground.buildlogic.playground
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.LockMode
import org.gradle.kotlin.dsl.assign

/**
 * Configures Gradle [Dependency Locking](https://docs.gradle.org/current/userguide/dependency_locking.html) for Android Application modules.
 * This heavily relies on the default Gradle `dependencies` tasks, and **always** requires the `--write-locks` flag for all operations.
 *
 * ⚠️ Currently, the dependency locking mechanism is only **active** when `--write-locks` flag is used because [LockMode.LENIENT] does not seem to prevent build failures.
 *
 * Note: implementing the checks with regular [org.gradle.api.Task]s can't be easily done because the `gradle.lockfile`s are only written to disk **after** the build finishes...
 */
internal object PlaygroundDependencyLocking {
    private const val LOG = "PlaygroundDependencyLocking:"
    private const val TASK_NAME = "dependencyLockState"

    fun configureProject(project: Project) = with(project) {
        pluginManager.withPlugin("com.android.application") {
            createAndroidDependencyLockingTasks()
        }
    }

    private fun Project.createAndroidDependencyLockingTasks(
        properties: PlaygroundProperties = playground(),
    ) {
        val isWriteDependencyLocks = gradle.startParameter.isWriteDependencyLocks

        // Configure Gradle's dependency locking handler
        dependencyLocking.lockMode = properties.dependencyLockingMode

        // Activate dependency locking for registered configurations
        val lockedConfigurations = properties.dependencyLockingConfigurations.get()
        // `LockMode` workaround to keep dependency locking disabled
        if (isWriteDependencyLocks) configurations.configureEach {
            if (name in lockedConfigurations) resolutionStrategy.activateDependencyLocking()
        }

        // Expose task to update lock files.
        logger.debug("{} Creating $TASK_NAME task for project '{}'", LOG, this)
        tasks.register(TASK_NAME) {
            notCompatibleWithConfigurationCache("This task relies on --write-locks which is not compatible with configuration cache.")
            doFirst {
                require(isWriteDependencyLocks) { "Dependency locking must be run with the `--write-locks` flag!" }
            }
            dependsOn("dependencies")
        }
    }

}
