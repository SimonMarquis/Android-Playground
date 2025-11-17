package fr.smarquis.playground.feature.home

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet
import fr.smarquis.playground.core.utils.navigation.EntryInstaller

@Module
@InstallIn(ActivityRetainedComponent::class)
internal object HomeModule {
    @IntoSet
    @Provides
    fun providesEntry(): EntryInstaller = { backstack -> entry<Home> { HomeScreen(backstack) } }
}

