plugins {
    alias(libs.plugins.playground.android.library)
    alias(libs.plugins.playground.hilt)
}

dependencies {
    implementation(libs.androidx.core)
    implementation(projects.core.di)
    implementation(projects.core.utils)
}
