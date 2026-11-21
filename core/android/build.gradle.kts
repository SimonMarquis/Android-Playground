plugins {
    alias(libs.plugins.playground.build)
}

playground {
    androidLibrary {
        metro()

        dependencies {
            implementation(libs.androidx.core)
            implementation(libs.androidx.lifecycle.process)
            implementation(libs.androidx.profileinstaller)
            implementation(libs.kotlinx.coroutines.guava)
            implementation(libs.metrox.viewmodel)
            implementation(project(":core:di"))
        }
    }
}
