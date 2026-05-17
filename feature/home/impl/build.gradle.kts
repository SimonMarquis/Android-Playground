plugins {
    alias(libs.plugins.playground.android.library)
    alias(libs.plugins.playground.android.compose)
    alias(libs.plugins.playground.screenshots)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.playground.metro)
}

dependencies {
    api(projects.domain.dice)
    api(projects.domain.settings)
    api(projects.feature.home)
    implementation(projects.core.di)
    implementation(projects.core.ui)
    implementation(projects.core.utils)
    implementation(projects.feature.licenses)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.metrox.viewmodel.compose)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.testParameterInjector)
    testImplementation(testFixtures(projects.core.ui))
    testImplementation(testFixtures(projects.core.utils))
    testImplementation(testFixtures(projects.domain.dice))
    testImplementation(testFixtures(projects.domain.settings))
}
