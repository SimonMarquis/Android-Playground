plugins {
    alias(libs.plugins.playground.android.library)
    alias(libs.plugins.playground.hilt)
}

dependencies {
    implementation(libs.androidx.core)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.profileinstaller)
    implementation(libs.kotlinx.coroutines.guava)
    implementation(projects.core.di)
    implementation(projects.core.utils)
}
