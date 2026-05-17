plugins {
    alias(libs.plugins.playground.kotlin.jvm)
    alias(libs.plugins.playground.metro)
    `java-test-fixtures`
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.metrox.viewmodel)
    testFixturesApi(libs.metro.runtime)
    testFixturesApi(libs.junit)
}
