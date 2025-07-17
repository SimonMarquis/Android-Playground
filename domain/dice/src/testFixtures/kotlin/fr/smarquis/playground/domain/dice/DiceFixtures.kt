package fr.smarquis.playground.domain.dice

import kotlin.random.Random

fun Random.nextDice(): Dice = Dice.entries.random(this)
