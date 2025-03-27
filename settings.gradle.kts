import org.gradle.api.JavaVersion.VERSION_17

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        mavenLocal()
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        maven("https://oss.sonatype.org/content/repositories/snapshots") { mavenContent { snapshotsOnly() } }
    }
    versionCatalogs {
        maybeCreate("libs").apply {
            // Override Gradle Catalog versions:
            // ./gradlew --system-prop "libs_version_minCompileSdk=35"
            // ./gradlew -Dlibs_version_minCompileSdk=35
            providers.systemPropertiesPrefixedBy("libs_version_").get()
                .mapKeys { (key, _) -> key.removePrefix("libs_version_") }
                .also { if (it.isNotEmpty()) logger.lifecycle("ℹ️ Overriding versions $it") }
                .forEach { (key, value) -> version(key, value) }
        }
        create("local") {
            from(files("gradle/local.versions.toml"))
        }
    }
}

plugins {
    id("com.gradle.develocity") version "3.19.2"
    id("com.gradle.common-custom-user-data-gradle-plugin") version "2.2"
}

develocity {
    buildScan {
        termsOfUseUrl = "https://gradle.com/terms-of-service"
        termsOfUseAgree = "yes"
        val isCi = providers.environmentVariable("CI").map(String::toBoolean)
        publishing.onlyIf { isCi.getOrElse(false) }
        uploadInBackground = isCi
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

// https://docs.gradle.org/current/userguide/configuration_cache.html#config_cache:stable
enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "Android-Playground"

include(
    ":app",
    ":lint",
    ":feature:home",
    ":core:android",
    ":core:datastore",
    ":core:di",
    ":core:ui",
    ":core:utils",
    ":domain:dice",
    ":domain:settings",
    ":data:dice",
    ":data:settings",
    ":platform",
    ":profiling",
)

with(VERSION_17) {
    check(JavaVersion.current().isCompatibleWith(this)) {
        """
        This build requires JDK $this+ but it is currently using JDK ${JavaVersion.current()}.
        Java Home: [${System.getProperty("java.home")}]
        """.trimIndent()
    }
}

gradle.lifecycle.beforeProject {
    logger.lifecycle("🏗️ Configuring $this")
    apply(plugin = "base")
}

gradle.lifecycle.afterProject {
    if (this == rootProject) return@afterProject
    if (!buildFile.exists()) return@afterProject
    // Ensures our base plugin is applied everywhere
    require(plugins.hasPlugin("playground.base")) {
        val sortedPlugins = plugins.toList().map(Plugin<Any>::toString).sorted()
        "👮 $this is missing base plugin!\n${sortedPlugins.joinToString("\n")}"
    }
}

logger.lifecycle(
    """
    📦 Gradle: ${GradleVersion.current().version}
    📦 Java:   ${JavaVersion.current()}
    📦 Kotlin: ${KotlinVersion.CURRENT}
    """.trimIndent(),
)
