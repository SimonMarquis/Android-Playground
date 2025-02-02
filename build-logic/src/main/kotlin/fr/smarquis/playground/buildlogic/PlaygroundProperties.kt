package fr.smarquis.playground.buildlogic

import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.provideDelegate
import kotlin.reflect.KProperty

internal fun Project.playground() = PlaygroundProperties(this)
internal fun Project.libraries() = playground().libraries
internal val Project.libs get() = libraries()
internal fun Project.versions() = playground().versions
internal val Project.versions get() = versions()

internal class PlaygroundProperties private constructor(private val project: Project) {

    private val catalog by lazy(project::getVersionsCatalog)

    val libraries by lazy { PlaygroundLibraries(catalog) }
    val versions by lazy { PlaygroundVersions(catalog) }

    val warningsAsErrors
        get() = project.providers.gradleProperty("playground.warningsAsErrors").toBoolean().getOrElse(true)
    val isMinifyEnabled
        get() = project.providers.gradleProperty("playground.isMinifyEnabled").toBoolean().getOrElse(true)
    val isRerunTest
        get() = project.providers.gradleProperty("playground.rerun-tests").isPresent
    val ciUnitTestVariant
        get() = project.providers.gradleProperty("playground.ci-unit-test.variant").orElse("release")
    val unitTestVerboseLogging
        get() = project.providers.gradleProperty("playground.unit-test.verbose").toBoolean().getOrElse(false)
    val unitTestMaxFailures
        get() = project.providers.gradleProperty("playground.unit-test-retry.maxFailures").toInt().getOrElse(10)
    val unitTestMaxRetries
        get() = project.providers.gradleProperty("playground.unit-test-retry.maxRetries").toInt().getOrElse(1)
    val unitTestFailOnPassedAfterRetry
        get() = project.providers.gradleProperty("playground.unit-test-retry.failOnPassedAfterRetry").toBoolean().getOrElse(true)
    val ciLintVariant
        get() = project.providers.gradleProperty("playground.ci-lint.variant").orElse("release")
    val lintWarningsAsErrors
        get() = project.providers.gradleProperty("playground.lint.warningsAsErrors").toBoolean().getOrElse(warningsAsErrors)
    val ciBadgingVariant
        get() = project.providers.gradleProperty("playground.ci-badging.variant").orElse("release")
    val composeCompilerMetrics
        get() = project.providers.gradleProperty("playground.compose.compilerMetrics").isPresent
    val composeCompilerReports
        get() = project.providers.gradleProperty("playground.compose.compilerReports").isPresent

    private fun Provider<String>.toBoolean(): Provider<Boolean> = map(String::toBoolean)
    private fun Provider<String>.toInt(): Provider<Int> = map(String::toInt)

    companion object {
        private const val EXT_KEY = "fr.smarquis.playground.PlaygroundProperties"
        operator fun invoke(project: Project): PlaygroundProperties =
            project.getOrCreateExtra(EXT_KEY, ::PlaygroundProperties)
    }

}

@Suppress("HasPlatformType")
internal class PlaygroundVersions(catalog: VersionCatalog) {

    val compileSdk by catalog
    val kotlin by catalog
    val minSdk by catalog
    val targetSdk by catalog

    @Suppress("unused")
    private operator fun VersionCatalog.getValue(
        thisRef: Any?,
        property: KProperty<*>,
    ) = findVersion(property.name).orElseThrow {
        IllegalStateException("Missing catalog version ${property.name}")
    }

}

@Suppress("HasPlatformType", "PropertyName")
internal class PlaygroundLibraries(catalog: VersionCatalog) {
    val `androidx-compose-foundations` by catalog
    val `androidx-compose-runtime` by catalog
    val `androidx-compose-ui-test-manifest` by catalog
    val `androidx-compose-ui-tooling` by catalog
    val `androidx-compose-ui-tooling-preview` by catalog
    val `android-security-lint` by catalog
    val `android-desugarJdkLibs` by catalog
    val assertk by catalog
    val hilt by catalog
    val `hilt-android` by catalog
    val `hilt-compiler` by catalog
    val junit by catalog
    val `kotlin-test` by catalog
    val `slack-compose-lint` by catalog

    val boms: Set<Provider<MinimalExternalModuleDependency>> by lazy {
        catalog.libraryAliases
            // aliases are normalized: '-', '_' and '.' have been replaced with '.'
            .filter { it.endsWith(".bom") }
            .mapTo(LinkedHashSet()) { catalog.findLibrary(it).get() }
    }

    @Suppress("unused")
    private operator fun VersionCatalog.getValue(
        thisRef: Any?,
        property: KProperty<*>,
    ) = findLibrary(property.name).orElseThrow {
        IllegalStateException("Missing catalog library ${property.name}")
    }
}
