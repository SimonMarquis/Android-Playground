playground {
    jvmLibrary {
        testFixtures()

        dependencies {
            api(libs.kotlinx.coroutines.core)
            testFixturesApi(libs.kotlinx.coroutines.core)
        }
    }
}
