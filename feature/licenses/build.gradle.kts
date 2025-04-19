plugins {
    alias(libs.plugins.playground.android.library)
    alias(libs.plugins.playground.android.compose)
    alias(libs.plugins.androidx.navigation)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

dependencies {
    implementation(projects.core.di)
    implementation(projects.core.ui)
    implementation(projects.domain.licenses)
    implementation(projects.domain.settings)
    implementation(libs.androidx.navigation)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.hilt.android)
    implementation(libs.kotlinx.collections)
    implementation(libs.kotlinx.serialization.json)
    ksp(libs.hilt.compiler)
    ksp(libs.hilt.compiler.android)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(testFixtures(projects.domain.licenses))
    testImplementation(testFixtures(projects.core.utils))
}
