import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `java-library`
    alias(libs.plugins.android.lint)
    alias(libs.plugins.playground.build)
}

playground {
    jvmLibrary {
        kotlin { compilerOptions.jvmTarget = JvmTarget.JVM_17 }

        dependencies {
            compileOnly(libs.kotlin.stdlib)
            compileOnly(libs.lint.api)
            compileOnly(libs.lint.checks)
            testImplementation(libs.lint)
            testImplementation(libs.lint.tests)
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}


lint {
    disable += "LintImplTrimIndent"
}
