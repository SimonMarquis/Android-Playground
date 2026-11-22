package fr.smarquis.playground.buildlogic

import com.gradle.develocity.agent.gradle.DevelocityConfiguration
import org.gradle.api.initialization.Settings

internal fun Settings.configureBuildScan() {
    val isCi = providers.environmentVariable("CI").map(String::toBoolean)
    extensions.configure(DevelocityConfiguration::class.java) {
        buildScan {
            termsOfUseUrl.set("https://gradle.com/terms-of-service")
            termsOfUseAgree.set("yes")
            publishing.onlyIf { isCi.getOrElse(false) }
            uploadInBackground.set(isCi)
            obfuscation {
                ipAddresses { it.map { "0.0.0.0" } }
            }
        }
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
