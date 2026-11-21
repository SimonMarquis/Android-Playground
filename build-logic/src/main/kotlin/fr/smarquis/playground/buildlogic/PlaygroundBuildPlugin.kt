package fr.smarquis.playground.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.dsl.TestExtension
import com.android.build.api.variant.TestAndroidComponentsExtension
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.dsl.Dependencies
import org.gradle.api.artifacts.dsl.DependencyCollector
import org.gradle.api.model.ObjectFactory
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import javax.inject.Inject

@DslMarker
public annotation class PlaygroundDslMarker

@PlaygroundDslMarker
public interface PlaygroundExtension {
    public fun androidApp(applicationId: String, action: Action<AndroidApp>)
    public fun androidLibrary(action: Action<AndroidLibrary> = Action {})
    public fun androidBenchmark(targetProjectPath: String, action: Action<AndroidBenchmark> = Action {})
    public fun jvmLibrary(action: Action<JvmLibrary> = Action {})
    public fun platform()
}

@PlaygroundDslMarker
public interface CommonOptions {
    public fun metro()
    public fun serialization()
    public fun dependencies(action: Action<PlaygroundDependencies>)
}

@PlaygroundDslMarker
public interface AndroidApp : CommonOptions {
    public fun compose()
    public fun testFixtures()
    public fun android(action: Action<ApplicationExtension>)
}

@PlaygroundDslMarker
public interface AndroidLibrary : CommonOptions {
    public fun compose()
    public fun screenshots()
    public fun testFixtures()
    public fun android(action: Action<LibraryExtension>)
}

@PlaygroundDslMarker
public interface AndroidBenchmark : CommonOptions {
    public fun android(action: Action<TestExtension>)
    public fun androidComponents(action: Action<TestAndroidComponentsExtension>)
}

@PlaygroundDslMarker
public interface JvmLibrary : CommonOptions {
    public fun testFixtures()
    public fun kotlin(action: Action<KotlinJvmProjectExtension>)
}

@PlaygroundDslMarker
public interface PlaygroundDependencies : Dependencies {
    public val api: DependencyCollector
    public val implementation: DependencyCollector
    public val compileOnly: DependencyCollector
    public val runtimeOnly: DependencyCollector
    public val testImplementation: DependencyCollector
    public val testFixturesApi: DependencyCollector
    public val testFixturesImplementation: DependencyCollector
    public fun testFixtures(dependency: Any): Dependency
}

internal abstract class PlaygroundDependenciesImpl @Inject constructor(private val target: Project) : PlaygroundDependencies {
    override fun testFixtures(dependency: Any): Dependency = target.dependencies.testFixtures(dependency)

    fun wire() {
        val collectors = mapOf(
            "api" to api, "implementation" to implementation, "compileOnly" to compileOnly,
            "runtimeOnly" to runtimeOnly, "testImplementation" to testImplementation,
            "testFixturesApi" to testFixturesApi, "testFixturesImplementation" to testFixturesImplementation,
        )
        target.configurations.configureEach {
            collectors[name]?.let { fromDependencyCollector(it) }
        }
    }
}

internal class PlaygroundBuildPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.extensions.create(PlaygroundExtension::class.java, "playground", PlaygroundExtensionImpl::class.java, target)
    }
}

internal abstract class PlaygroundExtensionImpl @Inject constructor(
    private val project: Project,
    private val objects: ObjectFactory,
) : PlaygroundExtension {
    private var configured = false

    private fun configure(block: () -> Unit) {
        check(!configured) { "Only one playground module type may be configured per project" }
        configured = true
        block()
    }

    override fun androidApp(applicationId: String, action: Action<AndroidApp>) = configure {
        val options = AndroidAppImpl(project, objects)
        action.execute(options)
        project.configurePlaygroundAndroidApplication()
        project.androidApplication { defaultConfig.applicationId = applicationId }
        options.evaluate()
        options.androidActions.forEach { project.extensions.configure(ApplicationExtension::class.java, it) }
    }

    override fun androidLibrary(action: Action<AndroidLibrary>) = configure {
        val options = AndroidLibraryImpl(project, objects)
        action.execute(options)
        project.configurePlaygroundAndroidLibrary()
        options.evaluate()
        options.androidActions.forEach { project.extensions.configure(LibraryExtension::class.java, it) }
    }

    override fun androidBenchmark(targetProjectPath: String, action: Action<AndroidBenchmark>) = configure {
        val options = AndroidBenchmarkImpl(project, objects)
        action.execute(options)
        project.configurePlaygroundAndroidTest()
        project.androidTest { this.targetProjectPath = targetProjectPath }
        options.evaluate()
        options.androidActions.forEach { project.extensions.configure(TestExtension::class.java, it) }
        options.componentActions.forEach { project.extensions.configure(TestAndroidComponentsExtension::class.java, it) }
    }

    override fun jvmLibrary(action: Action<JvmLibrary>) = configure {
        val options = JvmLibraryImpl(project, objects)
        action.execute(options)
        project.configurePlaygroundKotlinJvm()
        options.evaluate()
        options.kotlinActions.forEach { project.extensions.configure(KotlinJvmProjectExtension::class.java, it) }
    }

    override fun platform() = configure { project.configurePlaygroundPlatform() }
}

internal abstract class CommonOptionsImpl(protected val project: Project, private val objects: ObjectFactory) : CommonOptions {
    private var metroEnabled = false
    private var serializationEnabled = false
    protected var fixturesEnabled = false
    protected var composeEnabled = false
    protected var screenshotsEnabled = false
    private val dependencyActions = mutableListOf<Action<PlaygroundDependencies>>()
    override fun metro() { metroEnabled = true }
    override fun serialization() { serializationEnabled = true }
    override fun dependencies(action: Action<PlaygroundDependencies>) { dependencyActions.add(action) }
    protected open fun enableFixtures() = Unit
    fun evaluate() {
        if (fixturesEnabled) enableFixtures()
        if (composeEnabled) project.configurePlaygroundAndroidCompose()
        if (metroEnabled) project.configurePlaygroundMetro()
        if (serializationEnabled) project.pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")
        if (screenshotsEnabled) project.configurePlaygroundScreenshots()
        if (dependencyActions.isNotEmpty()) {
            val dependencies = objects.newInstance(PlaygroundDependenciesImpl::class.java, project)
            dependencyActions.forEach { it.execute(dependencies) }
            dependencies.wire()
        }
    }
}

internal class AndroidAppImpl(project: Project, objects: ObjectFactory) : CommonOptionsImpl(project, objects), AndroidApp {
    val androidActions = mutableListOf<Action<ApplicationExtension>>()
    override fun compose() { composeEnabled = true }
    override fun testFixtures() { fixturesEnabled = true }
    override fun enableFixtures() { project.androidApplication { testFixtures.enable = true } }
    override fun android(action: Action<ApplicationExtension>) { androidActions.add(action) }
}

internal class AndroidLibraryImpl(project: Project, objects: ObjectFactory) : CommonOptionsImpl(project, objects), AndroidLibrary {
    val androidActions = mutableListOf<Action<LibraryExtension>>()
    override fun compose() { composeEnabled = true }
    override fun screenshots() { screenshotsEnabled = true }
    override fun testFixtures() { fixturesEnabled = true }
    override fun enableFixtures() { project.androidLibrary { testFixtures.enable = true } }
    override fun android(action: Action<LibraryExtension>) { androidActions.add(action) }
}

internal class AndroidBenchmarkImpl(project: Project, objects: ObjectFactory) : CommonOptionsImpl(project, objects), AndroidBenchmark {
    val androidActions = mutableListOf<Action<TestExtension>>()
    val componentActions = mutableListOf<Action<TestAndroidComponentsExtension>>()
    override fun android(action: Action<TestExtension>) { androidActions.add(action) }
    override fun androidComponents(action: Action<TestAndroidComponentsExtension>) { componentActions.add(action) }
}

internal class JvmLibraryImpl(project: Project, objects: ObjectFactory) : CommonOptionsImpl(project, objects), JvmLibrary {
    val kotlinActions = mutableListOf<Action<KotlinJvmProjectExtension>>()
    override fun testFixtures() { fixturesEnabled = true }
    override fun enableFixtures() { project.pluginManager.apply("java-test-fixtures") }
    override fun kotlin(action: Action<KotlinJvmProjectExtension>) { kotlinActions.add(action) }
}
