playground {
    androidLibrary {
        compose()
        metro()
        flowTests()
        screenshots()
        serialization()

        dependencies {
            api(project(":domain:licenses"))
            api(project(":feature:licenses"))
            implementation(project(":core:di"))
            implementation(project(":core:ui"))
            implementation(project(":core:utils"))
            implementation(libs.androidx.compose.material.icons)
            implementation(libs.androidx.navigation3.runtime)
            implementation(libs.kotlinx.collections)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.metrox.viewmodel.compose)
            testImplementation(libs.testParameterInjector)
            testImplementation(testFixtures(project(":core:ui")))
            testImplementation(testFixtures(project(":domain:licenses")))
            testImplementation(testFixtures(project(":core:utils")))
        }
    }
}
