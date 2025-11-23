package fr.smarquis.playground.buildlogic

import com.android.build.gradle.TestPlugin
import fr.smarquis.playground.buildlogic.dsl.apply
import org.gradle.api.Plugin
import org.gradle.api.Project

internal class PlaygroundAndroidTestPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        apply<TestPlugin>()
        apply<PlaygroundAndroidBasePlugin>()
    }

}
