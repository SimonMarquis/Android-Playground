package fr.smarquis.playground.core.utils.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import fr.smarquis.playground.core.di.qualifier.CurrentTime
import kotlin.time.Clock
import kotlin.time.Instant

@ContributesTo(AppScope::class)
@BindingContainer
public object InstantsBindings {

    @Provides
    public fun providesClock(): Clock = Clock.System

    @Provides
    @CurrentTime
    public fun providesCurrentTime(clock: Clock): Instant = clock.now()

}
