package fr.smarquis.playground.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
internal class CastValidityDetectorTest : LintDetectorTest() {

    override fun getDetector() = CastValidityDetector()
    override fun getIssues() = listOf(CastValidityDetector.IMPOSSIBLE_CAST, CastValidityDetector.UNSAFE_CAST)

    @Test
    fun `compatible-casts`() = lint()
        .files(
            kotlin(
                """
                    class A
                    class B
                    class C: B

                    fun safe(a: A, b: B, c: C) {
                        a as A
                        b as B
                        c as B
                    }

                    fun nullsafe(a: A, b: B, c: C) {
                        a as? A
                        b as? B
                        c as? B
                    }
                    """,
            ).indented(),
        )
        .run()
        .expectClean()
        .cleanup()

    @Test
    fun `unsafe-casts`() = lint()
        .files(
            kotlin(
                """
                    open class A
                    class B: A()

                    fun safe(a: A) {
                        a as B
                    }

                    fun nullsafe(a: A) {
                        a as? B
                    }
                    """,
            ).indented(),
        )
        .run()
        .expect(
            """
                src/A.kt:5: Warning: Unsafe cast from A to B [UnsafeCast]
                    a as B
                    ~~~~~~
                src/A.kt:9: Warning: Unsafe cast from A to B [UnsafeCast]
                    a as? B
                    ~~~~~~~
                0 errors, 2 warnings
                """.trimIndent(),
        )
        .cleanup()

    @Test
    fun `impossible-casts`() = lint()
        .files(
            kotlin(
                """
                class A
                class B

                fun safe(a: A, b: B) {
                    a as B
                    b as A
                }

                fun nullsafe(a: A, b: B) {
                    a as? B
                    b as? A
                }
                """,
            ).indented(),
        )
        .run()
        .expect(
            """
            src/A.kt:5: Error: Impossible cast from A to B [ImpossibleCast]
                a as B
                ~~~~~~
            src/A.kt:6: Error: Impossible cast from B to A [ImpossibleCast]
                b as A
                ~~~~~~
            src/A.kt:10: Error: Impossible cast from A to B [ImpossibleCast]
                a as? B
                ~~~~~~~
            src/A.kt:11: Error: Impossible cast from B to A [ImpossibleCast]
                b as? A
                ~~~~~~~
            4 errors
            """.trimIndent(),
        )
        .cleanup()

    @Test
    fun `generic types`() = lint()
        .files(
            kotlin(
                """
                package kotlinx.coroutines.flow
        
                interface Flow<T>
                interface StateFlow<T> : Flow<T>
                interface MutableStateFlow<T> : StateFlow<T>
                """,
            ).indented(),
            kotlin(
                """
                package test

                import kotlinx.coroutines.flow.*

                fun ok(flow: StateFlow<Int>) {
                    val x = flow as StateFlow<Int>
                }

                fun ko(flow: StateFlow<Int>) {
                    val x = flow as MutableStateFlow<Int>
                }
                """,
            ).indented(),
        )
        .run()
        .expect(
            """
            src/test/test.kt:10: Warning: Unsafe cast from StateFlow<Int> to MutableStateFlow<Int> [UnsafeCast]
                val x = flow as MutableStateFlow<Int>
                        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            0 errors, 1 warning
            """.trimIndent(),
        )
        .cleanup()
}
