package fr.smarquis.playground.buildlogic.utils

import org.gradle.api.initialization.Settings

internal object BuildCache {
    fun configure(settings: Settings) = with (settings) {
        buildCache {
            local {
                // NOTE: can be disabled
                // isEnabled = providers.gradleProperty("...").orNull?.toBoolean() ?: true
                // NOTE: or even relocated
                // directory = ...
            }
        }
    }
}
