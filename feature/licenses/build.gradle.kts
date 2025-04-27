plugins {
    alias(libs.plugins.playground.android.library)
    alias(libs.plugins.playground.android.compose)
    alias(libs.plugins.playground.screenshots)
    alias(libs.plugins.androidx.navigation)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

dependencies {
    api(projects.domain.licenses)
    implementation(projects.core.di)
    implementation(projects.core.ui)
    implementation(libs.androidx.navigation)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.hilt.android)
    implementation(libs.kotlinx.collections)
    implementation(libs.kotlinx.serialization.json)
    ksp(libs.hilt.compiler)
    ksp(libs.hilt.compiler.android)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.testParameterInjector)
    testImplementation(libs.turbine)
    testImplementation(testFixtures(projects.core.ui))
    testImplementation(testFixtures(projects.domain.licenses))
    testImplementation(testFixtures(projects.core.utils))
}
