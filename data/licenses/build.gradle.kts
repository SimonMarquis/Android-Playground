playground {
    jvmLibrary {
        metro()
        flowTests()
        serialization()

        dependencies {
            api(project(":domain:licenses"))
            implementation(project(":core:di"))
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            testImplementation(testFixtures(project(":core:di")))
            testImplementation(testFixtures(project(":core:utils")))
        }
    }
}
