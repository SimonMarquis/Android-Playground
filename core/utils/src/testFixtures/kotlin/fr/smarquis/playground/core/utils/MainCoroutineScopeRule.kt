@file:OptIn(ExperimentalCoroutinesApi::class)

package fr.smarquis.playground.core.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.invoke
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Sets the main coroutine dispatcher to the provided [dispatcher].
 * Use it as a JUnit Rule with [StandardCoroutineScopeRule] or [UnconfinedCoroutineScopeRule].
 *
 * @see <a href="https://developer.android.com/kotlin/coroutines/test">Android documentation</a>
 * @see <a href="https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-test/">Kotlin documentation</a>
 */
class MainCoroutineScopeRule(val dispatcher: TestDispatcher) : TestWatcher(),
    CoroutineScope by TestScope(dispatcher) {
    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)
    override fun finished(description: Description) = Dispatchers.resetMain()
}

/** @see [MainCoroutineScopeRule] */
@Suppress("TestFunctionName")
fun StandardCoroutineScopeRule(scope: TestScope? = null): MainCoroutineScopeRule =
    MainCoroutineScopeRule(StandardTestDispatcher(scheduler = scope?.testScheduler))

/** @see [MainCoroutineScopeRule] */
@Suppress("TestFunctionName")
fun UnconfinedCoroutineScopeRule(scope: TestScope? = null): MainCoroutineScopeRule =
    MainCoroutineScopeRule(UnconfinedTestDispatcher(scheduler = scope?.testScheduler))

suspend inline fun <T> TestScope.withPausedDispatcher(
    noinline block: suspend CoroutineScope.() -> T,
): T = StandardTestDispatcher(testScheduler)(block).also { advanceUntilIdle() }
