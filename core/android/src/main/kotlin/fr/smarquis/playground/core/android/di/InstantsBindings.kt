package fr.smarquis.playground.core.android.di

import android.content.pm.PackageInfo
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import fr.smarquis.playground.core.di.qualifier.FirstInstallTime
import fr.smarquis.playground.core.di.qualifier.LastUpdateTime
import kotlin.time.Instant

@ContributesTo(AppScope::class)
@BindingContainer
public object InstantsBindings {
    @Provides
    @FirstInstallTime
    internal fun providesFirstInstallTime(pi: PackageInfo): Instant = Instant.fromEpochMilliseconds(pi.firstInstallTime)

    @Provides
    @LastUpdateTime
    internal fun providesLastUpdateTime(pi: PackageInfo): Instant = Instant.fromEpochMilliseconds(pi.lastUpdateTime)
}
