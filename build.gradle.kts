plugins {
    id("idea")

    alias(libs.plugins.playground.root)
    alias(libs.plugins.playground.greeting)

    alias(libs.plugins.androidx.baselineprofile) apply false
    alias(libs.plugins.develocity) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}

idea {
    module {
        excludeDirs.add(isolated.rootProject.projectDirectory.dir(".config").file("lint-baseline.xml").asFile)
    }
}

greeting {
    who = "Android"
}
