plugins {
    alias(libs.plugins.playground.build)
}

playground {
    androidLibrary {
        compose()
        testFixtures()

        dependencies {
            api(libs.androidx.material3)
            api(libs.androidx.compose.ui.text)
            implementation(libs.androidx.appcompat)
            implementation(libs.androidx.compose.ui)
            implementation(libs.androidx.splashscreen)
            testFixturesApi(libs.paparazzi)
        }
    }
}
