package fr.smarquis.playground.buildlogic

import fr.smarquis.playground.buildlogic.dsl.apply
import org.gradle.api.Plugin
import org.gradle.api.Project

internal class PlaygroundKspPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        apply<com.google.devtools.ksp.gradle.KspGradleSubplugin>()
    }

}
