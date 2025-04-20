plugins {
    alias(libs.plugins.playground.kotlin.jvm)
    alias(libs.plugins.playground.hilt)
    `java-test-fixtures`
}

dependencies {
    api(libs.androidx.datastore.core)
    implementation(libs.kotlinx.coroutines.core)
    implementation(projects.core.di)
    testFixturesApi(libs.androidx.datastore.core)
}
