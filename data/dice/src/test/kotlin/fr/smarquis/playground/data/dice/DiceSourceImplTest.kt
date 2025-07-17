package fr.smarquis.playground.data.dice

import androidx.datastore.preferences.core.edit
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import fr.smarquis.playground.core.datastore.InMemoryDataStore
import fr.smarquis.playground.core.utils.StandardCoroutineScopeRule
import fr.smarquis.playground.domain.dice.Dice
import fr.smarquis.playground.domain.dice.Dice.ONE
import fr.smarquis.playground.domain.dice.Dice.TWO
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import kotlin.test.Test

class DiceSourceImplTest {

    @get:Rule
    val coroutines = StandardCoroutineScopeRule()

    @Test
    fun `source initial value is an empty list`() = runTest {
        /* Given */
        val source = DiceSourceImpl(InMemoryDataStore(), coroutines.dispatcher)
        /* Then */
        assertThat(source.rolls.first()).isEmpty()
    }

    @Test
    fun `roll updates source`() = runTest {
        /* Given */
        val source = DiceSourceImpl(InMemoryDataStore(), coroutines.dispatcher)
        /* When */
        Dice.entries.forEach { source.roll(it) }
        /* Then */
        assertThat(source.rolls.first()).isEqualTo(Dice.entries)
    }

    @Test
    fun `reset clears source`() = runTest {
        /* Given */
        val source = DiceSourceImpl(InMemoryDataStore(), coroutines.dispatcher)
        /* When */
        Dice.entries.forEach { source.roll(it) }
        source.reset()
        /* Then */
        assertThat(source.rolls.first()).isEmpty()
    }

    @Test
    fun `corrupted entries are ignored`() = runTest {
        /* Given */
        val datastore = InMemoryDataStore()
        val source = DiceSourceImpl(datastore, coroutines.dispatcher)
        /* When */
        datastore.edit {
            it[RollsPreferenceKey] = byteArrayOf(
                ONE.ordinal.toByte(),
                -1, 100, // Invalid values
                TWO.ordinal.toByte(),
            )
        }
        /* Then */
        assertThat(source.rolls.first()).isEqualTo(persistentListOf(ONE, TWO))
    }

}
