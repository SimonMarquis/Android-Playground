plugins {
    alias(libs.plugins.playground.kotlin.jvm)
    alias(libs.plugins.playground.hilt)
}

dependencies {
    api(projects.domain.dice)
    implementation(projects.core.datastore)
    implementation(projects.core.di)
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(testFixtures(projects.core.datastore))
    testImplementation(testFixtures(projects.core.utils))
}
