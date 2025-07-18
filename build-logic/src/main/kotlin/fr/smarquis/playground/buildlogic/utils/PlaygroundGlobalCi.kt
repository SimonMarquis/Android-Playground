package fr.smarquis.playground.buildlogic.utils

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider

/**
 * Inspired by [androidx/androidx](https://github.com/androidx/androidx/blob/androidx-main/buildSrc/public/src/main/kotlin/androidx/build/BuildOnServer.kt)
 */
public object PlaygroundGlobalCi {
    internal const val GLOBAL_CI_TASK = "globalCi"

    public fun configureRootProject(project: Project): TaskProvider<Task> = project.tasks.register(GLOBAL_CI_TASK)
    public fun configureSubproject(project: Project): TaskProvider<Task> = project.tasks.register(GLOBAL_CI_TASK)

    public fun <T : Task> addToGlobalCi(project: Project, taskProvider: TaskProvider<T>): Unit =
        project.tasks.named(GLOBAL_CI_TASK).configure { dependsOn(taskProvider) }

    public fun addToGlobalCi(project: Project, taskPath: String): Unit =
        project.tasks.named(GLOBAL_CI_TASK).configure { dependsOn(taskPath) }
}
