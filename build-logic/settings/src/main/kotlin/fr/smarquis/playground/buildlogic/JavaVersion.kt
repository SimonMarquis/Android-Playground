package fr.smarquis.playground.buildlogic

import org.gradle.api.JavaVersion
import org.gradle.api.initialization.Settings

internal fun Settings.checkJavaVersion() {
    check(JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_17)) {
        """
        This build requires JDK ${JavaVersion.VERSION_17}+ but it is currently using JDK ${JavaVersion.current()}.
        Java Home: [${System.getProperty("java.home")}]
        """.trimIndent()
    }
}
