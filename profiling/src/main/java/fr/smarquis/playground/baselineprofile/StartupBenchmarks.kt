package fr.smarquis.playground.baselineprofile

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.CompilationMode.None
import androidx.benchmark.macro.CompilationMode.Partial
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
internal class StartupBenchmarks {

    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun none() = benchmark(compilationMode = None())

    @Test
    fun baselineProfiles() = benchmark(compilationMode = Partial(BaselineProfileMode.Require))

    private fun benchmark(compilationMode: CompilationMode) = rule.measureRepeated(
        packageName = targetAppId(),
        metrics = listOf(StartupTimingMetric()),
        compilationMode = compilationMode,
        startupMode = StartupMode.COLD,
        iterations = 10,
        setupBlock = MacrobenchmarkScope::pressHome,
        measureBlock = MacrobenchmarkScope::startActivityAndWait,
    )
}
