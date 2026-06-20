plugins {
    alias(libs.plugins.playground.android.library)
    alias(libs.plugins.playground.android.compose)
    alias(libs.plugins.playground.screenshots)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.playground.metro)
}

dependencies {
    api(project(":domain:licenses"))
    api(project(":feature:licenses"))
    implementation(project(":core:di"))
    implementation(project(":core:ui"))
    implementation(project(":core:utils"))
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.kotlinx.collections)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.metrox.viewmodel.compose)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.testParameterInjector)
    testImplementation(libs.turbine)
    testImplementation(testFixtures(project(":core:ui")))
    testImplementation(testFixtures(project(":domain:licenses")))
    testImplementation(testFixtures(project(":core:utils")))
}
