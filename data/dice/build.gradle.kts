plugins {
    alias(libs.plugins.playground.kotlin.jvm)
    alias(libs.plugins.playground.metro)
}

dependencies {
    api(project(":domain:dice"))
    implementation(project(":core:datastore"))
    implementation(project(":core:di"))
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(testFixtures(project(":core:datastore")))
    testImplementation(testFixtures(project(":core:utils")))
}
