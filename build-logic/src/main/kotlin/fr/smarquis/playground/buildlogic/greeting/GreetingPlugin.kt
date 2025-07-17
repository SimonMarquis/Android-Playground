package fr.smarquis.playground.buildlogic.greeting

import org.gradle.api.Plugin
import org.gradle.api.Project

public class GreetingPlugin public constructor() : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("greeting", GreetingExtension::class.java)
        extension.who.convention("World")
        project.tasks.register("greeting", GreetingTask::class.java) {
            who.convention(extension.who)
        }
    }
}
