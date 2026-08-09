package fr.smarquis.playground.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.checks.infrastructure.TestMode
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
internal class CastValidityDetectorTest : LintDetectorTest() {

    override fun getDetector() = CastValidityDetector()
    override fun getIssues() = listOf(CastValidityDetector.IMPOSSIBLE_CAST, CastValidityDetector.UNSAFE_CAST, CastValidityDetector.DEBUG)

    @Test
    fun `compatible-casts`() = lint()
        .files(
            kotlin(
                """
                    interface Printable
                    open class A
                    class B: A(), Printable

                    fun safe(a: A, b: B) {
                        a as A // identity
                        b as A // upcast
                        b as Printable // interface
                    }

                    fun nullsafe(a: A, b: B, p: Printable) {
                        a as? A // identity
                        a as? B // downcast
                        b as? A // upcast
                        a as? Printable // interface (through downcast)
                        b as? Printable // interface
                        p as? A
                        p as? B
                    }
                    """,
            ).indented(),
        )
        .run()
        .expect(
            """
src/Printable.kt:6: Hint: semanticallyEquals [A] [DEBUG]
    a as A // identity
    ~~~~~~
src/Printable.kt:7: Hint: [B] isSubtypeOf [A] [DEBUG]
    b as A // upcast
    ~~~~~~
src/Printable.kt:8: Hint: [B] isSubtypeOf [Printable] [DEBUG]
    b as Printable // interface
    ~~~~~~~~~~~~~~
src/Printable.kt:12: Hint: semanticallyEquals [A] [DEBUG]
    a as? A // identity
    ~~~~~~~
src/Printable.kt:14: Hint: [B] isSubtypeOf [A] [DEBUG]
    b as? A // upcast
    ~~~~~~~
src/Printable.kt:15: Hint: [A] is open/abstract class and [Printable] is interface (safe cast [as?]) [DEBUG]
    a as? Printable // interface (through downcast)
    ~~~~~~~~~~~~~~~
src/Printable.kt:16: Hint: [B] isSubtypeOf [Printable] [DEBUG]
    b as? Printable // interface
    ~~~~~~~~~~~~~~~
0 errors, 0 warnings, 7 hints
            """.trimIndent()
        )
        .cleanup()

    @Test
    fun `unsafe-casts`() = lint()
        .files(
            kotlin(
                """
                interface Printable
                open class A
                class B: A(), Printable

                fun test(a1: A, a2: A, p1: Printable, p2: Printable) {
                    a1 as Printable // interface (through downcast) 
                    a2 as B // downcast
                    p1 as A
                    p2 as B
                }

                fun testNullable(a1: A?, a2: A?, a3: A?, b1: B?, b2: B?) {
                    a1 as A // identity
                    a2 as B // downcast
                    a3 as Printable // interface (through downcast) 
                    b1 as A // upcast
                    b2 as Printable // interface
                }
                """,
            ).indented(),
        )
        .run()
        .expect(
            """
src/Printable.kt:6: Warning: Unsafe cast from A to Printable ([A] is open/abstract class and [Printable] is interface) [UnsafeCast]
    a1 as Printable // interface (through downcast) 
    ~~~~~~~~~~~~~~~
src/Printable.kt:7: Warning: Unsafe cast from A to B [UnsafeCast]
    a2 as B // downcast
    ~~~~~~~
src/Printable.kt:11: Warning: Unsafe cast from A? to A [UnsafeCast]
    a1 as A // identity
    ~~~~~~~
src/Printable.kt:12: Warning: Unsafe cast from A? to B [UnsafeCast]
    a2 as B // downcast
    ~~~~~~~
src/Printable.kt:13: Warning: Unsafe cast from B? to A [UnsafeCast]
    b as A // upcast
    ~~~~~~
src/Printable.kt:17: Warning: Unsafe cast from A? to Printable ([A?] is open/abstract class and [Printable] is interface) [UnsafeCast]
    a as Printable // interface (through downcast) 
    ~~~~~~~~~~~~~~
src/Printable.kt:18: Warning: Unsafe cast from B? to Printable (nullable source; target is interface — any subtype could implement it) [UnsafeCast]
    b as Printable // interface
    ~~~~~~~~~~~~~~
0 errors, 7 warnings
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
                class C
                interface Printable

                fun nonNull(a: A, b: B, c: C, p: Printable) {
                    a as B
                    b as A
                    c as Printable
                    p as A
                }

                fun nullable(a: A?, b: B?, c: C?, p: Printable?) {
                    a as B
                    b as A
                    c as Printable
                    p as A
                }

                fun nullsafe(a: A, b: B, c: C, p: Printable) {
                    a as? B
                    b as? A
                    c as? Printable
                    p as? A
                }
                """,
            ).indented(),
        )
        .run()
        .expect(
            """
src/A.kt:7: Error: Impossible cast from A to B [ImpossibleCast]
    a as B
    ~~~~~~
src/A.kt:8: Error: Impossible cast from B to A [ImpossibleCast]
    b as A
    ~~~~~~
src/A.kt:9: Error: Impossible cast from C to Printable [ImpossibleCast]
    c as Printable
    ~~~~~~~~~~~~~~
src/A.kt:13: Error: Impossible cast from A? to B [ImpossibleCast]
    a as B
    ~~~~~~
src/A.kt:14: Error: Impossible cast from B? to A [ImpossibleCast]
    b as A
    ~~~~~~
src/A.kt:15: Error: Impossible cast from C to Printable [ImpossibleCast]
    c as Printable
    ~~~~~~~~~~~~~~
src/A.kt:19: Error: Impossible cast from A to B [ImpossibleCast]
    a as? B
    ~~~~~~~
src/A.kt:20: Error: Impossible cast from B to A [ImpossibleCast]
    b as? A
    ~~~~~~~
src/A.kt:21: Error: Impossible cast from C to Printable [ImpossibleCast]
    c as? Printable
    ~~~~~~~~~~~~~~~
9 errors
            """.trimIndent(),
        )
        .cleanup()

    @Test
    fun `cast to nullable target`() = lint()
        .files(
            kotlin(
                """
fun testNullableTarget(a: Any, b: Any?, c: String?) {
    a as String?      // CAN succeed (when value is non-null String)
    b as String?      // CAN succeed or return null -- but not impossible
    c as String?      // identity with nullable -- should be clean
}
                """,
            ).indented(),
        )
        .run()
        .expect(
            """
src/test.kt:4: Hint: semanticallyEquals [String?] [DEBUG]
    c as String?      // identity with nullable -- should be clean
    ~~~~~~~~~~~~
src/test.kt:2: Warning: Unsafe cast from Any to String? [UnsafeCast]
    a as String?      // CAN succeed (when value is non-null String)
    ~~~~~~~~~~~~
src/test.kt:3: Warning: Unsafe cast from Any? to String? [UnsafeCast]
    b as String?      // CAN succeed or return null -- but not impossible
    ~~~~~~~~~~~~
0 errors, 2 warnings, 1 hint
            """.trimIndent()
        )
        .cleanup()



    @Test
    fun `cast from Unit literal`() = lint()
        .files(
            kotlin(
                """
fun testUnitCasts(u: Unit, x: Any) {
    u as Any          // Unit -> Any is possible (upcast)
    u as String       // impossible — Unit has exactly one value, never a String

    x as Unit         // from Any down to Unit — unsafe but possible
}
                """,
            ).indented(),
        )
        .run()
        .expect(
            """
src/test.kt:2: Hint: [Unit] isSubtypeOf [Any] [DEBUG]
    u as Any          // Unit -> Any is possible (upcast)
    ~~~~~~~~
src/test.kt:3: Error: Impossible cast from Unit to String [ImpossibleCast]
    u as String       // impossible — Unit has exactly one value, never a String
    ~~~~~~~~~~~
src/test.kt:5: Warning: Unsafe cast from Any to Unit [UnsafeCast]
    x as Unit         // from Any down to Unit — unsafe but possible
    ~~~~~~~~~
1 error, 1 warning, 1 hint
            """.trimIndent()
        )
        .cleanup()

    @Test
    fun `cast from type parameter`() = lint()
        .files(
            kotlin(
                """
                sealed class Step {
                    object First: Step()
                    object Second: Step()
                }
                
                fun step(): List<Step> = Step::class.sealedSubclasses.map { it.objectInstance as Step }
                """,
            ).indented(),
        )
        .run()
        .expect(
            """
src/Step.kt:6: Hint: [out Step] isSubtypeOf(unwrapped) [Step] [DEBUG]
fun step(): List<Step> = Step::class.sealedSubclasses.map { it.objectInstance as Step }
                                                            ~~~~~~~~~~~~~~~~~~~~~~~~~
0 errors, 0 warnings, 1 hint
            """.trimIndent()
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
                    val x = flow as StateFlow<Int> // identity
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
            src/test/test.kt:6: Hint: semanticallyEquals [StateFlow<Int>] [DEBUG]
                val x = flow as StateFlow<Int> // identity
                        ~~~~~~~~~~~~~~~~~~~~~~
            src/test/test.kt:10: Warning: Unsafe cast from StateFlow<Int> to MutableStateFlow<Int> [UnsafeCast]
                val x = flow as MutableStateFlow<Int>
                        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            0 errors, 1 warning, 1 hint
            """.trimIndent(),
        )
        .cleanup()

    @Test
    fun `testing1`() = lint()
        .testModes(TestMode.DEFAULT)
        .files(
            kotlin(
                """
sealed class A
class B : A()
class C : A()

class Box<T>(val value: T)

fun main() {
    val boxes: List<Box<out A>> = listOf(Box(B()), Box(C()))

    boxes.map { box: Box<out A> ->
        box.value as A
    }
}
                    """,
            ).indented(),
        )
        .run()
        .expect(
            """
src/A.kt:11: Hint: [out A] isSubtypeOf [A] [DEBUG]
        box.value as A
        ~~~~~~~~~~~~~~
0 errors, 0 warnings, 1 hint
            """.trimIndent()
        )
        .cleanup()


    @Test
    fun `testing2`() = lint()
        .testModes(TestMode.DEFAULT)
        .files(
            kotlin(
                """
sealed class A
class B : A()

class Foo<T>(val t: T)

fun main() {
    val foo: Foo<out A> = Foo(B())

    foo.t as A
}
                    """,
            ).indented(),
        )
        .run()
        .expect(
            """
src/A.kt:9: Hint: [out A] isSubtypeOf [A] [DEBUG]
    foo.t as A
    ~~~~~~~~~~
0 errors, 0 warnings, 1 hint
            """.trimIndent()
        )
        .cleanup()


    @Test
    fun `testing3`() = lint()
        .testModes(TestMode.DEFAULT)
        .files(
            kotlin(
                """
sealed class A
class B : A()

class Foo<T : Any>(val objectInstance: T?)

fun main() {
    val foo: Foo<out A> = Foo(B())

    foo.objectInstance as A
}
                    """,
            ).indented(),
        )
        .run()
        .expect(
            """
src/A.kt:9: Hint: [out A] isSubtypeOf(unwrapped) [A] [DEBUG]
    foo.objectInstance as A
    ~~~~~~~~~~~~~~~~~~~~~~~
0 errors, 0 warnings, 1 hint
            """.trimIndent()
        )
        .cleanup()


    @Test
    fun `testing viewmodel`() = lint()
        .testModes(TestMode.DEFAULT)
        .files(
            kotlin(
                """
open class ViewModel
class MyViewModel : ViewModel()

fun <T: ViewModel> create(model: Class<T>): T = MyViewModel() as T
                    """,
            ).indented(),
        )
        .run()
        .expectClean()
        .cleanup()


}
