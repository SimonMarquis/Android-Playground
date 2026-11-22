package fr.smarquis.playground.buildlogic

import app.cash.paparazzi.gradle.PaparazziPlugin
import fr.smarquis.playground.buildlogic.dsl.apply
import fr.smarquis.playground.buildlogic.utils.PlaygroundGlobalCi
import org.gradle.api.Project
import org.gradle.language.base.plugins.LifecycleBasePlugin.VERIFICATION_GROUP

internal fun Project.configurePlaygroundScreenshots() {
    apply<PaparazziPlugin>()
    val screenshotTestingVariant = playground().screenshotTestingVariant.map { it.capitalized() }
    tasks.register("recordScreenshots") {
        description = "Record screenshot tests golden images."
        dependsOn(screenshotTestingVariant.map { "recordPaparazzi$it" })
    }
    tasks.register("cleanRecordScreenshots") {
        description = "Clean and record screenshot tests golden images."
        dependsOn(screenshotTestingVariant.map { "cleanRecordPaparazzi$it" })
    }
    tasks.register("verifyScreenshots") {
        group = VERIFICATION_GROUP
        description = "Verify screenshot tests images against golden images."
        dependsOn(screenshotTestingVariant.map { "verifyPaparazzi$it" })
    }.also {
        PlaygroundGlobalCi.addToGlobalCi(project, it)
    }
}
