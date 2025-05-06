package fr.smarquis.playground.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.checks.infrastructure.TestMode
import fr.smarquis.playground.lint.NamedParametersDetector.Companion.MISMATCHED_NAMED_PARAMETERS
import fr.smarquis.playground.lint.NamedParametersDetector.Companion.MISSING_NAMED_PARAMETERS
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class NamedParametersDetectorTest : LintDetectorTest() {

    override fun getDetector() = NamedParametersDetector()
    override fun getIssues() = listOf(MISSING_NAMED_PARAMETERS, MISMATCHED_NAMED_PARAMETERS)

    private val KOTLIN_STUBS = kotlin(
        """
        @file:JvmName("Stubs")
        package foo

        fun distinct(int: Int, boolean: Boolean, string: String, any: Any) = Unit
        fun colliding(firstName: String, lastName: String, age: Int) = Unit
        fun collidingWithDefault(firstName: String = "John", lastName: String = "Doe", age: Int = 42) = Unit
        fun <T> vararg(vararg item: T) = Unit
        """,
    ).indented()

    private val KOTLIN_MAPS_STUBS = kotlin(
        """
        package kotlin.collections

        interface Map<K, out V>
        interface MutableMap<K, V> : Map<K, V> {
            fun put(key: K, value: V): V?
        }
        inline operator fun <K, V> MutableMap<K, V>.set(key: K, value: V) = put(key, value)
        """,
    ).indented()

    private val JAVA_STUBS = java(
        "Stubs.java",
        """
        package foo;

        public class Stubs {
            public static void distinct(Integer integer, Boolean bool, String string, Object object) {}
            public static void colliding(String firstName, String lastName, Integer age) {}
        }
        """,
    ).indented()


    @Test
    fun `calling Kotlin from Java`() {
        lint()
            .files(
                KOTLIN_STUBS,
                java(
                    """
                    package foo;

                    public class Foo {
                        public static void main(String[] args) {
                            Stubs.distinct(42, true, "test", new Object());
                            Stubs.colliding("John", "Doe", 42);
                            Stubs.collidingWithDefault("John", "Doe", 42);
                        }
                    }
                    """,
                ).indented(),
            )
            .run()
            .expectClean()
    }

    @Test
    fun `calling Java from Kotlin`() {
        lint()
            .files(
                JAVA_STUBS,
                kotlin(
                    """
                    package foo

                    fun main() {
                        Stubs.distinct(42, true, "test", Any())
                        Stubs.colliding("John", "Doe", 42)
                    }
                    """,
                ).indented(),
            )
            .run()
            .expectClean()
    }

    @Test
    fun `allow-listed methods`() {
        lint()
            // This mode will inject parameter names, but we want to have full control over this.
            .skipTestModes(TestMode.REORDER_ARGUMENTS)
            .files(
                KOTLIN_MAPS_STUBS,
                kotlin(
                    """
                    package foo

                    fun main(map: MutableMap<String, String>) {
                        map.set("key", "value")
                        map.put("key", "value")
                    }
                    """,
                ).indented(),
            )
            .run()
            .expectClean()
    }

    @Test
    fun `expects clean`() {
        lint()
            // This mode will inject parameter names, but we want to have full control over this.
            .skipTestModes(TestMode.REORDER_ARGUMENTS)
            .files(
                KOTLIN_STUBS,
                kotlin(
                    """
                    package foo

                    fun main() {
                        distinct(42, true, "test", Any())
                        colliding(firstName = "John", lastName = "Doe", 42)
                        colliding(firstName = "John", lastName = "Doe", age = 42)
                        collidingWithDefault()
                        collidingWithDefault("first")
                        collidingWithDefault(firstName = "first")
                        collidingWithDefault(firstName = "first", lastName = "last")
                        vararg("foo", "bar", "baz")
                    }
                    """,
                ).indented(),
            )
            .run()
            .expectClean()
    }

    @Test
    fun `expects warnings`() {
        lint()
            // This mode will inject parameter names, but we want to have full control over this.
            .skipTestModes(TestMode.REORDER_ARGUMENTS)
            .files(
                KOTLIN_STUBS,
                kotlin(
                    """
                    package foo

                    fun main() {
                        colliding("a", "b", 42)
                        colliding("a", "b", age = 42)
                        colliding(firstName = "a", "b", 42)
                        collidingWithDefault("first", "last")
                        collidingWithDefault(firstName = "first", "last")
                    }
                    """,
                ).indented(),
            )
            .run()
            .expect(
                """
                src/foo/test2.kt:4: Information: Parameters of the same type should be named [MissingNamedParameters]
                    colliding("a", "b", 42)
                    ~~~~~~~~~~~~~~~~~~~~~~~
                src/foo/test2.kt:5: Information: Parameters of the same type should be named [MissingNamedParameters]
                    colliding("a", "b", age = 42)
                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                src/foo/test2.kt:6: Information: Parameters of the same type should be named [MissingNamedParameters]
                    colliding(firstName = "a", "b", 42)
                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                src/foo/test2.kt:7: Information: Parameters of the same type should be named [MissingNamedParameters]
                    collidingWithDefault("first", "last")
                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                src/foo/test2.kt:8: Information: Parameters of the same type should be named [MissingNamedParameters]
                    collidingWithDefault(firstName = "first", "last")
                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                0 errors, 0 warnings, 5 hints
                """.trimIndent(),
            )
    }

    @Test
    fun `mismatched named parameters`() = lint()
        .issues(MISMATCHED_NAMED_PARAMETERS)
        .files(
            kotlin(
                """    
                fun test(foo: Boolean, bar: Boolean, baz: Boolean = true) = Unit

                @Suppress("BooleanLiteralArgument")
                fun good_inline_values() {
                    test(true, false, true)
                    test(foo = true, bar = false, baz = true)
                    test(baz = true, bar = false, foo = true)
                }

                fun good_local_variables() {
                    val foo = true
                    val bar = false
                    val baz = true
                    test(foo, bar, baz)
                    test(foo = foo, bar = bar, baz = baz)
                    test(baz = baz, bar = bar, foo = foo)
                }

                fun good_parameters(foo: Boolean, bar: Boolean, baz: Boolean) {
                    test(foo, bar, baz)
                    test(foo = foo, bar = bar, baz = baz)
                    test(baz = baz, bar = bar, foo = foo)
                }

                fun bad(foo: Boolean, bar: Boolean, baz: Boolean) {
                    test(baz, foo, bar)
                    test(true, foo, false)
                    test(foo = baz, bar = foo, baz = bar)
                    test(foo = true, bar = foo, baz = false)
                }
                """,
            ).indented(),
        )
        .run()
        .expect(
            """
            src/test.kt:26: Error: This variable has the same name as another parameter! [MismatchedNamedParameters]
                test(baz, foo, bar)
                     ~~~
            src/test.kt:26: Error: This variable has the same name as another parameter! [MismatchedNamedParameters]
                test(baz, foo, bar)
                          ~~~
            src/test.kt:26: Error: This variable has the same name as another parameter! [MismatchedNamedParameters]
                test(baz, foo, bar)
                               ~~~
            src/test.kt:27: Error: This variable has the same name as another parameter! [MismatchedNamedParameters]
                test(true, foo, false)
                           ~~~
            src/test.kt:28: Error: This variable has the same name as another parameter! [MismatchedNamedParameters]
                test(foo = baz, bar = foo, baz = bar)
                           ~~~
            src/test.kt:28: Error: This variable has the same name as another parameter! [MismatchedNamedParameters]
                test(foo = baz, bar = foo, baz = bar)
                                      ~~~
            src/test.kt:28: Error: This variable has the same name as another parameter! [MismatchedNamedParameters]
                test(foo = baz, bar = foo, baz = bar)
                                                 ~~~
            src/test.kt:29: Error: This variable has the same name as another parameter! [MismatchedNamedParameters]
                test(foo = true, bar = foo, baz = false)
                                       ~~~
            8 errors, 0 warnings
            """.trimIndent(),
        )
        .cleanup()

}
