tasks.register("ciLint") {
    dependsOn(":project:lint", ":settings:lint")
}

// Check AGP & Android Tools versions mismatch
// https://googlesamples.github.io/android-custom-lint-rules/api-guide.html#example:samplelintcheckgithubproject/lintversion?
val agp = libs.versions.agp.get()
val tools = libs.versions.android.tools.get()
val (agpMajor, agpMinor, agpPatch) = agp.split(".", "-")
val (toolsMajor, toolsMinor, toolsPatch) = tools.split(".", "-")
if (agpMajor.toInt() + 23 != toolsMajor.toInt() || agpMinor != toolsMinor || agpPatch != toolsPatch) {
    throw GradleException(
        """
        AGP/Tools versions mismatch:
        - AGP:   $agp
        - Tools: $tools
        """.trimIndent(),
    )
}
