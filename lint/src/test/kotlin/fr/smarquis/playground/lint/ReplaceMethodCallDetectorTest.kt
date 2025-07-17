package fr.smarquis.playground.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ReplaceMethodCallDetectorTest : LintDetectorTest() {

    override fun getDetector() = ReplaceMethodCallDetector()
    override fun getIssues() = mutableListOf(ReplaceMethodCallDetector.ISSUE)

    @Test
    fun test() = lint()
        .files(
            kotlin(
                """
                fun foo() = Unit
                fun main(): Unit = foo()
                """,
            ),
        )
        .run()
        .expect(
            """
            src/test.kt:3: Warning: The method foo() should not be called! [ReplaceMethodCall]
                            fun main(): Unit = foo()
                                               ~~~~~
            0 errors, 1 warnings
            """.trimIndent(),
        )
        .expectFixDiffs(
            """
            Autofix for src/test.kt line 3: Replace `foo` with `Unit`:
            @@ -3 +3
            -                 fun main(): Unit = foo()
            +                 fun main(): Unit = Unit
            """.trimIndent(),
        )
        .cleanup()

}
