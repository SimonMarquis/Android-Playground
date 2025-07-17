package fr.smarquis.playground.lint

import com.android.tools.lint.checks.infrastructure.TestFile
import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import fr.smarquis.playground.lint.TestMethodBannedWordsDetector.Companion.BANNED_WORDS
import fr.smarquis.playground.lint.TestMethodBannedWordsDetector.Companion.ISSUE
import org.junit.Test

class TestMethodBannedWordsDetectorTest {

    @Test
    fun `no errors`() = lint()
        .files(
            JUNIT_TEST_STUB,
            kotlin(
                """
                import org.junit.Test
                class Test {
                    @Test
                    fun test() = Unit
                }
                """,
            ).indented(),
        )
        .issues(ISSUE)
        .run()
        .expectClean()
        .cleanup()

    @Test
    fun `banned words produces errors`() = lint()
        .files(
            JUNIT_TEST_STUB,
            kotlin(
                """
                import org.junit.Test
                class Test {
                    @Test
                    fun `test has failed`() = Unit
                    @Test
                    fun `test is a failure`() = Unit
                }
                """,
            ).indented(),
        )
        .issues(ISSUE)
        .run()
        .expect(
            """
            src/Test.kt:4: Error: This method uses banned words: [failed] [TestMethodBannedWords]
                fun `test has failed`() = Unit
                    ~~~~~~~~~~~~~~~~~
            src/Test.kt:6: Error: This method uses banned words: [failure] [TestMethodBannedWords]
                fun `test is a failure`() = Unit
                    ~~~~~~~~~~~~~~~~~~~
            2 errors, 0 warnings
            """.trimIndent(),
        )
        .cleanup()

    @Test
    fun `custom banned words`() = lint()
        .configureOption(BANNED_WORDS, "foo,bar")
        .files(
            JUNIT_TEST_STUB,
            kotlin(
                """
                import org.junit.Test
                class Test {
                    @Test
                    fun `test foo`() = Unit
                    @Test
                    fun `test bar`() = Unit
                    @Test
                    fun `test baz`() = Unit
                }
                """,
            ).indented(),
        )
        .issues(ISSUE)
        .run()
        .expect(
            """
            src/Test.kt:4: Error: This method uses banned words: [foo] [TestMethodBannedWords]
                fun `test foo`() = Unit
                    ~~~~~~~~~~
            src/Test.kt:6: Error: This method uses banned words: [bar] [TestMethodBannedWords]
                fun `test bar`() = Unit
                    ~~~~~~~~~~
            2 errors, 0 warnings
            """.trimIndent(),
        )
        .cleanup()

    private companion object {

        private val JUNIT_TEST_STUB: TestFile = kotlin(
            """
                package org.junit
                annotation class Test
                """,
        ).indented()

    }

}
