plugins {
    alias(libs.plugins.playground.kotlin.jvm)
    alias(libs.plugins.playground.hilt)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    api(projects.domain.licenses)
    implementation(projects.core.di)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(testFixtures(projects.core.di))
    testImplementation(testFixtures(projects.core.utils))
}
