package fr.smarquis.playground.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.checks.infrastructure.TestFiles.toml
import fr.smarquis.playground.lint.GradleVersionCatalogDetector.Companion.BANNED_DEPENDENCY_NAME_REGEX
import fr.smarquis.playground.lint.GradleVersionCatalogDetector.Companion.CATALOG_NAME
import fr.smarquis.playground.lint.GradleVersionCatalogDetector.Companion.DEPENDENCY_NAME
import fr.smarquis.playground.lint.GradleVersionCatalogDetector.Companion.SORT
import org.intellij.lang.annotations.Language
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.test.Test

@RunWith(JUnit4::class)
class GradleVersionCatalogDetectorTest : LintDetectorTest() {

    override fun getDetector() = GradleVersionCatalogDetector()
    override fun getIssues() = GradleVersionCatalogDetector.ISSUES.toList()

    private fun `versions-toml`(name: String, @Language("TOML") toml: String) =
        toml(into = "../gradle/${name}.versions.toml", toml)

    @Test
    fun `DEPENDENCY_NAME issue`() = lint()
        .configureOption(CATALOG_NAME, "test.versions.toml")
        .configureOption(BANNED_DEPENDENCY_NAME_REGEX, "ba.?")
        .issues(DEPENDENCY_NAME)
        .files(
            `versions-toml`(
                "test",
                """                
                [libraries]
                foo = "fr.smarquis:foo:1.0.0"
                bar = "fr.smarquis:bar:1.0.0"
                baz = "fr.smarquis:baz:1.0.0"
                qux = "fr.smarquis:qux:1.0.0"
                """.trimIndent(),
            ),
        )
        .run()
        .expect(
            """
            ../gradle/test.versions.toml:3: Error: Dependency name does not follow the expected format [GradleVersionCatalogDependencyName]
            bar = "fr.smarquis:bar:1.0.0"
            ~~~
            ../gradle/test.versions.toml:4: Error: Dependency name does not follow the expected format [GradleVersionCatalogDependencyName]
            baz = "fr.smarquis:baz:1.0.0"
            ~~~
            2 errors, 0 warnings
            """.trimIndent(),
        )
        .cleanup()


    @Test
    fun `SORT issue`() = lint()
        .issues(SORT)
        .files(
            `versions-toml`(
                "libs",
                """
                [libraries]
                foo = "fr.smarquis:foo:1.0.0"
                bar = "fr.smarquis:bar:1.0.0"
                baz = "fr.smarquis:baz:1.0.0"
                qux = "fr.smarquis:qux:1.0.0"
                """.trimIndent(),
            ),
        )
        .run()
        .expect(
            """
            ../gradle/libs.versions.toml:3: Error: Dependencies are not sorted correctly [GradleVersionCatalogSort]
            bar = "fr.smarquis:bar:1.0.0"
            ~~~
            ../gradle/libs.versions.toml:4: Error: Dependencies are not sorted correctly [GradleVersionCatalogSort]
            baz = "fr.smarquis:baz:1.0.0"
            ~~~
            2 errors, 0 warnings
            """.trimIndent(),
        )
        .cleanup()

}
