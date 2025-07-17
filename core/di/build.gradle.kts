plugins {
    alias(libs.plugins.playground.kotlin.jvm)
    alias(libs.plugins.playground.hilt)
    `java-test-fixtures`
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    testFixturesApi(libs.hilt)
    testFixturesApi(libs.junit)
    kspTestFixtures(libs.hilt.compiler)
}
