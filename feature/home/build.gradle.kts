plugins {
    alias(libs.plugins.playground.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(libs.androidx.navigation3.runtime)
}
