plugins {
    alias(libs.plugins.playground.kotlin.jvm)
    alias(libs.plugins.playground.hilt)
}

dependencies {
    implementation(projects.core.datastore)
    implementation(projects.domain.settings)
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(testFixtures(projects.core.datastore))
    testImplementation(testFixtures(projects.core.utils))
}
