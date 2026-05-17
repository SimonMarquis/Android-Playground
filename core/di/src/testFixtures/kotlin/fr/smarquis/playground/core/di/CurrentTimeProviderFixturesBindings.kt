package fr.smarquis.playground.core.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import fr.smarquis.playground.core.di.qualifier.CurrentTime
import kotlin.time.Clock
import kotlin.time.Instant

@ContributesTo(AppScope::class)
@BindingContainer
public object CurrentTimeProviderFixturesBindings {
    @Provides @CurrentTime
    internal fun provides(): Instant = Clock.System.now()
}
