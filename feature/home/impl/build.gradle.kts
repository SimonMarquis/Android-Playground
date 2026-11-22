playground {
    androidLibrary {
        compose()
        metro()
        screenshots()
        serialization()

        dependencies {
            api(project(":domain:dice"))
            api(project(":domain:settings"))
            api(project(":feature:home"))
            implementation(project(":core:di"))
            implementation(project(":core:ui"))
            implementation(project(":core:utils"))
            implementation(project(":feature:licenses"))
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.compose.material.icons)
            implementation(libs.androidx.navigation3.runtime)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.metrox.viewmodel.compose)
            testImplementation(libs.kotlinx.coroutines.test)
            testImplementation(libs.testParameterInjector)
            testImplementation(libs.turbine)
            testImplementation(testFixtures(project(":core:ui")))
            testImplementation(testFixtures(project(":core:utils")))
            testImplementation(testFixtures(project(":domain:dice")))
            testImplementation(testFixtures(project(":domain:settings")))
        }
    }
}
