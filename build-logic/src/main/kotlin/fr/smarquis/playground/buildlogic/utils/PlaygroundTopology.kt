package fr.smarquis.playground.buildlogic.utils

import fr.smarquis.playground.buildlogic.utils.PlaygroundTopology.IGNORED_DEPENDENCIES
import fr.smarquis.playground.buildlogic.utils.PlaygroundTopology.LOG
import fr.smarquis.playground.buildlogic.utils.PlaygroundTopology.RULES
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.problems.ProblemGroup
import org.gradle.api.problems.ProblemId
import org.gradle.api.problems.Problems
import org.gradle.api.problems.Severity
import org.gradle.api.project.IsolatedProject
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.language.base.plugins.LifecycleBasePlugin.VERIFICATION_GROUP
import org.intellij.lang.annotations.Language
import java.lang.System.lineSeparator
import javax.inject.Inject

internal object PlaygroundTopology {

    const val LOG = "PlaygroundTopology:"
    private const val TASK_NAME = "topologyCheck"

    val IGNORED_DEPENDENCIES = setOf(":platform", ":lint")
    val RULES = mapOf(
        ":app".rules(":profiling", ":feature:.*", ":core:.*", ":domain:.*", ":data:.*"),
        ":core:.*".rules(":core:.*"),
        ":data:.*".rules(":domain:.*", ":core:.*"),
        ":feature:.*".rules(":domain:.*", ":core:.*"),
        ":profiling".rules(":app"),
    )

    private fun @receiver:Language("RegExp") String.rules(
        @Language("RegExp") vararg rules: String,
    ) = toRegex() to rules.map(String::toRegex)

    @Suppress("UnstableApiUsage")
    fun configureProject(project: Project) = with(project) {
        logger.debug("{} Creating $TASK_NAME task for project '{}'", LOG, this)
        tasks.register<CheckTopologyTask>(TASK_NAME) {
            projectPath = isolated.path
            dependencies = configurations.flatMap { it.dependencies.withType<ProjectDependency>().map { it.path } }.toSet()
            output = layout.buildDirectory.file("intermediates/$name/topology.txt")
        }
    }

}

@Suppress("UnstableApiUsage")
@CacheableTask
internal abstract class CheckTopologyTask : DefaultTask() {

    @get:Input
    abstract val projectPath: Property<String>

    @get:Input
    abstract val dependencies: SetProperty<String>

    @get:OutputFile
    abstract val output: RegularFileProperty

    @get:Inject
    abstract val problems: Problems

    init {
        group = VERIFICATION_GROUP
        description = "Checks the topology of project dependencies."
    }

    @TaskAction
    fun taskAction() {
        logger.debug("{} Checking {} with dependencies: {}", LOG, projectPath.get(), dependencies.get())
        val dependencies = dependencies.get().minus(IGNORED_DEPENDENCIES).minus(projectPath.get()) // ignore self-referencing dependencies
        if (dependencies.isEmpty()) return logger.debug("{} No dependencies found!", LOG)

        output.get().asFile.writeText(dependencies.sorted().joinToString(lineSeparator()))

        val matchingRules = RULES.filter { it.key.matches(projectPath.get()) }.values.flatten()
        logger.debug("{} Matching rules {}", LOG, matchingRules)
        if (matchingRules.isEmpty()) {
            val problemGroup = ProblemGroup.create(/* name = */ "playground-group", /* displayName = */ "Playground")
            val problemId = ProblemId.create(/* name = */ "playground-topology-missing-rule", /* displayName = */ "Topology missing rule!", /* group = */ problemGroup)
            val exception = GradleException("Missing topology rule for project ${projectPath.get()}!")
            problems.reporter.throwing(exception, problemId) {
                contextualLabel(exception.message.orEmpty())
                fileLocation(project.buildFile.absolutePath)
                solution(
                    """
                    Make sure the project path is following the conventions, or update the topology rules in ${PlaygroundTopology::class}.
                    Rules: ${RULES.keys}
                    """.trimIndent(),
                )
                severity(Severity.ERROR)
                withException(exception)
            }
        }

        val violations = dependencies.filter { d -> matchingRules.none { it.matches(d) } }
        logger.debug("{} Violations {}", LOG, violations)
        if (violations.isNotEmpty()) {
            val problemGroup = ProblemGroup.create(/* name = */ "playground-group", /* displayName = */ "Playground")
            val problemId = ProblemId.create(/* name = */ "playground-topology-violation", /* displayName = */ "Topology rule violation!", /* group = */ problemGroup)
            val exception = GradleException("Topology rule violations for project ${projectPath.get()}! $violations")
            problems.reporter.throwing(exception, problemId) {
                contextualLabel(exception.message.orEmpty())
                fileLocation(project.buildFile.absolutePath)
                solution(
                    """
                    Fix the project dependencies, or update the topology rules in ${PlaygroundTopology::class}.
                    Matching rules: $matchingRules
                    """.trimIndent(),
                )
                severity(Severity.ERROR)
                withException(exception)
            }
        }
    }

}
