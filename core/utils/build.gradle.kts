plugins {
    alias(libs.plugins.playground.build)
}

playground {
    jvmLibrary {
        metro()
        testFixtures()

        dependencies {
            api(libs.androidx.navigation3.runtime)
            implementation(libs.kotlinx.coroutines.core)
            implementation(project(":core:di"))
            testFixturesImplementation(libs.kotlinx.coroutines.test)
            testFixturesImplementation(libs.junit)
        }
    }
}
