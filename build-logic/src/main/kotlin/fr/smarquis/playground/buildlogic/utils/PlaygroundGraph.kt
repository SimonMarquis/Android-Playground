package fr.smarquis.playground.buildlogic.utils

import fr.smarquis.playground.buildlogic.associateWithNotNull
import fr.smarquis.playground.buildlogic.measureTimedValue
import fr.smarquis.playground.buildlogic.utils.MermaidBuilder.toMermaidLiveUrl
import fr.smarquis.playground.buildlogic.utils.PlaygroundGraph.Graph
import fr.smarquis.playground.buildlogic.utils.PlaygroundGraph.IGNORED_PROJECTS
import fr.smarquis.playground.buildlogic.utils.PlaygroundGraph.PluginType
import fr.smarquis.playground.buildlogic.utils.PlaygroundGraph.PluginType.AndroidApplication
import fr.smarquis.playground.buildlogic.utils.PlaygroundGraph.PluginType.AndroidLibrary
import fr.smarquis.playground.buildlogic.utils.PlaygroundGraph.PluginType.AndroidTest
import fr.smarquis.playground.buildlogic.utils.PlaygroundGraph.PluginType.Jvm
import fr.smarquis.playground.buildlogic.utils.PlaygroundGraph.PluginType.Unknown
import fr.smarquis.playground.buildlogic.utils.PlaygroundGraph.SUPPORTED_CONFIGURATIONS
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity.NONE
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.text.RegexOption.DOT_MATCHES_ALL
import kotlin.time.DurationUnit.MILLISECONDS

/**
 * Generates module dependency graphs, and add them to their corresponding README.md file.
 *
 * This is currently not an optimal implementation for a few reasons:
 * - It heavily filters the resulting graph with [SUPPORTED_CONFIGURATIONS] and [IGNORED_PROJECTS] to make it readable.
 * - [Graph.invoke] will **recursively** search through dependent projects (although in practice it will never reach a stack overflow).
 * - [Graph.invoke] will be entirely re-executed for all projects, without re-using intermediate values.
 */
internal object PlaygroundGraph {

    private const val LOG = "PlaygroundGraph"
    private const val MAIN_PROJECT_PATH = ":app"
    private val IGNORED_PROJECTS = setOf(":platform")
    private val SUPPORTED_CONFIGURATIONS = setOf("api", "implementation", "baselineProfile", "testedApks")

    fun configureProject(project: Project) {
        val dumpTask = project.tasks.register<GraphDumpTask>("graphDump") {
            val graph = measureTimedValue { Graph().invoke(project) }
                .also { logger.lifecycle("{} Computing graph for project '{}' in {}", LOG, this, it.duration.toString(MILLISECONDS)) }
                .value
            projectPath = project.path
            dependencies = graph.dependencies()
            plugins = graph.plugins()
            output = project.layout.buildDirectory.file("mermaid.txt")
        }
        project.tasks.register<GraphUpdateTask>("graphUpdate") {
            projectPath = project.path
            input = dumpTask.get().output
            output = project.run { (if (path == MAIN_PROJECT_PATH) rootProject else this).layout.projectDirectory.file("README.md") }
        }
    }

    /**
     * Declaration order is important, as only the first match will be retained.
     */
    internal enum class PluginType(val id: String, val ref: String) {
        AndroidApplication("com.android.application", "android-application"),
        AndroidLibrary("com.android.library", "android-library"),
        AndroidTest("com.android.test", "android-test"),
        Jvm("org.jetbrains.kotlin.jvm", "jvm"),
        Unknown("?", "unknown"),
    }

    private class Graph(
        private val dependencies: MutableMap<Project, Set<Pair<Configuration, Project>>> = mutableMapOf(),
        private val plugins: MutableMap<Project, PluginType> = mutableMapOf(),
        private val seen: MutableSet<String> = mutableSetOf(),
    ) {

        operator fun invoke(project: Project): Graph {
            if (project.path in seen) return this
            seen += project.path
            plugins.putIfAbsent(project, PluginType.values().firstOrNull { project.pluginManager.hasPlugin(it.id) } ?: Unknown)
            dependencies.compute(project) { _, u -> u.orEmpty() }
            project.configurations
                .matching { it.name in SUPPORTED_CONFIGURATIONS }
                .associateWithNotNull { it.dependencies.withType<ProjectDependency>().ifEmpty { null } }
                .flatMap { (c, value) -> value.map { dep -> c to project.project(dep.path) } }
                .filter { (_, p) -> p.path !in IGNORED_PROJECTS }
                .forEach { (configuration: Configuration, projectDependency: Project) ->
                    dependencies.compute(project) { _, u -> u.orEmpty() + (configuration to projectDependency) }
                    invoke(projectDependency)
                }
            return this
        }

        fun dependencies() = dependencies.mapKeys { it.key.path }.mapValues { it.value.map { (c, p) -> c.name to p.path }.toSet() }
        fun plugins() = plugins.mapKeys { it.key.path }
    }
}

@CacheableTask
private abstract class GraphDumpTask : DefaultTask() {

    @get:Input
    abstract val projectPath: Property<String>

    @get:Input
    abstract val dependencies: MapProperty<String, Set<Pair<String, String>>>

    @get:Input
    abstract val plugins: MapProperty<String, PluginType>

    @get:OutputFile
    abstract val output: RegularFileProperty

    override fun getDescription() = "Dumps project dependencies to a mermaid file."

    @TaskAction
    operator fun invoke() {
        val dependencies = dependencies.get()
            .flatMapTo(mutableSetOf()) { it.value.map { dep -> Triple(it.key, dep.first, dep.second) } }
        val mermaid = MermaidBuilder(projectPath.get(), dependencies, plugins.get())
        output.get().asFile.writeText(mermaid)
        logger.lifecycle(output.get().asFile.toPath().toUri().toString())
        logger.lifecycle(mermaid.toMermaidLiveUrl())
    }

}

@CacheableTask
private abstract class GraphUpdateTask : DefaultTask() {

    @get:Input
    abstract val projectPath: Property<String>

    @get:InputFile
    @get:PathSensitive(NONE)
    abstract val input: RegularFileProperty

    @get:OutputFile
    abstract val output: RegularFileProperty

    override fun getDescription() = "Updates Markdown file with the corresponding dependency graph."

    @TaskAction
    operator fun invoke() = with(output.get().asFile) {
        if (!exists()) {
            createNewFile()
            writeText(
                """
                # `${projectPath.get()}`
    
                <!--region graph--> <!--endregion-->
    
                """.trimIndent(),
            )
        }
        val regex = """(<!--region graph-->)(.*?)(<!--endregion-->)""".toRegex(DOT_MATCHES_ALL)
        val text = readText().replace(regex) { match ->
            val (start, _, end) = match.destructured
            val mermaid = input.get().asFile.readText()
            """
            |$start
            |```mermaid
            |$mermaid
            |```
            |[✨ View in `mermaid.live`](${mermaid.toMermaidLiveUrl()})
            |$end
            """.trimMargin()
        }
        writeText(text)
    }

}

private object MermaidBuilder {
    operator fun invoke(
        self: String,
        dependencies: Set<Triple<String, String, String>>,
        pluginTypes: Map<String, PluginType>,
    ) = buildString {
        // FrontMatter configuration
        appendLine(
            // language=YAML
            """
            ---
            config:
              layout: elk
              elk:
                nodePlacementStrategy: SIMPLE
            ---
            """.trimIndent(),
        )
        // Graph declaration
        appendLine("graph TB")
        // Nodes and subgraphs (limited to a single nested layer)
        val (rootProjects, nestedProjects) = dependencies
            .map { it.first to it.third }.map { it.toList() }.flatten().toSet()
            .plus(self) // Special case when this specific module has no other dependency
            .groupBy { it.substringBeforeLast(":") }
            .entries.partition { it.key.isEmpty() }
        nestedProjects.sortedByDescending { it.value.size }.forEach { (group, projects) ->
            appendLine("  subgraph $group")
            projects.sorted().forEach { appendLine(it.aliasWithType(indent = 4, pluginTypes.getValue(it))) }
            appendLine("  end")
        }
        rootProjects.flatMap { it.value }.sortedDescending().forEach {
            appendLine(it.aliasWithType(indent = 2, pluginTypes.getValue(it)))
        }
        // Links
        if (dependencies.isNotEmpty()) appendLine()
        dependencies
            .sortedWith(compareBy({ it.first }, { it.third }, { it.second }))
            .forEach { appendLine(it.link(indent = 2)) }
        // Classes
        appendLine()
        append(
            """
            classDef ${AndroidApplication.ref} fill:#2C4162,stroke:#fff,stroke-width:2px,color:#fff;
            classDef ${AndroidLibrary.ref} fill:#3BD482,stroke:#fff,stroke-width:2px,color:#fff;
            classDef ${AndroidTest.ref} fill:#3BD482,stroke:#fff,stroke-width:2px,color:#fff;
            classDef ${Jvm.ref} fill:#7F52FF,stroke:#fff,stroke-width:2px,color:#fff;
            """.trimIndent(),
        )
    }

    // :feature:home[home]:::android-library
    private fun String.aliasWithType(indent: Int, pluginType: PluginType): String = buildString {
        append(" ".repeat(indent))
        append(this@aliasWithType)
        append("[").append(substringAfterLast(":")).append("]:::")
        append(pluginType.ref)
    }

    // :app -.-> :feature:home
    private fun Triple<String, String, String>.link(indent: Int) = buildString {
        append(" ".repeat(indent))
        append(first).append(" ")
        append(
            when (val configuration = second) {
                "api" -> "--->"
                "implementation" -> "-.->"
                else -> "-.->|$configuration|"
            },
        )
        append(" ").append(third)
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun String.toMermaidLiveUrl(): String = buildJsonObject { put("code", this@toMermaidLiveUrl) }
        .toString()
        .toByteArray()
        .let { Base64.UrlSafe.encode(it) }
        .replace("/", "_").replace("+", "-")
        .let { "https://mermaid.live/view#base64:$it" }

}
