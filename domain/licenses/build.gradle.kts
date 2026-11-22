playground {
    jvmLibrary {
        serialization()
        testFixtures()

        dependencies {
            api(libs.kotlinx.collections)
            api(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            testFixturesApi(libs.kotlinx.coroutines.core)
        }
    }
}
