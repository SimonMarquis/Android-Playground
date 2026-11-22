package fr.smarquis.playground.buildlogic.utils

import org.gradle.api.GradleException
import org.gradle.api.Project
import java.util.Properties

internal fun Project.versionProperties() = isolated.rootProject.projectDirectory.file("version.properties")

private val Properties.major: String get() = getProperty("major")
private val Properties.minor: String get() = getProperty("minor")
private val Properties.patch: String get() = getProperty("patch")

/**
 * Version code is currently expected to follow this format:
 * - major: [0, ∞]
 * - minor: [0, 999]
 * - patch: [0, 999]
 *
 * And concatenated together with this convention:
 * ```
 * 'aabbbccc'
 *  └┤└└┤└└┴─patch
 *   │  └──minor
 *   └───major
 * ```
 *
 * Example: 1.2.3 -> 1002003
 */
internal fun Properties.computeVersionCode(): Int {
    fun Int.checkRange(range: IntRange, name: String): Unit =
        if (this !in range) throw GradleException("Invalid $name value. Expected $range.to, got $this") else Unit
    val major = major.toInt().also { it.checkRange(0..Integer.MAX_VALUE, "major") }
    val minor = minor.toInt().also { it.checkRange(0..999, "minor") }
    val patch = patch.toInt().also { it.checkRange(0..999, "patch") }
    return major * 1_000_000 + minor * 1_000 + patch
}

internal fun Properties.computeVersionName(): String = """$major.$minor.$patch"""
