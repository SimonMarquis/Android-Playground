package fr.smarquis.playground.data.dice

import fr.smarquis.playground.domain.dice.Dice
import fr.smarquis.playground.domain.dice.DiceRoller
import javax.inject.Inject

internal class RandomDiceRoller @Inject constructor() : DiceRoller {
    override fun invoke(): Dice = Dice.entries.random()
}
