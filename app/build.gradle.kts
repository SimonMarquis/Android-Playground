plugins {
    idea
    alias(libs.plugins.playground.android.application)
    alias(libs.plugins.playground.android.compose)
    alias(libs.plugins.playground.hilt)
    alias(libs.plugins.androidx.baselineprofile)
}

android {
    defaultConfig {
        applicationId = "fr.smarquis.playground"
        versionCode = 1
        versionName = "1.0"
    }
    baselineProfile {
        dexLayoutOptimization = true
    }
}

dependencies {
    baselineProfile(projects.profiling)

    implementation(libs.androidx.core)
    implementation(libs.androidx.datastore)
    implementation(libs.androidx.navigation)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.profileinstaller)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.splashscreen)
    implementation(libs.kotlinx.datetime)

    implementation(projects.feature.home)
    implementation(projects.feature.licenses)
    implementation(projects.core.android)
    implementation(projects.core.di)
    implementation(projects.core.ui)
    implementation(projects.domain.dice)
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
