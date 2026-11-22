@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package fr.smarquis.playground.buildlogic

import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.abi.KotlinAbiDumpTaskImpl

/**
 * Supplies AGP release class directories to KGP's ABI dump task.
 * Remove when https://issuetracker.google.com/issues/470109449 is fixed.
 */
internal fun Project.configureAndroidAbiWorkaround() {
    tasks.withType(KotlinAbiDumpTaskImpl::class.java).configureEach {
        val releaseKotlin = project.tasks.named("compileReleaseKotlin", KotlinCompile::class.java)
        val releaseJava = project.tasks.named("compileReleaseJavaWithJavac", JavaCompile::class.java)
        jvm.set(
            listOf(
                KotlinAbiDumpTaskImpl.JvmTargetInfo(
                    "",
                    project.files(
                        releaseKotlin.flatMap { it.destinationDirectory },
                        releaseJava.flatMap { it.destinationDirectory },
                    ),
                ),
            ),
        )
        dependsOn(releaseKotlin, releaseJava)
    }
}
