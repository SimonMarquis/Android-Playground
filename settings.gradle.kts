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
        maven("https://central.sonatype.com/repository/maven-snapshots") { mavenContent { snapshotsOnly() } }
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
        maven("https://central.sonatype.com/repository/maven-snapshots") { mavenContent { snapshotsOnly() } }
    }
    versionCatalogs {
        create("local") {
            from(files("gradle/local.versions.toml"))
        }
    }
}

plugins {
    id("com.gradle.develocity") version "4.6.1"
    id("com.gradle.common-custom-user-data-gradle-plugin") version "2.8.0"
    id("playground.settings")
}

rootProject.name = "Android-Playground"

include(
    ":app",
    ":lint",
    ":feature:home",
    ":feature:home:impl",
    ":feature:licenses",
    ":feature:licenses:impl",
    ":core:android",
    ":core:datastore",
    ":core:di",
    ":core:ui",
    ":core:utils",
    ":domain:dice",
    ":domain:licenses",
    ":domain:settings",
    ":data:dice",
    ":data:licenses",
    ":data:settings",
    ":platform",
    ":profiling",
)
