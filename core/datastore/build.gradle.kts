plugins {
    alias(libs.plugins.playground.build)
}

playground {
    jvmLibrary {
        metro()
        testFixtures()

        dependencies {
            api(libs.androidx.datastore.core)
            implementation(libs.kotlinx.coroutines.core)
            implementation(project(":core:di"))
            testFixturesApi(libs.androidx.datastore.core)
        }
    }
}
