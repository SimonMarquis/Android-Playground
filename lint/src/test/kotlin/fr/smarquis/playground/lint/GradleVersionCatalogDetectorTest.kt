package fr.smarquis.playground.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.checks.infrastructure.TestFiles.toml
import fr.smarquis.playground.lint.GradleVersionCatalogDetector.Companion.BANNED_DEPENDENCY_NAME_REGEX
import fr.smarquis.playground.lint.GradleVersionCatalogDetector.Companion.CATALOG_NAME
import fr.smarquis.playground.lint.GradleVersionCatalogDetector.Companion.DEPENDENCY_NAME
import fr.smarquis.playground.lint.GradleVersionCatalogDetector.Companion.SIMPLIFICATION
import fr.smarquis.playground.lint.GradleVersionCatalogDetector.Companion.SORT
import fr.smarquis.playground.lint.GradleVersionCatalogDetector.Companion.VERSION_INLINING
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
                name = "test",
                toml = """                
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
                name = "libs",
                toml = """
                [versions]
                foo = "1.0.0"
                bar = "1.0.0"
                baz = "1.0.0"
                qux = "1.0.0"
                [libraries]
                foo = "fr.smarquis:foo:1.0.0"
                bar = "fr.smarquis:bar:1.0.0"
                baz = "fr.smarquis:baz:1.0.0"
                qux = "fr.smarquis:qux:1.0.0"
                [plugins]
                foo = "fr.smarquis.foo:1.0.0"
                bar = "fr.smarquis.bar:1.0.0"
                baz = "fr.smarquis.baz:1.0.0"
                qux = "fr.smarquis.qux:1.0.0"
                [bundles]
                foo = ["foo"]
                bar = ["bar"]
                baz = ["baz"]
                qux = ["qux"]
                """.trimIndent(),
            ),
        )
        .run()
        .expect(
            """
            ../gradle/libs.versions.toml:3: Error: Entries are not sorted correctly [GradleVersionCatalogSort]
            bar = "1.0.0"
            ~~~
            ../gradle/libs.versions.toml:4: Error: Entries are not sorted correctly [GradleVersionCatalogSort]
            baz = "1.0.0"
            ~~~
            ../gradle/libs.versions.toml:8: Error: Entries are not sorted correctly [GradleVersionCatalogSort]
            bar = "fr.smarquis:bar:1.0.0"
            ~~~
            ../gradle/libs.versions.toml:9: Error: Entries are not sorted correctly [GradleVersionCatalogSort]
            baz = "fr.smarquis:baz:1.0.0"
            ~~~
            ../gradle/libs.versions.toml:13: Error: Entries are not sorted correctly [GradleVersionCatalogSort]
            bar = "fr.smarquis.bar:1.0.0"
            ~~~
            ../gradle/libs.versions.toml:14: Error: Entries are not sorted correctly [GradleVersionCatalogSort]
            baz = "fr.smarquis.baz:1.0.0"
            ~~~
            ../gradle/libs.versions.toml:18: Error: Entries are not sorted correctly [GradleVersionCatalogSort]
            bar = ["bar"]
            ~~~
            ../gradle/libs.versions.toml:19: Error: Entries are not sorted correctly [GradleVersionCatalogSort]
            baz = ["baz"]
            ~~~
            8 errors, 0 warnings
            """.trimIndent(),
        )
        .cleanup()

    @Test
    fun `VERSION_INLINING issue`() = lint()
        .issues(VERSION_INLINING)
        .files(
            `versions-toml`(
                name = "libs",
                toml = """
                [versions]
                FOO = "1.2.3"
                BAR = "3.2.1"
                OTHER = "1"
                [libraries]
                foo = { module = "fr.smarquis:foo", version.ref = "FOO" }
                other = { module = "fr.smarquis:other", version.ref = "OTHER" }
                [plugins]
                bar = { id = "fr.smarquis.bar", version.ref = "BAR" }
                other = { id = "fr.smarquis.other", version.ref = "OTHER" }
                """.trimIndent(),
            ),
        )
        .run()
        .expect(
            """
            ../gradle/libs.versions.toml:2: Error: Version is used only once, it can be inlined [GradleVersionCatalogVersionInlining]
            FOO = "1.2.3"
            ~~~~~~~~~~~~~
            ../gradle/libs.versions.toml:3: Error: Version is used only once, it can be inlined [GradleVersionCatalogVersionInlining]
            BAR = "3.2.1"
            ~~~~~~~~~~~~~
            ../gradle/libs.versions.toml:6: Error: Version is used only once, it can be inlined [GradleVersionCatalogVersionInlining]
            foo = { module = "fr.smarquis:foo", version.ref = "FOO" }
                                                ~~~~~~~~~~~~~~~~~~~
            ../gradle/libs.versions.toml:9: Error: Version is used only once, it can be inlined [GradleVersionCatalogVersionInlining]
            bar = { id = "fr.smarquis.bar", version.ref = "BAR" }
                                            ~~~~~~~~~~~~~~~~~~~
            4 errors, 0 warnings
            """.trimIndent(),
        )
        .cleanup()

    @Test
    fun `SIMPLIFICATION issue`() = lint()
        .issues(SIMPLIFICATION)
        .files(
            `versions-toml`(
                name = "libs",
                toml = """
                [versions]
                test = "1"
                [libraries]
                foo = { module = "fr.smarquis:foo", version = "1.2.3" }
                bar = { group = "fr.smarquis", name = "bar", version = "1.2.3" }
                baz = { group = "fr.smarquis", name = "baz", version.ref = "test" }
                ignored = { module = "fr.smarquis:ignored", version.ref = "test" }
                [plugins]
                foo = { id = "fr.smarquis.foo", version = "1.2.3" }
                ignored = { id = "fr.smarquis.ignored", version.ref = "test" }
                """.trimIndent(),
            ),
        )
        .run()
        .expect(
            """
            ../gradle/libs.versions.toml:4: Error: Dependency declaration can be simplified [GradleVersionCatalogSimplification]
            foo = { module = "fr.smarquis:foo", version = "1.2.3" }
                   ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            ../gradle/libs.versions.toml:5: Error: Dependency declaration can be simplified [GradleVersionCatalogSimplification]
            bar = { group = "fr.smarquis", name = "bar", version = "1.2.3" }
                   ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            ../gradle/libs.versions.toml:6: Error: Dependency declaration can be simplified [GradleVersionCatalogSimplification]
            baz = { group = "fr.smarquis", name = "baz", version.ref = "test" }
                   ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            ../gradle/libs.versions.toml:9: Error: Dependency declaration can be simplified [GradleVersionCatalogSimplification]
            foo = { id = "fr.smarquis.foo", version = "1.2.3" }
                   ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            4 errors, 0 warnings
            """.trimIndent(),
        )
        .expectFixDiffs(
            """
            Autofix for gradle/libs.versions.toml line 4: Replace library declaration with simpler form.:
            @@ -4 +4
            - foo = { module = "fr.smarquis:foo", version = "1.2.3" }
            + foo = "fr.smarquis:foo:1.2.3"
            Autofix for gradle/libs.versions.toml line 5: Replace library declaration with simpler form.:
            @@ -5 +5
            - bar = { group = "fr.smarquis", name = "bar", version = "1.2.3" }
            + bar = "fr.smarquis:bar:1.2.3"
            Autofix for gradle/libs.versions.toml line 6: Replace library declaration with simpler form.:
            @@ -6 +6
            - baz = { group = "fr.smarquis", name = "baz", version.ref = "test" }
            + baz = { module = "fr.smarquis:baz", version.ref = "test"
            Autofix for gradle/libs.versions.toml line 9: Replace plugin declaration with simpler form.:
            @@ -9 +9
            - foo = { id = "fr.smarquis.foo", version = "1.2.3" }
            + foo = "fr.smarquis.foo:1.2.3"
            """.trimIndent(),
        )
        .cleanup()

}
