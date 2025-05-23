plugins {
    alias(libs.plugins.playground.android.library)
    alias(libs.plugins.playground.android.compose)
}

android.testFixtures.enable = true

dependencies {
    api(libs.androidx.material3)
    api(libs.androidx.compose.ui.text)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.splashscreen)
    testFixturesApi(libs.paparazzi)
}
