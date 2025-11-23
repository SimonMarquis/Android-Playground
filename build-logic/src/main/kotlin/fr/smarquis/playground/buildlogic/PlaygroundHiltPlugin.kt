package fr.smarquis.playground.buildlogic

import com.google.devtools.ksp.gradle.KspExtension
import dagger.hilt.android.plugin.HiltExtension
import dagger.hilt.android.plugin.HiltGradlePlugin
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
            apply<HiltGradlePlugin>()
            extensions.configure<HiltExtension> {
                enableAggregatingTask = true
            }
            extensions.configure<KspExtension> {
                // https://dagger.dev/dev-guide/compiler-options.html#fastinit-mode
                arg(k = "dagger.fastInit", v = "ENABLED") // default value with Hilt
                // https://dagger.dev/dev-guide/compiler-options#ignore-provision-key-wildcards
                arg(k = "dagger.ignoreProvisionKeyWildcards", v = "ENABLED")
                // https://dagger.dev/dev-guide/compiler-options#useBindingGraphFix
                arg(k = "dagger.useBindingGraphFix", v = "ENABLED")
                // https://www.zacsweers.dev/dagger-party-tricks-refactoring/
                arg(k = "dagger.warnIfInjectionFactoryNotGeneratedUpstream", v = "ENABLED")
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
