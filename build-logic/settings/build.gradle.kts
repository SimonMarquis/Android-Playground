import org.gradle.api.JavaVersion.VERSION_21
import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.sam)
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

dependencies {
    implementation(project(":project"))
    compileOnly(libs.plugins.develocity.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version.requiredVersion}" })
    lintChecks(libs.androidx.lint.gradle)
}

gradlePlugin {
    plugins {
        register("PlaygroundSettingsPlugin") {
            id = "playground.settings"
            implementationClass = "fr.smarquis.playground.buildlogic.PlaygroundSettingsPlugin"
        }
    }
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
