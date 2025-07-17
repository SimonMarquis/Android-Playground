package fr.smarquis.playground.domain.dice

public enum class Dice(
    public val emoji: String,
) {
    ONE("⚀"),
    TWO("⚁"),
    THREE("⚂"),
    FOUR("⚃"),
    FIVE("⚄"),
    SIX("⚅"),
    ;

    public companion object {
        public val DEFAULT: Dice = ONE
    }
}
