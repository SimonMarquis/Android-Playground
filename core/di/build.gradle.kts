plugins {
    alias(libs.plugins.playground.kotlin.jvm)
    alias(libs.plugins.playground.metro)
    `java-test-fixtures`
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.datetime)
    implementation(libs.metrox.viewmodel)
    testFixturesApi(libs.metro.runtime)
    testFixturesApi(libs.kotlinx.datetime)
    testFixturesApi(libs.junit)
}
