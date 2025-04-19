plugins {
    alias(libs.plugins.playground.kotlin.jvm)
    alias(libs.plugins.playground.hilt)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(projects.core.di)
    implementation(projects.domain.licenses)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(testFixtures(projects.core.di))
    testImplementation(testFixtures(projects.core.utils))
}
