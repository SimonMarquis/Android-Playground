package fr.smarquis.playground.domain.dice

import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow

public interface DiceSource {
    public val rolls: Flow<ImmutableList<Dice>>
    public suspend fun roll(dice: Dice)
    public suspend fun reset()
}
