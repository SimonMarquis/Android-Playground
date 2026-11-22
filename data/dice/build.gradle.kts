playground {
    jvmLibrary {
        metro()

        dependencies {
            api(project(":domain:dice"))
            implementation(project(":core:datastore"))
            implementation(project(":core:di"))
            implementation(libs.kotlinx.coroutines.core)
            testImplementation(libs.kotlinx.coroutines.test)
            testImplementation(testFixtures(project(":core:datastore")))
            testImplementation(testFixtures(project(":core:utils")))
        }
    }
}
