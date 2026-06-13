package fr.smarquis.playground.domain.dice

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.plus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

public class SimpleDiceSource(rolls: PersistentList<Dice>) : DiceSource {
    override val rolls: StateFlow<ImmutableList<Dice>>
        field = MutableStateFlow(rolls)

    override suspend fun roll(dice: Dice) = rolls.update { it + dice }
    override suspend fun reset(): Unit = rolls.update { persistentListOf() }
}
