package fr.smarquis.playground.domain.dice

import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.plus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

public class SimpleDiceSource : DiceSource {
    private val mutableRolls = MutableStateFlow(persistentListOf<Dice>())
    override val rolls = mutableRolls
    override suspend fun roll(dice: Dice) = mutableRolls.update { it + dice }
    override suspend fun reset(): Unit = mutableRolls.update { persistentListOf() }
}
