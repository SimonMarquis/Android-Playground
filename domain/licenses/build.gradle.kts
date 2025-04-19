plugins {
    alias(libs.plugins.playground.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    `java-test-fixtures`
}

dependencies {
    api(libs.kotlinx.collections)
    api(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    testFixturesApi(libs.kotlinx.coroutines.core)
}
