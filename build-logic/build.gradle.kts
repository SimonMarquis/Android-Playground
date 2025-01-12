import org.gradle.api.JavaVersion.VERSION_17
import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    alias(libs.plugins.android.lint)
}

group = "fr.smarquis.playground.buildlogic"

java {
    sourceCompatibility = VERSION_17
    targetCompatibility = VERSION_17
}

kotlin {
    compilerOptions {
        allWarningsAsErrors = true
        jvmTarget = JVM_17
    }
    explicitApi()
}


dependencies {
    compileOnly(libs.gradlePlugins.android)
    compileOnly(libs.gradlePlugins.kotlin)
    compileOnly(libs.gradlePlugins.ksp)
    compileOnly(libs.gradlePlugins.compose)
    compileOnly(libs.gradlePlugins.develocity)
    compileOnly(libs.gradlePlugins.dependencyGuard)
    compileOnly(libs.gradlePlugins.hilt)
    compileOnly(libs.gradlePlugins.powerAssert)
    lintChecks(libs.androidx.lint.gradle)
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}

gradlePlugin {
    plugins {
        create("PlaygroundAndroidApplicationPlugin", libs.plugins.playground.android.application)
        create("PlaygroundAndroidComposePlugin", libs.plugins.playground.android.compose)
        create("PlaygroundAndroidLibraryPlugin", libs.plugins.playground.android.library)
        create("PlaygroundBasePlugin", libs.plugins.playground.base)
        create("PlaygroundHiltPlugin", libs.plugins.playground.hilt)
        create("PlaygroundKotlinJvmPlugin", libs.plugins.playground.kotlin.jvm)
        create("PlaygroundKspPlugin", libs.plugins.playground.ksp)
        create("PlaygroundPlatformPlugin", libs.plugins.playground.platform)
        create("PlaygroundRootPlugin", libs.plugins.playground.root)
        create("greeting.GreetingPlugin", libs.plugins.playground.greeting)
    }
}

private fun NamedDomainObjectContainer<PluginDeclaration>.create(
    name: String,
    plugin: Provider<PluginDependency>,
) = register(name) {
    id = plugin.get().pluginId
    implementationClass = "fr.smarquis.playground.buildlogic.$name"
}
