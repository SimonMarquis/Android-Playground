plugins {
    `java-library`
    alias(libs.plugins.android.lint)
    alias(libs.plugins.playground.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}

lint {
    disable += "LintImplTrimIndent"
}

dependencies {
    compileOnly(libs.kotlin.stdlib)
    compileOnly(libs.lint.api)
    compileOnly(libs.lint.checks)
    testImplementation(libs.lint)
    testImplementation(libs.lint.tests)
}
