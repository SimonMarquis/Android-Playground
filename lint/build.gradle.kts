plugins {
    `java-library`
    alias(libs.plugins.android.lint)
    alias(libs.plugins.playground.kotlin.jvm)
}

lint {
    disable += "LintImplTrimIndent"
}

dependencies {
    compileOnly(libs.kotlin.stdlib)
    compileOnly(libs.lint.api)
    compileOnly(libs.lint.checks)
    testImplementation(libs.lint.tests)
}
