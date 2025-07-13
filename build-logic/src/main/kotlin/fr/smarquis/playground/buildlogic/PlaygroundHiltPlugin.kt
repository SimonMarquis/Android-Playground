package fr.smarquis.playground.buildlogic

import com.google.devtools.ksp.gradle.KspExtension
import dagger.hilt.android.plugin.HiltExtension
import fr.smarquis.playground.buildlogic.dsl.apply
import fr.smarquis.playground.buildlogic.dsl.configure
import fr.smarquis.playground.buildlogic.dsl.withType
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile

internal class PlaygroundHiltPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        apply<PlaygroundKspPlugin>()

        pluginManager.withPlugin("com.android.base") {
            apply(plugin = "com.google.dagger.hilt.android")
            extensions.configure<HiltExtension> {
                enableAggregatingTask = true
            }
            extensions.configure<KspExtension> {
                // https://dagger.dev/dev-guide/compiler-options.html#fastinit-mode
                arg("dagger.fastInit", "ENABLED") // default value with Hilt
                // https://dagger.dev/dev-guide/compiler-options#ignore-provision-key-wildcards
                arg("dagger.ignoreProvisionKeyWildcards", "ENABLED")
                // https://dagger.dev/dev-guide/compiler-options#useBindingGraphFix
                arg("dagger.useBindingGraphFix", "ENABLED")
                // https://www.zacsweers.dev/dagger-party-tricks-refactoring/
                arg("dagger.warnIfInjectionFactoryNotGeneratedUpstream", "ENABLED")
            }
        }

        dependencies.add("implementation", libs.hilt)
        dependencies.add("ksp", libs.`hilt-compiler`)
        if (isAndroid) dependencies.add("implementation", libs.`hilt-android`)

        tasks.withType<JavaCompile>().configureEach {
            if (!name.startsWith("hiltJavaCompile")) return@configureEach
            options.isDeprecation = true // -Xlint:deprecation
        }
    }

}
