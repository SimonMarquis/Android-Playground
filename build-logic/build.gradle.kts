import org.gradle.api.JavaVersion.VERSION_17
import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.sam)
    alias(libs.plugins.kotlin.assignment)
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

samWithReceiver {
    annotation(HasImplicitReceiver::class.qualifiedName!!)
}
assignment {
    annotation(SupportsKotlinAssignmentOverloading::class.qualifiedName!!)
}

dependencies {
    compileOnly(plugin(libs.plugins.android.cacheFix))
    compileOnly(plugin(libs.plugins.androidx.baselineprofile))
    compileOnly(plugin(libs.plugins.androidx.navigation))
    compileOnly(plugin(libs.plugins.artifactsSizeReport))
    compileOnly(plugin(libs.plugins.binaryCompatibilityValidator))
    compileOnly(plugin(libs.plugins.develocity))
    compileOnly(plugin(libs.plugins.hilt))
    compileOnly(plugin(libs.plugins.kotlin.android))
    compileOnly(plugin(libs.plugins.kotlin.compose))
    compileOnly(plugin(libs.plugins.kotlin.jvm))
    compileOnly(plugin(libs.plugins.kotlin.powerAssert))
    compileOnly(plugin(libs.plugins.ksp))
    compileOnly(plugin(libs.plugins.licensee))
    compileOnly(plugin(libs.plugins.paparazzi))

    implementation(libs.android.gradle.api)
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
        create("PlaygroundScreenshotsPlugin", libs.plugins.playground.screenshots)
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
    textReport = true
    xmlReport = true
    htmlReport = true
    sarifReport = true
    absolutePaths = true
}

// Check Kotlin/KSP versions mismatch
val kotlin = libs.versions.kotlin.get()
val ksp = libs.plugins.ksp.get().version.toString()
if (kotlin != ksp.split("-").first()) {
    throw GradleException(
        """
        Kotlin/KSP versions mismatch:
        - Kotlin: $kotlin
        - KSP:    $ksp
        """.trimIndent(),
    )
}

// Check AGP & Android Tools versions mismatch
// https://googlesamples.github.io/android-custom-lint-rules/api-guide.html#example:samplelintcheckgithubproject/lintversion?
val agp = libs.versions.agp.get()
val tools = libs.versions.android.tools.get()
val (agpMajor, agpMinor, agpPatch) = agp.split(".", "-")
val (toolsMajor, toolsMinor, toolsPatch) = tools.split(".", "-")
if (agpMajor.toInt() + 23 != toolsMajor.toInt() || agpMinor != toolsMinor || agpPatch != toolsPatch) {
    throw GradleException(
        """
        AGP/Tools versions mismatch:
        - AGP:   $agp
        - Tools: $tools
        """.trimIndent(),
    )
}
