plugins {
    alias(libs.plugins.playground.android.application)
    alias(libs.plugins.playground.android.compose)
    alias(libs.plugins.playground.hilt)
}

android {
    defaultConfig {
        applicationId = "fr.smarquis.playground"
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    lintChecks(projects.lint)

    implementation(libs.androidx.core)
    implementation(libs.androidx.datastore)
    implementation(libs.androidx.navigation)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.splashscreen)
    implementation(libs.kotlinx.datetime)

    implementation(projects.feature.home)
    implementation(projects.core.android)
    implementation(projects.core.di)
    implementation(projects.core.ui)
    implementation(projects.domain.dice)
    implementation(projects.domain.settings)
    implementation(projects.data.dice)
    implementation(projects.data.settings)
}
