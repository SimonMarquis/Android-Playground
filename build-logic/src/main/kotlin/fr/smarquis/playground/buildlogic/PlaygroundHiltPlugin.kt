package fr.smarquis.playground.buildlogic

import dagger.hilt.android.plugin.HiltExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.withType

internal class PlaygroundHiltPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        apply<PlaygroundKspPlugin>()

        pluginManager.withPlugin("com.android.base") {
            apply(plugin = "com.google.dagger.hilt.android")
            extensions.configure<HiltExtension> {
                enableAggregatingTask = true
            }
        }

        val implementation by configurations
        val ksp by configurations
        dependencies {
            implementation(libs.hilt)
            ksp(libs.`hilt-compiler`)
            if (isAndroid) implementation(libs.`hilt-android`)
        }

        tasks.withType<JavaCompile>().configureEach {
            if (!name.startsWith("hiltJavaCompile")) return@configureEach
            options.isDeprecation = true // -Xlint:deprecation
        }
    }

}
