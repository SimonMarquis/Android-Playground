package fr.smarquis.playground.buildlogic.utils

import fr.smarquis.playground.buildlogic.dsl.getByType
import fr.smarquis.playground.buildlogic.isJavaPlatform
import fr.smarquis.playground.buildlogic.libs
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import kotlin.text.RegexOption.IGNORE_CASE

/**
 * Inspired by https://github.com/slackhq/foundry
 */
internal object PlaygroundPlatforms {

    private fun Project.platform() = project(":platform")

    fun logBillOfMaterials(project: Project) {
        val boms = project.libs.boms
        project.logger.lifecycle("📋 ${boms.size} BOMs\n${boms.joinToString("\n") { "- " + it.get().toString() }}")
    }

    fun configurePlatformProject(project: Project) {
        require(project.isJavaPlatform) { "⚠️ Platform project expected, but got $project" }
        for (catalog in project.extensions.getByType<VersionCatalogsExtension>()) {
            project.logger.lifecycle("🔗 ${catalog.libraryAliases.size} api constraints from ${catalog.name} ")
            project.dependencies.constraints {
                for (alias in catalog.libraryAliases) {
                    add("api", catalog.findLibrary(alias).get())
                }
            }
        }
    }

    fun configureProject(project: Project) {
        if (project.isJavaPlatform) return
        val boms = project.libs.boms
        project.configurations.configureEach {
            if (!isPlatformConfigurationName(name)) return@configureEach
            project.dependencies.apply {
                for (bom in boms) add(name, platform(bom))
                add(name, platform(project.platform()))
            }
        }
    }

    private val PLATFORM = setOf(
        "annotationProcessor",
        "api", "compile", "implementation", "runtimeOnly",
        "compileOnly",
        "androidTestUtil",
        "lintChecks", "lintDebug", "lintRelease",
    )

    private val testConfigurationRegex = "(androidTest|unitTest|instrumentedTest|jvmTest|androidUnitTest)".toRegex(IGNORE_CASE)

    /**
     * Best effort fuzzy matching on known configuration names that we want to opt into platforming.
     */
    private  fun isPlatformConfigurationName(name: String): Boolean {
        if (testConfigurationRegex in name && name.endsWith("api", ignoreCase = true)) return false
        // ksp/compileOnly are special cases since they can be combined with others
        if (name.startsWith("ksp", ignoreCase = true)) return true
        if (name == "compileOnly") return true
        // Try trimming the flavor by just matching the suffix
        if (PLATFORM.any { name.endsWith(it, ignoreCase = true) }) return true
        return false
    }

}
