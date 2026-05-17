plugins {
    alias(libs.plugins.playground.kotlin.jvm)
    `java-test-fixtures`
}

dependencies {
    api(libs.androidx.datastore.core)
    api(libs.kotlinx.coroutines.core)
    testFixturesApi(libs.kotlinx.coroutines.core)
}
