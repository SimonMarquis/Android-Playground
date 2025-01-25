package fr.smarquis.playground.feature.home

import android.util.DisplayMetrics
import app.cash.turbine.test
import assertk.all
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isSameInstanceAs
import assertk.assertions.startsWith
import fr.smarquis.playground.domain.dice.Dice
import fr.smarquis.playground.domain.dice.SimpleDiceSource
import fr.smarquis.playground.domain.settings.Settings
import fr.smarquis.playground.domain.settings.SimpleSettingsSource
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test

class HomeViewModelTest {

    @Test
    fun `rolls initial value, update and reset`() = runTest {
        /* Given */
        val initial = persistentListOf(Dice.ONE, Dice.TWO, Dice.THREE)
        val vm = viewModel(initialRolls = initial)

        /* When / Then */
        vm.rolls.test {
            assertThat(awaitItem()).isSameInstanceAs(initial)

            vm.roll()
            assertThat(awaitItem()).all {
                startsWith(*initial.toTypedArray())
                hasSize(initial.size + 1)
            }

            vm.reset()
            assertThat(awaitItem()).isEmpty()
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `settings initial value and update`() = runTest {
        /* Given */
        val initial = Settings(strictMode = true, uncaughtExceptionHandler = false)
        val new = Settings(strictMode = false, uncaughtExceptionHandler = true)
        val vm = viewModel(initialSettings = initial)

        /* When / Then */
        vm.settings.test {
            assertThat(awaitItem()).isSameInstanceAs(initial)
            vm.update(new)
            assertThat(awaitItem()).isSameInstanceAs(new)
            ensureAllEventsConsumed()
        }
    }

    private fun viewModel(
        initialRolls: PersistentList<Dice> = persistentListOf(),
        initialSettings: Settings = Settings(),
        now: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
    ) = HomeViewModel(
        diceSource = SimpleDiceSource(initialRolls),
        settingsSource = SimpleSettingsSource(initialSettings),
        data = HomeData(
            packageName = "unit.test",
            versionCode = 1,
            versionName = "1",
            deviceManufacturer = "manufacturer",
            deviceModel = "model",
            deviceProduct = "product",
            deviceSdkInt = 35,
            deviceRelease = "15",
            firstInstallTime = now,
            lastUpdateTime = now,
            currentTime = now,
            displayMetrics = DisplayMetrics(),
        ),
    )

}
