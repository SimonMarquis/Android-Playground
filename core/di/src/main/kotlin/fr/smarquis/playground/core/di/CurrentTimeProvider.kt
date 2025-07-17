package fr.smarquis.playground.core.di

public fun interface CurrentTimeProvider {

    /**
     * @return the difference, measured in milliseconds, between the current time and midnight, January 1, 1970 UTC.
     */
    public fun currentTimeMillis(): Long

    public companion object {
        public val SYSTEM: CurrentTimeProvider = CurrentTimeProvider { System.currentTimeMillis() }
    }

}
