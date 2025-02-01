package fr.smarquis.playground.buildlogic

import com.android.build.api.dsl.AndroidResources
import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.BuildFeatures
import com.android.build.api.dsl.BuildType
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.DefaultConfig
import com.android.build.api.dsl.Installation
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.dsl.Lint
import com.android.build.api.dsl.ProductFlavor
import com.android.build.api.dsl.TestExtension
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.android.build.api.variant.TestAndroidComponentsExtension
import com.android.build.gradle.BaseExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinBaseExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension


internal val Project.isMultiplatform: Boolean get() = pluginManager.hasPlugin("org.jetbrains.kotlin.multiplatform")
internal val Project.isJavaPlatform: Boolean get() = pluginManager.hasPlugin("org.gradle.java-platform")
internal val Project.isAndroid: Boolean get() = pluginManager.hasPlugin("com.android.base")
internal val Project.isAndroidApplication: Boolean get() = pluginManager.hasPlugin("com.android.application")
internal val Project.isAndroidLibrary: Boolean get() = pluginManager.hasPlugin("com.android.library")
internal val Project.isAndroidTest: Boolean get() = pluginManager.hasPlugin("com.android.test")

internal val Project.isCi: Boolean
    get() = providers.environmentVariable("CI").map(String::toBoolean).getOrElse(false)

internal fun Project.android(
    configure: CommonExtension<out BuildFeatures, out BuildType, out DefaultConfig, out ProductFlavor, out AndroidResources, out Installation>.() -> Unit,
) = when {
    isAndroidApplication -> androidApplication(configure)
    isAndroidLibrary -> androidLibrary(configure)
    isAndroidTest -> androidTest(configure)
    else -> TODO("Unsupported project $this (isAndroid=$isAndroid)")
}

internal fun Project.androidApplication(configure: ApplicationExtension.() -> Unit) = configure<ApplicationExtension>(configure)
internal fun Project.androidLibrary(configure: LibraryExtension.() -> Unit) = configure<LibraryExtension>(configure)
internal fun Project.androidTest(configure: TestExtension.() -> Unit) = configure<TestExtension>(configure)
internal fun Project.androidBase(configure: BaseExtension.() -> Unit) = configure<BaseExtension>(configure)
internal fun Project.androidComponents(configure: ApplicationAndroidComponentsExtension.() -> Unit) = configure<ApplicationAndroidComponentsExtension>(configure)

internal val Project.androidExtension: AndroidComponentsExtension<*, *, *>
    get() = androidExtensionNullable
        ?: throw IllegalArgumentException("Failed to find any registered Android extension")

internal val Project.androidExtensionNullable: AndroidComponentsExtension<*, *, *>?
    get() = extensions.findByType<LibraryAndroidComponentsExtension>()
        ?: extensions.findByType<ApplicationAndroidComponentsExtension>()
        ?: extensions.findByType<TestAndroidComponentsExtension>()

public fun Project.lint(configure: Lint.() -> Unit): Unit = android { lint { configure() } }

internal fun Project.getVersionsCatalog(): VersionCatalog = runCatching {
    project.extensions.getByType<VersionCatalogsExtension>().named("libs")
}.recoverCatching {
    throw IllegalStateException("No versions catalog found!", it)
}.getOrThrow()

internal fun <T> Project.getOrCreateExtra(
    key: String,
    create: (Project) -> T,
): T = extensions.extraProperties.run {
    @Suppress("UNCHECKED_CAST")
    (if (has(key)) get(key) as? T else null) ?: create(project).also { set(key, it) }
}

internal inline fun <reified T : KotlinBaseExtension> Project.configureKotlin(
    properties: PlaygroundProperties = playground(),
    crossinline configure: T.() -> Unit = {},
) {
    configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    configure<T> {
        when (this) {
            is KotlinAndroidProjectExtension -> compilerOptions
            is KotlinJvmProjectExtension -> compilerOptions
            else -> TODO("Unsupported project extension $this ${T::class}")
        }.apply {
            jvmTarget = JvmTarget.JVM_11
            allWarningsAsErrors = properties.warningsAsErrors
        }
        explicitApi()
        configure()
    }
}
