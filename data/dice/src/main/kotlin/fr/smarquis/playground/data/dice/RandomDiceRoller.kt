package fr.smarquis.playground.data.dice

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import fr.smarquis.playground.domain.dice.Dice
import fr.smarquis.playground.domain.dice.DiceRoller

@ContributesBinding(AppScope::class)
public class RandomDiceRoller : DiceRoller {
    override fun invoke(): Dice = Dice.entries.random()
}
