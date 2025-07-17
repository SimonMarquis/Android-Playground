package dagger

import javax.inject.Provider
import kotlin.reflect.KProperty

/**
 * ```kotlin
 * class FooImpl @Inject constructor(
 *     bar: dagger.Lazy<Bar>,
 * ) : Foo {
 *
 *   private val bar: Bar by bar
 *
 *   operator fun invoke() = bar() // instead of bar.get().invoke()
 *
 * }
 * ```
 */
public operator fun <T> Lazy<T>.getValue(thisRef: Any?, property: KProperty<*>): T = get()

/**
 * ```kotlin
 * class FooImpl @Inject constructor(
 *     bar: dagger.Lazy<Bar>,
 * ) : Foo {
 *
 *   private val bar: Bar by bar
 *
 *   operator fun invoke() = bar() // instead of bar.get().invoke()
 *
 * }
 * ```
 */
public operator fun <T> Provider<T>.getValue(thisRef: Any?, property: KProperty<*>): T = get()
