import org.gradle.api.JavaVersion.VERSION_21
import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.sam)
    alias(libs.plugins.kotlin.assignment)
    `java-gradle-plugin`
    alias(libs.plugins.android.lint)
}

group = "fr.smarquis.playground.buildlogic"

java {
    sourceCompatibility = VERSION_21
    targetCompatibility = VERSION_21
}

kotlin {
    compilerOptions {
        allWarningsAsErrors = providers.gradleProperty("playground.warningsAsErrors").map(String::toBoolean).getOrElse(true)
        jvmTarget = JVM_21
    }
    explicitApi()
}

samWithReceiver {
    annotation(HasImplicitReceiver::class.qualifiedName!!)
}
assignment {
    annotation(SupportsKotlinAssignmentOverloading::class.qualifiedName!!)
}

dependencies {
    implementation(project(":project"))
    compileOnly(plugin(libs.plugins.develocity))
    lintChecks(libs.androidx.lint.gradle)
}

gradlePlugin {
    plugins {
        create("PlaygroundSettingsPlugin", libs.plugins.playground.settings)
    }
}

private fun NamedDomainObjectContainer<PluginDeclaration>.create(
    name: String,
    plugin: Provider<PluginDependency>,
) = register(name) {
    id = plugin.get().pluginId
    implementationClass = "fr.smarquis.playground.buildlogic.$name"
}

private fun DependencyHandlerScope.plugin(plugin: Provider<PluginDependency>) = plugin.map {
    "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version.requiredVersion}"
}

tasks.validatePlugins {
    enableStricterValidation = true
    failOnWarning = true
}

lint {
    abortOnError = true
    explainIssues = true
    absolutePaths = true
}
