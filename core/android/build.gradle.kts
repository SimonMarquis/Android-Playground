plugins {
    alias(libs.plugins.playground.android.library)
    alias(libs.plugins.playground.metro)
}

dependencies {
    implementation(libs.androidx.core)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.profileinstaller)
    implementation(libs.kotlinx.coroutines.guava)
    implementation(libs.metrox.viewmodel)
    implementation(projects.core.di)
}
