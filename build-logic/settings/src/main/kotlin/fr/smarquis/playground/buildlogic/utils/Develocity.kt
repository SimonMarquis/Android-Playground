package fr.smarquis.playground.buildlogic.utils

import com.gradle.develocity.agent.gradle.DevelocityConfiguration
import org.gradle.api.initialization.Settings

internal object Develocity {
    fun configure(settings: Settings) = with(settings) {
        extensions.configure(DevelocityConfiguration::class.java) {
            buildScan {
                termsOfUseUrl.set("https://gradle.com/terms-of-service")
                termsOfUseAgree.set("yes")
                val isCi = providers.environmentVariable("CI").map(String::toBoolean)
                publishing.onlyIf { isCi.getOrElse(false) }
                uploadInBackground.set(isCi)
                obfuscation {
                    ipAddresses { it.map { "0.0.0.0" } }
                }
            }
        }
    }
}
