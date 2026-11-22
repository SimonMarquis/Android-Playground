package fr.smarquis.playground.buildlogic

import org.gradle.api.initialization.Settings

internal fun Settings.configureFeaturePreviews() {
    // https://docs.gradle.org/current/userguide/configuration_cache.html#config_cache:stable
    enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
    // https://docs.gradle.org/9.6.1/release-notes.html#opt-into-gradle-10-behavior-by-disabling-implicit-lookup-in-parent-projects
    enableFeaturePreview("NO_IMPLICIT_LOOKUP_IN_PARENT_PROJECTS")
}
