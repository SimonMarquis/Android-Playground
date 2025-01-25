package fr.smarquis.playground.feature.home

import android.util.DisplayMetrics
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isSameInstanceAs
import fr.smarquis.playground.core.utils.StandardCoroutineScopeRule
import fr.smarquis.playground.domain.dice.Dice
import fr.smarquis.playground.domain.dice.Dice.FOUR
import fr.smarquis.playground.domain.dice.Dice.ONE
import fr.smarquis.playground.domain.dice.Dice.THREE
import fr.smarquis.playground.domain.dice.Dice.TWO
import fr.smarquis.playground.domain.dice.DiceRoller
import fr.smarquis.playground.domain.dice.SimpleDiceSource
import fr.smarquis.playground.domain.settings.Settings
import fr.smarquis.playground.domain.settings.SimpleSettingsSource
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.Rule
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val coroutines = StandardCoroutineScopeRule()

    @Test
    fun `rolls initial value, update and reset`() = runTest {
        /* Given */
        val initial = persistentListOf(ONE, TWO, THREE)
        val diceRoller = { FOUR }
        val vm = viewModel(initialRolls = initial, diceRoller = diceRoller)
        advanceUntilIdle()

        /* When / Then */
        vm.rolls.test {
            assertThat(awaitItem()).isSameInstanceAs(initial)

            vm.roll()
            assertThat(awaitItem()).isEqualTo(persistentListOf(ONE, TWO, THREE, FOUR))

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
        advanceUntilIdle()

        /* When / Then */
        vm.settings.test {
            assertThat(awaitItem()).isSameInstanceAs(initial)
            vm.update(new)
            assertThat(awaitItem()).isSameInstanceAs(new)
            ensureAllEventsConsumed()
        }
    }

    private fun viewModel(
        diceRoller: DiceRoller = DiceRoller(Dice.entries::random),
        initialRolls: PersistentList<Dice> = persistentListOf(),
        initialSettings: Settings = Settings(),
        now: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
    ) = HomeViewModel(
        diceRoller = diceRoller,
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
