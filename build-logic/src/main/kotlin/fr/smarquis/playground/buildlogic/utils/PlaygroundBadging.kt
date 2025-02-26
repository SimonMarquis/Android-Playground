package fr.smarquis.playground.buildlogic.utils

import assertk.assertThat
import assertk.assertions.containsExactly
import com.android.SdkConstants.FD_BUILD_TOOLS
import com.android.SdkConstants.FN_AAPT2
import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.gradle.BaseExtension
import fr.smarquis.playground.buildlogic.PlaygroundProperties
import fr.smarquis.playground.buildlogic.capitalized
import fr.smarquis.playground.buildlogic.playground
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.problems.Problems
import org.gradle.api.problems.Severity
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity.NONE
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.gradle.language.base.plugins.LifecycleBasePlugin.VERIFICATION_GROUP
import org.gradle.process.ExecOperations
import javax.inject.Inject

/**
 * Inspired by [android/nowinandroid](https://github.com/android/nowinandroid/blob/main/build-logic/convention/src/main/kotlin/com/google/samples/apps/nowinandroid/Badging.kt).
 *
 * [Increase your app's availability across device types](https://android-developers.googleblog.com/2023/12/increase-your-apps-availability-across-device-types.html)
 */
@Suppress("UnstableApiUsage")
internal object PlaygroundBadging {
    private const val GLOBAL_CI_BADGING_TASK_NAME = "globalCiBadging"
    private const val CI_BADGING_TASK_NAME = "ciBadging"
    private const val LOG = "PlaygroundBadging:"

    fun configureRootProject(project: Project): TaskProvider<Task> =
        project.tasks.register(GLOBAL_CI_BADGING_TASK_NAME) {
            group = VERIFICATION_GROUP
            description = "Global lifecycle task to run all ciBadging tasks."
        }

    fun configureProject(project: Project) = with(project) {
        val globalTask = rootProject.tasks.named(GLOBAL_CI_BADGING_TASK_NAME)
        pluginManager.withPlugin("com.android.application") {
            createAndroidBadgingTasks(globalTask)
        }
    }

    private fun Project.createAndroidBadgingTasks(
        globalTask: TaskProvider<Task>,
        baseExtension: BaseExtension = project.extensions.getByType<BaseExtension>(),
        componentsExtension: ApplicationAndroidComponentsExtension = project.extensions.getByType<ApplicationAndroidComponentsExtension>(),
        properties: PlaygroundProperties = playground(),
    ) = componentsExtension.onVariants { variant ->
        val target = this@createAndroidBadgingTasks
        val capitalizedVariantName = variant.name.capitalized()

        val generateBadgingTaskName = "generate${capitalizedVariantName}Badging"
        val generateBadging = project.tasks.register<GenerateBadgingTask>(generateBadgingTaskName) {
            apk = variant.artifacts.get(SingleArtifact.APK_FROM_BUNDLE)
            // TODO: Replace with `sdkComponents.aapt2` when it's available in AGP https://issuetracker.google.com/issues/376815836
            aapt2Executable = componentsExtension.sdkComponents.sdkDirectory.map {
                it.file("${FD_BUILD_TOOLS}/${baseExtension.buildToolsVersion}/${FN_AAPT2}")
            }
            badging = layout.buildDirectory.file("outputs/apk_from_bundle/${variant.name}/${variant.name}-badging.txt")
        }

        val updateBadgingTaskName = "update${capitalizedVariantName}Badging"
        val updateBadgingTask = project.tasks.register<Copy>(updateBadgingTaskName) {
            description = "Copies the generated badging file into the main project directory."
            from(generateBadging.get().badging)
            into(layout.projectDirectory)
        }
        updateBadgingTask.toString()

        val checkBadgingTaskName = "check${capitalizedVariantName}Badging"
        val checkBadgingTask = project.tasks.register<CheckBadgingTask>(checkBadgingTaskName) {
            goldenBadging = layout.projectDirectory.file("${variant.name}-badging.txt")
            generatedBadging = generateBadging.get().badging
            targetUpdateBadgingTaskName = "${target.path}:$updateBadgingTaskName"
            output = layout.buildDirectory.dir("intermediates/$checkBadgingTaskName")
        }

        if (variant.name == properties.ciBadgingVariant.get()) {
            logger.debug("{} Creating CI Badging tasks for project '{}' and variant '{}'", LOG, target, variant.name)
            val ciBadging = tasks.register(CI_BADGING_TASK_NAME) {
                group = VERIFICATION_GROUP
                dependsOn(checkBadgingTask)
            }
            globalTask.configure { dependsOn(ciBadging) }
        }
    }

}

@CacheableTask
internal abstract class GenerateBadgingTask : DefaultTask() {

    @get:OutputFile
    abstract val badging: RegularFileProperty

    @get:PathSensitive(NONE)
    @get:InputFile
    abstract val apk: RegularFileProperty

    @get:PathSensitive(NONE)
    @get:InputFile
    abstract val aapt2Executable: RegularFileProperty

    @get:Inject
    abstract val execOperations: ExecOperations

    init {
        description = "Generates the badging file from the universal APK using the aapt2 executable."
    }

    @TaskAction
    fun taskAction() {
        execOperations.exec {
            commandLine(aapt2Executable.get().asFile.absolutePath, "dump", "badging", apk.get().asFile.absolutePath)
            standardOutput = badging.asFile.get().outputStream()
        }
    }
}

@Suppress("UnstableApiUsage")
@CacheableTask
internal abstract class CheckBadgingTask : DefaultTask() {

    @get:OutputDirectory
    abstract val output: DirectoryProperty

    @get:PathSensitive(NONE)
    @get:InputFile
    abstract val goldenBadging: RegularFileProperty

    @get:PathSensitive(NONE)
    @get:InputFile
    abstract val generatedBadging: RegularFileProperty

    @get:Input
    abstract val targetUpdateBadgingTaskName: Property<String>

    @get:Inject
    abstract val problems: Problems

    init {
        group = VERIFICATION_GROUP
        description = "Validates the generated badging file against the golden badging file."
    }

    @TaskAction
    fun taskAction() {
        runCatching {
            assertThat(generatedBadging.get().asFile.readLines().filterNot(String::isBlank))
                .containsExactly(*goldenBadging.get().asFile.readLines().filterNot(String::isBlank).toTypedArray())
        }.onFailure {
            /*problems.reporter.throwing {
                val message = "Generated Android badging file differs from the golden badging file!"
                id("playground-android-badging", "Android badging file changed!")
                contextualLabel(message)
                fileLocation(generatedBadging.get().asFile.absolutePath)
                solution("If this change is intended, run the `${targetUpdateBadgingTaskName.get()}` task.")
                severity(Severity.ERROR)
                withException(GradleException("$message\n\n${it.message}").initCause(it))
            }*/
        }
    }

}
