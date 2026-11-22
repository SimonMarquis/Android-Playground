plugins {
    id("idea")

    alias(libs.plugins.playground.root)
    alias(libs.plugins.playground.greeting)

    alias(libs.plugins.android.cacheFix) apply false
    alias(libs.plugins.androidx.baselineprofile) apply false
    alias(libs.plugins.artifactsSizeReport) apply false
    alias(libs.plugins.develocity) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.powerAssert) apply false
    alias(libs.plugins.licensee) apply false
    alias(libs.plugins.metro) apply false
    alias(libs.plugins.paparazzi) apply false
}

idea {
    module {
        excludeDirs.add(isolated.rootProject.projectDirectory.dir(".config").file("lint-baseline.xml").asFile)
    }
}

greeting {
    who = "Android"
}
