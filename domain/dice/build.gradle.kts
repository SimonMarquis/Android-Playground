plugins {
    alias(libs.plugins.playground.kotlin.jvm)
    `java-test-fixtures`
}

dependencies {
    api(libs.kotlinx.collections)
    api(libs.kotlinx.coroutines.core)
    testFixturesApi(libs.kotlinx.coroutines.core)
}
