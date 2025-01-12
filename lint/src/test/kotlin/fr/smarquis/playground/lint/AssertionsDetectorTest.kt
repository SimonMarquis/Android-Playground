package fr.smarquis.playground.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.checks.infrastructure.TestFiles
import fr.smarquis.playground.lint.AssertionsDetector.Companion.JUNIT_ASSERTION_ISSUE
import fr.smarquis.playground.lint.AssertionsDetector.Companion.KOTLIN_ASSERT_ISSUE
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class AssertionsDetectorTest : LintDetectorTest() {

    override fun getDetector() = AssertionsDetector()
    override fun getIssues() = listOf(JUNIT_ASSERTION_ISSUE, KOTLIN_ASSERT_ISSUE)

    @Test
    fun `junit assertions`() = lint()
        .files(
            JUNIT_TEST_STUB,
            JUNIT_4_ASSERT_STUB,
            kotlin(
                "test/MyTest.kt",
                """
                import org.junit.Assert.assertEquals
                import org.junit.Assert.assertFalse
                import org.junit.Assert.assertTrue
                import org.junit.Test
    
                class MyTest {
                    @Test
                    fun test() {
                        assertEquals(2, 1 + 1)
                        assertTrue(true)
                        assertFalse(false)
                    }
                }
                """.trimIndent(),
            ),
        )
        .run()
        .expect(
            """
            test/MyTest.kt:9: Warning: Use kotlin.test assertion [JUnitAssertionsUsage]
                    assertEquals(2, 1 + 1)
                    ~~~~~~~~~~~~~~~~~~~~~~
            test/MyTest.kt:10: Warning: Use kotlin.test assertion [JUnitAssertionsUsage]
                    assertTrue(true)
                    ~~~~~~~~~~~~~~~~
            test/MyTest.kt:11: Warning: Use kotlin.test assertion [JUnitAssertionsUsage]
                    assertFalse(false)
                    ~~~~~~~~~~~~~~~~~~
            0 errors, 3 warnings
            """.trimIndent(),
        )
        .cleanup()

    @Test
    fun `kotlin assertions`() = lint()
        .files(
            JUNIT_TEST_STUB,
            KOTLIN_TEST_STUBS,
            kotlin(
                "test/MyTest.kt",
                """
                import kotlin.test.assertEquals
                import kotlin.test.assertFalse
                import kotlin.test.assertTrue
                import org.junit.Test
    
                class MyTest {
                    @Test
                    fun test() {
                        assertEquals(expected = 2, actual = 1 + 1)
                        assertTrue(true)
                        assertFalse(false)
                        assertIs<Any>(Unit)
                    }
                }
                """.trimIndent(),
            ),
        )
        .run()
        .expectClean()
        .cleanup()

    @Test
    fun `kotlin assert in src code`() = lint()
        .files(
            kotlin(
                "src/MyClass.kt",
                """
                class MyClass {
                    init {
                        assert(true)
                        assert(false)
                    }
                }
                """.trimIndent(),
            ),
        )
        .run()
        .expectClean()
        .cleanup()

    @Test
    fun `kotlin assert in test code`() = lint()
        .files(
            JUNIT_TEST_STUB,
            kotlin(
                "test/MyTest.kt",
                """
                import org.junit.Test
    
                class MyTest {
                    @Test
                    fun test() {
                        assert(true)
                        kotlin.assert(true)
                        assert(false)
                        kotlin.assert(false)
                    }
                }
                """.trimIndent(),
            ),
        )
        .run()
        .expect(
            """
            test/MyTest.kt:6: Error: Use kotlin.test assertion [KotlinAssertUsage]
                    assert(true)
                    ~~~~~~~~~~~~
            test/MyTest.kt:7: Error: Use kotlin.test assertion [KotlinAssertUsage]
                    kotlin.assert(true)
                    ~~~~~~~~~~~~~~~~~~~
            test/MyTest.kt:8: Error: Use kotlin.test assertion [KotlinAssertUsage]
                    assert(false)
                    ~~~~~~~~~~~~~
            test/MyTest.kt:9: Error: Use kotlin.test assertion [KotlinAssertUsage]
                    kotlin.assert(false)
                    ~~~~~~~~~~~~~~~~~~~~
            4 errors, 0 warnings
            """.trimIndent(),
        )
        .cleanup()

    companion object {

        private val JUNIT_TEST_STUB = TestFiles.kotlin(
            """
            package org.junit
            annotation class Test
            """,
        ).indented()

        private val JUNIT_4_ASSERT_STUB = TestFiles.java(
            """
            package org.junit;

            public class Assert {
                public static void assertEquals(Object expected, Object actual) {
                }
                public static void assertFalse(boolean condition) {
                }
                public static void assertTrue(boolean condition) {
                }
            }
            """,
        ).indented()

        private val KOTLIN_TEST_STUBS = TestFiles.kotlin(
            """
            package kotlin.test

            fun <T> assertEquals(expected: T, actual: T) = Unit
            fun assertTrue(actual: Boolean, message: String? = null) = Unit
            fun assertFalse(actual: Boolean, message: String? = null) = Unit
            inline fun <reified T> assertIs(value: Any?, message: String? = null): T = TODO()
            """,
        ).indented()
    }
}
