package fr.smarquis.playground.buildlogic.greeting

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask

@UntrackedTask(because = "We expect it to print a message every time.")
public abstract class GreetingTask public constructor() : DefaultTask() {

    @get:Input
    public abstract val who: Property<String>

    @TaskAction
    public fun greet(): Unit = logger.lifecycle("Hello ${who.get()}!")

}
