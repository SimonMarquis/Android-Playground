plugins {
    alias(libs.plugins.playground.kotlin.jvm)
    alias(libs.plugins.playground.hilt)
    `java-test-fixtures`
}

dependencies {
    api(libs.kotlinx.datetime)
    implementation(libs.kotlinx.coroutines.core)
    implementation(projects.core.di)
    testFixturesImplementation(libs.kotlinx.coroutines.test)
    testFixturesImplementation(libs.junit)
}
