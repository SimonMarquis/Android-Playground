plugins {
    idea
    alias(libs.plugins.playground.android.application)
    alias(libs.plugins.playground.android.compose)
    alias(libs.plugins.playground.hilt)
    alias(libs.plugins.androidx.baselineprofile)
}

android {
    defaultConfig {
        applicationId = "fr.smarquis.testing"
    }
    baselineProfile {
        dexLayoutOptimization = true
    }
}

dependencies {
    baselineProfile(projects.profiling)

    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.profileinstaller)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.splashscreen)
    implementation(libs.kotlinx.datetime)

    implementation(projects.feature.home.impl)
    implementation(projects.feature.licenses.impl)
    implementation(projects.core.android)
    implementation(projects.core.di)
    implementation(projects.core.ui)
    implementation(projects.core.utils)
    implementation(projects.domain.dice)
    implementation(projects.domain.licenses)
    implementation(projects.domain.settings)
    implementation(projects.data.dice)
    implementation(projects.data.licenses)
    implementation(projects.data.settings)

    compileOnly(projects.lint) {
        isTransitive = false
        because(
            """
            Android Lint does not seem to behave as expected when executed on isolated JVM modules. 🤦
            The workaround is to add them to an Android module's dependency graph and enable `checkDependencies`.
            """.trimIndent()
        )
    }
}

idea {
    module {
        // Exclude baseline profiles: **/generated/baselineProfiles/*-prof.txt
        excludeDirs.add(file("src/release/generated"))
    }
}
