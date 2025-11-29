package fr.smarquis.playground.feature.licenses

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet
import fr.smarquis.playground.core.utils.navigation.EntryInstaller
import fr.smarquis.playground.feature.fr.smarquis.playground.feature.licenses.Licenses

@Module
@InstallIn(ActivityRetainedComponent::class)
internal object LicensesModule {
    @IntoSet
    @Provides
    fun providesEntry(): EntryInstaller = { backstack -> entry<Licenses> { LicensesScreen(backstack) } }
}
