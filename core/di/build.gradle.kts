playground {
    jvmLibrary {
        metro()
        testFixtures()

        dependencies {
            implementation(libs.kotlinx.coroutines.core)
            testFixturesApi(libs.metro.runtime)
            testFixturesApi(libs.junit)
        }
    }
}
