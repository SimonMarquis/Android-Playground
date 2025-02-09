plugins {
    id("idea")
    alias(libs.plugins.playground.root)
    alias(libs.plugins.playground.greeting)
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.lint) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.android.settings) apply false
    alias(libs.plugins.android.cacheFix) apply false
    alias(libs.plugins.androidx.baselineprofile) apply false
    alias(libs.plugins.androidx.navigation) apply false
    alias(libs.plugins.binaryCompatibilityValidator) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlin.powerAssert) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.dependencyGuard) apply false
}

idea {
    module {
        excludeDirs.add(isolated.rootProject.projectDirectory.dir(".config").file("lint-baseline.xml").asFile)
    }
}

greeting {
    who = "Android"
}
