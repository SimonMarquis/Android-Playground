package fr.smarquis.playground.feature.licenses

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import fr.smarquis.playground.core.utils.navigation.EntryInstaller

@ContributesTo(AppScope::class)
@BindingContainer
public object LicensesBindings {
    @IntoSet
    @Provides
    public fun providesEntry(): EntryInstaller = { backstack -> entry<Licenses> { LicensesScreen(backstack) } }
}
