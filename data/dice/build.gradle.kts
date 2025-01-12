plugins {
    alias(libs.plugins.playground.kotlin.jvm)
    alias(libs.plugins.playground.hilt)
    `java-test-fixtures`
}

dependencies {
    implementation(projects.core.datastore)
    implementation(projects.core.di)
    implementation(projects.domain.dice)
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(testFixtures(projects.core.datastore))
    testImplementation(testFixtures(projects.core.utils))
}
