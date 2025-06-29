pluginManagement {
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
        gradlePluginPortal()
        maven("https://oss.sonatype.org/content/repositories/snapshots") { mavenContent { snapshotsOnly() } }
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
            // NOTE: Copied from /settings.gradle.kts
            providers.systemPropertiesPrefixedBy("libs_version_").get()
                .mapKeys { (key, _) -> key.removePrefix("libs_version_") }
                .also { if (it.isNotEmpty()) logger.lifecycle("ℹ️ Overriding versions $it") }
                .forEach { (key, value) -> version(key, value) }
        }
    }
}

rootProject.name = "build-logic"
