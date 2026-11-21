plugins {
    alias(libs.plugins.playground.build)
}

playground {
    jvmLibrary {
        metro()
        serialization()

        dependencies {
            api(project(":domain:licenses"))
            implementation(project(":core:di"))
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            testImplementation(libs.kotlinx.coroutines.test)
            testImplementation(libs.turbine)
            testImplementation(testFixtures(project(":core:di")))
            testImplementation(testFixtures(project(":core:utils")))
        }
    }
}
