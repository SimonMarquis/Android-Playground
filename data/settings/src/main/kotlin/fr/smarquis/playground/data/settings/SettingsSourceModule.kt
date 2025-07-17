package fr.smarquis.playground.data.settings

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.smarquis.playground.domain.settings.SettingsSource

@Module
@InstallIn(SingletonComponent::class)
internal interface SettingsSourceModule {
    @Binds
    fun bindsSettingsSource(impl: SettingsSourceImpl): SettingsSource
}
