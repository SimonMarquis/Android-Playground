package fr.smarquis.playground.domain.dice

public fun interface DiceRoller {
    public operator fun invoke(): Dice
}
