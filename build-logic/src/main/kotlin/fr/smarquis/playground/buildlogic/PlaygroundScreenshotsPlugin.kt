package fr.smarquis.playground.buildlogic

import app.cash.paparazzi.gradle.PaparazziPlugin
import fr.smarquis.playground.buildlogic.dsl.apply
import fr.smarquis.playground.buildlogic.utils.PlaygroundGlobalCi
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.language.base.plugins.LifecycleBasePlugin.VERIFICATION_GROUP

internal class PlaygroundScreenshotsPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            apply<PaparazziPlugin>()
            val screenshotTestingVariant = playground().screenshotTestingVariant.map { it.capitalized() }
            tasks.register("recordScreenshots") {
                description = "Record screenshot tests golden images."
                dependsOn("recordPaparazzi${screenshotTestingVariant.get()}")
            }
            tasks.register("cleanRecordScreenshots") {
                description = "Clean and record screenshot tests golden images."
                dependsOn("cleanRecordPaparazzi${screenshotTestingVariant.get()}")
            }
            tasks.register("verifyScreenshots") {
                group = VERIFICATION_GROUP
                description = "Verify screenshot tests images against golden images."
                dependsOn("verifyPaparazzi${screenshotTestingVariant.get()}")
            }.also { PlaygroundGlobalCi.addToGlobalCi(project, it) }
        }
    }

}
