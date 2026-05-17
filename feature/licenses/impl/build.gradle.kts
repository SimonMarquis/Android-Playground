plugins {
    alias(libs.plugins.playground.android.library)
    alias(libs.plugins.playground.android.compose)
    alias(libs.plugins.playground.screenshots)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.playground.metro)
}

dependencies {
    api(projects.domain.licenses)
    api(projects.feature.licenses)
    implementation(projects.core.di)
    implementation(projects.core.ui)
    implementation(projects.core.utils)
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.kotlinx.collections)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.metrox.viewmodel.compose)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.testParameterInjector)
    testImplementation(libs.turbine)
    testImplementation(testFixtures(projects.core.ui))
    testImplementation(testFixtures(projects.domain.licenses))
    testImplementation(testFixtures(projects.core.utils))
}
