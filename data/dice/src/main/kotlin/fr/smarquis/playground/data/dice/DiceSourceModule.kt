package fr.smarquis.playground.data.dice

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.smarquis.playground.domain.dice.DiceRoller
import fr.smarquis.playground.domain.dice.DiceSource

@Module
@InstallIn(SingletonComponent::class)
internal interface DiceSourceModule {
    @Binds
    fun bindsDiceSource(impl: DiceSourceImpl): DiceSource
    @Binds
    fun bindsDiceRoller(impl: RandomDiceRoller): DiceRoller
}
