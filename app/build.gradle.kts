plugins {
    idea
    alias(libs.plugins.playground.android.application)
    alias(libs.plugins.playground.android.compose)
    alias(libs.plugins.playground.metro)
    alias(libs.plugins.androidx.baselineprofile)
}

android {
    defaultConfig {
        applicationId = "fr.smarquis.testing"
    }
}

baselineProfile {
    dexLayoutOptimization = true
}

dependencies {
    baselineProfile(project(":profiling"))

    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.profileinstaller)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.splashscreen)
    implementation(libs.metrox.viewmodel.compose)

    implementation(project(":feature:home:impl"))
    implementation(project(":feature:licenses:impl"))
    implementation(project(":core:android"))
    implementation(project(":core:datastore"))
    implementation(project(":core:di"))
    implementation(project(":core:ui"))
    implementation(project(":core:utils"))
    implementation(project(":domain:dice"))
    implementation(project(":domain:licenses"))
    implementation(project(":domain:settings"))
    implementation(project(":data:dice"))
    implementation(project(":data:licenses"))
    implementation(project(":data:settings"))

    compileOnly(project(":lint")) {
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
