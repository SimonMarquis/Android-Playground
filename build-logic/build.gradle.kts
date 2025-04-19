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
        allWarningsAsErrors = providers.gradleProperty("playground.warningsAsErrors").map(String::toBoolean).getOrElse(true)
        jvmTarget = JVM_17
    }
    explicitApi()
}


dependencies {
    compileOnly(plugin(libs.plugins.android.application))
    compileOnly(plugin(libs.plugins.android.library))
    compileOnly(plugin(libs.plugins.develocity))
    compileOnly(plugin(libs.plugins.hilt))
    compileOnly(plugin(libs.plugins.kotlin.compose))
    compileOnly(plugin(libs.plugins.kotlin.jvm))
    compileOnly(plugin(libs.plugins.kotlin.powerAssert))
    compileOnly(plugin(libs.plugins.ksp))
    compileOnly(plugin(libs.plugins.licensee))
    implementation(plugin(libs.plugins.artifactsSizeReport))
    implementation(libs.android.tools.common)
    implementation(libs.assertk)
    lintChecks(libs.androidx.lint.gradle)
}

private fun DependencyHandlerScope.plugin(plugin: Provider<PluginDependency>) = plugin.map {
    "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version.requiredVersion}"
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
        create("PlaygroundAndroidTestPlugin", libs.plugins.playground.android.test)
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
