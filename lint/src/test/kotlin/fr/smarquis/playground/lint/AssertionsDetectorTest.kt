package fr.smarquis.playground.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.checks.infrastructure.TestFiles
import com.android.tools.lint.checks.infrastructure.TestMode
import fr.smarquis.playground.lint.AssertionsDetector.Companion.JUNIT_ASSERTION_ISSUE
import fr.smarquis.playground.lint.AssertionsDetector.Companion.KOTLIN_ASSERT_ISSUE
import fr.smarquis.playground.lint.AssertionsDetector.Companion.KOTLIN_EQUALITY_ASSERTION_ISSUE
import fr.smarquis.playground.lint.AssertionsDetector.Companion.KOTLIN_TYPE_ASSERTION_ISSUE
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class AssertionsDetectorTest : LintDetectorTest() {

    override fun getDetector() = AssertionsDetector()
    override fun getIssues() = listOf(
        JUNIT_ASSERTION_ISSUE,
        KOTLIN_ASSERT_ISSUE,
        KOTLIN_TYPE_ASSERTION_ISSUE,
        KOTLIN_EQUALITY_ASSERTION_ISSUE,
    )

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

    @Test
    fun `kotlin type assertions`() = lint()
        .files(
            JUNIT_TEST_STUB,
            KOTLIN_TEST_STUBS,
            kotlin(
                "test/MyTest.kt",
                """
                import org.junit.Test
                import kotlin.test.assertFalse
                import kotlin.test.assertTrue
    
                class MyTest(val any: Any) {
                    @Test
                    fun testAssertTrue() {
                        assertTrue(any is String)
                        assertTrue(actual = any is String)
                        assertTrue(actual = any is String, message = "any is String")
                        assertTrue { any is String }
                        assertTrue(message = "any is String") { any is String }
                        assertTrue(message = "any is String", block = { any is String })
                        assertTrue(message = "any is String", block = { println() ; any is String }) // This complex block can't be easily extracted
                    }
                    @Test
                    fun testAssertFalse() {
                        assertFalse(any is String)
                        assertFalse(actual = any is String)
                        assertFalse(actual = any is String, message = "any is String")
                        assertFalse { any is String }
                        assertFalse(message = "any is String") { any is String }
                        assertFalse(message = "any is String", block = { any is String })
                        assertFalse(message = "any is String", block = { println() ; any is String }) // This complex block can't be easily extracted
                    }
                }
                """.trimIndent(),
            ).indented(),
        )
        .run()
        .expect(
            """
            test/MyTest.kt:8: Error: Replace boolean assertion with assertIs [KotlinTypeAssertion]
                    assertTrue(any is String)
                    ~~~~~~~~~~~~~~~~~~~~~~~~~
            test/MyTest.kt:9: Error: Replace boolean assertion with assertIs [KotlinTypeAssertion]
                    assertTrue(actual = any is String)
                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            test/MyTest.kt:10: Error: Replace boolean assertion with assertIs [KotlinTypeAssertion]
                    assertTrue(actual = any is String, message = "any is String")
                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            test/MyTest.kt:11: Error: Replace boolean assertion with assertIs [KotlinTypeAssertion]
                    assertTrue { any is String }
                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            test/MyTest.kt:12: Error: Replace boolean assertion with assertIs [KotlinTypeAssertion]
                    assertTrue(message = "any is String") { any is String }
                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            test/MyTest.kt:13: Error: Replace boolean assertion with assertIs [KotlinTypeAssertion]
                    assertTrue(message = "any is String", block = { any is String })
                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            test/MyTest.kt:18: Error: Replace boolean assertion with assertIsNot [KotlinTypeAssertion]
                    assertFalse(any is String)
                    ~~~~~~~~~~~~~~~~~~~~~~~~~~
            test/MyTest.kt:19: Error: Replace boolean assertion with assertIsNot [KotlinTypeAssertion]
                    assertFalse(actual = any is String)
                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            test/MyTest.kt:20: Error: Replace boolean assertion with assertIsNot [KotlinTypeAssertion]
                    assertFalse(actual = any is String, message = "any is String")
                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            test/MyTest.kt:21: Error: Replace boolean assertion with assertIsNot [KotlinTypeAssertion]
                    assertFalse { any is String }
                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            test/MyTest.kt:22: Error: Replace boolean assertion with assertIsNot [KotlinTypeAssertion]
                    assertFalse(message = "any is String") { any is String }
                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            test/MyTest.kt:23: Error: Replace boolean assertion with assertIsNot [KotlinTypeAssertion]
                    assertFalse(message = "any is String", block = { any is String })
                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            12 errors, 0 warnings
            """.trimIndent(),
        )
        .expectFixDiffs(
            """
            Autofix for test/MyTest.kt line 8: Replace with assertIs<java.lang.String>(any):
            @@ -3 +3
            + import kotlin.test.assertIs
            @@ -8 +9
            -         assertTrue(any is String)
            +         assertIs<String>(any)
            Autofix for test/MyTest.kt line 9: Replace with assertIs<java.lang.String>(any):
            @@ -3 +3
            + import kotlin.test.assertIs
            @@ -9 +10
            -         assertTrue(actual = any is String)
            +         assertIs<String>(any)
            Autofix for test/MyTest.kt line 10: Replace with assertIs<java.lang.String>(any, message = "any is String"):
            @@ -3 +3
            + import kotlin.test.assertIs
            @@ -10 +11
            -         assertTrue(actual = any is String, message = "any is String")
            +         assertIs<String>(any, message = "any is String")
            Autofix for test/MyTest.kt line 11: Replace with assertIs<java.lang.String>(any):
            @@ -3 +3
            + import kotlin.test.assertIs
            @@ -11 +12
            -         assertTrue { any is String }
            +         assertIs<String>(any)
            Autofix for test/MyTest.kt line 12: Replace with assertIs<java.lang.String>(any, message = "any is String"):
            @@ -3 +3
            + import kotlin.test.assertIs
            @@ -12 +13
            -         assertTrue(message = "any is String") { any is String }
            +         assertIs<String>(any, message = "any is String")
            Autofix for test/MyTest.kt line 13: Replace with assertIs<java.lang.String>(any, message = "any is String"):
            @@ -3 +3
            + import kotlin.test.assertIs
            @@ -13 +14
            -         assertTrue(message = "any is String", block = { any is String })
            +         assertIs<String>(any, message = "any is String")
            Autofix for test/MyTest.kt line 18: Replace with assertIsNot<java.lang.String>(any):
            @@ -3 +3
            + import kotlin.test.assertIsNot
            @@ -18 +19
            -         assertFalse(any is String)
            +         assertIsNot<String>(any)
            Autofix for test/MyTest.kt line 19: Replace with assertIsNot<java.lang.String>(any):
            @@ -3 +3
            + import kotlin.test.assertIsNot
            @@ -19 +20
            -         assertFalse(actual = any is String)
            +         assertIsNot<String>(any)
            Autofix for test/MyTest.kt line 20: Replace with assertIsNot<java.lang.String>(any, message = "any is String"):
            @@ -3 +3
            + import kotlin.test.assertIsNot
            @@ -20 +21
            -         assertFalse(actual = any is String, message = "any is String")
            +         assertIsNot<String>(any, message = "any is String")
            Autofix for test/MyTest.kt line 21: Replace with assertIsNot<java.lang.String>(any):
            @@ -3 +3
            + import kotlin.test.assertIsNot
            @@ -21 +22
            -         assertFalse { any is String }
            +         assertIsNot<String>(any)
            Autofix for test/MyTest.kt line 22: Replace with assertIsNot<java.lang.String>(any, message = "any is String"):
            @@ -3 +3
            + import kotlin.test.assertIsNot
            @@ -22 +23
            -         assertFalse(message = "any is String") { any is String }
            +         assertIsNot<String>(any, message = "any is String")
            Autofix for test/MyTest.kt line 23: Replace with assertIsNot<java.lang.String>(any, message = "any is String"):
            @@ -3 +3
            + import kotlin.test.assertIsNot
            @@ -23 +24
            -         assertFalse(message = "any is String", block = { any is String })
            +         assertIsNot<String>(any, message = "any is String")
            """.trimIndent(),
        )
        .cleanup()


    @Test
    fun `kotlin equality assertions`() = lint()
        .files(
            JUNIT_TEST_STUB,
            KOTLIN_TEST_STUBS,
            kotlin(
                "test/MyTest.kt",
                """
                import org.junit.Test
                import kotlin.test.assertFalse
                import kotlin.test.assertTrue
    
                class MyTest(val a: Any, val b: Any) {
                    @Test
                    fun testAssertTrue() {
                        assertTrue(a == b)
                        assertTrue(a === b)
                        assertTrue(a !== b)
                        assertTrue(actual = a == b)
                        assertTrue(actual = a == b, message = "a == b")
                        assertTrue { a == b }
                        assertTrue(message = "a == b") { a == b }
                        assertTrue(message = "a == b", block = { a == b })
                        assertTrue(message = "a == b", block = { println() ; a == b }) // This complex block can't be easily extracted
                    }
                    @Test
                    fun testAssertFalse() {
                        assertFalse(a == b)
                        assertFalse(a === b)
                        assertFalse(a !== b)
                        assertFalse(actual = a == b)
                        assertFalse(actual = a == b, message = "a == b")
                        assertFalse { a == b }
                        assertFalse(message = "a == b") { a == b }
                        assertFalse(message = "a == b", block = { a == b })
                        assertFalse(message = "a == b", block = { println() ; a == b }) // This complex block can't be easily extracted
                    }
                }
                """,
            ).indented(),
        )


        // STOPSHIP
        .testModes(TestMode.DEFAULT)
        .run()
        .expect(
            """
        test/MyTest.kt:8: Error: Replace boolean assertion with assertEquals [KotlinEqualityAssertion]
                assertTrue(a == b)
                ~~~~~~~~~~~~~~~~~~
        test/MyTest.kt:9: Error: Replace boolean assertion with assertSame [KotlinEqualityAssertion]
                assertTrue(a === b)
                ~~~~~~~~~~~~~~~~~~~
        test/MyTest.kt:10: Error: Replace boolean assertion with assertNotSame [KotlinEqualityAssertion]
                assertTrue(a !== b)
                ~~~~~~~~~~~~~~~~~~~
        test/MyTest.kt:11: Error: Replace boolean assertion with assertEquals [KotlinEqualityAssertion]
                assertTrue(actual = a == b)
                ~~~~~~~~~~~~~~~~~~~~~~~~~~~
        test/MyTest.kt:12: Error: Replace boolean assertion with assertEquals [KotlinEqualityAssertion]
                assertTrue(actual = a == b, message = "a == b")
                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        test/MyTest.kt:13: Error: Replace boolean assertion with assertEquals [KotlinEqualityAssertion]
                assertTrue { a == b }
                ~~~~~~~~~~~~~~~~~~~~~
        test/MyTest.kt:14: Error: Replace boolean assertion with assertEquals [KotlinEqualityAssertion]
                assertTrue(message = "a == b") { a == b }
                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        test/MyTest.kt:15: Error: Replace boolean assertion with assertEquals [KotlinEqualityAssertion]
                assertTrue(message = "a == b", block = { a == b })
                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        test/MyTest.kt:20: Error: Replace boolean assertion with assertNotEquals [KotlinEqualityAssertion]
                assertFalse(a == b)
                ~~~~~~~~~~~~~~~~~~~
        test/MyTest.kt:21: Error: Replace boolean assertion with assertNotSame [KotlinEqualityAssertion]
                assertFalse(a === b)
                ~~~~~~~~~~~~~~~~~~~~
        test/MyTest.kt:22: Error: Replace boolean assertion with assertSame [KotlinEqualityAssertion]
                assertFalse(a !== b)
                ~~~~~~~~~~~~~~~~~~~~
        test/MyTest.kt:23: Error: Replace boolean assertion with assertNotEquals [KotlinEqualityAssertion]
                assertFalse(actual = a == b)
                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        test/MyTest.kt:24: Error: Replace boolean assertion with assertNotEquals [KotlinEqualityAssertion]
                assertFalse(actual = a == b, message = "a == b")
                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        test/MyTest.kt:25: Error: Replace boolean assertion with assertNotEquals [KotlinEqualityAssertion]
                assertFalse { a == b }
                ~~~~~~~~~~~~~~~~~~~~~~
        test/MyTest.kt:26: Error: Replace boolean assertion with assertNotEquals [KotlinEqualityAssertion]
                assertFalse(message = "a == b") { a == b }
                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        test/MyTest.kt:27: Error: Replace boolean assertion with assertNotEquals [KotlinEqualityAssertion]
                assertFalse(message = "a == b", block = { a == b })
                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        16 errors, 0 warnings
        """.trimIndent(),
        )
        .expectFixDiffs(
            """
            Autofix for test/MyTest.kt line 8: Replace with assertEquals(expected = b, actual = a):
            @@ -2 +2
            + import kotlin.test.assertEquals
            @@ -8 +9
            -         assertTrue(a == b)
            +         assertEquals(expected = b, actual = a)
            Autofix for test/MyTest.kt line 9: Replace with assertSame(expected = b, actual = a):
            @@ -3 +3
            + import kotlin.test.assertSame
            @@ -9 +10
            -         assertTrue(a === b)
            +         assertSame(expected = b, actual = a)
            Autofix for test/MyTest.kt line 10: Replace with assertNotSame(expected = b, actual = a):
            @@ -3 +3
            + import kotlin.test.assertNotSame
            @@ -10 +11
            -         assertTrue(a !== b)
            +         assertNotSame(expected = b, actual = a)
            Autofix for test/MyTest.kt line 11: Replace with assertEquals(expected = b, actual = a):
            @@ -2 +2
            + import kotlin.test.assertEquals
            @@ -11 +12
            -         assertTrue(actual = a == b)
            +         assertEquals(expected = b, actual = a)
            Autofix for test/MyTest.kt line 12: Replace with assertEquals(expected = b, actual = a, message = "a == b"):
            @@ -2 +2
            + import kotlin.test.assertEquals
            @@ -12 +13
            -         assertTrue(actual = a == b, message = "a == b")
            +         assertEquals(expected = b, actual = a, message = "a == b")
            Autofix for test/MyTest.kt line 13: Replace with assertEquals(expected = b, actual = a):
            @@ -2 +2
            + import kotlin.test.assertEquals
            @@ -13 +14
            -         assertTrue { a == b }
            +         assertEquals(expected = b, actual = a)
            Autofix for test/MyTest.kt line 14: Replace with assertEquals(expected = b, actual = a, message = "a == b"):
            @@ -2 +2
            + import kotlin.test.assertEquals
            @@ -14 +15
            -         assertTrue(message = "a == b") { a == b }
            +         assertEquals(expected = b, actual = a, message = "a == b")
            Autofix for test/MyTest.kt line 15: Replace with assertEquals(expected = b, actual = a, message = "a == b"):
            @@ -2 +2
            + import kotlin.test.assertEquals
            @@ -15 +16
            -         assertTrue(message = "a == b", block = { a == b })
            +         assertEquals(expected = b, actual = a, message = "a == b")
            Autofix for test/MyTest.kt line 20: Replace with assertNotEquals(expected = b, actual = a):
            @@ -3 +3
            + import kotlin.test.assertNotEquals
            @@ -20 +21
            -         assertFalse(a == b)
            +         assertNotEquals(expected = b, actual = a)
            Autofix for test/MyTest.kt line 21: Replace with assertNotSame(expected = b, actual = a):
            @@ -3 +3
            + import kotlin.test.assertNotSame
            @@ -21 +22
            -         assertFalse(a === b)
            +         assertNotSame(expected = b, actual = a)
            Autofix for test/MyTest.kt line 22: Replace with assertSame(expected = b, actual = a):
            @@ -3 +3
            + import kotlin.test.assertSame
            @@ -22 +23
            -         assertFalse(a !== b)
            +         assertSame(expected = b, actual = a)
            Autofix for test/MyTest.kt line 23: Replace with assertNotEquals(expected = b, actual = a):
            @@ -3 +3
            + import kotlin.test.assertNotEquals
            @@ -23 +24
            -         assertFalse(actual = a == b)
            +         assertNotEquals(expected = b, actual = a)
            Autofix for test/MyTest.kt line 24: Replace with assertNotEquals(expected = b, actual = a, message = "a == b"):
            @@ -3 +3
            + import kotlin.test.assertNotEquals
            @@ -24 +25
            -         assertFalse(actual = a == b, message = "a == b")
            +         assertNotEquals(expected = b, actual = a, message = "a == b")
            Autofix for test/MyTest.kt line 25: Replace with assertNotEquals(expected = b, actual = a):
            @@ -3 +3
            + import kotlin.test.assertNotEquals
            @@ -25 +26
            -         assertFalse { a == b }
            +         assertNotEquals(expected = b, actual = a)
            Autofix for test/MyTest.kt line 26: Replace with assertNotEquals(expected = b, actual = a, message = "a == b"):
            @@ -3 +3
            + import kotlin.test.assertNotEquals
            @@ -26 +27
            -         assertFalse(message = "a == b") { a == b }
            +         assertNotEquals(expected = b, actual = a, message = "a == b")
            Autofix for test/MyTest.kt line 27: Replace with assertNotEquals(expected = b, actual = a, message = "a == b"):
            @@ -3 +3
            + import kotlin.test.assertNotEquals
            @@ -27 +28
            -         assertFalse(message = "a == b", block = { a == b })
            +         assertNotEquals(expected = b, actual = a, message = "a == b")
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
            inline fun assertTrue(message: String? = null, block: () -> Boolean) = Unit
            fun assertFalse(actual: Boolean, message: String? = null) = Unit
            inline fun assertFalse(message: String? = null, block: () -> Boolean) = Unit
            inline fun <reified T> assertIs(value: Any?, message: String? = null): T = TODO()
            inline fun <reified T> assertIsNot(value: Any?, message: String? = null): T = TODO()
            """,
        ).indented()
    }
}
