plugins {
    alias(libs.plugins.playground.build)
}

playground {
    jvmLibrary {
        serialization()

        dependencies {
            implementation(libs.androidx.navigation3.runtime)
        }
    }
}
