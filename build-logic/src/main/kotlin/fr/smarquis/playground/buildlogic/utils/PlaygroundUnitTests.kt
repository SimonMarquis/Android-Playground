package fr.smarquis.playground.buildlogic.utils

import fr.smarquis.playground.buildlogic.PlaygroundProperties
import fr.smarquis.playground.buildlogic.capitalized
import fr.smarquis.playground.buildlogic.dsl.apply
import fr.smarquis.playground.buildlogic.dsl.assign
import fr.smarquis.playground.buildlogic.dsl.configure
import fr.smarquis.playground.buildlogic.dsl.withType
import fr.smarquis.playground.buildlogic.isAndroidTest
import fr.smarquis.playground.buildlogic.isCi
import fr.smarquis.playground.buildlogic.libs
import fr.smarquis.playground.buildlogic.playground
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.gradle.api.tasks.testing.logging.TestLogEvent.STARTED
import org.gradle.kotlin.dsl.develocity
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.powerassert.gradle.PowerAssertGradleExtension
import org.jetbrains.kotlin.powerassert.gradle.PowerAssertGradlePlugin

/**
 * Inspired by https://github.com/slackhq/foundry
 */
internal object PlaygroundUnitTests {
    private const val CI_UNIT_TEST_TASK_NAME = "ciUnitTest"
    private const val COMPILE_CI_UNIT_TEST_NAME = "compileCiUnitTest"
    private const val LOG = "PlaygroundUnitTests:"

    fun configureSubproject(project: Project) = with(project) {
        if (isAndroidTest) return@with // Android Test modules are special, they don't have tests...
        pluginManager.withPlugin("com.android.base") {
            createAndroidCiUnitTestTask()
        }
        pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
            createJvmCiUnitTestTask()
        }
        configureTestTasks()
        addTestDependencies()
        applyPowerAssert()
    }

    private fun Project.createJvmCiUnitTestTask() {
        logger.debug("{} Creating CI unit test tasks for project '{}'", LOG, this)
        val ciUnitTest = registerCiUnitTestTask(
            name = CI_UNIT_TEST_TASK_NAME,
            dependencyTaskName = "test",
        )
        PlaygroundGlobalCi.addToGlobalCi(project, ciUnitTest)
        registerCiUnitTestTask(
            name = COMPILE_CI_UNIT_TEST_NAME,
            dependencyTaskName = "testClasses",
        )
    }

    private fun Project.createAndroidCiUnitTestTask() {
        val variant = playground().ciUnitTestVariant.get().capitalized()
        val variantUnitTestTaskName = "test${variant}UnitTest"
        val variantCompileUnitTestTaskName = "compile${variant}UnitTestSources"
        logger.debug("{} Creating CI unit test tasks for project '{}' and variant '{}'", LOG, this, variant)
        val ciUnitTest = registerCiUnitTestTask(
            name = CI_UNIT_TEST_TASK_NAME,
            dependencyTaskName = variantUnitTestTaskName,
        )
        PlaygroundGlobalCi.addToGlobalCi(project, ciUnitTest)
        registerCiUnitTestTask(
            name = COMPILE_CI_UNIT_TEST_NAME,
            dependencyTaskName = variantCompileUnitTestTaskName,
        )
    }

    private fun Project.registerCiUnitTestTask(
        name: String,
        dependencyTaskName: String,
    ) = tasks.register(name) {
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        dependsOn(dependencyTaskName)
    }

    private fun Project.configureTestTasks(
        properties: PlaygroundProperties = playground(),
    ) = tasks.withType<Test>().configureEach {
        maxHeapSize = "1g"
        // https://docs.gradle.org/current/userguide/performance.html#parallel_test_execution
        maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)

        if (properties.unitTestVerboseLogging) {
            testLogging {
                showStandardStreams = true
                showStackTraces = true
                exceptionFormat = FULL
                displayGranularity = 0
                events(STARTED, PASSED, FAILED, SKIPPED)
            }
        }

        if (isCi) {
            develocity.testRetry {
                maxRetries = properties.unitTestMaxRetries
                maxFailures = properties.unitTestMaxFailures
                failOnPassedAfterRetry = properties.unitTestFailOnPassedAfterRetry
            }
        }

        reports {
            // Disable System's out/err outputs in XML reports
            junitXml.includeSystemOutLog = false
            junitXml.includeSystemErrLog = false
            junitXml.mergeReruns = true
        }

        // Use `-Pplayground.rerun-tests` to force re-run tests
        if (properties.isRerunTest) outputs.upToDateWhen { false }
    }

    private fun Project.addTestDependencies() {
        dependencies.add("testImplementation", libs.`kotlin-test`)
        dependencies.add("testImplementation", libs.assertk)
        dependencies.add("testImplementation", libs.junit)
    }

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    private fun Project.applyPowerAssert() {
        apply<PowerAssertGradlePlugin>()
        configure<PowerAssertGradleExtension> {
            functions = listOf(
                "kotlin.assert",
                "kotlin.require", "kotlin.requireNotNull",
                "kotlin.check", "kotlin.checkNotNull",
                "kotlin.test.assertTrue", "kotlin.test.assertFalse",
                "kotlin.test.assertEquals", "kotlin.test.assertNotEquals",
                "kotlin.test.assertSame", "kotlin.test.assertNotSame",
                "kotlin.test.assertNull", "kotlin.test.assertNotNull",
                "kotlin.test.assertIs", "kotlin.test.assertNotIs",
                "kotlin.test.assertContains", "kotlin.test.assertContentEquals",
            )
        }
    }

}
