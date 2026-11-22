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
    implementation(plugin(libs.plugins.android.cacheFix))
    compileOnly(plugin(libs.plugins.androidx.baselineprofile))
    implementation(plugin(libs.plugins.artifactsSizeReport))
    compileOnly(plugin(libs.plugins.develocity))
    implementation(plugin(libs.plugins.kotlin.compose))
    implementation(plugin(libs.plugins.kotlin.jvm))
    implementation(plugin(libs.plugins.kotlin.powerAssert))
    implementation(plugin(libs.plugins.licensee))
    implementation(plugin(libs.plugins.paparazzi))
    implementation(plugin(libs.plugins.dagp))
    implementation(plugin(libs.plugins.metro))

    implementation(libs.android.gradle.api)
    compileOnly(libs.android.tools.common)
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
        create("PlaygroundProjectPlugin", libs.plugins.playground.project)
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

lint {
    abortOnError = true
    explainIssues = true
    absolutePaths = true
}
